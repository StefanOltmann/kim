/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PngImageParserTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testExtractPngText() {

        val index = KimTestData.PNG_TEST_IMAGE_INDEX

        val bytes = KimTestData.getHeaderBytesOf(index)

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(PngChunkType.IHDR, PngChunkType.TEXT, PngChunkType.ZTXT, PngChunkType.ITXT, PngChunkType.EXIF)
        )

        assertNotNull(chunks)
        assertEquals(4, chunks.size)

        val exifTxtChunk = chunks[1] as? PngChunkZtxt
        val iptcTxtChunk = chunks[2] as? PngChunkZtxt
        val xmpTxtChunk = chunks[3] as? PngChunkItxt

        assertNotNull(exifTxtChunk)
        assertEquals("Raw profile type exif", exifTxtChunk.keyword)

        assertNotNull(iptcTxtChunk)
        assertEquals("Raw profile type iptc", iptcTxtChunk.keyword)

        assertNotNull(xmpTxtChunk)
        assertEquals("XML:com.adobe.xmp", xmpTxtChunk.keyword)

        val expectedExif = KimTestData.getHeaderTextFile(index, "exif")
        val actualExif = exifTxtChunk.text

        assertEquals(expectedExif, actualExif, "EXIF is different.")

        val expectedIptc = KimTestData.getHeaderTextFile(index, "iptc")
        val actualIptc = iptcTxtChunk.text

        assertEquals(expectedIptc, actualIptc, "IPTC is different.")

        val expectedXmp = KimTestData.getHeaderTextFile(index, "xmp")
        val actualXmp = xmpTxtChunk.text

        assertEquals(expectedXmp, actualXmp, "XMP is different.")
    }

    /**
     * Text chunks whose "Raw profile type" content is not valid hex are
     * uninterpretable. They must be ignored like any other uninterpretable
     * chunk instead of failing the read of the whole file.
     */
    @Test
    fun testParseMetadataIgnoresGarbageRawProfileChunks() {

        val ihdrChunk = PngImageParser.readChunks(
            ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)
            ),
            listOf(PngChunkType.IHDR)
        ).single()

        val garbageExifChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type exif\u000045786966zzffd9".encodeToByteArray(),
            crc = 0
        )

        val garbageIptcChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type iptc\u00003842494dzz".encodeToByteArray(),
            crc = 0
        )

        val metadata = PngImageParser.parseMetadataFromChunks(
            listOf(ihdrChunk, garbageExifChunk, garbageIptcChunk)
        )

        assertNotNull(metadata)
    }

    /**
     * A "Raw profile type exif" chunk with valid hex that does not end
     * at the JPEG EOI marker is a truncated record. Per the strict read
     * policy the read fails instead of silently dropping the EXIF
     * content.
     */
    @Test
    fun testTruncatedExifTextChunkFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        val truncatedChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type exif\u00004578696600000002000a00ff".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    /**
     * A "Raw profile type iptc" chunk with an odd number of hex digits
     * cannot be converted to bytes completely - it is truncated. Per the
     * strict read policy the read fails instead of silently dropping the
     * IPTC content.
     */
    @Test
    fun testTruncatedIptcTextChunkFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        val truncatedChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type iptc\u00003842494d1c021".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    private fun readIhdrChunk() =
        PngImageParser.readChunks(
            ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)
            ),
            listOf(PngChunkType.IHDR)
        ).single()
}
