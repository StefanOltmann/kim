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
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.decompress
import de.stefan_oltmann.kim.common.indexOfNullTerminator
import de.stefan_oltmann.kim.format.png.PngChunkType
import de.stefan_oltmann.kim.format.png.PngConstants

/**
 * The compressed text chunk of a PNG file.
 */
public class PngChunkZtxt(
    bytes: ByteArray,
    crc: Int
) : PngTextChunk(PngChunkType.ZTXT, bytes, crc) {

    @kotlin.jvm.JvmField
    public val keyword: String

    @kotlin.jvm.JvmField
    public val text: String

    init {

        var index = bytes.indexOfNullTerminator()

        if (index < 0)
            throw ImageReadException("PNG zTXt chunk keyword is not terminated.")

        keyword = bytes.copyOfRange(
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

        text = decompress(compressedText)
    }

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
}
