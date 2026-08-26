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

import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import kotlin.math.max

/**
 * This class buffers the reading from the original ByteReader and
 * provides random access needed for parsing TIFF files.
 */
public class DefaultRandomAccessByteReader(
    public val byteReader: ByteReader
) : RandomAccessByteReader {

    override val contentLength: Long =
        byteReader.contentLength

    private var currentPosition: Int = 0

    /*
     * The number of valid bytes in the buffer. Reads past this position
     * return short arrays, so no zero-filled garbage is served.
     */
    private var bufferPosition: Int = 0

    private var buffer = ByteArray(0)

    override fun readByte(): Byte? {

        if (currentPosition >= contentLength)
            return null

        val endIndex = currentPosition + 1

        if (endIndex > bufferPosition)
            readToIndex(endIndex)

        if (currentPosition >= bufferPosition)
            return null

        return buffer[currentPosition++]
    }

    override fun readBytes(count: Int): ByteArray {

        if (currentPosition >= contentLength)
            return byteArrayOf()

        /*
         * Computed in Long space, so a content length beyond the signed
         * Int range cannot wrap the coercion into a negative value that
         * would crash copyOfRange below. Mirrors readBytes(offset, length).
         */
        val clampedCount = minOf(count.toLong(), contentLength).toInt()

        val endIndex = currentPosition + clampedCount

        if (endIndex > bufferPosition)
            readToIndex(endIndex)

        val bytes = buffer.copyOfRange(currentPosition, minOf(endIndex, bufferPosition))

        currentPosition += bytes.size

        return bytes
    }

    override fun moveTo(position: Int) {

        /*
         * Positioning exactly at the content end is allowed - like in
         * ByteArrayByteReader - so reads from there return short or
         * empty arrays instead of failing. This also keeps moveTo(0)
         * working for empty content.
         */
        require(position in 0..contentLength) {
            "Can't move to $position in content of $contentLength bytes."
        }

        this.currentPosition = position
    }

    override fun readBytes(offset: Int, length: Int): ByteArray {

        /*
         * Hostile files can resolve offsets or sizes beyond the signed
         * Int range, which must fail cleanly instead of crashing inside
         * copyOfRange with a wrapped-around index.
         */
        require(offset >= 0) { "Offset must not be negative: $offset" }

        require(length > 0) { "Length must be positive: $length" }

        if (offset.toLong() >= contentLength)
            return byteArrayOf()

        /*
         * Computed in Long space, so a hostile size cannot overflow the
         * addition back into a small or negative end index.
         */
        val endIndex = minOf(offset.toLong() + length, contentLength).toInt()

        if (endIndex > bufferPosition)
            readToIndex(endIndex)

        if (offset >= bufferPosition)
            return byteArrayOf()

        return buffer.copyOfRange(offset, minOf(endIndex, bufferPosition))
    }

    override fun close(): Unit {

        /* Free the buffered file prefix as soon as possible. */
        buffer = ByteArray(0)

        byteReader.close()
    }

    private fun readToIndex(index: Int) {

        /*
         * Check if the need to expand the buffer first.
         */
        if (index > buffer.size) {

            /*
             * Copying an array is expensive. So we want at least expand
             * it with a defined BUFFER_EXPANSION and never with just one byte.
             */

            val newBufferSize =
                max(index, buffer.size + BUFFER_EXPANSION)

            buffer = buffer.copyOf(newBufferSize)
        }

        val missingBytesCount = index - bufferPosition

        val bytes = byteReader.readBytes(missingBytesCount)

        for (i in bytes.indices)
            buffer[bufferPosition + i] = bytes[i]

        /*
         * Only advance by the bytes that were actually read. At the end of
         * the stream this keeps the buffer short, so later reads return
         * short arrays instead of zero-filled data.
         */
        bufferPosition += bytes.size
    }

    private companion object {

        /*
         * We read the file in chunks of the usual EXIF size (65kb).
         * This is to minimize array copy operations.
         */
        private const val BUFFER_EXPANSION = JpegConstants.MAX_SEGMENT_SIZE
    }
}
