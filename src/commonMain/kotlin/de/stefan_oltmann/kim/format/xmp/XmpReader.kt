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
import de.stefan_oltmann.xmp.XMPRegionArea
import de.stefan_oltmann.xmp.XmpFaceRegion
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmStatic

/**
 * We only read metadata that the user is likely to change/correct
 * like orientation, keywords and GPS.
 *
 * We ignore capture parameters like iso, focal length and so
 * on because we prefer to get that from EXIF.
 */
public object XmpReader {

    private const val AREA_UNIT_NORMALIZED: String = "normalized"
    private const val AREA_UNIT_PIXEL: String = "pixel"

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
                /*
                 * Unusable XMP date values are dropped: illegal values
                 * (garbage category 3) and Adobe's legal partial forms
                 * like "2023-05", which the summary's epoch-millis model
                 * cannot represent without fabricating a day (garbage
                 * category 5). The raw packet stays untouched on the
                 * metadata object.
                 */
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
            faces = readFaceRegions(xmpMeta),
            personsInImage = xmpMeta.getPersonsInImage()
        )
    }

    /**
     * Reads the face regions of mwg-rs:Regions in stored order. Regions of
     * other types and regions with incomplete area data are skipped, and a
     * region without a name keeps a null name.
     *
     * Unlike xmpcore's getFaceRegions the stArea:unit of each area is
     * honored: pixel coordinates are converted to the normalized form the
     * summary model carries, using the dimensions of
     * mwg-rs:AppliedToDimensions.
     */
    private fun readFaceRegions(xmpMeta: XMPMeta): List<XmpFaceRegion> {

        val regionListExists = xmpMeta.doesPropertyExist(
            XMPConst.NS_MWG_RS,
            XMPConst.XMP_MWG_RS_REGION_LIST
        )

        if (!regionListExists)
            return emptyList()

        val regionCount = xmpMeta.countArrayItems(
            XMPConst.NS_MWG_RS,
            XMPConst.XMP_MWG_RS_REGION_LIST
        )

        if (regionCount == 0)
            return emptyList()

        val regions = mutableListOf<XmpFaceRegion>()

        for (index in 1..regionCount) {

            val prefix = "${XMPConst.XMP_MWG_RS_REGION_LIST}[$index]/mwg-rs"

            /* We only want faces. */
            if (xmpMeta.getPropertyString(XMPConst.NS_MWG_RS, "$prefix:Type") != XMPConst.XMP_MWG_RS_TYPE_FACE)
                continue

            val name = xmpMeta.getPropertyString(XMPConst.NS_MWG_RS, "$prefix:Name")

            val area = readArea(xmpMeta, prefix) ?: continue

            regions.add(XmpFaceRegion(name, area))
        }

        return regions
    }

    /**
     * Reads the area of one region and applies its stArea:unit, or returns
     * NULL when area data is missing - such regions are skipped like
     * xmpcore skips them.
     */
    private fun readArea(xmpMeta: XMPMeta, prefix: String): XMPRegionArea? {

        val xPos = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:x")
        val yPos = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:y")
        val width = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:w")
        val height = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:h")

        /* Skip regions with missing area data. */
        @Suppress("ComplexCondition")
        if (xPos == null || yPos == null || width == null || height == null)
            return null

        val unit = xmpMeta.getPropertyString(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:unit")

        return when (unit) {
            null, AREA_UNIT_NORMALIZED -> XMPRegionArea(xPos, yPos, width, height)
            AREA_UNIT_PIXEL -> normalizePixelArea(xmpMeta, xPos, yPos, width, height)
            else -> throw ImageReadException("The face region area unit '$unit' is not supported.")
        }
    }

    /**
     * Divides the pixel coordinates by the dimensions of
     * mwg-rs:AppliedToDimensions. The strict read policy fails the read
     * when the packet does not carry them - reporting pixel values as
     * normalized ones would be silently wrong data.
     */
    private fun normalizePixelArea(
        xmpMeta: XMPMeta,
        xPos: Double,
        yPos: Double,
        width: Double,
        height: Double
    ): XMPRegionArea {

        /*
         * AppliedToDimensions is a stDim:Dimensions struct, its fields
         * carry the stDim prefix unlike the stArea fields of an area.
         */
        val appliedWidth = xmpMeta.getPropertyDouble(
            XMPConst.NS_MWG_RS,
            "${XMPConst.XMP_MWG_RS_APPLIED_TO_DIMENSIONS}/stDim:w"
        )

        val appliedHeight = xmpMeta.getPropertyDouble(
            XMPConst.NS_MWG_RS,
            "${XMPConst.XMP_MWG_RS_APPLIED_TO_DIMENSIONS}/stDim:h"
        )

        if (appliedWidth == null || appliedHeight == null)
            throw ImageReadException(
                "The face region area is in pixels, but the packet does not carry " +
                    "the mwg-rs:AppliedToDimensions the areas apply to."
            )

        if (appliedWidth <= 0.0 || appliedHeight <= 0.0)
            throw ImageReadException(
                "The mwg-rs:AppliedToDimensions of the face regions are not " +
                    "usable: width $appliedWidth, height $appliedHeight."
            )

        return XMPRegionArea(
            xPos = xPos / appliedWidth,
            yPos = yPos / appliedHeight,
            width = width / appliedWidth,
            height = height / appliedHeight
        )
    }
}
