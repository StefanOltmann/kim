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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * TIFF-based RAW and container formats are read-only: Kim parses them,
 * but never embeds or removes metadata. A refactor that misdetects one of
 * them as a writable format must fail here instead of rewriting user
 * files.
 */
class KimUpdateRejectionTest {

    /**
     * Updating a TIFF must be rejected before anything is written.
     */
    @Test
    fun testUpdateRejectsTiff() {

        val bytes = KimTestData.getBytesOf(KimTestData.TIFF_NONE_TEST_IMAGE_INDEX)

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = bytes,
                updates = setOf(MetadataUpdate.Title("x"))
            )
        }

        assertTrue(
            exception.message?.contains("Can't embed metadata") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Deleting the metadata of a HEIC must be rejected before anything is
     * written.
     */
    @Test
    fun testDeleteMetadataRejectsHeic() {

        val bytes = KimTestData.getBytesOf(KimTestData.HEIC_TEST_IMAGE_INDEX)

        val exception = assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(bytes)
        }

        assertTrue(
            exception.message?.contains("Can't delete metadata") == true,
            "Unexpected message: ${exception.message}"
        )
    }
}
