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
import de.stefan_oltmann.kim.common.head
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GeoTiffTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD1
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD2
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD3
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType.Companion.getFieldType
import de.stefan_oltmann.kim.format.tiff.geotiff.GeoTiffDirectory
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteParseResult
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.pentax.PentaxMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.ricoh.RicohMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByte
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.jvm.JvmStatic

/**
 * Reads the contents of TIFF files.
 */
@Suppress("TooManyFunctions", "LargeClass")
public object TiffReader {

    private val offsetFields = listOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET,
        ExifTag.EXIF_TAG_GPSINFO,
        ExifTag.EXIF_TAG_INTEROP_OFFSET,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET
    )

    /**
     * A sub-directory of a MakerNote that is stored as a binary blob.
     *
     * The fields are stored at tag * [byteOffsetMultiplier] within the
     * blob, where the multiplier is the size of the data type that the
     * vendor stores the fields in. [firstTag] and [offsetBase] shift the
     * field positions for tables whose entries do not start at the
     * beginning of the blob.
     */
    private val directoryTypeMap = mapOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET to TiffConstants.TIFF_DIRECTORY_EXIF,
        ExifTag.EXIF_TAG_GPSINFO to TiffConstants.TIFF_DIRECTORY_GPS,
        ExifTag.EXIF_TAG_INTEROP_OFFSET to TiffConstants.TIFF_DIRECTORY_INTEROP,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET to TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
    )

    /**
     * Convenience method for calls with short byte array like
     * the EXIF bytes in JPG, which are limited to 64 KB.
     */
    @JvmStatic
    public fun read(
        exifBytes: ByteArray,
        readTiffImageBytes: Boolean = false,
        directoryType: Int = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffContents =
        read(ByteArrayByteReader(exifBytes), readTiffImageBytes, directoryType)

    /**
     * Reads the TIFF file.
     *
     * @param byteReader The bytes source
     * @param readTiffImageBytes Flag to include strip bytes.
     *                           This should only set if a rewrite of the file is intended.
     *                           For normal reading of RAW metadata this consumes a lot of memory.
     * @param directoryType The type of the first directory to read
     */
    @JvmStatic
    public fun read(
        byteReader: RandomAccessByteReader,
        readTiffImageBytes: Boolean = false,
        directoryType: Int = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffContents {

        val tiffHeader = readTiffHeader(byteReader)

        byteReader.reset()

        val directories = mutableListOf<TiffDirectory>()

        readDirectory(
            byteReader = byteReader,
            byteOrder = tiffHeader.byteOrder,
            directoryOffset = tiffHeader.offsetToFirstIFD,
            directoryType = directoryType,
            visitedOffsets = mutableListOf(),
            readTiffImageBytes = readTiffImageBytes,
            addDirectory = {
                directories.add(it)
            }
        )

        if (directories.isEmpty())
            throw ImageReadException("Image did not contain any directories.")

        val makerNoteParseResult =
            tryToParseMakerNote(directories, byteReader, tiffHeader.byteOrder)

        val geoTiffDirectory = tryToParseGeoTiff(directories)

        return TiffContents(
            header = tiffHeader,
            directories = directories,
            makerNoteDirectory = makerNoteParseResult?.makerNoteDirectory,
            makerNoteSubDirectories = makerNoteParseResult?.subDirectories.orEmpty(),
            geoTiffDirectory = geoTiffDirectory
        )
    }

    /**
     * The MakerNote directory and its sub-directories.
     */
    internal fun readTiffHeader(byteReader: ByteReader): TiffHeader {

        val byteOrder1 = byteReader.readByte("Byte order: First byte")
        val byteOrder2 = byteReader.readByte("Byte Order: Second byte")

        if (byteOrder1 != byteOrder2)
            throw ImageReadException("Byte Order bytes don't match ($byteOrder1, $byteOrder2).")

        val byteOrder = getTiffByteOrder(byteOrder1)

        val tiffVersion = byteReader.read2BytesAsInt("TIFF version", byteOrder)

        val offsetToFirstIFD =
            byteReader.read4BytesAsInt("Offset to first IFD", byteOrder)

        return TiffHeader(byteOrder, tiffVersion, offsetToFirstIFD)
    }

    private fun getTiffByteOrder(byteOrderByte: Byte): ByteOrder =
        when (byteOrderByte.toInt()) {
            'I'.code -> ByteOrder.LITTLE_ENDIAN
            'M'.code -> ByteOrder.BIG_ENDIAN
            else -> throw ImageReadException("Invalid TIFF byte order ${byteOrderByte.toUInt()}")
        }

    internal fun readDirectory(
        byteReader: RandomAccessByteReader,
        byteOrder: ByteOrder,
        directoryOffset: Int,
        directoryType: Int,
        visitedOffsets: MutableList<Int>,
        readTiffImageBytes: Boolean,
        addDirectory: (TiffDirectory) -> Unit,
        valueOffsetBase: Int = 0,
        followNextDirectory: Boolean = true
    ): Boolean {

        /* We don't want to visit a directory twice. */
        if (visitedOffsets.contains(directoryOffset))
            return false

        visitedOffsets.add(directoryOffset)

        byteReader.reset()

        /*
         * Sometimes TIFF offsets are greater than the file itself.
         * We ignore such corruptions.
         */
        if (directoryOffset >= byteReader.contentLength)
            return true

        byteReader.skipBytes("Directory offset", directoryOffset)

        val fields = try {

            val entryCount = byteReader.read2BytesAsInt("entrycount", byteOrder)

            readTiffFields(
                byteReader = byteReader,
                fieldsOffset = directoryOffset + 2,
                entryCount = entryCount,
                byteOrder = byteOrder,
                directoryType = directoryType,
                valueOffsetBase = valueOffsetBase
            )

        } catch (ex: Exception) {

            /*
             * Check if it's just the thumbnail directory and if so, ignore this error.
             * Thumbnails are not essential and can be re-created anytime.
             */

            val isThumbnailDirectory = directoryType == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1

            if (isThumbnailDirectory)
                return true

            throw ex
        }

        val nextDirectoryOffset =
            byteReader.read4BytesAsInt("Next directory offset", byteOrder)

        val directory = TiffDirectory(
            type = directoryType,
            entries = fields,
            offset = directoryOffset,
            nextDirectoryOffset = nextDirectoryOffset,
            byteOrder = byteOrder
        )

        /*
         * Only the image directories (positive types) contain thumbnail data.
         * MakerNote directories use tag IDs that collide with the standard
         * JPEG thumbnail tags, so they must not be interpreted as thumbnails.
         */
        if (directoryType >= 0 && directory.hasJpegImageData())
            directory.thumbnailBytes = readThumbnailBytes(byteReader, directory)

        if (readTiffImageBytes && directory.hasStripImageData())
            directory.tiffImageBytes = readTiffImageBytes(byteReader, directory)

        addDirectory(directory)

        /* Read offset directories */
        for (offsetField in offsetFields) {

            val field = directory.findField(offsetField) ?: continue

            val subDirOffsets: IntArray = try {

                when (offsetField) {
                    is TagInfoLong -> intArrayOf(directory.getFieldValue(offsetField)!!)
                    is TagInfoLongs -> directory.getFieldValue(offsetField)
                    else -> error("Unknown offset type: $offsetField")
                }

            } catch (_: ImageReadException) {

                /*
                 * If the offset field is broken we don't try
                 * to read the sub directory.
                 *
                 * We need to remove the field pointing to wrong
                 * data or else we won't be able to update the file.
                 */

                fields.remove(field)

                continue
            }

            for ((index, subDirOffset) in subDirOffsets.withIndex()) {

                val subDirectoryRead = try {

                    readDirectory(
                        byteReader = byteReader,
                        byteOrder = byteOrder,
                        directoryOffset = subDirOffset,
                        directoryType = getSubDirectoryType(offsetField, index),
                        visitedOffsets = visitedOffsets,
                        readTiffImageBytes = readTiffImageBytes,
                        addDirectory = addDirectory
                    )

                } catch (ex: ImageReadException) {

                    /*
                     * If the subdirectory is broken we remove the field,
                     * because the file would otherwise not be updatable.
                     *
                     * Except for the ExifIFD, which carries the MakerNote:
                     * removing it would drop the MakerNote on rewrite, so
                     * such files are rejected, matching ExifTool's fatal
                     * error for an unreadable MakerNote field.
                     */

                    if (offsetField == ExifTag.EXIF_TAG_EXIF_OFFSET)
                        throw ex

                    false
                }

                if (!subDirectoryRead) {

                    if (offsetField == ExifTag.EXIF_TAG_EXIF_OFFSET)
                        throw ImageReadException("Failed to read the ExifIFD.")

                    fields.remove(field)
                }
            }
        }

        if (followNextDirectory && nextDirectoryOffset > 0)
            readDirectory(
                byteReader = byteReader,
                byteOrder = byteOrder,
                directoryOffset = directory.nextDirectoryOffset,
                directoryType = directoryType + 1,
                visitedOffsets = visitedOffsets,
                readTiffImageBytes = readTiffImageBytes,
                addDirectory = addDirectory
            )

        return true
    }

    /**
     * Rejects the file when the MakerNote field cannot be read.
     *
     * This mirrors ExifTool, which treats an unreadable MakerNote
     * value as a fatal error ("Error reading value for ... ID 0x927c
     * MakerNote") and aborts the write: a rewrite would otherwise
     * drop the MakerNote silently and damage the file. Unlike
     * unreadable MakerNote sub-directories, which ExifTool skips while
     * keeping the MakerNote as an opaque binary block, an unreadable
     * field cannot be preserved at all.
     */
    private fun rejectUnreadableMakerNote(tag: Int) {

        if (tag == ExifTag.EXIF_TAG_MAKER_NOTE.tag)
            throw ImageReadException("Failed to read the MakerNote.")
    }

    /*
     * Determines the directory type for a sub-directory offset.
     * Sub-IFDs are numbered per their position in the offset list.
     */
    @Suppress("MagicNumber")
    private fun getSubDirectoryType(offsetField: TagInfo, index: Int): Int =
        if (offsetField == ExifTag.EXIF_TAG_SUB_IFDS_OFFSET)
            when (index) {
                1 -> EXIF_SUB_IFD1
                2 -> EXIF_SUB_IFD2
                3 -> EXIF_SUB_IFD3
                else -> TIFF_DIRECTORY_TYPE_IFD1
            }
        else
            directoryTypeMap.getValue(offsetField)

    /**
     * Reads the fields of one TIFF directory.
     *
     * The stored value offsets are absolute, resolved against the
     * start of the TIFF bytes via the [valueOffsetBase] that the
     * directory's offsets live in, so consumers can use them without
     * per-directory knowledge of that base.
     */
    private fun readTiffFields(
        byteReader: RandomAccessByteReader,
        fieldsOffset: Int,
        entryCount: Int,
        byteOrder: ByteOrder,
        directoryType: Int,
        valueOffsetBase: Int
    ): MutableList<TiffField> {

        /*
         * We use an ArrayList to provide a capacity.
         * This shows a small performance improvement in the Profiler.
         */
        val fields = ArrayList<TiffField>(entryCount)

        @Suppress("LoopWithTooManyJumpStatements")
        for (entryIndex in 0 until entryCount) {

            val offset = fieldsOffset + entryIndex * TiffConstants.TIFF_ENTRY_LENGTH

            val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", byteOrder)
            val type = byteReader.read2BytesAsInt("Entry $entryIndex: 'type'", byteOrder)
            val count = byteReader.read4BytesAsInt("Entry $entryIndex: 'count'", byteOrder)

            /*
             * These bytes represent either the value for fields like orientation or
             * an offset to the value for fields like OriginalDateTime that
             * cannot be accommodated within 4 bytes.
             */
            val valueOrOffsetBytes: ByteArray =
                byteReader.readBytes("Entry $entryIndex: 'offset'", 4)

            val valueOrOffset: Int = valueOrOffsetBytes.toInt(byteOrder)

            /*
             * Skip invalid fields.
             *
             * These are seen very rarely, but can have invalid value lengths,
             * which can cause OOM problems.
             *
             * Except for the GPS directory where GPSVersionID is indeed zero,
             * but a valid field. So we shouldn't skip it.
             *
             * MakerNote directories use tag 0x0000 for their version fields,
             * so they must not be skipped either.
             */
            if (tag == 0 && directoryType >= 0 && directoryType != TiffConstants.TIFF_DIRECTORY_GPS)
                continue

            val fieldType = try {
                getFieldType(type)
            } catch (ignore: ImageReadException) {
                /*
                 * Skip over unknown field types, since we can't calculate
                 * their size without knowing their type.
                 *
                 * Except for fields that a rewrite cannot afford to lose.
                 */
                rejectUnreadableMakerNote(tag)
                continue
            }

            val valueLength: Int = count * fieldType.size

            /*
             * Skip corrupt counts and length overflows.
             *
             * A count of 0x80000000 or larger is read as a negative number,
             * and a huge count can overflow the multiplication. Both would
             * result in a negative value length, which the local-value branch
             * cannot handle.
             *
             * Except for fields that a rewrite cannot afford to lose.
             */
            if (count < 0 || valueLength < 0) {
                rejectUnreadableMakerNote(tag)
                continue
            }

            val isLocalValue: Boolean =
                valueLength <= TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH

            val valueBytes: ByteArray = if (!isLocalValue) {

                val endPos = valueOffsetBase + valueOrOffset + valueLength

                /*
                 * Ignore corrupt offsets.
                 *
                 * Note that the endPos may become negative if one value is too large for an int.
                 * That's why we need to check both offset and endPos for negativity.
                 *
                 * Except for fields that a rewrite cannot afford to lose.
                 */
                if (valueOrOffset < 0 || endPos < 0 || endPos > byteReader.contentLength) {
                    rejectUnreadableMakerNote(tag)
                    continue
                }

                byteReader.readBytes(valueOffsetBase + valueOrOffset, valueLength)

            } else {

                valueOrOffsetBytes.head(valueLength)
            }

            fields.add(
                TiffField(
                    offset = offset,
                    tag = tag,
                    directoryType = directoryType,
                    fieldType = fieldType,
                    count = count,
                    localValue = if (isLocalValue) valueOrOffset else null,
                    valueOffset = if (!isLocalValue) valueOffsetBase + valueOrOffset else null,
                    valueBytes = valueBytes,
                    byteOrder = byteOrder,
                    sortHint = entryIndex
                )
            )
        }

        return fields
    }

    /**
     * Reads the thumbnail image if data is valid or returns NULL if a problem was found.
     *
     * Discarding corrupt thumbnails is not a big issue, so no exceptions will be thrown here.
     */
    private fun readThumbnailBytes(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): ByteArray? {

        val element = directory.getJpegImageDataElement() ?: return null

        val offset = element.offset
        var length = element.length

        /*
         * If the length is not correct (going beyond the file size) we need to adjust it.
         */
        if (offset + length > byteReader.contentLength)
            length = (byteReader.contentLength - offset).toInt()

        /*
         * If the new length is 0 or negative, ignore this element.
         */
        if (length <= 0)
            return null

        val bytes = byteReader.readBytes(offset, length)

        if (bytes.size != length)
            return null

        /*
         * Ignore it if it's not a JPEG.
         * Some files have random garbage bytes here.
         */
        if (!bytes.startsWith(MediaFormatMagicNumbers.jpeg))
            return null

        /*
         * Note: Apache Commons Imaging has a validation check here to ensure that
         * the embedded thumbnail ends with DD F9, as it should.
         * However, during tests, it was discovered that OOC JPEGs from a Canon 60D
         * have an incorrect length specified for the thumbnail bytes, and after DD 99,
         * there are some random bytes present.
         */

        return bytes
    }

    private fun readTiffImageBytes(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): ByteArray? {

        val elements = directory.getStripImageDataElements() ?: return null

        val byteArrayByteWriter = ByteArrayByteWriter()

        for (element in elements) {

            val offset = element.offset
            var length = element.length

            /*
             * If the length is not correct (going beyond the file size) we need to adjust it.
             */
            if (offset + length > byteReader.contentLength)
                length = (byteReader.contentLength - offset).toInt()

            /*
             * If the new length is 0 or negative, ignore this element.
             */
            if (length <= 0)
                continue

            val bytes = byteReader.readBytes(offset, length)

            /*
             * Break if something is wrong.
             */
            if (bytes.size != length)
                return null

            byteArrayByteWriter.write(bytes)
        }

        return byteArrayByteWriter.toByteArray()
    }

    /**
     * Inspect if MakerNotes are present and could be added as
     * TiffDirectory. This is true for almost all manufacturers.
     */
    private fun tryToParseMakerNote(
        directories: MutableList<TiffDirectory>,
        byteReader: RandomAccessByteReader,
        byteOrder: ByteOrder
    ): MakerNoteParseResult? {

        val makerNoteField = TiffDirectory.findTiffField(
            directories,
            ExifTag.EXIF_TAG_MAKER_NOTE
        )

        if (makerNoteField != null && makerNoteField.valueOffset != null) {

            val make = TiffDirectory.findTiffField(
                directories, TiffTag.TIFF_TAG_MAKE
            )?.valueDescription

            val model = TiffDirectory.findTiffField(
                directories, TiffTag.TIFF_TAG_MODEL
            )?.valueDescription

            val makerNoteDirectories = mutableListOf<TiffDirectory>()

            createMakerNoteDirectory(
                byteReader = byteReader,
                makerNoteValueOffset = makerNoteField.valueOffset,
                makerNoteLength = makerNoteField.count * makerNoteField.fieldType.size,
                make = make,
                model = model,
                byteOrder = byteOrder,
                addDirectory = {
                    makerNoteDirectories.add(it)
                }
            )

            if (makerNoteDirectories.isEmpty())
                return null

            return MakerNoteParseResult(
                makerNoteDirectory = makerNoteDirectories.first(),
                subDirectories = makerNoteDirectories.drop(1)
            )
        }

        return null
    }

    /**
     * Try to read MakerNote and add it as a directory.
     *
     * See https://exiftool.sourceforge.net/makernote_types.html
     */
    private fun createMakerNoteDirectory(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        makerNoteLength: Int,
        make: String?,
        model: String?,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (make == null)
            return

        when {
            make == "Canon" ->
                CanonMakerNoteHandler.read(
                    byteReader, makerNoteValueOffset, makerNoteLength, byteOrder, model, addDirectory
                )

            make.trim().lowercase().startsWith("nikon") ->
                NikonMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("FUJIFILM", ignoreCase = true) ->
                FujiFilmMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("Apple", ignoreCase = true) ->
                AppleMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("PENTAX", ignoreCase = true) || make.contains("SAMSUNG", ignoreCase = true) ->
                PentaxMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("RICOH", ignoreCase = true) ->
                RicohMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("OLYMPUS", ignoreCase = true) ->
                OlympusMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.contains("Panasonic", ignoreCase = true) ->
                PanasonicMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)

            make.startsWith("SONY", ignoreCase = true) ->
                SonyMakerNoteHandler.read(byteReader, makerNoteValueOffset, addDirectory)
        }
    }

    /**
     * Parses the GeoTIFF directory from the GeoKeyDirectory tag of the
     * given directories, or returns null when the tag is missing.
     *
     * Failures are silent, because GeoTIFF interpretation is optional.
     */
    private fun tryToParseGeoTiff(
        directories: MutableList<TiffDirectory>
    ): GeoTiffDirectory? {

        try {

            val geoTiffDirectoryField = TiffDirectory.findTiffField(
                directories,
                GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG
            ) ?: return null

            val shorts = geoTiffDirectoryField.value as? ShortArray

            if (shorts != null)
                return GeoTiffDirectory.parseFrom(shorts)

            return null

        } catch (ignore: Exception) {

            /*
             * Be silent here as GeoTiff interpretation is not essential.
             */

            return null
        }
    }
}


