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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.JpegSegmentAnalyzer
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MediaFormat

/**
 * Parses the metadata of TIFF files.
 */
public object TiffImageParser : ImageParser {

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            /**
             * TIFF files, such as CR2 RAW files, can be extremely large.
             * It is not advisable to load them entirely into a ByteArray.
             */
            val randomAccessByteReader = DefaultRandomAccessByteReader(byteReader)

            val exif = TiffReader.read(randomAccessByteReader)

            val contents = parseEmbeddedJpegMakerNote(exif)

            val imageSize = getImageSize(contents)
            val xmp = getXmpXml(contents)

            return@tryWithImageReadException MediaMetadata(
                mediaFormat = MediaFormat.TIFF,
                imageSize = imageSize,
                exif = contents,
                exifBytes = null,
                iptc = null,
                xmp = xmp
            )
        }

    /**
     * The Panasonic RW2 stores its MakerNote inside the EXIF of the
     * embedded JPEG preview instead of the TIFF, so it is read from there.
     *
     * Like ExifTool, an embedded JPEG whose EXIF cannot be read is
     * skipped instead of rejecting the file: the MakerNote stays in
     * the file as an opaque binary block.
     */
    private fun parseEmbeddedJpegMakerNote(tiffContents: TiffContents): TiffContents {

        val jpegBytes = tiffContents.directories.firstOrNull()
            ?.getFieldValue(TiffTag.TIFF_TAG_JPG_FROM_RAW, false)
            ?: return tiffContents

        val exifBytes = extractExifBytesFromJpeg(jpegBytes)
            ?: return tiffContents

        val previewTiffContents = try {
            TiffReader.read(exifBytes)
        } catch (_: Exception) {
            /*
             * Skip the unreadable embedded JPEG.
             *
             * Like ExifTool, the MakerNote is kept as an opaque
             * binary block in this case.
             */
            return tiffContents
        }

        val makerNoteDirectory = previewTiffContents.makerNoteDirectory
            ?: return tiffContents

        return tiffContents.copy(
            makerNoteDirectory = makerNoteDirectory,
            makerNoteSubDirectories = previewTiffContents.makerNoteSubDirectories
        )
    }

    /**
     * Extracts the payload of the first APP1 EXIF segment of the
     * given JPEG bytes.
     */
    private fun extractExifBytesFromJpeg(jpegBytes: ByteArray): ByteArray? {

        val segments = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(jpegBytes)
        )

        val exifSegment = segments.firstOrNull { info ->

            if (info.marker != JpegConstants.JPEG_APP1_MARKER)
                return@firstOrNull false

            if (info.offset + info.length > jpegBytes.size)
                return@firstOrNull false

            val segmentBytes = jpegBytes.copyOfRange(info.offset + 4, info.offset + info.length)

            segmentBytes.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE)
        } ?: return null

        val payloadStart = exifSegment.offset + 4 + JpegConstants.EXIF_IDENTIFIER_CODE.size

        return jpegBytes.copyOfRange(payloadStart, exifSegment.offset + exifSegment.length)
    }

    private fun getImageSize(tiffContents: TiffContents): ImageSize? {

        /*
         * First check if ExifImageWidth & ExifImageHeight are set.
         * This is the case with Sony ARW files and the only correct information.
         */

        val exifIfdDir = tiffContents.findTiffDirectory(TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD.typeId)

        if (exifIfdDir != null) {

            val exifImageWidth = exifIfdDir.findField(ExifTag.EXIF_TAG_EXIF_IMAGE_WIDTH)?.toInt()
            val exifImageHeight = exifIfdDir.findField(ExifTag.EXIF_TAG_EXIF_IMAGE_HEIGHT)?.toInt()

            if (exifImageWidth != null && exifImageHeight != null)
                return ImageSize(exifImageWidth, exifImageHeight)
        }

        /*
         * The Panasonic RW2 has no EXIF image size. The size of the
         * full resolution image is stored in the MakerNote.
         */

        val makerNoteDirectory = tiffContents.makerNoteDirectory

        if (makerNoteDirectory?.type == TiffConstants.TIFF_MAKER_NOTE_PANASONIC) {

            val panasonicImageWidth =
                makerNoteDirectory.findField(PanasonicTag.PANASONIC_IMAGE_WIDTH)?.toInt()
            val panasonicImageHeight =
                makerNoteDirectory.findField(PanasonicTag.PANASONIC_IMAGE_HEIGHT)?.toInt()

            if (panasonicImageWidth != null && panasonicImageHeight != null)
                return ImageSize(panasonicImageWidth, panasonicImageHeight)
        }

        /*
         * NEF files have the image length of the full resoltion
         * image in SubIFD1 and not in the first directory, which
         * contains the thumbnail. Just always taking the first
         * directory is wrong.
         *
         * Other vendors use the SubIFD differently.
         * Just look for the biggest size and report that.
         */

        val imageSizes = mutableListOf<ImageSize>()

        for (directory in tiffContents.directories)
            getImageSize(directory)?.let { imageSizes.add(it) }

        return imageSizes.maxByOrNull { it.width * it.height }
    }

    private fun getImageSize(directory: TiffDirectory): ImageSize? {

        val widthField = directory.findField(TiffTag.TIFF_TAG_IMAGE_WIDTH, false)
        val heightField = directory.findField(TiffTag.TIFF_TAG_IMAGE_HEIGHT, false)

        if (widthField == null || heightField == null)
            return null

        return ImageSize(widthField.toInt(), heightField.toInt())
    }

    private fun getXmpXml(tiffContents: TiffContents): String? {

        val firstDirectory = tiffContents.directories.first()

        val bytes = firstDirectory.getFieldValue(TiffTag.TIFF_TAG_XMP, false) ?: return null

        if (bytes.isEmpty())
            return null

        return bytes.decodeToString()
    }
}
