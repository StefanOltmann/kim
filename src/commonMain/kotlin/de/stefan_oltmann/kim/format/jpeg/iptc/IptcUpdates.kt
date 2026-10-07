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
package de.stefan_oltmann.kim.format.jpeg.iptc

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.number
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Instant

/* Seconds of one hour, for the UTC offset of the IPTC time */
private const val SECONDS_PER_HOUR = 3600

/* Seconds of one minute, for the UTC offset of the IPTC time */
private const val SECONDS_PER_MINUTE = 60

/* Character count of the year field of the IPTC date */
private const val YEAR_STRING_LENGTH = 4

private val LOCATION_SHOWN_IPTC_TYPES: Set<IptcType> = setOf(
    IptcTypes.SUBLOCATION,
    IptcTypes.CITY,
    IptcTypes.PROVINCE_STATE,
    IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME
)

/**
 * The IPTC datasets 2:055 (DateCreated) and 2:060 (TimeCreated)
 * represent the taken date like EXIF DateTimeOriginal and XMP
 * exif:DateTimeOriginal do. An update that changes an IPTC-representable
 * field therefore rewrites the affected records - like ExifTool's MWG
 * mapping - so no storage reports the old values next to the new ones.
 *
 * Every format that stores the IPTC as the Photoshop image resource
 * structure (JPEG APP13 segments, PNG "Raw profile type iptc" text
 * chunks) shares this logic, so both carry identical records.
 */
internal fun createIptcMetadata(
    iptc: IptcMetadata?,
    updates: Set<MetadataUpdate>
): IptcMetadata? {

    val iptcUpdates = updates.filter { update ->
        update is MetadataUpdate.Title ||
            update is MetadataUpdate.Description ||
            update is MetadataUpdate.TakenDate ||
            update is MetadataUpdate.LocationShown ||
            update is MetadataUpdate.GpsCoordinatesAndLocationShown ||
            update is MetadataUpdate.Keywords
    }

    if (iptcUpdates.isEmpty())
        return null

    val newBlocks = iptc?.nonIptcBlocks ?: emptyList()
    val oldRecords = iptc?.records ?: emptyList()

    val removedIptcTypes = mutableSetOf<IptcType>()
    val newRecords = mutableListOf<IptcRecord>()

    for (update in iptcUpdates) {

        when (update) {

            is MetadataUpdate.Title -> {

                removedIptcTypes.add(IptcTypes.OBJECT_NAME)

                update.title?.let { title ->
                    newRecords.add(IptcRecord(IptcTypes.OBJECT_NAME, title))
                }
            }

            is MetadataUpdate.Description -> {

                removedIptcTypes.add(IptcTypes.CAPTION_ABSTRACT)

                update.description?.let { description ->
                    newRecords.add(IptcRecord(IptcTypes.CAPTION_ABSTRACT, description))
                }
            }

            is MetadataUpdate.TakenDate -> {

                /*
                 * The IPTC datasets 2:055 and 2:060 represent the
                 * taken date like EXIF and XMP do - like ExifTool's
                 * MWG mapping, they are rewritten with the new date
                 * or removed with it.
                 */
                removedIptcTypes.add(IptcTypes.DATE_CREATED)
                removedIptcTypes.add(IptcTypes.TIME_CREATED)

                val epochMilliseconds = update.takenDate

                if (epochMilliseconds != null) {

                    val timeZone = Kim.effectiveTimeZone

                    val instant = Instant.fromEpochMilliseconds(epochMilliseconds)

                    val localDateTime = instant.toLocalDateTime(timeZone)

                    newRecords.add(
                        IptcRecord(
                            IptcTypes.DATE_CREATED,
                            localDateTime.toIptcDateString()
                        )
                    )

                    newRecords.add(
                        IptcRecord(
                            IptcTypes.TIME_CREATED,
                            localDateTime.toIptcTimeString(timeZone.offsetAt(instant))
                        )
                    )
                }
            }

            is MetadataUpdate.LocationShown -> {

                removedIptcTypes.addAll(LOCATION_SHOWN_IPTC_TYPES)

                update.locationShown?.let { locationShown ->
                    newRecords.addAll(createLocationShownRecords(locationShown))
                }
            }

            is MetadataUpdate.GpsCoordinatesAndLocationShown -> {

                removedIptcTypes.addAll(LOCATION_SHOWN_IPTC_TYPES)

                update.locationShown?.let { locationShown ->
                    newRecords.addAll(createLocationShownRecords(locationShown))
                }
            }

            is MetadataUpdate.Keywords -> {

                removedIptcTypes.add(IptcTypes.KEYWORDS)

                for (keyword in update.keywords.sorted())
                    newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keyword))
            }

            else -> throw ImageWriteException("Can't perform update $update.")
        }
    }

    val remainingRecords = oldRecords.filter { record -> record.iptcType !in removedIptcTypes }

    /*
     * The rewrite must remove the segments the parsed stream came
     * from, so its identity is carried through the update. The
     * foreign datasets are carried so the rewrite re-emits them
     * instead of silently dropping them.
     */
    return IptcMetadata(
        remainingRecords + newRecords,
        newBlocks,
        iptc?.sourceSegmentBytes ?: emptyList(),
        iptc?.foreignDatasets ?: emptyList()
    )
}

