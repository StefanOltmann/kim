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
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt

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
     * The offset of the first TIFF header byte within the Exif data.
     */
    public val tiffHeaderOffset: Int

    /**
     * The Exif data as a TIFF file.
     *
     * The offset field and any prefix bytes before the TIFF header
     * are not part of the TIFF data.
     */
    public val exifBytes: ByteArray

    /* Directly parse here to ensure it's valid. */
    public val tiffContents: TiffContents

    init {

        val byteReader = ByteArrayByteReader(payload)

        tiffHeaderOffset = byteReader.read4BytesAsInt("tiff header offset", BMFF_BYTE_ORDER)

        if (
            tiffHeaderOffset < 0 ||
            tiffHeaderOffset.toLong() + TIFF_HEADER_OFFSET_BYTE_COUNT > payload.size.toLong()
        )
            throw ImageReadException(
                "Invalid Exif box: TIFF header offset $tiffHeaderOffset exceeds the box size."
            )

        exifBytes = payload.copyOfRange(
            fromIndex = TIFF_HEADER_OFFSET_BYTE_COUNT + tiffHeaderOffset,
            toIndex = payload.size
        )

        /* Directly parse here to ensure it's valid. */
        tiffContents = TiffReader.read(exifBytes)
    }
}
