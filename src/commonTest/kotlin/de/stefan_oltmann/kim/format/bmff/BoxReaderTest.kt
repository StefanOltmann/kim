/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.box.BoxContainer
import de.stefan_oltmann.kim.format.bmff.box.ItemInfoEntryBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInformationBox
import de.stefan_oltmann.kim.format.bmff.box.MediaDataBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBoxTopLevel
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.testdata.BmffTestBoxes
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.box
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.hdlrBox
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.iinfBox
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoxReaderTest {

    /**
     * A box that declares a size smaller than its own 8-byte header is
     * corrupt. It must be rejected: the metadata path would otherwise
     * rewind its position and re-parse consumed header bytes as boxes.
     */
    @Test
    fun testBoxSmallerThanHeaderIsRejected() {

        /* A free box (12 bytes) followed by a pseudo-box that claims
           a size of 6 bytes. */
        val bytes = byteArrayOf(
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2, 3, 4,
            0, 0, 0, 6,
            0x66, 0x72, 0x65, 0x65 // "free"
        )

        val exception = assertFailsWith<ImageReadException> {
            BoxReader.readAllBoxes(
                byteReader = ByteArrayByteReader(bytes)
            )
        }

        assertTrue(exception.message?.contains("smaller than its header") == true)
    }

    /**
     * A largesize box stores its size behind a 16-byte header, so a
     * declared size below both headers cannot hold a payload. It must be
     * rejected: the metadata scan would otherwise compute a negative
     * remaining length, treat it as truncation and silently stop in the
     * middle of the file, losing every box behind the broken one.
     */
    @Test
    fun testLargesizeBelowBothHeadersIsRejected() {

        /* A free box (12 bytes), a largesize box that declares 12, and
           a box behind it that the broken scan would never reach. */
        val bytes = byteArrayOf(
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2, 3, 4,
            0, 0, 0, 1, // size 1 -> the real size follows
            0x66, 0x72, 0x65, 0x65, // "free"
            0, 0, 0, 0, 0, 0, 0, 12, // largesize 12 < 2 * 8 header bytes
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            5, 6, 7, 8
        )

        val exception = assertFailsWith<ImageReadException> {
            BoxReader.scanMetadataBoxes(
                byteReader = ByteArrayByteReader(bytes)
            )
        }

        assertTrue(exception.message?.contains("largesize") == true)
    }

    /**
     * Bytes that end inside a box header are a truncated box. A rewrite
     * re-emits only the parsed boxes and would silently drop such a
     * fragment, so the rewrite-feeding read must fail instead of
     * stopping at the fragment.
     */
    @Test
    fun testReadForRewriteRejectsTruncatedTrailingBoxHeader() {

        /* A free box (12 bytes) followed by 3 bytes of a cut-off box. */
        val bytes = byteArrayOf(
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2, 3, 4,
            0, 0, 0, // Truncated box header.
            0x66
        )

        assertFailsWith<ImageReadException> {
            BoxReader.readAllBoxesForRewrite(
                byteReader = ByteArrayByteReader(bytes)
            )
        }
    }

    /**
     * Reads that do not feed a rewrite treat the fragment as a clean
     * boundary: the raw bytes are never re-emitted, so nothing is lost.
     * The CR3 metadata walk and the external viewer rely on this.
     */
    @Test
    fun testReadAllBoxesToleratesTruncatedTrailingBoxHeader() {

        /* A free box (12 bytes) followed by 3 bytes of a cut-off box. */
        val bytes = byteArrayOf(
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2, 3, 4,
            0, 0, 0, // Truncated box header.
            0x66
        )

        val boxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(bytes)
        )

        assertEquals(1, boxes.size)
    }

    /**
     * A box with size 0 extends to the end of the file per ISOBMFF.
     */
    @Test
    fun testSizeZeroBoxExtendsToEndOfFile() {

        val bytes = byteArrayOf(
            0, 0, 0, 12,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2, 3, 4,
            0, 0, 0, 0,
            0x66, 0x72, 0x65, 0x65, // "free"
            9, 9, 9, 9
        )

        val boxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(bytes)
        )

        assertEquals(2, boxes.size)
    }

    @Test
    fun readsBoxesFromHeic() {

        val bytes = KimTestData.getBytesOf(KimTestData.HEIC_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val boxes = BoxReader.readAllBoxes(
            byteReader = byteReader
        )

        val allBoxes = BoxContainer.findAllBoxesRecursive(boxes)

        assertEquals(0, allBoxes.first { it.type == BoxType.FTYP }.offset)
        assertEquals(36, allBoxes.first { it.type == BoxType.META }.offset)
        assertEquals(48, allBoxes.first { it.type == BoxType.HDLR }.offset)
        assertEquals(118, allBoxes.first { it.type == BoxType.PITM }.offset)
        assertEquals(132, allBoxes.first { it.type == BoxType.IINF }.offset)
        assertEquals(146, allBoxes.first { it.type == BoxType.INFE }.offset)
        assertEquals(2572, allBoxes.first { it.type == BoxType.ILOC }.offset)
        assertEquals(3404, allBoxes.first { it.type == BoxType.MDAT }.offset)
    }

    @Test
    fun readsBoxesFromAvif() {

        val bytes = KimTestData.getBytesOf(KimTestData.AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val boxes = BoxReader.readAllBoxes(
            byteReader = byteReader
        )

        val allBoxes = BoxContainer.findAllBoxesRecursive(boxes)

        assertEquals(0, allBoxes.first { it.type == BoxType.FTYP }.offset)
        assertEquals(28, allBoxes.first { it.type == BoxType.META }.offset)
        assertEquals(40, allBoxes.first { it.type == BoxType.HDLR }.offset)
        assertEquals(73, allBoxes.first { it.type == BoxType.PITM }.offset)
        assertEquals(87, allBoxes.first { it.type == BoxType.ILOC }.offset)
        assertEquals(157, allBoxes.first { it.type == BoxType.IINF }.offset)
        assertEquals(171, allBoxes.first { it.type == BoxType.INFE }.offset)
        assertEquals(401, allBoxes.first { it.type == BoxType.MDAT }.offset)
    }

    @Test
    fun readsBoxesFromAnimatedAvif() {

        val bytes = KimTestData.getBytesOf(KimTestData.ANIMATED_AVIF_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val boxes = BoxReader.readAllBoxes(
            byteReader = byteReader
        )

        val allBoxes = BoxContainer.findAllBoxesRecursive(boxes)

        assertEquals(0, allBoxes.first { it.type == BoxType.FTYP }.offset)
        assertEquals(44, allBoxes.first { it.type == BoxType.META }.offset)
        assertEquals(56, allBoxes.first { it.type == BoxType.HDLR }.offset)
        assertEquals(89, allBoxes.first { it.type == BoxType.PITM }.offset)
        assertEquals(103, allBoxes.first { it.type == BoxType.ILOC }.offset)
        assertEquals(161, allBoxes.first { it.type == BoxType.IINF }.offset)
        assertEquals(175, allBoxes.first { it.type == BoxType.INFE }.offset)
        assertEquals(416, allBoxes.first { it.type == BoxType.MOOV }.offset)
        assertEquals(544, allBoxes.first { it.type == BoxType.TRAK }.offset)
        assertEquals(552, allBoxes.first { it.type == BoxType.TKHD }.offset)
        assertEquals(872, allBoxes.first { it.type == BoxType.MDIA }.offset)
        assertEquals(957, allBoxes.first { it.type == BoxType.MINF }.offset)
        assertEquals(1298, allBoxes.first { it.type == BoxType.MDAT }.offset)
    }

    @Test
    fun reportsInfeOffsetForIinfVersionZero() {

        val mimeEntry = BmffTestBoxes.InfeEntry(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_MIME)

        val boxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(iinfBox(version = 0, entries = listOf(mimeEntry)))
        )

        val iinf = boxes.first() as ItemInformationBox

        assertEquals(0, iinf.version)

        /* The infe box starts after header (8), version & flags (4) and the 2-byte entry count. */
        val infe = iinf.boxes.first() as ItemInfoEntryBox

        assertEquals(14L, infe.offset)
    }

    @Test
    fun reportsInfeOffsetForIinfVersionOne() {

        val mimeEntry = BmffTestBoxes.InfeEntry(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_MIME)

        val boxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(iinfBox(version = 1, entries = listOf(mimeEntry)))
        )

        val iinf = boxes.first() as ItemInformationBox

        assertEquals(1, iinf.version)

        /* The infe box starts after header (8), version & flags (4) and the 4-byte entry count. */
        val infe = iinf.boxes.first() as ItemInfoEntryBox

        assertEquals(16L, infe.offset)
    }

    /**
     * A box size of 2^32-1 overflows the signed read count and must be
     * rejected with a clear error instead of corrupting the read.
     */
    @Test
    fun rejectsBoxWithSizeOverflowingInt() {

        val box = ByteArrayByteWriter()

        box.writeInt(0xFFFF_FFFF.toInt(), BMFF_BYTE_ORDER)
        box.write("free".encodeToByteArray())

        assertFailsWith<ImageReadException> {
            BoxReader.readAllBoxes(
                byteReader = ByteArrayByteReader(box.toByteArray())
            )
        }
    }

    /**
     * Regression test: during metadata reads the mdat payload must not be
     * duplicated into the box object when the underlying reader already
     * retains the consumed bytes itself - otherwise whole image data ends
     * up in memory several times on meta-after-mdat layouts.
     */
    @Test
    fun testMdatPayloadIsNotDuplicatedOnRetainingReaders() {

        val mdatPayload = ByteArray(64)

        val box = ByteArrayByteWriter()

        box.writeInt(mdatPayload.size + 8, BMFF_BYTE_ORDER)
        box.write(BoxType.MDAT.bytes)
        box.write(mdatPayload)

        val copyReader = CopyByteReader(ByteArrayByteReader(box.toByteArray()))

        val boxes = BoxReader.scanMetadataBoxes(
            byteReader = copyReader
        )

        /* The reader itself retained everything... */
        assertEquals(box.toByteArray().size.toLong() + 0, copyReader.getBytes().size.toLong())

        /* ... while the box object carries no duplicate of the payload. */
        val mdatBox = boxes.filterIsInstance<MediaDataBox>().firstOrNull()

        assertNotNull(mdatBox)
        assertEquals(0, mdatBox.payload.size)

        /* The offset stays intact for extent-based re-reads. */
        assertEquals(0L, mdatBox.offset)
    }

    /**
     * Regression test: container boxes nested beyond the depth limit must
     * be rejected with a clear error instead of overflowing the call stack.
     */
    @Test
    fun testDeeplyNestedContainerBoxesAreRejected() {

        var bytes = ByteArray(0)

        repeat(DEPTH_LIMIT_TEST_LEVELS) {
            bytes = box(BoxType.MOOV, bytes)
        }

        assertFailsWith<ImageReadException> {
            BoxReader.readAllBoxes(
                byteReader = ByteArrayByteReader(bytes)
            )
        }
    }

    /**
     * Regression test: a meta box below the top level must be parsed as a
     * plain container, not as another top-level meta box. Both the meta
     * inside the moov box and the one inside that meta are plain ones.
     */
    @Test
    fun testNestedMetaBoxesArePlainContainers() {

        val innerMeta = box(BoxType.META, VERSION_AND_FLAGS + hdlrBox())

        val outerMeta =
            box(BoxType.META, VERSION_AND_FLAGS + hdlrBox() + innerMeta)

        val bytes = box(BoxType.MOOV, outerMeta)

        val boxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(bytes)
        )

        val movieBox = boxes.filterIsInstance<MovieBox>().single()

        val outerMetaBox = movieBox.boxes.filterIsInstance<MetaBox>().single()

        assertFalse(outerMetaBox is MetaBoxTopLevel)

        /* The meta inside the meta exercises the nested parse path. */
        val innerMetaBox = outerMetaBox.boxes.filterIsInstance<MetaBox>().single()

        assertFalse(innerMetaBox is MetaBoxTopLevel)
    }

    private companion object {

        const val VERSION_AND_FLAGS_SIZE: Int = 4

        val VERSION_AND_FLAGS: ByteArray = ByteArray(VERSION_AND_FLAGS_SIZE)

        /*
         * One more than the parsing depth limit, so the rejection
         * is guaranteed to be reached.
         */
        const val DEPTH_LIMIT_TEST_LEVELS: Int = 20
    }
}
