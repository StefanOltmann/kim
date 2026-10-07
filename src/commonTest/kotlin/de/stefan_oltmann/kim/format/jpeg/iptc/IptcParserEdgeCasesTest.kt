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
import kotlin.test.assertContentEquals
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

    /**
     * Wraps the given data in an 8BIM block of the given type, framed
     * like Photoshop writes it: empty Pascal name with its padding
     * byte and a 4-byte data size.
     */
    private fun wrapInTyped8BimBlock(
        blockType: Int,
        data: ByteArray
    ): ByteArray {

        val padding = if (data.size % 2 != 0) byteArrayOf(0) else byteArrayOf()

        return byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            (blockType shr 8).toByte(), (blockType and 0xFF).toByte(),
            0,
            0,
            0, 0, 0, data.size.toByte()
        ) + data + padding
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

    /**
     * Block types the Photoshop specification recommends not to
     * interpret - like the ICC-untagged flag (0x043C) - are kept as
     * opaque blocks. They are file content, so dropping them would
     * destroy their bytes on an IPTC rewrite.
     */
    @Test
    fun testParseKeepsNonInterpretedBlockTypeOpaque() {

        val untaggedFlag = byteArrayOf(0x01)

        val metadata = IptcParser.parseIptc(
            bytes = wrapInTyped8BimBlock(0x043C, untaggedFlag) +
                wrapIn8BimBlock(captionRecord("Caption")),
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Caption",
            actual = metadata.records.single().value
        )

        val preservedBlock = metadata.rawBlocks.single { block ->
            block.blockType == 0x043C
        }

        assertEquals(
            expected = untaggedFlag.toList(),
            actual = preservedBlock.blockData.toList()
        )
    }

    @Test
    fun testParseKeepsConsecutiveNonInterpretedBlockTypesOpaque() {

        /* Two non-interpreted blocks followed by a valid IPTC block. */
        val metadata = IptcParser.parseIptc(
            bytes = wrapInTyped8BimBlock(0x043C, byteArrayOf(0x01)) +
                wrapInTyped8BimBlock(0x043D, byteArrayOf(0x02, 0x03)) +
                wrapIn8BimBlock(captionRecord("Caption")),
            startsWithApp13Header = false
        )

        assertEquals(
            expected = "Caption",
            actual = metadata.records.single().value
        )

        assertEquals(
            expected = listOf(0x043C, 0x043D),
            actual = metadata.rawBlocks
                .map { it.blockType }
                .filter { it != 0x0404 }
        )
    }

    @Test
    fun testParseKeepsBlocksAfterNonInterpretedBlockType() {

        /*
         * The first block after a non-interpreted block must not be
         * swallowed.
         */
        val metadata = IptcParser.parseIptc(
            bytes = wrapInTyped8BimBlock(0x043C, byteArrayOf(0x01)) +
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
     * The extended length 0x7FFFFFFF is the largest positive Int. It is
     * larger than any possible record data, so the record header lies
     * and the read fails instead of overflowing the index on the next
     * loop iteration.
     */
    @Test
    fun testParseExtendedRecordLengthOverflows() {

        val recordBytes = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0x80.toByte(), 0x00,
            0x7F.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = wrapIn8BimBlock(recordBytes),
                startsWithApp13Header = false
            )
        }
    }

    /**
     * The 8-byte extended length form carries unsigned 64-bit values. A
     * length with the sign bit set turns negative in the signed Long
     * accumulator, would slip past the too-large guard and silently
     * swallow the record - the read must fail like for any other lying
     * length.
     */
    @Test
    fun testParseExtendedRecordLengthWithSignBitSetFailsTheRead() {

        val recordBytes = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0x80.toByte(), 0x08,
            0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = wrapIn8BimBlock(recordBytes),
                startsWithApp13Header = false
            )
        }
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
     * A standard record whose file-controlled length exceeds the
     * remaining block bytes is unreadable - the block itself is
     * complete, so the record header lies. Per the strict read policy
     * the read fails instead of keeping what was parsed so far, which
     * would silently drop every record behind it on a rewrite.
     */
    @Test
    fun testParseStandardRecordBeyondRemainingData() {

        /* ObjectName record declaring 64 data bytes - only 7 remain. */
        val recordBytes = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0x00, 0x40
        ) + "partial".encodeToByteArray()

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = wrapIn8BimBlock(recordBytes),
                startsWithApp13Header = false
            )
        }
    }

    /**
     * A corrupt record in the middle of the block must fail the whole
     * read: the graceful stop kept only the records before it, and the
     * rewrite built from those silently dropped every record behind the
     * corruption.
     */
    @Test
    fun testCorruptRecordDropsNoRecordsBehindIt() {

        /* ObjectName "Keep" declaring 4 bytes - complete. */
        val goodRecord = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            25,
            0x00, 0x04
        ) + "Keep".encodeToByteArray()

        /* Caption record declaring 64 bytes - only 7 follow. */
        val corruptRecord = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            6,
            0x00, 0x40
        ) + "partial".encodeToByteArray()

        val blockBytes = goodRecord + corruptRecord + captionRecord("Lost")

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = wrapIn8BimBlock(blockBytes),
                startsWithApp13Header = false
            )
        }
    }

    /**
     * The block name is a Pascal string whose length byte is unsigned per
     * the Photoshop IRB spec. A length above 127 must be read as the
     * unsigned value, or the whole block - and everything after it - is
     * silently dropped.
     */
    @Test
    fun testParsePhotoshopBlockWithLongName() {

        val nameBytes = ByteArray(200) { it.toByte() }

        val captionBytes = captionRecord("One")

        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            200.toByte()
        ) + nameBytes +
            byteArrayOf(0) +
            byteArrayOf(0, 0, 0, captionBytes.size.toByte()) +
            captionBytes

        val metadata = IptcParser.parseIptc(
            bytes = block,
            startsWithApp13Header = false
        )

        assertEquals(
            expected = listOf("One"),
            actual = metadata.records.map { it.value }
        )
    }

    /**
     * The data can end right after an 8BIM signature, e.g. after a
     * truncated write. Like the tolerated EOF inside the block data,
     * that must stop the parse gracefully instead of failing it.
     */
    @Test
    fun testParseToleratesEofAfterBlockSignature() {

        val metadata = IptcParser.parseIptc(
            bytes = byteArrayOf(0x38, 0x42, 0x49, 0x4D),
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

    /**
     * A 1:90-flagged keywords record with a malformed UTF-8 sequence
     * cannot be read cleanly: replacement-mode decoding would fabricate
     * U+FFFD into the keyword, so the parse fails instead.
     */
    @Test
    fun testUtf8FlaggedRecordWithBrokenSequenceFailsTheRead() {

        /* 1:90 envelope announcing UTF-8 (ESC % G). */
        val envelope = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER.toByte(),
            IptcParser.CODED_CHARACTER_SET_IPTC_CODE.toByte(),
            0, 3,
            0x1B, 0x25, 0x47
        )

        /*
         * Keywords record whose value ends in a truncated 2-byte UTF-8
         * lead: the declared length is consistent with the block, so the
         * failure must come from the strict UTF-8 decode, not the length
         * guard.
         */
        val keywords = byteArrayOf(
            IptcConstants.IPTC_RECORD_TAG_MARKER.toByte(),
            IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER.toByte(),
            IptcTypes.KEYWORDS.type.toByte(),
            0, 2
        ) + "H".encodeToByteArray() + 0xC3.toByte()

        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = wrapIn8BimBlock(envelope + keywords),
                startsWithApp13Header = false
            )
        }
    }

    @Test
    fun testParseRejectsInvalidBlockSize() {

        /*
         * A complete block size field that announces far more data
         * than the block holds.
         */
        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
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

    /**
     * A block size with the sign bit set is read as a negative number.
     * It must fail like an oversized size instead of slipping past the
     * size check and silently truncating the parse - a rewrite would
     * then destroy the unparsed tail.
     */
    @Test
    fun testParseRejectsNegativeBlockSize() {

        /* Block size field 0x80000000, read as a negative Int. */
        val block = byteArrayOf(
            0x38, 0x42, 0x49, 0x4D,
            0x04, 0x04,
            0,
            0,
            0x80.toByte(), 0, 0, 0
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

    /**
     * IIM datasets the specification defines as binary (2:125 rasterized
     * caption, 2:202 objectData preview) have no text form: re-encoding
     * them through a String would corrupt every byte >= 0x80 and grow
     * the dataset. They must be carried through as raw bytes so a
     * rewrite re-emits them exactly.
     */
    @Test
    fun testBinaryRecord2DatasetIsCarriedThroughVerbatim() {

        val binaryDataset = byteArrayOf(
            0x1C, 0x02, 0xCA.toByte(), 0x00, 0x03, 0x89.toByte(), 0x50.toByte(), 0xFF.toByte()
        )

        val keywordsDataset = byteArrayOf(
            0x1C, 0x02, 0x19, 0x00, 0x03, 0x6B, 0x65, 0x79
        )

        val metadata = IptcParser.parseIptc(
            bytes = wrapIn8BimBlock(binaryDataset + keywordsDataset),
            startsWithApp13Header = false
        )

        /* The keywords decode as text... */
        assertEquals(1, metadata.records.size)

        /* ... while the binary dataset is carried through verbatim. */
        assertEquals(1, metadata.foreignDatasets.size)

        val rewritten = IptcWriter.writeIptcBlockData(
            metadata.records,
            metadata.foreignDatasets
        )

        val verbatimBinary = byteArrayOf(
            0x1C, 0x02, 0xCA.toByte(), 0x00, 0x03, 0x89.toByte(), 0x50.toByte(), 0xFF.toByte()
        )

        assertContentEquals(verbatimBinary, metadata.foreignDatasets.single())

        /* The rewrite must re-emit the binary dataset byte-exact. */
        assertTrue(rewritten.toList().containsAll(verbatimBinary.toList()))
    }
}
