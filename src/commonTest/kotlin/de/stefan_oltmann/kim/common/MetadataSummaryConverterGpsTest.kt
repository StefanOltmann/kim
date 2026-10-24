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
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeRational
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.tiffDirectory
import de.stefan_oltmann.kim.testdata.tiffField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Pins how the summary derives GPS coordinates: which EXIF field shapes
 * are accepted, which are ignored, and that the XMP position wins over
 * the EXIF one.
 */
class MetadataSummaryConverterGpsTest {

    private fun gpsContents(vararg entries: TiffField): TiffContents =

        TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(
                tiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS, entries.toList())
            ),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )

    private fun gpsRationalsBytes(): ByteArray =
        RationalNumbers(
            arrayOf(
                RationalNumber(1, 1),
                RationalNumber(2, 1),
                RationalNumber(3, 1)
            )
        ).toBytes(ByteOrder.BIG_ENDIAN)

    @Test
    fun testMalformedGpsFieldsAreIgnored() {

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                /* Wrong type: SHORT instead of RATIONAL. */
                tiffField(
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
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "North".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE, gpsRationalsBytes(), FieldTypeRational, 3),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
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
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                /* Only two of the required three values. */
                tiffField(
                    GpsTag.GPS_TAG_GPS_LATITUDE,
                    RationalNumbers(
                        arrayOf(RationalNumber(1, 1), RationalNumber(2, 1))
                    ).toBytes(ByteOrder.BIG_ENDIAN),
                    FieldTypeRational,
                    2
                ),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
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
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE, gpsRationalsBytes(), FieldTypeRational, 3),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
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

    /**
     * Coordinates that parse cleanly as rationals but lie outside the
     * valid range are sanctioned garbage category 2: physically
     * meaningless, so the summary omits them instead of reporting them
     * as the photo's location.
     */
    @Test
    fun testOutOfRangeGpsIsOmitted() {

        val outOfRangeLatitude = RationalNumbers(
            arrayOf(
                RationalNumber(91, 1),
                RationalNumber(0, 1),
                RationalNumber(0, 1)
            )
        ).toBytes(ByteOrder.BIG_ENDIAN)

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE, outOfRangeLatitude, FieldTypeRational, 3),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
            ),
            exifBytes = null,
            iptc = null,
            xmp = null
        )

        assertNull(metadata.convertToSummary().gpsCoordinates)
    }

    /**
     * The summary reports the XMP position when the packet carries one,
     * even when the EXIF GPS directory holds coordinates too - the
     * reverse of the taken-date rule. This test pins that precedence so
     * a silent flip fails here.
     */
    @Test
    fun testGpsPrefersXmpOverExif() {

        val xmp = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:exif="http://ns.adobe.com/exif/1.0/"
                    exif:GPSLatitude="51,30.5N"
                    exif:GPSLongitude="0,7.5W"/>
              </rdf:RDF>
            </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        val metadata = MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = null,
            exif = gpsContents(
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF, "N".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, "E".encodeToByteArray()),
                tiffField(GpsTag.GPS_TAG_GPS_LATITUDE, gpsRationalsBytes(), FieldTypeRational, 3),
                tiffField(GpsTag.GPS_TAG_GPS_LONGITUDE, gpsRationalsBytes(), FieldTypeRational, 3)
            ),
            exifBytes = null,
            iptc = null,
            xmp = xmp
        )

        val gpsCoordinates = metadata.convertToSummary().gpsCoordinates

        assertNotNull(gpsCoordinates)
        assertEquals(
            expected = 51.0 + 30.5 / 60.0,
            actual = gpsCoordinates.latitude,
            absoluteTolerance = 1e-9
        )
        assertEquals(
            expected = -(7.5 / 60.0),
            actual = gpsCoordinates.longitude,
            absoluteTolerance = 1e-9
        )
    }
}
