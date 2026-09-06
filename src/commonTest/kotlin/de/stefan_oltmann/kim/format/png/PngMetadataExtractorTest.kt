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
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
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
            "IEND chunk must carry CRC AE 42 60 82, but was ${actualCrcBytes.joinToString(" ") { it.toUByte().toString(16) }}"
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

            val actualExifBytes = PngMetadataExtractor.extractExifBytes(byteReader)

            val expectedExifBytes = KimTestData.getHeaderExifBytesOf(index)

            assertTrue(
                expectedExifBytes.contentEquals(actualExifBytes),
                "Photo $index has not the expected bytes!"
            )
        }
    }
}
