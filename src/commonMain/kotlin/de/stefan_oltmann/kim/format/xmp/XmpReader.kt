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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPException
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.jvm.JvmStatic

/**
 * We only read metadata that the user is likely to change/correct
 * like orientation, keywords and GPS.
 *
 * We ignore capture parameters like iso, focal length and so
 * on because we prefer to get that from EXIF.
 */
public object XmpReader {

    @Throws(XMPException::class)
    @JvmStatic
    public fun readMetadata(xmp: String): MetadataSummary {

        val xmpMeta = XMPMetaFactory.parseFromString(xmp)

        /*
         * Read taken date
         */

        val timeZone = Kim.effectiveTimeZone

        val takenDate = xmpMeta.getDateTimeOriginal()?.let { date ->

            try {

                val localDateTime = LocalDateTime(
                    year = date.year,
                    month = Month(date.month),
                    day = date.day,
                    hour = date.hour,
                    minute = date.minute,
                    second = date.second,
                    nanosecond = date.nanosecond
                )

                /*
                 * An embedded offset is authoritative - use it for the
                 * epoch conversion instead of assuming the reader's zone.
                 */
                val utcOffset = date.utcOffsetMinutes?.let { UtcOffset(minutes = it) }

                val instant = utcOffset
                    ?.let { localDateTime.toInstant(it) }
                    ?: localDateTime.toInstant(timeZone)

                instant.toEpochMilliseconds()
            } catch (_: Exception) {
                /* We ignore invalid XMP DateTimeOriginal values. */
                null
            }
        }

        /*
         * Read location
         *
         * The XMP library parses both property values and validates the
         * range, so corrupt coordinates yield NULL instead of flowing
         * into the position.
         */

        val gpsCoordinates = xmpMeta.getGpsCoordinates()?.let {
            GpsCoordinates(it.latitude, it.longitude)
        }

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

        val faces = xmpMeta.getFaceRegions()
            .mapNotNull { region -> region.name?.let { name -> name to region.area } }
            .toMap()

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
            faces = faces,
            personsInImage = xmpMeta.getPersonsInImage()
        )
    }
}
