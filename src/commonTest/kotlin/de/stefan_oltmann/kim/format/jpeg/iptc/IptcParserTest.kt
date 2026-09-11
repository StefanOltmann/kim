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

    /**
     * The IPTC extended-length encoding stores the number of bytes of the
     * length field in its low bits. ExifTool reads any length field size
     * between 1 and 8; the two byte variant is the smallest one a writer
     * can legally produce.
     */
    @Test
    fun testParseIptcWithTwoByteExtendedLengthField() {

        val value = "y".repeat(300)

        val dataset = byteArrayOf(
            0x1C.toByte(), 0x02, 0x19,
            0x80.toByte(), 0x02,
            0x01, 0x2C
        ) + value.encodeToByteArray()

        val iptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = dataset
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(iptcBlock),
            includeApp13Identifier = false
        )

        val iptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf(IptcRecord(IptcTypes.KEYWORDS, value)),
            actual = iptc.records
        )
    }

    /**
     * Some broken writers store IPTC with 32-bit word swapped bytes, so
     * the marker ends up at index 3 of every word. ExifTool detects the
     * swap, reads the data after swapping it back - Kim must not return
     * garbage for such files.
     */
    @Test
    fun testParseIptcDetectsByteSwappedData() {

        val iptcData = convertHexStringToByteArray(IPTC_BLOCK_DATA_HEX)

        /* Like ExifTool, the data is padded to full 32-bit words. */
        val paddedSize = iptcData.size + (4 - iptcData.size % 4) % 4

        val swapped = ByteArray(paddedSize)

        for (group in 0 until paddedSize step 4) {
            for (offset in 0 until 4) {
                val source = group + offset
                swapped[group + (3 - offset)] =
                    if (source < iptcData.size) iptcData[source] else 0
            }
        }

        val iptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = swapped
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(iptcBlock),
            includeApp13Identifier = false
        )

        val iptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf(IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)),
            actual = iptc.records
        )
    }
}
