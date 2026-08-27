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
 * Prepends the given list of [Byte]s and continues to
 * read from [ByteReader] after that. Can be used to
 * prepend the magic number again.
 *
 * Attention: [contentLength] reports the delegate's total size, which
 * already matches the logical length of this reader, because the
 * prepended bytes were read from the same delegate before. It must not
 * be increased by [prependedBytes], or bounds checks would over-report
 * the available bytes by exactly that count.
 */
internal class PrePendingByteReader(
    private val delegate: ByteReader,
    prependedBytes: List<Byte>
) : ByteReader {

    override val contentLength: Long = delegate.contentLength

    private val prependedBytesBuffer = ArrayDeque(prependedBytes)

    override fun readByte(): Byte? {

        if (prependedBytesBuffer.isNotEmpty())
            return prependedBytesBuffer.removeAt(0)

        return delegate.readByte()
    }

    override fun readBytes(count: Int): ByteArray {

        if (prependedBytesBuffer.isEmpty())
            return delegate.readBytes(count)

        val bytes = ByteArray(count)
        var bytesRead = 0

        /* Read prepended bytes first */
        while (bytesRead < count && prependedBytesBuffer.isNotEmpty()) {

            bytes[bytesRead] = prependedBytesBuffer.removeAt(0)

            bytesRead++
        }

        /* Read remaining bytes from the delegate */
        val delegateBytes = delegate.readBytes(count - bytesRead)

        delegateBytes.copyInto(bytes, bytesRead)

        /*
         * Return a short array when the delegate is exhausted, matching the
         * ByteReader contract. Zero padding would be parsed as data.
         */
        return bytes.copyOf(bytesRead + delegateBytes.size)
    }

    override fun close() {
        delegate.close()
    }
}
