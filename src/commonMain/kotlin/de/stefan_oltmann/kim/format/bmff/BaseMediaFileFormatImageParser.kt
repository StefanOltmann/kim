/*
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MetadataType
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import de.stefan_oltmann.kim.format.bmff.box.BoxContainer
import de.stefan_oltmann.kim.format.bmff.box.FileTypeBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBoxTopLevel
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.format.cr3.Cr3Reader
import de.stefan_oltmann.kim.format.jxl.JxlReader
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.PrePendingByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.readRemainingBytes
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Reads containers that follow the ISO base media file format
 * as defined in ISO/IEC 14496-12.
 * Examples for these are HEIC, AVIF & JPEG XL.
 *
 * https://en.wikipedia.org/wiki/ISO_base_media_file_format
 */
public object BaseMediaFileFormatImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): MediaMetadata {

        val copyByteReader = CopyByteReader(byteReader)

        /*
         * A naked JPEG XL codestream starts with this signature and
         * consists of raw code stream data without any ISOBMFF boxes, so
         * it cannot carry metadata. It is detected as JXL and answered
         * with empty metadata instead of failing box parsing.
         */
        val firstTwoBytes = copyByteReader.readBytes(2)

        if (firstTwoBytes.contentEquals(MediaFormatMagicNumbers.jxlCodeStream.toByteArray()))
            return MediaMetadata.createEmpty(mediaFormat = MediaFormat.JXL)

        val copyPendingByteReader =
            PrePendingByteReader(copyByteReader, firstTwoBytes.toList())

        var position: Long = 0

        val allBoxes = BoxReader.readBoxes(
            byteReader = copyPendingByteReader,
            stopAfterMetadataRead = true,
            positionOffset = 0,
            offsetShift = 0,
            updatePosition = { position = it }
        )

        if (allBoxes.isEmpty())
            throw ImageReadException("Illegal ISOBMFF: Has no boxes.")

        val fileTypeBox = allBoxes.filterIsInstance<FileTypeBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: Has no 'ftyp' Box.")

        /**
         * Handle JPEG XL
         *
         * This format has EXIF & XMP neatly in dedicated boxes, so we can just extract these.
         */
        if (fileTypeBox.majorBrand == FileTypeBox.JXL_BRAND)
            return JxlReader.createMetadata(allBoxes)

        /**
         * Handle CR3
         */
        if (fileTypeBox.majorBrand == FileTypeBox.CR3_BRAND)
            return Cr3Reader.createMetadata(allBoxes)

        val metaBox = allBoxes.filterIsInstance<MetaBoxTopLevel>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: Has no top-level 'meta' Box.")

        val uuidBoxes = BoxContainer.findAllBoxesRecursive(allBoxes).filterIsInstance<UuidBox>()

        val metadataItems = metaBox.findMetadataItems()

        /* Return empty object if no metadata is found. */
        if (metadataItems.isEmpty() && uuidBoxes.none { it.isXmp })
            return MediaMetadata.createEmpty(mediaFormat = null)

        val minOffset = metadataItems.firstOrNull()?.extents?.firstOrNull()?.offset

        /*
         * In case of Samsung Galaxy HEIC files the mdat Box comes
         * before the meta Box. We need to reset the reader here,
         * but as we may read from a cloud stream we really don't
         * have a "reset" function.
         *
         * We currently do this by having a copy of all bytes
         * in buffer and input everything we read so far in again.
         * FIXME There must be a better solution. Find it.
         *
         * If minOffset is null, there is no metadata to read from metadata offsets. The only
         * metadata we have is in UUID boxes, which we have already read into memory by this point.
         * If this is the case, we can avoid resetting the reader position.
         */
        val onPositionBeforeMinimumOffset = minOffset == null || position <= minOffset

        val byteReaderToUse = if (onPositionBeforeMinimumOffset) {

            byteReader

        } else {

            /* Read all remaining bytes. */
            copyByteReader.readRemainingBytes()

            ByteArrayByteReader(copyByteReader.getBytes())
        }

        check(byteReader.contentLength == byteReaderToUse.contentLength) {
            "Content length is different: ${byteReader.contentLength} != ${byteReaderToUse.contentLength}"
        }

        if (!onPositionBeforeMinimumOffset)
            position = 0

        var exifBytes: ByteArray? = null
        var exif: TiffContents? = null
        var xmp: String? = null

