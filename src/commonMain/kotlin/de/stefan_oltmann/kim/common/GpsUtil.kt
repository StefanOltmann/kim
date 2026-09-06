/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Provides helpers to convert GPS coordinates.
 */
public object GpsUtil {

    internal const val MINUTES_PER_HOUR: Double = 60.0
    internal const val SECONDS_PER_HOUR: Double = 3600.0
    private const val SECONDS_PER_MINUTE: Double = 60.0
    private const val MAX_DDM_FRACTION_DIGITS: Int = 4
    private const val MAX_LATITUDE_DEGREES: Int = 90
    private const val MAX_LONGITUDE_DEGREES: Int = 180

    /**
     * Converts a GPS coordinate in DMS (Degrees, Minutes, Seconds) format
     * or DDM (Degrees, Decimal Minutes) to decimal degrees.
     *
     * Direction letters are accepted in upper and lower case.
     *
     * This method is designed to be robust and will not throw any errors.
     *
     * @param dms the GPS coordinate in DMS or DDM format.
     * @return the decimal value of the GPS coordinate, or null if the input is null or invalid.
     */
    @Suppress("MagicNumber")
    public fun dmsToDecimal(dms: String?): Double? {

        /* Blank values are illegal. */
        if (dms.isNullOrBlank())
            return null

        val normalized = dms.uppercase()

        val directionLetter = normalized.last()

        /* Proper dms ends with a direction letter. */
        if (directionLetter !in setOf('N', 'S', 'E', 'W'))
            return null

        val parts = normalized.split(",", "N", "S", "E", "W")

        /* Proper dms requires degrees and minutes. Only seconds are optional. */
        if (parts.size < 2)
            return null

        val degrees = parts[0].toDoubleOrNull() ?: return null
        val minutes = parts[1].toDoubleOrNull() ?: return null
        val seconds = if (parts.size >= 3) parts[2].toDoubleOrNull() ?: 0.0 else 0.0

        /*
         * Minutes and seconds of 60 or more are implausible and usually
         * a sign of corrupt data, so they are rejected instead of
         * silently producing an out-of-range coordinate.
         */
        if (minutes >= MINUTES_PER_HOUR || seconds >= SECONDS_PER_MINUTE)
            return null

        val direction = if (directionLetter == 'S' || directionLetter == 'W') -1 else 1

        return direction * (degrees + minutes / MINUTES_PER_HOUR + seconds / SECONDS_PER_HOUR)
    }

    /**
     * XMP requires geo data to be in DDM (Degrees, decimal minutes) format.
     */
    public fun decimalLatitudeToDDM(latitude: Double): String =
        toDdm(
            value = latitude,
            maxDegrees = MAX_LATITUDE_DEGREES,
            positiveDirection = "N",
            negativeDirection = "S"
        )

    /**
     * XMP requires geo data to be in DDM (Degrees, decimal minutes) format.
     */
    public fun decimalLongitudeToDDM(longitude: Double): String =
        toDdm(
            value = longitude,
            maxDegrees = MAX_LONGITUDE_DEGREES,
            positiveDirection = "E",
            negativeDirection = "W"
        )

    private fun toDdm(
        value: Double,
        maxDegrees: Int,
        positiveDirection: String,
        negativeDirection: String
    ): String {

        val direction = if (value >= 0) positiveDirection else negativeDirection

        /*
         * Clamp before splitting, so an out-of-range input cannot
         * produce an out-of-sphere DDM output: "90,30.0N" would decode
         * back to 90.5, which is outside the valid range.
         */
        val absoluteValue = abs(value).coerceIn(0.0, maxDegrees.toDouble())

        var degrees = absoluteValue.toInt()

        var minutes = (absoluteValue - degrees) * MINUTES_PER_HOUR

        val minutesRounded = minutes.roundTo(MAX_DDM_FRACTION_DIGITS)

        /*
         * The minutes can round up to 60, which is not a valid DDM value.
         * Carry the full minute over to the degrees instead.
         */
        if (minutesRounded >= MINUTES_PER_HOUR) {

            degrees++
            minutes = 0.0

        } else {

            minutes = minutesRounded
        }

        /*
         * The minutes are rendered platform-independently, because the
         * string is written into files - Kotlin/JS would drop the ".0"
         * of whole minutes and produce different bytes than the JVM.
         */
        return "$degrees,${minutes.toInvariantString()}$direction"
    }

    private fun Double.roundTo(numFractionDigits: Int): Double {
        val factor = 10.0.pow(numFractionDigits.toDouble())
        return (this * factor).roundToLong() / factor
    }
}
