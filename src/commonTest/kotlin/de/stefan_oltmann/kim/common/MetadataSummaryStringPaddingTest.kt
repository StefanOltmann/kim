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
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.model.MediaFormat
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
                field(TiffTag.TIFF_TAG_MAKE, "OLYMPUS IMAGING CORP.  ".encodeToByteArray()),
                field(TiffTag.TIFF_TAG_MODEL, "E-M10           ".encodeToByteArray())
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
                field(
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

    private fun field(
        tag: TagInfo,
        bytes: ByteArray
    ): TiffField = TiffField(
        offset = 0,
        tag = tag.tag,
        directoryType = tag.directoryType?.typeId ?: TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
        fieldType = de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii,
        count = bytes.size,
        localValue = null,
        valueOffset = 0,
        valueBytes = bytes,
        byteOrder = ByteOrder.BIG_ENDIAN,
        sortHint = 0
    )

    private fun tiffContents(vararg entries: TiffField): TiffContents =

        TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(
                TiffDirectory(
                    type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
                    entries = entries.toList(),
                    offset = 8,
                    nextDirectoryOffset = 0,
                    byteOrder = ByteOrder.BIG_ENDIAN
                )
            ),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
}
