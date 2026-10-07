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
package de.stefan_oltmann.kim.format.gif.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GifChunkApplicationExtensionTest {

    /**
     * Regression test: sub chunk sizes of 128 to 255 are legal, but the
     * size byte was read as a signed value, so such extensions failed
     * with an "Invalid size" error.
     */
    @Test
    fun testLargeFirstSubChunkSizeIsAccepted() {

        val identifier = "TESTAPP1".encodeToByteArray()

        val firstSubChunkData = ByteArray(200)

        identifier.copyInto(firstSubChunkData)

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(byteArrayOf(200.toByte()) + firstSubChunkData)
        )

        assertEquals("TESTAPP1", chunk.applicationIdentifier)
    }

    /**
     * A first sub-block too short for the 8-byte identifier keeps the
     * chunk with a NULL identifier instead of failing construction, so
     * one malformed extension cannot make a whole file unreadable.
     */
    @Test
    fun testShortFirstSubChunkYieldsNullIdentifier() {

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(byteArrayOf(4, 1, 2, 3, 4))
        )

        assertNull(chunk.applicationIdentifier)
        assertNull(chunk.applicationCode)
    }

    /**
     * A packet cut off between the opening and the closing element is
     * incomplete. Returning it would hand sidecar writers metadata that
     * only looks complete, so the parse fails instead - no synthetic
     * closer is fabricated to disguise the truncation either.
     */
    @Test
    fun testTruncatedPacketIsRejected() {

        val truncatedPacket = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF"

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(
                byteArrayOf(truncatedPacket.length.toByte()) +
                    truncatedPacket.encodeToByteArray()
            )
        )

        val exception = assertFailsWith<ImageReadException> {
            chunk.parseAsXmpOrThrow()
        }

        assertTrue(
            exception.message?.contains("truncated", ignoreCase = true) == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * The XMP specification allows a bare rdf:RDF root as an alternative
     * to the recommended x:xmpmeta envelope, and every other container
     * accepts it through the shared validator. A complete bare-RDF packet
     * must not fail a GIF extension as "truncated" - only the shared
     * completeness rule decides.
     */
    @Test
    fun testBareRdfPacketIsAccepted() {

        val packet =
            """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about=""/>""" +
                "</rdf:RDF>"

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(
                byteArrayOf(packet.length.toByte()) + packet.encodeToByteArray()
            )
        )

        assertEquals(packet, chunk.parseAsXmpOrThrow())
    }

    /**
     * Sub-blocks are size-prefixed chunks of at most 255 bytes, so any
     * packet beyond that size is spread over several of them. A multi-
     * byte UTF-8 sequence straddling such a boundary belongs to the
     * packet as a whole - decoding each block on its own turns it into
     * replacement characters and corrupts clean content.
     */
    @Test
    fun testMultiByteUtf8AcrossSubBlockBoundarySurvives() {

        val prefix = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF>M"
            .encodeToByteArray()
        val suffix = "ller</rdf:RDF></x:xmpmeta>".encodeToByteArray()

        val firstBlock = (prefix + byteArrayOf(0xC3.toByte()))
        val secondBlock = (byteArrayOf(0xBC.toByte()) + suffix)

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(
                byteArrayOf(firstBlock.size.toByte()) + firstBlock,
                byteArrayOf(secondBlock.size.toByte()) + secondBlock
            )
        )

        assertEquals(
            "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF>Müller</rdf:RDF></x:xmpmeta>",
            chunk.parseAsXmpOrThrow()
        )
    }

    /**
     * The raw-bytes fallback for extensions written without sub-block
     * framing must fail the read on invalid UTF-8 instead of fabricating
     * replacement characters into the packet.
     */
    @Test
    fun testRawFallbackRejectsInvalidUtf8() {

        /*
         * The size byte of the second block is 0x70: in the raw stream
         * it doubles as the 'p' of "<x:xmpmeta", so the raw bytes carry
         * a recognizable packet while the stripped payload does not.
         * The invalid sequence sits inside the envelope, so only the
         * decode - not the container padding - can be at fault.
         */
        val firstPayload = "abc<x:xm".encodeToByteArray()
        val secondPayload = "meta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF/>".encodeToByteArray() +
            byteArrayOf(0xC3.toByte(), 0x28) +
            "</x:xmpmeta>".encodeToByteArray()

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(
                byteArrayOf(firstPayload.size.toByte()) + firstPayload,
                byteArrayOf(0x70) + secondPayload
            )
        )

        val exception = assertFailsWith<ImageReadException> {
            chunk.parseAsXmpOrThrow()
        }

        assertTrue(
            exception.message?.contains("UTF-8", ignoreCase = false) == true,
            "Unexpected message: ${exception.message}"
        )
    }
}
