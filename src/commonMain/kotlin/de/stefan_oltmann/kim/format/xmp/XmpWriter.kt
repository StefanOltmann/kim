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
import de.stefan_oltmann.kim.common.GpsUtil
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.xmp.XMPConst
import de.stefan_oltmann.xmp.XMPException
import de.stefan_oltmann.xmp.XMPLocation
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory
import de.stefan_oltmann.xmp.options.SerializeOptions
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Writes XMP data.
 */
public object XmpWriter {

    private val xmpSerializeOptions =
        SerializeOptions()
            .setOmitXmpMetaElement(false)
            .setOmitPacketWrapper(false)
            .setUseCompactFormat(true)
            .setUseCanonicalFormat(false)
            .setSort(true)

    @OptIn(ExperimentalTime::class)
    @JvmStatic
    public fun XMPMeta.applyUpdate(update: MetadataUpdate) {

        when (update) {

            is MetadataUpdate.Orientation ->
                setOrientation(update.tiffOrientation.value)

            is MetadataUpdate.TakenDate -> {

                if (update.takenDate != null) {

                    val timeZone = Kim.defaultTimeZone ?: TimeZone.currentSystemDefault()

                    val isoDate = Instant
                        .fromEpochMilliseconds(update.takenDate)
                        .toLocalDateTime(timeZone)
                        .toString()

                    setDateTimeOriginal(isoDate)

                } else {

                    deleteDateTimeOriginal()
                }
            }

            is MetadataUpdate.GpsCoordinates -> {

                if (update.gpsCoordinates != null) {

                    requireValidGpsCoordinates(update.gpsCoordinates)

                    setGpsCoordinates(
                        GpsUtil.decimalLatitudeToDDM(update.gpsCoordinates.latitude),
                        GpsUtil.decimalLongitudeToDDM(update.gpsCoordinates.longitude)
                    )

                } else {

                    deleteGpsCoordinates()
                }
            }

            is MetadataUpdate.LocationShown -> {

                val locationShown = update.locationShown

                if (locationShown == null) {
                    setLocation(null)
                    return
                }

                setLocation(
                    XMPLocation(
                        name = locationShown.name,
                        location = locationShown.street,
                        city = locationShown.city,
                        state = locationShown.state,
                        country = locationShown.country
                    )
                )
            }

            is MetadataUpdate.GpsCoordinatesAndLocationShown -> {

                /* GPS */

                if (update.gpsCoordinates != null) {

                    requireValidGpsCoordinates(update.gpsCoordinates)

                    setGpsCoordinates(
                        GpsUtil.decimalLatitudeToDDM(update.gpsCoordinates.latitude),
                        GpsUtil.decimalLongitudeToDDM(update.gpsCoordinates.longitude)
                    )

                } else {

                    deleteGpsCoordinates()
                }

                /* Location */

                val locationShown = update.locationShown

                if (locationShown == null) {
                    setLocation(null)
                    return
                }

                setLocation(
                    XMPLocation(
                        name = locationShown.name,
                        location = locationShown.street,
                        city = locationShown.city,
                        state = locationShown.state,
                        country = locationShown.country
                    )
                )
            }

            is MetadataUpdate.Title ->
                setTitle(update.title)

            is MetadataUpdate.Description ->
                setDescription(update.description)

            is MetadataUpdate.Flagged -> {

                setFlagged(update.flagged)

                /*
                 * In the case of flagging/picking a photo a rejected
                 * rating will be reset to UNRATED for logical consistency.
                 */
                if (update.flagged && getRating() == ExifRating.REJECTED.value)
                    setRating(ExifRating.UNRATED.value)
            }

            is MetadataUpdate.Rating -> {

                setRating(update.exifRating.value)

                /*
                 * In the case of rejecting a photo a flag/pick marker
                 * will be removed for logical consistency.
                 */
                if (update.exifRating == ExifRating.REJECTED && isFlagged())
                    setFlagged(false)
            }

            is MetadataUpdate.Keywords ->
                setKeywords(update.keywords)

            is MetadataUpdate.Faces ->
                setFaces(update.faces, update.widthPx, update.heightPx)

            is MetadataUpdate.Persons ->
                setPersonsInImage(update.personsInImage)
        }
    }

    /**
     * Note: Parameter 'writePackageWrapper' should be "true" for embedded XMP.
     */
    @Throws(XMPException::class)
    @Suppress("LoopWithTooManyJumpStatements")
    @JvmStatic
    public fun updateXmp(
        xmpMeta: XMPMeta,
        updates: Set<MetadataUpdate>,
        writePackageWrapper: Boolean
    ): String {

        for (update in updates)
            xmpMeta.applyUpdate(update)

        deleteStaleExtendedXmpReference(xmpMeta)

        return xmpMeta.serializeToString(writePackageWrapper)
    }

    /**
     * Note: Parameter 'writePackageWrapper' should be "true" for embedded XMP.
     */
    @Throws(XMPException::class)
    @Suppress("LoopWithTooManyJumpStatements")
    @JvmStatic
    public fun updateXmp(
        xmpMeta: XMPMeta,
        update: MetadataUpdate,
        writePackageWrapper: Boolean
    ): String {

        xmpMeta.applyUpdate(update)

        deleteStaleExtendedXmpReference(xmpMeta)

        return xmpMeta.serializeToString(writePackageWrapper)
    }

    /**
     * Removes a stale "xmpNote:HasExtendedXMP" reference that was read from
     * the file. Kim regenerates the reference automatically when oversized
     * XMP is written, exactly like ExifTool, which deletes the tag because
     * "we create it as needed". A stale reference would point at extension
     * segments that no longer exist after a rewrite.
     */
    private fun deleteStaleExtendedXmpReference(xmpMeta: XMPMeta) {
        xmpMeta.deleteProperty(XMPConst.NS_XMP_NOTE, "HasExtendedXMP")
    }

    /**
     * Throws an [ImageWriteException] for coordinates outside the valid
     * range, so invalid GPS data is never written into the XMP.
     *
     * The same check guards the EXIF path in
     * [de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet.setGpsCoordinates].
     */
    private fun requireValidGpsCoordinates(gpsCoordinates: GpsCoordinates) {

        if (!gpsCoordinates.isValid())
            throw ImageWriteException(
                "Invalid GPS coordinates: ${gpsCoordinates.latLongString}"
            )
    }

    private fun XMPMeta.serializeToString(
        writePackageWrapper: Boolean
    ): String {

        /* We clone and modify the clone to prevent concurrency issues. */
        val options =
            xmpSerializeOptions.clone().setOmitPacketWrapper(!writePackageWrapper)

        return XMPMetaFactory.serializeToString(this, options)
    }
}
