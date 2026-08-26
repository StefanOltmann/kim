/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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

import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes

/**
 * EIC/ISO 14496-12 iinf box.
 */
public class ItemInformationBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray,
    depth: Int = 0
) : Box(BoxType.IINF, offset, size, largeSize, payload), BoxContainer {

    public val version: Int

    public val flags: ByteArray

    public val entryCount: Int

    public val map: Map<Int, ItemInfoEntryBox>

    override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        entryCount = if (version == 0)
            byteReader.read2BytesAsInt("entryCount", BMFF_BYTE_ORDER)
        else
            byteReader.read4BytesAsInt("entryCount", BMFF_BYTE_ORDER)

        /*
         * The reader starts after the entry count field, so positionOffset
         * accounts for it. offsetShift maps payload-relative positions to
         * file positions, which is always the box header (offset + 8).
         */
        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            positionOffset = 4L + if (version == 0) 2 else 4,
            offsetShift = offset + 8,
            parentBoxType = type,
            depth = depth
        )

        val map = mutableMapOf<Int, ItemInfoEntryBox>()

        for (box in boxes) {

            /*
             * The iinf payload may contain other box types in the future
             * (ISO 14496-12 keeps the entry format open), so unknown
             * entries are skipped in this lookup index instead of crashing.
             *
             * Attention: This is a lookup index only. The unknown entries
             * stay in [boxes] and the raw payload is preserved byte for
             * byte, so rewrites cannot lose them. See "Never destroy
             * metadata" in the [Kim] documentation.
             */
            val entry = box as? ItemInfoEntryBox ?: continue

            map[entry.itemId] = entry
        }

        this.map = map
    }

    override fun toString(): String =
        "$type Box version=$version flags=${flags.toHex()} ($entryCount entries)"
}
