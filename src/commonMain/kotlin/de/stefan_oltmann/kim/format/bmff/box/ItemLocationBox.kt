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

import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.Extent
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.readXBytesAtInt
import de.stefan_oltmann.kim.input.skipBytes

/**
 * EIC/ISO 14496-12 iloc box.
 */
public class ItemLocationBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.ILOC, offset, size, largeSize, payload) {

    /**
     * The version of the box.
     */
    public val version: Int

    /**
     * Flags that provide additional information about the box.
     */
    public val flags: ByteArray

    /**
     * The size (in bytes) of the offset field in each item location entry.
     */
    public val offsetSize: Int

    /**
     * The size (in bytes) of the length field in each item location entry.
     */
    public val lengthSize: Int

    /**
     * The size (in bytes) of the base offset field in each item location entry.
     */
    public val baseOffsetSize: Int

    /**
     * This part contains the actual entries describing the location of items within the file.
     */
    public val indexSize: Int

    public val itemCount: Int

    public val extents: List<Extent>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        /* Fail fast if the code needs to be updated for a newer version. */
        check(version in 0..2) {
            "Unsupported ILOC version: $version"
        }

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        val offsetAndLengthSize = byteReader.readByteAsInt()
        offsetSize = (offsetAndLengthSize and UPPER_NIBBLE_MASK) shr NIBBLE_SHIFT
        lengthSize = offsetAndLengthSize and LOWER_NIBBLE_MASK

        val baseOffsetSizeAndIndexSize = byteReader.readByteAsInt()
        baseOffsetSize = (baseOffsetSizeAndIndexSize and UPPER_NIBBLE_MASK) shr NIBBLE_SHIFT

        indexSize = if (version in 1..2)
            baseOffsetSizeAndIndexSize and LOWER_NIBBLE_MASK
        else
            0 // Unused

        itemCount = if (version < 2)
            byteReader.read2BytesAsInt("itemCount", BMFF_BYTE_ORDER)
        else if (version == 2)
            byteReader.read4BytesAsInt("itemCount", BMFF_BYTE_ORDER)
        else
            error("Unknown version $version")

        val extents = mutableListOf<Extent>()

        repeat(itemCount) {

            val itemId: Int = if (version < 2)
                byteReader.read2BytesAsInt("itemId", BMFF_BYTE_ORDER)
            else if (version == 2)
                byteReader.read4BytesAsInt("itemId", BMFF_BYTE_ORDER)
            else
                error("Unknown version $version")

            /*
             * 0 means the offsets are absolute file positions, 1 means
             * they are relative to the idat box payload.
             */
            val constructionMethod: Int = if (version in 1..2)
                byteReader.read2BytesAsInt("constructionMethod", BMFF_BYTE_ORDER)
            else
                0

            byteReader.skipBytes("dataReferenceIndex", 2)

            /*
             * The spec allows field sizes of zero - the value is absent -
             * and 1, 2, 4 or 8 bytes. Sizes 1 and 2 occur in real files;
             * treating them as zero silently mislocated every extent of
             * the item.
             */
            val baseOffset: Long =
                if (baseOffsetSize == 0)
                    0
                else
                    byteReader.readXBytesAtInt("baseOffset", baseOffsetSize, BMFF_BYTE_ORDER)

            val extentCount = byteReader.read2BytesAsInt("extentCount", BMFF_BYTE_ORDER)

            repeat(extentCount) {

                val extentIndex: Long? = if (version in 1..2 && indexSize > 0)
                    byteReader.readXBytesAtInt("extentIndex", indexSize, BMFF_BYTE_ORDER)
                else
                    null

                /*
                 * The spec allows a field size of zero, which means the
                 * value is absent and contributes nothing.
                 */
                val extentOffset: Long =
                    if (offsetSize == 0) 0 else byteReader.readXBytesAtInt("extentOffset", offsetSize, BMFF_BYTE_ORDER)

                val extentLength: Long =
                    if (lengthSize == 0) 0 else byteReader.readXBytesAtInt("extentLength", lengthSize, BMFF_BYTE_ORDER)

                extents.add(
                    Extent(
                        itemId = itemId,
                        index = extentIndex,
                        offset = extentOffset + baseOffset,
                        length = extentLength,
                        constructionMethod = constructionMethod
                    )
                )
            }
        }

        /*
         * Sort by offset to support reading fields in order.
         * Warning: This is important for other logic to function properly.
         */
        extents.sortBy { it.offset }

        this.extents = extents
    }

    override fun toString(): String =
        "$type " +
            "offsetSize=$offsetSize " +
            "lengthSize=$lengthSize " +
            "baseOffsetSize=$baseOffsetSize " +
            "indexSize=$indexSize " +
            "itemCount=$itemCount " +
            "extents=$extents"

    private companion object {

        /* Bit mask for the upper nibble of the size byte */
        const val UPPER_NIBBLE_MASK = 0xF0

        /* Bit mask for the lower nibble of the size byte */
        const val LOWER_NIBBLE_MASK = 0x0F

        /* Shift to move the upper nibble to the lower position */
        const val NIBBLE_SHIFT = 4
    }
}
