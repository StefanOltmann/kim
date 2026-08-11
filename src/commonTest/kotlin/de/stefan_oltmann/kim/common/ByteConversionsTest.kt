/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.common

import de.stefan_oltmann.kim.input.readXBytesAtInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteConversionsTest {

    private val testBytes = byteArrayOf(-128, 0, 127, 64, -48, 110, 8, -4)

    @Test
    fun testToUInt8() {

        assertEquals(128, testBytes[0].toUInt8())
        assertEquals(0, testBytes[1].toUInt8())
        assertEquals(127, testBytes[2].toUInt8())
        assertEquals(64, testBytes[3].toUInt8())
        assertEquals(208, testBytes[4].toUInt8())
        assertEquals(110, testBytes[5].toUInt8())
        assertEquals(8, testBytes[6].toUInt8())
        assertEquals(252, testBytes[7].toUInt8())
    }

    @Test
    fun testShortToBytes() {

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02),
            actual = 0x0102.toShort().toBytes(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0x02, 0x01),
            actual = 0x0102.toShort().toBytes(ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            actual = shortArrayOf(0x0102.toShort(), 0x0304.toShort()).toBytes(ByteOrder.BIG_ENDIAN)
        )
    }

    @Test
    fun testIntToBytes() {

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            actual = 0x01020304.toInt().toBytes(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0x04, 0x03, 0x02, 0x01),
            actual = 0x01020304.toInt().toBytes(ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(1, 0, 0, 0, 2, 0, 0, 0),
            actual = intArrayOf(1, 2).toBytes(ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testFloatToBytes() {

        assertContentEquals(
            expected = byteArrayOf(0x3F, 0x80.toByte(), 0, 0),
            actual = 1.0f.toBytes(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0, 0, 0x80.toByte(), 0x3F),
            actual = 1.0f.toBytes(ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(0, 0, 0x80.toByte(), 0x3F, 0, 0, 0, 0x40),
            actual = floatArrayOf(1.0f, 2.0f).toBytes(ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testDoubleToBytes() {

        val expectedBigEndian = byteArrayOf(
            0x3F, 0xF0.toByte(), 0, 0, 0, 0, 0, 0
        )

        assertContentEquals(
            expected = expectedBigEndian,
            actual = 1.0.toBytes(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = expectedBigEndian.reversedArray(),
            actual = 1.0.toBytes(ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = expectedBigEndian + expectedBigEndian,
            actual = doubleArrayOf(1.0, 1.0).toBytes(ByteOrder.BIG_ENDIAN)
        )
    }

    @Test
    fun testRationalToBytes() {

        val rational = RationalNumber(1, 2)

        assertContentEquals(
            expected = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 2),
            actual = rational.toBytes(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(1, 0, 0, 0, 2, 0, 0, 0),
            actual = rational.toBytes(ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = byteArrayOf(1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0),
            actual = RationalNumbers(
                arrayOf(RationalNumber(1, 2), RationalNumber(3, 4))
            ).toBytes(ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testByteArrayToShorts() {

        assertEquals(
            expected = 0x0102,
            actual = byteArrayOf(0x01, 0x02).toUInt16(ByteOrder.BIG_ENDIAN)
        )

        assertEquals(
            expected = 0x0201,
            actual = byteArrayOf(0x01, 0x02).toUInt16(ByteOrder.LITTLE_ENDIAN)
        )

        assertEquals(
            expected = 0x0201,
            actual = byteArrayOf(0x00, 0x01, 0x02).toUInt16(1, ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = shortArrayOf(0x0102.toShort(), 0x0304.toShort()),
            actual = byteArrayOf(0x01, 0x02, 0x03, 0x04).toShorts(ByteOrder.BIG_ENDIAN)
        )
    }

    @Test
    fun testByteArrayToInt() {

        assertEquals(
            expected = 0x01020304,
            actual = byteArrayOf(0x01, 0x02, 0x03, 0x04).toInt(ByteOrder.BIG_ENDIAN)
        )

        assertEquals(
            expected = 0x04030201,
            actual = byteArrayOf(0x01, 0x02, 0x03, 0x04).toInt(ByteOrder.LITTLE_ENDIAN)
        )

        assertEquals(
            expected = 0x04030201,
            actual = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04).toInt(1, ByteOrder.LITTLE_ENDIAN)
        )

        assertContentEquals(
            expected = intArrayOf(0x01020304, 0x05060708),
            actual = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08).toInts(ByteOrder.BIG_ENDIAN)
        )
    }

    @Test
    fun testByteArrayToFloats() {

        assertContentEquals(
            expected = floatArrayOf(1.0f, 2.0f),
            actual = byteArrayOf(0x3F, 0x80.toByte(), 0, 0, 0x40, 0, 0, 0).toFloats(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = floatArrayOf(1.0f),
            actual = byteArrayOf(0, 0, 0x80.toByte(), 0x3F).toFloats(ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testByteArrayToDoubles() {

        assertContentEquals(
            expected = doubleArrayOf(1.0, 2.0),
            actual = byteArrayOf(
                0x3F, 0xF0.toByte(), 0, 0, 0, 0, 0, 0,
                0x40, 0, 0, 0, 0, 0, 0, 0
            ).toDoubles(ByteOrder.BIG_ENDIAN)
        )

        assertContentEquals(
            expected = doubleArrayOf(1.0),
            actual = byteArrayOf(0, 0, 0, 0, 0, 0, 0xF0.toByte(), 0x3F).toDoubles(ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testByteArrayToRationals() {

        val rationals = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 2).toRationals(
            unsignedType = false,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        assertEquals(RationalNumber(1, 2), rationals.values.first())

        /* Unsigned values keep their mask. */
        val unsignedRationals = byteArrayOf(-1, -1, -1, -1, 0, 0, 0, 1).toRationals(
            unsignedType = true,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        assertEquals(0xFFFFFFFFL, unsignedRationals.values.first().numerator)
    }

    @Test
    fun testQuadsToByteArray() {

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            actual = 0x01020304.toInt().quadsToByteArray()
        )
    }

    @Test
    fun testReadXBytesAtInt() {

        val reader = de.stefan_oltmann.kim.input.ByteArrayByteReader(
            byteArrayOf(
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
            )
        )

        assertEquals(0x01, reader.readXBytesAtInt("1 byte", 1, ByteOrder.BIG_ENDIAN))
        assertEquals(0x0203, reader.readXBytesAtInt("2 bytes", 2, ByteOrder.BIG_ENDIAN))
        assertEquals(0x04050607, reader.readXBytesAtInt("4 bytes", 4, ByteOrder.BIG_ENDIAN))
        assertEquals(
            expected = 0x08090A0B0C0D0E0FL,
            actual = reader.readXBytesAtInt("8 bytes", 8, ByteOrder.BIG_ENDIAN)
        )
    }
}
