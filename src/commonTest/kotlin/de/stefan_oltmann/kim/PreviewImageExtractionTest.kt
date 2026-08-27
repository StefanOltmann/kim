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

import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class PreviewImageExtractionTest {

    val indicesWithPreviewImage: Set<Int> = setOf(
        KimTestData.CR2_TEST_IMAGE_INDEX,
        KimTestData.RAF_TEST_IMAGE_INDEX,
        KimTestData.NEF_TEST_IMAGE_INDEX,
        KimTestData.ARW_TEST_IMAGE_INDEX,
        KimTestData.RW2_TEST_IMAGE_INDEX,
        KimTestData.ORF_TEST_IMAGE_INDEX,
        KimTestData.CR3_TEST_IMAGE_INDEX,
        KimTestData.DNG_CR2_TEST_IMAGE_INDEX,
        KimTestData.DNG_RAF_TEST_IMAGE_INDEX,
        KimTestData.DNG_NEF_TEST_IMAGE_INDEX,
        KimTestData.DNG_ARW_TEST_IMAGE_INDEX,
        KimTestData.DNG_RW2_TEST_IMAGE_INDEX,
        KimTestData.DNG_ORF_TEST_IMAGE_INDEX
    )

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractPreviewImage() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in indicesWithPreviewImage) {

            val bytes = KimTestData.getBytesOf(index)

            val previewImageBytes = Kim.extractPreviewImage(
                ByteArrayByteReader(bytes)
            )

            assertNotNull(previewImageBytes, "File #$index has no preview.")

            val expectedPreviewImageBytes = KimTestData.getPreviewBytesOf(index)

            val equals = expectedPreviewImageBytes.contentEquals(previewImageBytes)

            if (!equals) {

                Path("build/media_${index}_preview.jpg")
                    .writeBytes(previewImageBytes)

                fail("Media $index has not the expected bytes!")
            }
        }
    }
}
