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
package de.stefan_oltmann.kim.format.jpeg.jfif

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.JpegUtils
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.output.ByteWriter

internal open class JFIFPieceSegment(
    val marker: Int,
    val markerBytes: ByteArray,
    val segmentLengthBytes: ByteArray,
    val segmentBytes: ByteArray
) : JFIFPiece {

    constructor(marker: Int, segmentBytes: ByteArray) : this(
        marker = marker,
        markerBytes = marker.toShort().toBytes(JpegConstants.JPEG_BYTE_ORDER),
        segmentLengthBytes = toSegmentLengthBytes(segmentBytes),
        segmentBytes = segmentBytes
    )

    override fun toString(): String = "JFIF Piece $marker"

    override fun write(byteWriter: ByteWriter) {
        byteWriter.write(markerBytes)
        byteWriter.write(segmentLengthBytes)
        byteWriter.write(segmentBytes)
    }

    fun isAppSegment(): Boolean =
        marker >= JpegConstants.JPEG_APP0_MARKER && marker <= JpegConstants.JPEG_APP15_MARKER

    /*
     * The tolerant match is used here as well, so a rewrite replaces
     * a variant header instead of keeping it beside the new segment.
     */
    fun isExifSegment(): Boolean =
        marker == JpegConstants.JPEG_APP1_MARKER && JpegUtils.findExifHeaderEnd(segmentBytes) != null

    fun isIptcSegment(): Boolean =
        marker == JpegConstants.JPEG_APP13_MARKER && IptcParser.isPhotoshopApp13Segment(segmentBytes)

    /**
     * Matches standard XMP packets and Adobe extended XMP segments alike,
     * so a rewrite replaces or removes both instead of leaving stale
     * extensions behind.
     */
    fun isXmpSegment(): Boolean =
        marker == JpegConstants.JPEG_APP1_MARKER && (segmentBytes.startsWith(JpegConstants.XMP_IDENTIFIER) ||
            segmentBytes.startsWith(JpegConstants.EXTENDED_XMP_IDENTIFIER))

    companion object {

        /**
         * Bounds a freshly built payload at the 16-bit length field. The
         * cast would wrap silently for larger payloads and serialize a
         * corrupt JPEG, so the choke point rejects them instead.
         */
        private fun toSegmentLengthBytes(segmentBytes: ByteArray): ByteArray {

            if (segmentBytes.size > JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT)
                throw ImageWriteException(
                    "Segment payload is too large for a single JPEG segment: " +
                        "${segmentBytes.size} bytes " +
                        "(maximum ${JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT})."
                )

            return (segmentBytes.size + 2).toShort().toBytes(JpegConstants.JPEG_BYTE_ORDER)
        }
    }
}
