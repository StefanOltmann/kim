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

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/*
 * The test is placed in jvmTest, because the reader lives in ktorMain
 * which is not visible to commonTest.
 */
class KtorByteReadChannelByteReaderTest {

    /**
     * Regression test: reads beyond the end of the channel must return a
     * short array instead of zero-padded data.
     */
    @Test
    fun testReadBytesReturnsShortArrayAtEndOfChannel() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val reader = KtorByteReadChannelByteReader(
            channel = ByteReadChannel(bytes),
            contentLength = bytes.size.toLong()
        )

        assertEquals(
            expected = bytes.copyOfRange(0, 6).toList(),
            actual = reader.readBytes(6).toList()
        )

        /* The second read crosses the end of the channel. */
        assertEquals(
            expected = bytes.copyOfRange(6, 10).toList(),
            actual = reader.readBytes(100).toList()
        )

        /* The channel is exhausted now. */
        assertEquals(0, reader.readBytes(100).size)
        assertNull(reader.readByte())
    }

    /**
     * Regression test: readRemainingBytes must not append zeros for the
     * unfilled final chunk.
     */
    @Test
    fun testReadRemainingBytesReturnsExactChannelContent() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val reader = KtorByteReadChannelByteReader(
            channel = ByteReadChannel(bytes),
            contentLength = bytes.size.toLong()
        )

        assertEquals(
            expected = bytes.toList(),
            actual = reader.readRemainingBytes().toList()
        )
    }

    /**
     * Regression test: a channel that closes with no data must be treated as
     * end-of-data. readByte returns null instead of indexing an empty buffer,
     * and readBytes returns an empty array instead of looping forever.
     */
    @Test
    fun testEmptyChannelIsTreatedAsEndOfData() {

        val reader = KtorByteReadChannelByteReader(
            channel = ByteReadChannel(ByteArray(0)),
            contentLength = 0
        )

        assertNull(reader.readByte())
        assertEquals(0, reader.readBytes(100).size)
        assertNull(reader.readByte())
    }

    /**
     * A zero hint means "size unknown" and must read as unbounded like
     * on the JVM facade - passed through raw, the JPEG header budget
     * check would treat every position as exhausted.
     */
    @Test
    fun testZeroHintIsReportedAsUnbounded() {

        val reader = KtorByteReadChannelByteReader(
            channel = ByteReadChannel(byteArrayOf(1, 2, 3)),
            contentLength = 0
        )

        assertEquals(Long.MAX_VALUE, reader.contentLength)
    }

    /**
     * Regression test: bytes the channel already delivered must be
     * readable without waiting for the full buffer size or the channel
     * close - a bidirectional protocol that wrote a header and keeps
     * the channel open must not stall the very first read.
     */
    @Test
    fun testReadReturnsAvailableBytesWithoutWaitingForTheBufferMax() {

        val bytes = byteArrayOf(1, 2, 3, 4)

        /* autoFlush makes the written bytes immediately readable. */
        val channel = ByteChannel(autoFlush = true)

        /* The channel delivers the bytes but never closes. */
        runBlocking {
            channel.writeFully(bytes)
        }

        val reader = KtorByteReadChannelByteReader(
            channel = channel,
            contentLength = bytes.size.toLong()
        )

        val executor = Executors.newSingleThreadExecutor()

        try {
            val future = executor.submit(Callable { reader.readBytes(bytes.size) })

            /*
             * The old refill parked until 32 KB arrived or the channel
             * closed, so this future only completed on timeout.
             */
            assertEquals(
                expected = bytes.toList(),
                actual = future.get(5, TimeUnit.SECONDS).toList()
            )
        } finally {
            channel.cancel(null)
            executor.shutdownNow()
        }
    }
}
