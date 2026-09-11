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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.readBytes

internal object JpegUtils {

    /**
     * The buffered header is what a rewrite carries in memory, so a
     * hostile file of many small segments must not accumulate
     * unboundedly. Legitimate files stay far below this limit.
     */
    private const val MAX_HEADER_SEGMENT_BYTES: Int = 16 * 1024 * 1024

    /*
     * The identifier is "Exif\0" plus one more byte. Some cameras omit
     * the second NUL, and up to four garbage bytes before the identifier
     * have been seen as well.
     */
    private const val MAX_GARBAGE_PREFIX_LENGTH: Int = 4

    private const val EXIF_HEADER_LENGTH: Int = 6

    /**
     * Returns the end of the "Exif\0?" header within the given segment
     * bytes, or NULL when the segment does not carry an EXIF header.
     *
     * Like ExifTool, the identifier is tolerated case-insensitively with
     * up to four garbage bytes in front of it and one arbitrary byte
     * behind the NUL, because those variants occur in files of real
     * cameras.
     */
    fun findExifHeaderEnd(segmentBytes: ByteArray): Int? {

        for (prefixLength in 0..MAX_GARBAGE_PREFIX_LENGTH) {

            if (prefixLength + EXIF_HEADER_LENGTH > segmentBytes.size)
                return null

            val isIdentifier =
                isExifChar(segmentBytes[prefixLength], 'E') &&
                    isExifChar(segmentBytes[prefixLength + 1], 'x') &&
                    isExifChar(segmentBytes[prefixLength + 2], 'i') &&
                    isExifChar(segmentBytes[prefixLength + 3], 'f') &&
                    segmentBytes[prefixLength + 4] == 0x00.toByte()

            if (isIdentifier)
                return prefixLength + EXIF_HEADER_LENGTH
        }

        return null
    }

    private fun isExifChar(byte: Byte, expected: Char): Boolean {

        val value = byte.toInt() and 0xFF

        val lower = expected.lowercaseChar().code

        val upper = expected.uppercaseChar().code

        return value == lower || value == upper
    }

    /**
     * Reads the header segments of a JPEG file up to the image data.
     *
     * Returns the kept segments and the SOS marker bytes, or NULL when the
     * file ends before the SOS marker (a truncated or header-only file).
     * The image data behind the SOS marker is left in the reader, so
     * callers can stream it in bounded chunks.
     *
     * Attention: Redundant fill bytes (0xFF) between the header markers are
     * deliberately not carried into the returned segments, so a rewrite
     * normalizes them away. They carry no meaning - every decoder skips
     * them per the spec - and buffering arbitrary inter-marker junk is
     * exactly the unbounded growth the scanner must not do.
     */
    fun readSegments(
        byteReader: ByteReader,
        keepMarker: (Int) -> Boolean = { true }
    ): Pair<List<JFIFPieceSegment>, ByteArray?> {

        val segments = mutableListOf<JFIFPieceSegment>()

        /* The consumed bytes are only counted here, so the scanner must not
         * buffer a potentially unbounded inter-marker gap. */
        val scanner = JpegMarkerScanner(byteReader, keepConsumedBytes = false)

        byteReader.readAndVerifyBytes("JPEG SOI (0xFFD8)", JpegConstants.SOI)

        /*
         * Counted in Long space, so streams larger than the signed Int
         * range cannot wrap the counter and silently disable the
         * truncation checks below.
         */
        var readBytesCount = JpegConstants.SOI.size.toLong()

        var headerSegmentBytes = 0

        while (true) {

            /*
             * The rewriter's legacy tolerance: a 0x00 behind the 0xFF is
             * treated as a marker byte, so corrupt files fail loudly on
             * the length checks instead of being scanned over.
             */
            val scan = scanner.nextMarker(zeroIsFillByte = false)
                ?: return segments to null

            readBytesCount += scan.consumedCount

            /* The EOI marker means the file has no image data. */
            if (scan.marker == JpegConstants.EOI_MARKER)
                return segments to null

            if (scan.marker == JpegConstants.SOS_MARKER)
                return segments to scan.markerBytes

            /* If we don't have enough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - readBytesCount < 2)
                return segments to null

            val segmentLengthBytes = byteReader.readBytes("segmentLengthBytes", 2)

            readBytesCount += 2L

            val segmentLength = segmentLengthBytes.toUInt16(JPEG_BYTE_ORDER)

            val segmentContentLength = segmentLength - 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /* A zero content length is an empty segment, which is spec-legal. */
            if (segmentContentLength < 0 || segmentContentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentContentLength")

            val segmentData = byteReader.readBytes("segmentData", segmentContentLength)

            readBytesCount += segmentContentLength.toLong()

            if (keepMarker(scan.marker)) {

                headerSegmentBytes += segmentContentLength

                if (headerSegmentBytes > MAX_HEADER_SEGMENT_BYTES)
                    throw ImageReadException(
                        "JPEG header exceeds $MAX_HEADER_SEGMENT_BYTES bytes."
                    )

                segments.add(
                    JFIFPieceSegment(
                        scan.marker,
                        scan.markerBytes,
                        segmentLengthBytes,
                        segmentData
                    )
                )
            }
        }
    }
}
