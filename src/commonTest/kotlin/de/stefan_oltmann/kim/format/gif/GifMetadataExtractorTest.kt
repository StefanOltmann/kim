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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GifMetadataExtractorTest {

    @Test
    fun testExtractMetadataBytes() {

        val index = KimTestData.GIF_TEST_IMAGE_INDEX

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
    /**
     * A byte that is not a known block introducer must fail the
     * metadata extraction like the full read does. Silently dropping
     * it produces a shortened metadata GIF.
     */
    @Test
    fun testExtractMetadataBytesRejectsUnknownBlockIntroducer() {

        /* Header, logical screen descriptor, one stray byte, then the
           terminator. */
        val bytes = "GIF89a".encodeToByteArray() +
            byteArrayOf(1, 0, 1, 0, 0, 0, 0) +
            byteArrayOf(0x55.toByte()) +
            byteArrayOf(GifConstants.GIF_TERMINATOR)

        assertFailsWith<ImageReadException> {
            GifMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
        }
    }

}
