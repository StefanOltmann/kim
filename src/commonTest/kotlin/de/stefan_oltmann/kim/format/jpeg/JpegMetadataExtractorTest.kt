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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.fail

class JpegMetadataExtractorTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractMetadataBytes() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            /* Broken files are rejected by the segment length validation. */
            if (rejectedJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.extractMetadataBytes(ByteArrayByteReader(bytes))
                }

                continue
            }

            val byteReader = ByteArrayByteReader(bytes)

            /* Use the public Kim interface to ensure it works. */
            val actualMetadataBytes = Kim.extractMetadataBytes(byteReader).second

            val expectedMetadataBytes = KimTestData.getHeaderBytesOf(index)

            val equals = expectedMetadataBytes.contentEquals(actualMetadataBytes)

            if (!equals) {

                Path("build/media_${index}_header.jpg")
                    .writeBytes(actualMetadataBytes)

                fail("Media $index has not the expected bytes!")
            }
        }
    }

    private companion object {

        /* Media 45 and 47 contain invalid segment lengths. */
        private val rejectedJpegIds = setOf(45, 47)
    }
}
