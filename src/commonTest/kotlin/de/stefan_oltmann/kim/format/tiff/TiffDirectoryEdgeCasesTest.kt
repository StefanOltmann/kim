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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriterLossy
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TiffDirectoryEdgeCasesTest {

    private fun field(
        tag: Int,
        fieldType: de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType<out Any>,
        bytes: ByteArray,
        count: Int = bytes.size / fieldType.size
    ): TiffField = TiffField(
        offset = 0,
        tag = tag,
        directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
        fieldType = fieldType,
        count = count,
        localValue = null,
        valueOffset = 0,
        valueBytes = bytes,
        byteOrder = ByteOrder.BIG_ENDIAN,
        sortHint = 0
    )

    private fun directory(vararg entries: TiffField): TiffDirectory = TiffDirectory(
        type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
        entries = entries.toList(),
        offset = 8,
        nextDirectoryOffset = 0,
        byteOrder = ByteOrder.BIG_ENDIAN
    )

    @Test
    fun testFindFieldWithFailIfMissing() {

        val tiffDirectory = directory()

        assertNull(tiffDirectory.findField(TiffTag.TIFF_TAG_IMAGE_WIDTH))

        assertFailsWith<ImageReadException> {
            tiffDirectory.findField(TiffTag.TIFF_TAG_IMAGE_WIDTH, failIfMissing = true)
        }
    }

    @Test
    fun testGetFieldValueBytesMustExist() {

        val tiffDirectory = directory()

        assertFailsWith<ImageReadException> {
            tiffDirectory.getFieldValue(TiffTag.TIFF_TAG_XMP, mustExist = true)
        }

        /* Missing optional fields return null. */
        assertNull(tiffDirectory.getFieldValue(TiffTag.TIFF_TAG_XMP, mustExist = false))
    }

    @Test
    fun testGetFieldValueLongRejectsWrongType() {

        /* The field exists but has the wrong type. */
        val tiffDirectory = directory(
            field(
                TiffTag.TIFF_TAG_IMAGE_WIDTH.tag,
                FieldTypeShort,
                shortArrayOf(100).toBytes(ByteOrder.BIG_ENDIAN),
                1
            )
        )

        assertFailsWith<ImageReadException> {
            tiffDirectory.getFieldValue(TiffTag.TIFF_TAG_IMAGE_WIDTH)
        }
    }

    @Test
    fun testGetFieldValueLongRejectsWrongCount() {

        /* The field has more than one value. */
        val tiffDirectory = directory(
            field(
                TiffTag.TIFF_TAG_IMAGE_WIDTH.tag,
                FieldTypeLong,
                intArrayOf(100, 200).toBytes(ByteOrder.BIG_ENDIAN),
                2
            )
        )

        assertFailsWith<ImageReadException> {
            tiffDirectory.getFieldValue(TiffTag.TIFF_TAG_IMAGE_WIDTH)
        }
    }

    @Test
    fun testGetFieldValueLongsRejectsMissing() {

        assertFailsWith<ImageReadException> {
            directory().getFieldValue(TiffTag.TIFF_TAG_FREE_OFFSETS)
        }
    }

    @Test
    fun testGetFieldValueLongsRejectsWrongType() {

        val tiffDirectory = directory(
            field(
                TiffTag.TIFF_TAG_FREE_OFFSETS.tag,
                FieldTypeByte,
                byteArrayOf(1, 2, 3, 4)
            )
        )

        assertFailsWith<ImageReadException> {
            tiffDirectory.getFieldValue(TiffTag.TIFF_TAG_FREE_OFFSETS)
        }
    }

    @Test
    fun testGetStripImageDataElementsRejectsMismatch() {

        /* Two offsets but only one length. */
        val tiffDirectory = directory(
            field(
                TiffTag.TIFF_TAG_STRIP_OFFSETS.tag,
                FieldTypeLong,
                intArrayOf(100, 200).toBytes(ByteOrder.BIG_ENDIAN),
                2
            ),
            field(
                TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS.tag,
                FieldTypeLong,
                intArrayOf(50).toBytes(ByteOrder.BIG_ENDIAN),
                1
            )
        )

        assertFailsWith<ImageReadException> {
            tiffDirectory.getStripImageDataElements()
        }
    }

    @Test
    fun testCreateOutputDirectoryRejectsMakerNoteType() {

        val makerNoteDirectory = TiffDirectory(
            type = TiffConstants.TIFF_MAKER_NOTE_CANON,
            entries = emptyList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        assertFailsWith<IllegalStateException> {
            makerNoteDirectory.createOutputDirectory(ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testDescription() {

        assertEquals("Unknown", TiffDirectory.description(TiffConstants.DIRECTORY_TYPE_UNKNOWN))
        assertEquals("IFD0", TiffDirectory.description(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0))
        assertEquals("Unknown type 42", TiffDirectory.description(42))
    }

    @Test
    fun testTiffImageParserReadsXmp() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_WIDTH, 10)
        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_HEIGHT, 10)
        rootDirectory.add(TiffTag.TIFF_TAG_XMP, "<x:xmpmeta>XMP</x:xmpmeta>".encodeToByteArray())

        val byteWriter = ByteArrayByteWriter()

        TiffWriterLossy(ByteOrder.BIG_ENDIAN).write(byteWriter, outputSet)

        val metadata = TiffImageParser.parseMetadata(
            ByteArrayByteReader(byteWriter.toByteArray())
        )

        assertEquals("<x:xmpmeta>XMP</x:xmpmeta>", metadata.xmp)
        assertTrue(metadata.imageSize != null)
    }
}