/**
 * Replaces the data of the Photoshop IPTCDigest resource (0x0425,
 * 16 raw MD5 bytes) with the given digest, so the MWG sync
 * indicator matches the rewritten IPTC data - like ExifTool
 * maintains it when the IPTC is written.
 */
internal fun IptcMetadata.withIptcDigestResource(digestBytes: ByteArray): IptcMetadata {

    if (nonIptcBlocks.isEmpty())
        return this

    val blocks = nonIptcBlocks.map { block ->
        if (block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST &&
            block.blockData.size == digestBytes.size
        )
            IptcBlock(block.blockType, block.blockNameBytes, digestBytes)
        else
            block
    }

    return IptcMetadata(records, blocks, sourceSegmentBytes, foreignDatasets)
}

/**
 * The IPTC dataset 2:055 carries the local date as YYYYMMDD.
 */
private fun LocalDateTime.toIptcDateString(): String {

    val paddedMonth = month.number.toString().padStart(2, '0')
    val paddedDay = day.toString().padStart(2, '0')

    return "${year.toString().padStart(YEAR_STRING_LENGTH, '0')}$paddedMonth$paddedDay"
}

/**
 * The IPTC dataset 2:060 carries the local time as HHMMSS followed
 * by the UTC offset, so the capture time stays unambiguous across
 * time zones - like ExifTool writes it.
 */
private fun LocalDateTime.toIptcTimeString(offset: UtcOffset): String {

    val totalSeconds = offset.totalSeconds

    val sign = if (totalSeconds < 0) "-" else "+"

    val absoluteSeconds = abs(totalSeconds)

    val offsetHours = absoluteSeconds / SECONDS_PER_HOUR
    val offsetMinutes = absoluteSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE

    val paddedHour = hour.toString().padStart(2, '0')
    val paddedSecond = second.toString().padStart(2, '0')
    val paddedMinute = minute.toString().padStart(2, '0')
    val paddedOffsetHours = offsetHours.toString().padStart(2, '0')
    val paddedOffsetMinutes = offsetMinutes.toString().padStart(2, '0')

    return "$paddedHour$paddedMinute$paddedSecond$sign$paddedOffsetHours$paddedOffsetMinutes"
}

private fun createLocationShownRecords(locationShown: LocationShown): List<IptcRecord> {

    val records = mutableListOf<IptcRecord>()

    locationShown.street?.let { location ->
        records.add(IptcRecord(IptcTypes.SUBLOCATION, location))
    }

    locationShown.city?.let { city ->
        records.add(IptcRecord(IptcTypes.CITY, city))
    }

    locationShown.state?.let { state ->
        records.add(IptcRecord(IptcTypes.PROVINCE_STATE, state))
    }

    locationShown.country?.let { country ->
        records.add(IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME, country))
    }

    return records
}
