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
import kotlin.math.pow
import kotlin.math.sin

/**
 * MD5 message digest (RFC 1321).
 *
 * Pure Kotlin implementation, because the digest must be identical on all
 * platforms and there is no multiplatform MD5 in the stdlib. It is used to
 * compute and verify the GUID of Adobe extended XMP packets, where it
 * matches the reference behavior of ExifTool and the Adobe SDK.
 *
 * The magic numbers are the constants defined by RFC 1321; naming them
 * would move the implementation further away from the specification text
 * instead of closer. Correctness is enforced by cross-validation tests
 * against the JVM reference implementation.
 */
@Suppress("MagicNumber")
internal object Md5 {

    private val SHIFT_AMOUNTS = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    )

    /*
     * Computed from the sine table definition of RFC 1321 instead of being
     * hardcoded, so no transcription errors can slip in.
     */
    private val SINE_CONSTANTS = IntArray(64) { index ->
        (abs(sin((index + 1).toDouble())) * 2.0.pow(32.0)).toLong().toInt()
    }

    /**
     * Returns the 16-byte MD5 digest of the given input.
     */
    fun digest(input: ByteArray): ByteArray {

        var a0 = 0x67452301
        var b0 = 0xefcdab89.toInt()
        var c0 = 0x98badcfe.toInt()
        var d0 = 0x10325476

        val paddedLength = ((input.size + 8) / 64 + 1) * 64

        val message = ByteArray(paddedLength)

        input.copyInto(message)

        /* The padding starts with a single one bit. */
        message[input.size] = 0x80.toByte()

        /* The original length in bits as 64-bit little-endian value. */
        val bitLength = input.size.toLong() * 8

        for (i in 0 until 8)
            message[paddedLength - 8 + i] = (bitLength ushr (8 * i)).toByte()

        var chunkOffset = 0

        while (chunkOffset < paddedLength) {

            val chunk = IntArray(16)

            for (j in 0 until 16) {

                val base = chunkOffset + j * 4

                chunk[j] = (message[base].toInt() and 0xFF) or
                    ((message[base + 1].toInt() and 0xFF) shl 8) or
                    ((message[base + 2].toInt() and 0xFF) shl 16) or
                    ((message[base + 3].toInt() and 0xFF) shl 24)
            }

            var a = a0
            var b = b0
            var c = c0
            var d = d0

            for (i in 0 until 64) {

                val f: Int
                val g: Int

                when (i / 16) {
                    0 -> {
                        f = (b and c) or (b.inv() and d)
                        g = i
                    }

                    1 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * i + 1) % 16
                    }

                    2 -> {
                        f = b xor c xor d
                        g = (3 * i + 5) % 16
                    }

                    else -> {
                        f = c xor (b or d.inv())
                        g = (7 * i) % 16
                    }
                }

                val temp = d

                d = c
                c = b
                b = b + (a + f + chunk[g] + SINE_CONSTANTS[i]).rotateLeft(SHIFT_AMOUNTS[i])
                a = temp
            }

            a0 += a
            b0 += b
            c0 += c
            d0 += d

            chunkOffset += 64
        }

        return byteArrayOf(
            (a0).toByte(), (a0 ushr 8).toByte(), (a0 ushr 16).toByte(), (a0 ushr 24).toByte(),
            (b0).toByte(), (b0 ushr 8).toByte(), (b0 ushr 16).toByte(), (b0 ushr 24).toByte(),
            (c0).toByte(), (c0 ushr 8).toByte(), (c0 ushr 16).toByte(), (c0 ushr 24).toByte(),
            (d0).toByte(), (d0 ushr 8).toByte(), (d0 ushr 16).toByte(), (d0 ushr 24).toByte()
        )
    }
}
