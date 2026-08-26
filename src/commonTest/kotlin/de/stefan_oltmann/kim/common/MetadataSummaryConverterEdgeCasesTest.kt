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
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeRational
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonTag
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MetadataSummaryConverterEdgeCasesTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    private fun field(
        tag: de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo,
        bytes: ByteArray,
        fieldType: de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType<out Any> = FieldTypeAscii,
        count: Int = bytes.size,
        directoryType: Int = tag.directoryType?.typeId ?: TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffField = TiffField(
        offset = 0,
        tag = tag.tag,
        directoryType = directoryType,
        fieldType = fieldType,
        count = count,
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

    private fun gpsContents(vararg entries: TiffField): TiffContents {

        val gpsDirectory = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_GPS,
            entries = entries.toList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        return TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(gpsDirectory),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
    }

    private fun gpsRationalsBytes(): ByteArray =
        RationalNumbers(
            arrayOf(
                RationalNumber(1, 1),
                RationalNumber(2, 1),
                RationalNumber(3, 1)
            )
        ).toBytes(ByteOrder.BIG_ENDIAN)

    private fun tiffContentsWithThumbnail(thumbnailBytes: ByteArray): TiffContents {

        val thumbnailDirectory = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD1,
            entries = emptyList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        ).apply {
            this.thumbnailBytes = thumbnailBytes
        }

        return TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(thumbnailDirectory),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
    }

    @Test
    fun testIgnoreOrientation() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    TiffTag.TIFF_TAG_ORIENTATION,
                    TiffOrientation.ROTATE_RIGHT.value.toShort().toBytes(ByteOrder.BIG_ENDIAN),
                    FieldTypeShort,
                    1
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertEquals(
            expected = TiffOrientation.ROTATE_RIGHT,
            actual = metadata.convertToSummary().orientation
        )

        assertEquals(
            expected = TiffOrientation.STANDARD,
            actual = metadata.convertToSummary(ignoreOrientation = true).orientation
        )
    }

    @Test
    fun testTakenDateWithSubSecond() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:08:30 18:43:00.5\u0000".encodeToByteArray()
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNotNull(metadata.convertToSummary().takenDate)
    }

    @Test
    fun testTakenDateWithoutSubSecond() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:08:30 18:43:00\u0000".encodeToByteArray()
                ),
                field(
                    ExifTag.EXIF_TAG_SUB_SEC_TIME_ORIGINAL,
                    "500\u0000".encodeToByteArray()
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNotNull(metadata.convertToSummary().takenDate)
    }

    /**
     * SubSecTime is a digit string whose length encodes the fraction, so
     * "05" must yield 50 ms. Converting it through a number would strip
     * the leading zero and report 500 ms instead.
     */
    @Test
    fun testTakenDateSubSecondKeepsLeadingZeros() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "2020:08:30 18:43:00\u0000".encodeToByteArray()
                ),
                field(
                    ExifTag.EXIF_TAG_SUB_SEC_TIME_ORIGINAL,
                    "05\u0000".encodeToByteArray()
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val expectedTakenDate = LocalDateTime
            .parse("2020-08-30T18:43:00.05")
            .toInstant(TimeZone.of("GMT+02:00"))
            .toEpochMilliseconds()

        assertEquals(expectedTakenDate, metadata.convertToSummary().takenDate)
    }

    @Test
    fun testInvalidTakenDateIsIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                    "not a date\u0000".encodeToByteArray()
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().takenDate)
    }

    @Test
    fun testLocationFromIptc() {

        val iptc = IptcMetadata(
            records = listOf(
                IptcRecord(IptcTypes.CITY, "Rastede"),
                IptcRecord(IptcTypes.PROVINCE_STATE, "Niedersachsen"),
                IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME, "Deutschland")
            ),
            rawBlocks = emptyList()
        )

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = null,
            exifBytes = null,
            iptc = iptc,
            xmp = null
        )

        val locationShown = metadata.convertToSummary().locationShown

        assertNotNull(locationShown)
        assertNull(locationShown.name)
        assertNull(locationShown.street)
        assertEquals("Rastede", locationShown.city)
        assertEquals("Niedersachsen", locationShown.state)
        assertEquals("Deutschland", locationShown.country)
    }

    @Test
    fun testEmptyIptcLocationIsIgnored() {

        val iptc = IptcMetadata(
            records = listOf(
                IptcRecord(IptcTypes.CITY, "   ")
            ),
            rawBlocks = emptyList()
        )

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = null,
            exifBytes = null,
            iptc = iptc,
            xmp = null
        )

        assertNull(metadata.convertToSummary().locationShown)
    }

    @Test
    fun testFilmSimulationWithoutMake() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().filmSimulation)
    }

    @Test
    fun testIsoAbove32767IsNotNegative() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_ISO,
                    byteArrayOf(0xC8.toByte(), 0x00), // ISO 51200, big-endian
                    FieldTypeShort,
                    1
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertEquals(
            expected = 51200,
            actual = metadata.convertToSummary().iso
        )
    }

    @Test
    fun testPanasonicIsoAbove32767IsNotNegative() {

        /* Panasonic RW2 stores the ISO value in IFD0. */
        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContents(
                field(
                    ExifTag.EXIF_TAG_ISO_PANASONIC,
                    byteArrayOf(0xC8.toByte(), 0x00), // ISO 51200, big-endian
                    FieldTypeShort,
                    1
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertEquals(
            expected = 51200,
            actual = metadata.convertToSummary().iso
        )
    }

    @Test
    fun testMalformedGpsFieldsAreIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                field(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                /* Wrong type: SHORT instead of RATIONAL. */
                field(
                    GpsTag.GPS_TAG_GPS_LATITUDE,
                    shortArrayOf(1, 2, 3).toBytes(ByteOrder.BIG_ENDIAN),
                    FieldTypeShort,
                    3
                )
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().gpsCoordinates)
    }

    @Test
    fun testUnknownGpsRefIsIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                field(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "North".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LATITUDE, gpsRationalsBytes(), FieldTypeRational, 3),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().gpsCoordinates)
    }

    @Test
    fun testGpsWithWrongValueCountIsIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                field(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                /* Only two of the required three values. */
                field(
                    GpsTag.GPS_TAG_GPS_LATITUDE,
                    RationalNumbers(
                        arrayOf(RationalNumber(1, 1), RationalNumber(2, 1))
                    ).toBytes(ByteOrder.BIG_ENDIAN),
                    FieldTypeRational,
                    2
                ),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().gpsCoordinates)
    }

    @Test
    fun testValidGpsIsParsed() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                field(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LATITUDE, gpsRationalsBytes(), FieldTypeRational, 3),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        val gpsCoordinates = metadata.convertToSummary().gpsCoordinates

        assertNotNull(gpsCoordinates)
        assertEquals(
            expected = 1.0 + 2.0 / 60.0 + 3.0 / 3600.0,
            actual = gpsCoordinates.latitude
        )
        assertEquals(
            expected = 1.0 + 2.0 / 60.0 + 3.0 / 3600.0,
            actual = gpsCoordinates.longitude
        )
    }

    @Test
    fun testNonJpegThumbnailIsIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContentsWithThumbnail("not a jpeg".encodeToByteArray()),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().thumbnailImageSize)
    }

    @Test
    fun testTruncatedThumbnailIsIgnored() {

        /* A JPEG thumbnail that is cut off before the SOF segment. */
        val thumbnail = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(),
            0x00, 0x0A, 0x00
        )

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = tiffContentsWithThumbnail(thumbnail),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().thumbnailImageSize)
    }

    @Test
    fun testTakenDateWithSystemTimeZone() {

        Kim.defaultTimeZone = null

        try {

            val metadata = MediaMetadata(
                mediaFormat = MediaFormat.JPEG,
                imageSize = null,
                exif = tiffContents(
                    field(
                        ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL,
                        "2020:08:30 18:43:00\u0000".encodeToByteArray()
                    )
                ),
                exifBytes = null,
                iptc = null,
                xmp = null
            )

            /*
             * The local EXIF date must map to the epoch milliseconds of
             * the very same wall clock time in the platform time zone,
             * so the expected value is computed from that zone and
             * asserted exactly.
             */
            val expectedTakenDate = LocalDateTime.parse("2020-08-30T18:43:00")
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()

            assertEquals(
                expected = expectedTakenDate,
                actual = metadata.convertToSummary().takenDate
            )

        } finally {
            Kim.defaultTimeZone = null
        }
    }

    /**
     * Regression test: a Nikon Lens tag with zero divisors must not put
     * NaN or infinity into the lens model string.
     */
    @Test
    fun testNikonLensModelWithZeroDivisorsIsIgnored() {

        val makerNoteDirectory = TiffDirectory(
            type = TiffConstants.TIFF_MAKER_NOTE_NIKON,
            entries = listOf(
                TiffField(
                    offset = 0,
                    tag = NikonTag.LENS.tag,
                    directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON,
                    fieldType = FieldTypeRational,
                    count = 4,
                    localValue = null,
                    valueOffset = 0,
                    valueBytes = ByteArray(32),
                    byteOrder = ByteOrder.LITTLE_ENDIAN,
                    sortHint = 0
                )
            ),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN
        )

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.NEF,
            imageSize = null,
            exif = tiffContentsWithMakerNote(makerNoteDirectory),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().lensModel)
    }

    private fun tiffContentsWithMakerNote(makerNoteDirectory: TiffDirectory): TiffContents =

        TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(
                TiffDirectory(
                    type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
                    entries = emptyList(),
                    offset = 8,
                    nextDirectoryOffset = 0,
                    byteOrder = ByteOrder.LITTLE_ENDIAN
                )
            ),
            makerNoteDirectory = makerNoteDirectory,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
}
