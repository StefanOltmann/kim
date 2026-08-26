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
package de.stefan_oltmann.kim.format.jpeg.iptc

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.toHex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IptcParserTest {

    @Test
    fun testParseIptc() {

        val iptcBytes = convertHexStringToByteArray(IPTC_HEX)

        val actualIptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(1, actualIptc.records.size)
        assertEquals(1, actualIptc.rawBlocks.size)

        assertEquals(
            expected = IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD),
            actual = actualIptc.records.first()
        )

        val rawBlock = actualIptc.rawBlocks.first()

        assertEquals(
            expected = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            actual = rawBlock.blockType
        )

        assertTrue(rawBlock.blockNameBytes.isEmpty())

        assertEquals(
            expected = IPTC_BLOCK_DATA_HEX,
            actual = rawBlock.blockData.toHex()
        )
    }

    @Test
    fun testParseIptcWithExtendedLengthDataset() {

        val value = "x".repeat(33000)

        val iptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(
                listOf(IptcRecord(IptcTypes.KEYWORDS, value))
            )
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(iptcBlock),
            includeApp13Identifier = false
        )

        val actualIptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf(IptcRecord(IptcTypes.KEYWORDS, value)),
            actual = actualIptc.records
        )
    }

    /**
     * Regression test: the CodedCharacterSet record may be padded with
     * spaces. The padding must not prevent the UTF-8 escape sequence
     * detection, or umlauts would be decoded as Latin-1 mojibake.
     */
    @Test
    fun testParseIptcDetectsUtf8WithPaddedCodedCharacterSet() {

        /*
         * CodedCharacterSet (1:90) with a space-padded "ESC % G" value,
         * followed by a Keywords (2:25) record with UTF-8 umlauts.
         */
        val blockData = convertHexStringToByteArray(
            "1c015a000420" + "1b2547" +
                "1c02190011c38475c39f6572737420736368c3b66e21"
        )

        val iptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = blockData
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(iptcBlock),
            includeApp13Identifier = false
        )

        val actualIptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf(IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)),
            actual = actualIptc.records
        )
    }
}
