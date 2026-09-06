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

    /*
     * Real files nest directories at most a few levels deep (IFD0,
     * ExifIFD, InteropIFD), so this limit only rejects hostile input.
     */
    private const val MAX_SUB_DIRECTORY_DEPTH: Int = 16

    private const val BIGTIFF_VERSION: Int = 43

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
            visitedOffsets = hashSetOf(),
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

        /*
         * A BigTIFF (version 43, 20-byte directory entries) misparsed as
         * classic TIFF would produce a valid-looking file with truncated
         * metadata on rewrite. Other version signatures like "RO" for ORF
         * or "U\0" for RW2 use classic 12-byte entries and stay readable.
         */
        if (tiffVersion == BIGTIFF_VERSION)
            throw ImageReadException("BigTIFF is not supported.")

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
        visitedOffsets: MutableSet<Int>,
        readTiffImageBytes: Boolean,
        addDirectory: (TiffDirectory) -> Unit,
        valueOffsetBase: Int = 0,
        followNextDirectory: Boolean = true,
        depth: Int = 0
    ): Boolean {

        /* We don't want to visit a directory twice. */
        if (directoryOffset in visitedOffsets)
            return false

        visitedOffsets.add(directoryOffset)

        /*
         * Hostile files can nest sub-directories arbitrarily by chaining
         * offset fields across directories. The depth limit keeps the
         * recursion bounded.
         */
        if (depth >= MAX_SUB_DIRECTORY_DEPTH)
            throw ImageReadException(
                "Too many nested TIFF directories at offset $directoryOffset."
            )

        var currentOffset = directoryOffset
        var currentType = directoryType

        while (true) {

            byteReader.reset()

            /*
             * Sometimes TIFF offsets are greater than the file itself.
             * We ignore such corruptions.
             */
            if (currentOffset >= byteReader.contentLength)
                return true

            byteReader.skipBytes("Directory offset", currentOffset)

            val fields = try {

                val entryCount = byteReader.read2BytesAsInt("entrycount", byteOrder)

                readTiffFields(
                    byteReader = byteReader,
                    fieldsOffset = currentOffset + 2,
                    entryCount = entryCount,
                    byteOrder = byteOrder,
                    directoryType = currentType,
                    valueOffsetBase = valueOffsetBase
                )

            } catch (ex: Exception) {

                /*
                 * Check if it's just the thumbnail directory and if so, ignore this error.
                 * Thumbnails are not essential and can be re-created anytime.
                 */

                val isThumbnailDirectory = currentType == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1

                if (isThumbnailDirectory)
                    return true

                throw ex
            }

            val nextDirectoryOffset =
                byteReader.read4BytesAsInt("Next directory offset", byteOrder)

            val directory = TiffDirectory(
                type = currentType,
                entries = fields,
                offset = currentOffset,
                nextDirectoryOffset = nextDirectoryOffset,
                byteOrder = byteOrder
            )

            /*
             * Only the image directories (positive types) contain thumbnail data.
             * MakerNote directories use tag IDs that collide with the standard
             * JPEG thumbnail tags, so they must not be interpreted as thumbnails.
             */
            if (currentType >= 0 && directory.hasJpegImageData())
                directory.thumbnailBytes = readThumbnailBytes(byteReader, directory)

            if (readTiffImageBytes && directory.hasStripImageData())
                directory.tiffImageBytes = readTiffImageBytes(byteReader, directory)

            addDirectory(directory)

            readOffsetDirectories(
                byteReader = byteReader,
                byteOrder = byteOrder,
                directory = directory,
                fields = fields,
                visitedOffsets = visitedOffsets,
                readTiffImageBytes = readTiffImageBytes,
                addDirectory = addDirectory,
                depth = depth
            )

            /*
             * The next directory in the chain is followed iteratively, so a
             * long chain of directories cannot overflow the call stack.
             */
            if (!followNextDirectory || nextDirectoryOffset <= 0)
                return true

            /* We don't want to visit a directory twice. */
            if (nextDirectoryOffset in visitedOffsets)
                return true

            currentOffset = nextDirectoryOffset
            currentType += 1
        }
    }

    /**
     * The Exif, GPS and Interop offset fields point to sub-IFDs whose
     * content is user-visible metadata that must survive updates.
     */
    private fun isMetadataBearingOffsetField(offsetField: TagInfo): Boolean =
        offsetField == ExifTag.EXIF_TAG_EXIF_OFFSET ||
            offsetField == ExifTag.EXIF_TAG_GPSINFO ||
            offsetField == ExifTag.EXIF_TAG_INTEROP_OFFSET

    private fun readOffsetDirectories(
        byteReader: RandomAccessByteReader,
        byteOrder: ByteOrder,
        directory: TiffDirectory,
        fields: MutableList<TiffField>,
        visitedOffsets: MutableSet<Int>,
        readTiffImageBytes: Boolean,
        addDirectory: (TiffDirectory) -> Unit,
        depth: Int
    ) {

        for (offsetField in offsetFields) {

            val field = directory.findField(offsetField) ?: continue

            val subDirOffsets: IntArray = try {

                when (offsetField) {
                    is TagInfoLong -> {

                        val value = directory.getFieldValue(offsetField)
                            ?: throw ImageReadException("Missing value for ${offsetField.name}")

                        intArrayOf(value)
                    }

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
                 *
                 * This only ever happens for data that is certainly
                 * unreadable (the value cannot even be parsed), never
                 * for data that might be valid. See "Never destroy
                 * metadata" in the [Kim] documentation.
                 */

                fields.remove(field)

                continue
            }

            for ((index, subDirOffset) in subDirOffsets.withIndex()) {

                /*
                 * An offset at or beyond the end of the file exits
                 * readDirectory with "success" (the lenient root-chain
                 * behavior), which would silently drop the pointer and
                 * its sub-IFD from the rewrite. That must fail loudly
                 * for the metadata-bearing sub-IFDs.
                 */
                if (isMetadataBearingOffsetField(offsetField) &&
                    subDirOffset.toLong() >= byteReader.contentLength
                )
                    throw ImageReadException(
                        "The ${offsetField.name} offset $subDirOffset points beyond the end of the file."
                    )

                val subDirectoryRead = try {

                    readDirectory(
                        byteReader = byteReader,
                        byteOrder = byteOrder,
                        directoryOffset = subDirOffset,
                        directoryType = getSubDirectoryType(offsetField, index),
                        visitedOffsets = visitedOffsets,
                        readTiffImageBytes = readTiffImageBytes,
                        addDirectory = addDirectory,
                        /*
                         * The next-IFD pointer must only be followed in the
                         * root chain, where it steps IFD0 -> IFD1 -> ... .
                         * In sub-directories it must be zero per spec, and
                         * following it would escalate the directory type
                         * (+1), so a chained GPS IFD was mis-tagged as an
                         * Exif IFD.
                         */
                        followNextDirectory = false,
                        depth = depth + 1
                    )

                } catch (ex: ImageReadException) {

                    /*
                     * The Exif, GPS and Interop sub-IFDs carry metadata that
                     * must survive updates. Removing the pointer field of an
                     * unreadable sub-IFD would make the next update drop its
                     * data permanently, so these fail the read like ExifTool
                     * fails an unreadable MakerNote. See "Never destroy
                     * metadata" in the [Kim] documentation.
                     */

                    if (isMetadataBearingOffsetField(offsetField))
                        throw ex

                    false
                }

                if (!subDirectoryRead) {

                    if (isMetadataBearingOffsetField(offsetField))
                        throw ImageReadException(
                            "Failed to read the ${offsetField.name} sub-directory."
                        )

                    fields.remove(field)
                }
            }
        }
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

            /*
             * Skip corrupt counts and length overflows.
             *
             * A count of 0x80000000 or larger is read as a negative number,
             * and a huge count can overflow the multiplication. The product
             * is therefore computed in Long space, because an Int overflow
             * can also wrap around to a small positive value that would
             * smuggle a bogus length through the check below.
             *
             * Except for fields that a rewrite cannot afford to lose.
             */
            val totalLength = count.toLong() * fieldType.size

            if (count < 0 || totalLength > Int.MAX_VALUE) {
                rejectUnreadableMakerNote(tag)
                continue
            }

            val valueLength: Int = totalLength.toInt()

            val isLocalValue: Boolean =
                valueLength <= TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH

            /*
             * Ignore corrupt offsets.
             *
             * Offsets come from unsigned LONGs, so they can resolve beyond
             * the signed Int range, and the end position can wrap around.
             * Both checks therefore run in Long space.
             */
            val resolvedOffset = valueOffsetBase.toLong() + valueOrOffset.toLong()

            val endPos = resolvedOffset + valueLength

            val valueBytes: ByteArray = if (!isLocalValue) {

                /*
                 * Except for fields that a rewrite cannot afford to lose.
                 */
                if (resolvedOffset < 0 || endPos < 0 || endPos > byteReader.contentLength) {
                    rejectUnreadableMakerNote(tag)
                    continue
                }

                byteReader.readBytes(resolvedOffset.toInt(), valueLength)

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
                    valueOffset = if (!isLocalValue) resolvedOffset.toInt() else null,
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

        /*
         * The offset comes from an unsigned LONG and can resolve beyond
         * the signed Int range in hostile files, so it is skipped instead
         * of crashing the reader.
         */
        if (element.offset < 0)
            return null

        val offset = element.offset
        var length = element.length

        /*
         * If the length is not correct (going beyond the file size) we need to adjust it.
         * Computed in Long space, so a hostile length cannot wrap around.
         */
        if (offset.toLong() + length > byteReader.contentLength)
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

            /*
             * The offset comes from an unsigned LONG and can resolve beyond
             * the signed Int range in hostile files, so the strip is skipped
             * instead of crashing the reader.
             */
            if (element.offset < 0)
                return null

            val offset = element.offset
            var length = element.length

            /*
             * If the length is not correct (going beyond the file size) we need to adjust it.
             * Computed in Long space, so a hostile length cannot wrap around.
             */
            if (offset.toLong() + length > byteReader.contentLength)
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

        /*
         * Like ExifTool, a MakerNote that cannot be parsed completely stays
         * an opaque binary block instead of rejecting the file: whatever
         * was parsed is kept and parse errors end the interpretation. See
         * the documentation of MakerNoteHandler for the policy.
         */
        try {

            when {
                make == "Canon" ->
                    CanonMakerNoteHandler.read(
                        byteReader, makerNoteValueOffset, makerNoteLength, byteOrder, model, addDirectory
                    )

                make.trim().lowercase().startsWith("nikon") ->
                    NikonMakerNoteHandler.read(byteReader, makerNoteValueOffset, model, addDirectory)

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
        } catch (_: Exception) {

            /*
             * Interpretation failures are non-fatal here, because the
             * MakerNote field itself was already read successfully.
             */
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


