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
import de.stefan_oltmann.kim.common.decodeStrictUtf8
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.isEquals
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.gif.GifChunkType
import de.stefan_oltmann.kim.format.gif.GifConstants
import de.stefan_oltmann.kim.format.xmp.RDF_ROOT_END_TAG
import de.stefan_oltmann.kim.format.xmp.RDF_ROOT_START_TAG
import de.stefan_oltmann.kim.format.xmp.XMP_PACKET_END_TAG
import de.stefan_oltmann.kim.format.xmp.XMP_PACKET_START_TAG
import de.stefan_oltmann.kim.format.xmp.requireValidXmpPacket
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readBytes

/**
 * An application extension chunk of a GIF file.
 */
public class GifChunkApplicationExtension(
    header: ByteArray,
    private val subChunks: List<ByteArray>,
    internal val contiguouslyFramed: Boolean = false
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

        if (firstSubChunk != null && firstSubChunkSize >= GifConstants.APPLICATION_IDENTIFIER_LENGTH) {

            val firstSubChunkByteReader = ByteArrayByteReader(firstSubChunk)

            /* The first byte is the sub-block size, not part of the payload. */
            firstSubChunkByteReader.readByte()

            applicationIdentifier = firstSubChunkByteReader.readBytes(
                fieldName = "application identifier",
                count = GifConstants.APPLICATION_IDENTIFIER_LENGTH
            ).decodeToString()

            applicationCode = firstSubChunkByteReader.readBytes(
                fieldName = "application code",
                count = firstSubChunkSize - GifConstants.APPLICATION_IDENTIFIER_LENGTH
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
         * Strip the size bytes and decode the payload as a whole: a
         * multi-byte UTF-8 sequence straddling a sub-block boundary
         * would be corrupted by decoding each block on its own.
         *
         * The contiguous Adobe binding stores the packet directly, so
         * stripping would cut its first byte - the framing decides
         * which byte stream leads the envelope search, and the other
         * stream stays as the fallback.
         */
        val strippedPayload = subChunks
            .map { subChunk -> subChunk.copyOfRange(1, subChunk.size) }
            .reduceOrNull(ByteArray::plus)
            ?: ByteArray(0)

        val content =
            if (contiguouslyFramed)
                decodePacketBytes(bytes, "The GIF XMP extension")
                    ?: decodePacketBytes(strippedPayload, "The GIF XMP extension payload")
            else
                decodePacketBytes(strippedPayload, "The GIF XMP extension payload")
                    ?: decodePacketBytes(bytes, "The GIF XMP extension")
            ?: throw ImageReadException("No XMP data found in application extension.")

        /*
         * Completeness and the accepted envelope forms - the recommended
         * x:xmpmeta wrapper or a bare rdf:RDF root - are decided by the
         * shared validator, like in every other container. No synthetic
         * closer is fabricated to disguise a truncation either.
         */
        return requireNotNull(
            requireValidXmpPacket(
                xmp = content,
                sourceDescription = "The GIF XMP extension"
            )
        )
    }

    /**
     * Decodes the bytes of the XMP packet, strictly, or NULL when the
     * payload carries neither the recommended `x:xmpmeta` envelope nor a
     * complete bare `rdf:RDF` root. Two reasons keep the search in the
     * byte domain: Adobe's XMP toolkit pads GIF packets to a whole
     * sub-block with a binary filler sequence behind the closing element
     * - that filler is container structure and must neither fabricate
     * replacement characters nor fail the read - and the unframed
     * fallback blob contains size and header bytes that are not UTF-8 by
     * structure.
     */
    private fun decodePacketBytes(
        payload: ByteArray,
        sourceDescription: String
    ): String? {

        val xmpMetaStart = payload.indexOfSequence(XMP_META_START_TAG_BYTES, 0)

        if (xmpMetaStart >= 0) {

            /*
             * The recommended envelope wins when both forms appear. An
             * envelope without its closing element is handed to the
             * validator, whose truncation error names the missing closer.
             */
            val closeTagStart =
                payload.indexOfSequence(XMP_META_END_TAG_BYTES, xmpMetaStart)

            val packetBytes =
                if (closeTagStart > -1)
                    payload.slice(xmpMetaStart, closeTagStart + XMP_META_END_TAG_BYTES.size - xmpMetaStart)
                else
                    payload.getRemainingBytes(xmpMetaStart)

            return packetBytes.decodeStrictUtf8(sourceDescription)
        }

        /*
         * The bare RDF root is the alternative envelope form. A fragment
         * without its closing element is not decoded: it must not block
         * the raw-bytes fallback, whose error then reports no readable
         * packet.
         */
        val rdfRootStart = payload.indexOfSequence(RDF_ROOT_START_TAG_BYTES, 0)

        if (rdfRootStart < 0)
            return null

        val closeTagStart =
            payload.indexOfSequence(RDF_ROOT_END_TAG_BYTES, rdfRootStart)

        if (closeTagStart < 0)
            return null

        return payload.slice(rdfRootStart, closeTagStart + RDF_ROOT_END_TAG_BYTES.size - rdfRootStart)
            .decodeStrictUtf8(sourceDescription)
    }

    private fun ByteArray.indexOfSequence(
        needle: ByteArray,
        fromIndex: Int
    ): Int {

        val lastIndex = size - needle.size

        for (start in fromIndex..lastIndex) {
            if (isEquals(start, needle, 0, needle.size))
                return start
        }

        return -1
    }

    private companion object {

        /*
         * The envelope elements as bytes, for the search in the raw
         * payload. The tag texts live in XmpPacketValidation, so the
         * accepted forms cannot drift between the containers.
         */
        val XMP_META_START_TAG_BYTES = XMP_PACKET_START_TAG.encodeToByteArray()
        val XMP_META_END_TAG_BYTES = XMP_PACKET_END_TAG.encodeToByteArray()
        val RDF_ROOT_START_TAG_BYTES = RDF_ROOT_START_TAG.encodeToByteArray()
        val RDF_ROOT_END_TAG_BYTES = RDF_ROOT_END_TAG.encodeToByteArray()
    }
}
