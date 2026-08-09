/*
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
import de.stefan_oltmann.kim.common.getRemainingBytes
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
import de.stefan_oltmann.kim.format.tiff.write.TiffWriterBase
import de.stefan_oltmann.kim.format.tiff.write.TiffWriterLossless
import de.stefan_oltmann.kim.format.tiff.write.TiffWriterLossy
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import kotlin.jvm.JvmStatic

/**
 * Interface for Exif write/update/remove functionality for Jpeg/JFIF images.
 */
public object JpegRewriter {

    private fun readSegments(byteReader: ByteReader): JFIFPieces {

        val allPieces = mutableListOf<JFIFPiece>()
        val segmentPieces = mutableListOf<JFIFPiece>()

        val visitor: JpegVisitor = object : JpegVisitor {

            /* Read the whole file. */
            override fun beginSOS(): Boolean =
                true

            override fun visitSOS(marker: Int, markerBytes: ByteArray, imageData: ByteArray) {
                allPieces.add(JFIFPieceImageData(markerBytes, imageData))
            }

            override fun visitSegment(
                marker: Int,
                markerBytes: ByteArray,
                segmentLength: Int,
                segmentLengthBytes: ByteArray,
                segmentBytes: ByteArray
            ): Boolean {

                val piece: JFIFPiece = JFIFPieceSegment(marker, markerBytes, segmentLengthBytes, segmentBytes)

                allPieces.add(piece)
                segmentPieces.add(piece)

                return true
            }
        }

        JpegUtils.traverseJFIF(byteReader, visitor)

        return JFIFPieces(allPieces, segmentPieces)
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
    public fun updateExifMetadataLossless(byteReader: ByteReader, byteWriter: ByteWriter, outputSet: TiffOutputSet) {

        val (oldSegments, segmentPieces) = readSegments(byteReader)

        val oldSegmentsWithoutExif =
            oldSegments.filterNot { piece -> piece is JFIFPieceSegment && piece.isExifSegment() }

        val exifSegmentPieces =
            segmentPieces.filterIsInstance<JFIFPieceSegment>().filter(JFIFPieceSegment::isExifSegment)

        val writer: TiffWriterBase

        if (exifSegmentPieces.isNotEmpty()) {

            val exifPiece = exifSegmentPieces.first()

            val exifBytes = exifPiece.segmentBytes.getRemainingBytes(JpegConstants.EXIF_IDENTIFIER_CODE.size)

            writer = TiffWriterLossless(outputSet.byteOrder, exifBytes)

        } else {

            writer = TiffWriterLossy(outputSet.byteOrder)
        }

        val newBytes = writeExifSegment(writer, outputSet)

        writeSegmentsReplacingExif(byteWriter, oldSegmentsWithoutExif, newBytes)
    }

    @JvmStatic
    private fun writeSegmentsReplacingExif(
        byteWriter: ByteWriter,
        oldSegments: List<JFIFPiece>,
        newBytes: ByteArray?
    ) {

        val newSegments = oldSegments.toMutableList()

        byteWriter.write(JpegConstants.SOI)

        if (newBytes != null) {

            if (newBytes.size > JpegConstants.MAX_SEGMENT_SIZE)
                throw ImageWriteException("APP1 Segment is too long: " + newBytes.size)

            val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(JPEG_BYTE_ORDER)

            val markerLength = newBytes.size + 2
            val markerLengthBytes = markerLength.toShort().toBytes(JPEG_BYTE_ORDER)
            var index = 0

            val firstSegment = newSegments[index] as JFIFPieceSegment

            if (firstSegment.marker == JpegConstants.JFIF_MARKER)
                index = 1

            val exifSegment =
                JFIFPieceSegmentExif(JpegConstants.JPEG_APP1_MARKER, markerBytes, markerLengthBytes, newBytes)

            newSegments.add(index, exifSegment)
        }

        var app1Written = false

        @Suppress("LoopWithTooManyJumpStatements")
        for (piece in newSegments) {

            if (piece is JFIFPieceSegmentExif) {

                /* Only replace first APP1 segment; skips others. */
                if (app1Written)
                    continue

                app1Written = true

                /* It's NULL if the user wants to delete EXIF */
                if (newBytes == null)
                    continue

                if (newBytes.size > JpegConstants.MAX_SEGMENT_SIZE)
                    throw ImageWriteException("APP1 Segment is too long: " + newBytes.size)

                val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(JPEG_BYTE_ORDER)

                val markerLength = newBytes.size + 2
                val markerLengthBytes = markerLength.toShort().toBytes(JPEG_BYTE_ORDER)

                byteWriter.write(markerBytes)
                byteWriter.write(markerLengthBytes)
                byteWriter.write(newBytes)

            } else {

                piece.write(byteWriter)
            }
        }
    }

    private fun writeExifSegment(
        writer: TiffWriterBase,
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

        val oldPieces = readSegments(byteReader).allPieces

        val newBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(metadata.records)
        )

        val mergedBlocks = metadata.nonIptcBlocks + newBlock

        val newApp13Segment = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP13_MARKER,
            segmentBytes = IptcWriter.writeIptcBlocks(mergedBlocks)
        )

        val oldPiecesWithoutApp13Segments =
            oldPieces.filterNot { piece -> piece is JFIFPieceSegment && piece.isIptcSegment() }

        val mergedPieces = insertAfterLastAppSegments(
            oldPiecesWithoutApp13Segments,
            listOf(newApp13Segment)
        )

        byteWriter.write(JpegConstants.SOI)

        for (piece in mergedPieces)
            piece.write(byteWriter)
    }

    @JvmStatic
    public fun updateXmpXml(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmpXml: String
    ) {

        val (segments, _) = readSegments(byteReader)

        val segmentsWithoutXmp = segments.filterNot { segment ->
            segment is JFIFPieceSegment && segment.isXmpSegment()
        }

        val newSegments = createXmpSegments(xmpXml)

        val mergedPieces = insertAfterLastAppSegments(
            segmentsWithoutXmp,
            newSegments
        )

        byteWriter.write(JpegConstants.SOI)

        mergedPieces.forEach { piece ->
            piece.write(byteWriter)
        }
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

    private data class JFIFPieces(
        val allPieces: List<JFIFPiece>,
        val segmentPieces: List<JFIFPiece>
    )
}
