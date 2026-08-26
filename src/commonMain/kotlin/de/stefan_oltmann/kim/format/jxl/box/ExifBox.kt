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
package de.stefan_oltmann.kim.format.jxl.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader

/**
 * JPEG XL Exif box.
 */
public class ExifBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.EXIF, offset, size, largeSize, payload) {

    /**
     * The raw offset of the first TIFF header byte within the Exif data,
     * or -1 when the payload is too short to carry it.
     *
     * No validation happens here on purpose: a hostile or corrupt offset
     * must not prevent construction of the box, or a single broken Exif
     * box would make the whole JXL file unreadable and undeletable.
     */
    public val tiffHeaderOffset: Int by lazy {
        if (payload.size < TIFF_HEADER_OFFSET_BYTE_COUNT)
            -1
        else
            payload.toInt(0, BMFF_BYTE_ORDER)
    }

    /**
     * The Exif data as a TIFF file, without the offset field and any
     * prefix bytes before the TIFF header.
     *
     * Empty when the offset is invalid.
     */
    public val exifBytes: ByteArray by lazy {

        val isValid =
            tiffHeaderOffset >= 0 &&
                tiffHeaderOffset.toLong() + TIFF_HEADER_OFFSET_BYTE_COUNT <= payload.size.toLong()

        if (!isValid)
            ByteArray(0)
        else
            payload.copyOfRange(
                fromIndex = TIFF_HEADER_OFFSET_BYTE_COUNT + tiffHeaderOffset,
                toIndex = payload.size
            )
    }

    /**
     * The parsed Exif data, or NULL when it cannot be parsed as TIFF.
     *
     * Parsing is deferred and failure-tolerant for the same reason: one
     * corrupt box must not take down the whole file, which would also
     * make it impossible to delete the broken metadata.
     */
    public val tiffContents: TiffContents? by lazy {
        try {
            TiffReader.read(exifBytes)
        } catch (_: ImageReadException) {
            null
        }
    }
}
