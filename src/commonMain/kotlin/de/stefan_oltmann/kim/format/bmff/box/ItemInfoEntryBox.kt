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
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.readNullTerminatedString

/**
 * EIC/ISO 14496-12 infe box.
 */
public class ItemInfoEntryBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.INFE, offset, size, largeSize, payload) {

    public val version: Int

    public val flags: ByteArray

    public val itemId: Int

    public val itemProtectionIndex: Int

    public val itemType: Int

    public val itemName: String

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        /*
         * Version 2 uses a 16-bit item ID; version 3 widened it to 32 bits
         * (ISO/IEC 14496-12, item info entry). Version 3 must not be
         * rejected, or valid modern files would lose all item metadata.
         */
        check(version == SUPPORTED_MIN_VERSION || version == SUPPORTED_MAX_VERSION) {
            "Unsupported INFE version: $version"
        }

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        itemId =
            if (version == SUPPORTED_MIN_VERSION)
                byteReader.read2BytesAsInt("itemId", BMFF_BYTE_ORDER)
            else
                byteReader.read4BytesAsInt("itemId", BMFF_BYTE_ORDER)

        itemProtectionIndex =
            byteReader.read2BytesAsInt("itemProtectionIndex", BMFF_BYTE_ORDER)

        itemType = byteReader.read4BytesAsInt("itemType", BMFF_BYTE_ORDER)

        /* Item name was always empty in test files. */
        itemName = byteReader.readNullTerminatedString("itemName")
    }

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "itemId=$itemId " +
            "itemProtectionIndex=$itemProtectionIndex " +
            "itemType=${itemType.toFourCCTypeString()} " +
            "itemName=$itemName"

    private companion object {

        /* Version 2 uses a 16-bit item ID and introduced the 32-bit item type field. */
        const val SUPPORTED_MIN_VERSION: Int = 2

        /* Version 3 widened the item ID to 32 bits. */
        const val SUPPORTED_MAX_VERSION: Int = 3
    }
}
