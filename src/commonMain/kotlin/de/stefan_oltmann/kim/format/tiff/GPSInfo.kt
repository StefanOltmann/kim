/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.GpsUtil.MINUTES_PER_HOUR
import de.stefan_oltmann.kim.common.GpsUtil.SECONDS_PER_HOUR
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag

internal data class GPSInfo(
    private val latitudeRef: String,
    private val longitudeRef: String,
    private val latitudeDegrees: Double,
    private val latitudeMinutes: Double,
    private val latitudeSeconds: Double,
    private val longitudeDegrees: Double,
    private val longitudeMinutes: Double,
    private val longitudeSeconds: Double
) {

    fun getLongitudeAsDegreesEast(): Double {

        val result =
            longitudeDegrees + longitudeMinutes / MINUTES_PER_HOUR + longitudeSeconds / SECONDS_PER_HOUR

        if (longitudeRef.trim().equals("e", ignoreCase = true))
            return result

        if (longitudeRef.trim().equals("w", ignoreCase = true))
            return -result

        throw ImageReadException("Unknown longitude ref: \"$longitudeRef\"")
    }

    fun getLatitudeAsDegreesNorth(): Double {

        val result =
            latitudeDegrees + latitudeMinutes / MINUTES_PER_HOUR + latitudeSeconds / SECONDS_PER_HOUR

        if (latitudeRef.trim().equals("n", ignoreCase = true))
            return result

        if (latitudeRef.trim().equals("s", ignoreCase = true))
            return -result

        throw ImageReadException("Unknown latitude ref: \"$latitudeRef\"")
    }

    companion object {

        /* A GPS coordinate has degrees, minutes and seconds */
        private const val GPS_DMS_COMPONENT_COUNT = 3

        fun createFrom(gpsDirectory: TiffDirectory): GPSInfo? {

            /*
             * Hostile files can store any tag with any type, so every field
             * is type-checked instead of cast. A mismatch degrades to
             * unknown GPS, like a missing field.
             */
            val latitudeRefField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LATITUDE_REF)

            val longitudeRefField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF)

            if (latitudeRefField?.value !is String || longitudeRefField?.value !is String)
                return null

            val latitudeRef = latitudeRefField.toStringValue()

            val longitudeRef = longitudeRefField.toStringValue()

            /*
             * The popular Android App "Aves Gallery" nullifies all GPS fields on export.
             */
            if (latitudeRef == "" || longitudeRef == "")
                return null

            val latitudeField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LATITUDE)
                ?: return null

            val longitudeField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LONGITUDE)
                ?: return null

            val latitude = latitudeField.value as? RationalNumbers
                ?: return null

            val longitude = longitudeField.value as? RationalNumbers
                ?: return null

            if (latitude.values.size != GPS_DMS_COMPONENT_COUNT ||
                longitude.values.size != GPS_DMS_COMPONENT_COUNT
            )
                throw ImageReadException("Expected three values for latitude and longitude.")

            return GPSInfo(
                latitudeRef = latitudeRef,
                longitudeRef = longitudeRef,
                latitudeDegrees = latitude.values[0].doubleValue(),
                latitudeMinutes = latitude.values[1].doubleValue(),
                latitudeSeconds = latitude.values[2].doubleValue(),
                longitudeDegrees = longitude.values[0].doubleValue(),
                longitudeMinutes = longitude.values[1].doubleValue(),
                longitudeSeconds = longitude.values[2].doubleValue()
            )
        }
    }
}
