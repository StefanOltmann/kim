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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GpsCoordinatesTest {

    /**
     * Whole degrees must render invariantly on every platform: the raw
     * Double toString omits the fraction on JS and Wasm, so the display
     * string would diverge between targets.
     */
    @Test
    fun testLatLongStringIsInvariantForWholeDegrees() {

        assertEquals(
            expected = "8.0, 53.0",
            actual = GpsCoordinates(latitude = 8.0, longitude = 53.0).latLongString
        )
    }

    /**
     * Coordinates below 0.001 must render in plain decimal notation: the
     * invariant conversion switches to exponent notation there, which the
     * parse regex rejects and which is unreadable in the display.
     */
    @Test
    fun testLatLongStringRendersSmallValuesInPlainNotation() {

        val coordinate = GpsCoordinates(latitude = 0.00045, longitude = 30.0)

        assertEquals(
            expected = "0.00045, 30.0",
            actual = coordinate.latLongString
        )

        assertEquals(
            expected = coordinate,
            actual = GpsCoordinates.parse(coordinate.latLongString)
        )
    }

    @Test
    fun testLatLongString() {

        assertEquals(
            expected = "53.21939, 8.23966",
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).latLongString
        )
    }

    @Test
    fun testToRoundedCoordinates() {

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.21939,
                longitude = 8.23966
            ),
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).toRoundedCoordinates(
                precision = GpsCoordinates.Precision.ONE_METER
            )
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.2194,
                longitude = 8.2397
            ),
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).toRoundedCoordinates(
                precision = GpsCoordinates.Precision.TEN_METERS
            )
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219,
                longitude = 8.24
            ),
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).toRoundedCoordinates(
                precision = GpsCoordinates.Precision.HUNDRED_METERS
            )
        )
    }

    @Test
    fun testIsNullIsland() {

        assertTrue(
            GpsCoordinates(
                latitude = 0.0,
                longitude = 0.0
            ).isNullIsland()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).isNullIsland()
        )
    }

    @Test
    fun testIsValid() {

        /* Valid values */

        assertTrue(
            GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).isValid()
        )

        /* Edge values */

        assertTrue(
            GpsCoordinates(
                latitude = 90.0,
                longitude = 180.0
            ).isValid()
        )

        assertTrue(
            GpsCoordinates(
                latitude = -90.0,
                longitude = -180.0
            ).isValid()
        )

        /* Invalid values */

        assertFalse(
            GpsCoordinates(
                latitude = 91.0,
                longitude = 180.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = -90.0,
                longitude = -181.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 0.0,
                longitude = 200.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 200.0,
                longitude = 200.0
            ).isValid()
        )
    }

    @Test
    fun testParseEmptyValues() {

        assertNull(GpsCoordinates.parse(null))
        assertNull(GpsCoordinates.parse(""))
        assertNull(GpsCoordinates.parse("    "))
        assertNull(GpsCoordinates.parse("hello"))
    }

    @Test
    fun testParseValidValues() {

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse("53.219391,8.239661")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse("53.219391, 8.239661")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse(" 53.219391, 8.239661 ")
        )

        /* Edge values */

        assertEquals(
            expected = GpsCoordinates(
                latitude = -90.0,
                longitude = -180.0
            ),
            actual = GpsCoordinates.parse("-90.0, -180.0")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 90.0,
                longitude = 180.0
            ),
            actual = GpsCoordinates.parse("90.0, 180.0")
        )
    }

    @Test
    fun testParseInvalidValues() {

        assertNull(GpsCoordinates.parse("91.0, 45.0"))
        assertNull(GpsCoordinates.parse("60.0, 190.0"))
    }
}
