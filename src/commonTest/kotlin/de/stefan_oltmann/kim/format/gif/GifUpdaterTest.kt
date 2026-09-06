/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GifUpdaterTest : AbstractUpdaterTest(
    format = "gif",
    testThumbnail = false
) {

    /**
     * A GIF without an image is not valid, so the update must be rejected
     * instead of writing unreachable metadata behind the trailer.
     */
    @Test
    fun testUpdateHeaderOnlyGifIsRejected() {

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = createHeaderOnlyGif(),
                update = MetadataUpdate.Title("Test")
            )
        }
    }

    /**
     * Deleting the metadata of a GIF without an image must be rejected as
     * well.
     */
    @Test
    fun testDeleteMetadataHeaderOnlyGifIsRejected() {

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(createHeaderOnlyGif())
        }
    }

    /**
     * The XMP application extension is a GIF89a feature, so the header of a
     * GIF87a file must be upgraded when XMP is written.
     */
    @Test
    fun testUpdateGif87aUpgradesHeaderVersion() {

        val updatedBytes = Kim.update(
            bytes = createGif87aWithImage(),
            update = MetadataUpdate.Title("Test")
        )

        val header = updatedBytes.copyOfRange(0, 6).decodeToString()

        assertEquals("GIF89a", header)

        /* The image must survive the upgrade. */
        val metadata = Kim.readMetadata(updatedBytes)!!

        assertEquals(ImageSize(1, 1), metadata.imageSize)
    }

    /**
     * GPS coordinates outside the valid range must be rejected on the XMP
     * path as well, so a GIF cannot end up with invalid GPS data while the
     * same update on a JPEG would throw.
     */
    @Test
    fun testUpdateRejectsInvalidGpsCoordinates() {

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = createGif87aWithImage(),
                update = MetadataUpdate.GpsCoordinates(
                    GpsCoordinates(latitude = 95.0, longitude = 8.0)
                )
            )
        }
    }

    /**
     * Builds a GIF that only consists of the header, the logical screen
     * descriptor, a comment extension and the trailer.
     */
    private fun createHeaderOnlyGif(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("GIF89a".encodeToByteArray())
        byteWriter.write(byteArrayOf(1, 0, 1, 0, 0, 0, 0)) /* Logical screen descriptor, no color table */
        byteWriter.write(byteArrayOf(0x21, 0xFE.toByte(), 0x02, 0x41, 0x42, 0x00)) /* Comment extension */
        byteWriter.write(byteArrayOf(GifConstants.GIF_TERMINATOR))

        return byteWriter.toByteArray()
    }

    /**
     * Builds a GIF87a file with a single 1x1 image.
     */
    private fun createGif87aWithImage(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("GIF87a".encodeToByteArray())
        byteWriter.write(byteArrayOf(1, 0, 1, 0, 0, 0, 0)) /* Logical screen descriptor, no color table */
        byteWriter.write(byteArrayOf(GifConstants.IMAGE_SEPARATOR))
        byteWriter.write(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0)) /* 1x1 image descriptor, no color table */
        byteWriter.write(byteArrayOf(2)) /* LZW minimum code size */
        byteWriter.write(byteArrayOf(2, 2, 0x44, 0)) /* Image data sub-chunks */
        byteWriter.write(byteArrayOf(GifConstants.GIF_TERMINATOR))

        return byteWriter.toByteArray()
    }

    /**
     * Comment extensions behind the first frame must be removed by
     * deleteMetadata, or deleted comments would remain in the file.
     */
    @Test
    fun testDeleteMetadataRemovesTrailingCommentExtension() {

        val deletedBytes = Kim.deleteMetadata(createGif89aWithTrailingCommentExtension())

        assertFalse(deletedBytes.decodeToString().contains(STALE_COMMENT))
    }

    /**
     * A comment extension behind the first frame must not survive an
     * update either, otherwise the file ends up with stale duplicates.
     */
    @Test
    fun testUpdateStripsTrailingCommentExtension() {

        val updatedBytes = Kim.update(
            bytes = createGif89aWithTrailingCommentExtension(),
            update = MetadataUpdate.Title("New title")
        )

        assertFalse(updatedBytes.decodeToString().contains(STALE_COMMENT))

        /* The new XMP metadata and the image must be present. */
        assertTrue(updatedBytes.decodeToString().contains("<x:xmpmeta"))
    }

    /**
     * Builds a GIF89a file with a single 1x1 image and a comment extension
     * behind it, a legal position that animated files use between frames.
     */
    private fun createGif89aWithTrailingCommentExtension(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("GIF89a".encodeToByteArray())
        byteWriter.write(byteArrayOf(1, 0, 1, 0, 0, 0, 0)) /* Logical screen descriptor, no color table */
        byteWriter.write(byteArrayOf(GifConstants.IMAGE_SEPARATOR))
        byteWriter.write(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0)) /* 1x1 image descriptor, no color table */
        byteWriter.write(byteArrayOf(2)) /* LZW minimum code size */
        byteWriter.write(byteArrayOf(2, 2, 0x44, 0)) /* Image data sub-chunks */

        val commentBytes = STALE_COMMENT.encodeToByteArray()

        byteWriter.write(
            byteArrayOf(
                GifConstants.EXTENSION_INTRODUCER,
                GifConstants.COMMENT_EXTENSION_LABEL,
                commentBytes.size.toByte()
            ) + commentBytes + byteArrayOf(GifConstants.BLOCK_TERMINATOR)
        )

        byteWriter.write(byteArrayOf(GifConstants.GIF_TERMINATOR))

        return byteWriter.toByteArray()
    }

    /**
     * XMP behind the first frame is legal but cannot be merged by the
     * streaming update, which only sees the chunks before the first
     * image. Dropping it silently would destroy the metadata, so the
     * update must fail loudly instead.
     */
    @Test
    fun testUpdateFailsOnXmpBehindFirstFrame() {

        val staleXmp =
            "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><dc:title>Keep me</dc:title></x:xmpmeta>"

        val xmpBytes = staleXmp.encodeToByteArray()

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("GIF89a".encodeToByteArray())
        byteWriter.write(byteArrayOf(1, 0, 1, 0, 0, 0, 0))
        byteWriter.write(byteArrayOf(GifConstants.IMAGE_SEPARATOR))
        byteWriter.write(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0))
        byteWriter.write(byteArrayOf(2))
        byteWriter.write(byteArrayOf(2, 2, 0x44, 0))

        /* XMP application extension behind the first frame. */
        byteWriter.write(byteArrayOf(GifConstants.EXTENSION_INTRODUCER, 0xFF.toByte()))
        byteWriter.write(11)
        byteWriter.write("XMP DataXMP".encodeToByteArray())

        var offset = 0

        while (offset < xmpBytes.size) {

            val chunkSize = minOf(255, xmpBytes.size - offset)

            byteWriter.write(chunkSize.toByte())
            byteWriter.write(xmpBytes.copyOfRange(offset, offset + chunkSize))

            offset += chunkSize
        }

        byteWriter.write(0)
        byteWriter.write(byteArrayOf(GifConstants.GIF_TERMINATOR))

        assertFailsWith<ImageWriteException> {
            Kim.update(bytes = byteWriter.toByteArray(), update = MetadataUpdate.Title("New title"))
        }
    }

    /**
     * An extension block with a label this library does not know must
     * survive an update byte-exact. Historically the parser skipped only
     * introducer and label, so the stream desynced and a 0x2C inside such
     * a chain was misread as the image separator, corrupting rewritten
     * files.
     */
    @Test
    fun testUpdatePreservesUnknownExtensionBeforeFirstFrame() {

        val updatedBytes = Kim.update(
            bytes = createGif89aWithUnknownExtension(),
            update = MetadataUpdate.Title("New title")
        )

        assertTrue(updatedBytes.containsBytes(UNKNOWN_EXTENSION_BYTES))

        /* The image data behind the extension must parse at its true position. */
        val metadata = Kim.readMetadata(updatedBytes)!!

        assertEquals(ImageSize(1, 1), metadata.imageSize)
        assertNotNull(metadata.xmp)
    }

    /**
     * deleteMetadata keeps everything except XMP and comments, so the
     * unknown extension must survive here as well.
     */
    @Test
    fun testDeleteMetadataPreservesUnknownExtensionBeforeFirstFrame() {

        val deletedBytes = Kim.deleteMetadata(createGif89aWithUnknownExtension())

        assertTrue(deletedBytes.containsBytes(UNKNOWN_EXTENSION_BYTES))

        val metadata = Kim.readMetadata(deletedBytes)!!

        assertEquals(ImageSize(1, 1), metadata.imageSize)
        assertEquals(null, metadata.xmp)
    }

    /**
     * Builds a GIF89a file with an extension of an unknown private label
     * before the first 1x1 frame. The payload deliberately contains the
     * image separator byte, which exposed the stream desync.
     */
    private fun createGif89aWithUnknownExtension(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("GIF89a".encodeToByteArray())
        byteWriter.write(byteArrayOf(1, 0, 1, 0, 0, 0, 0)) /* Logical screen descriptor, no color table */
        byteWriter.write(UNKNOWN_EXTENSION_BYTES)

        byteWriter.write(byteArrayOf(GifConstants.IMAGE_SEPARATOR))
        byteWriter.write(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0)) /* 1x1 image descriptor, no color table */
        byteWriter.write(byteArrayOf(2)) /* LZW minimum code size */
        byteWriter.write(byteArrayOf(2, 2, 0x44, 0)) /* Image data sub-chunks */
        byteWriter.write(byteArrayOf(GifConstants.GIF_TERMINATOR))

        return byteWriter.toByteArray()
    }

    /**
     * Returns whether the array contains the given byte sequence.
     */
    private fun ByteArray.containsBytes(needle: ByteArray): Boolean =
        (0..size - needle.size).any { index ->
            copyOfRange(index, index + needle.size).contentEquals(needle)
        }

    private companion object {

        const val STALE_COMMENT: String = "stale comment"

        /*
         * Extension introducer, unknown private label 0x99, one sub-block
         * of four bytes containing the image separator, block terminator.
         */
        val UNKNOWN_EXTENSION_BYTES: ByteArray =
            byteArrayOf(
                GifConstants.EXTENSION_INTRODUCER,
                0x99.toByte(),
                4,
                GifConstants.IMAGE_SEPARATOR,
                0x41,
                0x42,
                0x43,
                GifConstants.BLOCK_TERMINATOR
            )
    }
}
