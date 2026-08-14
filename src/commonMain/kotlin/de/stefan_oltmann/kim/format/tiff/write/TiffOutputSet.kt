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
package de.stefan_oltmann.kim.format.tiff.write

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.GpsUtil.MINUTES_PER_HOUR
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.RationalNumber.Companion.valueOf
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.toExifDateString
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.DEFAULT_TIFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A set of TIFF directories to be written.
 */
@Suppress("TooManyFunctions")
public class TiffOutputSet(
    public val byteOrder: ByteOrder = DEFAULT_TIFF_BYTE_ORDER
) {

    private val directories = mutableListOf<TiffOutputDirectory>()

    internal fun getOutputItems(tiffOffsetItems: TiffOffsetItems): List<TiffOutputItem> {

        val outputItems = mutableListOf<TiffOutputItem>()

        for (directory in directories)
            outputItems.addAll(directory.getOutputItems(tiffOffsetItems))

        return outputItems
    }

    public fun addDirectory(directory: TiffOutputDirectory): TiffOutputDirectory {

        if (findDirectory(directory.type) != null)
            throw ImageWriteException("Output set already contains a directory of that type.")

        /*
         * Sub-directories are inserted before the thumbnail directory, so
         * the embedded thumbnail stays the last block of the EXIF data.
         */
        if (directory.type < 0) {

            val thumbnailDirectoryIndex =
                directories.indexOfFirst { it.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1 }

            if (thumbnailDirectoryIndex >= 0)
                directories.add(thumbnailDirectoryIndex, directory)
            else
                directories.add(directory)

        } else {

            directories.add(directory)
        }

        return directory
    }

    public fun getDirectories(): List<TiffOutputDirectory> = directories

    public fun getOrCreateRootDirectory(): TiffOutputDirectory =
        findDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0) ?: addRootDirectory()

    public fun getOrCreateExifDirectory(): TiffOutputDirectory {

        /* The EXIF directory requires root directory. */
        getOrCreateRootDirectory()

        return findDirectory(TiffConstants.TIFF_DIRECTORY_EXIF) ?: addExifDirectory()
    }

    public fun getOrCreateThumbnailDirectory(): TiffOutputDirectory {

        /* The Thumbnail directory requires root directory. */
        getOrCreateRootDirectory()

        return findDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD1) ?: addThumbnailDirectory()
    }

    public fun getOrCreateGPSDirectory(): TiffOutputDirectory {

        /* The GPS directory requires EXIF directory */
        getOrCreateExifDirectory()

        return findDirectory(TiffConstants.TIFF_DIRECTORY_GPS) ?: addGPSDirectory()
    }

    public fun findDirectory(directoryType: Int): TiffOutputDirectory? =
        directories.find { it.type == directoryType }

    @OptIn(ExperimentalTime::class)
    public fun applyUpdate(update: MetadataUpdate) {

        val rootDirectory = getOrCreateRootDirectory()
        val exifDirectory = getOrCreateExifDirectory()

        when (update) {

            is MetadataUpdate.Orientation -> {

                rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
                rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, update.tiffOrientation.value.toShort())
            }

            is MetadataUpdate.TakenDate -> {

                exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
                exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED)

                if (update.takenDate != null) {

                    val timeZone = if (Kim.underUnitTesting)
                        TimeZone.of("GMT+02:00")
                    else
                        TimeZone.currentSystemDefault()

                    val exifDateString = Instant
                        .fromEpochMilliseconds(update.takenDate)
                        .toLocalDateTime(timeZone)
                        .toExifDateString()

                    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, exifDateString)
                    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED, exifDateString)
                }
            }

            is MetadataUpdate.Description -> {

                rootDirectory.removeField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)

                update.description?.let { description ->
                    rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION, description)
                }
            }

            is MetadataUpdate.GpsCoordinates -> {

                setGpsCoordinates(update.gpsCoordinates)
            }

            is MetadataUpdate.GpsCoordinatesAndLocationShown -> {

                setGpsCoordinates(update.gpsCoordinates)
            }

            else -> throw ImageWriteException("Can't perform update $update.")
        }
    }

    /**
     * Applies every update of the given set that can be represented in EXIF/TIFF
     * metadata and ignores all others.
     *
     * Returns whether at least one update was applied, which signals that the
     * EXIF data needs to be rewritten.
     */
    public fun applyUpdates(updates: Set<MetadataUpdate>): Boolean {

        var appliedAny = false

        for (update in updates) {

            if (!update.isExifUpdate)
                continue

            applyUpdate(update)

            appliedAny = true
        }

        return appliedAny
    }

    /**
     * Sets the provided thumbnail bytes to the thumbnail directory (IFD1).
     */
    public fun setThumbnailBytes(thumbnailBytes: ByteArray) {

        val thumbnailDirectory = getOrCreateThumbnailDirectory()

        thumbnailDirectory.setThumbnailBytes(thumbnailBytes)
    }

    /**
     * A convenience method to update GPS values in EXIF metadata.
     */
    public fun setGpsCoordinates(gpsCoordinates: GpsCoordinates?) {

        if (gpsCoordinates != null && !gpsCoordinates.isValid())
            throw ImageWriteException(
                "Invalid GPS coordinates: ${gpsCoordinates.latLongString}"
            )

        val gpsDirectory = getOrCreateGPSDirectory()

        /* First delete everything. */
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_VERSION_ID)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LATITUDE_REF)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LONGITUDE)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LATITUDE)

        if (gpsCoordinates == null)
            return

        /* Add the data back. */

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_VERSION_ID, GpsTag.GPS_VERSION)

        val longitudeRef = if (gpsCoordinates.longitude < 0) "W" else "E"
        val latitudeRef = if (gpsCoordinates.latitude < 0) "S" else "N"

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, longitudeRef)
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_LATITUDE_REF, latitudeRef)

        run {

            var value = abs(gpsCoordinates.longitude)

            val longitudeDegrees = value.toLong().toDouble()
            value %= 1.0
            value *= MINUTES_PER_HOUR

            val longitudeMinutes = value.toLong().toDouble()
            value %= 1.0
            value *= MINUTES_PER_HOUR

            val longitudeSeconds = value

            gpsDirectory.add(
                GpsTag.GPS_TAG_GPS_LONGITUDE,
                RationalNumbers(
                    arrayOf(
                        valueOf(longitudeDegrees),
                        valueOf(longitudeMinutes),
                        valueOf(longitudeSeconds)
                    )
                )
            )
        }

        run {

            var value = abs(gpsCoordinates.latitude)

            val latitudeDegrees = value.toLong().toDouble()

            value %= 1.0
            value *= MINUTES_PER_HOUR

            val latitudeMinutes = value.toLong().toDouble()

            value %= 1.0
            value *= MINUTES_PER_HOUR

            val latitudeSeconds = value

            gpsDirectory.add(
                GpsTag.GPS_TAG_GPS_LATITUDE,
                RationalNumbers(
                    arrayOf(
                        valueOf(latitudeDegrees),
                        valueOf(latitudeMinutes),
                        valueOf(latitudeSeconds)
                    )
                )
            )
        }
    }

    public fun findMakerNoteField(): TiffOutputField? =
        findField(ExifTag.EXIF_TAG_MAKER_NOTE.tag)

    public fun findField(tag: Int): TiffOutputField? =
        directories.firstNotNullOfOrNull { directory -> directory.findField(tag) }

    public fun addRootDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0, byteOrder))

    public fun addExifDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_DIRECTORY_EXIF, byteOrder))

    public fun addThumbnailDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD1, byteOrder))

    public fun addGPSDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_DIRECTORY_GPS, byteOrder))
}

/**
 * Returns whether this update can be represented in EXIF/TIFF metadata.
 *
 * This mirrors the updates supported by [TiffOutputSet.applyUpdate].
 */
internal val MetadataUpdate.isExifUpdate: Boolean
    get() = when (this) {
        is MetadataUpdate.Orientation,
        is MetadataUpdate.TakenDate,
        is MetadataUpdate.Description,
        is MetadataUpdate.GpsCoordinates,
        is MetadataUpdate.GpsCoordinatesAndLocationShown -> true

        else -> false
    }
