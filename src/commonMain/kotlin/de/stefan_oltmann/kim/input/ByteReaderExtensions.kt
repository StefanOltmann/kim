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
@file:Suppress("TooManyFunctions")

package de.stefan_oltmann.kim.input

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.quadsToByteArray
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter

/*
 * For easier implementation of the [ByteReader] in
 * Java these functions are designed as extension functions.
 */

internal fun ByteReader.readByte(fieldName: String): Byte =
    readByte() ?: throw ImageReadException("Couldn't read byte for $fieldName")

internal fun ByteReader.readBytes(fieldName: String, count: Int): ByteArray {

    if (count < 0)
        throw ImageReadException("Couldn't read $fieldName, invalid length: $count")

    val bytes = readBytes(count)

    if (bytes.size != count)
        throw ImageReadException("Couldn't read $count bytes for $fieldName. Got only ${bytes.size}.")

    return bytes
}

internal fun ByteReader.readNullTerminatedString(fieldName: String): String {

    val bytes = mutableListOf<Byte>()

    var byte: Byte?

    while (true) {

        byte = readByte()

        if (byte == null)
            throw ImageReadException("No bytes for $fieldName, never reached terminator byte.")

        if (byte.toInt() == 0)
            break

        bytes.add(byte)
    }

    return bytes.toByteArray().decodeToString()
}

/** Reads one byte as unsigned number, also known as "byte" or "UInt8". */
internal fun ByteReader.readByteAsInt(): Int =
    readByte()?.toUInt8() ?: -1

/** Reads 2 bytes as unsigned number, also known as "short" or "UInt16". */
internal fun ByteReader.read2BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()

    if (byte0 or byte1 < 0)
        throw ImageReadException("Couldn't read two bytes for $fieldName")

    return if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0 shl Byte.SIZE_BITS or byte1
    else
        byte1 shl Byte.SIZE_BITS or byte0
}

/** Reads 4 bytes as unsigned number, also known as "int" or "UInt32". */
internal fun ByteReader.read4BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()
    val byte2 = readByteAsInt()
    val byte3 = readByteAsInt()

    if (byte0 or byte1 or byte2 or byte3 < 0)
        throw ImageReadException("Couldn't read 4 bytes for $fieldName")

    val result: Int = if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0 shl 24 or (byte1 shl 16) or (byte2 shl 8) or (byte3 shl 0)
    else
        byte3 shl 24 or (byte2 shl 16) or (byte1 shl 8) or (byte0 shl 0)

    return result
}

/** Reads 8 bytes as unsigned number, also known as "long" or "UInt64". */
internal fun ByteReader.read8BytesAsLong(fieldName: String, byteOrder: ByteOrder): Long {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()
    val byte2 = readByteAsInt()
    val byte3 = readByteAsInt()
    val byte4 = readByteAsInt()
    val byte5 = readByteAsInt()
    val byte6 = readByteAsInt()
    val byte7 = readByteAsInt()

    if (byte0 or byte1 or byte2 or byte3 or byte4 or byte5 or byte6 or byte7 < 0)
        throw ImageReadException("Couldn't read 8 bytes for $fieldName")

    val result: Long = if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0.toLong() shl 56 or (byte1.toLong() shl 48) or (byte2.toLong() shl 40) or (byte3.toLong() shl 32) or
            (byte4.toLong() shl 24) or (byte5.toLong() shl 16) or (byte6.toLong() shl 8) or (byte7.toLong() shl 0)
    else
        byte7.toLong() shl 56 or (byte6.toLong() shl 48) or (byte5.toLong() shl 40) or (byte4.toLong() shl 32) or
            (byte3.toLong() shl 24) or (byte2.toLong() shl 16) or (byte1.toLong() shl 8) or (byte0.toLong() shl 0)

    return result
}

internal fun ByteReader.readXBytesAtInt(fieldName: String, count: Int, byteOrder: ByteOrder): Long =
    when (count) {
        Byte.SIZE_BYTES -> readByteAsInt().toLong()
        Short.SIZE_BYTES -> read2BytesAsInt(fieldName, byteOrder).toLong()
        Int.SIZE_BYTES -> read4BytesAsInt(fieldName, byteOrder).toLong()
        Long.SIZE_BYTES -> read8BytesAsLong(fieldName, byteOrder)
        else -> error("Illegal byteCount specified: $count")
    }

