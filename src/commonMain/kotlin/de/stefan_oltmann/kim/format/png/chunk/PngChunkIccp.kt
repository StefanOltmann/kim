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
package de.stefan_oltmann.kim.format.png.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.decompressBytes
import de.stefan_oltmann.kim.common.indexOfNullTerminator
import de.stefan_oltmann.kim.format.png.PngChunkType
import de.stefan_oltmann.kim.format.png.PngConstants

/**
 * The iCCP chunk of a PNG file: a Latin-1 keyword, the compression
 * method byte and the zlib-compressed ICC color profile behind it.
 */
public class PngChunkIccp(
    bytes: ByteArray,
    crc: Int
) : PngChunk(PngChunkType.ICCP, bytes, crc) {

    /** The profile name, like "ICC profile". */
    public val keyword: String

    /** The decompressed ICC color profile bytes. */
    public val profileBytes: ByteArray

    init {

        val keywordTerminator = bytes.indexOfNullTerminator()

        if (keywordTerminator < 0)
            throw ImageReadException("PNG iCCP chunk keyword is not terminated.")

        keyword = bytes.copyOfRange(0, keywordTerminator)
            .decodeLatin1BytesToString()

        val compressionMethodIndex = keywordTerminator + 1

        if (compressionMethodIndex >= bytes.size)
            throw ImageReadException("PNG iCCP chunk is too short for the compression method.")

        if (bytes[compressionMethodIndex].toInt() != PngConstants.COMPRESSION_DEFLATE_INFLATE)
            throw ImageReadException(
                "PNG iCCP chunk has unexpected compression method: " +
                    bytes[compressionMethodIndex].toInt()
            )

        profileBytes = decompressBytes(
            bytes.copyOfRange(compressionMethodIndex + 1, bytes.size)
        )
    }

}
