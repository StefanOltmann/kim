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
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.kotlinx.readMetadata
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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
     * Verifies that the kotlinx-io Path read facade produces exactly the
     * metadata the in-memory facade produces: the full corpus is compared
     * against the committed golden dumps, like
     * [de.stefan_oltmann.kim.MediaMetadataTest.testToString] does for
     * the byte-array path.
     */
    @Test
    fun testReadMetadataCorpusMatchesGoldensViaPath() {

        val mismatchedIndexes = mutableListOf<Int>()

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val diskPath = getFullImageDiskPath(index)

            /* Broken files are rejected by the segment length validation. */
            if (KimTestData.brokenJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(Path(diskPath))
                }

                continue
            }

            /*
             * media_80 stores its Exif and XMP in brob containers, which
             * cannot be read without brotli support - the read fails per
             * the strict read policy.
             */
            if (index == KimTestData.JXL_CONTAINER_COMPRESSED_INDEX) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(Path(diskPath))
                }

                continue
            }

            val metadata = Kim.readMetadata(Path(diskPath))

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
}
