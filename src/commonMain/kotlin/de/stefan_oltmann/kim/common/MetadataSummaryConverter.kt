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
package de.stefan_oltmann.kim.common

import de.stefan_oltmann.kim.Kim.underUnitTesting
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.JpegImageParser
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.GPSInfo
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.FujiFilmTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.xmp.XmpReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime

/*
 * This is a dedicated object with @JvmStatic methods
 * to provide a better API to pure Java projects.
 */
public object MetadataSummaryConverter {

    @JvmStatic
    @JvmOverloads
    @Suppress("LongMethod")
    public fun convertToSummary(
        mediaMetadata: MediaMetadata,
        ignoreOrientation: Boolean = false
    ): MetadataSummary {

        val xmpMetadata: MetadataSummary? = mediaMetadata.xmp?.let {
            XmpReader.readMetadata(it)
        }

        val orientation = if (ignoreOrientation)
            TiffOrientation.STANDARD
        else
            TiffOrientation.of(mediaMetadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())

        val takenDateMillis: Long? = xmpMetadata?.takenDate
            ?: extractTakenDateMillisFromExif(mediaMetadata)

        val gpsCoordinates: GpsCoordinates? = xmpMetadata?.gpsCoordinates
            ?: extractGpsCoordinatesFromExif(mediaMetadata)

        val cameraMake = mediaMetadata.findStringValue(TiffTag.TIFF_TAG_MAKE)
        val cameraModel = mediaMetadata.findStringValue(TiffTag.TIFF_TAG_MODEL)

        val lensMake = mediaMetadata.findStringValue(ExifTag.EXIF_TAG_LENS_MAKE)
        val lensModel = mediaMetadata.findStringValue(ExifTag.EXIF_TAG_LENS_MODEL)

        /* Look for ISO at the standard place and fall back to test RW2 logic. */
        val iso = mediaMetadata.findShortValue(ExifTag.EXIF_TAG_ISO)
            ?: mediaMetadata.findShortValue(ExifTag.EXIF_TAG_ISO_PANASONIC)

        val exposureTime = mediaMetadata.findDoubleValue(ExifTag.EXIF_TAG_EXPOSURE_TIME)
        val fNumber = mediaMetadata.findDoubleValue(ExifTag.EXIF_TAG_FNUMBER)
        val focalLength = mediaMetadata.findDoubleValue(ExifTag.EXIF_TAG_FOCAL_LENGTH)

        /* Extract Fujifilm film simulation from MakerNote */
        val filmSimulation = extractFilmSimulation(mediaMetadata)

        val keywords = xmpMetadata?.keywords?.ifEmpty {
            extractKeywordsFromIptc(mediaMetadata)
        } ?: extractKeywordsFromIptc(mediaMetadata)

        val iptcRecords = mediaMetadata.iptc?.records

        val title = xmpMetadata?.title ?: iptcRecords
            ?.find { it.iptcType == IptcTypes.OBJECT_NAME }
            ?.value

        val description = xmpMetadata?.description ?: iptcRecords
            ?.find { it.iptcType == IptcTypes.CAPTION_ABSTRACT }
            ?.value

        val location = xmpMetadata?.locationShown
            ?: extractLocationFromIptc(mediaMetadata)

        val thumbnailBytes = mediaMetadata.getExifThumbnailBytes()

        val thumbnailImageSize = thumbnailBytes?.let {
            JpegImageParser.getImageSize(
                ByteArrayByteReader(thumbnailBytes)
            )
        }

        /*
         * Embedded XMP metadata has higher priority than EXIF or IPTC
         * for certain fields because it's the newer format. Some fields
         * like rating, faces and persons in image are exclusive to XMP.
         *
         * Resolution, orientation and capture parameters (camera make,
         * iso, exposure time, etc.) are always taken from EXIF.
         */
        return MetadataSummary(
            mediaFormat = mediaMetadata.mediaFormat,
            widthPx = mediaMetadata.imageSize?.width,
            heightPx = mediaMetadata.imageSize?.height,
            orientation = orientation,
            takenDate = takenDateMillis,
            gpsCoordinates = gpsCoordinates,
            locationShown = location,
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            lensMake = lensMake,
            lensModel = lensModel,
            iso = iso?.toInt(),
            exposureTime = exposureTime,
            fNumber = fNumber,
            focalLength = focalLength,
            filmSimulation = filmSimulation,
            title = title,
            description = description,
            flagged = xmpMetadata?.flagged ?: false,
            rating = xmpMetadata?.rating,
            keywords = keywords,
            faces = xmpMetadata?.faces ?: emptyMap(),
            personsInImage = xmpMetadata?.personsInImage ?: emptySet(),
            thumbnailImageSize = thumbnailImageSize,
            thumbnailBytes = thumbnailBytes
        )
    }

