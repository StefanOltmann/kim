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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * A sequential reader over the bytes a [CopyByteReader] has retained so
 * far.
 *
 * The replay serves every read out of the existing buffer, copying only
 * the requested range, so repositioning the Samsung layout parse does not
 * allocate a second full copy of the file.
 */
internal class CopyByteReaderReplay(
    private val retainedBytes: ByteArrayByteWriter
) : ByteReader {

    private var position: Int = 0

    override val contentLength: Long =
        retainedBytes.writtenByteCount.toLong()

    override fun readByte(): Byte? {

        val bytes = readBytes(1)

        return bytes.firstOrNull()
    }

    override fun readBytes(count: Int): ByteArray {

        val result = retainedBytes.getBytesAt(position, count)

        position += result.size

        return result
    }

    override fun close() {
        /* Nothing to do. The retained buffer outlives this replay. */
    }
}
