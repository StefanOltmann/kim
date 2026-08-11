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

class ImageSizeTest {

    @Test
    fun testLongestSide() {

        assertEquals(4000, ImageSize(4000, 3000).longestSide)
        assertEquals(4000, ImageSize(3000, 4000).longestSide)
        assertEquals(100, ImageSize(100, 100).longestSide)
    }

    @Test
    fun testToString() {

        assertEquals("4000 x 3000", ImageSize(4000, 3000).toString())
    }
}
