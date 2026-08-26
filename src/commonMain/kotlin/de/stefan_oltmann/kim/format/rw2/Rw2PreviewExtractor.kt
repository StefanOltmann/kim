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
package de.stefan_oltmann.kim.format.rw2

import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Extracts the preview image of Panasonic RW2 files.
 */
public object Rw2PreviewExtractor : TiffPreviewExtractor {

    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val ifd0 = tiffContents.directories.first()

        /*
         * Some files carry random garbage here, so only data with the
         * JPEG signature is returned.
         */
        val previewBytes = ifd0.getFieldValue(TiffTag.TIFF_TAG_JPG_FROM_RAW, false)
            ?: return@extractPreviewImage null

        return@extractPreviewImage if (previewBytes.startsWith(MediaFormatMagicNumbers.jpeg))
            previewBytes
        else
            null
    }
}
