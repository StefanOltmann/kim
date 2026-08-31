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

import kotlin.math.abs

/**
 * Renders the float as the shortest unique decimal that round-trips
 * through a float, using the JVM notation rules, on every platform.
 *
 * Kotlin/JS and Kotlin/Wasm render floats through their double value:
 * whole numbers lose their ".0" and the digits carry double precision
 * ("0.9912109375" instead of "0.99121094"). Text dumps must be
 * byte-identical on all platforms, so the shortest digit sequence that
 * still round-trips through the float is searched and rendered with the
 * JVM float notation rules (same thresholds as the double rendering, see
 * [Double.toInvariantString]).
 *
 * Attention: JDK 17's [Float.toString] may emit extra digits for some
 * values, for example "1.04036508E11" instead of "1.0403651E11". The
 * language specification asks for the shortest unique spelling, which
 * this function follows, matching JDK 21 and later.
 */
internal fun Float.toInvariantString(): String {

    /* Special values already render identically everywhere. */
    if (isNaN())
        return "NaN"

    if (this == Float.POSITIVE_INFINITY)
        return "Infinity"

    if (this == Float.NEGATIVE_INFINITY)
        return "-Infinity"

    /*
     * Zero is handled before the digit extraction, because the JS
     * rendering drops the sign of negative zero.
     */
    if (this == 0.0f)
        return if (1.0f / this < 0) "-0.0" else "0.0"

    val negative = this < 0

    val absolute = abs(this)

    /*
     * A float is uniquely identified by at most [FLOAT_MAX_SIGNIFICANT_DIGITS]
     * significant digits, so the shortest round-tripping digit count is
     * found by trial. The digits are computed from the double rendering,
     * which carries the exact float value with digits to spare.
     */
    val shortestDigits = extractShortestDigits(absolute.toDouble())

    /*
     * Round-trip checks compare IEEE bits, because Kotlin/JS stores
     * Float as a plain number and [String.toFloat] therefore has to go
     * through [Float.toBits] to recover binary32 rounding.
     */
    val originalBits = absolute.toBits()

    /*
     * Subnormal floats render with at least two significant digits, like
     * the JVM does - its shortest-digit search never collapses them to a
     * single digit, even when one would round-trip (2^-149 renders as
     * "1.4E-45", not "1.0E-45").
     */
    val isSubnormal = absolute < SMALLEST_NORMAL_FLOAT

    var precision = if (isSubnormal) 2 else 1

    var resultDigits = roundToSignificantDigits(shortestDigits, precision)

    var roundTrips = candidateRoundTrips(resultDigits, originalBits)

    while (!roundTrips && precision < FLOAT_MAX_SIGNIFICANT_DIGITS) {

        precision++

        resultDigits = roundToSignificantDigits(shortestDigits, precision)

        roundTrips = candidateRoundTrips(resultDigits, originalBits)
    }

    val numberString = renderFloatNotation(resultDigits.first, resultDigits.second)

    return if (negative) "-$numberString" else numberString
}

/**
 * Renders digits and exponent with the JVM float notation: plain notation
 * with at least one fraction digit inside [1.0E-3, 1.0E7) and "d.dddEn"
 * outside of it.
 */
private fun renderFloatNotation(
    digits: String,
    exponent: Int
): String =

    if (exponent in PLAIN_NOTATION_MIN_EXPONENT..PLAIN_NOTATION_MAX_EXPONENT)
        renderPlainNotation(digits, exponent)
    else
        renderExponentNotation(digits, exponent)

/**
 * Whether the decimal rendering of the candidate digits describes the
 * same float as [originalBits].
 */
private fun candidateRoundTrips(
    digitsWithExponent: Pair<String, Int>,
    originalBits: Int
): Boolean {

    val candidate = renderFloatNotation(digitsWithExponent.first, digitsWithExponent.second)

    return candidate.toFloat().toBits() == originalBits
}

/**
 * Maximum number of significant digits a float can require to be
 * uniquely identified.
 */
private const val FLOAT_MAX_SIGNIFICANT_DIGITS: Int = 9

/**
 * The smallest positive normal float value, 2^-126. Everything below is
 * a subnormal float.
 */
private const val SMALLEST_NORMAL_FLOAT: Float = 1.17549435E-38f

