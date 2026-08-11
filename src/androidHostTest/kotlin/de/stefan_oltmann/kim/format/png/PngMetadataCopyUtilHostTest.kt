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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests copying PNG metadata chunks.
 *
 * The test is placed in androidHostTest, because PngMetadataCopyUtil lives in ktorMain.
 */
class PngMetadataCopyUtilHostTest {

    @Test
    fun testCopyByteArray() {

        val sourceBytes = KimTestData.getBytesOf(KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX)

        val destinationBytes = KimTestData.getBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)

        val expectedBytes = com.goncalossilva.resources.Resource(
            "de/stefan_oltmann/kim/copy_test.png"
        ).readBytes()

        val actualBytes = PngMetadataCopyUtil.copy(
            source = sourceBytes,
            destination = destinationBytes
        )

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals)
            fail("copy_test.png has not the expected bytes!")
    }

    @Test
    fun testCopyByteArrayWithoutMetadata() {

        /* Both files without metadata chunks. */
        val sourceBytes = KimTestData.getBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)

        val actualBytes = PngMetadataCopyUtil.copy(
            source = sourceBytes,
            destination = sourceBytes
        )

        assertTrue(actualBytes.isNotEmpty())
    }
}
