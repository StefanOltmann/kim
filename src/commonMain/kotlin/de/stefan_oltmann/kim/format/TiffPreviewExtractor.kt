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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Extracts a preview image from a TIFF-based media file.
 */
public fun interface TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    public fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray?

    public companion object {

        /**
         * Reads the claimed preview bytes and validates them.
         *
         * Some files carry random garbage in the preview tags, so like
         * [de.stefan_oltmann.kim.format.tiff.TiffReader] for thumbnails,
         * out-of-bounds ranges and data without the JPEG signature are
         * rejected with NULL instead of returning unusable bytes.
         */
        internal fun readValidatedPreviewBytes(
            randomAccessByteReader: RandomAccessByteReader,
            start: Int,
            length: Int
        ): ByteArray? {

            /*
             * Long math, so hostile offsets cannot overflow the Int range.
             */
            val startIndex = start.toLong()
            val endIndex = startIndex + length.toLong()

            if (startIndex < 0 || length <= 0 || endIndex > randomAccessByteReader.contentLength)
                return null

            randomAccessByteReader.moveTo(startIndex.toInt())

            val previewBytes = randomAccessByteReader.readBytes(length)

            if (!previewBytes.startsWith(MediaFormatMagicNumbers.jpeg))
                return null

            return previewBytes
        }
    }
}
