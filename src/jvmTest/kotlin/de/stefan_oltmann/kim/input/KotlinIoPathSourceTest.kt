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
package de.stefan_oltmann.kim.input

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.kotlinx.readMetadata
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/*
 * The test is placed in jvmTest, because iOS Simulator won't run it.
 */
class KotlinIoPathSourceTest {

    fun getFullImageDiskPath(index: Int): String {

        val bytes = KimTestData.getBytesOf(index)

        val tmp = java.io.File.createTempFile("kim-test-$index-", ".tmp")

        tmp.writeBytes(bytes)

        tmp.deleteOnExit()

        return tmp.absolutePath
    }

    /**
     * Test to check that KotlinIoSourceByteReader works correctly.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testToStringWithKotlinIoPath() {

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val diskPath = getFullImageDiskPath(index)

            /* Broken files are rejected by the segment length validation. */
            if (rejectedJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(Path(diskPath))
                }

                continue
            }

            val metadata = Kim.readMetadata(Path(diskPath))

            val actualToString = metadata.toString().encodeToByteArray()

            val expectedToString = KimTestData.getToStringText(index)

            val equals = expectedToString.contentEquals(actualToString)

            /*
             * Note that ImageMetadataTest already writes the expected result.
             * This test does not write it to avoid confusion.
             */
            assertTrue(equals, "photo_$index.txt is different.")
        }
    }

    private companion object {

        /* Media 44, 45 and 47 contain invalid segment lengths. */
        private val rejectedJpegIds = setOf(44, 45, 47)
    }
}
