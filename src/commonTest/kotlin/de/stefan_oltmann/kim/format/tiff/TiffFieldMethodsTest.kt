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
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeDouble
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeFloat
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeRational
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSShort
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TiffFieldMethodsTest {

    private fun field(
        tag: Int,
        fieldType: de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType<out Any>,
        valueBytes: ByteArray,
        count: Int = valueBytes.size / fieldType.size
    ): TiffField = TiffField(
        offset = 0,
        tag = tag,
        directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
        fieldType = fieldType,
        count = count,
        localValue = null,
        valueOffset = 0,
        valueBytes = valueBytes,
        byteOrder = ByteOrder.BIG_ENDIAN,
        sortHint = 0
    )

    @Test
    fun testOffsetAndTagFormatted() {

        val tiffField = field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 1))

        assertEquals("0000000000", tiffField.offsetFormatted)
        assertEquals("0x0100", tiffField.tagFormatted)
    }

    @Test
    fun testValueDescriptionForByteArrays() {

        /* Single byte. */
        assertEquals(
            expected = "-5",
            actual = field(0x0100, FieldTypeByte, byteArrayOf(-5)).valueDescription
        )

        /* Small array as hex. */
        assertEquals(
            expected = "[0x01, 0x02]",
            actual = field(0x0100, FieldTypeByte, byteArrayOf(1, 2)).valueDescription
        )

        /* Large array shows the size. */
        assertEquals(
            expected = "[20 bytes]",
            actual = field(0x0100, FieldTypeByte, ByteArray(20)).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForIntArrays() {

        assertEquals(
            expected = "5",
            actual = field(
                0x0100,
                FieldTypeLong,
                intArrayOf(5).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )

        assertEquals(
            expected = "[1, 2]",
            actual = field(
                0x0100,
                FieldTypeLong,
                intArrayOf(1, 2).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )

        /* More than 16 values show the size. */
        val manyInts = IntArray(17) { it }

        assertEquals(
            expected = "[17 ints]",
            actual = field(
                0x0100,
                FieldTypeLong,
                manyInts.toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForShorts() {

        assertEquals(
            expected = "7",
            actual = field(
                0x0100,
                FieldTypeShort,
                shortArrayOf(7).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForDoubles() {

        assertEquals(
            expected = "1.5",
            actual = field(
                0x0100,
                FieldTypeDouble,
                doubleArrayOf(1.5).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )

        /* More than 16 values show the size. */
        assertEquals(
            expected = "[17 doubles]",
            actual = field(
                0x0100,
                FieldTypeDouble,
                DoubleArray(17) { it.toDouble() }.toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForFloats() {

        assertEquals(
            expected = "1.5",
            actual = field(
                0x0100,
                FieldTypeFloat,
                floatArrayOf(1.5f).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForRationals() {

        assertEquals(
            expected = "1/2 (0.5)",
            actual = field(
                0x0100,
                FieldTypeRational,
                RationalNumber(1, 2).toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )

        /* More than 16 values show the size. */
        val manyRationals = RationalNumbers(
            Array(17) { index ->
                RationalNumber(index + 1, 1)
            }
        )

        assertEquals(
            expected = "[17 rationals]",
            actual = field(
                0x0100,
                FieldTypeRational,
                manyRationals.toBytes(ByteOrder.BIG_ENDIAN)
            ).valueDescription
        )
    }

    @Test
    fun testValueDescriptionForStrings() {

        assertEquals(
            expected = "Hello",
            actual = field(
                0x0100,
                FieldTypeAscii,
                "Hello\u0000".encodeToByteArray()
            ).valueDescription
        )
    }

    @Test
    fun testToStringWithUnknownTag() {

        val tiffField = field(0x9999, FieldTypeLong, byteArrayOf(0, 0, 0, 1))

        assertTrue(tiffField.tagInfo == null)
        assertTrue(tiffField.toString().contains("Unknown"))
    }

    @Test
    fun testToStringValue() {

        /* List values return the first element. */
        val listField = field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 1))
        assertEquals("1", listField.toInt().toString())

        val stringField = field(0x0100, FieldTypeAscii, "Hi\u0000".encodeToByteArray())
        assertEquals("Hi", stringField.toStringValue())

        /* Non-string values fail. */
        assertFailsWith<ImageReadException> {
            field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 1)).toStringValue()
        }
    }

    @Test
    fun testToIntArray() {

        /* Numbers. */
        assertContentEquals(
            expected = intArrayOf(5),
            actual = field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 5)).toIntArray()
        )

        /* Int arrays. */
        assertContentEquals(
            expected = intArrayOf(1, 2),
            actual = field(
                0x0100,
                FieldTypeLong,
                intArrayOf(1, 2).toBytes(ByteOrder.BIG_ENDIAN)
            ).toIntArray()
        )

        /* Short arrays are converted as unsigned. */
        assertContentEquals(
            expected = intArrayOf(65535),
            actual = field(
                0x0100,
                FieldTypeShort,
                shortArrayOf(-1).toBytes(ByteOrder.BIG_ENDIAN)
            ).toIntArray()
        )

        /* Strings fail. */
        assertFailsWith<ImageReadException> {
            field(0x0100, FieldTypeAscii, "Hi\u0000".encodeToByteArray()).toIntArray()
        }
    }

    @Test
    fun testToInt() {

        assertEquals(
            expected = 5,
            actual = field(0x0100, FieldTypeByte, byteArrayOf(5)).toInt()
        )

        assertEquals(
            expected = 6,
            actual = field(0x0100, FieldTypeShort, shortArrayOf(6).toBytes(ByteOrder.BIG_ENDIAN)).toInt()
        )

        assertEquals(
            expected = 7,
            actual = field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 7)).toInt()
        )

        /* The SHORT type is unsigned, values above 32767 are not negative. */
        assertEquals(
            expected = 51200,
            actual = field(
                0x0100,
                FieldTypeShort,
                shortArrayOf(51200.toShort()).toBytes(ByteOrder.BIG_ENDIAN)
            ).toInt()
        )

        /* The SSHORT type is signed, negative values must stay negative. */
        assertEquals(
            expected = -300,
            actual = field(
                0x0100,
                FieldTypeSShort,
                shortArrayOf((-300).toShort()).toBytes(ByteOrder.BIG_ENDIAN)
            ).toInt()
        )
    }

    @Test
    fun testToDouble() {

        assertEquals(
            expected = 0.5,
            actual = field(
                0x0100,
                FieldTypeRational,
                RationalNumber(1, 2).toBytes(ByteOrder.BIG_ENDIAN)
            ).toDouble()
        )

        assertEquals(
            expected = 1.0,
            actual = field(0x0100, FieldTypeFloat, floatArrayOf(1.0f).toBytes(ByteOrder.BIG_ENDIAN)).toDouble()
        )

        assertEquals(
            expected = 2.0,
            actual = field(0x0100, FieldTypeDouble, doubleArrayOf(2.0).toBytes(ByteOrder.BIG_ENDIAN)).toDouble()
        )

        assertEquals(
            expected = 3.0,
            actual = field(0x0100, FieldTypeLong, byteArrayOf(0, 0, 0, 3)).toDouble()
        )

        /* The SHORT type is unsigned, values above 32767 are not negative. */
        assertEquals(
            expected = 51200.0,
            actual = field(
                0x0100,
                FieldTypeShort,
                shortArrayOf(51200.toShort()).toBytes(ByteOrder.BIG_ENDIAN)
            ).toDouble()
        )
    }
}
