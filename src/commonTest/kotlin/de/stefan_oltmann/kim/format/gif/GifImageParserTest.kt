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

package de.stefan_oltmann.kim.format.gif

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GifImageParserTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testExtractGifText() {

        val index = KimTestData.GIF_TEST_IMAGE_INDEX

        val bytes = KimTestData.getHeaderBytesOf(index)

        val chunks = GifImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(
                GifChunkType.HEADER,
                GifChunkType.LOGICAL_SCREEN_DESCRIPTOR,
                GifChunkType.GLOBAL_COLOR_TABLE,
                GifChunkType.APPLICATION_EXTENSION,
                GifChunkType.IMAGE_DESCRIPTOR,
                GifChunkType.IMAGE_DATA,
                GifChunkType.TERMINATOR
            )
        )

        assertNotNull(chunks)
        assertEquals(7, chunks.size)

        val xmpApplicationChunk = chunks[3] as? GifChunkApplicationExtension

        assertNotNull(xmpApplicationChunk)
        assertEquals("XMP Data", xmpApplicationChunk.applicationIdentifier)
        assertEquals("XMP", xmpApplicationChunk.applicationCode)

        val expectedXmp = KimTestData.getHeaderTextFile(index, "xmp").trim()
        val actualXmp = xmpApplicationChunk.parseAsXmpOrThrow().trim()

        assertEquals(expectedXmp, actualXmp, "XMP is different.")
    }

    /**
     * Application extensions written without sub-block framing,
     * where the size bytes are part of the data, must still be readable.
     */
    @Test
    fun testParseUnframedApplicationExtension() {

        val xmp = """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF/></x:xmpmeta>"""

        val xmpBytes = xmp.encodeToByteArray()

        val subChunks = listOf(
            byteArrayOf(11) + "XMP DataXMP".encodeToByteArray(),
            byteArrayOf(xmpBytes[0]) + xmpBytes.copyOfRange(1, xmpBytes.size)
        )

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = subChunks
        )

        assertEquals(xmp, chunk.parseAsXmpOrThrow())
    }

    /**
     * A byte that is not a known block introducer must fail the parse
     * like the streaming write path does. Silently dropping it would
     * shift all following data and produce a shortened GIF.
     */
    @Test
    fun testReadChunksRejectsUnknownBlockIntroducer() {

        /* Header, logical screen descriptor without color table,
           one stray byte, then the terminator. */
        val bytes = "GIF89a".encodeToByteArray() +
            byteArrayOf(1, 0, 1, 0, 0, 0, 0) +
            byteArrayOf(0x55.toByte()) +
            byteArrayOf(GifConstants.GIF_TERMINATOR)

        assertFailsWith<ImageReadException> {
            GifImageParser.readChunks(ByteArrayByteReader(bytes), null)
        }
    }
}
