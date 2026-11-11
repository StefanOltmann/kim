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
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.RST0_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.RST7_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.markerDescription
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * Algorithm to find segment offsets, types, and lengths.
 *
 * Offsets are absolute positions from the stream start and lengths include
 * the 2 marker bytes. In the default mode the SOS segment reaches to the
 * end of the file; an EOI segment is only reported when the marker is
 * physically present, so a truncated file yields exactly the segments its
 * bytes contain.
 *
 * With [findSegmentInfos]' `fullScan` the scan continues behind the image
 * data: the SOS segments cover only their scan headers, every length
 * prefixed segment between the scans is reported individually, restart
 * markers inside the entropy coded data stay unreported, and the scan
 * ends at the first structural EOI marker. Nothing behind that EOI is
 * reported. Standalone markers without a length word (TEM everywhere, a
 * stray restart marker in the header) carry no payload and are reported
 * as two byte entries in both modes. The full scan fails when the file
 * does not end with the structural EOI marker, when an SOI marker
 * appears inside the image data, or when a segment between the scans is
 * truncated.
 *
 * [forEachSegment] reports exactly these entries incrementally and stops
 * the walk behind the last entry the caller accepts, so the enumeration
 * stays bounded without building a list.
 *
 * **Attention:** Must be public API as this is used by https://stefan-oltmann.de/exif-viewer
 */
public object JpegSegmentAnalyzer {

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun findSegmentInfos(
        byteReader: ByteReader,
        fullScan: Boolean = false
    ): List<JpegSegmentInfo> = tryWithImageReadException {

        val segmentInfos = mutableListOf<JpegSegmentInfo>()

        walkSegments(byteReader, fullScan) { segmentInfo ->
            segmentInfos.add(segmentInfo)
            true
        }

        return@findSegmentInfos segmentInfos
    }

    /**
     * Walks the segments of a JPEG file and reports each one to
     * [onSegment] as soon as its boundaries are known. Returning false
     * from [onSegment] stops the walk behind the last reported entry, so
     * a caller that needs only the first entries bounds the enumeration
     * without paying for the bytes behind them.
     *
     * The reported entries and the failure behavior are exactly the ones
     * of [findSegmentInfos]; see its documentation for the scan modes.
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun forEachSegment(
        byteReader: ByteReader,
        fullScan: Boolean = false,
        onSegment: (JpegSegmentInfo) -> Boolean
    ): Unit = tryWithImageReadException {
        walkSegments(byteReader, fullScan, onSegment)
    }

    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    private fun walkSegments(
        byteReader: ByteReader,
        fullScan: Boolean,
        onSegment: (JpegSegmentInfo) -> Boolean
    ) {

        val soiMarker = byteReader.read2BytesAsInt("SOI", JPEG_BYTE_ORDER)

        require(soiMarker == SOI_MARKER) {
            "JPEG magic number mismatch: ${soiMarker.toHexString()}"
        }

        if (!onSegment(
                JpegSegmentInfo(
                    offset = 0,
                    marker = SOI_MARKER,
                    length = 2
                )
            )
        )
            return

        var positionCounter: Long = MediaFormatMagicNumbers.jpeg.size.toLong()

        /*
         * The consumed bytes are only counted here, so the scanner must not
         * buffer a potentially unbounded inter-marker gap.
         */
        val scanner = JpegMarkerScanner(byteReader, keepConsumedBytes = false)

        /*
         * Full scan stage behind the first SOS marker, where the entropy
         * coded data, the segments between the scans and the structural
         * EOI marker are walked.
         */
        var fullStage = false

        /* Whether the full stage reached the structural EOI marker. */
        var eoiSeen = false

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            positionCounter += scan.consumedCount

            if (fullStage) {

                if (scan.marker == EOI_MARKER) {

                    /* The structural EOI ends the whole scan. */

                    onSegment(
                        JpegSegmentInfo(
                            offset = positionCounter - 2,
                            marker = EOI_MARKER,
                            length = 2
                        )
                    )

                    eoiSeen = true

                    break
                }

                if (scan.marker == SOI_MARKER)
                    throw ImageReadException(
                        "An SOI marker inside the image data is no readable structure."
                    )

                /*
                 * Restart markers only occur inside the entropy coded
                 * data and have no length field: they stay unreported
                 * gap data of the scan ranges.
                 */
                if (scan.marker in RST0_MARKER..RST7_MARKER)
                    continue
            }

            /*
             * Standalone markers without a length word (TEM everywhere,
             * a stray restart marker in the header) carry no payload, so
             * the bytes behind them must not be read as a segment
             * length. They are reported as two byte entries like the
             * other standalone markers SOI and EOI.
             */
            if (JpegUtils.isMarkerWithoutLengthWord(scan.marker)) {

                if (!onSegment(
                        JpegSegmentInfo(
                            offset = positionCounter - 2,
                            marker = scan.marker,
                            length = 2
                        )
                    )
                )
                    return

                continue
            }

            if (scan.marker == SOS_MARKER && !fullScan) {

                val remainingBytesCount = byteReader.contentLength - positionCounter

                if (!onSegment(
                        JpegSegmentInfo(
                            offset = positionCounter - 2,
                            marker = scan.marker,
                            length = remainingBytesCount
                        )
                    )
                )
                    return

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

                    onSegment(
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

            /*
             * Only the negative check applies: skipping the segment
             * below fails loudly when the content ends inside it.
             */
            JpegUtils.validateSegmentContentLength(
                remainingSegmentLength,
                Long.MAX_VALUE
            )

            /*
             * The entry is complete with its length field, so a caller
             * that stops here saves the payload read behind it.
             */
            if (!onSegment(
                    JpegSegmentInfo(
                        offset = positionCounter - 2,
                        marker = scan.marker,
                        length = remainingSegmentLength + 4L
                    )
                )
            )
                return

            positionCounter += 2

            byteReader.skipBytes("skip segment", remainingSegmentLength)

            positionCounter += remainingSegmentLength

            if (fullScan && scan.marker == SOS_MARKER)
                fullStage = true

        } while (true)

        if (fullScan && !eoiSeen)
            throw ImageReadException(
                "The full scan requires the file to end with the structural EOI marker."
            )
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
