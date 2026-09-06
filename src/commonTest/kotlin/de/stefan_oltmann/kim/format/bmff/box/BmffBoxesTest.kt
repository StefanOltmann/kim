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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.Extent
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BmffBoxesTest {

    @Test
    fun testFileTypeBox() {

        /*
         * Payload: major brand, minor version and two compatible brands,
         * like in real CR3 files.
         */
        val payload =
            "heic".encodeToByteArray() +
                "0000".encodeToByteArray() +
                "mif1".encodeToByteArray() +
                "heix".encodeToByteArray()

        val box = FileTypeBox(
            offset = 0,
            size = 8 + payload.size.toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("heic", box.majorBrand)
        assertEquals("0000", box.minorBrand)
        assertEquals(listOf("mif1", "heix"), box.compatibleBrands)

        assertEquals(
            expected = "ftyp major=heic minor=0000 compatible=[mif1, heix]",
            actual = box.toString()
        )
    }

    @Test
    fun testFileTypeBoxWithSingleBrand() {

        /* Like real MP4 files with one compatible brand. */
        val payload =
            "mp42".encodeToByteArray() +
                "0000".encodeToByteArray() +
                "mp42".encodeToByteArray()

        val box = FileTypeBox(
            offset = 0,
            size = 8 + payload.size.toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("mp42", box.majorBrand)
        assertEquals(listOf("mp42"), box.compatibleBrands)
    }

    @Test
    fun testFileTypeBoxWithoutBrands() {

        /* A box with only major brand and minor version. */
        val payload =
            "heic".encodeToByteArray() +
                "0000".encodeToByteArray()

        val box = FileTypeBox(
            offset = 0,
            size = 8 + payload.size.toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("heic", box.majorBrand)
        assertEquals(emptyList<String>(), box.compatibleBrands)
    }

    @Test
    fun testFileTypeBoxClampsCorruptBrandCount() {

        /*
         * The length claims two compatible brands, but the payload
         * only holds one. The missing brand must not crash the parse.
         */
        val payload =
            "heic".encodeToByteArray() +
                "0000".encodeToByteArray() +
                "mif1".encodeToByteArray()

        val box = FileTypeBox(
            offset = 0,
            size = 8 + payload.size.toLong() + 4,
            largeSize = null,
            payload = payload
        )

        assertEquals("heic", box.majorBrand)
        assertEquals(listOf("mif1"), box.compatibleBrands)
    }

    @Test
    fun testFileTypeBoxOfRealFiles() {

        /* CR3: major brand "crx " with two compatible brands. */
        val cr3Bytes = de.stefan_oltmann.kim.testdata.KimTestData.getBytesOf(
            de.stefan_oltmann.kim.testdata.KimTestData.CR3_TEST_IMAGE_INDEX
        )

        val cr3Box = de.stefan_oltmann.kim.format.bmff.BoxReader.readBoxes(
            byteReader = de.stefan_oltmann.kim.input.ByteArrayByteReader(cr3Bytes),
            stopAfterMetadataRead = false
        ).filterIsInstance<FileTypeBox>().first()

        assertEquals(FileTypeBox.CR3_BRAND, cr3Box.majorBrand)
        assertEquals(listOf("crx ", "isom"), cr3Box.compatibleBrands)

        /* MP4: major brand "mp42" with one compatible brand. */
        val mp4Bytes = de.stefan_oltmann.kim.testdata.KimTestData.getBytesOf(
            de.stefan_oltmann.kim.testdata.KimTestData.MP4_TEST_VIDEO_INDEX
        )

        val mp4Box = de.stefan_oltmann.kim.format.bmff.BoxReader.readBoxes(
            byteReader = de.stefan_oltmann.kim.input.ByteArrayByteReader(mp4Bytes),
            stopAfterMetadataRead = false
        ).filterIsInstance<FileTypeBox>().first()

        assertEquals("mp42", mp4Box.majorBrand)
        assertEquals(listOf("mp42"), mp4Box.compatibleBrands)
    }

    @Test
    fun testItemLocationBoxVersion0() {

        /*
         * version 0, no index, 4-byte offsets, 4-byte lengths,
         * one item with one extent.
         */
        val payload = byteArrayOf(
            0, 0, 0, 0,
            0x44,
            0x00,
            0, 1,
            0, 1,
            0, 0,
            0, 1,
            0, 0, 0, 200.toByte(),
            0, 0, 0, 50
        )

        val box = ItemLocationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.version)
        assertEquals(4, box.offsetSize)
        assertEquals(4, box.lengthSize)
        assertEquals(0, box.baseOffsetSize)
        assertEquals(0, box.indexSize)
        assertEquals(1, box.itemCount)
        assertEquals(
            expected = listOf(Extent(itemId = 1, index = null, offset = 200, length = 50)),
            actual = box.extents
        )

        assertTrue(box.toString().startsWith("iloc offsetSize=4"))
    }

    /**
     * Regression test: the spec allows an extent field size of zero,
     * which means the value is absent. Such files must parse instead of
     * failing with "Illegal byteCount specified: 0".
     */
    @Test
    fun testItemLocationBoxWithAbsentExtentFields() {

        /*
         * version 0, offset size 0, length size 4: the item has no
         * offset and a length of zero.
         */
        val payload = byteArrayOf(
            0, 0, 0, 0,
            0x04,
            0x00,
            0, 1,
            0, 1,
            0, 0,
            0, 1,
            0, 0, 0, 50
        )

        val box = ItemLocationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.offsetSize)
        assertEquals(4, box.lengthSize)

        assertEquals(
            expected = listOf(Extent(itemId = 1, index = null, offset = 0, length = 50)),
            actual = box.extents
        )
    }

    /**
     * Regression test: the spec allows base offset field sizes of 1, 2, 4
     * and 8 bytes. Size 2 previously fell into a silent zero case that
     * also desynchronized the stream.
     */
    @Test
    fun testItemLocationBoxWithTwoByteBaseOffset() {

        /*
         * version 0, offset size 4, length size 4,
         * base offset size 2, one item with one extent.
         */
        val payload = byteArrayOf(
            0, 0, 0, 0,
            0x44,
            0x20,
            0, 1,
            0, 1,
            0, 0,
            0x10, 0x00,
            0, 1,
            0, 0, 0, 100.toByte(),
            0, 0, 0, 50
        )

        val box = ItemLocationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(2, box.baseOffsetSize)

        val extent = box.extents.single()

        /* The 2-byte base offset (4096) must be added to the extent. */
        assertEquals(4196L, extent.offset)
        assertEquals(50L, extent.length)
    }

    @Test
    fun testItemLocationBoxVersion2WithEightByteOffsets() {
        /*
         * version 2, index size 8, base offset size 8, 8-byte
         * item id and offset/length values.
         */
        val payload = byteArrayOf(
            2, 0, 0, 0,
            0x88.toByte(),
            0x88.toByte(),
            0, 0, 0, 1,
            0, 0, 0, 1,
            0, 0,
            0, 0,
            0, 0, 0, 0, 0, 0, 0, 42,
            0, 1,
            0, 0, 0, 0, 0, 0, 0, 7,
            0, 0, 0, 0, 0, 0, 0, 9,
            0, 0, 0, 0, 0, 0, 0, 5
        )

        val box = ItemLocationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(2, box.version)
        assertEquals(8, box.offsetSize)
        assertEquals(8, box.lengthSize)
        assertEquals(8, box.baseOffsetSize)
        assertEquals(8, box.indexSize)
        assertEquals(1, box.itemCount)

        val extent = box.extents.single()
        assertEquals(1, extent.itemId)
        assertEquals(7, extent.index)
        assertEquals(42 + 9, extent.offset)
        assertEquals(5, extent.length)
        assertEquals(0, extent.constructionMethod)
    }

    /**
     * A construction method of 1 marks the extent offsets as relative to
     * the idat box instead of the file start. The value must be parsed
     * so consumers can skip such extents instead of misreading them.
     */
    @Test
    fun testItemLocationBoxParsesConstructionMethod() {

        /* version 1, absolute offsets (construction method 0). */
        val absolutePayload = byteArrayOf(
            1, 0, 0, 0,
            0x44,
            0x00,
            0, 1,
            0, 1,
            0, 0,
            0, 0,
            0, 1,
            0, 0, 0, 200.toByte(),
            0, 0, 0, 50
        )

        val absoluteBox = ItemLocationBox(
            offset = 0,
            size = absolutePayload.size.toLong() + 8,
            largeSize = null,
            payload = absolutePayload
        )

        assertEquals(0, absoluteBox.extents.single().constructionMethod)

        /* version 1, idat-relative offsets (construction method 1). */
        val idatRelativePayload = byteArrayOf(
            1, 0, 0, 0,
            0x44,
            0x00,
            0, 1,
            0, 1,
            0, 1,
            0, 0,
            0, 1,
            0, 0, 0, 200.toByte(),
            0, 0, 0, 50
        )

        val idatRelativeBox = ItemLocationBox(
            offset = 0,
            size = idatRelativePayload.size.toLong() + 8,
            largeSize = null,
            payload = idatRelativePayload
        )

        assertEquals(1, idatRelativeBox.extents.single().constructionMethod)
    }

    /**
     * Extents with an idat-relative construction method cannot be
     * resolved without idat support. They must be skipped instead of
     * reading image data as metadata.
     */
    @Test
    fun testMetaBoxTopLevelSkipsIdatRelativeExtents() {

        val hdlrBox = createHdlrBox()
        val pitmBox = createPitmBox(itemId = 1)
        val iinfBox = createIinfBoxWithExifEntry(itemId = 1)

        /*
         * One EXIF item whose only extent uses construction method 1.
         */
        val ilocPayload = byteArrayOf(
            1, 0, 0, 0,
            0x44,
            0x00,
            0, 1,
            0, 1,
            0, 1,
            0, 0,
            0, 1,
            0, 0, 0, 200.toByte(),
            0, 0, 0, 50
        )

        val ilocBox = createBox(BoxType.ILOC, ilocPayload)

        val metaPayload =
            byteArrayOf(0, 0, 0, 0) + hdlrBox + pitmBox + iinfBox + ilocBox

        val metaBox = MetaBoxTopLevel(
            offset = 0,
            size = metaPayload.size.toLong() + 8,
            largeSize = null,
            payload = metaPayload
        )

        assertTrue(metaBox.itemLocationBox.extents.single().constructionMethod == 1)

        assertTrue(metaBox.findMetadataOffsets().isEmpty())
    }

    @Test
    fun testItemLocationBoxRejectsUnsupportedVersion() {

        val payload = byteArrayOf(
            3, 0, 0, 0,
            0x44,
            0x00,
            0, 1
        )

        assertFailsWith<IllegalStateException> {
            ItemLocationBox(
                offset = 0,
                size = payload.size.toLong() + 8,
                largeSize = null,
                payload = payload
            )
        }
    }

    @Test
    fun testItemInfoEntryBox() {

        val payload = byteArrayOf(
            2, 0, 0, 0,
            0, 1,
            0, 0,
            0x6D, 0x69, 0x66, 0x31
        ) + "ItemName\u0000".encodeToByteArray()

        val box = ItemInfoEntryBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(2, box.version)
        assertEquals(1, box.itemId)
        assertEquals(0, box.itemProtectionIndex)
        assertEquals(0x6D696631, box.itemType)
        assertEquals("ItemName", box.itemName)

        assertTrue(box.toString().startsWith("infe version=2"))
    }

    /**
     * Regression test: infe version 3 (added by newer ISO 14496-12
     * editions) widened the item ID to 32 bits and must be parsed with
     * the correct field widths instead of rejecting the whole item
     * information or shifting every following field.
     */
    @Test
    fun testItemInfoEntryBoxVersion3() {

        val payload = byteArrayOf(
            3, 0, 0, 0,
            0, 0, 0, 1,
            0, 0,
            0x6D, 0x69, 0x66, 0x31
        ) + "ItemName\u0000".encodeToByteArray()

        val box = ItemInfoEntryBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(3, box.version)
        assertEquals(1, box.itemId)
        assertEquals(0, box.itemProtectionIndex)
        assertEquals(0x6D696631, box.itemType)
        assertEquals("ItemName", box.itemName)

        assertTrue(box.toString().startsWith("infe version=3"))
    }

    @Test
    fun testItemInfoEntryBoxRejectsUnsupportedVersion() {

        assertFailsWith<IllegalStateException> {
            ItemInfoEntryBox(
                offset = 0,
                size = 10,
                largeSize = null,
                payload = byteArrayOf(0, 0, 0, 0, 0, 1)
            )
        }
    }

    @Test
    fun testItemInformationBox() {

        /*
         * iinf payload: version 0, flags, entry count 1,
         * then a full infe box (header + payload).
         */
        val infePayload = byteArrayOf(
            2, 0, 0, 0,
            0, 1,
            0, 0,
            0x6D, 0x69, 0x66, 0x31
        ) + "\u0000".encodeToByteArray()

        val infeBox = byteArrayOf(
            0, 0, 0, (infePayload.size + 8).toByte(),
            'i'.code.toByte(), 'n'.code.toByte(), 'f'.code.toByte(), 'e'.code.toByte()
        ) + infePayload

        val payload = byteArrayOf(
            0, 0, 0, 0,
            0, 1
        ) + infeBox

        val box = ItemInformationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.version)
        assertEquals(1, box.entryCount)
        assertEquals(1, box.map.size)
        assertEquals(1, box.map.getValue(1).itemId)
    }

    @Test
    fun testPrimaryItemBox() {

        /* Version 0 with 2-byte item id. */
        val version0 = PrimaryItemBox(
            offset = 0,
            size = 10,
            largeSize = null,
            payload = byteArrayOf(0, 0, 0, 0, 0, 42)
        )

        assertEquals(0, version0.version)
        assertEquals(42, version0.itemId)
        assertTrue(version0.toString().startsWith("pitm version=0"))

        /* Version 1 with 4-byte item id. */
        val version1 = PrimaryItemBox(
            offset = 0,
            size = 12,
            largeSize = null,
            payload = byteArrayOf(1, 0, 0, 0, 0, 0, 0, 43)
        )

        assertEquals(1, version1.version)
        assertEquals(43, version1.itemId)
    }

    @Test
    fun testMetaBox() {

        val hdlrPayload = byteArrayOf(
            0, 0, 0, 0,
            0, 0, 0, 0,
            'p'.code.toByte(), 'i'.code.toByte(), 'c'.code.toByte(), 't'.code.toByte(),
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        ) + "Main Image\u0000".encodeToByteArray()

        val hdlrBox = byteArrayOf(
            0, 0, 0, (hdlrPayload.size + 8).toByte(),
            'h'.code.toByte(), 'd'.code.toByte(), 'l'.code.toByte(), 'r'.code.toByte()
        ) + hdlrPayload

        val payload = byteArrayOf(
            0, 0, 0, 0
        ) + hdlrBox

        val box = MetaBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.version)
        assertEquals(BoxType.HDLR, box.handlerReferenceBox.type)
        assertEquals("pict", box.handlerReferenceBox.handlerType)
        assertEquals("Main Image", box.handlerReferenceBox.name)
    }

    @Test
    fun testTrackBox() {

        val tkhdPayload = byteArrayOf(0, 0, 0, 0)

        val tkhdBox = byteArrayOf(
            0, 0, 0, (tkhdPayload.size + 8).toByte(),
            't'.code.toByte(), 'k'.code.toByte(), 'h'.code.toByte(), 'd'.code.toByte()
        ) + tkhdPayload

        val hdlrPayload = byteArrayOf(
            0, 0, 0, 0,
            0, 0, 0, 0,
            'v'.code.toByte(), 'i'.code.toByte(), 'd'.code.toByte(), 'e'.code.toByte(),
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        ) + "\u0000".encodeToByteArray()

        val hdlrBox = byteArrayOf(
            0, 0, 0, (hdlrPayload.size + 8).toByte(),
            'h'.code.toByte(), 'd'.code.toByte(), 'l'.code.toByte(), 'r'.code.toByte()
        ) + hdlrPayload

        val mdiaPayload = hdlrBox

        val mdiaBox = byteArrayOf(
            0, 0, 0, (mdiaPayload.size + 8).toByte(),
            'm'.code.toByte(), 'd'.code.toByte(), 'i'.code.toByte(), 'a'.code.toByte()
        ) + mdiaPayload

        /* The payload contains the sub-boxes with their headers. */
        val trackPayload = tkhdBox + mdiaBox

        val box = TrackBox(
            offset = 0,
            size = trackPayload.size.toLong() + 8,
            largeSize = null,
            payload = trackPayload
        )

        assertEquals(BoxType.TKHD, box.trackHeaderBox.type)
        assertEquals(BoxType.MDIA, box.mediaBox.type)
        assertTrue(box.boxes.size >= 2)
    }

    @Test
    fun testTrackBoxWithSmallSubBoxes() {

        /*
         * Small sub-boxes after the first one must still be read.
         * The mdia box is only 8 bytes (header without payload).
         */
        val tkhdPayload = byteArrayOf(0, 0, 0, 0)

        val tkhdBox = byteArrayOf(
            0, 0, 0, (tkhdPayload.size + 8).toByte(),
            't'.code.toByte(), 'k'.code.toByte(), 'h'.code.toByte(), 'd'.code.toByte()
        ) + tkhdPayload

        val mdiaBox = byteArrayOf(
            0, 0, 0, 8,
            'm'.code.toByte(), 'd'.code.toByte(), 'i'.code.toByte(), 'a'.code.toByte()
        )

        val trackPayload = tkhdBox + mdiaBox

        val box = TrackBox(
            offset = 0,
            size = trackPayload.size.toLong() + 8,
            largeSize = null,
            payload = trackPayload
        )

        assertEquals(BoxType.TKHD, box.trackHeaderBox.type)
        assertEquals(BoxType.MDIA, box.mediaBox.type)
        assertEquals(2, box.boxes.size)

        /* The mdia box is reported at its real payload offset. */
        assertEquals(20L, box.mediaBox.offset)
    }

    @Test
    fun testTrackBoxRejectsMissingBoxes() {

        assertFailsWith<ImageReadException> {
            TrackBox(
                offset = 0,
                size = 12,
                largeSize = null,
                payload = byteArrayOf(0, 0, 0, 0)
            )
        }
    }

    /**
     * Regression test: a top-level meta box without the mandatory pitm,
     * iinf or iloc boxes must fail with a descriptive error instead of
     * an opaque null cast exception.
     */
    @Test
    fun testMetaBoxTopLevelRejectsMissingMandatoryBoxes() {

        val metaPayload = byteArrayOf(0, 0, 0, 0) + createHdlrBox()

        assertFailsWith<ImageReadException> {
            MetaBoxTopLevel(
                offset = 0,
                size = metaPayload.size.toLong() + 8,
                largeSize = null,
                payload = metaPayload
            )
        }
    }

    /**
     * Regression test: a meta box without its mandatory hdlr box must
     * fail with a descriptive error instead of an opaque cast exception.
     */
    @Test
    fun testMetaBoxRejectsMissingHandlerReference() {

        assertFailsWith<ImageReadException> {
            MetaBox(
                offset = 0,
                size = 12,
                largeSize = null,
                payload = byteArrayOf(0, 0, 0, 0)
            )
        }
    }

    /**
     * Regression test guarding the "never destroy metadata" contract:
     *
     * 1. Unknown entry box types inside iinf are skipped in the item
     *    lookup index, but stay in the parsed [ItemInformationBox.boxes].
     * 2. The raw payload is preserved byte for byte, so a rewrite - which
     *    serializes the stored payloads, not the parsed tree - cannot
     *    lose the unknown entries.
     */
    @Test
    fun testItemInformationBoxKeepsUnknownEntriesAndPayload() {

        val infePayload = byteArrayOf(
            2, 0, 0, 0,
            0, 1,
            0, 0,
            0x6D, 0x69, 0x66, 0x31
        ) + "\u0000".encodeToByteArray()

        val unknownPayload = byteArrayOf(1, 2, 3, 4)

        val payload = byteArrayOf(
            0, 0, 0, 0,
            0, 2
        ) +
            createBox(BoxType.INFE, infePayload) +
            createBox(BoxType.of("unk ".encodeToByteArray()), unknownPayload)

        val box = ItemInformationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        /* The known entry is indexed for metadata lookups. */
        assertEquals(1, box.map.size)
        assertEquals(1, box.map.getValue(1).itemId)

        /* The unknown entry is kept as a generic box. */
        assertEquals(2, box.boxes.size)
        assertEquals(BoxType.of("unk ".encodeToByteArray()), box.boxes.last().type)

        /* The raw payload is untouched, so rewrites preserve everything. */
        assertContentEquals(payload, box.payload)
    }

    private fun createBox(type: BoxType, payload: ByteArray): ByteArray {

        val box = byteArrayOf(
            0, 0, 0, (payload.size + 8).toByte()
        ) + type.bytes + payload

        return box
    }

    private fun createHdlrBox(): ByteArray {

        val hdlrPayload = byteArrayOf(
            0, 0, 0, 0,
            0, 0, 0, 0,
            'p'.code.toByte(), 'i'.code.toByte(), 'c'.code.toByte(), 't'.code.toByte(),
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        ) + "Main Image\u0000".encodeToByteArray()

        return createBox(BoxType.HDLR, hdlrPayload)
    }

    private fun createPitmBox(itemId: Int): ByteArray =

        createBox(
            BoxType.PITM,
            byteArrayOf(0, 0, 0, 0, 0, itemId.toByte())
        )

    /**
     * Builds an iinf box (version 0) with one infe entry of the given
     * item id and the "Exif" item type.
     */
    private fun createIinfBoxWithExifEntry(itemId: Int): ByteArray {

        val infePayload = byteArrayOf(
            2, 0, 0, 0,
            0, itemId.toByte(),
            0, 0,
            'E'.code.toByte(), 'x'.code.toByte(), 'i'.code.toByte(), 'f'.code.toByte()
        ) + "\u0000".encodeToByteArray()

        return createBox(
            BoxType.IINF,
            byteArrayOf(0, 0, 0, 0, 0, 1) + createBox(BoxType.INFE, infePayload)
        )
    }
}
