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

import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.tiffContents
import de.stefan_oltmann.kim.testdata.tiffField
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EXIF and IPTC strings are padded with trailing spaces or NUL bytes.
 * The summary reports them like ExifTool does: without the padding, and
 * with the EXIF ImageDescription as the description fallback.
 */
class MetadataSummaryStringPaddingTest {

    @Test
    fun testTrailingAsciiPaddingIsTrimmed() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.TIFF,
            imageSize = null,
            exif = tiffContents(
                tiffField(TiffTag.TIFF_TAG_MAKE, "OLYMPUS IMAGING CORP.  ".encodeToByteArray()),
                tiffField(TiffTag.TIFF_TAG_MODEL, "E-M10           ".encodeToByteArray())
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        assertEquals("OLYMPUS IMAGING CORP.", summary.cameraMake)
        assertEquals("E-M10", summary.cameraModel)
    }

    /**
     * When neither XMP nor IPTC carry a description, the EXIF
     * ImageDescription is used, like ExifTool fills its Composite
     * Description. This keeps the summary symmetric with
     * MetadataUpdate.Description, which writes exactly that tag.
     */
    @Test
    fun testDescriptionFallsBackToExifImageDescription() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.TIFF,
            imageSize = null,
            exif = tiffContents(
                tiffField(
                    TiffTag.TIFF_TAG_IMAGE_DESCRIPTION,
                    "OLYMPUS DIGITAL CAMERA          ".encodeToByteArray()
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        assertEquals("OLYMPUS DIGITAL CAMERA", summary.description)
    }
}
