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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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

        assertEquals(bytes.size.toLong(), reader.contentLength)
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

    /** Negative counts are rejected like in the other implementations. */
    @Test
    fun testReadBytesRejectsNegativeCount() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(byteArrayOf(1, 2, 3)))

        val exception = assertFailsWith<IllegalArgumentException> {
            reader.readBytes(-1)
        }

        assertTrue(exception.message?.contains("negative") == true)
    }

    @Test
    fun testMoveToRejectsNegativePosition() {

        val reader = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes))

        assertFailsWith<IllegalArgumentException> {
            reader.moveTo(-1)
        }
    }

    /**
     * A stream that delivers fewer bytes than its declared contentLength
     * must produce short reads instead of a raw range error once the
     * position is past the delivered data.
     */
    @Test
    fun testReadBytesWithShortDeliveringDelegateReturnsShortRead() {

        val reader = DefaultRandomAccessByteReader(ShortStreamReader(byteArrayOf(1, 2, 3, 4)))

        reader.moveTo(15)

        assertEquals(0, reader.readBytes(5).size)
    }

    /**
     * The content length of stream sources is only a hint that the
     * delegate may understate - a cloud provider can report size 0 for a
     * perfectly readable file. Reads must be decided by the delegate's
     * actual end of data, never by the hint.
     */
    @Test
    fun testUnderstatedContentLengthHintDoesNotGateReads() {

        val reader = DefaultRandomAccessByteReader(ZeroHintStreamReader(bytes))

        assertContentEquals(bytes, reader.readBytes(bytes.size))
        assertContentEquals(byteArrayOf(), reader.readBytes(1))

        reader.moveTo(5)

        assertContentEquals(byteArrayOf(6, 7, 8), reader.readBytes(3))

        /* Random access behind the hint must read the real data too. */
        assertContentEquals(byteArrayOf(9, 10), reader.readBytes(8, 2))

        /* The sequential position is unchanged by the random access read. */
        assertContentEquals(byteArrayOf(9, 10), reader.readBytes(2))

        assertNull(reader.readByte())
    }

    /**
     * A TIFF whose reader reports a zero content length must parse
     * completely: the whole TIFF chain wraps its sources in this reader,
     * so a false EOF here would render valid files unreadable.
     */
    @Test
    fun testTiffParseWithUnderstatedContentLengthHint() {

        val bytes = buildMinimalTiff()

        val contents = TiffReader.read(DefaultRandomAccessByteReader(ZeroHintStreamReader(bytes)))

        assertEquals(1, contents.directories.size)

        assertEquals("Kim", contents.directories[0].findField(TiffTag.TIFF_TAG_MAKE)?.value.toString())
    }

    /**
     * Builds a little endian classic TIFF: 8-byte header and an IFD0
     * with the inline Make tag and no successor.
     */
    private fun buildMinimalTiff(): ByteArray {

        val bytes = ByteArray(26)

        bytes[0] = 'I'.code.toByte()
        bytes[1] = 'I'.code.toByte()
        bytes[2] = 0x2A
        bytes[3] = 0

        bytes[4] = 8

        /* IFD0 at offset 8: entry count 1, one Make entry, no successor. */
        bytes[8] = 1

        /* Entry at 10: tag 10-11, type 12-13, count 14-17, value 18-21. */
        bytes[10] = 0x0F
        bytes[11] = 0x01

        bytes[12] = 2

        bytes[14] = 4

        "Kim\u0000".encodeToByteArray().copyInto(bytes, 18)

        return bytes
    }

    /**
     * A reader whose contentLength understates the actual data, like a
     * stream from a provider that reports an unknown file size as 0.
     */
    private class ZeroHintStreamReader(
        private val bytes: ByteArray
    ) : ByteReader {

        override val contentLength: Long = 0

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

    /**
     * A reader over content beyond the signed Int range cannot address
     * that content (the interface is Int-indexed). It must fail loudly
     * with a descriptive message instead of reading phantom EOFs or
     * wrapped-around ranges.
     */
    @Test
    fun testContentBeyondIntMaxValueIsRejected() {

        val beyondIntMax = Int.MAX_VALUE.toLong() + 1

        val delegate = object : ByteReader {
            override val contentLength: Long = beyondIntMax
            override fun readByte(): Byte? = null
            override fun readBytes(count: Int): ByteArray = ByteArray(0)
            override fun close() {
                /* Nothing to do. */
            }
        }

        val exception = assertFailsWith<ImageReadException> {
            DefaultRandomAccessByteReader(delegate)
        }

        assertTrue(exception.message?.contains("exceeds the supported maximum") == true)
    }
}
