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
package de.stefan_oltmann.kim.format.png.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MAX_DECOMPRESSED_BYTE_COUNT
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.decompressBytes
import de.stefan_oltmann.kim.common.indexOfNullTerminator
import de.stefan_oltmann.kim.format.png.PngChunkType
import de.stefan_oltmann.kim.format.png.PngConstants

/**
 * The compressed text chunk of a PNG file.
 *
 * Instances are built by [of].
 */
public class PngChunkZtxt private constructor(
    bytes: ByteArray,
    crc: Int,
    @kotlin.jvm.JvmField
    public val keyword: String,
    @kotlin.jvm.JvmField
    public val text: String,
    decompressedByteCount: Int
) : PngTextChunk(PngChunkType.ZTXT, bytes, crc) {

    override val decompressedByteCount: Int = decompressedByteCount

    /**
     * @return Returns the keyword.
     */
    override fun getKeyword(): String =
        keyword

    /**
     * @return Returns the text.
     */
    override fun getText(): String =
        text

    public companion object {

        /**
         * Builds the chunk from its payload: the keyword is decoded as
         * Latin-1 up to its NUL terminator, the compression method must
         * be deflate/inflate, and the text decompresses to at most
         * [maxDecompressedByteCount] bytes, decoded as Latin-1.
         */
        internal fun of(
            bytes: ByteArray,
            crc: Int,
            maxDecompressedByteCount: Int = MAX_DECOMPRESSED_BYTE_COUNT
        ): PngChunkZtxt {

            var index = bytes.indexOfNullTerminator()

            if (index < 0)
                throw ImageReadException("PNG zTXt chunk keyword is not terminated.")

            val keyword = bytes.copyOfRange(
                fromIndex = 0,
                toIndex = index
            ).decodeLatin1BytesToString()

            index++

            /*
             * A chunk that ends directly behind the keyword terminator has
             * no compression method and must be rejected cleanly.
             */
            if (index >= bytes.size)
                throw ImageReadException("PNG zTXt chunk is too short for the compression method.")

            val compressionMethod = bytes[index++].toInt()

            if (compressionMethod != PngConstants.COMPRESSION_DEFLATE_INFLATE)
                throw ImageReadException("PNG zTXt chunk has unexpected compression method: $compressionMethod")

            val compressedText = bytes.copyOfRange(index, bytes.size)

            /* The PNG specification defines zTXt text as Latin-1 - like the keyword. */
            val decompressedText = decompressBytes(compressedText, maxDecompressedByteCount)

            return PngChunkZtxt(
                bytes = bytes,
                crc = crc,
                keyword = keyword,
                text = decompressedText.decodeLatin1BytesToString(),
                decompressedByteCount = decompressedText.size
            )
        }
    }
}
