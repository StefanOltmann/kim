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
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests the AndroidInputStreamByteReader with partial reads.
 */
class AndroidInputStreamByteReaderTest {

    /**
     * InputStream stub that returns partial reads, like Android
     * ContentResolver streams commonly do.
     */
    private class PartialReadInputStream(
        private val bytes: ByteArray
    ) : InputStream() {

        private var position = 0

        override fun read(): Int {

            if (position >= bytes.size)
                return -1

            return bytes[position++].toUByte().toInt()
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {

            if (position >= bytes.size)
                return -1

            /* Deliver at most PARTIAL_READ_CHUNK_SIZE bytes per call. */
            val count = minOf(PARTIAL_READ_CHUNK_SIZE, len, bytes.size - position)

            System.arraycopy(bytes, position, b, off, count)

            position += count

            return count
        }
    }

    /**
     * Regression test: partial reads must be combined into one result,
     * so callers never see short arrays before the end of the stream.
     */
    @Test
    fun testReadBytesCombinesPartialReads() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val reader = AndroidInputStreamByteReader(
            inputStream = PartialReadInputStream(bytes),
            contentLength = bytes.size.toLong()
        )

        /* The stream delivers only 3 bytes per call, but the reader must loop. */
        assertEquals(bytes.toList(), reader.readBytesLegacy(READ_REQUEST_SIZE).toList())

        /* The stream is exhausted now. */
        assertEquals(0, reader.readBytesLegacy(READ_REQUEST_SIZE).size)
        assertNull(reader.readByte())
    }

    /**
     * Regression test: a checked read across several partial reads
     * must not fail with a spurious ImageReadException.
     */
    @Test
    fun testCheckedReadAcrossPartialReads() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val reader = AndroidInputStreamByteReader(
            inputStream = PartialReadInputStream(bytes),
            contentLength = bytes.size.toLong()
        )

        val readBytes = reader.readBytes("test data", bytes.size)

        assertEquals(bytes.toList(), readBytes.toList())
    }

    /**
     * A checked read beyond the end of the stream must fail.
     */
    @Test
    fun testCheckedReadBeyondEndOfStream() {

        val reader = AndroidInputStreamByteReader(
            inputStream = PartialReadInputStream(byteArrayOf(1, 2, 3)),
            contentLength = 3
        )

        assertFailsWith<ImageReadException> {
            reader.readBytes("test data", READ_REQUEST_SIZE)
        }
    }

    /**
     * readByte must return null at the end of the stream.
     */
    @Test
    fun testReadByte() {

        val reader = AndroidInputStreamByteReader(
            inputStream = PartialReadInputStream(byteArrayOf(42)),
            contentLength = 1
        )

        assertEquals(42.toByte(), reader.readByte())
        assertNull(reader.readByte())
    }

    /**
     * close() must close the underlying stream.
     */
    @Test
    fun testClose() {

        var closed = false

        val stream = object : InputStream() {

            override fun read(): Int = -1

            override fun close() {
                closed = true
            }
        }

        AndroidInputStreamByteReader(stream, 0).close()

        assertEquals(true, closed)
    }

    private companion object {

        /* The stub delivers at most this many bytes per read call */
        const val PARTIAL_READ_CHUNK_SIZE = 3

        /* How many bytes the reader is asked to read in the test */
        const val READ_REQUEST_SIZE = 100
    }
}
