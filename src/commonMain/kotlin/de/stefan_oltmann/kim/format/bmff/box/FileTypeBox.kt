/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.toFourCCTypeString
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BOX_HEADER_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt

/**
 * EIC/ISO 14496-12 ftyp box.
 */
public class FileTypeBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.FTYP, offset, size, largeSize, payload) {

    public val majorBrand: String

    public val minorBrand: String

    public val compatibleBrands: List<String>

    init {

        val byteReader = ByteArrayByteReader(payload)

        majorBrand = byteReader
            .read4BytesAsInt("majorBrand", BMFF_BYTE_ORDER)
            .toFourCCTypeString()

        minorBrand = byteReader
            .read4BytesAsInt("minorBrand", BMFF_BYTE_ORDER)
            .toFourCCTypeString()

        /*
         * The payload contains the major brand (4 bytes), the minor version
         * (4 bytes) and the compatible brands with 4 bytes each.
         */
        val brandCountFromLength = (actualLength.toInt() - BOX_HEADER_LENGTH - 8) / 4

        /*
         * Corrupt files may claim more brands than the payload holds.
         * Clamp to the brands that are actually present.
         */
        val maxBrandCount = ((payload.size - 8) / 4).coerceAtLeast(0)

        val brandCount = brandCountFromLength.coerceIn(0, maxBrandCount)

        val brands = mutableListOf<String>()

        repeat(brandCount) {
            brands.add(
                byteReader
                    .read4BytesAsInt("brand $it", BMFF_BYTE_ORDER)
                    .toFourCCTypeString()
            )
        }

        compatibleBrands = brands
    }

    override fun toString(): String =
        "$type major=$majorBrand minor=$minorBrand compatible=$compatibleBrands"

    internal companion object {
        const val JXL_BRAND = "jxl "
        const val CR3_BRAND = "crx "
        const val MP4V2_BRAND = "mp42"
        const val MOV_BRAND = "qt  "
    }
}
