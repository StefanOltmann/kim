/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcBlock
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPiece
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegmentExif
import de.stefan_oltmann.kim.format.jpeg.xmp.ExtendedXmpWriter
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.copyRemainingTo
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import kotlin.jvm.JvmStatic

/**
 * Interface for Exif write/update/remove functionality for Jpeg/JFIF images.
 */
public object JpegRewriter {

    /*
     * The processing instruction that terminates an XMP packet. The
     * whitespace between the packet content and this marker is editable
     * padding that carries no information.
     */
    private const val XMP_PACKET_END_MARKER = "<?xpacket end="

    /**
     * Inserts the new segments at a place where readers look for
     * metadata: behind the last APP segment if one exists, otherwise in
     * front of the image data - never behind it.
     *
     * For files with existing header segments the position behind the
     * first header segment is kept for byte compatibility with previous
     * releases. A file that consists only of image data gets its
     * metadata directly between SOI and SOS, which is a valid and
     * readable location, so a rewrite never fails for a lack of
     * existing APP segments.
     */
    private fun insertAfterLastAppSegments(
        segments: List<JFIFPiece>,
        newSegments: List<JFIFPiece>
    ): List<JFIFPiece> {

        val lastAppIndex = segments.indexOfLast { piece ->
            piece is JFIFPieceSegment && piece.isAppSegment()
        }

        if (lastAppIndex != -1)
            return segments.toMutableList().apply { addAll(lastAppIndex + 1, newSegments) }

        /*
         * Without an APP segment the new segments go in front of the image
         * data, or to the very front when the list holds only header
         * segments (the streaming path). Both produce the same output.
         */
        val insertIndex = if (segments.isEmpty()) 0 else 1

        return segments.toMutableList().apply { addAll(insertIndex, newSegments) }
    }

