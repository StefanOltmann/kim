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
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
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
            /* 0xFFFFFFFF */
withDeclaredRiffSize(originalBytes, -1)

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
     * Regression test: an animated WebP keeps every animation chunk on
     * a metadata update - a chunk iteration or padding bug would drop
     * or garble frames silently, and no other test reads animation
     * chunks at all.
     */
    @Test
    fun testUpdateAnimatedWebpPreservesAllFrameChunks() {

        val animatedBytes = KimTestData.getBytesOf(KimTestData.ANIMATED_WEBP_TEST_IMAGE_INDEX)

        val originalCounts = chunkTypeCounts(animatedBytes)

        assertTrue(originalCounts.getValue("ANMF") >= 2, "The fixture must be animated.")

        val updatedBytes = Kim.update(
            bytes = animatedBytes,
            updates = setOf(MetadataUpdate.Title("Animated"))
        )

        /* Every animation chunk must survive the rewrite unchanged. */
        assertEquals(originalCounts, chunkTypeCounts(updatedBytes))

        val updatedMetadata = assertNotNull(Kim.readMetadata(updatedBytes))

        assertTrue(
            updatedMetadata.xmp?.contains("Animated") == true,
            "The rewritten file must report the new XMP."
        )
    }

    /**
     * The reader length hint is caller-supplied and may understate the
     * content - like the RIFF size field, which the parser already
     * refuses to trust. The chunk walk must end at the delegate's real
     * end of data: a walk bounded by the hint would drop the metadata
     * chunks behind it silently on a rewrite.
     */
    @Test
    fun testUpdatePreservesChunksBehindUnderstatedContentLengthHint() {

        /*
         * The hint ends exactly where the EXIF chunk starts, so a walk
         * bounded by the hint never starts that chunk - and the EXIF
         * sits at the end, behind the image data.
         */
        val exifOffset = firstChunkOffset(originalBytes, "EXIF")

        val reader = UnderstatedHintByteReader(
            delegate = ByteArrayByteReader(originalBytes),
            hintedLength = exifOffset.toLong()
        )

        val byteWriter = ByteArrayByteWriter()

        Kim.update(
            byteReader = reader,
            byteWriter = byteWriter,
            updates = setOf(MetadataUpdate.Title("test"))
        )

        val originalMetadata = assertNotNull(Kim.readMetadata(originalBytes))
        val updatedMetadata = assertNotNull(Kim.readMetadata(byteWriter.toByteArray()))

        assertContentEquals(
            expected = originalMetadata.exifBytes,
            actual = updatedMetadata.exifBytes
        )
    }

    /**
     * A ByteReader whose length hint understates the content while the
     * delegate delivers every byte.
     */
    private class UnderstatedHintByteReader(
        private val delegate: ByteReader,
        private val hintedLength: Long
    ) : ByteReader {

        override val contentLength: Long = hintedLength

        override fun readByte(): Byte? = delegate.readByte()

        override fun readBytes(count: Int): ByteArray = delegate.readBytes(count)

        override fun close() = delegate.close()
    }

    /**
     * Counts the chunks of the given WebP file by type, in file order
     * independent form - only the counts are compared.
     */
    private fun chunkTypeCounts(webpBytes: ByteArray): Map<String, Int> {

        val counts = mutableMapOf<String, Int>()

        var offset = WEBP_SIGNATURE_TOTAL_LENGTH

        while (offset + WebPConstants.CHUNK_HEADER_LENGTH <= webpBytes.size) {

            val type = webpBytes.copyOfRange(
                offset,
                offset + WebPConstants.TYPE_LENGTH
            ).decodeToString()

            counts[type] = (counts[type] ?: 0) + 1

            val size = readChunkSize(webpBytes, offset)

            offset += WebPConstants.CHUNK_HEADER_LENGTH + size + size % 2
        }

        return counts
    }

    /**
     * Returns the file offset of the first chunk of the given type.
     */
    private fun firstChunkOffset(webpBytes: ByteArray, chunkType: String): Int {

        var offset = WEBP_SIGNATURE_TOTAL_LENGTH

        while (offset + WebPConstants.CHUNK_HEADER_LENGTH <= webpBytes.size) {

            val type = webpBytes.copyOfRange(
                offset,
                offset + WebPConstants.TYPE_LENGTH
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

            val byte = webpBytes[chunkOffset + WebPConstants.TYPE_LENGTH + index].toInt() and 0xFF

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
                offset + WebPConstants.TYPE_LENGTH
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
     * A VP8X header behind the first chunk belongs to a nonconformant
     * file. Inserting a fresh header would leave the stale one in place
     * with undeclared ICC or animation flags, so the rewrite refuses the
     * file instead of silently falsifying them.
     */
    @Test
    fun testUpdateRejectsVp8xBehindTheFirstChunk() {

        val movedBytes = moveVp8xBehindTheImageData(originalBytes)

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = movedBytes,
                updates = setOf(MetadataUpdate.Title("test"))
            )
        }

        assertTrue(
            exception.message?.contains("VP8X") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Moves the VP8X chunk of the file behind the image data chunk, the
     * nonconformant layout the rejection targets.
     */
    private fun moveVp8xBehindTheImageData(webpBytes: ByteArray): ByteArray {

        val vp8xOffset = firstChunkOffset(webpBytes, "VP8X")

        val vp8xSize = readChunkSize(webpBytes, vp8xOffset)

        val vp8xTotalLength = WebPConstants.CHUNK_HEADER_LENGTH + vp8xSize + vp8xSize % 2

        val vp8xChunk = webpBytes.copyOfRange(vp8xOffset, vp8xOffset + vp8xTotalLength)

        val withoutVp8x =
            webpBytes.copyOfRange(0, vp8xOffset) +
                webpBytes.copyOfRange(vp8xOffset + vp8xTotalLength, webpBytes.size)

        val imageDataOffset = firstChunkOffset(withoutVp8x, "VP8 ")

        val imageDataSize = readChunkSize(withoutVp8x, imageDataOffset)

        val imageDataTotalLength =
            WebPConstants.CHUNK_HEADER_LENGTH + imageDataSize + imageDataSize % 2

        val insertIndex = imageDataOffset + imageDataTotalLength

        return withoutVp8x.copyOfRange(0, insertIndex) +
            vp8xChunk +
            withoutVp8x.copyOfRange(insertIndex, withoutVp8x.size)
    }

    /**
     * A file whose VP8X denies the ICC profile while carrying an ICCP
     * chunk is as nonconformant as a stale EXIF flag: the flags must
     * describe the chunks that are actually written, so the rewrite
     * declares the profile the chunk list still carries.
     */
    @Test
    fun testUpdateDeclaresIccWhenVp8xFlagIsStaleButChunkIsPresent() {

        /* Sanity: the source carries the profile chunk. */
        assertTrue("ICCP" in chunkTypes(originalBytes))

        val staleFlagBytes = withVp8xFlags(originalBytes, hasIcc = false)

        /* Sanity: the flag now lies about the chunk. */
        assertFalse(vp8xChunk(staleFlagBytes).hasIcc)

        val updatedBytes = Kim.update(
            bytes = staleFlagBytes,
            updates = setOf(MetadataUpdate.Title("test"))
        )

        val updatedVp8x = vp8xChunk(updatedBytes)

        assertTrue(updatedVp8x.hasIcc, "The rewritten VP8X must declare the ICCP chunk.")

        assertTrue("ICCP" in chunkTypes(updatedBytes))
    }

    /**
     * Returns a copy of the given WebP bytes whose VP8X header is
     * rebuilt with the given ICC flag, keeping every other flag.
     */
    private fun withVp8xFlags(webpBytes: ByteArray, hasIcc: Boolean): ByteArray {

        val vp8xOffset = firstChunkOffset(webpBytes, "VP8X")

        val vp8x = vp8xChunk(webpBytes)

        val replacementPayload = WebPChunkVP8X.createBytes(
            hasIcc = hasIcc,
            hasAlpha = vp8x.hasAlpha,
            hasExif = vp8x.hasExif,
            hasXmp = vp8x.hasXmp,
            hasAnimation = vp8x.hasAnimation,
            imageSize = vp8x.imageSize
        )

        val vp8xTotalLength = WebPConstants.CHUNK_HEADER_LENGTH + replacementPayload.size

        val sizeBytes = byteArrayOf(
            (replacementPayload.size and 0xFF).toByte(),
            ((replacementPayload.size shr 8) and 0xFF).toByte(),
            ((replacementPayload.size shr 16) and 0xFF).toByte(),
            ((replacementPayload.size shr 24) and 0xFF).toByte()
        )

        return webpBytes.copyOfRange(0, vp8xOffset) +
            "VP8X".encodeToByteArray() +
            sizeBytes +
            replacementPayload +
            webpBytes.copyOfRange(vp8xOffset + vp8xTotalLength, webpBytes.size)
    }

    /**
     * A nonconformant legacy file (image chunk without VP8X) can carry an
     * ICCP chunk. The synthesized VP8X header must declare it - decoders
     * honor the profile only when the flag is set, so the rewritten file
     * would render differently from what the read reported.
     */
    @Test
    fun testUpdateLegacyFileWithIccChunkDeclaresIccInSynthesizedVp8x() {
        /*
         * The legacy layout the spec grew out of: the image chunk first,
         * the ICCP chunk behind it. The ICCP chunk moves to the end,
         * because without a VP8X it has no declared place before the
         * image data.
         */
        val iccpOffset = firstChunkOffset(originalBytes, "ICCP")

        val iccpSize = readChunkSize(originalBytes, iccpOffset)

        val iccpTotalLength = WebPConstants.CHUNK_HEADER_LENGTH + iccpSize + iccpSize % 2

        val iccpChunk = originalBytes.copyOfRange(iccpOffset, iccpOffset + iccpTotalLength)

        val legacyBytes = removeFirstChunk(
            removeFirstChunk(
                removeFirstChunk(
                    removeFirstChunk(originalBytes, "VP8X"),
                    "EXIF"
                ),
                "XMP "
            ),
            "ICCP"
        ) + iccpChunk

        /* Sanity: the image chunk leads, the profile is still carried. */
        assertEquals(setOf("VP8 ", "ICCP"), chunkTypes(legacyBytes))

        val updatedBytes = Kim.update(
            bytes = legacyBytes,
            updates = setOf(MetadataUpdate.Title("test"))
        )

        val updatedVp8x = vp8xChunk(updatedBytes)

        assertTrue(updatedVp8x.hasIcc, "The synthesized VP8X must declare the ICCP chunk.")

        assertTrue("ICCP" in chunkTypes(updatedBytes))
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

            val chunkType = webpBytes.copyOfRange(offset, offset + WebPConstants.TYPE_LENGTH).decodeToString()

            val chunkSize = (webpBytes[offset + 4].toInt() and 0xFF) or
                ((webpBytes[offset + 5].toInt() and 0xFF) shl 8) or
                ((webpBytes[offset + 6].toInt() and 0xFF) shl 16) or
                ((webpBytes[offset + 7].toInt() and 0xFF) shl 24)

            chunkTypes.add(chunkType)

            offset += WebPConstants.TYPE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize + chunkSize % 2
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

            val chunkType = webpBytes.copyOfRange(offset, offset + WebPConstants.TYPE_LENGTH).decodeToString()

            val chunkSize = (webpBytes[offset + 4].toInt() and 0xFF) or
                ((webpBytes[offset + 5].toInt() and 0xFF) shl 8) or
                ((webpBytes[offset + 6].toInt() and 0xFF) shl 16) or
                ((webpBytes[offset + 7].toInt() and 0xFF) shl 24)

            if (chunkType == "VP8X") {

                val payload = webpBytes.copyOfRange(
                    offset + WebPConstants.TYPE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH,
                    offset + WebPConstants.TYPE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize
                )

                return WebPChunkVP8X(payload)
            }

            offset += WebPConstants.TYPE_LENGTH + WebPConstants.CHUNK_SIZE_LENGTH + chunkSize + chunkSize % 2
        }

        error("WebP bytes contain no VP8X chunk.")
    }

    private companion object {

        /* "RIFF" signature, the 4-byte size field and the "WEBP" form type. */
        private const val WEBP_SIGNATURE_TOTAL_LENGTH: Int = 12
    }
}
