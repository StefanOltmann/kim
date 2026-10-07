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
package de.stefan_oltmann.kim.common

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/* Copies stream in bounded chunks, like the file readers beside them. */
private const val COPY_CHUNK_BYTES: Int = 64 * 1024

public fun Path.copyTo(destination: Path) {

    require(exists()) { "$this does not exist." }

    val metadata = SystemFileSystem.metadataOrNull(this)

    requireNotNull(metadata) { "Failed to read metadata of $this" }
    require(metadata.isRegularFile) { "Source $this must be a regular file." }

    SystemFileSystem.source(this).buffered().use { rawSource ->
        SystemFileSystem.sink(destination).buffered().use { sink ->

            /*
             * Copy to the real end of data instead of the stat snapshot:
             * the reported size is a snapshot that concurrent growth
             * invalidates, and a copy bounded by it would end "successfully"
             * with the file tail silently missing.
             */
            val chunk = ByteArray(COPY_CHUNK_BYTES)

            while (true) {

                val readByteCount = rawSource.readAtMostTo(chunk, 0, chunk.size)

                if (readByteCount == -1)
                    break

                sink.write(chunk, startIndex = 0, endIndex = readByteCount)
            }
        }
    }
}

public fun Path.writeBytes(byteArray: ByteArray): Unit =
    SystemFileSystem
        .sink(this)
        .buffered()
        .use { it.write(byteArray) }

public fun Path.readBytes(): ByteArray =
    SystemFileSystem
        .source(this)
        .buffered()
        .use { it.readByteArray() }

public fun Path.exists(): Boolean =
    SystemFileSystem.exists(this)
