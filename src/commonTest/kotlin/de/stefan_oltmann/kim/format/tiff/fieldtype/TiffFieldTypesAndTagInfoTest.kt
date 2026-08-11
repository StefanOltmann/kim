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
package de.stefan_oltmann.kim.format.tiff.fieldtype

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoGpsText
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TiffFieldTypesAndTagInfoTest {

    @Test
    fun testGetFieldType() {

        assertEquals(FieldTypeByte, FieldType.getFieldType(TiffConstants.FIELD_TYPE_BYTE_INDEX))
        assertEquals(FieldTypeAscii, FieldType.getFieldType(TiffConstants.FIELD_TYPE_ASCII_INDEX))
        assertEquals(FieldTypeShort, FieldType.getFieldType(TiffConstants.FIELD_TYPE_SHORT_INDEX))
        assertEquals(FieldTypeLong, FieldType.getFieldType(TiffConstants.FIELD_TYPE_LONG_INDEX))
        assertEquals(FieldTypeRational, FieldType.getFieldType(TiffConstants.FIELD_TYPE_RATIONAL_INDEX))
        assertEquals(FieldTypeSByte, FieldType.getFieldType(TiffConstants.FIELD_TYPE_SBYTE_INDEX))
        assertEquals(FieldTypeUndefined, FieldType.getFieldType(TiffConstants.FIELD_TYPE_UNDEFINED_INDEX))
        assertEquals(FieldTypeSShort, FieldType.getFieldType(TiffConstants.FIELD_TYPE_SSHORT_INDEX))
        assertEquals(FieldTypeSLong, FieldType.getFieldType(TiffConstants.FIELD_TYPE_SLONG_INDEX))
        assertEquals(FieldTypeSRational, FieldType.getFieldType(TiffConstants.FIELD_TYPE_SRATIONAL_INDEX))
        assertEquals(FieldTypeFloat, FieldType.getFieldType(TiffConstants.FIELD_TYPE_FLOAT_INDEX))
        assertEquals(FieldTypeDouble, FieldType.getFieldType(TiffConstants.FIELD_TYPE_DOUBLE_INDEX))
        assertEquals(FieldTypeIfd, FieldType.getFieldType(TiffConstants.FIELD_TYPE_IFD_INDEX))

        /* Unknown field type. */
        assertFailsWith<ImageReadException> {
            FieldType.getFieldType(99)
        }
    }

    @Test
    fun testFieldTypeSByte() {

        assertEquals(TiffConstants.FIELD_TYPE_SBYTE_INDEX, FieldTypeSByte.type)
        assertEquals("SByte", FieldTypeSByte.name)
        assertEquals(1, FieldTypeSByte.size)

        assertContentEquals(
            expected = byteArrayOf(-5),
            actual = FieldTypeSByte.writeData((-5).toByte(), ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(1, 2),
            actual = FieldTypeSByte.writeData(byteArrayOf(1, 2), ByteOrder.BIG_ENDIAN)
        )

        assertFailsWith<ImageWriteException> {
            FieldTypeSByte.writeData(5, ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testFieldTypeIfd() {

        assertEquals(TiffConstants.FIELD_TYPE_IFD_INDEX, FieldTypeIfd.type)
        assertEquals("IFD", FieldTypeIfd.name)
        assertEquals(4, FieldTypeIfd.size)

        assertContentEquals(
            expected = byteArrayOf(0, 0, 0, 5),
            actual = FieldTypeIfd.writeData(5, ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 2),
            actual = FieldTypeIfd.writeData(intArrayOf(1, 2), ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = intArrayOf(1, 2),
            actual = FieldTypeIfd.getValue(
                byteArrayOf(0, 0, 0, 1, 0, 0, 0, 2),
                ByteOrder.BIG_ENDIAN
            )
        )

        assertFailsWith<ImageWriteException> {
            FieldTypeIfd.writeData("not an int", ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testFieldTypeFloat() {

        assertContentEquals(
            expected = floatArrayOf(1.5f),
            actual = FieldTypeFloat.getValue(
                byteArrayOf(0x3F, 0xC0.toByte(), 0, 0),
                ByteOrder.BIG_ENDIAN
            )
        )

        assertContentEquals(
            expected = byteArrayOf(0x3F, 0xC0.toByte(), 0, 0),
            actual = FieldTypeFloat.writeData(1.5f, ByteOrder.BIG_ENDIAN)
        )

        assertFailsWith<ImageWriteException> {
            FieldTypeFloat.writeData("not a float", ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testFieldTypeSShort() {

        assertContentEquals(
            expected = shortArrayOf(1000, -1000),
            actual = FieldTypeSShort.getValue(
                byteArrayOf(0x03, 0xE8.toByte(), 0xFC.toByte(), 0x18),
                ByteOrder.BIG_ENDIAN
            )
        )

        assertContentEquals(
            expected = byteArrayOf(0x03, 0xE8.toByte()),
            actual = FieldTypeSShort.writeData(1000.toShort(), ByteOrder.BIG_ENDIAN)
        )

        assertFailsWith<ImageWriteException> {
            FieldTypeSShort.writeData(5, ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testFieldTypeAscii() {

        /* Values are null-terminated on write. */
        assertContentEquals(
            expected = "Hi\u0000".encodeToByteArray(),
            actual = FieldTypeAscii.writeData("Hi", ByteOrder.BIG_ENDIAN)
        )

        assertFailsWith<ImageWriteException> {
            FieldTypeAscii.writeData(5, ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testTagInfoBaseClass() {

        val tagInfo = TiffTag.TIFF_TAG_IMAGE_WIDTH

        assertEquals("0x0100", tagInfo.tagFormatted)
        assertEquals("0x0100 ImageWidth", tagInfo.description)
        assertEquals("0x0100 ImageWidth", tagInfo.toString())
        assertFalse(tagInfo.isText())

        /* Equality. */
        assertEquals(tagInfo, TiffTag.TIFF_TAG_IMAGE_WIDTH)
        assertEquals(tagInfo.hashCode(), TiffTag.TIFF_TAG_IMAGE_WIDTH.hashCode())
        assertNotEquals(tagInfo, TiffTag.TIFF_TAG_IMAGE_HEIGHT)
        assertNotEquals<Any>(tagInfo, "ImageWidth")
        assertEquals(tagInfo, tagInfo)
    }

    @Test
    fun testTagInfoGpsTextEncodeAndDecode() {

        val tagInfo = GpsTagGpsProcessingMethod

        val encoded = tagInfo.encodeValue("GPS")

        /* 8-byte ASCII prefix followed by the text. */
        assertEquals(11, encoded.size)

        val tiffField = TiffField(
            offset = 0,
            tag = tagInfo.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
            fieldType = de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined,
            count = encoded.size,
            localValue = null,
            valueOffset = 0,
            valueBytes = encoded,
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

        assertEquals("GPS", tagInfo.getValue(tiffField))
    }

    @Test
    fun testTagInfoGpsTextDecodeShortBytes() {

        val tagInfo = GpsTagGpsProcessingMethod

        /* Short values are decoded without a prefix. */
        val shortField = createGpsTextField(tagInfo, "AB".encodeToByteArray())

        assertEquals("AB", tagInfo.getValue(shortField))
    }

    @Test
    fun testTagInfoGpsTextDecodeZeroBytes() {

        val tagInfo = GpsTagGpsProcessingMethod

        /* All-zero values are empty. */
        val zeroField = createGpsTextField(tagInfo, ByteArray(10))

        assertEquals("", tagInfo.getValue(zeroField))

        /* All-zero payload after the prefix is empty too. */
        val zeroPayloadField = createGpsTextField(tagInfo, ByteArray(8) + ByteArray(4))
        assertEquals("", tagInfo.getValue(zeroPayloadField))
    }

    @Test
    fun testTagInfoGpsTextDecodeAsciiFieldType() {

        val tagInfo = GpsTagGpsProcessingMethod

        val field = TiffField(
            offset = 0,
            tag = tagInfo.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
            fieldType = de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii,
            count = 4,
            localValue = null,
            valueOffset = 0,
            valueBytes = "GPS\u0000".encodeToByteArray(),
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

        assertEquals("GPS", tagInfo.getValue(field))
    }

    @Test
    fun testTagInfoGpsTextDecodeRejectsOtherTypes() {

        val tagInfo = GpsTagGpsProcessingMethod

        assertFailsWith<ImageReadException> {
            TiffField(
                offset = 0,
                tag = tagInfo.tag,
                directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
                fieldType = de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong,
                count = 1,
                localValue = null,
                valueOffset = 0,
                valueBytes = byteArrayOf(0, 0, 0, 1),
                byteOrder = ByteOrder.BIG_ENDIAN,
                sortHint = 0
            )
        }
    }

    @Test
    fun testTagInfoGpsTextEncodeRejectsNonStrings() {

        assertFailsWith<ImageWriteException> {
            GpsTagGpsProcessingMethod.encodeValue(5)
        }
    }

    private fun createGpsTextField(
        tagInfo: TagInfoGpsText,
        bytes: ByteArray
    ): TiffField = TiffField(
        offset = 0,
        tag = tagInfo.tag,
        directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
        fieldType = de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined,
        count = bytes.size,
        localValue = null,
        valueOffset = 0,
        valueBytes = bytes,
        byteOrder = ByteOrder.BIG_ENDIAN,
        sortHint = 0
    )

    private companion object {

        val GpsTagGpsProcessingMethod: TagInfoGpsText = TagInfoGpsText(
            tag = 0x001B,
            name = "GPSProcessingMethod",
            exifDirectory = TiffDirectoryType.EXIF_DIRECTORY_GPS
        )
    }
}
