/*
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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.Kim.underUnitTesting
import de.stefan_oltmann.kim.common.GpsUtil
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPException
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime

/**
 * We only read metadata that the user is likely to change/correct
 * like orientation, keywords and GPS.
 *
 * We ignore capture parameters like iso, focal length and so
 * on because we prefer to get that from EXIF.
 */
public object XmpReader {

    /**
     * Matches a trailing ISO-8601 timezone, like +05:00, -05:00 or Z.
     */
    private val ISO8601_TIMEZONE_REGEX: Regex = Regex("[+-]\\d{2}:\\d{2}$|Z$")

    @OptIn(ExperimentalTime::class)
    @Suppress("LoopWithTooManyJumpStatements")
    @Throws(XMPException::class)
    @JvmStatic
    public fun readMetadata(xmp: String): MetadataSummary {

        val xmpMeta = XMPMetaFactory.parseFromString(xmp)

        /*
         * Read taken date
         */

        val takenDateIsoString = xmpMeta.getDateTimeOriginal()

        val timeZone = if (underUnitTesting)
            TimeZone.of("GMT+02:00")
        else
            TimeZone.currentSystemDefault()

        val takenDateIsoStringWithoutTimezone =
            takenDateIsoString?.replace(ISO8601_TIMEZONE_REGEX, "")

        val takenDate = takenDateIsoStringWithoutTimezone?.let {
            try {
                LocalDateTime.parse(it)
                    .toInstant(timeZone)
                    .toEpochMilliseconds()
            } catch (_: Exception) {
                /* We ignore invalid XMP DateTimeOriginal values. */
                null
            }
        }

        /*
         * Read location
         */

        val latitude = GpsUtil.dmsToDecimal(xmpMeta.getGpsLatitude())
        val longitude = GpsUtil.dmsToDecimal(xmpMeta.getGpsLongitude())

        val gpsCoordinates = if (latitude != null && longitude != null)
            GpsCoordinates(latitude, longitude)
        else
            null

        val locationShown = xmpMeta.getLocation()?.let {

            LocationShown(
                name = it.name,
                street = it.location,
                city = it.city,
                state = it.state,
                country = it.country
            )
        }

        /*
         * Compile into MetadataSummary object
         */

        return MetadataSummary(
            orientation = TiffOrientation.of(xmpMeta.getOrientation()),
            takenDate = takenDate,
            gpsCoordinates = gpsCoordinates,
            locationShown = locationShown,
            title = xmpMeta.getTitle(),
            description = xmpMeta.getDescription(),
            flagged = xmpMeta.isFlagged(),
            rating = xmpMeta.getRating()?.let { ExifRating.of(it) },
            keywords = xmpMeta.getKeywords().ifEmpty {
                xmpMeta.getAcdSeeKeywords()
            },
            faces = xmpMeta.getFaces(),
            personsInImage = xmpMeta.getPersonsInImage()
        )
    }
}
