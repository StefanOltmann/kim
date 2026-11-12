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
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.isEquals
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPiece
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.xmp.XMP_PACKET_END_TAG
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.copyRemainingTo
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlin.jvm.JvmStatic

/**
 * Interface for Exif write/update/remove functionality for Jpeg/JFIF images.
 *
 * The rewrite normalizes redundant fill bytes (0xFF) between the header
 * markers away. They carry no meaning for any decoder, so the output stays
 * semantically identical - but a no-op rewrite is not byte-identical for
 * such files.
 */
public object JpegRewriter {

    /**
     * The error for a streaming rewrite whose file ends before the SOS
     * marker: a truncated header means writing the output would destroy
     * the image data.
     */
    private const val TRUNCATED_HEADER_MESSAGE: String =
        "JPEG file is truncated or invalid: no SOS marker found."

    /**
     * The error for the delete walk whose file ends inside its image
     * data: without the structural EOI marker the image data cannot be
     * separated from the metadata behind it.
     */
    private const val TRUNCATED_IMAGE_DATA_MESSAGE: String =
        "The JPEG data ends inside its image data without the structural EOI marker."

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
    @Throws(ImageWriteException::class)
    public fun updateExifMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        outputSet: TiffOutputSet
    ) {

        tryWithImageWriteException {

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
    }

    /**
     * Returns the payload of the new EXIF APP1 segment for the given output set.
     */
    private fun createExifSegmentBytes(outputSet: TiffOutputSet): ByteArray =
        JpegConstants.EXIF_IDENTIFIER_CODE + outputSet.toTiffBytes()

    /**
     * Removes the first EXIF block (its own segments plus every
     * continuation behind it) from the given segments and returns them
     * with the new EXIF segment inserted after the JFIF segment.
     *
     * Additional *independent* EXIF blocks - ones that start with their
     * own TIFF byte order marker - belong to other tools. They survive
     * byte-exact, because Kim never interpreted them and deleting them
     * would destroy data. Kim keeps reading only the first block.
     *
     * A NULL payload deletes the first EXIF data instead, so the segments
     * are returned without it - independent blocks still survive.
     */
    private fun replaceExifSegments(
        segments: List<JFIFPiece>,
        newBytes: ByteArray?
    ): List<JFIFPiece> {

        var removedFirstExif = false

        /*
         * The start of an independent EXIF block ends the removal for
         * good: every EXIF segment behind it belongs to that foreign
         * block - removing a continuation there would truncate the block
         * mid-TIFF and destroy data Kim never interpreted.
         */
        var removalStopped = false

        val oldSegmentsWithoutExif = segments.filterNot { piece ->

            if (piece !is JFIFPieceSegment || !piece.isExifSegment())
                return@filterNot false

            if (removalStopped)
                return@filterNot false

            /*
             * Continuation parts of a multi-segment EXIF block are removed
             * together with the first block they belong to; the start of a
             * second, independent EXIF block stops the removal.
             */
            val isContinuation =
                JpegUtils.findExifContinuationHeaderEnd(piece.marker, piece.segmentBytes) != null

            if (removedFirstExif && !isContinuation) {
                removalStopped = true
                return@filterNot false
            }

            removedFirstExif = true

            true
        }

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

        val exifSegment = JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, markerBytes, markerLengthBytes, newBytes)

        newSegments.add(index, exifSegment)

        return newSegments
    }

    /**
     * Writes the given segments prefixed with the JPEG start-of-image marker (SOI).
     */
    internal fun writeSegments(byteWriter: ByteWriter, segments: List<JFIFPiece>) {

        byteWriter.write(JpegConstants.SOI)

        for (piece in segments)
            piece.write(byteWriter)
    }

    /**
     * Reads a JPEG image, replaces the IPTC data in the App13 segment, but leaves the other
     * data in that segment (if present) unchanged and writes the result to a stream.
     *
     * Attention: Only [de.stefan_oltmann.kim.Kim.update] maintains the
     * IPTCDigest resource (0x0425) and the `xmpNote:IPTCDigest` XMP
     * property. This low-level write re-emits the digest blocks the
     * given metadata carries as they are.
     */
    @JvmStatic
    public fun writeIPTC(byteReader: ByteReader, byteWriter: ByteWriter, metadata: IptcMetadata) {

        tryWithImageWriteException {

            /*
             * Streaming keeps memory bounded: the image data behind the SOS
             * marker is transferred in bounded chunks instead of being
             * buffered as a whole.
             */
            updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

                writeSegments(
                    byteWriter = outputWriter,
                    segments = segments.replaceIptcSegments(
                        metadata = metadata,
                        newSegments = createIptcSegments(metadata)
                    )
                )
            }
        }
    }

    /**
     * Replaces the Photoshop segments the given metadata was parsed from
     * with the given new segments. Other Photoshop streams of the file
     * survive byte-exact, and the new segments take the position of the
     * replaced stream: the read returns the first record-bearing stream,
     * so the replacement landing behind a surviving stream would make it
     * shadow the update on the next read.
     *
     * Without a replaced stream the new segments go behind the last APP
     * segment, like a stream that was not parsed from the file.
     */
    private fun List<JFIFPiece>.replaceIptcSegments(
        metadata: IptcMetadata,
        newSegments: List<JFIFPiece>
    ): List<JFIFPiece> {

        var firstReplacedIndex = -1

        val output = mutableListOf<JFIFPiece>()

        for (piece in this) {

            if (isReplacedIptcSegment(piece, metadata)) {

                if (firstReplacedIndex < 0)
                    firstReplacedIndex = output.size

                continue
            }

            output.add(piece)
        }

        if (firstReplacedIndex < 0)
            return insertAfterLastAppSegments(output, newSegments)

        return output.toMutableList().apply { addAll(firstReplacedIndex, newSegments) }
    }

    /**
     * Replaces the XMP segments a rewrite owns with the given new
     * segments: the first standard packet (the one Kim reads and merges
     * the update into) is replaced, additional independent packets
     * belong to other tools and survive byte-exact - Kim keeps reading
     * only the first packet, so the new segments are inserted ahead of
     * the survivors when any exist.
     *
     * A packet naively split across several standard segments is one
     * packet: the read concatenates the segments until the closing
     * element appears, so every standard segment before that boundary
     * is a continuation and is removed with the first packet - keeping
     * it would leave the stale second half of the old packet behind
     * the new one. A segment behind a complete packet is independent
     * and survives.
     *
     * Extended-XMP segments in front of a surviving packet are removed,
     * including orphans whose GUID no packet references: their content
     * is undecodable without the lost main packet (the read already
     * skips them silently, like ExifTool), so keeping them would
     * preserve bytes no tool can ever decode. Extended segments behind
     * a surviving packet belong to that foreign packet - they are
     * referenced by its GUID, so they survive with it.
     *
     * Without surviving packets the new segments go behind the last
     * APP segment, exactly like the single-packet case always did.
     */
    private fun List<JFIFPiece>.replaceXmpSegments(
        newSegments: List<JFIFPieceSegment>
    ): List<JFIFPiece> {

        val output = mutableListOf<JFIFPiece>()

        var removedFirstXmp = false
        var firstPacketClosed = false
        var firstSurvivingXmpIndex = -1

        for (piece in this) {

            if (piece !is JFIFPieceSegment || !piece.isXmpSegment()) {
                output.add(piece)
                continue
            }

            if (piece.segmentBytes.startsWith(JpegConstants.EXTENDED_XMP_IDENTIFIER)) {

                /* The extension chunks of a surviving packet stay with it. */
                if (firstSurvivingXmpIndex >= 0)
                    output.add(piece)

                continue
            }

            if (removedFirstXmp) {

                if (firstPacketClosed) {

                    if (firstSurvivingXmpIndex < 0)
                        firstSurvivingXmpIndex = output.size

                    output.add(piece)
                } else {

                    /*
                     * A continuation of the replaced packet: removed with
                     * it. The segment that completes the packet flips the
                     * boundary, so the following standard segments are
                     * independent packets again.
                     */
                    firstPacketClosed = segmentClosesPacket(piece.segmentBytes)
                }

                continue
            }

            removedFirstXmp = true

            /*
             * The replaced packet closes within its first segment when
             * the read's boundary element is present; otherwise the
             * following standard segments continue it.
             */
            firstPacketClosed = segmentClosesPacket(piece.segmentBytes)
        }

        if (!removedFirstXmp)
            return insertAfterLastAppSegments(output, newSegments)

        if (firstSurvivingXmpIndex < 0)
            return insertAfterLastAppSegments(output, newSegments)

        output.addAll(firstSurvivingXmpIndex, newSegments)

        return output
    }

    /**
     * Returns whether the standard XMP segment carries the closing
     * element the read's concatenation loop stops at. The raw bytes are
     * searched instead of decoding the XML, because the element is
     * ASCII and a segment that cannot be decoded fails the read before
     * any rewrite sees it.
     */
    private fun segmentClosesPacket(segmentBytes: ByteArray): Boolean {

        val closeTag = XMP_PACKET_END_TAG.encodeToByteArray()

        for (start in 0..segmentBytes.size - closeTag.size) {

            if (segmentBytes.isEquals(start, closeTag, 0, closeTag.size))
                return true
        }

        return false
    }

    /**
     * Decides whether an IPTC rewrite removes the given APP13 segment.
     *
     * Only the segments the parsed metadata came from are removed; other
     * Photoshop streams of the file belong to other tools and survive
     * byte-exact. The segments are identified by content, because the
     * rewrite parses the file again. Metadata that was not parsed from
     * the file carries no identity, so every Photoshop stream is
     * replaced, which keeps the documented contract of [writeIPTC].
     */
    private fun isReplacedIptcSegment(piece: JFIFPiece, metadata: IptcMetadata): Boolean {

        if (piece !is JFIFPieceSegment || !piece.isIptcSegment())
            return false

        if (metadata.sourceSegmentBytes.isEmpty())
            return true

        return metadata.sourceSegmentBytes.any { sourceBytes ->
            sourceBytes.contentEquals(piece.segmentBytes)
        }
    }

    /**
     * Returns the APP13 segments for the given IPTC metadata, split across
     * multiple segments when the payload exceeds one segment.
     */
    private fun createIptcSegments(metadata: IptcMetadata): List<JFIFPieceSegment> =

        createApp13Segments(photoshopData = IptcWriter.writeIptcResourceBlocks(metadata))

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

            /*
             * A continuation segment that starts exactly on a resource
             * block boundary begins with the 8BIM signature. Readers
             * then treat it as an independent Photoshop stream instead
             * of a continuation - ExifTool skips such a tail entirely -
             * so the split moves one byte into the previous block's
             * data, which continuation readers handle by design.
             */
            val chunkEndIsBlockBoundary =
                chunkEnd + JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_LENGTH <= photoshopData.size &&
                    photoshopData.getRemainingBytes(chunkEnd).startsWith(
                        convertHexStringToByteArray(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX)
                    )

            val effectiveChunkEnd = if (chunkEndIsBlockBoundary) chunkEnd - 1 else chunkEnd

            val segmentWriter = ByteArrayByteWriter()

            segmentWriter.write(JpegConstants.APP13_IDENTIFIER)
            segmentWriter.write(photoshopData.copyOfRange(offset, effectiveChunkEnd))

            segments.add(
                JFIFPieceSegment(JpegConstants.JPEG_APP13_MARKER, segmentWriter.toByteArray())
            )

            offset = effectiveChunkEnd
        } while (offset < photoshopData.size)

        return segments
    }

    /**
     * Replaces the XMP of the file with the given packet.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun updateXmpXml(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmpXml: String
    ) {

        tryWithImageWriteException {

            /*
             * Streaming keeps memory bounded: the image data behind the SOS
             * marker is transferred in bounded chunks instead of being
             * buffered as a whole.
             */
            updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

                writeSegments(
                    byteWriter = outputWriter,
                    segments = segments.replaceXmpSegments(
                        newSegments = createXmpSegments(xmpXml)
                    )
                )
            }
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

        val (segments, sosMarkerBytes) = readHeaderForStreaming(byteReader)

        updateComputer(segments.toMutableList(), byteWriter)

        byteWriter.write(sosMarkerBytes)

        byteReader.copyRemainingTo(byteWriter)
    }

    /**
     * Streams a JPEG to the given writer with every metadata segment
     * removed: the delete policy of the header, extended through the
     * structural EOI marker (GitHub issue 41).
     *
     * The headerComputer receives the collected header segments and the
     * output writer, and must write the complete header (SOI and all kept
     * segments) to it, like [updateMetadataStreaming]'s computer. The
     * entropy coded data behind the SOS marker is then transferred
     * byte-exact - restart markers, fill bytes and byte stuffings
     * included - while every APP1 to APP15 and COM segment between the
     * scans is removed like the header's metadata segments, and the
     * output ends at the structural EOI marker: every byte behind it
     * belongs to other tools and is discarded.
     *
     * A file that ends inside its image data without the structural EOI
     * marker fails, because the image data cannot be separated from the
     * metadata behind it and the result would not be verifiably clean.
     */
    internal fun deleteMetadataStreaming(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        headerComputer: (MutableList<JFIFPieceSegment>, ByteWriter) -> Unit
    ) {

        /*
         * The walk behind the header writes the SOS marker and its scan
         * header by itself, so the headerComputer's output is followed
         * directly by the image data.
         */
        val segments = readHeaderForStreaming(byteReader).first

        headerComputer(segments.toMutableList(), byteWriter)

        streamImageDataThroughStructuralEoi(byteReader, byteWriter)
    }

    /**
     * Reads the header segments of a JPEG file up to the image data for
     * one of the streaming rewrites, failing on a truncated file: writing
     * just the header would silently destroy the image data.
     */
    private fun readHeaderForStreaming(
        byteReader: ByteReader
    ): Pair<List<JFIFPieceSegment>, ByteArray> {

        val (segments, sosMarkerBytes) = JpegUtils.readSegments(byteReader)

        if (sosMarkerBytes == null)
            throw ImageWriteException(TRUNCATED_HEADER_MESSAGE)

        return segments to sosMarkerBytes
    }

    /**
     * Streams the region behind the first SOS marker to the writer: the
     * walk writes the SOS marker and its scan header, transfers the
     * entropy coded data byte-exact, removes every metadata segment
     * between the scans, keeps the decoding structures and the scan
     * headers of the following scans, and ends the output at the
     * structural EOI marker.
     */
    private fun streamImageDataThroughStructuralEoi(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) {

        /*
         * The scan header is transferred by its length field, so no byte
         * inside its payload - a crafted EOI pair included - can end the
         * file early. The entropy walk starts behind it.
         */
        writeSegmentBehindImageData(
            byteReader = byteReader,
            byteWriter = byteWriter,
            marker = JpegConstants.SOS_MARKER
        )

        /*
         * The consumed bytes are transferred to the writer by the scanner,
         * so no unbounded inter-marker gap is buffered.
         */
        val scanner = JpegMarkerScanner(byteReader, keepConsumedBytes = false)

        while (true) {

            val scan = scanner.transferToNextMarker(byteWriter, zeroIsFillByte = true)
                ?: throw ImageWriteException(TRUNCATED_IMAGE_DATA_MESSAGE)

            when {
                /* The structural EOI ends the file: nothing behind it survives. */
                scan.marker == JpegConstants.EOI_MARKER -> {
                    byteWriter.write(JpegConstants.EOI)
                    return
                }

                scan.marker == JpegConstants.SOI_MARKER ->
                    throw ImageWriteException(
                        "An SOI marker inside the image data is no readable structure."
                    )

                /*
                 * Restart markers and the TEM marker carry no payload:
                 * they are part of the entropy coded data and keep their
                 * bytes.
                 */
                JpegUtils.isMarkerWithoutLengthWord(scan.marker) ->
                    byteWriter.write(scan.markerBytes)

                /* The scan header of the next progressive scan keeps its bytes. */
                scan.marker == JpegConstants.SOS_MARKER ->
                    writeSegmentBehindImageData(byteReader, byteWriter, scan.marker)

                /*
                 * Every APP segment and comment between the scans is
                 * metadata: EXIF, XMP, IPTC and every foreign payload,
                 * the JFIF and Adobe blocks beside them. The position is
                 * one no decoder reads, so none of them stays.
                 */
                scan.marker in JpegConstants.JPEG_APP0_MARKER..JpegConstants.JPEG_APP15_MARKER ||
                    scan.marker == JpegConstants.COM_MARKER_1 ->
                    skipSegmentBehindImageData(byteReader)

                /* Decoding structures between the scans (DHT, DNL...) keep their bytes. */
                else ->
                    writeSegmentBehindImageData(byteReader, byteWriter, scan.marker)
            }
        }
    }

    /**
     * Reads the length field and the payload of a segment behind the
     * image data, failing when the file ends inside it.
     */
    private fun readSegmentBehindImageData(byteReader: ByteReader): ByteArray {

        val segmentLengthBytes = byteReader.readBytes(2)

        if (segmentLengthBytes.size != 2)
            throw ImageWriteException("Truncated JPEG segment length behind the image data.")

        val segmentContentLength =
            segmentLengthBytes.toUInt16(JPEG_BYTE_ORDER) - 2

        if (segmentContentLength < 0)
            throw ImageWriteException(
                "Illegal JPEG segment length behind the image data: $segmentContentLength"
            )

        val segmentBytes = byteReader.readBytes(segmentContentLength)

        if (segmentBytes.size != segmentContentLength)
            throw ImageWriteException(
                "Truncated JPEG segment behind the image data: " +
                    "${segmentBytes.size} of $segmentContentLength bytes."
            )

        return segmentBytes
    }

    /**
     * Discards a metadata segment between the scans: its length field and
     * payload are read only to keep the walk in sync with the file.
     */
    private fun skipSegmentBehindImageData(byteReader: ByteReader) {
        readSegmentBehindImageData(byteReader)
    }

    /**
     * Writes a segment behind the image data to the writer byte-exact.
     * The 2 length bytes are reconstructed from the payload size, which
     * they equal by definition of the segment length field.
     */
    private fun writeSegmentBehindImageData(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        marker: Int
    ) {

        val payloadBytes = readSegmentBehindImageData(byteReader)

        val segmentLength = (payloadBytes.size + 2).toShort()

        byteWriter.write(marker.toShort().toBytes(JPEG_BYTE_ORDER))

        byteWriter.write(segmentLength.toBytes(JPEG_BYTE_ORDER))

        byteWriter.write(payloadBytes)
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

            /*
             * Only the first XMP packet (plus its extended data, which the
             * update merged into the new packet) is replaced. Additional
             * independent packets belong to other tools and survive
             * byte-exact - Kim keeps reading only the first packet.
             *
             * Every extended-XMP segment is removed, including orphans
             * whose GUID no surviving packet references: their content is
             * undecodable without the lost main packet (the read already
             * skips them silently, like ExifTool), so keeping them would
             * preserve bytes no tool can ever decode.
             */
            updatedSegments = updatedSegments.replaceXmpSegments(
                newSegments = createXmpSegments(xmpXml)
            )
        }

        if (outputSet != null)
            updatedSegments = replaceExifSegments(
                updatedSegments,
                createExifSegmentBytes(outputSet)
            )

        if (iptc != null) {

            updatedSegments = updatedSegments.replaceIptcSegments(
                metadata = iptc,
                newSegments = createIptcSegments(iptc)
            )
        }

        return updatedSegments
    }

    /**
     * Returns the APP1 segments for the given XMP packet.
     *
     * Oversized packets are split by the XMP library using Adobe extended
     * XMP, exactly like ExifTool does it: the main packet keeps as many
     * whole rdf:Description blocks as fit and references the rest through
     * "xmpNote:HasExtendedXMP", followed by extension segments carrying the
     * GUID and chunks of the extended data. This scheme is part of the
     * Adobe XMP specification and is read by ExifTool, Photoshop and
     * Lightroom. Naive byte splitting of the packet across standard XMP
     * segments would produce truncated XML that third-party readers reject.
     */
    private fun createXmpSegments(xmpXml: String): List<JFIFPieceSegment> {

        val partitioned = XMPMetaFactory.partitionPacket(
            packet = xmpXml,
            maxMainPacketBytes = JpegConstants.MAX_XMP_BYTES_PER_SEGMENT,
            maxExtendedChunkBytes = JpegConstants.MAX_EXTENDED_XMP_BYTES_PER_SEGMENT
        )

        val segments = mutableListOf(
            createStandardXmpSegment(partitioned.mainPacket.encodeToByteArray())
        )

        for (chunk in partitioned.extendedChunks)
            segments.add(JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, chunk))

        return segments
    }

    private fun createStandardXmpSegment(xmpBytes: ByteArray): JFIFPieceSegment {

        val segmentWriter = ByteArrayByteWriter()

        segmentWriter.write(JpegConstants.XMP_IDENTIFIER)
        segmentWriter.write(xmpBytes)

        return JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentWriter.toByteArray())
    }
}
