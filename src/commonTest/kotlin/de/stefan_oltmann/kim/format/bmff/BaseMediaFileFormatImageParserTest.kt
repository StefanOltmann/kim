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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.ITEM_TYPE_MIME
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.testdata.BmffTestBoxes
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.box
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.hdlrBox
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.iinfBox
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.pitmBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Regression tests for reading metadata items whose extents are
 * fragmented across the file.
 */
class BaseMediaFileFormatImageParserTest {

    /**
     * The public parseMetadata documents only ImageReadException. Hostile
     * input that makes an eagerly constructed box fail with an
     * IllegalStateException must be wrapped at this boundary like every
     * other failure, so direct callers only see ImageException.
     */
    @Test
    fun testParseMetadataWrapsUnsupportedInfeVersion() {

        /* An infe with the unsupported version 0. */
        val infeV0 = box(
            BoxType.INFE,
            byteArrayOf(0, 0, 0, 0, 0, 1, 0x6D, 0x69, 0x66, 0x31, 0)
        )

        val iinf = box(
            BoxType.IINF,
            byteArrayOf(0, 0, 0, 0) + byteArrayOf(0, 1) + infeV0
        )

        val meta = box(
            BoxType.META,
            byteArrayOf(0, 0, 0, 0) + hdlrBox() + iinf
        )

        val file = box(
            BoxType.FTYP,
            "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray()
        ) + meta

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(file))
        }
    }

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
        val hdlr = hdlrBox()
        val pitm = pitmBox(itemId = 1)
        val iinf = iinfBox(
            entries = listOf(BmffTestBoxes.InfeEntry(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_EXIF))
        )
        val ilocPlaceholder =
            box(
                type = BoxType.ILOC,
                payload = createIlocPayload(
                    extent1Offset = 0L,
                    extent1Length = TIFF_HEADER_OFFSET_SIZE + firstPart.size,
                    extent2Offset = 0L,
                    extent2Length = secondPart.size
                )
            )

        val ftypBox =
            box(BoxType.FTYP, "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray())

        val metaPayloadSize =
            VERSION_AND_FLAGS_SIZE + hdlr.size + pitm.size + iinf.size +
                ilocPlaceholder.size

        val mdatDataOffset: Long =
            (ftypBox.size + 8 + metaPayloadSize + 8).toLong()

        /* The first extent starts with the 4-byte TIFF header offset field. */
        val extent1Offset: Long = mdatDataOffset
        val extent1Length: Int = TIFF_HEADER_OFFSET_SIZE + firstPart.size

        val extent2Offset: Long = extent1Offset + extent1Length

        val ilocBox = box(
            type = BoxType.ILOC,
            payload = createIlocPayload(
                extent1Offset = extent1Offset,
                extent1Length = extent1Length,
                extent2Offset = extent2Offset,
                extent2Length = secondPart.size
            )
        )

        val metaBox = box(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlr + pitm + iinf + ilocBox
        )

        /* The mdat payload carries both extents back to back. */
        val mdatBox = box(
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
     * Regression test: an item with one illegal extent must fail the
     * read. Its EXIF content exists in the file but cannot be read
     * cleanly, so a successful read without it would silently drop
     * metadata from sidecar exports - the old skip-only-this-item
     * behavior did exactly that and was aligned with the strict read
     * policy.
     */
    @Test
    fun testOversizedMiddleExtentFailsTheRead() {

        val bytes = buildHeicFile(
            iinfEntries = listOf(ItemSpec(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_EXIF))
        ) { _ ->
            val ilocBox = box(
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

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * Regression test: an item that starts before the end position of
     * the previously processed item must fail the read. The reader
     * would have to jump backwards and desync, and silently dropping
     * the item would drop its EXIF content from sidecar exports.
     */
    @Test
    fun testOverlappingExtentFailsTheRead() {

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

            /* A well formed packet, because the test is about the overlap. */
            val xmpPayload = "<x:xmpmeta></x:xmpmeta>".encodeToByteArray()

            val ilocBox = box(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = ITEM_TYPE_MIME,
                            extents = listOf(
                                ExtentSpec(offset = xmpOffset, length = xmpPayload.size)
                            )
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
                xmpPayload + ByteArray(TIFF_HEADER_OFFSET_SIZE + 60)

            Pair(ilocBox, mdatPayload)
        }

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * Two items of the same metadata type make the authoritative packet
     * ambiguous: silently letting the last item win would hide the
     * first item's content from every consumer. Like QuickTime's
     * duplicate XMP boxes, the read fails instead.
     */
    @Test
    fun testDuplicateMetadataItemsFailTheRead() {

        /* Minimal TIFF: header plus an empty IFD0. */
        val tiffBytes = convertHexStringToByteArray(
            "49492A00" + "08000000" + "0000" + "00000000"
        )

        val exifPayload = ByteArray(TIFF_HEADER_OFFSET_SIZE) + tiffBytes

        val bytes = buildHeicFile(
            iinfEntries = listOf(
                ItemSpec(itemId = 1, itemType = BMFFConstants.ITEM_TYPE_EXIF),
                ItemSpec(itemId = 2, itemType = BMFFConstants.ITEM_TYPE_EXIF)
            )
        ) { mdatDataOffset ->

            val ilocBox = box(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = BMFFConstants.ITEM_TYPE_EXIF,
                            extents = listOf(
                                ExtentSpec(offset = mdatDataOffset, length = exifPayload.size)
                            )
                        ),
                        ItemSpec(
                            itemId = 2,
                            itemType = BMFFConstants.ITEM_TYPE_EXIF,
                            extents = listOf(
                                ExtentSpec(
                                    offset = mdatDataOffset + exifPayload.size,
                                    length = exifPayload.size
                                )
                            )
                        )
                    )
                )
            )

            Pair(ilocBox, exifPayload + exifPayload)
        }

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * Like WebP, JXL and CR3, an XMP item without a `<x:xmpmeta>` element
     * must fail the read instead of being handed to sidecar writers as a
     * corrupt packet.
     */
    @Test
    fun testCorruptXmpItemFailsTheRead() {

        val bytes = buildHeicFile(
            iinfEntries = listOf(ItemSpec(itemId = 1, itemType = ITEM_TYPE_MIME))
        ) { mdatDataOffset ->

            val ilocBox = box(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = ITEM_TYPE_MIME,
                            extents = listOf(
                                ExtentSpec(offset = mdatDataOffset, length = 8)
                            )
                        )
                    )
                )
            )

            Pair(ilocBox, "truncated".encodeToByteArray())
        }

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * XMP can also hide in a top level UUID box. A packet without the
     * `<x:xmpmeta>` element is corrupt there as well and must fail the
     * read.
     */
    @Test
    fun testCorruptXmpUuidBoxFailsTheRead() {

        val ftypBox =
            box(BoxType.FTYP, "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray())

        /* A meta box without metadata items, so only the UUID box carries XMP. */
        val metaBox = box(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) +
                hdlrBox() +
                pitmBox(itemId = 1) +
                iinfBox(entries = emptyList()) +
                box(BoxType.ILOC, createIlocPayloadForItems(items = emptyList()))
        )

        val uuidBox = box(
            type = BoxType.UUID,
            payload = convertHexStringToByteArray(BMFFConstants.XMP_UUID) +
                "truncated".encodeToByteArray()
        )

        val mdatBox = box(type = BoxType.MDAT, payload = ByteArray(32))

        val bytes = ftypBox + metaBox + uuidBox + mdatBox

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * When the file declares XMP twice - as a metadata item and in a
     * UUID box - the authoritative packet is ambiguous. Picking one
     * would silently drop the other from sidecar exports, so the read
     * fails instead of guessing.
     */
    @Test
    fun testAmbiguousXmpItemAndUuidBoxFailsTheRead() {

        val xmpPayload = "<x:xmpmeta></x:xmpmeta>".encodeToByteArray()

        val uuidBox = box(
            type = BoxType.UUID,
            payload = convertHexStringToByteArray(BMFFConstants.XMP_UUID) +
                "<x:xmpmeta></x:xmpmeta>".encodeToByteArray()
        )

        val bytes = buildHeicFile(
            iinfEntries = listOf(ItemSpec(itemId = 1, itemType = ITEM_TYPE_MIME)),
            prefixBoxes = uuidBox
        ) { mdatDataOffset ->

            val ilocBox = box(
                type = BoxType.ILOC,
                payload = createIlocPayloadForItems(
                    items = listOf(
                        ItemSpec(
                            itemId = 1,
                            itemType = ITEM_TYPE_MIME,
                            extents = listOf(
                                ExtentSpec(offset = mdatDataOffset, length = xmpPayload.size)
                            )
                        )
                    )
                )
            )

            Pair(ilocBox, xmpPayload)
        }

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
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

        /* Version 0: absolute offsets, 2-byte item ids. */
            writer.write(0)
        /* Flags */
            writer.write(byteArrayOf(0, 0, 0))

        /* Offset size 4, length size 4 */
            writer.write(0x44)
        /* Base offset size 0, no index */
            writer.write(0x00)

        /* Item count */
            writer.write2BytesAsInt(items.size, BMFF_BYTE_ORDER)

        for (item in items) {

            /* Item id */
            writer.write2BytesAsInt(item.itemId, BMFF_BYTE_ORDER)
            /*
             * Version 0 has no construction method field.
             * Data reference index.
             */
            writer.write2BytesAsInt(0, BMFF_BYTE_ORDER)

            /* Extent count */
            writer.write2BytesAsInt(item.extents.size, BMFF_BYTE_ORDER)

            for (extent in item.extents) {
                writer.writeInt(extent.offset.toInt(), BMFF_BYTE_ORDER)
                writer.writeInt(extent.length, BMFF_BYTE_ORDER)
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
        prefixBoxes: ByteArray = ByteArray(0),
        buildParts: (mdatDataOffset: Long) -> Pair<ByteArray, ByteArray>
    ): ByteArray {

        val hdlr = hdlrBox()
        val pitm = pitmBox(itemId = 1)
        val iinf = iinfBox(
            entries = iinfEntries.map { BmffTestBoxes.InfeEntry(it.itemId, it.itemType) }
        )

        /*
         * A first pass with a placeholder offset determines the final
         * layout, because the iloc size does not depend on the offsets.
         * The mdat offset is derived from the actually assembled prefix,
         * so the test cannot drift from the real layout arithmetic.
         */
        val (placeholderIloc, _) = buildParts(0L)

        val ftypBox =
            box(BoxType.FTYP, "heic\u0000\u0000\u0000\u0000mif1".encodeToByteArray())

        val metaBox = box(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlr + pitm + iinf + placeholderIloc
        )

        val mdatDataOffset: Long =
            (ftypBox.size + prefixBoxes.size + metaBox.size + 8).toLong()

        val (ilocBox, mdatPayload) = buildParts(mdatDataOffset)

        val realMetaBox = box(
            type = BoxType.META,
            payload = byteArrayOf(0, 0, 0, 0) + hdlr + pitm + iinf + ilocBox
        )

        val mdatBox = box(type = BoxType.MDAT, payload = mdatPayload)

        return ftypBox + prefixBoxes + realMetaBox + mdatBox
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