    @JvmStatic
    private fun extractTakenDateAsIsoString(metadata: MediaMetadata): String? {

        val takenDateField = metadata.findTiffField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            ?: return null

        var takenDate = takenDateField.value as? String

        /*
         * Workaround in case that it's a String array.
         */
        if (takenDate == null)
            takenDate = takenDateField.toStringValue()

        if (isExifDateEmpty(takenDate))
            return null

        return convertExifDateToIso8601Date(takenDate)
    }

    @OptIn(ExperimentalTime::class)
    @JvmStatic
    private fun extractTakenDateMillisFromExif(
        metadata: MediaMetadata
    ): Long? {

        try {

            val takenDate = extractTakenDateAsIsoString(metadata) ?: return null

            val takenDateSubSecond = metadata
                .findStringValue(ExifTag.EXIF_TAG_SUB_SEC_TIME_ORIGINAL)
                ?.toIntOrNull()
                ?: 0

            /*
             * If the date string itself contains a sub second like "2020-08-30T18:43:00.500"
             * this should be used. We append it, if the string does not have a dot yet.
             */
            val takenDatePlusSubSecond = if (!takenDate.contains('.'))
                "$takenDate.$takenDateSubSecond"
            else
                takenDate

            val timeZone = if (underUnitTesting)
                TimeZone.of("GMT+02:00")
            else
                TimeZone.currentSystemDefault()

            return LocalDateTime
                .parse(takenDatePlusSubSecond)
                .toInstant(timeZone)
                .toEpochMilliseconds()

        } catch (_: Exception) {

            /*
             * Many photos contain wrong values here. We ignore this problem and hope
             * that another taken date source like embedded XMP has a valid date instead.
             */

            return null
        }
    }

    @JvmStatic
    private fun extractGpsCoordinatesFromExif(
        metadata: MediaMetadata
    ): GpsCoordinates? {

        val gpsDirectory = metadata.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)

        val gps = gpsDirectory?.let(GPSInfo::createFrom)

        val latitude = gps?.getLatitudeAsDegreesNorth()
        val longitude = gps?.getLongitudeAsDegreesEast()

        if (latitude == null || longitude == null)
            return null

        return GpsCoordinates(
            latitude = latitude,
            longitude = longitude
        )
    }

    @JvmStatic
    private fun extractKeywordsFromIptc(
        metadata: MediaMetadata
    ): Set<String> {

        return metadata.iptc?.records
            ?.filter { it.iptcType == IptcTypes.KEYWORDS }
            ?.map { it.value }
            ?.toSet()
            ?: emptySet()
    }

    @JvmStatic
    private fun extractLocationFromIptc(
        metadata: MediaMetadata
    ): LocationShown? {

        val iptcRecords = metadata.iptc?.records
            ?: return null

        val iptcCity = iptcRecords
            .find { it.iptcType == IptcTypes.CITY }
            ?.value

        val iptcState = iptcRecords
            .find { it.iptcType == IptcTypes.PROVINCE_STATE }
            ?.value

        val iptcCountry = iptcRecords
            .find { it.iptcType == IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME }
            ?.value

        /* Don't create an object if everything is NULL */
        if (iptcCity.isNullOrBlank() && iptcState.isNullOrBlank() && iptcCountry.isNullOrBlank())
            return null

        return LocationShown(
            name = null,
            street = null,
            city = iptcCity,
            state = iptcState,
            country = iptcCountry
        )
    }

    @JvmStatic
    private fun extractFilmSimulation(metadata: MediaMetadata): String? {

        /* Only check for Fujifilm cameras */
        val cameraMake = metadata.findStringValue(TiffTag.TIFF_TAG_MAKE)
            ?: return null

        if (!cameraMake.contains("FUJIFILM", ignoreCase = true))
            return null

        /* Try to read from MakerNote directory */
        val makerNoteDir = metadata.exif?.makerNoteDirectory
            ?: return null

        val filmModeField = makerNoteDir.findField(FujiFilmTag.FILM_MODE)
            ?: return null

        val filmModeValue = filmModeField.toShort()?.toInt() ?: return null

        return FujiFilmTag.getFilmModeName(filmModeValue)
    }
}

public fun MediaMetadata.convertToSummary(
    ignoreOrientation: Boolean = false
): MetadataSummary =
    MetadataSummaryConverter.convertToSummary(
        mediaMetadata = this,
        ignoreOrientation = ignoreOrientation
    )

