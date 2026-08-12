/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package de.stefan_oltmann.kim.format.webp

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebpUpdaterTest : AbstractUpdaterTest("webp") {

    private val originalBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/updates_webp/original.webp").readBytes()

    /**
     * Verifies that deleting the metadata removes the EXIF and XMP chunks
     * and clears the VP8X flags, but keeps the ICCP chunk that affects how
     * the image is displayed.
     */
    @Test
    fun testDeleteMetadataKeepsIccChunkAndClearsVp8xFlags() {

        val newBytes = Kim.deleteMetadata(originalBytes)

        val chunkTypes = chunkTypes(newBytes)

        /* The ICC profile affects the display and must be kept. */
        assertTrue("ICCP" in chunkTypes)

        /* The EXIF and XMP chunks must be removed. */
        assertFalse("EXIF" in chunkTypes)
        assertFalse("XMP " in chunkTypes)

        val vp8xChunk = vp8xChunk(newBytes)

        /* The VP8X flags must match the remaining chunks. */
        assertTrue(vp8xChunk.hasIcc)
        assertFalse(vp8xChunk.hasExif)
        assertFalse(vp8xChunk.hasXmp)
    }

    /**
     * Returns the types of all chunks of the given WebP bytes.
     */
    private fun chunkTypes(webpBytes: ByteArray): Set<String> {

        val chunkTypes = mutableSetOf<String>()

        var offset = WebPConstants.RIFF_SIGNATURE.size +
            WebPConstants.CHUNK_SIZE_LENGTH +
            WebPConstants.WEBP_SIGNATURE.size

        while (offset + 8 <= webpBytes.size) {

            val chunkType = webpBytes.copyOfRange(offset, offset + WebPConstants.TPYE_LENGTH).decodeToString()

            val chunkSize = (webpBytes[offset + 4].toInt() and 0xFF) or
                ((webpBytes[offset + 5].toInt() and 0xFF) shl 8) or
                ((webpBytes[offset + 6].toInt() and 0xFF) shl 16) or
                ((webpBytes[offset + 7].toInt() and 0xFF) shl 24)

            chunkTypes.add(chunkType)

            offset += WebPConstants.TPYE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize + chunkSize % 2
        }

        return chunkTypes
    }

    /**
     * Returns the VP8X header chunk of the given WebP bytes.
     */
    private fun vp8xChunk(webpBytes: ByteArray): WebPChunkVP8X {

        var offset = WebPConstants.RIFF_SIGNATURE.size +
            WebPConstants.CHUNK_SIZE_LENGTH +
            WebPConstants.WEBP_SIGNATURE.size

        while (offset + 8 <= webpBytes.size) {

            val chunkType = webpBytes.copyOfRange(offset, offset + WebPConstants.TPYE_LENGTH).decodeToString()

            val chunkSize = (webpBytes[offset + 4].toInt() and 0xFF) or
                ((webpBytes[offset + 5].toInt() and 0xFF) shl 8) or
                ((webpBytes[offset + 6].toInt() and 0xFF) shl 16) or
                ((webpBytes[offset + 7].toInt() and 0xFF) shl 24)

            if (chunkType == "VP8X") {

                val payload = webpBytes.copyOfRange(
                    offset + WebPConstants.TPYE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH,
                    offset + WebPConstants.TPYE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize
                )

                return WebPChunkVP8X(payload)
            }

            offset += WebPConstants.TPYE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize + chunkSize % 2
        }

        error("WebP bytes contain no VP8X chunk.")
    }
}
