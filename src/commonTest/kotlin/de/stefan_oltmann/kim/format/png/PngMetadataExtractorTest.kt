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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.format.png.chunk.PngChunkExif
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PngMetadataExtractorTest {

    @Test
    fun testIendCrc() {

        val bytes = KimTestData.getBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val metadataBytes = Kim.extractMetadataBytes(byteReader).second

        /* The CRC32 of the chunk type "IEND" is AE 42 60 82. */
        val expectedCrcBytes = byteArrayOf(0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte())

        val actualCrcBytes = metadataBytes.copyOfRange(metadataBytes.size - 4, metadataBytes.size)

        assertTrue(
            expectedCrcBytes.contentEquals(actualCrcBytes),
            "IEND chunk must carry CRC AE 42 60 82, but was ${actualCrcBytes.toSingleNumberHexes()}"
        )
    }

    @Test
    fun testExtractMetadataBytes() {

        val indices = setOf(
            KimTestData.PNG_TEST_IMAGE_INDEX,
            KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX,
            KimTestData.PNG_GIMP_TEST_IMAGE_INDEX
        )

        for (index in indices) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            /* Use the public Kim interface to ensure it works. */
            val actualMetadataBytes = Kim.extractMetadataBytes(byteReader).second

            val expectedMetadataBytes = KimTestData.getHeaderBytesOf(index)

            assertTrue(
                expectedMetadataBytes.contentEquals(actualMetadataBytes),
                "Photo $index has not the expected bytes!"
            )
        }
    }

    @Test
    fun testTestExtractExifBytes() {

        val indices = setOf(
            KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX
        )

        for (index in indices) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

            val exifChunk = chunks.filterIsInstance<PngChunkExif>().firstOrNull()

            val actualExifBytes = exifChunk?.bytes

            val expectedExifBytes = KimTestData.getHeaderExifBytesOf(index)

            assertTrue(
                expectedExifBytes.contentEquals(actualExifBytes),
                "Photo $index has not the expected bytes!"
            )
        }
    }

    /**
     * Pre-IDAT chunks beyond the shared metadata byte budget must fail
     * the extraction instead of accumulating without any bound - the
     * PNG parser enforces the same budget for its retained chunks.
     */
    @Test
    fun testExtractMetadataBytesRejectsOversizedChunks() {

        /* 17 chunks of 1 MiB lie beyond the 16 MiB budget. */
        val bytes = pngWithLargePreIdatChunks(chunkCount = 17)

        assertFailsWith<ImageReadException> {
            PngMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
        }
    }

    /**
     * Chunks just below the budget are a legitimate file and must keep
     * extracting, so the budget is not set too low.
     */
    @Test
    fun testExtractMetadataBytesAcceptsChunksBelowBudget() {

        /* 15 chunks of 1 MiB keep the accumulated output below the 16 MiB budget. */
        val bytes = pngWithLargePreIdatChunks(chunkCount = 15)

        PngMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
    }

    /**
     * Builds a PNG with the given number of 1 MiB pre-IDAT chunks, so
     * tests can exceed or stay below a byte budget with one-chunk
     * granularity. The CRC bytes are dummies, because the extractor
     * copies them without verification.
     */
    private fun pngWithLargePreIdatChunks(chunkCount: Int): ByteArray {

        val out = ByteArrayByteWriter()

        out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))

        /* IHDR with 13 data bytes. */
        out.write(int32BigEndian(13))
        out.write("IHDR".encodeToByteArray())
        out.write(ByteArray(13))
        out.write(int32BigEndian(0))

        repeat(chunkCount) {
            out.write(int32BigEndian(1024 * 1024))
            out.write("tEXt".encodeToByteArray())
            out.write(ByteArray(1024 * 1024))
            out.write(int32BigEndian(0))
        }

        /* The IDAT ends the metadata region. */
        out.write(int32BigEndian(6))
        out.write("IDAT".encodeToByteArray())
        out.write(ByteArray(6))
        out.write(int32BigEndian(0))

        out.write(int32BigEndian(0))
        out.write("IEND".encodeToByteArray())
        out.write(int32BigEndian(0xAE426082.toInt()))

        return out.toByteArray()
    }

    private fun int32BigEndian(value: Int): ByteArray =
        byteArrayOf(
            (value ushr 24).toByte(),
            (value ushr 16).toByte(),
            (value ushr 8).toByte(),
            value.toByte()
        )
}
