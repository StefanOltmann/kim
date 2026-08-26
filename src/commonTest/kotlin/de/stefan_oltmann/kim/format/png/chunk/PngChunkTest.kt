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
import de.stefan_oltmann.kim.common.MAX_DECOMPRESSED_BYTE_COUNT
import de.stefan_oltmann.kim.common.compress
import de.stefan_oltmann.kim.format.png.PngChunkType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PngChunkTest {

    @Test
    fun testChunkTypeOf() {

        assertEquals(PngChunkType.IHDR, PngChunkType.of("IHDR".encodeToByteArray()))
        assertEquals(PngChunkType.IDAT, PngChunkType.of("IDAT".encodeToByteArray()))
        assertEquals(PngChunkType.IEND, PngChunkType.of("IEND".encodeToByteArray()))
        assertEquals(PngChunkType.TIME, PngChunkType.of("tIME".encodeToByteArray()))
        assertEquals(PngChunkType.TEXT, PngChunkType.of("tEXt".encodeToByteArray()))
        assertEquals(PngChunkType.ZTXT, PngChunkType.of("zTXt".encodeToByteArray()))
        assertEquals(PngChunkType.ITXT, PngChunkType.of("iTXt".encodeToByteArray()))
        assertEquals(PngChunkType.EXIF, PngChunkType.of("eXIf".encodeToByteArray()))
    }

    @Test
    fun testChunkTypeEquality() {

        val first = PngChunkType.of("iTXt".encodeToByteArray())
        val second = PngChunkType.of("iTXt".encodeToByteArray())
        val other = PngChunkType.of("tEXt".encodeToByteArray())

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, other)
        assertNotEquals<Any>(first, "iTXt")
        assertEquals(first, first)
    }

    @Test
    fun testChunkTypeRejectsWrongLength() {

        assertFailsWith<IllegalArgumentException> {
            PngChunkType.of("IHD".encodeToByteArray())
        }
    }

    @Test
    fun testChunkProperties() {

        /* tEXt: lowercase first letter means ancillary. */
        val ancillary = PngChunk(PngChunkType.TEXT, byteArrayOf(1), 0)

        assertTrue(ancillary.ancillary)
        assertFalse(ancillary.isPrivate)
        assertFalse(ancillary.reserved)
        assertTrue(ancillary.safeToCopy)

        /* IDAT: uppercase first letter means critical. */
        val critical = PngChunk(PngChunkType.IDAT, byteArrayOf(1), 0)

        assertFalse(critical.ancillary)

        assertEquals(
            expected = "PngChunk tEXt (1 bytes, ancillary, public, not reserved, safe to copy)",
            actual = ancillary.toString()
        )
    }

    @Test
    fun testItxtChunk() {

        val keyword = "XML:com.adobe.xmp"
        val languageTag = "en"
        val translatedKeyword = "XMP"
        val text = "Some XMP data"

        val bytes = keyword.encodeToByteArray() +
            byteArrayOf(0) +
            byteArrayOf(0) +
            byteArrayOf(0) +
            languageTag.encodeToByteArray() +
            byteArrayOf(0) +
            translatedKeyword.encodeToByteArray() +
            byteArrayOf(0) +
            text.encodeToByteArray()

        val chunk = PngChunkItxt(bytes, 0)

        assertEquals(keyword, chunk.getKeyword())
        assertEquals(text, chunk.getText())
        assertEquals(languageTag, chunk.languageTag)
        assertEquals(translatedKeyword, chunk.translatedKeyword)
    }

    @Test
    fun testItxtChunkWithCompressedText() {

        val keyword = "Comment"
        val text = "A very long comment that gets compressed."

        val bytes = keyword.encodeToByteArray() +
            byteArrayOf(0) +
            byteArrayOf(1) +
            byteArrayOf(0) +
            byteArrayOf(0) +
            byteArrayOf(0) +
            compress(text)

        val chunk = PngChunkItxt(bytes, 0)

        assertEquals(keyword, chunk.getKeyword())
        assertEquals(text, chunk.getText())
    }

    @Test
    fun testItxtChunkRejectsInvalidData() {

        /* Keyword is not terminated. */
        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword".encodeToByteArray(), 0)
        }

        /* Invalid compression flag. */
        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword\u0000\u0002\u0000\u0000\u0000".encodeToByteArray(), 0)
        }

        /* Compressed with unexpected method. */
        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword\u0000\u0001\u0001\u0000\u0000".encodeToByteArray(), 0)
        }

        /* Language tag is not terminated. */
        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword\u0000\u0000\u0000en".encodeToByteArray(), 0)
        }
    }

    @Test
    fun testZtxtChunk() {

        val keyword = "Comment"
        val text = "Compressed comment text"

        val bytes = keyword.encodeToByteArray() +
            byteArrayOf(0) +
            byteArrayOf(0) +
            compress(text)

        val chunk = PngChunkZtxt(bytes, 0)

        assertEquals(keyword, chunk.getKeyword())
        assertEquals(text, chunk.getText())
    }

    @Test
    fun testZtxtChunkRejectsInvalidData() {

        /* Keyword is not terminated. */
        assertFailsWith<ImageReadException> {
            PngChunkZtxt("Keyword".encodeToByteArray(), 0)
        }

        /* Unexpected compression method. */
        assertFailsWith<ImageReadException> {
            PngChunkZtxt("Keyword\u0000\u0001".encodeToByteArray(), 0)
        }
    }

    /**
     * Regression test: a chunk that ends directly behind the keyword
     * terminator must be rejected with an [ImageReadException] instead
     * of a raw IndexOutOfBoundsException.
     */
    @Test
    fun testZtxtChunkRejectsMissingCompressionMethod() {

        assertFailsWith<ImageReadException> {
            PngChunkZtxt("Keyword\u0000".encodeToByteArray(), 0)
        }
    }

    /**
     * Decompression of untrusted chunks must abort once the output
     * exceeds the limit, so hostile input cannot exhaust the memory.
     */
    @Test
    fun testZtxtChunkRejectsDecompressionBomb() {

        val text = "A".repeat(MAX_DECOMPRESSED_BYTE_COUNT + 1)

        val bytes = "Comment\u0000\u0000".encodeToByteArray() + compress(text)

        assertFailsWith<ImageReadException> {
            PngChunkZtxt(bytes, 0)
        }
    }

    /**
     * Regression test: a chunk that ends directly behind the keyword
     * terminator must be rejected with an [ImageReadException] instead
     * of a raw IndexOutOfBoundsException.
     */
    @Test
    fun testItxtChunkRejectsMissingCompressionFields() {

        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword\u0000".encodeToByteArray(), 0)
        }
    }

    /**
     * A chunk that ends behind the compression fields, but has no
     * terminated language tag, must also fail cleanly.
     */
    @Test
    fun testItxtChunkRejectsUnterminatedLanguageTag() {

        assertFailsWith<ImageReadException> {
            PngChunkItxt("Keyword\u0000\u0000\u0000".encodeToByteArray(), 0)
        }
    }
}
