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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * A byte reader that retains a copy of every consumed byte, so
 * already-read regions stay accessible on forward-only streams.
 *
 * Boxes parsed from it must not additionally buffer large payloads
 * themselves, or the data ends up in memory twice.
 */
internal class CopyByteReader(
    val byteReader: ByteReader
) : ByteReader {

    private val byteWriter = ByteArrayByteWriter()

    override val contentLength: Long =
        byteReader.contentLength

    override val isRetaining: Boolean = true

    fun getBytes(): ByteArray =
        byteWriter.toByteArray()

    /**
     * A reader over the bytes retained so far, without copying the whole
     * buffer a second time. Used to reposition a forward-only parse on an
     * already buffered prefix, such as the Samsung layout reposition.
     */
    fun replayByteReader(): ByteReader =
        CopyByteReaderReplay(byteWriter)

    override fun readByte(): Byte? {

        val byte = byteReader.readByte() ?: return null

        byteWriter.write(byteArrayOf(byte))

        return byte
    }

    override fun readBytes(count: Int): ByteArray {

        val bytes = byteReader.readBytes(count)

        byteWriter.write(bytes)

        return bytes
    }

    override fun close() =
        byteReader.close()
}
