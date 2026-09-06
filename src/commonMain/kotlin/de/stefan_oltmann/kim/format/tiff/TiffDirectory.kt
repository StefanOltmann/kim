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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.toInts
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoGpsText
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputDirectory
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputField
import de.stefan_oltmann.kim.model.TiffOrientation

/**
 * Provides methods and elements for accessing an Image File Directory (IFD)
 * from a TIFF file. In the TIFF specification, the IFD is the main container
 * for individual images or sets of metadata. While not all Directories contain
 * images, images are always stored in a Directory.
 */
public class TiffDirectory(
    public val type: Int,
    public val entries: List<TiffField>,
    offset: Int,
    public val nextDirectoryOffset: Int,
    public val byteOrder: ByteOrder
) : TiffElement(
    debugDescription = "Directory " + description(type) + " @ $offset",
    offset = offset,
    length = TiffConstants.TIFF_DIRECTORY_HEADER_LENGTH + entries.size *
        TiffConstants.TIFF_ENTRY_LENGTH + TiffConstants.TIFF_DIRECTORY_FOOTER_LENGTH
) {

    internal var thumbnailBytes: ByteArray? = null
    internal var tiffImageBytes: ByteArray? = null

    public fun getDirectoryEntries(): List<TiffField> = entries

    public fun hasJpegImageData(): Boolean =
        null != findField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)

    public fun hasStripImageData(): Boolean =
        null != findField(TiffTag.TIFF_TAG_STRIP_OFFSETS)

    public fun findField(tag: TagInfo): TiffField? {
        return findField(
            tag = tag,
            failIfMissing = false
        )
    }

    public fun findField(tag: TagInfo, failIfMissing: Boolean = false): TiffField? {

        for (field in entries)
            if (field.tag == tag.tag)
                return field

        if (failIfMissing)
            throw ImageReadException("Missing expected field: " + tag.tagFormatted)

        return null
    }

    public fun getFieldValue(tag: TagInfoBytes, mustExist: Boolean): ByteArray? {

        val field = findField(tag)

        if (field == null) {

            if (mustExist)
                throw ImageReadException("Required field ${tag.name} is missing")

            return null
        }

        return field.valueBytes
    }

    @Suppress("ThrowsCount")
    public fun getFieldValue(tag: TagInfoLong): Int? {

        val field = findField(tag) ?: return null

        if (tag.fieldType != field.fieldType)
            throw ImageReadException("Required field ${tag.name} has incorrect type ${field.fieldType.name}")

        if (field.count != 1)
            throw ImageReadException("Field ${tag.name} has wrong count ${field.count}")

        return field.valueBytes.toInt(field.byteOrder)
    }

    @Suppress("ThrowsCount")
    public fun getFieldValue(tag: TagInfoLongs): IntArray {

        val field = findField(tag)
            ?: throw ImageReadException("Required field ${tag.name} is missing")

        if (tag.fieldType != field.fieldType)
            throw ImageReadException("Required field ${tag.name} has incorrect type ${field.fieldType.name}")

        return field.valueBytes.toInts(field.byteOrder)
    }

    public fun getJpegImageDataElement(): ImageDataElement? {

        val jpegInterchangeFormat = findField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)
        val jpegInterchangeFormatLength = findField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)

        if (jpegInterchangeFormat != null && jpegInterchangeFormatLength != null) {

            /* Zero-count fields convert to NULL and mean no image data. */
            val offset = jpegInterchangeFormat.toInt() ?: return null
            val byteCount = jpegInterchangeFormatLength.toInt() ?: return null

            return ImageDataElement(offset, byteCount)
        }

        return null
    }

    /**
     * Returns a list as tiff image bytes can be splitted upon the whole file.
     * ImageIO creates small splits while GIMP creates a single big chunk.
     */
    public fun getStripImageDataElements(): List<ImageDataElement>? {

        val offsetField = findField(TiffTag.TIFF_TAG_STRIP_OFFSETS)
        val lengthField = findField(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS)

        if (offsetField != null && lengthField != null) {

            val offsets = offsetField.toIntArray()
            val lengths = lengthField.toIntArray()

            if (offsets.size != lengths.size)
                throw ImageReadException("Offsets & Lengths mismatch: ${offsets.size} != ${lengths.size}")

            val imageDataElements = mutableListOf<ImageDataElement>()

            for (index in offsets.indices)
                imageDataElements.add(ImageDataElement(offsets[index], lengths[index]))

            return imageDataElements
        }

        return null
    }

    public fun createOutputDirectory(byteOrder: ByteOrder): TiffOutputDirectory {

        /*
         * Prevent attempts to add MakerNote directories.
         */
        @Suppress("MagicNumber")
        check(type > -100) {
            "Can't create OutputDirectory for artifical MakerNote directory."
        }

        try {

            val outputDirectory = TiffOutputDirectory(type, byteOrder)

            @Suppress("LoopWithTooManyJumpStatements")
            for (entry in entries) {

                /* Don't add double entries. */
                if (outputDirectory.findField(entry.tag) != null)
                    continue

                /* Skip known offsets. */
                if (entry.tagInfo?.isOffset == true)
                    continue

                /*
                 * Counterpart fields are only written as complete pairs:
                 * when the thumbnail or strip bytes could not be captured,
                 * the length field would remain as a dangling reference
                 * without its offset in the output.
                 */
                if (entry.tag == TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH.tag &&
                    thumbnailBytes == null
                )
                    continue

                if (entry.tag == TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS.tag && tiffImageBytes == null)
                    continue

                val tagInfo = entry.tagInfo
                val fieldType = entry.fieldType

                /*
                 * Text fields are copied byte exact. Decoding and
                 * re-encoding is lossy: Latin-1 bytes become mojibake,
                 * multi-string values are truncated at the first NUL and
                 * GPS text encoding prefixes are replaced. Values the
                 * caller did not change must survive a rewrite untouched,
                 * see "Never destroy metadata" in the [Kim] documentation.
                 */
                val bytes = if (fieldType === FieldTypeAscii || tagInfo is TagInfoGpsText)
                    entry.valueBytes
                else
                    fieldType.writeData(entry.value, byteOrder)

                val count = bytes.size / fieldType.size

                val outputField = TiffOutputField(entry.tag, fieldType, count, bytes)

                outputField.sortHint = entry.sortHint

                outputField.originalOffset = entry.valueOffset

                outputDirectory.add(outputField)
            }

            /*
             * Check if the root directory has an orientation flag and
             * add this per default it is missing. If it is present we
             * can update the orientation easily the next time we need
             * to touch the file.
             */
            if (type == TiffDirectoryType.TIFF_DIRECTORY_IFD0.typeId) {

                val orientationField = outputDirectory.findField(TiffTag.TIFF_TAG_ORIENTATION)

                if (orientationField == null)
                    outputDirectory.add(
                        tagInfo = TiffTag.TIFF_TAG_ORIENTATION,
                        value = TiffOrientation.STANDARD.value.toShort()
                    )
            }

            outputDirectory.setThumbnailBytes(thumbnailBytes)
            outputDirectory.setTiffImageBytes(tiffImageBytes)

            return outputDirectory

        } catch (ex: ImageReadException) {
            throw ImageWriteException(ex.message, ex)
        }
    }

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- $debugDescription ----")

        for (entry in entries)
            sb.appendLine(entry)

        return sb.toString()
    }

    public companion object {

        @kotlin.jvm.JvmStatic
        public fun description(type: Int): String {
            return when (type) {
                TiffConstants.DIRECTORY_TYPE_UNKNOWN -> "Unknown"
                TiffConstants.TIFF_DIRECTORY_TYPE_IFD0 -> "IFD0"
                TiffConstants.TIFF_DIRECTORY_TYPE_IFD1 -> "IFD1"
                TiffConstants.EXIF_SUB_IFD1 -> "SubIFD1"
                TiffConstants.EXIF_SUB_IFD2 -> "SubIFD2"
                TiffConstants.EXIF_SUB_IFD3 -> "SubIFD3"
                TiffConstants.TIFF_DIRECTORY_EXIF -> "ExifIFD"
                TiffConstants.TIFF_DIRECTORY_GPS -> "GPS"
                TiffConstants.TIFF_DIRECTORY_INTEROP -> "InteropIFD"
                TiffConstants.TIFF_MAKER_NOTE_CANON -> "MakerNoteCanon"
                TiffConstants.TIFF_MAKER_NOTE_NIKON -> "MakerNoteNikon"
                TiffConstants.TIFF_MAKER_NOTE_FUJIFILM -> "MakerNoteFujiFilm"
                TiffConstants.TIFF_MAKER_NOTE_APPLE -> "MakerNoteApple"
                TiffConstants.TIFF_MAKER_NOTE_OLYMPUS -> "MakerNoteOlympus"
                TiffConstants.TIFF_MAKER_NOTE_PANASONIC -> "MakerNotePanasonic"
                TiffConstants.TIFF_MAKER_NOTE_SONY -> "MakerNoteSony"
                TiffConstants.TIFF_MAKER_NOTE_SONY5 -> "MakerNoteSony5"
                TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON -> "MakerNoteSonyEricsson"
                else -> TiffDirectoryType.entries
                    .firstOrNull { it.typeId == type }
                    ?.displayName
                    ?: "Unknown type $type"
            }
        }

        /*
         * Note: Keep in sync with TiffTags.getTag()
         */
        public fun findTiffField(directories: List<TiffDirectory>, tagInfo: TagInfo): TiffField? {

            /*
             * TagInfos that specify a directory (like GPS and MakerNotes)
             * should be exact matches.
             */
            if (tagInfo.directoryType != null) {

                return directories
                    .firstOrNull { directory -> directory.type == tagInfo.directoryType.typeId }
                    ?.findField(tagInfo)
            }

            /*
             * All others are matched with all directories.
             */
            for (directory in directories)
                directory.findField(tagInfo)?.let {
                    return@findTiffField it
                }

            return null
        }
    }
}
