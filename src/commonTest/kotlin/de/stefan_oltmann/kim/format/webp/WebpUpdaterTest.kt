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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebpUpdaterTest : AbstractUpdaterTest("webp") {

    private val originalBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/updates_webp/original.webp").readBytes()

    /**
     * Encoders that stream WebP files sometimes understate the RIFF size,
     * so metadata chunks appended behind the declared end are a real world
     * case. The parser must not trust the declared size, otherwise those
     * chunks would silently vanish on a rewrite.
     */
    @Test
    fun testUpdatePreservesExifBehindUnderstatedRiffSize() {

        val exifOffset = firstChunkOffset(originalBytes, "EXIF")

        /*
         * The declared size must end exactly before the EXIF chunk, so the
         * old parser stopped there: it is the sum of the bytes after the
         * size field up to the EXIF chunk plus the "WEBP" form type.
         */
        val declaredRiffSize =
            (exifOffset - WEBP_SIGNATURE_TOTAL_LENGTH) + WebPConstants.WEBP_SIGNATURE.size

        val understatedBytes = withDeclaredRiffSize(originalBytes, declaredRiffSize)

        /* The EXIF behind the declared end must be found on read. */
        val metadata = Kim.readMetadata(understatedBytes)

        assertNotNull(metadata?.exifBytes)

        val updatedBytes = Kim.update(
            bytes = understatedBytes,
            updates = setOf(MetadataUpdate.Title("test"))
        )

        /* The update must carry the EXIF over into the rewritten file. */
        val updatedMetadata = Kim.readMetadata(updatedBytes)

        assertContentEquals(
            expected = metadata.exifBytes,
            actual = updatedMetadata?.exifBytes
        )
    }

    /**
     * A RIFF size of 0xFFFFFFFF is invalid, but must not make the file
     * unparseable, because the chunk area is bounded by the content anyway.
     */
    @Test
    fun testReadMetadataWithInvalidMaxRiffSize() {

        val bogusSizeBytes =
            withDeclaredRiffSize(originalBytes, -1) /* 0xFFFFFFFF */

        val metadata = Kim.readMetadata(bogusSizeBytes)

        assertEquals(MediaFormat.WEBP, metadata?.mediaFormat)
        assertNotNull(metadata?.exifBytes)
    }

    /**
     * Returns a copy of the given WebP bytes with the declared RIFF size
     * replaced by the given value.
     */
    private fun withDeclaredRiffSize(webpBytes: ByteArray, declaredSize: Int): ByteArray {

        val result = webpBytes.copyOf()

        for (index in 0 until WebPConstants.CHUNK_SIZE_LENGTH)
            result[WebPConstants.RIFF_SIGNATURE.size + index] =
                ((declaredSize shr (index * Byte.SIZE_BITS)) and 0xFF).toByte()

        return result
    }

    /**
     * Returns the file offset of the first chunk of the given type.
     */
    private fun firstChunkOffset(webpBytes: ByteArray, chunkType: String): Int {

        var offset = WEBP_SIGNATURE_TOTAL_LENGTH

        while (offset + WebPConstants.CHUNK_HEADER_LENGTH <= webpBytes.size) {

            val type = webpBytes.copyOfRange(
                offset,
                offset + WebPConstants.TPYE_LENGTH
            ).decodeToString()

            if (type == chunkType)
                return offset

            val size = readChunkSize(webpBytes, offset)

            offset += WebPConstants.CHUNK_HEADER_LENGTH + size + size % 2
        }

        error("No $chunkType chunk found.")
    }

    private fun readChunkSize(webpBytes: ByteArray, chunkOffset: Int): Int {

        var size = 0

        for (index in 0 until WebPConstants.CHUNK_SIZE_LENGTH) {

            val byte = webpBytes[chunkOffset + WebPConstants.TPYE_LENGTH + index].toInt() and 0xFF

            size = size or (byte shl (index * Byte.SIZE_BITS))
        }

        return size
    }

    /**
     * Regression test: a file can claim EXIF in its VP8X flags without
     * carrying an EXIF chunk. The update must derive the flags from the
     * chunks that are actually written, so the stale EXIF flag does not
     * survive.
     */
    @Test
    fun testUpdateClearsStaleExifFlagWhenChunkIsMissing() {

        val bytesWithoutExif = removeFirstChunk(originalBytes, "EXIF")

        /* Sanity: the source really has the stale flag. */
        assertTrue(vp8xChunk(bytesWithoutExif).hasExif)

        val updatedBytes = Kim.update(
            bytes = bytesWithoutExif,
            updates = setOf(MetadataUpdate.Title("test"))
        )

        val updatedVp8x = vp8xChunk(updatedBytes)

        /* The stale flag must be gone ... */
        assertFalse(updatedVp8x.hasExif)

        /* ... while the written XMP is declared. */
        assertTrue(updatedVp8x.hasXmp)
    }

    /**
     * Returns a copy of the given WebP bytes with the first chunk of the
     * given type removed. The RIFF size field is left untouched, since it
     * is not trusted anyway.
     */
    private fun removeFirstChunk(webpBytes: ByteArray, chunkType: String): ByteArray {

        var offset = WEBP_SIGNATURE_TOTAL_LENGTH

        while (offset + WebPConstants.CHUNK_HEADER_LENGTH <= webpBytes.size) {

            val type = webpBytes.copyOfRange(
                offset,
                offset + WebPConstants.TPYE_LENGTH
            ).decodeToString()

            val size = readChunkSize(webpBytes, offset)

            val totalLength = WebPConstants.CHUNK_HEADER_LENGTH + size + size % 2

            if (type == chunkType)
                return webpBytes.copyOfRange(0, offset) +
                    webpBytes.copyOfRange(offset + totalLength, webpBytes.size)

            offset += totalLength
        }

        error("No $chunkType chunk found.")
    }

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
     * A WebP truncated inside its last chunk must be rejected, because
     * streaming the chunk payloads would otherwise end early and the output
     * would declare more chunk bytes than were written.
     */
    @Test
    fun testUpdateTruncatedWebpIsRejected() {

        /* Cut inside the last chunk payload. */
        val truncatedWebp = originalBytes.copyOfRange(0, originalBytes.size - 10)

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = truncatedWebp,
                updates = setOf(MetadataUpdate.Title("test"))
            )
        }
    }

    /**
     * A WebP truncated inside its last chunk must be rejected by the
     * deleteMetadata API as well.
     */
    @Test
    fun testDeleteMetadataTruncatedWebpIsRejected() {

        val truncatedWebp = originalBytes.copyOfRange(0, originalBytes.size - 10)

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(truncatedWebp)
        }
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

    private companion object {

        /* "RIFF" signature, the 4-byte size field and the "WEBP" form type. */
        private const val WEBP_SIGNATURE_TOTAL_LENGTH: Int = 12
    }
}
