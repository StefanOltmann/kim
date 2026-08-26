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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.ITEM_TYPE_MIME
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression tests for reading metadata items whose extents are
 * fragmented across the file.
 */
class BaseMediaFileFormatImageParserTest {

    /**
     * Regression test: an EXIF item that is split into two extents must
     * be concatenated and parsed as one stream. Parsing each extent on
     * its own would fail, because the continuation does not start with
     * a TIFF header.
     */
    @Test
    fun testMultiExtentExifItemIsParsedAsOneStream() {

        /* Minimal TIFF: header plus an empty IFD0. */
        val tiffBytes = convertHexStringToByteArray(
            "49492A00" + "08000000" + "0000" + "00000000"
        )

        val firstPart = tiffBytes.copyOfRange(0, 8)
        val secondPart = tiffBytes.copyOfRange(8, tiffBytes.size)

        /*
         * The iloc size does not depend on the offset values, so a
         * placeholder build determines the file layout before the real
         * extent offsets are known.
         */
        val hdlrBox = createHdlrBox()
        val pitmBox = createPitmBox(itemId = 1)
        val iinfBox = createIinfBox(
            entries = listOf(ItemSpec(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_EXIF))
        )
        val ilocPlaceholder =
            createBox(
                type = BoxType.ILOC,
                payload = createIlocPayload(
                    extent1Offset = 0L,
                    extent1Length = TIFF_HEADER_OFFSET_SIZE + firstPart.size,
                    extent2Offset = 0L,
                    extent2Length = secondPart.size
                )
            )

        val ftypBox =
            createBox(BoxType.FTYP, "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray())

        val metaPayloadSize =
            VERSION_AND_FLAGS_SIZE + hdlrBox.size + pitmBox.size + iinfBox.size +
                ilocPlaceholder.size

        val mdatDataOffset: Long =
            (ftypBox.size + 8 + metaPayloadSize + 8).toLong()

        /* The first extent starts with the 4-byte TIFF header offset field. */
        val extent1Offset: Long = mdatDataOffset
        val extent1Length: Int = TIFF_HEADER_OFFSET_SIZE + firstPart.size

        val extent2Offset: Long = extent1Offset + extent1Length

        val ilocBox = createBox(
            type = BoxType.ILOC,
            payload = createIlocPayload(
                extent1Offset = extent1Offset,
                extent1Length = extent1Length,
                extent2Offset = extent2Offset,
                extent2Length = secondPart.size
            )
        )

        val metaBox = createBox(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlrBox + pitmBox + iinfBox + ilocBox
        )

        /* The mdat payload carries both extents back to back. */
        val mdatBox = createBox(
            type = BoxType.MDAT,
            payload = ByteArray(TIFF_HEADER_OFFSET_SIZE) + firstPart + secondPart
        )

        val bytes = ftypBox + metaBox + mdatBox

        val metadata =
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))

        assertNotNull(metadata.exif)

