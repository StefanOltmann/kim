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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcType
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.isExifUpdate
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.readRemainingBytes
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object JpegUpdater : MetadataUpdater {

    private val LOCATION_SHOWN_IPTC_TYPES: Set<IptcType> = setOf(
        IptcTypes.SUBLOCATION,
        IptcTypes.CITY,
        IptcTypes.PROVINCE_STATE,
        IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME
    )

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        /*
         * TODO Avoid the read all bytes and stream instead.
         *  This will require the implementation of single-shot updates to all fields.
         */
        val bytes = byteReader.readRemainingBytes()

        if (!bytes.startsWith(MediaFormatMagicNumbers.jpeg))
            throw ImageWriteException("Provided input bytes are not JPEG!")

        val kimMetadata = JpegImageParser.parseMetadata(
            ByteArrayByteReader(bytes)
        )

        /*
         * Use existing XMP or create a new block.
         */
        val xmpMeta: XMPMeta = if (kimMetadata.xmp != null)
            XMPMetaFactory.parseFromString(kimMetadata.xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

        val exifUpdates = updates.filter(MetadataUpdate::isExifUpdate)

        /*
         * A sole orientation update can be applied losslessly by swapping a
         * single byte in the file. Any other EXIF update requires a rewrite,
         * which then also applies the orientation.
         */
        val onlyOrientation = exifUpdates.singleOrNull() as? MetadataUpdate.Orientation

        /*
         * Note: a successful tryLosslessOrientationUpdate() swaps the
         * orientation byte inside "bytes" in place, so the rewrite below
         * must re-read the modified array.
         */
        val losslessOrientationApplied =
            onlyOrientation != null && tryLosslessOrientationUpdate(bytes, onlyOrientation.tiffOrientation)

        val outputSet = if (exifUpdates.isEmpty() || losslessOrientationApplied)
            null
        else
            createExifOutputSet(kimMetadata.exif, exifUpdates)

        val iptc = createIptcMetadata(kimMetadata.iptc, updates)

        JpegRewriter.updateMetadata(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            xmpXml = updatedXmp,
            outputSet = outputSet,
            iptc = iptc
        )
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.jpeg))
            throw ImageWriteException("Provided input bytes are not JPEG!")

        val metadata = JpegImageParser.parseMetadata(ByteArrayByteReader(bytes))

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        return byteWriter.toByteArray()
    }

    /**
     * Creates the output set with the given EXIF-applicable updates applied.
     */
    private fun createExifOutputSet(
        exif: TiffContents?,
        exifUpdates: List<MetadataUpdate>
    ): TiffOutputSet {

        val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

        for (update in exifUpdates)
            outputSet.applyUpdate(update)

        return outputSet
    }

    /**
     * Applies the orientation losslessly by swapping the orientation value
     * byte in the given bytes in place, if an orientation field exists.
     *
     * Returns whether the swap was performed.
     */
    private fun tryLosslessOrientationUpdate(
        inputBytes: ByteArray,
        tiffOrientation: TiffOrientation
    ): Boolean {

        val byteReader = ByteArrayByteReader(inputBytes)

        val orientationOffset = JpegOrientationOffsetFinder.findOrientationOffset(byteReader)

        if (orientationOffset != null) {

            inputBytes[orientationOffset.toInt()] = tiffOrientation.value.toByte()

            return true
        }

        return false
    }

    /**
     * Creates the IPTC metadata with all IPTC-applicable updates applied, or
     * NULL if the IPTC data does not need to be rewritten.
     */
    private fun createIptcMetadata(
        iptc: IptcMetadata?,
        updates: Set<MetadataUpdate>
    ): IptcMetadata? {

        val iptcUpdates = updates.filter { update ->
            update is MetadataUpdate.Title ||
                update is MetadataUpdate.Description ||
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

        return IptcMetadata(remainingRecords + newRecords, newBlocks)
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
}
