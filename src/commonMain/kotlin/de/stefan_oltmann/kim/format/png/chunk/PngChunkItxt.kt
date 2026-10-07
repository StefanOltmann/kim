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
import de.stefan_oltmann.kim.common.decodeStrictUtf8
import de.stefan_oltmann.kim.common.decompressBytes
import de.stefan_oltmann.kim.common.indexOfNullTerminator
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.format.png.PngChunkType
import de.stefan_oltmann.kim.format.png.PngConstants

/**
 * The UTF-8 text chunk of a PNG file.
 */
public class PngChunkItxt(
    bytes: ByteArray,
    crc: Int
) : PngTextChunk(PngChunkType.ITXT, bytes, crc) {

    /** The field name of the key/value pair, like "Comment". */
    @kotlin.jvm.JvmField
    public val keyword: String

    /** The value of the key/value pair, decoded as UTF-8. */
    @kotlin.jvm.JvmField
    public var text: String

    /** The language of the text, like "de" - empty when unspecified. */
    public val languageTag: String

    /** The keyword translated into the language of the text. */
    public val translatedKeyword: String

    init {

        var terminatorIndex = bytes.indexOfNullTerminator()

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk keyword is not terminated.")

        keyword = bytes.slice(
            startIndex = 0,
            count = terminatorIndex
        ).decodeLatin1BytesToString()

        var index = terminatorIndex + 1

        /*
         * A chunk that ends directly behind the keyword terminator has
         * no compression fields and must be rejected cleanly.
         */
        if (index + COMPRESSION_FIELDS_LENGTH > bytes.size)
            throw ImageReadException("PNG iTXt chunk is too short for the compression fields.")

        val compressionFlag = bytes[index++].toInt()

        if (compressionFlag != 0 && compressionFlag != 1)
            throw ImageReadException("PNG iTXt chunk has invalid compression flag: $compressionFlag")

        val compressed = compressionFlag == 1

        val compressionMethod = bytes[index++].toInt()

        if (compressed && compressionMethod != PngConstants.COMPRESSION_DEFLATE_INFLATE)
            throw ImageReadException("PNG iTXt chunk has unexpected compression method: $compressionMethod")

        terminatorIndex = bytes.indexOfNullTerminator(index)

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk language tag is not terminated.")

        languageTag = bytes.copyOfRange(
            fromIndex = index,
            toIndex = terminatorIndex
        ).decodeLatin1BytesToString()

        index = terminatorIndex + 1

        terminatorIndex = bytes.indexOfNullTerminator(index)

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk translated keyword is not terminated.")

        translatedKeyword = bytes.copyOfRange(
            fromIndex = index,
            toIndex = terminatorIndex
        ).decodeStrictUtf8("The PNG iTXt chunk translated keyword")

        index = terminatorIndex + 1

        val subBytes = bytes.copyOfRange(
            fromIndex = index,
            toIndex = bytes.size
        )

        text = if (compressed)
            decompressBytes(subBytes)
                .decodeStrictUtf8("The PNG iTXt chunk text")
        else
            subBytes.decodeStrictUtf8("The PNG iTXt chunk text")
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

    private companion object {

        /* The compression flag and the compression method. */
        const val COMPRESSION_FIELDS_LENGTH: Int = 2
    }
}
