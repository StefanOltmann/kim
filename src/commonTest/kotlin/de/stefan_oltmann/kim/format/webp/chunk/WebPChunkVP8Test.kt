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

class WebPChunkVP8Test {

    /**
     * Creates a VP8 chunk payload: 10 bytes header with a valid key frame
     * start code and the given dimensions and scales.
     */
    @Suppress("MagicNumber")
    private fun createVp8Bytes(
        width: Int,
        height: Int,
        horizontalScale: Int = 0,
        verticalScale: Int = 0
    ): ByteArray {

        val frameHeaderByte = 0x10.toByte()

        return byteArrayOf(
            frameHeaderByte,
            0x00, 0x00,
            0x9D.toByte(), 0x01, 0x2A,
            (width and 0xFF).toByte(),
            ((width shr 8) or (horizontalScale shl 6)).toByte(),
            (height and 0xFF).toByte(),
            ((height shr 8) or (verticalScale shl 6)).toByte()
        )
    }

    /**
     * The scale factors scale the display size per RFC 6386 section 9.2:
     * 100 x 50 becomes 125 x 40.
     */
    @Test
    fun testParseAppliesScaleFactors() {

        val chunk = WebPChunkVP8(
            createVp8Bytes(
                width = 100,
                height = 50,
                horizontalScale = 1,
                verticalScale = 2
            )
        )

        assertEquals(ImageSize(125, 40), chunk.imageSize)
        assertEquals(0, chunk.versionNumber)
        assertEquals(1, chunk.horizontalScale)
        assertEquals(2, chunk.verticalScale)

        assertEquals(
            expected = "WebPChunk 'VP8 ' (10 bytes) versionNumber=0 imageSize=125 x 40 " +
                "horizontalScale=1 verticalScale=2",
            actual = chunk.toString()
        )
    }

    /**
     * Without scale factors the stored dimension is reported as-is.
     */
    @Test
    fun testParseWithoutScaling() {

        val chunk = WebPChunkVP8(
            createVp8Bytes(width = 100, height = 50)
        )

        assertEquals(ImageSize(100, 50), chunk.imageSize)
        assertEquals(0, chunk.horizontalScale)
        assertEquals(0, chunk.verticalScale)
    }

    @Test
    fun testParseRejectsTooShortChunk() {

        assertFailsWith<ImageReadException> {
            WebPChunkVP8(byteArrayOf(0x10, 0, 0, 0, 0x9D.toByte(), 0x01, 0x2A, 0, 0))
        }
    }

    @Test
    fun testParseRejectsInterframe() {

        val bytes = createVp8Bytes(10, 10)
        bytes[0] = 0x11.toByte()

        assertFailsWith<ImageReadException> {
            WebPChunkVP8(bytes)
        }
    }

    @Test
    fun testParseRejectsNonDisplayFrame() {

        val bytes = createVp8Bytes(10, 10)
        bytes[0] = 0x00.toByte()

        assertFailsWith<ImageReadException> {
            WebPChunkVP8(bytes)
        }
    }

    @Test
    fun testParseRejectsInvalidSignature() {

        val bytes = createVp8Bytes(10, 10)
        bytes[3] = 0x00.toByte()

        assertFailsWith<ImageReadException> {
            WebPChunkVP8(bytes)
        }
    }
}
