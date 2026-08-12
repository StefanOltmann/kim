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

    /* JPEG markers are 0xFF fill bytes */
    private const val FILL_BYTE = 0xFF

    /**
     * Reads the header segments of a JPEG file up to the image data.
     *
     * Returns the kept segments and the SOS marker bytes, or NULL when the
     * file ends before the SOS marker (a truncated or header-only file).
     * The image data behind the SOS marker is left in the reader, so
     * callers can stream it in bounded chunks.
     */
    fun readSegments(
        byteReader: ByteReader,
        keepMarker: (Int) -> Boolean = { true }
    ): Pair<List<JFIFPieceSegment>, ByteArray?> {

        val segments = mutableListOf<JFIFPieceSegment>()

        byteReader.readAndVerifyBytes("JPEG SOI (0xFFD8)", JpegConstants.SOI)

        var readBytesCount = JpegConstants.SOI.size

        while (true) {

            val markerBytes = ByteArray(2)

            /*
             * Find next marker bytes.
             *
             * If there are no more bytes left we end.
             */
            do {

                markerBytes[0] = markerBytes[1]
                markerBytes[1] = byteReader.readByte() ?: return segments to null

                readBytesCount++

            } while (
                FILL_BYTE and markerBytes[0].toInt() != FILL_BYTE ||
                FILL_BYTE and markerBytes[1].toInt() == FILL_BYTE
            )

            val marker = markerBytes.toUInt16(JPEG_BYTE_ORDER)

            /* The EOI marker means the file has no image data. */
            if (marker == JpegConstants.EOI_MARKER)
                return segments to null

            if (marker == JpegConstants.SOS_MARKER)
                return segments to markerBytes

            /* If we don't have enough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - readBytesCount < 2)
                return segments to null

            val segmentLengthBytes = byteReader.readBytes("segmentLengthBytes", 2)

            readBytesCount += 2

            val segmentLength = segmentLengthBytes.toUInt16(JPEG_BYTE_ORDER)

            val segmentContentLength = segmentLength - 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /*
             * If the segment specifies a zero length or a length that is
             * longer than the remaining bytes, the file is corrupt and
             * must be rejected.
             */
            if (segmentContentLength <= 0 || segmentContentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentContentLength")

            val segmentData = byteReader.readBytes("segmentData", segmentContentLength)

            readBytesCount += segmentContentLength

            if (keepMarker(marker))
                segments.add(JFIFPieceSegment(marker, markerBytes, segmentLengthBytes, segmentData))
        }
    }
}