        @Suppress("LoopWithTooManyJumpStatements")
        for (item in metadataItems) {

            val firstExtent = item.extents.first()

            /*
             * Ignore illegal offsets.
             *
             * Every extent is validated against the content bounds.
             * Checking only the last extent would let a hostile file hide
             * an oversized extent between two legal ones, which would then
             * abort the read of all remaining items further below, even
             * though only this single item is broken. Items that start
             * before the current position would make the reader jump
             * backwards and desync it, so they are skipped as well.
             * endPosition is checked for negative values to also catch
             * value overflows.
             */
            val hasIllegalExtent = item.extents.any { extent ->
                extent.endPosition < 0 || extent.endPosition > byteReader.contentLength
            }

            if (hasIllegalExtent || firstExtent.offset < position)
                continue

            val lastExtent = item.extents.last()

            when (item.type) {

                MetadataType.EXIF -> {

                    exifBytes = readExifBytes(byteReaderToUse, position, item)

                    /* Parse EXIF in place to fail fast if reading went wrong. */
                    exif = TiffReader.read(exifBytes)

                    position = lastExtent.endPosition
                }

                MetadataType.IPTC ->
                    continue // Unsupported

                MetadataType.XMP -> {
                    xmp = readXmpString(byteReaderToUse, position, item)
                    position = lastExtent.endPosition
                }
            }
        }

        /* XMP data can also be found in a UUID box, if we didn't find it in the metadata offsets. */
        if (xmp == null) {
            xmp = uuidBoxes.firstOrNull { it.isXmp }?.data?.decodeToString()
        }

        return MediaMetadata(
            mediaFormat = null, // could be any ISO BMFF
            imageSize = null, // not covered by ISO BMFF
            exif = exif,
            exifBytes = exifBytes,
            iptc = null, // not supported by ISO BMFF
            xmp = xmp
        )
    }

    /**
     * Reads the EXIF stream of one item, concatenating all of its extents.
     *
     * The first extent starts with a 4-byte TIFF header offset field,
     * followed by the EXIF header and the TIFF data. Later extents are
     * pure continuations of that data.
     */
    private fun readExifBytes(
        byteReader: ByteReader,
        position: Long,
        item: MetadataItem
    ): ByteArray {

        val firstExtent = item.extents.first()

        val bytesToSkip = firstExtent.offset - position

        check(bytesToSkip >= 0) {
            "Position must be before extent offset: position=$position extent=$firstExtent"
        }

        byteReader.skipBytes("offset to EXIF extent", bytesToSkip)

        val tiffHeaderOffset =
            byteReader.read4BytesAsInt("tiffHeaderOffset", BMFF_BYTE_ORDER)

        /* Usualy there are 6 bytes skipped, which are the EXIF header. ("Exif.."). */
        byteReader.skipBytes("offset to TIFF header", tiffHeaderOffset)

        val exifBytesWriter = ByteArrayByteWriter()

        var currentPosition =
            firstExtent.offset + TIFF_HEADER_OFFSET_BYTE_COUNT + tiffHeaderOffset

        for ((index, extent) in item.extents.withIndex()) {

            /*
             * Extents of an item do not need to be adjacent, so gaps
             * between them are skipped without being interpreted.
             */
            val gapToSkip = extent.offset - currentPosition

            if (gapToSkip > 0)
                byteReader.skipBytes("gap between extents", gapToSkip)

            /*
             * A single extent larger than the signed Int range cannot be
             * read into one array - fail with a clear error instead of
             * silently truncating the length.
             */
            if (extent.length > Int.MAX_VALUE)
                throw ImageReadException(
                    "EXIF extent is too large: ${extent.length} bytes."
                )

            val length = if (index == 0)
                extent.length.toInt() - TIFF_HEADER_OFFSET_BYTE_COUNT - tiffHeaderOffset
            else
                extent.length.toInt()

            if (length < 0)
                throw ImageReadException("Invalid EXIF extent length: $length")

            exifBytesWriter.write(byteReader.readBytes("EXIF extent data", length))

            currentPosition = extent.offset + extent.length
        }

        return exifBytesWriter.toByteArray()
    }

    /**
     * Reads the XMP string of one item, concatenating all of its extents.
     */
    private fun readXmpString(
        byteReader: ByteReader,
        position: Long,
        item: MetadataItem
    ): String {

        val xmpBytesWriter = ByteArrayByteWriter()

        var currentPosition = position

        for (extent in item.extents) {

            val gapToSkip = extent.offset - currentPosition

            if (gapToSkip > 0)
                byteReader.skipBytes("gap between extents", gapToSkip)

            /*
             * A single extent larger than the signed Int range cannot be
             * read into one array - fail with a clear error instead of
             * silently truncating the length.
             */
            if (extent.length > Int.MAX_VALUE)
                throw ImageReadException(
                    "XMP extent is too large: ${extent.length} bytes."
                )

            xmpBytesWriter.write(byteReader.readBytes("MIME extent data", extent.length.toInt()))

            currentPosition = extent.offset + extent.length
        }

        return xmpBytesWriter.toByteArray().decodeToString()
    }
}