    @JvmStatic
    public fun updateExifMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        outputSet: TiffOutputSet
    ) {

        /*
         * Streaming keeps memory bounded: the image data behind the SOS
         * marker is transferred in bounded chunks instead of being
         * buffered as a whole.
         */
        updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->
            writeSegments(
                byteWriter = outputWriter,
                segments = replaceExifSegments(segments, createExifSegmentBytes(outputSet))
            )
        }
    }

    /**
     * Returns the payload of the new EXIF APP1 segment for the given output set.
     */
    private fun createExifSegmentBytes(outputSet: TiffOutputSet): ByteArray =
        writeExifSegment(TiffWriter(outputSet.byteOrder), outputSet)

    /**
     * Removes all EXIF segments from the given segments and returns them with
     * the new EXIF segment inserted after the JFIF segment.
     *
     * A NULL payload deletes the EXIF data instead, so the segments are
     * returned without any EXIF segment.
     */
    private fun replaceExifSegments(
        segments: List<JFIFPiece>,
        newBytes: ByteArray?
    ): List<JFIFPiece> {

        val oldSegmentsWithoutExif =
            segments.filterNot { piece -> piece is JFIFPieceSegment && piece.isExifSegment() }

        /*
         * A NULL payload means the EXIF data is deleted, so the EXIF-free
         * segments can be returned as they are.
         */
        if (newBytes == null)
            return oldSegmentsWithoutExif

        if (newBytes.size > JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT)
            throw ImageWriteException(
                "EXIF data is too large for a single APP1 segment: ${newBytes.size} " +
                    "bytes (maximum ${JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT})."
            )

        val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(JPEG_BYTE_ORDER)

        val markerLength = newBytes.size + 2
        val markerLengthBytes = markerLength.toShort().toBytes(JPEG_BYTE_ORDER)

        val newSegments = oldSegmentsWithoutExif.toMutableList()

        var index = 0

        /*
         * The JFIF APP0 segment must remain the first segment after SOI, so
         * the EXIF segment is inserted behind it. Without a JFIF segment the
         * EXIF segment becomes the first segment of the file. Only real
         * segments are inspected here, so a file without any header segment
         * does not fail below.
         */
        val jfifIndex = newSegments.indexOfFirst { piece ->
            piece is JFIFPieceSegment && piece.marker == JpegConstants.JFIF_MARKER
        }

        if (jfifIndex != -1)
            index = jfifIndex + 1

        val exifSegment = JFIFPieceSegmentExif(JpegConstants.JPEG_APP1_MARKER, markerBytes, markerLengthBytes, newBytes)

        newSegments.add(index, exifSegment)

        return newSegments
    }

    /**
     * Writes the given segments prefixed with the JPEG start-of-image marker (SOI).
     */
    private fun writeSegments(byteWriter: ByteWriter, segments: List<JFIFPiece>) {

        byteWriter.write(JpegConstants.SOI)

        for (piece in segments)
            piece.write(byteWriter)
    }

    private fun writeExifSegment(
        writer: TiffWriter,
        outputSet: TiffOutputSet
    ): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* Write prefix */
        byteWriter.write(JpegConstants.EXIF_IDENTIFIER_CODE)

        writer.write(byteWriter, outputSet)

        return byteWriter.toByteArray()
    }

    /**
     * Reads a JPEG image, replaces the IPTC data in the App13 segment, but leaves the other
     * data in that segment (if present) unchanged and writes the result to a stream.
     */
    @JvmStatic
    public fun writeIPTC(byteReader: ByteReader, byteWriter: ByteWriter, metadata: IptcMetadata) {

        /*
         * Streaming keeps memory bounded: the image data behind the SOS
         * marker is transferred in bounded chunks instead of being
         * buffered as a whole.
         */
        updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

            writeSegments(
                byteWriter = outputWriter,
                segments = insertAfterLastAppSegments(
                    segments.filterNot { piece -> piece is JFIFPieceSegment && piece.isIptcSegment() },
                    createIptcSegments(metadata)
                )
            )
        }
    }

    /**
     * Returns the APP13 segments for the given IPTC metadata, split across
     * multiple segments when the payload exceeds one segment.
     */
    private fun createIptcSegments(metadata: IptcMetadata): List<JFIFPieceSegment> {

        val newBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(metadata.records)
        )

        val mergedBlocks = metadata.nonIptcBlocks + newBlock

        return createApp13Segments(
            IptcWriter.writeIptcBlocks(mergedBlocks, includeApp13Identifier = false)
        )
    }

    /**
     * Returns the APP13 segments for the given Photoshop data.
     *
     * Data larger than a single segment is split across consecutive APP13
     * segments, exactly like Photoshop and ExifTool do it: every segment
     * repeats the "Photoshop 3.0" identifier and the continuation segments
     * continue mid resource block. This multi-segment layout is handled by
     * all established readers; ExifTool documents it as
     * "APP13 - Photoshop IRB (multi-segment, includes IPTC)".
     */
    private fun createApp13Segments(photoshopData: ByteArray): List<JFIFPieceSegment> {

        val segments = mutableListOf<JFIFPieceSegment>()

        var offset = 0

        do {

            val chunkEnd = minOf(
                offset + JpegConstants.MAX_PHOTOSHOP_BYTES_PER_SEGMENT,
                photoshopData.size
            )

            val segmentWriter = ByteArrayByteWriter()

            segmentWriter.write(JpegConstants.APP13_IDENTIFIER)
            segmentWriter.write(photoshopData.copyOfRange(offset, chunkEnd))

            segments.add(
                JFIFPieceSegment(JpegConstants.JPEG_APP13_MARKER, segmentWriter.toByteArray())
            )

            offset = chunkEnd
        } while (offset < photoshopData.size)

        return segments
    }

    @JvmStatic
    public fun updateXmpXml(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmpXml: String
    ) {

        /*
         * Streaming keeps memory bounded: the image data behind the SOS
         * marker is transferred in bounded chunks instead of being
         * buffered as a whole.
         */
        updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

            writeSegments(
                byteWriter = outputWriter,
                segments = insertAfterLastAppSegments(
                    segments.filterNot { segment ->
                        segment is JFIFPieceSegment && segment.isXmpSegment()
                    },
                    createXmpSegments(xmpXml)
                )
            )
        }
    }

    /**
     * Streams a JPEG from the given reader to the given writer, so the
     * updateComputer can rewrite the header once the SOS marker is reached.
     *
     * The updateComputer receives the collected header segments and the
     * output writer, and must write the complete header (SOI and all
     * segments) to it. The image data behind the SOS marker is then streamed
     * in bounded chunks, so the whole file never has to be buffered in memory.
     */
    internal fun updateMetadataStreaming(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updateComputer: (MutableList<JFIFPieceSegment>, ByteWriter) -> Unit
    ) {

        val (segments, sosMarkerBytes) = JpegUtils.readSegments(byteReader)

        /*
         * A truncated file can end before the SOS marker. Writing just the
         * header would silently destroy the image data, so the update is
         * rejected instead.
         */
        if (sosMarkerBytes == null)
            throw ImageWriteException("JPEG file is truncated or invalid: no SOS marker found.")

        updateComputer(segments.toMutableList(), byteWriter)

        byteWriter.write(sosMarkerBytes)

        byteReader.copyRemainingTo(byteWriter)
    }

    /**
     * Applies the given XMP, EXIF and IPTC updates to the given segments.
     *
     * A NULL value leaves the corresponding metadata unchanged.
     */
    internal fun applyMetadataUpdates(
        segments: List<JFIFPiece>,
        xmpXml: String?,
        outputSet: TiffOutputSet?,
        iptc: IptcMetadata?
    ): List<JFIFPiece> {

        var updatedSegments = segments

        if (xmpXml != null) {

            updatedSegments = insertAfterLastAppSegments(
                updatedSegments.filterNot { segment ->
                    segment is JFIFPieceSegment && segment.isXmpSegment()
                },
                createXmpSegments(xmpXml)
            )
        }

        if (outputSet != null)
            updatedSegments = replaceExifSegments(
                updatedSegments,
                createExifSegmentBytes(outputSet)
            )

        if (iptc != null) {

            updatedSegments = insertAfterLastAppSegments(
                updatedSegments.filterNot { piece -> piece is JFIFPieceSegment && piece.isIptcSegment() },
                createIptcSegments(iptc)
            )
        }

        return updatedSegments
    }

    /**
     * Returns the APP1 segments for the given XMP packet.
     *
     * The editable padding of the packet is removed when the packet does
     * not fit into a single segment, because large padding can push an
     * otherwise small packet beyond the segment size.
     *
     * Oversized packets are written using Adobe extended XMP, exactly like
     * ExifTool does it: the main packet keeps as many whole rdf:Description
     * blocks as fit and references the rest through "xmpNote:HasExtendedXMP",
     * followed by extension segments carrying the GUID and chunks of the
     * extended data. This scheme is part of the Adobe XMP specification and
     * is read by ExifTool, Photoshop and Lightroom. Naive byte splitting of
     * the packet would produce truncated XML that third-party readers reject.
     */
    private fun createXmpSegments(xmpXml: String): List<JFIFPieceSegment> {

        var xmpBytes = xmpXml.encodeToByteArray()

        if (xmpBytes.size > JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
            xmpBytes = removeXmpPadding(xmpXml).encodeToByteArray()

        val partitioned =
            ExtendedXmpWriter.partition(xmpBytes.decodeToString())

        val segments = mutableListOf(
            createStandardXmpSegment(partitioned.mainPacketXml.encodeToByteArray())
        )

        for (payload in partitioned.extensionSegmentPayloads)
            segments.add(JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, payload))

        return segments
    }

    private fun createStandardXmpSegment(xmpBytes: ByteArray): JFIFPieceSegment {

        val segmentWriter = ByteArrayByteWriter()

        segmentWriter.write(JpegConstants.XMP_IDENTIFIER)
        segmentWriter.write(xmpBytes)

        return JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentWriter.toByteArray())
    }

    /**
     * Removes the whitespace padding between the XMP content and the packet
     * terminator processing instruction. Padding exists so tools can edit a
     * packet in place; it carries no information.
     */
    private fun removeXmpPadding(xmpXml: String): String {

        val endIndex = xmpXml.indexOf(XMP_PACKET_END_MARKER)

        if (endIndex == -1)
            return xmpXml

        var contentEnd = endIndex

        while (contentEnd > 0 && xmpXml[contentEnd - 1].isWhitespace())
            contentEnd--

        return xmpXml.substring(0, contentEnd) + xmpXml.substring(endIndex)
    }
}
