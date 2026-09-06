/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes

/**
 * This algorithm quickly identifies the EXIF orientation offset.
 * If the file already has one no restructuring of the whole file is necessary.
 */
public object JpegOrientationOffsetFinder {

    @Throws(ImageReadException::class)
    public fun findOrientationOffset(
        byteReader: ByteReader
    ): Long? = tryWithImageReadException {

        val magicNumberBytes = byteReader.readBytes(MediaFormatMagicNumbers.jpeg.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == MediaFormatMagicNumbers.jpeg) {
            "JPEG magic number mismatch: ${magicNumberBytes.toSingleNumberHexes()}"
        }

        var positionCounter: Long = MediaFormatMagicNumbers.jpeg.size.toLong()

        val scanner = JpegMarkerScanner(byteReader)

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            positionCounter += scan.consumedBytes.size

            if (scan.marker == JpegConstants.SOS_MARKER || scan.marker == JpegConstants.EOI_MARKER)
                break

            /* If we don't have anough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - positionCounter < 2)
                break

            /* Note: Segment length includes size bytes */
            val segmentLength =
                byteReader.read2BytesAsInt("segmentLength", JPEG_BYTE_ORDER) - 2

            positionCounter += 2

            val remainingByteCount = byteReader.contentLength - positionCounter

            /* A zero content length is an empty segment, which is spec-legal. */
            if (segmentLength < 0 || segmentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            /* We are only looking for the EXIF segment. */
            if (scan.marker != JpegConstants.JPEG_APP1_MARKER) {

                byteReader.skipBytes("skip segment", segmentLength)

                positionCounter += segmentLength

                continue
            }

            /*
             * Read only what the segment actually holds. A short non-EXIF
             * APP1 must be skipped without a desynced read past its end,
             * which would fail the whole orientation update.
             */
            val identifierLength =
                minOf(segmentLength, JpegConstants.EXIF_IDENTIFIER_CODE.size)

            val exifIdentifierBytes = byteReader.readBytes(
                "EXIF identifier",
                identifierLength
            )

            positionCounter += identifierLength

            /* Skip the APP1 XMP segment. */
            if (!exifIdentifierBytes.contentEquals(JpegConstants.EXIF_IDENTIFIER_CODE)) {

                byteReader.skipBytes(
                    "skip segment",
                    segmentLength - identifierLength
                )

                positionCounter += segmentLength - identifierLength

                continue
            }

            val tiffHeader = TiffReader.readTiffHeader(byteReader)

            val exifByteOrder = tiffHeader.byteOrder

            byteReader.skipBytes(
                "skip bytes to first IFD",
                tiffHeader.offsetToFirstIFD - TIFF_HEADER_SIZE
            )

            val entryCount = byteReader.read2BytesAsInt("entrycount", exifByteOrder)

            positionCounter += tiffHeader.offsetToFirstIFD + 2

            for (entryIndex in 0 until entryCount) {

                val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", exifByteOrder)

                if (tag == TiffTag.TIFF_TAG_ORIENTATION.tag) {

                    positionCounter += TIFF_ENTRY_LENGTH - TIFF_ENTRY_MAX_VALUE_LENGTH

                    if (exifByteOrder == ByteOrder.BIG_ENDIAN)
                        positionCounter++

                    return positionCounter

                } else {

                    byteReader.skipBytes("skip TIFF entry", TIFF_ENTRY_LENGTH - 2)

                    positionCounter += TIFF_ENTRY_LENGTH
                }
            }

            /*
             * We are now past the EXIF segment.
             * If we reach this point there is no orientation flag.
             */
            return null

        } while (true)

        return null
    }
}
