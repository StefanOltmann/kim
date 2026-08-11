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
import kotlin.test.assertNull

class LocationShownTest {

    @Test
    fun testDisplayStringWithName() {

        assertEquals(
            expected = "Times Square, USA",
            actual = LocationShown(
                name = "Times Square",
                street = null,
                city = null,
                state = null,
                country = "USA"
            ).displayString
        )

        assertEquals(
            expected = "Times Square",
            actual = LocationShown(
                name = "Times Square",
                street = null,
                city = null,
                state = null,
                country = null
            ).displayString
        )
    }

    @Test
    fun testDisplayStringWithCity() {

        assertEquals(
            expected = "Rastede, Deutschland",
            actual = LocationShown(
                name = null,
                street = null,
                city = "Rastede",
                state = null,
                country = "Deutschland"
            ).displayString
        )

        assertEquals(
            expected = "Rastede",
            actual = LocationShown(
                name = null,
                street = null,
                city = "Rastede",
                state = null,
                country = null
            ).displayString
        )
    }

    @Test
    fun testDisplayStringWithState() {

        assertEquals(
            expected = "Niedersachsen, Deutschland",
            actual = LocationShown(
                name = null,
                street = null,
                city = null,
                state = "Niedersachsen",
                country = "Deutschland"
            ).displayString
        )

        assertEquals(
            expected = "Niedersachsen",
            actual = LocationShown(
                name = null,
                street = null,
                city = null,
                state = "Niedersachsen",
                country = null
            ).displayString
        )
    }

    @Test
    fun testDisplayStringWithCountryOnly() {

        assertEquals(
            expected = "Deutschland",
            actual = LocationShown(
                name = null,
                street = null,
                city = null,
                state = null,
                country = "Deutschland"
            ).displayString
        )

        /* No location at all. */
        assertNull(
            LocationShown(
                name = null,
                street = null,
                city = null,
                state = null,
                country = null
            ).displayString
        )
    }
}
