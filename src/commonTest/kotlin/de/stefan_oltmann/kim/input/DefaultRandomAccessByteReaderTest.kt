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

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRandomAccessByteReaderTest {

    private val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    @Test
    fun testSequentialReads() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertContentEquals(byteArrayOf(1, 2, 3), reader.readBytes(3))
        assertContentEquals(byteArrayOf(4, 5, 6, 7, 8, 9, 10), reader.readBytes(100))

        assertNull(reader.readByte())
        assertContentEquals(byteArrayOf(), reader.readBytes(10))
    }

    /**
     * Reads past the end of the stream must return short arrays
     * instead of zero-filled data.
     */
    @Test
    fun testReadBytesPastEofIsShort() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        val result = reader.readBytes(0, 100)

        assertContentEquals(bytes, result)
    }

    /**
     * Even when the reported content length is larger than the actual
     * data, reads must not be padded with zeros.
     */
    @Test
    fun testReadBytesPastEofWithInaccurateContentLength() {

        val reader = DefaultRandomAccessByteReader(ShortStreamReader(bytes))

        /* Random access must not be padded with zeros either. */
        assertContentEquals(bytes, reader.readBytes(0, 100))

        /* Sequential reads must stop at the end of the actual data. */
        reader.moveTo(0)

        assertContentEquals(bytes, reader.readBytes(100))
        assertContentEquals(byteArrayOf(), reader.readBytes(100))

        assertNull(reader.readByte())
    }

    @Test
    fun testRandomAccessReadPastEofIsShort() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertContentEquals(byteArrayOf(9, 10), reader.readBytes(8, 100))

        /* Reads starting past the end are empty. */
        assertContentEquals(byteArrayOf(), reader.readBytes(10, 100))
    }

    /**
     * Hostile files can resolve offsets beyond the signed Int range.
     * Such reads must fail cleanly instead of crashing inside
     * copyOfRange with a wrapped-around index.
     */
    @Test
    fun testRandomAccessReadRejectsNegativeOffset() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertFailsWith<IllegalArgumentException> {
            reader.readBytes(-1, 4)
        }
    }

    /**
     * A hostile length that overflows the offset addition must not wrap
     * the end index back into the valid range and crash inside
     * copyOfRange. Reads behind the content stay empty.
     */
    @Test
    fun testRandomAccessReadWithOverflowingOffsetSumIsShort() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertContentEquals(byteArrayOf(), reader.readBytes(Int.MAX_VALUE - 4, 100))
    }

    /**
     * The preview extraction pattern: move to an offset and read forward.
     */
    @Test
    fun testMoveToAndRead() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        reader.moveTo(5)

        assertContentEquals(byteArrayOf(6, 7, 8), reader.readBytes(3))

        /* The random access read is independent of the position. */
        assertContentEquals(byteArrayOf(3, 4, 5), reader.readBytes(2, 3))

        assertTrue(reader.contentLength == bytes.size.toLong())
    }

    /**
     * Regression test: moving to the content end must be allowed - like
     * in ByteArrayByteReader - so reads from there return empty arrays
     * instead of failing. This also keeps moveTo(0) working for empty
     * content.
     */
    @Test
    fun testMoveToContentEnd() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        reader.moveTo(bytes.size)

        assertNull(reader.readByte())
        assertContentEquals(byteArrayOf(), reader.readBytes(4))
    }

    @Test
    fun testMoveToZeroOnEmptyContent() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(byteArrayOf()))

        /* Must not fail for empty content. */
        reader.moveTo(0)

        assertNull(reader.readByte())
    }

    @Test
    fun testMoveToRejectsNegativePosition() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertFailsWith<IllegalArgumentException> {
            reader.moveTo(-1)
        }
    }

    /**
     * Regression test: a content length beyond the signed Int range
     * (e.g. a 3 GiB TIFF) previously wrapped [DefaultRandomAccessByteReader.readBytes]'s
     * coercion into a negative value, crashing inside copyOfRange.
     * The Long-space clamp must return a normal short read instead.
     */
    @Test
    fun testSequentialReadWithHugeContentLengthDoesNotCrash() {

        val hugeLengthReader = object : ByteReader {
            override val contentLength: Long = Int.MAX_VALUE + 1024L

            override fun readByte(): Byte? = null

            override fun readBytes(count: Int): ByteArray = byteArrayOf()

            override fun close() { /* Does nothing. */
            }
        }

        val reader = DefaultRandomAccessByteReader(hugeLengthReader)

        /* Must return an empty array instead of crashing. */
        assertContentEquals(byteArrayOf(), reader.readBytes(10))
    }

    /**
     * A reader whose contentLength is larger than the actual data,
     * like a stream with an inaccurate file size.
     */
    private class ShortStreamReader(
        private val bytes: ByteArray
    ) : ByteReader {

        override val contentLength: Long = 100

        private var position = 0

        override fun readByte(): Byte? {

            if (position >= bytes.size)
                return null

            return bytes[position++]
        }

        override fun readBytes(count: Int): ByteArray {

            val result = bytes.copyOfRange(position, minOf(position + count, bytes.size))

            position += result.size

            return result
        }

        override fun close() {
            /* Does nothing. */
        }
    }
}
