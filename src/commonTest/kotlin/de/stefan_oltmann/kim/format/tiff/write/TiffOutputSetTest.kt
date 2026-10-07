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
package de.stefan_oltmann.kim.format.tiff.write

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TiffOutputSetTest {

    /**
     * The writer is the last line of defense against inconsistent
     * fields: an entry claiming more elements than its bytes hold
     * would be silently invalid for every consumer.
     */
    @Test
    fun testOutputFieldRejectsCountMismatch() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            TiffOutputField(
                tag = TiffTag.TIFF_TAG_IMAGE_DESCRIPTION.tag,
                fieldType = FieldTypeLong,
                count = 8,
                bytes = byteArrayOf(0, 0, 0, 0)
            )
        )

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)
        }
    }

    /**
     * Out-of-range coordinates must be rejected before they
     * are written to the GPS directory.
     */
    @Test
    fun testSetGpsCoordinatesRejectsOutOfRangeValues() {

        val outputSet = TiffOutputSet()

        assertFailsWith<ImageWriteException> {
            outputSet.setGpsCoordinates(GpsCoordinates(latitude = 91.0, longitude = 0.0))
        }

        assertFailsWith<ImageWriteException> {
            outputSet.setGpsCoordinates(GpsCoordinates(latitude = 0.0, longitude = 181.0))
        }

        assertFailsWith<ImageWriteException> {
            outputSet.setGpsCoordinates(GpsCoordinates(latitude = Double.NaN, longitude = 0.0))
        }

        /* Valid coordinates, including the boundaries, are accepted. */
        outputSet.setGpsCoordinates(GpsCoordinates(latitude = -90.0, longitude = 180.0))
    }

    /**
     * A NULL GpsCoordinates documents "remove the location". All GPS
     * fields must go - residual altitude, timestamps or a free-text
     * processing method would still expose the recorded place.
     */
    @Test
    fun testSetGpsCoordinatesNullRemovesAllGpsTags() {

        val outputSet = TiffOutputSet()

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 50.0, longitude = 8.0))

        val gpsDirectory = outputSet.getOrCreateGPSDirectory()

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_PROCESSING_METHOD, "Home, Riverside Drive")
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_ALTITUDE, RationalNumber(120, 1))
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_MAP_DATUM, "WGS-84")

        outputSet.setGpsCoordinates(null)

        /* No GPS field of any kind may survive the removal. */
        for (tag in listOf(
            GpsTag.GPS_TAG_GPS_LATITUDE,
            GpsTag.GPS_TAG_GPS_LONGITUDE,
            GpsTag.GPS_TAG_GPS_ALTITUDE,
            GpsTag.GPS_TAG_GPS_PROCESSING_METHOD,
            GpsTag.GPS_TAG_GPS_MAP_DATUM,
            GpsTag.GPS_TAG_GPS_TIME_STAMP,
            GpsTag.GPS_TAG_GPS_DATE_STAMP
        ))
            assertNull(gpsDirectory.findField(tag), "Field ${tag.name} survived the removal")
    }

    /**
     * GPSDestDistanceRef (0x0019) is a residual GPS companion like every
     * other ref tag: a position rewrite that removes GPSDestDistance but
     * leaves its ref behind would let a stale companion describe data
     * the file no longer carries.
     */
    @Test
    fun testPositionRewriteRemovesDestDistanceRef() {

        val outputSet = TiffOutputSet()

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 50.0, longitude = 8.0))

        outputSet.getOrCreateGPSDirectory().add(
            TiffOutputField(
                tag = 0x0019,
                fieldType = FieldTypeAscii,
                count = 2,
                bytes = "N\u0000".encodeToByteArray()
            )
        )

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 51.0, longitude = 9.0))

        assertNull(
            outputSet.findField(0x0019),
            "GPSDestDistanceRef survived the position rewrite"
        )
    }

    /**
     * Removing the position must not leave a GPS structure behind: the
     * writer registers a GPSInfo pointer for every present GPS directory,
     * so keeping the emptied directory would emit
     * IFD0 -> 0x8825 -> an empty GPS IFD - a "has location" signal with
     * no data, like ExifTool's GPS removal leaves none.
     */
    @Test
    fun testSetGpsCoordinatesNullDropsTheGpsDirectoryAndPointer() {

        val outputSet = TiffOutputSet()

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 50.0, longitude = 8.0))

        outputSet.setGpsCoordinates(null)

        val tiffBytes = outputSet.toTiffBytes()

        val contents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        val rootDirectory = assertNotNull(
            contents.directories.find { directory ->
                directory.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
            }
        )

        assertNull(rootDirectory.findField(ExifTag.EXIF_TAG_GPSINFO))

        assertNull(contents.directories.find { directory -> directory.type == TiffConstants.TIFF_DIRECTORY_GPS })
    }

    /**
     * A position rewrite yields a fresh GPS state: external writers
     * keep a fleet of companion properties next to the position, and
     * mixing the new coordinates with the old altitude, timestamps or
     * track would misdescribe the new position. The EXIF write path
     * must therefore clear them exactly like the XMP write path does.
     */
    @Test
    fun testSetGpsCoordinatesClearsResidualCompanions() {

        val outputSet = TiffOutputSet()

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 50.0, longitude = 8.0))

        val gpsDirectory = outputSet.getOrCreateGPSDirectory()

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_ALTITUDE, RationalNumber(120, 1))
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_ALTITUDE_REF, 0.toByte())
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_SATELLITES, "5")
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_TRACK, RationalNumber(270, 1))

        outputSet.setGpsCoordinates(GpsCoordinates(latitude = 51.0, longitude = 9.0))

        /* The new position must not be paired with the old companions. */
        for (tag in listOf(
            GpsTag.GPS_TAG_GPS_ALTITUDE,
            GpsTag.GPS_TAG_GPS_ALTITUDE_REF,
            GpsTag.GPS_TAG_GPS_SATELLITES,
            GpsTag.GPS_TAG_GPS_TRACK
        ))
            assertNull(gpsDirectory.findField(tag), "Field ${tag.name} survived the position rewrite")

        assertNotNull(gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LATITUDE))
    }

    /**
     * An empty thumbnail array would produce a
     * JPEGInterchangeFormat/Length pair with length 0, which the EXIF
     * spec defines as invalid. The write must reject it instead.
     */
    @Test
    fun testSetThumbnailBytesRejectsEmptyArray() {

        val outputSet = TiffOutputSet()

        val exception = assertFailsWith<ImageWriteException> {
            outputSet.setThumbnailBytes(ByteArray(0))
        }

        assertTrue(
            exception.message?.contains("empty") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Writing the taken date creates ExifIFD tags that Exif 2.3
     * validators require the ExifVersion for. Like ExifTool, the
     * version is added when the EXIF does not carry one yet.
     */
    @Test
    fun testTakenDateUpdateAddsExifVersionWhenMissing() {

        val outputSet = TiffOutputSet()

        outputSet.applyUpdate(MetadataUpdate.TakenDate(1700000000000L))

        val exifDirectory = assertNotNull(outputSet.findDirectory(TiffConstants.TIFF_DIRECTORY_EXIF))

        val exifVersion = assertNotNull(
            exifDirectory.findField(ExifTag.EXIF_TAG_EXIF_VERSION),
            "The ExifVersion was not added."
        )

        assertTrue(
            exifVersion.bytesEqual("0232".encodeToByteArray()),
            "Unexpected ExifVersion value."
        )
    }

    /**
     * The OffsetTime tags describe the offset of the replaced date. A
     * rewrite that keeps them makes every reader interpret the new date
     * in the old zone - Kim itself does - so they must be removed with
     * the date they belong to.
     */
    @Test
    fun testTakenDateUpdateRemovesStaleOffsetTime() {

        val outputSet = TiffOutputSet()

        val exifDirectory = outputSet.getOrCreateExifDirectory()

        exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2020:01:01 10:00:00")
        exifDirectory.add(ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL, "+05:00")
        exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED, "2020:01:01 10:00:00")
        exifDirectory.add(ExifTag.EXIF_TAG_OFFSET_TIME_DIGITIZED, "+05:00")

        outputSet.applyUpdate(MetadataUpdate.TakenDate(1700000000000L))

        assertNull(exifDirectory.findField(ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL))
        assertNull(exifDirectory.findField(ExifTag.EXIF_TAG_OFFSET_TIME_DIGITIZED))
    }
}
