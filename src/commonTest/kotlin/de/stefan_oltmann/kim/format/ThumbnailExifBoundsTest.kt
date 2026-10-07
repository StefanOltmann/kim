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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.tiffContents
import de.stefan_oltmann.kim.testdata.tiffField
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Pins what the shared thumbnail-EXIF helper bounds: the thumbnail
 * itself. The total EXIF is deliberately unbounded here - chunk-based
 * containers (PNG/WebP/JXL) legally embed EXIF blocks beyond the JPEG
 * APP1 payload limit, and large pre-existing maker notes must not fail
 * an unrelated thumbnail replacement.
 */
class ThumbnailExifBoundsTest {

    @Test
    fun testLargePreExistingExifEmbedsWithSmallThumbnail() {

        val largeUserComment = "UNICODE".encodeToByteArray() + byteArrayOf(0) +
            ByteArray(70 * 1024) { index -> (index % 97).toByte() }

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.PNG,
            imageSize = null,
            exif = tiffContents(
                tiffField(
                    ExifTag.EXIF_TAG_USER_COMMENT,
                    largeUserComment,
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        /* A small, valid JPEG thumbnail (SOI + one byte). */
        val exifBytes = metadata.exifBytesWithThumbnail(
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x01)
        )

        /* The pre-existing EXIF block survives the rewrite. */
        assertTrue(exifBytes.size > 70 * 1024)
    }
}
