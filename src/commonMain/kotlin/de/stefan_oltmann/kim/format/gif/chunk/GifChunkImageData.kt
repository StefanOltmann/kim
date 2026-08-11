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

import de.stefan_oltmann.kim.format.gif.GifChunkType

/**
 * The LZW-compressed image data chunk of a GIF file.
 */
public class GifChunkImageData(
    lzwMinimumCodeSize: Byte,
    subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.IMAGE_DATA,
    subChunks
        .fold(byteArrayOf(lzwMinimumCodeSize)) { acc, subChunk ->
            acc + subChunk
        }
        .plus(0x00)
)
