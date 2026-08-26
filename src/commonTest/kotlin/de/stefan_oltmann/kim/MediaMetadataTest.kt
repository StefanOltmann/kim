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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MediaMetadataTest {

    /**
     * Regression test based on a fixed small set of test files.
     *
     * Compares the metadata output of every test file against its committed
     * golden dump, so parser regressions across all formats are caught.
     * Mismatching outputs are written to "build/regenerated_txt" to ease
     * updating the goldens after an intentional change.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testToString() {

        val mismatchedIndexes = mutableListOf<Int>()

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val bytes = KimTestData.getBytesOf(index)

            /* Broken files are rejected by the segment length validation. */
            if (rejectedJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(bytes)
                }

                continue
            }

            val metadata = Kim.readMetadata(bytes)

            val actualToString = metadata.toString().encodeToByteArray()

            val expectedToString = KimTestData.getToStringText(index)

            if (!expectedToString.contentEquals(actualToString)) {

                mismatchedIndexes.add(index)

                SystemFileSystem.createDirectories(Path("build/regenerated_txt"))

                Path("build/regenerated_txt/media_$index.txt")
                    .writeBytes(actualToString)
            }
        }

        assertTrue(
            mismatchedIndexes.isEmpty(),
            "Metadata output does not match the golden files for media " +
                mismatchedIndexes.joinToString(prefix = "[", postfix = "]") +
                ". The regenerated dumps were written to build/regenerated_txt."
        )
    }

    private companion object {

        /* Media 44, 45 and 47 contain invalid segment lengths. */
        private val rejectedJpegIds = setOf(44, 45, 47)
    }
}
