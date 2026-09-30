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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPConst
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlin.coroutines.cancellation.CancellationException
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

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun readMetadata(xmp: String): MetadataSummary =
        tryWithImageReadException {
            buildMetadataSummary(XMPMetaFactory.parseFromString(xmp))
        }

    /**
     * Reads the packet like [readMetadata] and additionally reports which fields the
     * packet carries, independently of their value, plus the parsed packet itself.
     *
     * The packet is parsed once; the summary and the presence facts are derived from
     * that single parse.
     *
     * @throws ImageReadException When the packet cannot be parsed.
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun readMetadataDetails(xmp: String): XmpMetadataDetails =
        tryWithImageReadException {

            val xmpMeta = XMPMetaFactory.parseFromString(xmp)

            XmpMetadataDetails(
                metadata = buildMetadataSummary(xmpMeta),
                xmpMeta = xmpMeta,
                carriesFlag = xmpMeta.carriesFlag(),
                carriesKeywords = xmpMeta.doesPropertyExist(XMPConst.NS_DC, XMPConst.XMP_DC_SUBJECT),
                carriesPersonsInImage = xmpMeta.doesPropertyExist(
                    XMPConst.NS_IPTC_EXT,
                    XMPConst.XMP_IPTC_EXT_PERSON_IN_IMAGE
                ),
                carriesFaces = xmpMeta.doesPropertyExist(
                    XMPConst.NS_MWG_RS,
                    XMPConst.XMP_MWG_RS_REGION_LIST
                )
            )
        }

    /**
     * Whether the packet carries a flag property in any schema [XMPMeta.isFlagged] reads
     * the flag value from - an explicit "not flagged" counts as carried, like silence
     * counts as not carried.
     */
    private fun XMPMeta.carriesFlag(): Boolean =
        doesPropertyExist(XMPConst.NS_DM, XMPConst.FLAGGED_TAG_ADOBE_NAME) ||
            doesPropertyExist(XMPConst.NS_DM, XMPConst.FLAGGED_TAG_ADOBE_GOOD_NAME) ||
            doesPropertyExist(XMPConst.NS_ACDSEE, XMPConst.FLAGGED_TAG_ACDSEE_NAME) ||
            doesPropertyExist(XMPConst.NS_MYLIO, XMPConst.FLAGGED_TAG_MYLIO_NAME) ||
            doesPropertyExist(XMPConst.NS_NARRATIVE, XMPConst.FLAGGED_TAG_NARRATIVE_NAME)

    private fun buildMetadataSummary(xmpMeta: XMPMeta): MetadataSummary {

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
            } catch (ex: CancellationException) {
                throw ex
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
            faces = xmpMeta.getFaceRegions(),
            personsInImage = xmpMeta.getPersonsInImage()
        )
    }
}
