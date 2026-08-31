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
 * Renders the float exactly like the JVM's [Float.toString] on every
 * platform.
 *
 * Kotlin/JS and Kotlin/Wasm render floats through their double value:
 * whole numbers lose their ".0" and the digits carry double precision
 * ("0.9912109375" instead of "0.99121094"). Text dumps must be
 * byte-identical on all platforms, so the shortest digit sequence that
 * still round-trips through the float is searched and rendered with the
 * JVM float notation rules (same thresholds as the double rendering, see
 * [Double.toInvariantString]).
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

    /*
     * A float is uniquely identified by at most [FLOAT_MAX_SIGNIFICANT_DIGITS]
     * significant digits, so the shortest round-tripping digit count is
     * found by trial. The digits are computed from the double rendering,
     * which carries the exact float value with digits to spare.
     */
    val shortestDigits = extractShortestDigits(abs(this).toDouble())

    /*
     * The nearest-float comparison runs in double space, because
     * Kotlin/JS cannot convert a double back into the float it
     * represents - Float is a plain number there. The absolute value is
     * compared, because the candidate digits are positive - the sign is
     * applied to the final string only.
     */
    val floatAsDouble = abs(this).toDouble().roundToNearestFloat()

    /*
     * Subnormal floats render with at least two significant digits, like
     * the JVM does - its shortest-digit search never collapses them to a
     * single digit, even when one would round-trip (2^-149 renders as
     * "1.4E-45", not "1.0E-45").
     */
    val isSubnormal = abs(this) < SMALLEST_NORMAL_FLOAT

    var precision = if (isSubnormal) 2 else 1

    var resultDigits = roundToSignificantDigits(shortestDigits, precision)

    var roundTrips = candidateRoundTrips(resultDigits, floatAsDouble)

    while (!roundTrips && precision < FLOAT_MAX_SIGNIFICANT_DIGITS) {

        precision++

        resultDigits = roundToSignificantDigits(shortestDigits, precision)

        roundTrips = candidateRoundTrips(resultDigits, floatAsDouble)
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
 * same float as [floatAsDouble].
 */
private fun candidateRoundTrips(
    digitsWithExponent: Pair<String, Int>,
    floatAsDouble: Double
): Boolean {

    val candidate = renderFloatNotation(digitsWithExponent.first, digitsWithExponent.second)

    return candidate.toDouble().roundToNearestFloat() == floatAsDouble
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

/**
 * Rounds this double to the nearest float value - with round to nearest
 * even at the 24th significand bit - and returns the result as a double.
 *
 * This is the manual equivalent of the JVM's float conversion, which
 * Kotlin/JS cannot perform, because Float is a plain number there.
 *
 * The magic numbers are the field widths and biases of the IEEE 754
 * binary32 and binary64 formats; naming them would move the code further
 * away from the specification text instead of closer.
 */
@Suppress("MagicNumber")
private fun Double.roundToNearestFloat(): Double {

    val bits = toBits()

    val signBits = bits and Long.MIN_VALUE

    val biasedExponent = ((bits ushr 52) and 0x7FF).toInt()

    val fraction = bits and 0xF_FFFF_FFFF_FFFFL

    /* NaN and infinity transfer unchanged. */
    if (biasedExponent == 0x7FF)
        return this

    /* A double subnormal lies far below the smallest float. */
    if (biasedExponent == 0)
        return Double.fromBits(signBits)

    val valueExponent = biasedExponent - 1023

    /* Beyond the float range: infinity. */
    if (valueExponent > 127)
        return Double.fromBits(signBits or 0x7FF0_0000_0000_0000L)

    /*
     * The 52 fraction bits are reduced to the 23 float fraction bits.
     * Subnormal floats have their exponent fixed to -126 and therefore
     * shift further. The significand carries the implicit bit, so the
     * dropped part includes it for the larger subnormal shifts.
     */
    val shift = 29 + (if (valueExponent >= -126) 0 else (-126 - valueExponent))

    val significand = fraction or (1L shl 52)

    var kept = significand ushr shift

    val dropped = significand and ((1L shl shift) - 1)

    val half = 1L shl (shift - 1)

    if (dropped > half || (dropped == half && kept and 1L == 1L))
        kept++

    /*
     * A carry can overflow the significand, which bumps the exponent -
     * this also moves the smallest normal range up by one.
     */
    if (kept == 1L shl 24) {

        kept = kept ushr 1

        if (valueExponent >= -126)
            return Double.fromBits(
                signBits or ((valueExponent + 127 + 1).toLong() shl 23)
            )
    }

    if (valueExponent >= -126)
        return Double.fromBits(
            signBits or ((valueExponent + 127).toLong() shl 23) or (kept and 0x7F_FFFF)
        )

    /* Below the smallest subnormal float: zero. */
    if (kept == 0L)
        return Double.fromBits(signBits)

    /* A subnormal significand of 2^23 encodes exactly the smallest normal. */
    return Double.fromBits(signBits or kept)
}
