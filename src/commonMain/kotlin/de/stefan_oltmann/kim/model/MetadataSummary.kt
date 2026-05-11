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
package de.stefan_oltmann.kim.model

import de.stefan_oltmann.kim.common.KimValueFormatter
import de.stefan_oltmann.xmp.XMPRegionArea

/**
 * Represents a high-level summary of image metadata extracted from raw MediaMetadata.
 */
public data class MetadataSummary(

    val mediaFormat: MediaFormat? = null,

    /* Image resolution */
    val widthPx: Int? = null,
    val heightPx: Int? = null,

    /** tiff:Orientation */
    val orientation: TiffOrientation? = null,

    /* Capture parameters */
    val takenDate: Long? = null,
    val gpsCoordinates: GpsCoordinates? = null,
    val locationShown: LocationShown? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensMake: String? = null,
    val lensModel: String? = null,
    val iso: Int? = null,
    val exposureTime: Double? = null,
    val fNumber: Double? = null,
    val focalLength: Double? = null,

    /* Film simulation (Fujifilm specific) */
    val filmSimulation: String? = null,

    /* Title & Description */
    val title: String? = null,
    val description: String? = null,

    /* Ratings & Tags */
    val flagged: Boolean = false,
    val rating: ExifRating? = null,
    val keywords: Set<String> = emptySet(),

    /* Persons */
    val faces: Map<String, XMPRegionArea> = emptyMap(),
    val personsInImage: Set<String> = emptySet(),

    /* EXIF Thumbnail (IFD1) */
    val thumbnailImageSize: ImageSize? = null,
    val thumbnailBytes: ByteArray? = null

) {

    val megaPixelCount: Int
        get() = if (widthPx == null || heightPx == null)
            0
        else
            (widthPx * heightPx).div(KimValueFormatter.MEGA_PIXEL_COUNT)

    val locationDisplay: String?
        get() = locationShown?.displayString
            ?: gpsCoordinates?.let { "GPS: " + gpsCoordinates.latLongString }

    val cameraName: String?
        get() = KimValueFormatter.createCameraOrLensName(cameraMake, cameraModel)

    val lensName: String?
        get() = KimValueFormatter.createModifiedLensName(
            cameraName = cameraName,
            lensName = KimValueFormatter.createCameraOrLensName(lensMake, lensModel)
        )

    val cameraAndLensName: String?
        get() = KimValueFormatter.createCameraAndLensName(
            cameraName = cameraName,
            lensName = lensName
        )

    val orientedSize: ImageSize?
        get() = when {
            widthPx == null || heightPx == null -> null
            orientation?.hasFlippedDimensions() == true -> ImageSize(heightPx, widthPx)
            else -> ImageSize(widthPx, heightPx)
        }

    @Suppress("DataClassContainsFunctions")
    public fun isEmpty(): Boolean =
        this == emptySummary

    /**
     * Combine the current metadata with the given one,
     * but only replace fields that are NULL.
     *
     * We read metadata in a certain order where XMP
     * is more important than EXIF, and so on.
     *
     * If the other metadata is NULL the same object is returned.
     * This API is provided to chain it.
     */
    public fun merge(other: MetadataSummary?): MetadataSummary {

        if (other == null)
            return this

        return this.copy(

            mediaFormat = mediaFormat ?: other.mediaFormat,

            /* Image resolution */
            widthPx = widthPx ?: other.widthPx,
            heightPx = heightPx ?: other.heightPx,

            /** tiff:Orientation */
            orientation = orientation ?: other.orientation,

            /* Capture parameters */
            takenDate = takenDate ?: other.takenDate,
            gpsCoordinates = gpsCoordinates ?: other.gpsCoordinates,
            locationShown = locationShown ?: other.locationShown,
            cameraMake = cameraMake ?: other.cameraMake,
            cameraModel = cameraModel ?: other.cameraModel,
            lensMake = lensMake ?: other.lensMake,
            lensModel = lensModel ?: other.lensModel,
            iso = iso ?: other.iso,
            exposureTime = exposureTime ?: other.exposureTime,
            fNumber = fNumber ?: other.fNumber,
            focalLength = focalLength ?: other.focalLength,

            /* Film simulation (Fujifilm specific) */
            filmSimulation = filmSimulation ?: other.filmSimulation,

            /* Title & Description */
            title = title ?: other.title,
            description = description ?: other.description,

            /* Ratings & Tags */
            flagged = flagged || other.flagged,
            rating = rating ?: other.rating,
            keywords = keywords.ifEmpty { other.keywords },

            /* Persons */
            faces = faces.ifEmpty { other.faces },
            personsInImage = personsInImage.ifEmpty { other.personsInImage },

            /* EXIF Thumbnail (IFD1) */
            thumbnailImageSize = thumbnailImageSize ?: other.thumbnailImageSize,
            thumbnailBytes = thumbnailBytes ?: other.thumbnailBytes
        )
    }

    public companion object {

        public val emptySummary: MetadataSummary = MetadataSummary()
    }
}
