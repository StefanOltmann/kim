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
package de.stefan_oltmann.kim.format.webp.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.model.ImageSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WebPChunkVP8LTest {

    /**
     * Creates a VP8L chunk payload for the given size, alpha flag and version.
     *
     * Byte 0 is the signature 0x2F, bytes 1-4 contain a 32-bit little-endian
     * value: 14 bits width-1, 14 bits height-1, 1 bit alpha, 3 bits version.
     */
    @Suppress("MagicNumber")
    private fun createVp8LBytes(
        width: Int,
        height: Int,
        hasAlpha: Boolean,
        version: Int
    ): ByteArray {

        val value =
            (width - 1) or
                ((height - 1) shl 14) or
                (if (hasAlpha) 1 shl 28 else 0) or
                (version shl 29)

        return byteArrayOf(
            0x2F,
            (value and 0xFF).toByte(),
            (value shr 8 and 0xFF).toByte(),
            (value shr 16 and 0xFF).toByte(),
            (value shr 24 and 0xFF).toByte()
        )
    }

    @Test
    fun testParse() {

        val chunk = WebPChunkVP8L(
            createVp8LBytes(
                width = 100,
                height = 50,
                hasAlpha = true,
                version = 0
            )
        )

        assertEquals(ImageSize(100, 50), chunk.imageSize)
        assertTrue(chunk.hasAlpha)
        assertEquals(0, chunk.versionNumber)

        assertEquals(
            expected = "WebPChunk 'VP8L' (5 bytes) imageSize=100 x 50 hasAlpha=true versionNumber=0",
            actual = chunk.toString()
        )
    }

    @Test
    fun testParseWithoutAlpha() {

        val chunk = WebPChunkVP8L(
            createVp8LBytes(
                width = 1,
                height = 1,
                hasAlpha = false,
                version = 0
            )
        )

        assertEquals(ImageSize(1, 1), chunk.imageSize)
        assertEquals(false, chunk.hasAlpha)
    }

    @Test
    fun testParseRejectsInvalidVersion() {

        assertFailsWith<ImageReadException> {
            WebPChunkVP8L(
                createVp8LBytes(
                    width = 10,
                    height = 10,
                    hasAlpha = false,
                    version = 1
                )
            )
        }
    }

    @Test
    fun testParseRejectsIllegalDimensions() {

        assertFailsWith<ImageReadException> {
            WebPChunkVP8L(
                createVp8LBytes(
                    width = 16384,
                    height = 10,
                    hasAlpha = false,
                    version = 0
                )
            )
        }
    }

    @Test
    fun testParseRejectsTooShortChunk() {

        /* A 3-byte chunk does not contain the required 5-byte header. */
        assertFailsWith<ImageReadException> {
            WebPChunkVP8L(byteArrayOf(0x2F, 0x00, 0x00))
        }
    }
}
