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
package de.stefan_oltmann.kim.common

import kotlin.test.Test
import kotlin.test.assertEquals

class GpsUtilTest {

    @Test
    fun testDecimalLatitudeToDDM() {

        assertEquals("53,13.1635N", GpsUtil.decimalLatitudeToDDM(53.219391))
        assertEquals("5,47.7178S", GpsUtil.decimalLatitudeToDDM(-5.795296))
    }

    @Test
    fun testDecimalLongitudeToDDM() {

        assertEquals("8,14.3797E", GpsUtil.decimalLongitudeToDDM(8.239661))
        assertEquals("64,26.6986W", GpsUtil.decimalLongitudeToDDM(-64.444976))
    }

    @Test
    fun testDecimalLatitudeToDDMWithMinuteCarry() {

        /* The minutes round up to 60, which must carry over to the degrees. */
        assertEquals("90,0.0N", GpsUtil.decimalLatitudeToDDM(89.9999999))

        /* Values beyond the boundary are clamped to the valid range. */
        assertEquals("90,0.0N", GpsUtil.decimalLatitudeToDDM(90.9999999))
    }

    @Test
    fun testDecimalLongitudeToDDMWithMinuteCarry() {

        /* The minutes round up to 60, which must carry over to the degrees. */
        assertEquals("180,0.0E", GpsUtil.decimalLongitudeToDDM(179.9999999))

        /* Values beyond the boundary are clamped to the valid range. */
        assertEquals("180,0.0E", GpsUtil.decimalLongitudeToDDM(180.9999999))

        /*
         * The clamp must cover the fractional part too: 90.5 decodes
         * back to 90.5 unless it is clamped to the pole.
         */
        assertEquals("90,0.0N", GpsUtil.decimalLatitudeToDDM(90.5))
        assertEquals("90,0.0S", GpsUtil.decimalLatitudeToDDM(-90.5))
        assertEquals("180,0.0E", GpsUtil.decimalLongitudeToDDM(200.0))
    }
}
