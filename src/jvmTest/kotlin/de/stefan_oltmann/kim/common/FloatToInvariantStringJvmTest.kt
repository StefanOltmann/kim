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
 * Cross-checks the platform-invariant float rendering on the JVM.
 *
 * The golden literals in [DoubleExtensionsTest] pin individual spellings
 * on every platform. This sweep only checks that the invariant rendering
 * round-trips through [String.toFloat], because host [Float.toString]
 * is not a stable oracle: JDK 17 may emit extra digits for a value that
 * already has a shorter unique spelling (see
 * [JAVA17_EXTRA_DIGIT_FLOAT_BITS]).
 */
class FloatToInvariantStringJvmTest {

    @Test
    fun testRoundTripsThroughJvmFloatParsing() {

        val values = mutableListOf(
            -2.3007393E-4f,
            3.824234E-4f,
            -1.835823E-5f,
            2.3031235E-4f,
            -8.211136E-4f,
            1.1014938E-4f,
            0.99121094f,
            90.0f,
            0.1f,
            0.2f,
            1.0E-5f,
            1.0E-4f,
            9.999999f,
            1.0E7f,
            1.2345679E7f,
            1.0E-42f,
            3.4028235E38f,
            1.4E-45f,
            -0.0f,
            123456.78f,
            Float.fromBits(JAVA17_EXTRA_DIGIT_FLOAT_BITS)
        )

        var seed = 42L

        repeat(RANDOM_VALUE_COUNT) {

            seed = seed * RANDOM_SEED_MULTIPLIER + RANDOM_SEED_INCREMENT

            values.add(Float.fromBits(seed.toInt()))
        }

        for (value in values) {

            if (value.isNaN())
                continue

            val invariant = value.toInvariantString()

            assertEquals(
                expected = value.toBits(),
                actual = invariant.toFloat().toBits(),
                message = "bits=${value.toBits().toUInt().toString(2)} invariant=$invariant"
            )
        }
    }

    private companion object {

        /**
         * IEEE bits of 1.0403651E11. JDK 17 prints this with an extra digit.
         */
        const val JAVA17_EXTRA_DIGIT_FLOAT_BITS: Int = 0x51C1C879

        const val RANDOM_VALUE_COUNT: Int = 2000

        /* Arbitrary odd constants, so the low bits shuffle well. */
        const val RANDOM_SEED_MULTIPLIER: Long = 6364136223846793005L

        const val RANDOM_SEED_INCREMENT: Long = 1442695040888963407L
    }
}
