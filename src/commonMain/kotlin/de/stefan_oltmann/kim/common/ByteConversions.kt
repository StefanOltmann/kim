/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
@file:Suppress("MagicNumber", "TooManyFunctions")

package de.stefan_oltmann.kim.common

/**
 * Convenience methods for converting data types to and from
 * byte arrays.
 */

@Suppress("MagicNumber")
internal fun Byte.toUInt8(): Int = 0xFF and toInt()

/*
 * The byte-order primitives all conversions below build on: a value is
 * split into or assembled from its bytes, most significant byte first
 * for big endian and least significant byte first for little endian.
 *
 * All arithmetic runs on the 64-bit bit pattern of the value, so the
 * low byte of a shift is identical for signed and unsigned readings.
 */
private fun writeBytes(
    dest: ByteArray,
    offset: Int,
    bits: Long,
    byteCount: Int,
    byteOrder: ByteOrder
) {

    for (index in 0 until byteCount) {

        val shift = 8 * (if (byteOrder == ByteOrder.BIG_ENDIAN) byteCount - 1 - index else index)

        dest[offset + index] = (bits shr shift).toByte()
    }
}

/**
 * Reads up to 8 bytes starting at [offset] as an unsigned number in the
 * given [byteOrder], so callers with already-buffered bytes get the same
 * conversion the ByteReader extensions provide.
 */
internal fun ByteArray.readUnsignedInt(offset: Int, byteCount: Int, byteOrder: ByteOrder): Long =
    readBytes(this, offset, byteCount, byteOrder)

private fun readBytes(
    source: ByteArray,
    offset: Int,
    byteCount: Int,
    byteOrder: ByteOrder
): Long {

    var bits = 0L

    for (index in 0 until byteCount) {

        val shift = 8 * (if (byteOrder == ByteOrder.BIG_ENDIAN) byteCount - 1 - index else index)

        bits = bits or (0xFFL and source[offset + index].toLong() shl shift)
    }

    return bits
}

internal fun Short.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(2)

    writeBytes(result, 0, toInt().toLong(), 2, byteOrder)

    return result
}

internal fun ShortArray.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(size * 2)

    for (index in indices)
        writeBytes(result, index * 2, this[index].toInt().toLong(), 2, byteOrder)

    return result
}

internal fun Int.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(4)

    writeBytes(result, 0, toLong(), 4, byteOrder)

    return result
}

internal fun IntArray.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(size * 4)

    for (i in indices)
        writeBytes(result, i * 4, this[i].toLong(), 4, byteOrder)

    return result
}

internal fun Long.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(8)

    writeBytes(result, 0, this, 8, byteOrder)

    return result
}

internal fun LongArray.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(size * 8)

    for (i in indices)
        writeBytes(result, i * 8, this[i], 8, byteOrder)

    return result
}

internal fun Float.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(4)

    writeBytes(result, 0, toRawBits().toLong(), 4, byteOrder)

    return result
}

internal fun FloatArray.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(size * 4)

    for (i in indices)
        writeBytes(result, i * 4, this[i].toRawBits().toLong(), 4, byteOrder)

    return result
}

internal fun Double.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(8)

    writeBytes(result, 0, toRawBits(), 8, byteOrder)

    return result
}

internal fun DoubleArray.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(size * 8)

    for (i in indices)
        writeBytes(result, i * 8, this[i].toRawBits(), 8, byteOrder)

    return result
}

internal fun RationalNumber.toBytes(byteOrder: ByteOrder): ByteArray {

    val result = ByteArray(8)

    writeBytes(result, 0, numerator, 4, byteOrder)
    writeBytes(result, 4, divisor, 4, byteOrder)

    return result
}

internal fun RationalNumbers.toBytes(
    byteOrder: ByteOrder
): ByteArray {

    val result = ByteArray(values.size * 8)

    for (index in values.indices)
        values[index].toBytes(result, index * 8, byteOrder)

    return result
}

private fun RationalNumber.toBytes(
    result: ByteArray,
    offset: Int,
    byteOrder: ByteOrder
) {

    writeBytes(result, offset, numerator, 4, byteOrder)
    writeBytes(result, offset + 4, divisor, 4, byteOrder)
}

internal fun ByteArray.toShorts(byteOrder: ByteOrder): ShortArray =
    ShortArray(size / 2) { index -> toUInt16(2 * index, byteOrder).toShort() }

internal fun ByteArray.toUInt16(byteOrder: ByteOrder): Int =
    toUInt16(0, byteOrder)

internal fun ByteArray.toUInt16(offset: Int, byteOrder: ByteOrder): Int =
    readBytes(this, offset, 2, byteOrder).toInt()

internal fun ByteArray.toInt(byteOrder: ByteOrder): Int =
    this.toInt(0, byteOrder)

internal fun ByteArray.toInt(offset: Int, byteOrder: ByteOrder): Int =
    readBytes(this, offset, 4, byteOrder).toInt()

internal fun ByteArray.toInts(byteOrder: ByteOrder): IntArray =
    IntArray(size / 4) { index -> toInt(4 * index, byteOrder) }

internal fun ByteArray.toLong(offset: Int, byteOrder: ByteOrder): Long =
    readBytes(this, offset, 8, byteOrder)

internal fun ByteArray.toLongs(byteOrder: ByteOrder): LongArray =
    LongArray(size / 8) { index -> toLong(8 * index, byteOrder) }

private fun ByteArray.toFloat(
    offset: Int,
    byteOrder: ByteOrder
): Float = Float.fromBits(readBytes(this, offset, 4, byteOrder).toInt())

internal fun ByteArray.toFloats(byteOrder: ByteOrder): FloatArray =
    FloatArray(size / 4) { index -> toFloat(4 * index, byteOrder) }

private fun ByteArray.toDouble(
    offset: Int,
    byteOrder: ByteOrder
): Double = Double.fromBits(readBytes(this, offset, 8, byteOrder))

internal fun ByteArray.toDoubles(byteOrder: ByteOrder): DoubleArray =
    DoubleArray(size / 8) { index -> toDouble(8 * index, byteOrder) }

private fun ByteArray.toRational(
    offset: Int,
    unsignedType: Boolean,
    byteOrder: ByteOrder
): RationalNumber = RationalNumber(
    numerator = readBytes(this, offset, 4, byteOrder).toInt(),
    divisor = readBytes(this, offset + 4, 4, byteOrder).toInt(),
    unsignedType = unsignedType
)

internal fun ByteArray.toRationals(
    unsignedType: Boolean,
    byteOrder: ByteOrder
): RationalNumbers = RationalNumbers(
    values = Array(size / 8) { index ->
        this.toRational(8 * index, unsignedType, byteOrder)
    }
)
