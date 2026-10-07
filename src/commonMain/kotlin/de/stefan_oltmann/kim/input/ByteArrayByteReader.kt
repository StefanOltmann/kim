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

/**
 * ByteArray backed ByteReader
 *
 * This is intended to be used for EXIF, because this is at max 64 kb in size.
 *
 * Note that huge files in production shouldn't be loaded into a ByteArray.
 * For unit the purpose of unit tests this is acceptable.
 */
public class ByteArrayByteReader(
    private val bytes: ByteArray
) : RandomAccessByteReader {

    private var windowStartField: Int = 0

    private var windowEndField: Int = bytes.size

    private var currentPosition: Int = 0

    override val contentLength: Long
        get() = (windowEndField - windowStartField).toLong()

    internal val windowArray: ByteArray
        get() = bytes

    internal val windowPosition: Int
        get() = currentPosition

    internal val windowEnd: Int
        get() = windowEndField

    /**
     * Internal constructor that reads through a window into the given
     * array: positions are relative to the window, so the children of a
     * container box can parse from the parent's buffer without copying
     * the payload a second time.
     */
    internal constructor(
        bytes: ByteArray,
        windowStart: Int,
        windowEnd: Int
    ) : this(bytes) {
        require(windowStart in 0..windowEnd) {
            "Invalid window: [$windowStart, $windowEnd)."
        }

        require(windowEnd <= bytes.size) {
            "Window end $windowEnd exceeds the array of ${bytes.size} bytes."
        }

        this.windowStartField = windowStart
        this.windowEndField = windowEnd
        this.currentPosition = windowStartField
    }

    internal fun moveWindowPositionTo(index: Int) {

        require(index in windowStartField..windowEndField) {
            "Can't move to $index outside the window [$windowStartField, $windowEndField]."
        }

        currentPosition = index
    }

    override fun readByte(): Byte? {

        if (currentPosition == windowEndField)
            return null

        return bytes[currentPosition++]
    }

    override fun readBytes(count: Int): ByteArray {
        require(count >= 0) { "Count must not be negative: $count" }

        val result = copyClamped(
            source = bytes,
            fromIndex = currentPosition.toLong(),
            count = count.toLong(),
            limit = windowEndField.toLong()
        )

        currentPosition += result.size

        return result
    }

    override fun moveTo(position: Int) {

        require(position in 0..contentLength) {
            "Can't move to $position in content of $contentLength bytes."
        }

        this.currentPosition = windowStartField + position
    }

    override fun readBytes(offset: Int, length: Int): ByteArray {

        require(offset >= 0) { "Offset must be positive: $offset" }
        require(length > 0) { "Length must be positive: $length" }

        return copyClamped(
            source = bytes,
            fromIndex = (windowStartField + offset).toLong(),
            count = length.toLong(),
            limit = windowEndField.toLong()
        )
    }

    override fun close() {
        /* Does nothing. */
    }

}
