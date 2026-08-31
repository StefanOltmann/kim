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
 * Renders the double exactly like the JVM's [Double.toString] on every
 * platform.
 *
 * The platform renderings differ: Kotlin/JS and Kotlin/Wasm render whole
 * numbers without a fraction digit ("300" instead of "300.0"), Kotlin/Wasm
 * carries the full digit expansion instead of the shortest digits
 * ("0.0031580000000000002" instead of "0.003158"), and the exponent
 * notation thresholds differ ("0.0001" instead of "1.0E-4"). Written
 * metadata and text dumps must be byte-identical on all platforms, so the
 * shortest digit sequence that round-trips back into the same double is
 * searched and re-assembled using the JVM rules: plain notation from
 * 1.0E-3 (inclusive) to 1.0E7 (exclusive), always with at least one
 * fraction digit, and "d.dddEn" notation outside that range.
 */
internal fun Double.toInvariantString(): String {

    /* Special values already render identically everywhere. */
    if (isNaN())
        return "NaN"

    if (this == Double.POSITIVE_INFINITY)
        return "Infinity"

    if (this == Double.NEGATIVE_INFINITY)
        return "-Infinity"

    /*
     * Zero is handled before the digit extraction, because the JS
     * rendering drops the sign of negative zero.
     */
    if (this == 0.0)
        return if (1.0 / this < 0) "-0.0" else "0.0"

    val absoluteValue = abs(this)

    val shortestDigits = extractShortestDigits(absoluteValue)

    var precision = 1

    var resultDigits = roundToSignificantDigits(shortestDigits, precision)

    var roundTrips = renderCandidate(resultDigits).toDouble() == absoluteValue

    while (!roundTrips && precision < DOUBLE_MAX_SIGNIFICANT_DIGITS) {

        precision++

        resultDigits = roundToSignificantDigits(shortestDigits, precision)

        roundTrips = renderCandidate(resultDigits).toDouble() == absoluteValue
    }

    val numberString = renderCandidate(resultDigits)

    return if (this < 0) "-$numberString" else numberString
}

/**
 * Renders digits with exponent using the notation rules of the JVM's
 * shortest rendering.
 */
private fun renderCandidate(digitsWithExponent: Pair<String, Int>): String {

    val digits = digitsWithExponent.first
    val exponent = digitsWithExponent.second

    return if (exponent in PLAIN_NOTATION_MIN_EXPONENT..PLAIN_NOTATION_MAX_EXPONENT)
        renderPlainNotation(digits, exponent)
    else
        renderExponentNotation(digits, exponent)
}

/**
 * The smallest exponent the JVM renders in plain notation.
 */
internal const val PLAIN_NOTATION_MIN_EXPONENT: Int = -3

/**
 * The exponent one above the largest the JVM renders in plain notation.
 */
internal const val PLAIN_NOTATION_MAX_EXPONENT: Int = 6

/**
 * Maximum number of significant digits a double can require to be
 * uniquely identified.
 */
private const val DOUBLE_MAX_SIGNIFICANT_DIGITS: Int = 17

/**
 * Rounds [digits] with [exponent] - as produced by
 * [extractShortestDigits] - to [precision] significant digits, with round
 * to nearest and ties to even, like the JVM's shortest rendering.
 */
internal fun roundToSignificantDigits(
    digitsWithExponent: Pair<String, Int>,
    precision: Int
): Pair<String, Int> {

    val digits = digitsWithExponent.first
    val exponent = digitsWithExponent.second

    if (digits.length <= precision)
        return digitsWithExponent

    val cutDigit = digits[precision]

    /* An exact tie has a cut digit of 5 followed only by zeros. */
    val restContainsNonZero = digits.drop(precision + 1).any { it != '0' }

    val lastKeptDigitIsOdd = (digits[precision - 1] - '0') % 2 == 1

    val roundUp = cutDigit > '5' ||
        (cutDigit == '5' && (restContainsNonZero || lastKeptDigitIsOdd))

    var rounded = digits.take(precision)

    var adjustedExponent = exponent

    if (roundUp) {

        /*
         * Rounding can carry through every digit, for example "999"
         * becoming "1000", which also shifts the exponent.
         */
        val incremented = (rounded.toLong() + 1L).toString()

        if (incremented.length > precision)
            adjustedExponent++

        rounded = incremented
    }

    /* Strip a trailing zero that a carry may have produced, like "10". */
    var end = rounded.length

    while (end > 1 && rounded[end - 1] == '0')
        end--

    return rounded.take(end) to adjustedExponent
}

/**
 * Extracts the significant digits and the exponent of the given positive
 * value, so that the value equals "d.ddd" × 10^exponent with exactly one
 * digit before the implied decimal point.
 *
 * The platform rendering is the shortest string that round-trips, so its
 * digits are identical on every platform - only the notation differs.
 */
internal fun extractShortestDigits(value: Double): Pair<String, Int> {

    val rendering = value.toString()

    val exponentSeparatorIndex = rendering.indexOfAny(charArrayOf('e', 'E'))

    val mantissa: String
    var exponent: Int

    if (exponentSeparatorIndex == -1) {

        mantissa = rendering
        exponent = 0

    } else {

        mantissa = rendering.substring(0, exponentSeparatorIndex)

        exponent = rendering.substring(exponentSeparatorIndex + 1)
            .removePrefix("+")
            .toInt()
    }

    val pointIndex = mantissa.indexOf('.')

    val allDigits: String
    val digitsBeforePoint: Int

    if (pointIndex == -1) {

        allDigits = mantissa
        digitsBeforePoint = mantissa.length

    } else {

        allDigits = mantissa.removeRange(pointIndex, pointIndex + 1)
        digitsBeforePoint = pointIndex
    }

    exponent += digitsBeforePoint - 1

    /*
     * Leading zeros of fractions like "0.0001" carry no information, but
     * they shift the exponent.
     */
    var first = 0

    while (first < allDigits.length - 1 && allDigits[first] == '0') {

        first++
        exponent--
    }

    /*
     * The ".0" suffix that whole numbers carry on the JVM is not a
     * significant digit.
     */
    var end = allDigits.length

    while (end > 1 && allDigits[end - 1] == '0')
        end--

    return allDigits.substring(startIndex = first, endIndex = end) to exponent
}

/**
 * Renders "d.ddd × 10^exponent" without an exponent, always with at least
 * one fraction digit, like the JVM does inside its plain notation range.
 */
internal fun renderPlainNotation(
    digits: String,
    exponent: Int
): String {

    /* More integer digits than significant digits: pad with zeros. */
    if (exponent >= digits.length - 1)
        return digits + "0".repeat(exponent - (digits.length - 1)) + ".0"

    /* Pure fraction: fill the gap behind the decimal point with zeros. */
    if (exponent < 0)
        return "0." + "0".repeat(-exponent - 1) + digits

    return digits.substring(0, exponent + 1) + "." + digits.substring(exponent + 1)
}

/**
 * Renders "d.ddd × 10^exponent" in the JVM's "d.dddEn" notation, always
 * with at least one fraction digit.
 */
internal fun renderExponentNotation(
    digits: String,
    exponent: Int
): String =

    digits[0] + "." +
        (if (digits.length == 1) "0" else digits.substring(1)) +
        "E$exponent"