internal fun ByteReader.readAndVerifyBytes(fieldName: String, expectedBytes: ByteArray) {

    for (index in expectedBytes.indices) {

        val byte = readByte()
            ?: throw ImageReadException("Unexpected EOF for $fieldName")

        if (byte != expectedBytes[index])
            throw ImageReadException("Byte $index is different by reading $fieldName: ${byte.toHex()}")
    }
}

internal fun ByteReader.readRemainingBytes(): ByteArray {

    val os = ByteArrayByteWriter()

    while (true) {

        val bytes = readBytes(DEFAULT_BUFFER_SIZE)

        if (bytes.isEmpty())
            break

        os.write(bytes)
    }

    return os.toByteArray()
}

/**
 * Streams the remaining bytes to the given writer in bounded chunks, so the
 * whole content never has to be buffered in memory at once.
 */
internal fun ByteReader.copyRemainingTo(byteWriter: ByteWriter) {

    while (true) {

        val chunk = readBytes(DEFAULT_BUFFER_SIZE)

        if (chunk.isEmpty())
            break

        byteWriter.write(chunk)
    }
}

/**
 * Transfers exactly the given number of bytes from the reader to the
 * writer, or discards them when the writer is NULL, so container chunks
 * can be skipped while the stream stays in sync.
 *
 * Unlike [copyRemainingTo] this works with a known byte count, which
 * allows filtering individual chunks of a container format.
 *
 * A negative count or a stream that ends before all bytes are transferred
 * means the source file is corrupt and throws [ImageReadException], so a
 * rewrite can never emit a silently truncated copy of the image data.
 */
internal fun ByteReader.transferExactly(
    byteWriter: ByteWriter?,
    count: Long
) {

    if (count < 0)
        throw ImageReadException("Can't transfer a negative byte count: $count")

    var remaining = count

    while (remaining > 0) {

        val chunk = readBytes(minOf(remaining, DEFAULT_BUFFER_SIZE.toLong()).toInt())

        /*
         * The stream must not end before all requested bytes were read,
         * otherwise the output would be truncated without notice.
         */
        if (chunk.isEmpty())
            throw ImageReadException(
                "Stream ended after ${count - remaining} of $count bytes."
            )

        byteWriter?.write(chunk)

        remaining -= chunk.size
    }
}

internal fun ByteReader.skipBytes(fieldName: String, count: Int) =
    skipBytes(fieldName, count.toLong())

/**
 * Skips the given number of bytes in bounded chunks.
 *
 * The count is a Long, so streams larger than the signed Int range can be
 * skipped without wrapping around into a negative or wrong count.
 */
internal fun ByteReader.skipBytes(fieldName: String, count: Long) {

    /* Nothing to do. */
    if (count == 0L)
        return

    if (count < 0L)
        throw ImageReadException("Couldn't read $fieldName, invalid length: $count")

    var remaining = count

    /*
     * Read in bounded chunks, so a large skip does not allocate
     * the whole request size at once.
     */
    while (remaining > 0) {

        val skippedByteCount = readBytes(minOf(remaining, DEFAULT_BUFFER_SIZE.toLong()).toInt()).size

        if (skippedByteCount == 0) {

            val missingBytesCount = remaining

            throw ImageReadException(
                "Skipped ${count - remaining} bytes of $count for $fieldName: " +
                    "Missing $missingBytesCount bytes."
            )
        }

        remaining -= skippedByteCount
    }
}

internal fun ByteReader.skipToQuad(quad: Int): Boolean =
    skipToBytes(quad.quadsToByteArray())

internal fun ByteReader.skipToBytes(needle: ByteArray): Boolean {

    var position = 0

    while (true) {

        val byte = readByteAsInt()

        if (byte == -1)
            break

        if (needle[position].toInt() == byte) {

            position++

            if (position == needle.size) {
                return true
            }

        } else {

            position = 0
        }
    }

    return false
}
