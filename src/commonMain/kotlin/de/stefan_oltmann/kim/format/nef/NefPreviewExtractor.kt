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
package de.stefan_oltmann.kim.format.nef

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Extracts the preview image of Nikon NEF files.
 */
public object NefPreviewExtractor : TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val ifd1 = tiffContents.directories.find {
            it.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
        } ?: return@extractPreviewImage null

        val previewImageStart = ifd1.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)
            ?: return@extractPreviewImage null

        val previewLength = ifd1.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)
            ?: return@extractPreviewImage null

        if (previewLength == 0)
            return@extractPreviewImage null

        return@tryWithImageReadException TiffPreviewExtractor.readValidatedPreviewBytes(
            randomAccessByteReader = randomAccessByteReader,
            start = previewImageStart,
            length = previewLength
        )
    }
}
