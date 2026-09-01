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
package de.stefan_oltmann.kim.input

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray

/**
 * This class allows streaming data from a cloud service and read only
 * the headers.
 *
 * The synchronous [ByteReader] methods bridge to the suspending
 * [ByteReadChannel] through [runBlocking], so every buffer refill
 * blocks the calling thread until the channel delivers the next chunk.
 *
 * Attention: Never read from the dispatcher or thread that produces
 * the channel data - for example a Ktor server handler reading
 * `call.request.receiveChannel()`. `runBlocking` parks that thread
 * while the channel still needs it to run its read continuation,
 * which deadlocks the call. Invoke the reader from another dispatcher
 * or a background thread instead.
 *
 * Attention: Instances are not thread-safe. The buffer state is
 * unsynchronized by design and must be confined to a single thread.
 */
public class KtorByteReadChannelByteReader(
    private val channel: ByteReadChannel,
    override val contentLength: Long,
    private val bufferSize: Long = DEFAULT_BUFFER_SIZE
) : ByteReader {

    private var buffer: ByteArray = byteArrayOf()
    private var bufferOffset = 0
    private var bufferLimit = 0

    override fun readByte(): Byte? {

        if (bufferOffset >= bufferLimit) {

            if (channel.isClosedForRead)
                return null

            buffer = runBlocking {
                channel.readRemaining(max = bufferSize).readByteArray()
            }
            bufferLimit = buffer.size
            bufferOffset = 0

            /*
             * A zero-byte refill means the channel delivered no data even
             * though it was not flagged closed before the read (premature
             * server close or a race). Treat that as end-of-data instead of
             * indexing an empty buffer.
             */
            if (bufferLimit == 0)
                return null
        }

        return buffer[bufferOffset++]
    }

    override fun readBytes(count: Int): ByteArray {

        val result = ByteArray(count)
        var remaining = count
        var offset = 0

        while (remaining > 0) {

            if (bufferOffset >= bufferLimit) {

                if (channel.isClosedForRead)
                    break

                buffer = runBlocking {
                    channel.readRemaining(max = bufferSize).readByteArray()
                }
                bufferLimit = buffer.size
                bufferOffset = 0

                /*
                 * A zero-byte refill means the channel delivered no data
                 * despite not being flagged closed before the read (premature
                 * server close or a race). Treat that as end-of-data instead
                 * of looping forever on an empty buffer.
                 */
                if (bufferLimit == 0)
                    break
            }

            val bytesToCopy = minOf(remaining, bufferLimit - bufferOffset)

            buffer.copyInto(result, offset, bufferOffset, bufferOffset + bytesToCopy)

            offset += bytesToCopy
            bufferOffset += bytesToCopy
            remaining -= bytesToCopy
        }

        /*
         * Return a short array when the channel is exhausted, matching the
         * ByteReader contract. Zero padding would be parsed as data.
         */
        return result.copyOf(offset)
    }

    override fun close() {
        runBlocking {
            channel.cancel()
        }
    }

    public companion object {
        private const val DEFAULT_BUFFER_SIZE: Long = 32 * 1024
    }
}
