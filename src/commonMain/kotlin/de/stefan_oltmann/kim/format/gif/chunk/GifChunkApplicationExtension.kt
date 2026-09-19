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
import de.stefan_oltmann.kim.format.gif.GifConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readBytes

/**
 * An application extension chunk of a GIF file.
 */
public class GifChunkApplicationExtension(
    header: ByteArray,
    private val subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.APPLICATION_EXTENSION,
    joinGifSubChunks(header, subChunks)
) {

    /** The 8-byte identifier that names the application, or NULL when the extension is too short to carry one. */
    public val applicationIdentifier: String?

    /** The authentication code that follows the identifier, or NULL when absent. */
    public val applicationCode: String?

    /**
     * Whether this extension carries an XMP packet, matched by the
     * well-known "XMP DataXMP" application identifier.
     */
    public val isXmpExtension: Boolean
        get() = applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER

    init {

        /*
         * The extension is kept even when its first sub-block is empty or
         * too short to hold the 8-byte identifier: unknown structures
         * stay untouched, and only the XMP matching cares about the
         * identifier, which is NULL then.
         */
        val firstSubChunk = subChunks.firstOrNull()

        val firstSubChunkSize = firstSubChunk
            ?.firstOrNull()
            ?.toUInt8()
            ?: 0

        if (firstSubChunk != null && firstSubChunkSize >= APPLICATION_IDENTIFIER_LENGTH) {

            val firstSubChunkByteReader = ByteArrayByteReader(firstSubChunk)

            /* The first byte is the sub-block size, not part of the payload. */
            firstSubChunkByteReader.readByte()

            applicationIdentifier = firstSubChunkByteReader.readBytes(
                fieldName = "application identifier",
                count = APPLICATION_IDENTIFIER_LENGTH
            ).decodeToString()

            applicationCode = firstSubChunkByteReader.readBytes(
                fieldName = "application code",
                count = firstSubChunkSize - APPLICATION_IDENTIFIER_LENGTH
            ).decodeToString()

        } else {

            applicationIdentifier = null
            applicationCode = null
        }
    }

    /**
     * Returns the XMP packet this extension carries, or throws when the
     * payload holds no readable packet.
     */
    @Throws(ImageReadException::class)
    public fun parseAsXmpOrThrow(): String {

        /*
         * The XMP payload is spread over size-prefixed sub-blocks.
         * Strip the size bytes and search the payload.
         * Fall back to the raw bytes for files written without
         * sub-block framing, where the size bytes are part of the data.
         */
        val unpackedContent = subChunks
            .map { subChunk -> subChunk.copyOfRange(1, subChunk.size).decodeToString() }
            .joinToString("")

        val content =
            if (unpackedContent.contains("<$XMP_META_TAG")) unpackedContent
            else bytes.decodeToString()

        if (!content.contains("<$XMP_META_TAG"))
            throw ImageReadException("No XMP data found in application extension.")

        return "<$XMP_META_TAG" + content
            .substringAfter("<$XMP_META_TAG")
            .substringBefore("</$XMP_META_TAG>")
            .plus("</$XMP_META_TAG>")
    }

    private companion object {

        /* The application identifier is 8 bytes */
        const val APPLICATION_IDENTIFIER_LENGTH = 8

        /* The opening element of an XMP packet */
        const val XMP_META_TAG = "x:xmpmeta"
    }
}
