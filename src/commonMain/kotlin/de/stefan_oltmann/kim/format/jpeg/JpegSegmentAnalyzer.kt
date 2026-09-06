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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.markerDescription
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * Algorithm to find segment offsets, types and lengths.
 */
public object JpegSegmentAnalyzer {

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    @JvmStatic
    public fun findSegmentInfos(
        byteReader: ByteReader
    ): List<JpegSegmentInfo> = tryWithImageReadException {

        val soiMarker = byteReader.read2BytesAsInt("SOI", JPEG_BYTE_ORDER)

        require(soiMarker == SOI_MARKER) {
            "JPEG magic number mismatch: ${soiMarker.toHexString()}"
        }

        val segmentInfos = mutableListOf<JpegSegmentInfo>()

        segmentInfos.add(
            JpegSegmentInfo(
                offset = 0,
                marker = SOI_MARKER,
                length = 2
            )
        )

        var positionCounter: Long = MediaFormatMagicNumbers.jpeg.size.toLong()

        val scanner = JpegMarkerScanner(byteReader)

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            positionCounter += scan.consumedBytes.size

            if (scan.marker == SOS_MARKER) {

                val remainingBytesCount = byteReader.contentLength - positionCounter

                segmentInfos.add(
                    JpegSegmentInfo(
                        offset = positionCounter - 2,
                        marker = scan.marker,
                        length = remainingBytesCount
                    )
                )

                /*
                 * A file can end right at the SOS marker, without image
                 * bytes and without an EOI marker.
                 */
                if (remainingBytesCount < 2)
                    break

                byteReader.skipBytes("image bytes", remainingBytesCount - 2)

                positionCounter += remainingBytesCount

                val eoiMarker = byteReader.read2BytesAsInt("EOI", JPEG_BYTE_ORDER)

                if (eoiMarker == EOI_MARKER) {

                    /* Write the EOI marker if it's really there. */

                    segmentInfos.add(
                        JpegSegmentInfo(
                            offset = positionCounter - 2,
                            marker = EOI_MARKER,
                            length = 2
                        )
                    )
                }

                break
            }

            /* Note: Segment length includes size bytes */
            val remainingSegmentLength =
                byteReader.read2BytesAsInt("segmentLength", JPEG_BYTE_ORDER) - 2

            segmentInfos.add(
                JpegSegmentInfo(
                    offset = positionCounter - 2,
                    marker = scan.marker,
                    length = remainingSegmentLength + 4L
                )
            )

            positionCounter += 2

            /* A zero content length is an empty segment, which is spec-legal. */
            if (remainingSegmentLength < 0)
                throw ImageReadException("Illegal JPEG segment length: $remainingSegmentLength")

            byteReader.skipBytes("skip segment", remainingSegmentLength)

            positionCounter += remainingSegmentLength

        } while (true)

        return segmentInfos
    }

    /**
     * The location and size of a JPEG segment.
     *
     * The values are Longs, so files larger than the signed Int range are
     * represented correctly.
     */
    public data class JpegSegmentInfo(
        val offset: Long,
        val marker: Int,
        val length: Long
    ) {

        override fun toString(): String =
            "$offset = ${markerDescription(marker)} [$length bytes]"
    }
}
