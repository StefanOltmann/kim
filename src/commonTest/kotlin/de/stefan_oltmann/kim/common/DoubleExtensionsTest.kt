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
package de.stefan_oltmann.kim.common

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The expected values pin the JVM's Double.toString rendering, because
 * written metadata and text dumps must be byte-identical on all platforms.
 * On JS and WASM this test fails for plain Double.toString whenever the
 * platform deviates (whole numbers without ".0", exponent thresholds).
 */
class DoubleExtensionsTest {

    @Test
    fun testWholeNumbersKeepFractionDigit() {

        assertEquals("0.0", 0.0.toInvariantString())

        assertEquals("-0.0", (-0.0).toInvariantString())

        assertEquals("1.0", 1.0.toInvariantString())

        assertEquals("300.0", 300.0.toInvariantString())

        assertEquals("100.0", 100.0.toInvariantString())

        assertEquals("-300.0", (-300.0).toInvariantString())
    }

    @Test
    fun testFractionsRenderAsIs() {

        assertEquals("0.1", 0.1.toInvariantString())

        assertEquals("0.25", 0.25.toInvariantString())

        assertEquals("7.1", 7.1.toInvariantString())

        assertEquals("54.678955", 54.678955.toInvariantString())

        assertEquals("0.3333333333333333", (1.0 / 3.0).toInvariantString())
    }

    @Test
    fun testPlainNotationRangeMatchesJvm() {

        /* 1.0E-3 itself is still plain. */
        assertEquals("0.001", 0.001.toInvariantString())

        /* Smaller values switch to exponent notation. */
        assertEquals("1.0E-4", 0.0001.toInvariantString())

        assertEquals("9.9999E-5", 0.000099999.toInvariantString())

        assertEquals("1.25E-5", 1.25E-5.toInvariantString())

        assertEquals("5.0E-6", 5.0E-6.toInvariantString())
    }

    @Test
    fun testExponentNotationRangeMatchesJvm() {

        /* 9999999.0 itself is still plain. */
        assertEquals("9999999.0", 9999999.0.toInvariantString())

        /* 1.0E7 and above switch to exponent notation. */
        assertEquals("1.0E7", 1.0E7.toInvariantString())

        assertEquals("1.23456789E8", 1.23456789E8.toInvariantString())
    }

    @Test
    fun testSpecialValues() {

        assertEquals("NaN", Double.NaN.toInvariantString())

        assertEquals("Infinity", Double.POSITIVE_INFINITY.toInvariantString())

        assertEquals("-Infinity", Double.NEGATIVE_INFINITY.toInvariantString())
    }

    /*
     * The float expectations were taken from the JVM's Float.toString for
     * Olympus ImageProcessing float fields, where the shortest float
     * digits differ from the double digits of the same value.
     */
    @Test
    fun testWholeFloatsKeepFractionDigit() {

        assertEquals("90.0", 90.0f.toInvariantString())

        assertEquals("0.0", 0.0f.toInvariantString())

        assertEquals("-0.0", (-0.0f).toInvariantString())
    }

    @Test
    fun testFloatsUseShortestFloatDigits() {

        /* A double rendering would carry nine extra digits here. */
        assertEquals("0.99121094", 0.99121094f.toInvariantString())

        assertEquals("-2.3007393E-4", (-2.3007393E-4f).toInvariantString())

        assertEquals("3.824234E-4", 3.824234E-4f.toInvariantString())

        assertEquals("-1.835823E-5", (-1.835823E-5f).toInvariantString())

        assertEquals("2.3031235E-4", 2.3031235E-4f.toInvariantString())

        assertEquals("-8.211136E-4", (-8.211136E-4f).toInvariantString())

        assertEquals("1.1014938E-4", 1.1014938E-4f.toInvariantString())

        /* The smallest subnormal float. */
        assertEquals("1.4E-45", 1.4E-45f.toInvariantString())
    }

    @Test
    fun testFloatSpecialValues() {

        assertEquals("NaN", Float.NaN.toInvariantString())

        assertEquals("Infinity", Float.POSITIVE_INFINITY.toInvariantString())

        assertEquals("-Infinity", Float.NEGATIVE_INFINITY.toInvariantString())
    }
}
