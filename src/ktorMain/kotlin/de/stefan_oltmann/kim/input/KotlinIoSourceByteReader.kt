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

import de.stefan_oltmann.kim.common.exists
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * A ByteReader that reads from a kotlinx.io Source.
 */
public class KotlinIoSourceByteReader(
    private val source: Source,
    override val contentLength: Long
) : ByteReader {

    private var position: Long = 0

    override fun readByte(): Byte? {

        if (source.exhausted())
            return null

        position++

        return source.readByte()
    }

    override fun readBytes(count: Int): ByteArray {

        require(count >= 0) { "Count must not be negative: $count" }

        /*
         * Byte-at-a-time, terminating at the real source EOF: the
         * contentLength is a caller-provided hint that must never gate
         * the reads (a hint of 0 would report "no data" for a valid
         * source, an overstated hint would make exact-count reads throw).
         * Metadata chunks are small, so the per-byte loop cost is
         * negligible; image data is streamed via transferExactly.
         */
        val result = ByteArray(count)

        var filled = 0

        while (filled < count) {

            if (source.exhausted())
                break

            result[filled++] = source.readByte()
        }

        position += filled

        return if (filled == count) result else result.copyOf(filled)
    }

    override fun close(): Unit =
        source.close()

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun <T> read(path: Path, block: (ByteReader?) -> T): T {

            if (!path.exists())
                return block(null)

            val metadata = SystemFileSystem.metadataOrNull(path)

            if (metadata == null || !metadata.isRegularFile)
                return block(null)

            return SystemFileSystem.source(path).buffered().use { source ->
                block(KotlinIoSourceByteReader(source, metadata.size))
            }
        }
    }
}
