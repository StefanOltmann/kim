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
package de.stefan_oltmann.kim.input

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ByteReaderExtensionsTest {

    private fun readerOf(vararg bytes: Int): ByteArrayByteReader =
        ByteArrayByteReader(bytes.map { it.toByte() }.toByteArray())

    @Test
    fun testReadByte() {

        val reader = readerOf(0x01, 0x02)

        assertEquals(0x01.toByte(), reader.readByte("byte"))
        assertEquals(0x02.toByte(), reader.readByte("byte"))

        /* No bytes left. */
        assertFailsWith<ImageReadException> {
            reader.readByte("byte")
        }
    }

    @Test
    fun testReadBytes() {

        val reader = readerOf(0x01, 0x02, 0x03)

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02),
            actual = reader.readBytes("bytes", 2)
        )

        /* Negative length. */
        assertFailsWith<ImageReadException> {
            reader.readBytes("bytes", -1)
        }

        /* Too few bytes left. */
        assertFailsWith<ImageReadException> {
            reader.readBytes("bytes", 5)
        }
    }

    @Test
    fun testReadNullTerminatedString() {

        val reader = readerOf('H'.code, 'i'.code, 0, 'x'.code)

        assertEquals("Hi", reader.readNullTerminatedString("string"))

        /* No terminator. */
        assertFailsWith<ImageReadException> {
            readerOf('a'.code, 'b'.code).readNullTerminatedString("string")
        }
    }

    @Test
    fun testReadByteAsInt() {

        val reader = readerOf(0xFF, 0x01)

        assertEquals(255, reader.readByteAsInt())

        /* Returns -1 when no bytes are left. */
        assertEquals(1, reader.readByteAsInt())
        assertEquals(-1, reader.readByteAsInt())
    }

    @Test
    fun testRead2BytesAsInt() {

        assertEquals(
            expected = 0x0102,
            actual = readerOf(0x01, 0x02).read2BytesAsInt("short", ByteOrder.BIG_ENDIAN)
        )

        assertEquals(
            expected = 0x0201,
            actual = readerOf(0x01, 0x02).read2BytesAsInt("short", ByteOrder.LITTLE_ENDIAN)
        )

        /* Not enough bytes. */
        assertFailsWith<ImageReadException> {
            readerOf(0x01).read2BytesAsInt("short", ByteOrder.BIG_ENDIAN)
        }
    }

    @Test
    fun testRead4BytesAsInt() {

        assertEquals(
            expected = 0x01020304,
            actual = readerOf(0x01, 0x02, 0x03, 0x04).read4BytesAsInt("int", ByteOrder.BIG_ENDIAN)
        )

        assertEquals(
            expected = 0x04030201,
            actual = readerOf(0x01, 0x02, 0x03, 0x04).read4BytesAsInt("int", ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testRead8BytesAsLong() {

        assertEquals(
            expected = 0x0102030405060708L,
            actual = readerOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
                .read8BytesAsLong("long", ByteOrder.BIG_ENDIAN)
        )

        assertEquals(
            expected = 0x0807060504030201L,
            actual = readerOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
                .read8BytesAsLong("long", ByteOrder.LITTLE_ENDIAN)
        )
    }

    @Test
    fun testReadAndVerifyBytes() {

        val reader = readerOf(0x01, 0x02, 0x03)

        reader.readAndVerifyBytes("signature", byteArrayOf(0x01, 0x02))

        /* Unexpected byte. */
        assertFailsWith<ImageReadException> {
            readerOf(0x01, 0x02).readAndVerifyBytes("signature", byteArrayOf(0x01, 0x03))
        }

        /* Unexpected EOF. */
        assertFailsWith<ImageReadException> {
            readerOf(0x01).readAndVerifyBytes("signature", byteArrayOf(0x01, 0x02))
        }
    }

    @Test
    fun testReadRemainingBytes() {

        val reader = readerOf(0x01, 0x02, 0x03)

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x03),
            actual = reader.readRemainingBytes()
        )

        /* Nothing left. */
        assertContentEquals(byteArrayOf(), reader.readRemainingBytes())
    }

    @Test
    fun testSkipBytes() {

        val reader = readerOf(0x01, 0x02, 0x03, 0x04)

        /* Skipping zero bytes is a no-op. */
        reader.skipBytes("skip", 0)
        assertEquals(0x01, reader.readByteAsInt())

        reader.skipBytes("skip", 2)
        assertEquals(0x04, reader.readByteAsInt())

        /* Negative count. */
        assertFailsWith<ImageReadException> {
            reader.skipBytes("skip", -1)
        }

        /* Too many bytes. */
        assertFailsWith<ImageReadException> {
            readerOf(0x01).skipBytes("skip", 2)
        }
    }

    @Test
    fun testSkipToBytes() {

        val reader = readerOf(0x00, 0x00, 0x01, 0x02, 0x03)

        assertTrue(reader.skipToBytes(byteArrayOf(0x01, 0x02)))
        assertEquals(0x03, reader.readByteAsInt())

        /* Not found. */
        assertFalse(readerOf(0x00, 0x00).skipToBytes(byteArrayOf(0x01)))
    }

    @Test
    fun testSkipToQuad() {

        val reader = readerOf(0x00, 0x00, 0x00, 0x01, 0x02, 0x03)

        assertTrue(reader.skipToQuad(1))
        assertEquals(0x02, reader.readByteAsInt())
    }
}
