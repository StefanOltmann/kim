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

import java.security.MessageDigest
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Cross-validates the pure Kotlin MD5 implementation against the reference
 * implementation of the JVM on every platform-relevant edge case. The GUID
 * of Adobe extended XMP data must be identical on all platforms, so any
 * deviation here would silently corrupt interoperability with ExifTool.
 */
class Md5JvmTest {

    @OptIn(ExperimentalStdlibApi::class)
    private fun referenceDigest(input: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(input).toHexString(HexFormat.UpperCase)

    private fun assertMatchesReference(input: ByteArray) {

        val expected = referenceDigest(input)

        val actual = Md5.digest(input).toHexString(HexFormat.UpperCase)

        assertEquals(
            expected = expected,
            actual = actual,
            message = "MD5 mismatch for input of ${input.size} bytes."
        )
    }

    /**
     * Lengths 0 to 200 walk through every padding boundary of the algorithm:
     * the 56/64 byte block edges where the length field spills into an
     * extra block, and multi-block inputs.
     */
    @Test
    fun testMatchesJavaForAllLengthsAroundBlockBoundaries() {

        val random = Random(SEED)

        for (length in 0..200)
            assertMatchesReference(ByteArray(length) { random.nextInt().toByte() })
    }

    /**
     * Binary data covering all 256 possible byte values, which exercises
     * sign handling in the byte-to-int conversions.
     */
    @Test
    fun testMatchesJavaForAllByteValues() {

        val input = ByteArray(256) { index -> index.toByte() }

        assertMatchesReference(input)
    }

    /**
     * Pseudo-random binary data far larger than a few blocks.
     */
    @Test
    fun testMatchesJavaForLargeBinaryInput() {

        val random = Random(SEED)

        val input = ByteArray(LARGE_INPUT_BYTES)

        random.nextBytes(input)

        assertMatchesReference(input)
    }

    /**
     * Text in UTF-8 encoding, matching how the digest is applied to the
     * extended XMP data in production.
     */
    @Test
    fun testMatchesJavaForUtf8Text() {

        val text = "Grüße aus Köln - extended XMP data with umlauts: äöüß " +
            "and some more text to cross at least one 64-byte block."

        assertMatchesReference(text.encodeToByteArray())
    }

    /**
     * Uniform byte patterns at every length around the padding boundaries,
     * where a broken length encoding or off-by-one would show up.
     */
    @Test
    fun testMatchesJavaForUniformPatternsAtBlockBoundaries() {

        val boundaryLengths = intArrayOf(
            54, 55, 56, 57, 63, 64, 65, 66,
            118, 119, 120, 121, 127, 128, 129, 130,
            183, 184, 185, 191, 192, 193, 194
        )

        for (length in boundaryLengths) {

            assertMatchesReference(ByteArray(length)) /* All zeros. */
            assertMatchesReference(ByteArray(length) { -1 }) /* All ones. */

            /* Alternating and incrementing patterns. */
            assertMatchesReference(ByteArray(length) { index -> (index * 7).toByte() })
            assertMatchesReference(ByteArray(length) { index -> if (index % 2 == 0) 0x55 else -102 })
        }
    }

    /**
     * Fuzz test with many pseudo-random inputs of random lengths up to two
     * full blocks beyond the padding boundaries. This is the strongest guard
     * against subtle implementation errors, because it compares against the
     * JVM reference on thousands of arbitrary byte sequences.
     */
    @Test
    fun testFuzzAgainstJavaReference() {

        val random = Random(FUZZ_SEED)

        repeat(FUZZ_ITERATIONS) {

            val length = random.nextInt(MAX_FUZZ_LENGTH + 1)

            val input = ByteArray(length)

            var buffer = random.nextInt()

            for (index in input.indices) {

                /* Refill lazily, so each byte costs only a shift. */
                if (index % 4 == 0)
                    buffer = random.nextInt()

                input[index] = (buffer ushr ((index % 4) * 8)).toByte()
            }

            assertMatchesReference(input)
        }
    }

    companion object {

        /* A fixed seed keeps failures reproducible. */
        private const val SEED: Long = 42

        private const val FUZZ_SEED: Long = 1337

        private const val FUZZ_ITERATIONS: Int = 5_000

        private const val MAX_FUZZ_LENGTH: Int = 2_048

        private const val LARGE_INPUT_BYTES: Int = 1_000_003
    }
}