        assertEquals(tiffBytes.size, metadata.exifBytes?.size)
    }

    /**
     * Regression test: an item with one oversized extent between legal
     * ones must be skipped on its own. The old validation only checked
     * the LAST extent, so the hostile extent passed and its read aborted
     * the whole loop, discarding the metadata of all other items.
     */
    @Test
    fun testOversizedMiddleExtentSkipsOnlyItsItem() {

        val bytes = buildHeicFile(
            iinfEntries = listOf(ItemSpec(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_EXIF))
        ) { _ ->
            val ilocBox = createBox(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = BMFFConstants.ITEM_TYPE_EXIF,
                            extents = listOf(
                                ExtentSpec(offset = 100L, length = 0x40000000),
                                ExtentSpec(offset = 110L, length = 4)
                            )
                        )
                    )
                )
            )

            Pair(ilocBox, ByteArray(32))
        }

        /* The hostile item is skipped instead of aborting the parse. */
        val metadata = BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))

        assertNull(metadata.exif)
    }

    /**
     * Regression test: an item that starts before the end position of the
     * previously processed item must be skipped. The old code hit a
     * check() for the backwards jump and turned it into an exception that
     * discarded the metadata of all other items.
     */
    @Test
    fun testOverlappingItemIsSkippedWithoutLosingOtherMetadata() {

        val bytes = buildHeicFile(
            iinfEntries = listOf(
                ItemSpec(itemId = 1, itemType = ITEM_TYPE_MIME),
                ItemSpec(itemId = 2, itemType = BMFFConstants.ITEM_TYPE_EXIF)
            )
        ) { mdatDataOffset ->

            /*
             * The XMP extent sits at the start of the mdat payload; the
             * EXIF extent starts INSIDE it, so processing the EXIF item
             * after the XMP item requires a backwards jump.
             */
            val xmpOffset = mdatDataOffset
            val exifOffset = xmpOffset + 2L

            val ilocBox = createBox(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = ITEM_TYPE_MIME,
                            extents = listOf(ExtentSpec(offset = xmpOffset, length = 8))
                        ),
                        ItemSpec(
                            itemId = 2,
                            itemType = BMFFConstants.ITEM_TYPE_EXIF,
                            extents = listOf(
                                ExtentSpec(offset = exifOffset, length = 12)
                            )
                        )
                    )
                )
            )

            val mdatPayload =
                "xmpdata".encodeToByteArray() + ByteArray(TIFF_HEADER_OFFSET_SIZE + 60)

            Pair(ilocBox, mdatPayload)
        }

        val metadata = BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))

        /* The XMP of the first item must survive. */
        assertTrue(metadata.xmp?.startsWith("xmpdata") == true)

        /* The overlapping EXIF item is skipped instead of failing everything. */
        assertNull(metadata.exif)
    }

    /**
     * Builds a version-0 iloc box payload with one EXIF item that is
     * fragmented into two extents.
     */
    private fun createIlocPayload(
        extent1Offset: Long,
        extent1Length: Int,
        extent2Offset: Long,
        extent2Length: Int
    ): ByteArray =
        createIlocPayloadForItems(
            items = listOf(
                ItemSpec(
                    itemId = 1,
                    itemType = BMFFConstants.ITEM_TYPE_EXIF,
                    extents = listOf(
                        ExtentSpec(extent1Offset, extent1Length),
                        ExtentSpec(extent2Offset, extent2Length)
                    )
                )
            )
        )

    /**
     * Builds a version-0 iloc box payload with the given items and extents.
     *
     * Offsets are written as absolute 4-byte values (construction method 0).
     */
    private fun createIlocPayloadForItems(items: List<ItemSpec>): ByteArray {

        val writer = ByteArrayByteWriter()

        writer.write(0) /* Version 0: absolute offsets, 2-byte item ids. */
        writer.write(byteArrayOf(0, 0, 0)) /* Flags */

        writer.write(0x44) /* Offset size 4, length size 4 */
        writer.write(0x00) /* Base offset size 0, no index */

        writer.write2BytesAsInt(items.size, BMFF_BYTE_ORDER) /* Item count */

        for (item in items) {

            writer.write2BytesAsInt(item.itemId, BMFF_BYTE_ORDER) /* Item id */
            /* Version 0 has no construction method field. */
            writer.write2BytesAsInt(0, BMFF_BYTE_ORDER) /* Data reference index */

            writer.write2BytesAsInt(item.extents.size, BMFF_BYTE_ORDER) /* Extent count */

            for (extent in item.extents) {
                writer.writeInt(extent.offset.toInt(), BMFF_BYTE_ORDER)
                writer.writeInt(extent.length.toInt(), BMFF_BYTE_ORDER)
            }
        }

        return writer.toByteArray()
    }

    /**
     * Assembles a full HEIC file: ftyp + meta(hdlr, pitm, iinf, iloc) + mdat.
     *
     * The iloc box and the mdat payload are created by the given builder,
     * because their contents depend on the mdat offset, which in turn
     * depends on the final file layout. The builder receives that offset.
     *
     * The iinf declares one EXIF item (id 1) and one MIME/XMP item (id 2).
     */
    private fun buildHeicFile(
        iinfEntries: List<ItemSpec>,
        buildParts: (mdatDataOffset: Long) -> Pair<ByteArray, ByteArray>
    ): ByteArray {

        val hdlrBox = createHdlrBox()
        val pitmBox = createPitmBox(itemId = 1)
        val iinfBox = createIinfBox(entries = iinfEntries)

        /*
         * A first pass with a placeholder offset determines the final
         * layout, because the iloc size does not depend on the offsets.
         * The mdat offset is derived from the actually assembled prefix,
         * so the test cannot drift from the real layout arithmetic.
         */
        val (placeholderIloc, _) = buildParts(0L)

        val ftypBox =
            createBox(BoxType.FTYP, "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray())

        val metaBox = createBox(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlrBox + pitmBox + iinfBox + placeholderIloc
        )

        val mdatDataOffset: Long =
            (ftypBox.size + metaBox.size + 8).toLong()

        val (ilocBox, mdatPayload) = buildParts(mdatDataOffset)

        val realMetaBox = createBox(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlrBox + pitmBox + iinfBox + ilocBox
        )

        val mdatBox = createBox(type = BoxType.MDAT, payload = mdatPayload)

        return ftypBox + realMetaBox + mdatBox
    }

    private fun createBox(type: BoxType, payload: ByteArray): ByteArray {

        val size = payload.size + 8

        val box = ByteArrayByteWriter()

        box.writeInt(size, BMFF_BYTE_ORDER)
        box.write(type.bytes)
        box.write(payload)

        return box.toByteArray()
    }

    private fun createHdlrBox(): ByteArray {

        val payload = ByteArrayByteWriter()

        payload.write(byteArrayOf(0, 0, 0, 0)) /* Version & flags */
        payload.write(byteArrayOf(0, 0, 0, 0)) /* Pre-defined */
        payload.write("pict".encodeToByteArray()) /* Handler type */
        payload.write(ByteArray(12)) /* Reserved */
        payload.write(0) /* Empty name terminator */

        return createBox(BoxType.HDLR, payload.toByteArray())
    }

    private fun createPitmBox(itemId: Int): ByteArray {

        val payload = ByteArrayByteWriter()

        payload.write(byteArrayOf(0, 0, 0, 0)) /* Version & flags */
        payload.write2BytesAsInt(itemId, BMFF_BYTE_ORDER)

        return createBox(BoxType.PITM, payload.toByteArray())
    }

    /**
     * Builds an iinf box (version 0) with one infe entry per given item.
     */
    private fun createIinfBox(entries: List<ItemSpec>): ByteArray {

        val entryBoxes = entries.map { entry ->

            val entryPayload = ByteArrayByteWriter()

            entryPayload.write(2) /* The only supported infe version. */
            entryPayload.write(byteArrayOf(0, 0, 0)) /* Flags */
            entryPayload.write2BytesAsInt(entry.itemId, BMFF_BYTE_ORDER) /* Item id */
            entryPayload.write2BytesAsInt(0, BMFF_BYTE_ORDER) /* Item protection index */
            entryPayload.writeInt(entry.itemType, BMFF_BYTE_ORDER) /* Item type */
            entryPayload.write(0) /* Empty item name */

            createBox(BoxType.INFE, entryPayload.toByteArray())
        }

        return createBox(
            BoxType.IINF,
            byteArrayOf(0, 0, 0, 0) + byteArrayOf(0, entries.size.toByte()) + entryBoxes.reduce { a, b -> a + b }
        )
    }

    private companion object {

        const val VERSION_AND_FLAGS_SIZE: Int = 4

        const val TIFF_HEADER_OFFSET_SIZE: Int = 4
    }

    /**
     * One metadata item declaration for the iloc and iinf builders.
     */
    private data class ItemSpec(
        val itemId: Int,
        val itemType: Int,
        val extents: List<ExtentSpec> = emptyList()
    )

    /**
     * The offset and length of a single extent.
     */
    private data class ExtentSpec(
        val offset: Long,
        val length: Int
    )
}
