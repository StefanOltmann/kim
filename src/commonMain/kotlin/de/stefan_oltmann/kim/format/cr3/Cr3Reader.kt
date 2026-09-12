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
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonMakerNoteHandler
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

        /*
         * Metadata that exists in the file but cannot be read cleanly fails
         * the read (see "Strict read policy" in the [Kim] documentation):
         * sidecar writers consume this result, so silently returning only
         * part of the metadata would lose data.
         */
        val subBoxes = findMetadataSubBoxes(allBoxes)

        val xmpFromUuidBox = allBoxes.filterIsInstance<UuidBox>().find {
            it.uuidAsHex == CR3_XMP_UUID
        }?.data?.decodeToString()

        /*
         * Like WebP and JXL, corrupt XMP must fail the read instead of
         * being handed to sidecar writers as a corrupt packet (read/update
         * symmetry: an update would embed the broken bytes as-is).
         */
        if (xmpFromUuidBox != null && !xmpFromUuidBox.contains("<x:xmpmeta"))
            throw ImageReadException("The CR3 XMP UUID box has no <x:xmpmeta> element.")

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
                iptc = null, /* Not existent in CR3. */
                xmp = xmpFromUuidBox
            )
        }

        val idf0Directory = idf0.directories.first()

        val exifIfdDirectory: TiffDirectory? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT2,
            directoryType = TiffConstants.TIFF_DIRECTORY_EXIF
        )?.directories?.firstOrNull()

        val makerNoteContents = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT3,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_CANON
        )

        val makerNoteDirectory: TiffDirectory? = makerNoteContents?.directories?.firstOrNull()

        val makerNoteSubDirectories = mutableListOf<TiffDirectory>()

        makerNoteContents?.let { contents ->
            makerNoteDirectory?.let { directory ->
                CanonMakerNoteHandler.readSubDirectories(
                    directory = directory,
                    byteOrder = contents.header.byteOrder,
                    model = idf0Directory.entries
                        .find { it.tag == TiffTag.TIFF_TAG_MODEL.tag }
                        ?.valueDescription,
                    addDirectory = { makerNoteSubDirectories.add(it) }
                )
            }
        }

        val gpsIfdDirectory: TiffDirectory? = readTiffContents(
            boxes = subBoxes,
            boxType = BoxType.CMT4,
            directoryType = TiffConstants.TIFF_DIRECTORY_GPS
        )?.directories?.firstOrNull()

        val tiffContents = TiffContents(
            header = idf0.header,
            directories = listOfNotNull(idf0Directory, exifIfdDirectory, gpsIfdDirectory),
            makerNoteDirectory = makerNoteDirectory,
            makerNoteSubDirectories = makerNoteSubDirectories,
            geoTiffDirectory = null /* Not present in CR3. */
        )

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
            exifBytes = null, /* TODO Generate bytes? */
            iptc = null, /* Not covered by ISO BMFF. */
            xmp = xmpFromUuidBox
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

    /**
     * Returns the sub-boxes of the Canon metadata UUID box inside the
     * moov box.
     *
     * A CR3 without a moov box or without the EXIF metadata UUID box
     * simply has no EXIF metadata - an empty list is the correct result,
     * not a degradation.
     *
     * A metadata UUID box that IS present but cannot be scanned cleanly
     * fails the read per the strict read policy in the
     * [de.stefan_oltmann.kim.Kim] documentation: sidecar writers consume
     * this result, so silently
     * returning only part of the existing metadata would lose data.
     */
    fun findMetadataSubBoxes(allBoxes: List<Box>): List<Box> {

        val moovBox = allBoxes.filterIsInstance<MovieBox>().firstOrNull()
            ?: return emptyList()

        val metadataBox = moovBox.boxes.filterIsInstance<UuidBox>().find { box ->
            box.uuidAsHex == CR3_EXIF_UUID
        } ?: return emptyList()

        return BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(metadataBox.data),
            offsetShift = metadataBox.offset + 24
        )
    }
}
