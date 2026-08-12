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
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BoxReaderTest {

    @Test
    fun readsBoxesFromHeic() {

        val bytes = KimTestData.getBytesOf(KimTestData.HEIC_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
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

        val boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
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

        val boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
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

        val boxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(createIinfBox(version = 0)),
            stopAfterMetadataRead = false
        )

        val iinf = boxes.first() as ItemInformationBox

        assertEquals(0, iinf.version)

        /* The infe box starts after header (8), version & flags (4) and the 2-byte entry count. */
        val infe = iinf.boxes.first() as ItemInfoEntryBox

        assertEquals(14L, infe.offset)
    }

    @Test
    fun reportsInfeOffsetForIinfVersionOne() {

        val boxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(createIinfBox(version = 1)),
            stopAfterMetadataRead = false
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
            BoxReader.readBoxes(
                byteReader = ByteArrayByteReader(box.toByteArray()),
                stopAfterMetadataRead = false
            )
        }
    }

    /**
     * Builds a standalone iinf box of the given version containing one
     * version-2 infe entry with item type "mime".
     */
    private fun createIinfBox(version: Int): ByteArray {

        val infePayload = ByteArrayByteWriter()

        infePayload.write(2) /* The only supported infe version. */
        infePayload.write(byteArrayOf(0, 0, 0)) /* flags */
        infePayload.write2BytesAsInt(1, BMFF_BYTE_ORDER) /* itemId */
        infePayload.write2BytesAsInt(0, BMFF_BYTE_ORDER) /* itemProtectionIndex */
        infePayload.writeInt(0x6D696D65, BMFF_BYTE_ORDER) /* "mime" */
        infePayload.write(0) /* empty item name */

        val infeBytes = infePayload.toByteArray()

        val iinfPayload = ByteArrayByteWriter()

        iinfPayload.write(version)
        iinfPayload.write(byteArrayOf(0, 0, 0)) /* flags */

        if (version == 0)
            iinfPayload.write2BytesAsInt(1, BMFF_BYTE_ORDER)
        else
            iinfPayload.writeInt(1, BMFF_BYTE_ORDER)

        /* The infe box with its 8-byte header. */
        iinfPayload.writeInt(infeBytes.size + 8, BMFF_BYTE_ORDER)
        iinfPayload.write(BoxType.INFE.bytes)
        iinfPayload.write(infeBytes)

        val payloadBytes = iinfPayload.toByteArray()

        val box = ByteArrayByteWriter()

        box.writeInt(payloadBytes.size + 8, BMFF_BYTE_ORDER)
        box.write(BoxType.IINF.bytes)
        box.write(payloadBytes)

        return box.toByteArray()
    }
}
