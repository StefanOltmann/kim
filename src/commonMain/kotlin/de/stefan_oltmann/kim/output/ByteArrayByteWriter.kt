/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.output

import de.stefan_oltmann.kim.input.DEFAULT_BUFFER_SIZE

/**
 * A ByteWriter that writes into an in-memory byte array.
 */
public class ByteArrayByteWriter : ByteWriter {

    private var bytes: ByteArray = ByteArray(DEFAULT_BUFFER_SIZE)

    private var position: Int = 0

    /**
     * The number of bytes written so far. Together with [getBytesAt] this
     * lets a caller replay the buffer without copying it as a whole.
     */
    internal val writtenByteCount: Int
        get() = position

    override fun write(byte: Byte) {
        ensureCapacity(1)
        bytes[position++] = byte
    }

    override fun write(byteArray: ByteArray) {
        ensureCapacity(byteArray.size)
        byteArray.copyInto(bytes, position)
        position += byteArray.size
    }

    override fun close() {
        /* Nothing to do */
    }

    override fun flush() {
        /* Nothing to do */
    }

    public fun toByteArray(): ByteArray = bytes.copyOfRange(0, position)

    /**
     * Returns up to [count] bytes starting at [offset], copying only the
     * requested range instead of the whole buffer.
     */
    internal fun getBytesAt(offset: Int, count: Int): ByteArray {

        require(offset >= 0) { "Offset must not be negative: $offset" }
        require(count >= 0) { "Count must not be negative: $count" }

        if (offset >= position)
            return byteArrayOf()

        val toIndex = minOf(offset.toLong() + count, position.toLong()).toInt()

        return bytes.copyOfRange(offset, toIndex)
    }

    private fun ensureCapacity(requiredCapacity: Int) {

        val currentCapacity = bytes.size

        if (position + requiredCapacity > currentCapacity) {

            val newCapacity = (currentCapacity + requiredCapacity) * 2
            val newBytes = ByteArray(newCapacity)

            bytes.copyInto(newBytes)
            bytes = newBytes
        }
    }
}
