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
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceImageData
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegmentExif
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.copyRemainingTo
import de.stefan_oltmann.kim.input.readRemainingBytes
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import kotlin.jvm.JvmStatic

/**
 * Interface for Exif write/update/remove functionality for Jpeg/JFIF images.
 */
public object JpegRewriter {

    private fun readSegments(byteReader: ByteReader): List<JFIFPiece> {

        val (segments, sosMarkerBytes) = JpegUtils.readSegments(byteReader)

        /*
         * A truncated file can end before the SOS marker. Writing just the
         * header would silently destroy the image data, so the rewrite is
         * rejected instead.
         */
        if (sosMarkerBytes == null)
            throw ImageWriteException("JPEG file is truncated or invalid: no SOS marker found.")

        return segments + JFIFPieceImageData(sosMarkerBytes, byteReader.readRemainingBytes())
    }

    private fun insertAfterLastAppSegments(
        segments: List<JFIFPiece>,
        newSegments: List<JFIFPiece>
    ): List<JFIFPiece> {

        val lastAppIndex = segments.indices.lastOrNull { index ->
            val segment = segments[index]
            segment is JFIFPieceSegment && segment.isAppSegment()
        } ?: -1

        val mergedSegments = segments.toMutableList()

        if (lastAppIndex == -1) {

            if (segments.isEmpty())
                throw ImageWriteException("JPEG file has no APP segments.")

            mergedSegments.addAll(1, newSegments)

        } else {

            mergedSegments.addAll(lastAppIndex + 1, newSegments)
        }

        return mergedSegments
    }

    @JvmStatic
    public fun updateExifMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        outputSet: TiffOutputSet
    ) {

        val segments = readSegments(byteReader)

        writeSegments(
            byteWriter = byteWriter,
            segments = replaceExifSegments(segments, createExifSegmentBytes(outputSet))
        )
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
            throw ImageWriteException("APP1 Segment is too long: " + newBytes.size)

        val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(JPEG_BYTE_ORDER)

        val markerLength = newBytes.size + 2
        val markerLengthBytes = markerLength.toShort().toBytes(JPEG_BYTE_ORDER)

        val newSegments = oldSegmentsWithoutExif.toMutableList()

        var index = 0

        val firstSegment = newSegments[index] as JFIFPieceSegment

        /*
         * The JFIF APP0 segment must remain the first segment after SOI, so
         * the EXIF segment is inserted behind it. Without a JFIF segment the
         * EXIF segment becomes the first segment of the file.
         */
        if (firstSegment.marker == JpegConstants.JFIF_MARKER)
            index = 1

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

        val segments = readSegments(byteReader)

        writeSegments(
            byteWriter = byteWriter,
            segments = insertAfterLastAppSegments(
                segments.filterNot { piece -> piece is JFIFPieceSegment && piece.isIptcSegment() },
                createIptcSegments(metadata)
            )
        )
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
     * Splits the given Photoshop data across multiple APP13 segments.
     *
     * A JPEG segment has a maximum size of around 65 KB. Every segment starts
     * with the Photoshop identifier, so readers can concatenate the payloads
     * of consecutive segments.
     */
    private fun createApp13Segments(photoshopData: ByteArray): List<JFIFPieceSegment> {

        return photoshopData
            .asList()
            .chunked(JpegConstants.MAX_PHOTOSHOP_BYTES_PER_SEGMENT)
            .map { chunk ->

                val segmentWriter = ByteArrayByteWriter()

                segmentWriter.write(JpegConstants.APP13_IDENTIFIER)
                segmentWriter.write(chunk.toByteArray())

                JFIFPieceSegment(JpegConstants.JPEG_APP13_MARKER, segmentWriter.toByteArray())
            }
    }

    @JvmStatic
    public fun updateXmpXml(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmpXml: String
    ) {

        val segments = readSegments(byteReader)

        writeSegments(
            byteWriter = byteWriter,
            segments = insertAfterLastAppSegments(
                segments.filterNot { segment ->
                    segment is JFIFPieceSegment && segment.isXmpSegment()
                },
                createXmpSegments(xmpXml)
            )
        )
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

    private fun createXmpSegments(xmpXml: String): List<JFIFPieceSegment> {

        val xmpBytes = xmpXml.encodeToByteArray()

        /*
         * A JPEG segment has a maximum size of around 65 KB.
         * Split larger XMP data across multiple APP1 segments.
         */
        return xmpBytes
            .asList()
            .chunked(JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
            .map { chunk ->

                val segmentWriter = ByteArrayByteWriter()

                segmentWriter.write(JpegConstants.XMP_IDENTIFIER)
                segmentWriter.write(chunk.toByteArray())

                val segmentBytes = segmentWriter.toByteArray()

                JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentBytes)
            }
    }
}
