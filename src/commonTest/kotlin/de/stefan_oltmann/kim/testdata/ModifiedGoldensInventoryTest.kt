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
package de.stefan_oltmann.kim.testdata

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The golden consumers skip indices without a modified variant, so a
 * deleted resource would silently remove that index from every golden
 * verification (segment dumps and byte roundtrips) without a failing
 * test. This inventory check turns a missing resource into a loud
 * failure instead.
 */
class ModifiedGoldensInventoryTest {

    /**
     * Every JPEG test image must come with a modified variant, so the
     * golden-based verification always covers the full index range.
     */
    @Test
    fun testModifiedGoldensAreComplete() {

        val missingIndices = (1..KimTestData.HIGHEST_JPEG_INDEX)
            .filterNot(KimTestData::hasModifiedBytesOf)

        assertTrue(
            missingIndices.isEmpty(),
            "Missing modified goldens for indices: $missingIndices"
        )
    }
}
