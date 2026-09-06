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
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
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

    /**
     * A large skip must be read in bounded chunks, so the peak
     * allocation stays within the buffer size.
     */
    @Test
    fun testSkipBytesUsesBoundedChunks() {

        val skipCount = 5 * 1024 * 1024 + 123

        val recordingReader = RecordingByteReader(
            ByteArrayByteReader(ByteArray(skipCount))
        )

        recordingReader.skipBytes("skip", skipCount)

        /* The skip must be complete. */
        assertNull(recordingReader.readByte())

        /* The largest single read must stay within the buffer size. */
        assertTrue(recordingReader.maxRequestedReadSize <= DEFAULT_BUFFER_SIZE)
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

    @Test
    fun testSkipToBytesFindsSelfOverlappingPattern() {

        /* Needle FF D8 occurs at offset 1 in FF FF D8 - naive reset misses it. */
        assertTrue(
            readerOf(0xFF, 0xFF, 0xD8).skipToBytes(
                byteArrayOf(0xFF.toByte(), 0xD8.toByte())
            )
        )

        /* Needle 01 01 02 occurs at offset 1 in 01 01 01 02. */
        assertTrue(
            readerOf(0x01, 0x01, 0x01, 0x02).skipToBytes(
                byteArrayOf(0x01, 0x01, 0x02)
            )
        )

        /* Overlapping prefix-suffix: AB AB occurs at offset 1 in AB AB AB. */
        assertTrue(
            readerOf(0xAB, 0xAB, 0xAB).skipToBytes(
                byteArrayOf(0xAB.toByte(), 0xAB.toByte(), 0xAB.toByte())
            )
        )
    }

    @Test
    fun testSkipToBytesFindsNeedleAtEofBoundaryWithOverlap() {

        val reader = readerOf(0x01, 0x02, 0x01, 0x02, 0x01, 0x03)

        /* Needle 01 02 01 03 has prefix 01 that is also suffix of false start. */
        assertTrue(
            reader.skipToBytes(
                byteArrayOf(0x01, 0x02, 0x01, 0x03)
            )
        )

        /* Must be at EOF after consuming the needle. */
        assertEquals(-1, reader.readByteAsInt())
    }

    @Test
    fun testTransferExactly() {

        val reader = readerOf(0x01, 0x02, 0x03)

        val writer = ByteArrayByteWriter()

        reader.transferExactly(writer, 2)

        reader.transferExactly(null, 1)

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02),
            actual = writer.toByteArray()
        )

        /* Everything was consumed. */
        assertEquals(-1, reader.readByteAsInt())
    }

    /*
     * An early end of stream must fail loudly, otherwise a rewrite would
     * emit a silently truncated copy of the image data.
     */
    @Test
    fun testTransferExactlyRejectsEarlyEndOfStream() {

        assertFailsWith<ImageReadException> {
            readerOf(0x01).transferExactly(ByteArrayByteWriter(), 2)
        }
    }

    @Test
    fun testTransferExactlyRejectsNegativeCount() {

        assertFailsWith<ImageReadException> {
            readerOf(0x01).transferExactly(null, -1)
        }
    }

    /**
     * A ByteReader that records the largest read request size.
     */
    private class RecordingByteReader(
        private val delegate: ByteReader
    ) : ByteReader {

        var maxRequestedReadSize: Int = 0
            private set

        override val contentLength: Long =
            delegate.contentLength

        override fun readByte(): Byte? =
            delegate.readByte()

        override fun readBytes(count: Int): ByteArray {

            if (count > maxRequestedReadSize)
                maxRequestedReadSize = count

            return delegate.readBytes(count)
        }

        override fun close() =
            delegate.close()
    }
    /**
     * Reading a 1-byte field at the end of the data must fail like the
     * 2/4/8-byte reads do. The -1 sentinel of readByteAsInt must not
     * leak into callers as a legitimate value.
     */
    @Test
    fun testReadXBytesAtIntOneByteAtEofThrows() {

        val reader = ByteArrayByteReader(byteArrayOf())

        assertFailsWith<ImageReadException> {
            reader.readXBytesAtInt("test field", count = 1, byteOrder = ByteOrder.BIG_ENDIAN)
        }
    }
}
