/*
 * Copyright 2026 Stefan Oltmann
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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IptcParserEdgeCasesTest {

    /**
     * Wraps raw IPTC record bytes into an 8BIM block with an empty name,
     * including the empty-name padding byte and the padding byte for
     * odd-sized block data.
     */
    private fun wrapIn8BimBlock(recordBytes: ByteArray): ByteArray {

        val padding = if (recordBytes.size % 2 != 0) byteArrayOf(0) else byteArrayOf()

        return byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
            0,
            0, 0, 0, recordBytes.size.toByte()
        ) + recordBytes + padding
    }

    /**
     * Creates an IPTC record with the given caption text.
     */
    private fun captionRecord(text: String): ByteArray {

        val textBytes = text.encodeToByteArray()

        return byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0, textBytes.size.toByte()
        ) + textBytes
    }

    @Test
    fun testParseSkipsInvalidSignature() {

        /* Invalid 8BIM signature bytes followed by a valid block. */
        val bytes = byteArrayOf(0x04, 0x3A, 0x00, 0x00) +
            wrapIn8BimBlock(captionRecord("Found"))

        val metadata = IptcParser.parseIptc(
            bytes = bytes,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Found",
            actual = metadata.records.single().value
        )
    }

    @Test
    fun testParseSkipsIgnoredBlockType() {

        /* An ignored block type (1084) followed by a valid IPTC block. */
        val ignoredBlock = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x3C,
            0
        )

        val metadata = IptcParser.parseIptc(
            bytes = ignoredBlock + wrapIn8BimBlock(captionRecord("Caption")),
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Caption",
            actual = metadata.records.single().value
        )
    }

    @Test
    fun testParseSkipsConsecutiveIgnoredBlockTypes() {

        /* Two ignored blocks followed by a valid IPTC block. */
        val ignoredBlock = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x3C,
            0
        )

        val metadata = IptcParser.parseIptc(
            bytes = ignoredBlock + ignoredBlock + wrapIn8BimBlock(captionRecord("Caption")),
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Caption",
            actual = metadata.records.single().value
        )
    }

    @Test
    fun testParseKeepsBlocksAfterIgnoredBlockType() {

        /*
         * The first block after an ignored block must not be swallowed.
         */
        val ignoredBlock = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x3C,
            0
        )

        val metadata = IptcParser.parseIptc(
            bytes = ignoredBlock +
                wrapIn8BimBlock(captionRecord("One")) +
                wrapIn8BimBlock(captionRecord("Two")),
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf("One", "Two"),
            actual = metadata.records.map { it.value }
        )
    }

    @Test
    fun testParseWithoutApp13Header() {

        /* IPTC block data without the Photoshop APP13 identifier. */
        val blockData = convertHexStringToByteArray(IPTC_BLOCK_DATA_HEX)

        val metadata = IptcParser.parseIptc(
            bytes = wrapIn8BimBlock(blockData),
            startsWithApp13Header = false
        )

        assertTrue(metadata.records.isNotEmpty())
    }

    @Test
    fun testParseRejectsWrongApp13Header() {

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = "not photoshop".encodeToByteArray()
            )
        }
    }

    @Test
    fun testIsPhotoshopApp13Segment() {

        assertTrue(
            IptcParser.isPhotoshopApp13Segment(
                JpegConstants.APP13_IDENTIFIER + byteArrayOf(0, 0)
            )
        )

        assertEquals(
            expected = false,
            actual = IptcParser.isPhotoshopApp13Segment("something else".encodeToByteArray())
        )
    }

    @Test
    fun testParseExtendedRecordLengthTruncated() {

        /*
         * An extended length record (2-byte size 0x8000) without the
         * following 4-byte size must stop parsing gracefully.
         */
        val recordBytes = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0x80.toByte(), 0x00,
            0, 0
        )

        val metadata = IptcParser.parseIptc(
            bytes = wrapIn8BimBlock(recordBytes),
            startsWithApp13Header = false
        )

        assertTrue(metadata.records.isEmpty())
    }

    /**
     * A block whose data ends right after a record tag marker must
     * not read past the end of the data.
     */
    @Test
    fun testParseTruncatedRecordData() {

        /* 8BIM block with a 3-byte data payload ending after a marker. */
        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
            0,
            0, 0, 0, 3,
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(), 0x02, 0x00,
            0
        )

        val metadata = IptcParser.parseIptc(
            bytes = block,
            startsWithApp13Header = false
        )

        assertTrue(metadata.records.isEmpty())
    }

    /**
     * An odd-sized block without its trailing padding byte must be
     * kept and the parse must stop gracefully.
     */
    @Test
    fun testParseMissingBlockPadding() {

        /* 8BIM block with a 3-byte data payload and no padding byte. */
        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
            0,
            0, 0, 0, 3,
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(), 0x02, 0x00
        )

        val metadata = IptcParser.parseIptc(
            bytes = block,
            startsWithApp13Header = false
        )

        assertTrue(metadata.records.isEmpty())
    }

    @Test
    fun testParseBlockWithNameAndPadding() {

        /* A block with a 2-byte name and odd-sized data. */
        val recordData = "Odd".encodeToByteArray()

        val iptcRecord = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0, recordData.size.toByte()
        ) + recordData

        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            2,
            'n'.code.toByte(), 'm'.code.toByte(),
            0,
            0, 0, 0, iptcRecord.size.toByte()
        ) + iptcRecord + byteArrayOf(0)

        val metadata = IptcParser.parseIptc(
            bytes = block,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Odd",
            actual = metadata.records.single().value
        )
    }

    @Test
    fun testParseRejectsInvalidBlockSize() {

        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
            0, 0, 0x10, 0x00
        )

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = block,
                startsWithApp13Header = false
            )
        }
    }

    @Test
    fun testIptcTypes() {

        /* Known types. */
        assertEquals("Keywords (25)", IptcTypes.KEYWORDS.toString())
        assertEquals("Keywords", IptcTypes.KEYWORDS.fieldName)

        /* Unknown types get a placeholder. */
        val unknown = IptcTypes.getIptcType(999)

        assertEquals("Unknown", unknown.fieldName)
        assertEquals(999, unknown.type)
        assertEquals("Unknown (999)", unknown.toString())
    }
}
