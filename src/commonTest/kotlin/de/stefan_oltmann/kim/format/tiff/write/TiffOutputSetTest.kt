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
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.model.GpsCoordinates
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TiffOutputSetTest {

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
}
