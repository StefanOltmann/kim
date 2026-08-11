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
package de.stefan_oltmann.kim.format.cr3

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MediaFormat

/**
 * Parses CR3 as documented on https://github.com/lclevy/canon_cr3
 */
internal object Cr3Reader {

    const val CR3_EXIF_UUID = "85c0b687820f11e08111f4ce462b6a48"
    const val CR3_XMP_UUID = "be7acfcb97a942e89c71999491e3afac"
    const val CR3_PREVIEW_UUID = "eaf42b5e1c984b88b9fbb7dc406e4d16"

    fun createMetadata(allBoxes: List<Box>): MediaMetadata {

        val subBoxes = findMetadaSubBoxes(allBoxes)

        val idf0: TiffContents? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT1,
            directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
        )

        if (idf0 == null) {

            return MediaMetadata(
                mediaFormat = MediaFormat.CR3,
                imageSize = null,
                exif = null,
                exifBytes = null,
                iptc = null, // not existent in CR3
                xmp = null // not existent in CR3
            )
        }

        val idf0Directory = idf0.directories.first()

        val exifIfdDirectory: TiffDirectory? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT2,
            directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
        )?.directories?.firstOrNull()

        val makerNoteDirectory: TiffDirectory? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT3,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_CANON
        )?.directories?.firstOrNull()

        val gpsIfdDirectory: TiffDirectory? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT4,
            directoryType = TiffConstants.TIFF_DIRECTORY_GPS
        )?.directories?.firstOrNull()

        val tiffContents = TiffContents(
            header = idf0.header,
            directories = listOfNotNull(idf0Directory, exifIfdDirectory, gpsIfdDirectory),
            makerNoteDirectory = makerNoteDirectory,
            geoTiffDirectory = null // not present in CR3
        )

        val xmpBox = allBoxes.filterIsInstance<UuidBox>().find {
            it.uuidAsHex == CR3_XMP_UUID
        }

        val imageWidth = idf0.findTiffField(TiffTag.TIFF_TAG_IMAGE_WIDTH)?.toInt()
        val imageHeight = idf0.findTiffField(TiffTag.TIFF_TAG_IMAGE_HEIGHT)?.toInt()

        val imageSize = if (imageWidth != null && imageHeight != null)
            ImageSize(
                width = imageWidth,
                height = imageHeight
            )
        else
            null

        return MediaMetadata(
            mediaFormat = MediaFormat.CR3,
            imageSize = imageSize,
            exif = tiffContents,
            exifBytes = null, // TODO Generate bytes?
            iptc = null, // not covered by ISO BMFF
            xmp = xmpBox?.data?.decodeToString()
        )
    }

    private fun readTiffContents(
        boxes: List<Box>,
        boxType: BoxType,
        directoryType: Int
    ): TiffContents? {

        val exifBytes = boxes.find { it.type == boxType }?.payload
            ?: return null

        val tiffContents: TiffContents = TiffReader.read(
            exifBytes = exifBytes,
            directoryType = directoryType
        )

        return tiffContents
    }

    fun findMetadaSubBoxes(allBoxes: List<Box>): List<Box> {

        val moovBox = allBoxes.filterIsInstance<MovieBox>().firstOrNull()
            ?: throw ImageReadException("Illegal CR3: No 'moov' box found.")

        val metadataBox = moovBox.boxes.filterIsInstance<UuidBox>().find { box ->
            box.uuidAsHex == CR3_EXIF_UUID
        } ?: throw ImageReadException("Illegal CR3: No metadata UUID box found.")

        return BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(metadataBox.data),
            stopAfterMetadataRead = false,
            positionOffset = 0,
            offsetShift = metadataBox.offset + 24
        )
    }
}
