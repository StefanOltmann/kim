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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RationalNumberTest {

    @Test
    fun testConstructors() {

        /* Signed constructor. */
        val signed = RationalNumber(1, 2)
        assertEquals(1L, signed.numerator)
        assertEquals(2L, signed.divisor)
        assertFalse(signed.unsignedType)

        /* Unsigned constructor masks to 32 bits. */
        val unsigned = RationalNumber(-1, -1, unsignedType = true)
        assertEquals(0xFFFFFFFFL, unsigned.numerator)
        assertEquals(0xFFFFFFFFL, unsigned.divisor)
        assertTrue(unsigned.unsignedType)
    }

    @Test
    fun testDoubleValue() {

        assertEquals(0.5, RationalNumber(1, 2).doubleValue())
        assertEquals(-0.25, RationalNumber(-1, 4).doubleValue())
        assertEquals(0.0, RationalNumber(0, 7).doubleValue())

        /*
         * The IEEE 754 contract for zero divisors: callers must check
         * for finiteness before using the result.
         */
        assertTrue(RationalNumber(1, 0).doubleValue().isInfinite())
        assertTrue(RationalNumber(-1, 0).doubleValue().isInfinite())
        assertTrue(RationalNumber(0, 0).doubleValue().isNaN())
    }

    @Test
    fun testNegate() {

        /* Plain signed negation. */
        val negated = RationalNumber(1, 2).negate()
        assertEquals(-1L, negated.numerator)
        assertEquals(2L, negated.divisor)
        assertFalse(negated.unsignedType)

        /* Unsigned value whose high bit is set is negated via the gcd. */
        val large = RationalNumber(-1, 2, unsignedType = true)
        assertEquals(0xFFFFFFFFL, large.numerator)

        val negatedLarge = large.negate()
        assertEquals(-0xFFFFFFFFL, negatedLarge.numerator)
        assertEquals(2L, negatedLarge.divisor)
    }

    @Test
    fun testToString() {

        /* Zero divisor. */
        assertEquals("Invalid rational (1/0)", RationalNumber(1, 0).toString())

        /* Rounded to six fraction digits. */
        assertEquals("1/3 (0.333333)", RationalNumber(1, 3).toString())
        assertEquals("1/2 (0.5)", RationalNumber(1, 2).toString())
    }

    @Test
    fun testEqualsAndHashCode() {

        val first = RationalNumber(1, 2)
        val second = RationalNumber(1, 2)
        val third = RationalNumber(2, 4)
        val unsigned = RationalNumber(1, 2, unsignedType = true)

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())

        assertNotEquals(first, third)
        assertNotEquals(first, unsigned)
        assertNotEquals<Any>(first, "not a rational number")
        assertNotEquals<Any?>(first, null)

        /* Same instance. */
        assertEquals(first, first)
    }

    @Test
    fun testCreate() {

        /* Full reduction. */
        val reduced = RationalNumber.create(2, 4)
        assertEquals(RationalNumber(1, 2), reduced)

        /* Normalization of too-large values. */
        val normalized = RationalNumber.create(3L * Int.MAX_VALUE, 2L * Int.MAX_VALUE)
        assertTrue(normalized.numerator <= Int.MAX_VALUE.toLong())
    }

    @Test
    fun testCreateRejectsZeroDivisor() {

        assertFailsWith<IllegalStateException> {
            RationalNumber.create(1, 0)
        }
    }

    @Test
    fun testValueOfIntegerBounds() {

        assertEquals(RationalNumber(Int.MAX_VALUE, 1), RationalNumber.valueOf(Int.MAX_VALUE.toDouble()))
        assertEquals(RationalNumber(-Int.MAX_VALUE, 1), RationalNumber.valueOf(-Int.MAX_VALUE.toDouble()))
    }

    @Test
    fun testValueOfSimpleValues() {

        assertEquals(RationalNumber(0, 1), RationalNumber.valueOf(0.0))
        assertEquals(RationalNumber(1, 2), RationalNumber.valueOf(0.5))
        assertEquals(RationalNumber(2, 1), RationalNumber.valueOf(2.0))

        /* Negative values are negated. */
        val negative = RationalNumber.valueOf(-0.5)
        assertEquals(-1L, negative.numerator)
        assertEquals(2L, negative.divisor)
    }

    @Test
    fun testValueOfUsesSuccessiveApproximations() {

        /* Pi is approximated with a small error. */
        val pi = RationalNumber.valueOf(3.141592653589793)

        assertTrue(
            kotlin.math.abs(pi.doubleValue() - 3.141592653589793) < 1E-6,
            "Expected a close approximation of pi, but was ${pi.doubleValue()}"
        )
    }

    /**
     * Regression test: NaN and infinity are not representable as rational
     * numbers and must fail with a clear exception instead of producing
     * bogus results like 1/-1.
     */
    @Test
    fun testValueOfRejectsNonFiniteValues() {

        assertFailsWith<IllegalArgumentException> {
            RationalNumber.valueOf(Double.NaN)
        }

        assertFailsWith<IllegalArgumentException> {
            RationalNumber.valueOf(Double.POSITIVE_INFINITY)
        }

        assertFailsWith<IllegalArgumentException> {
            RationalNumber.valueOf(Double.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun testRationalNumbersEquality() {

        val first = RationalNumbers(arrayOf(RationalNumber(1, 2), RationalNumber(3, 4)))
        val second = RationalNumbers(arrayOf(RationalNumber(1, 2), RationalNumber(3, 4)))
        val third = RationalNumbers(arrayOf(RationalNumber(1, 2)))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, third)
        assertNotEquals<Any>(first, "not a collection")
        assertNotEquals<Any?>(first, null)

        /* Same instance. */
        assertEquals(first, first)

        /* toString delegates to the array. */
        assertEquals("[1/2 (0.5), 3/4 (0.75)]", first.toString())
    }
}
