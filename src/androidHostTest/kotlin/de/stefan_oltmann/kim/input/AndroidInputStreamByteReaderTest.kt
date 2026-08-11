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

import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
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
     * Regression test: partial reads must return only the bytes actually
     * read, without zero padding.
     */
    @Test
    fun testReadBytesReturnsShortArrayOnPartialReads() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val reader = AndroidInputStreamByteReader(
            inputStream = PartialReadInputStream(bytes),
            contentLength = bytes.size.toLong()
        )

        /* The stream delivers only 3 bytes per call. */
        val expectedChunks = bytes.toList().chunked(PARTIAL_READ_CHUNK_SIZE)

        for (chunk in expectedChunks)
            assertEquals(chunk, reader.readBytes(READ_REQUEST_SIZE).toList())

        /* The stream is exhausted now. */
        assertEquals(0, reader.readBytes(READ_REQUEST_SIZE).size)
        assertNull(reader.readByte())
    }

    private companion object {

        /* The stub delivers at most this many bytes per read call */
        const val PARTIAL_READ_CHUNK_SIZE = 3

        /* How many bytes the reader is asked to read in the test */
        const val READ_REQUEST_SIZE = 100
    }
}
