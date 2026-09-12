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
package de.stefan_oltmann.kim.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExifRatingTest {

    private val nullString: String? = null

    @Test
    fun testOfInt() {

        assertNull(ExifRating.of(-2))
        assertNull(ExifRating.of(6))

        assertEquals(
            ExifRating.REJECTED,
            ExifRating.of(-1)
        )

        assertEquals(
            ExifRating.UNRATED,
            ExifRating.of(0)
        )

        assertEquals(
            ExifRating.ONE_STAR,
            ExifRating.of(1)
        )

        assertEquals(
            ExifRating.TWO_STARS,
            ExifRating.of(2)
        )

        assertEquals(
            ExifRating.THREE_STARS,
            ExifRating.of(3)
        )

        assertEquals(
            ExifRating.FOUR_STARS,
            ExifRating.of(4)
        )

        assertEquals(
            ExifRating.FIVE_STARS,
            ExifRating.of(5)
        )
    }

    @Test
    fun testOfString() {

        assertNull(ExifRating.of(nullString))
        assertNull(ExifRating.of(""))
        assertNull(ExifRating.of("   "))
        assertNull(ExifRating.of("hello"))
        assertNull(ExifRating.of("-2"))
        assertNull(ExifRating.of("6"))

        assertEquals(
            ExifRating.REJECTED,
            ExifRating.of("-1")
        )

        assertEquals(
            ExifRating.UNRATED,
            ExifRating.of("0")
        )

        assertEquals(
            ExifRating.ONE_STAR,
            ExifRating.of("1")
        )

        assertEquals(
            ExifRating.TWO_STARS,
            ExifRating.of("2")
        )

        assertEquals(
            ExifRating.THREE_STARS,
            ExifRating.of("3")
        )

        assertEquals(
            ExifRating.FOUR_STARS,
            ExifRating.of("4")
        )

        assertEquals(
            ExifRating.FIVE_STARS,
            ExifRating.of("5")
        )
    }
}
