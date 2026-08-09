/*
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
package de.stefan_oltmann.kim.format.jpeg.iptc

import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IptcWriterTest {

    @Test
    fun testWriteIptcBlocks() {

        val newIptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(
                listOf(
                    IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
                )
            )
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(newIptcBlock),
            includeApp13Identifier = false
        )

        assertEquals(
            expected = IPTC_HEX,
            actual = iptcBytes.toHex()
        )
    }

    @Test
    fun testWriteIptcBlocksWithApp13Identifier() {

        val newIptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(
                listOf(
                    IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
                )
            )
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(newIptcBlock),
            includeApp13Identifier = true
        )

        assertEquals(
            expected = JpegConstants.APP13_IDENTIFIER.toHex() + IPTC_HEX,
            actual = iptcBytes.toHex()
        )
    }

    @Test
    fun testWriteIptcBlockData() {

        val blockData = IptcWriter.writeIptcBlockData(
            listOf(
                IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
            )
        )

        assertEquals(
            expected = IPTC_BLOCK_DATA_HEX,
            actual = blockData.toHex()
        )
    }

    @Test
    fun testRecordsSortAscendingByType() {

        val records = listOf(
            IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD),
            IptcRecord(IptcTypes.OBJECT_NAME, "Object Name"),
            IptcRecord(IptcTypes.RECORD_VERSION, "2")
        )

        assertEquals(
            expected = listOf(
                IptcTypes.RECORD_VERSION,
                IptcTypes.OBJECT_NAME,
                IptcTypes.KEYWORDS
            ),
            actual = records.sorted().map { it.iptcType }
        )
    }

    @Test
    fun testWriteIptcBlockDataWithExtendedLength() {

        val blockData = IptcWriter.writeIptcBlockData(
            listOf(IptcRecord(IptcTypes.KEYWORDS, "x".repeat(33000)))
        )

        /*
         * Envelope and record version stay standard.
         * The keyword dataset uses the extended-length encoding:
         * 80 00 followed by the 4-byte size 000080e8 (33000).
         */
        assertTrue(
            blockData.toHex().startsWith("1c015a00031b25471c0200000200041c02198000000080e8")
        )
    }

    @Test
    fun testWriteIptcBlockDataWithMaximumStandardLength() {

        val blockData = IptcWriter.writeIptcBlockData(
            listOf(IptcRecord(IptcTypes.KEYWORDS, "x".repeat(32767)))
        )

        /* The 2-byte length field holds 7fff, the maximum standard size. */
        assertTrue(
            blockData.toHex().startsWith("1c015a00031b25471c0200000200041c02197fff")
        )
    }

    @Test
    fun testWriteIptcBlocksWithLargeBlockData() {

        val newIptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = ByteArray(40_000) { 1 }
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(newIptcBlock),
            includeApp13Identifier = false
        )

        /* The block size field is 4 bytes and must hold the full size. */
        assertTrue(
            iptcBytes.toHex().startsWith(
                JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX + "0404" + "00" + "00" + "00009c40"
            )
        )
    }
}
