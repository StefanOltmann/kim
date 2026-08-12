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
}
