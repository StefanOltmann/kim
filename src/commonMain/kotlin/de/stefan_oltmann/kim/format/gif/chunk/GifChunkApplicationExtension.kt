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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.gif.GifChunkType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readByte
import de.stefan_oltmann.kim.input.readBytes

/**
 * An application extension chunk of a GIF file.
 */
public class GifChunkApplicationExtension(
    header: ByteArray,
    private val subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.APPLICATION_EXTENSION,
    subChunks
        .fold(header) { acc, subChunk ->
            acc + subChunk
        }
        .plus(0x00)
) {

    private val xmpMetaTag: String = "x:xmpmeta"

    public val applicationIdentifier: String
    public val applicationCode: String

    init {

        if (subChunks.isEmpty())
            throw ImageReadException("Application extension must have at least 1 subchunk.")

        val firstSubChunkByteReader = ByteArrayByteReader(subChunks.first())

        /*
         * The size is an unsigned byte, so legal sub chunk sizes of 128
         * to 255 must not become negative through sign extension.
         */
        val firstSubChunkSize = firstSubChunkByteReader.readByte("first sub chunk size").toUInt8()

        if (firstSubChunkSize < APPLICATION_IDENTIFIER_LENGTH)
            throw ImageReadException(
                "Invalid size for initial application extension sub chunk: $firstSubChunkSize bytes," +
                    " expected at least 8 bytes (typically 11)."
            )

        applicationIdentifier = firstSubChunkByteReader.readBytes(
            fieldName = "application identifier",
            count = APPLICATION_IDENTIFIER_LENGTH
        ).decodeToString()

        applicationCode = firstSubChunkByteReader.readBytes(
            fieldName = "application code",
            count = firstSubChunkSize - APPLICATION_IDENTIFIER_LENGTH
        ).decodeToString()
    }

    public fun parseAsXmpOrThrow(): String {

        val extensionContentAsString = try {

            /*
             * The XMP payload is spread over size-prefixed sub-blocks.
             * Strip the size bytes and search the payload.
             * Fall back to the raw bytes for files written without
             * sub-block framing, where the size bytes are part of the data.
             */
            val unpackedContent = subChunks
                .map { subChunk -> subChunk.copyOfRange(1, subChunk.size).decodeToString() }
                .joinToString("")

            if (unpackedContent.contains("<x:xmpmeta"))
                unpackedContent
            else
                bytes.decodeToString()

        } catch (ex: CharacterCodingException) {
            throw ImageReadException("Failed to decode application extension bytes as string.", ex)
        }

        if (!extensionContentAsString.contains("<x:xmpmeta"))
            throw ImageReadException("No XMP data found in application extension.")

        return "<$xmpMetaTag" + extensionContentAsString
            .substringAfter("<$xmpMetaTag")
            .substringBefore("</$xmpMetaTag>")
            .plus("</$xmpMetaTag>")
    }

    private companion object {

        /* The application identifier is 8 bytes */
        const val APPLICATION_IDENTIFIER_LENGTH = 8
    }
}
