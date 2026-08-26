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
package de.stefan_oltmann.kim.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MetadataSummaryTest {

    /**
     * Regression test: widthPx * heightPx for very large stitched
     * panoramas (e.g. 60000 × 40000 = 2.4e9) overflows Int silently,
     * producing a negative megapixel count. The multiplication must run
     * in Long space.
     */
    @Test
    fun testMegaPixelCountDoesNotOverflowForLargeImages() {

        val summary = MetadataSummary(widthPx = 60000, heightPx = 40000)

        assertEquals(2400, summary.megaPixelCount)
    }

    @Test
    fun testMegaPixelCountNormalImage() {

        val summary = MetadataSummary(widthPx = 6000, heightPx = 4000)

        assertEquals(24, summary.megaPixelCount)
    }

    @Test
    fun testMegaPixelCountNullDimensions() {

        val summary = MetadataSummary()

        assertEquals(0, summary.megaPixelCount)
    }
}
