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
import de.stefan_oltmann.kim.format.TiffPreviewExtractor.Companion.previewFromTags
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

        /*
         * The extractor is gated to Nikon: other TIFF-family vendors
         * (Sony ARW for example) store a small thumbnail in their chain
         * IFD1 and a much larger preview behind their own tags, and
         * their own extractors - which run later in the fallback chain -
         * read that real preview.
         */
        val make = tiffContents.directories.firstOrNull()
            ?.findField(TiffTag.TIFF_TAG_MAKE)
            ?.value as? String

        if (make?.contains("NIKON", ignoreCase = true) != true)
            return@tryWithImageReadException null

        /*
         * Nikon bodies that carry the preview in a SubIFDs entry used to
         * have that sub-IFD folded into the IFD1 type; it now carries its
         * own SubIFD0 type, so both locations are checked in order.
         */
        val previewDirectory = tiffContents.directories.find {
            it.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
        } ?: tiffContents.directories.find {
            it.type == TiffConstants.EXIF_SUB_IFD0
        }

        previewFromTags(
            directory = previewDirectory,
            randomAccessByteReader = randomAccessByteReader,
            startTag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT,
            lengthTag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH
        )
    }
}
