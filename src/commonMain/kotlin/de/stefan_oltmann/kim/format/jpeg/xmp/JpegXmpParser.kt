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
package de.stefan_oltmann.kim.format.jpeg.xmp

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.jpeg.JpegConstants

internal object JpegXmpParser {

    fun isXmpJpegSegment(segmentData: ByteArray): Boolean =
        segmentData.startsWith(JpegConstants.XMP_IDENTIFIER)

    /**
     * Returns whether the segment carries a chunk of Adobe extended XMP
     * data instead of a complete packet.
     */
    fun isExtendedXmpJpegSegment(segmentData: ByteArray): Boolean =
        segmentData.startsWith(JpegConstants.EXTENDED_XMP_IDENTIFIER)

    fun parseXmpJpegSegment(segmentData: ByteArray): String {

        if (!isXmpJpegSegment(segmentData))
            throw ImageReadException("Invalid JPEG XMP Segment.")

        val index = JpegConstants.XMP_IDENTIFIER.size

        /* The data is UTF-8 encoded XML */
        return segmentData.slice(
            startIndex = index,
            count = segmentData.size - index
        ).decodeToString()
    }

    /**
     * Parses one extended XMP segment into its GUID, the total size of the
     * complete extended data and this segment's chunk of it.
     *
     * The layout is specified by Adobe and matches what ExifTool writes:
     * identifier, 32-character hexadecimal GUID, 4-byte big-endian total
     * length, then the raw chunk bytes.
     */
    fun parseExtendedXmpJpegSegment(segmentData: ByteArray): ExtendedXmpFragment {

        if (!isExtendedXmpJpegSegment(segmentData))
            throw ImageReadException("Invalid JPEG extended XMP segment.")

        val headerSize = JpegConstants.EXTENDED_XMP_IDENTIFIER.size +
            JpegConstants.EXTENDED_XMP_GUID_LENGTH +
            JpegConstants.EXTENDED_XMP_TOTAL_LENGTH_BYTES

        if (segmentData.size < headerSize)
            throw ImageReadException("Truncated JPEG extended XMP segment.")

        var index = JpegConstants.EXTENDED_XMP_IDENTIFIER.size

        val guidEnd = index + JpegConstants.EXTENDED_XMP_GUID_LENGTH

        val guid = segmentData.decodeToString(index, guidEnd)

        index = guidEnd

        val totalLength = (segmentData[index].toInt() and 0xFF) shl 24 or
            ((segmentData[index + 1].toInt() and 0xFF) shl 16) or
            ((segmentData[index + 2].toInt() and 0xFF) shl 8) or
            (segmentData[index + 3].toInt() and 0xFF)

        index += JpegConstants.EXTENDED_XMP_TOTAL_LENGTH_BYTES

        val data = segmentData.slice(
            startIndex = index,
            count = segmentData.size - index
        )

        return ExtendedXmpFragment(guid, totalLength, data)
    }
}
