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
package de.stefan_oltmann.kim.format.gif

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkHeader
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkImageDescriptor
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkTerminator
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.ImageSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GifImageParserFullTest {

    /**
     * Creates a small GIF89a file with a comment, an XMP application
     * extension, a plain text extension, a graphics control extension
     * and a single 1x1 image.
     */
    private fun createGif89aWithMetadata(): ByteArray {

        val xmp = """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF/></x:xmpmeta>"""
        val xmpBytes = xmp.encodeToByteArray()

        val bytes = mutableListOf<Byte>()

        /* Header */
        bytes.addAll("GIF89a".encodeToByteArray().toList())

        /* Logical screen descriptor: 1x1, global color table */
        bytes.addAll(byteArrayOf(1, 0, 1, 0, 0x90.toByte(), 0, 0).toList())

        /* Global color table: 2 colors */
        bytes.addAll(byteArrayOf(0, 0, 0, -1, -1, -1).toList())

        /* Comment extension */
        bytes.addAll(
            byteArrayOf(
                0x21, 0xFE.toByte(), 3,
                'H'.code.toByte(), 'i'.code.toByte(), '!'.code.toByte(), 0
            ).toList()
        )

        /* Application extension with XMP data */
        bytes.addAll(byteArrayOf(0x21, 0xFF.toByte(), 11).toList())
        bytes.addAll("XMP DataXMP".encodeToByteArray().toList())
        bytes.add(xmpBytes.size.toByte())
        bytes.addAll(xmpBytes.toList())
        bytes.add(0)

        /* Plain text extension */
        bytes.addAll(
            byteArrayOf(
                0x21, 0x01, 12,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            ).toList()
        )

        /* Graphics control extension */
        bytes.addAll(byteArrayOf(0x21, 0xF9.toByte(), 4, 0, 0, 0, 0, 0).toList())

        /* Image separator and descriptor: 1x1 image at 0,0 */
        bytes.add(0x2C)
        bytes.addAll(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0).toList())

        /* Image data: LZW minimum code size 2, one sub chunk, terminator */
        bytes.addAll(byteArrayOf(2, 1, 5, 0).toList())

        /* Terminator */
        bytes.add(0x3B)

        return bytes.toByteArray()
    }

    @Test
    fun testReadChunks() {

        val chunks = GifImageParser.readChunks(
            byteReader = ByteArrayByteReader(createGif89aWithMetadata()),
            chunkTypeFilter = null
        )

        val chunkTypes = chunks.map { it.type }

        assertTrue(GifChunkType.HEADER in chunkTypes)
        assertTrue(GifChunkType.LOGICAL_SCREEN_DESCRIPTOR in chunkTypes)
        assertTrue(GifChunkType.GLOBAL_COLOR_TABLE in chunkTypes)
        assertTrue(GifChunkType.COMMENT_EXTENSION in chunkTypes)
        assertTrue(GifChunkType.APPLICATION_EXTENSION in chunkTypes)
        assertTrue(GifChunkType.PLAIN_TEXT_EXTENSION in chunkTypes)
        assertTrue(GifChunkType.GRAPHICS_CONTROL_EXTENSION in chunkTypes)
        assertTrue(GifChunkType.IMAGE_DESCRIPTOR in chunkTypes)
        assertTrue(GifChunkType.IMAGE_DATA in chunkTypes)
        assertTrue(GifChunkType.TERMINATOR in chunkTypes)
    }

    @Test
    fun testReadChunksWithFilter() {

        val chunks = GifImageParser.readChunks(
            byteReader = ByteArrayByteReader(createGif89aWithMetadata()),
            chunkTypeFilter = listOf(GifChunkType.HEADER)
        )

        assertEquals(listOf(GifChunkType.HEADER), chunks.map { it.type })
    }

    @Test
    fun testParseMetadata() {

        val metadata = GifImageParser.parseMetadata(
            ByteArrayByteReader(createGif89aWithMetadata())
        )

        assertEquals(de.stefan_oltmann.kim.model.MediaFormat.GIF, metadata.mediaFormat)
        assertEquals(ImageSize(1, 1), metadata.imageSize)
        assertNotNull(metadata.xmp)
        assertTrue(metadata.xmp.contains("<x:xmpmeta"))
    }

    @Test
    fun testParseMetadataFromChunksGif87aHasNoXmp() {

        val gif87aBytes = buildList<Byte> {
            addAll("GIF87a".encodeToByteArray().toList())
            addAll(byteArrayOf(1, 0, 1, 0, 0x80.toByte(), 0, 0).toList())
            addAll(byteArrayOf(0, 0, 0, -1, -1, -1).toList())
            add(0x2C)
            addAll(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0).toList())
            addAll(byteArrayOf(2, 1, 5, 0).toList())
            add(0x3B)
        }.toByteArray()

        val metadata = GifImageParser.parseMetadata(ByteArrayByteReader(gif87aBytes))

        assertEquals(de.stefan_oltmann.kim.model.MediaFormat.GIF, metadata.mediaFormat)
        assertEquals(ImageSize(1, 1), metadata.imageSize)
        assertEquals(null, metadata.xmp)
    }

    @Test
    fun testParseMetadataRejectsEmptyChunks() {

        assertFailsWith<ImageReadException> {
            GifImageParser.parseMetadataFromChunks(emptyList())
        }
    }

    @Test
    fun testParseMetadataRejectsMissingHeaderChunk() {

        val chunks = GifImageParser.readChunks(
            byteReader = ByteArrayByteReader(createGif89aWithMetadata()),
            chunkTypeFilter = listOf(GifChunkType.IMAGE_DESCRIPTOR)
        )

        assertFailsWith<ImageReadException> {
            GifImageParser.parseMetadataFromChunks(chunks)
        }
    }

    @Test
    fun testParseMetadataRejectsMissingImageDescriptor() {

        val chunks = GifImageParser.readChunks(
            byteReader = ByteArrayByteReader(createGif89aWithMetadata()),
            chunkTypeFilter = listOf(GifChunkType.HEADER)
        )

        assertFailsWith<ImageReadException> {
            GifImageParser.parseMetadataFromChunks(chunks)
        }
    }

    @Test
    fun testParseMetadataRejectsFileWithoutChunks() {

        assertFailsWith<ImageReadException> {
            GifImageParser.parseMetadata(ByteArrayByteReader("GIF89a".encodeToByteArray()))
        }
    }

    @Test
    fun testExtractMetadataBytes() {

        val original = createGif89aWithMetadata()

        val modified = GifMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(original))

        /* The signature and terminator are kept. */
        assertTrue(modified.copyOfRange(0, 6).decodeToString().startsWith("GIF89a"))
        assertEquals(0x3B, modified.last().toInt())

        /* The extracted bytes are a valid GIF again. */
        val chunks = GifImageParser.readChunks(ByteArrayByteReader(modified), null)

        assertNotNull(chunks.firstOrNull { it.type == GifChunkType.HEADER })
        assertNotNull(chunks.firstOrNull { it.type == GifChunkType.IMAGE_DESCRIPTOR })
    }

    @Test
    fun testExtractMetadataBytesRejectsWrongSignature() {

        assertFailsWith<ImageReadException> {
            GifMetadataExtractor.extractMetadataBytes(
                ByteArrayByteReader("PNG89a".encodeToByteArray())
            )
        }
    }

    @Test
    fun testExtractMetadataBytesRejectsWrongVersion() {

        assertFailsWith<ImageReadException> {
            GifMetadataExtractor.extractMetadataBytes(
                ByteArrayByteReader("GIF99a".encodeToByteArray())
            )
        }
    }

    @Test
    fun testGifChunkHeader() {

        val header = GifChunkHeader("GIF89a".encodeToByteArray())
        assertEquals(GifVersion.GIF89A, header.version)

        val header87 = GifChunkHeader("GIF87a".encodeToByteArray())
        assertEquals(GifVersion.GIF87A, header87.version)

        assertFailsWith<ImageReadException> {
            GifChunkHeader("GIF8".encodeToByteArray())
        }

        assertFailsWith<ImageReadException> {
            GifChunkHeader("PNG89a".encodeToByteArray())
        }

        assertFailsWith<ImageReadException> {
            GifChunkHeader("GIF99a".encodeToByteArray())
        }
    }

    @Test
    fun testGifChunkTerminator() {

        GifChunkTerminator(byteArrayOf(0x3B))

        assertFailsWith<ImageReadException> {
            GifChunkTerminator(byteArrayOf(0x3B, 0x3B))
        }

        assertFailsWith<ImageReadException> {
            GifChunkTerminator(byteArrayOf(0x00))
        }
    }

    @Test
    fun testGifChunkApplicationExtension() {

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

        assertEquals("XMP Data", chunk.applicationIdentifier)
        assertEquals("XMP", chunk.applicationCode)
        assertEquals(xmp, chunk.parseAsXmpOrThrow())

        /* Empty sub chunks. */
        assertFailsWith<ImageReadException> {
            GifChunkApplicationExtension(
                header = byteArrayOf(0x21, 0xFF.toByte()),
                subChunks = emptyList()
            )
        }

        /* Too small first sub chunk. */
        assertFailsWith<ImageReadException> {
            GifChunkApplicationExtension(
                header = byteArrayOf(0x21, 0xFF.toByte()),
                subChunks = listOf(byteArrayOf(3) + "abc".encodeToByteArray())
            )
        }

        /* No XMP content. */
        assertFailsWith<ImageReadException> {
            GifChunkApplicationExtension(
                header = byteArrayOf(0x21, 0xFF.toByte()),
                subChunks = listOf(
                    byteArrayOf(11) + "XMP DataXMP".encodeToByteArray(),
                    byteArrayOf(4) + "nope".encodeToByteArray()
                )
            ).parseAsXmpOrThrow()
        }
    }

    @Test
    fun testGifChunkImageDescriptor() {

        val descriptor = GifChunkImageDescriptor(
            byteArrayOf(0x2C) + byteArrayOf(0, 0, 0, 0, 10, 0, 20, 0, 0)
        )

        assertEquals(ImageSize(10, 20), descriptor.imageSize)
    }
}
