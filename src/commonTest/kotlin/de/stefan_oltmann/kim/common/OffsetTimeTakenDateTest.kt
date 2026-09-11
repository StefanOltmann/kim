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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.model.MediaFormat
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ExifTool derives the absolute date from DateTimeOriginal plus the
 * OffsetTime tags of the file itself. These tests pin that the offset
 * tags take priority over the time zone of the viewer.
 */
class OffsetTimeTakenDateTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    @Test
    fun testTakenDateUsesOffsetTimeOriginal() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:01:01 12:00:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                ),
                field(
                    ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL,
                    "+05:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        /* 12:00:00+05:00 is 07:00:00Z - not 10:00:00Z from the pinned zone. */
        assertEquals(1577862000000L, summary.takenDate)
    }

    /**
     * The generic OffsetTime tag applies when OffsetTimeOriginal is
     * missing.
     */
    @Test
    fun testTakenDateUsesGenericOffsetTime() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:01:01 12:00:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                ),
                field(
                    ExifTag.EXIF_TAG_OFFSET_TIME,
                    "-06:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        /* 12:00:00-06:00 is 18:00:00Z. */
        assertEquals(1577901600000L, summary.takenDate)
    }

    @Test
    fun testTakenDateFallsBackToTimeZoneWithoutOffsetTags() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:01:01 12:00:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        /* Without offset tags the pinned zone applies: 12:00+02:00. */
        assertEquals(1577872800000L, summary.takenDate)
    }

    /**
     * An offset tag that is not a valid "+HH:MM" value is ignored, so the
     * configured time zone decides instead of failing the date.
     */
    @Test
    fun testTakenDateIgnoresInvalidOffsetTime() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:01:01 12:00:00".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                ),
                field(
                    ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL,
                    "not an offset".encodeToByteArray(),
                    directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val summary = metadata.convertToSummary()

        assertEquals(1577872800000L, summary.takenDate)
    }

    private fun field(
        tag: TagInfo,
        bytes: ByteArray,
        directoryType: Int = tag.directoryType?.typeId ?: TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffField = TiffField(
        offset = 0,
        tag = tag.tag,
        directoryType = directoryType,
        fieldType = FieldTypeAscii,
        count = bytes.size,
        localValue = null,
        valueOffset = 0,
        valueBytes = bytes,
        byteOrder = ByteOrder.BIG_ENDIAN,
        sortHint = 0
    )

    private fun tiffContents(vararg entries: TiffField): TiffContents {

        val directory = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
            entries = entries.toList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val exifDirectory = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_EXIF,
            entries = entries.filter { it.directoryType == TiffConstants.TIFF_DIRECTORY_EXIF },
            offset = 100,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val directories = mutableListOf(directory)

        if (exifDirectory.entries.isNotEmpty())
            directories.add(exifDirectory)

        return TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = directories,
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
    }
}
