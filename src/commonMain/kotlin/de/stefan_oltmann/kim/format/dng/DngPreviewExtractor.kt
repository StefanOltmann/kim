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
package de.stefan_oltmann.kim.format.dng

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Extracts the preview image of Adobe DNG files.
 */
public object DngPreviewExtractor : TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val ifd0 = tiffContents.directories.first()

        /* Ensure that the file is a DNG by checking the required tag. */
        if (ifd0.getFieldValue(TiffTag.TIFF_TAG_DNG_VERSION, false) == null)
            return null

        val ifd2 = tiffContents.directories.find {
            it.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD2
        } ?: return null

        val previewImageStart =
            ifd2.getFieldValue(ExifTag.EXIF_TAG_PREVIEW_IMAGE_START_SUB_IFD1) ?: return null

        val previewLength =
            ifd2.getFieldValue(ExifTag.EXIF_TAG_PREVIEW_IMAGE_LENGTH_SUB_IFD1) ?: return null

        if (previewLength == 0)
            return null

        randomAccessByteReader.moveTo(previewImageStart)

        val previewBytes = randomAccessByteReader.readBytes(previewLength)

        return@tryWithImageReadException previewBytes
    }
}
