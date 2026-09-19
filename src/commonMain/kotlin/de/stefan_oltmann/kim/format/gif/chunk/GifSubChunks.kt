/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif.chunk

import de.stefan_oltmann.kim.format.gif.GifConstants

/**
 * Joins the payload bytes of a GIF chunk: the leading header bytes, the
 * size prefixed sub-blocks behind it and the block terminator that ends
 * the chunk.
 *
 * The bytes are copied into a buffer of the final size, so large image
 * data is not copied once per sub-block the way repeated concatenation
 * would do.
 */
internal fun joinGifSubChunks(
    header: ByteArray,
    subChunks: List<ByteArray>
): ByteArray {

    val bytes = ByteArray(header.size + subChunks.sumOf { it.size } + 1)

    header.copyInto(bytes)

    var position = header.size

    for (subChunk in subChunks) {

        subChunk.copyInto(bytes, position)

        position += subChunk.size
    }

    bytes[bytes.lastIndex] = GifConstants.BLOCK_TERMINATOR

    return bytes
}
