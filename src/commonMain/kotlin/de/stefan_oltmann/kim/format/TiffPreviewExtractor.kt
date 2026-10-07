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
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
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
         * Reads the preview described by the given start/length tag pair
         * of [directory].
         *
         * This is the shared skeleton of the TIFF-family preview
         * extractors: read both tags, ignore an empty length, and let
         * [readValidatedPreviewBytes] decide whether the bytes are a
         * usable preview. A NULL directory (the format variant that uses
         * this extractor does not exist in the file) reports no preview.
         */
        internal fun previewFromTags(
            directory: TiffDirectory?,
            randomAccessByteReader: RandomAccessByteReader,
            startTag: TagInfoLong,
            lengthTag: TagInfoLong
        ): ByteArray? {

            if (directory == null)
                return null

            val previewImageStart = directory.getFieldValue(startTag)
                ?: return null

            val previewLength = directory.getFieldValue(lengthTag)
                ?: return null

            if (previewLength == 0)
                return null

            return readValidatedPreviewBytes(
                randomAccessByteReader = randomAccessByteReader,
                start = previewImageStart,
                length = previewLength
            )
        }

        /**
         * Reads the claimed preview bytes and validates them.
         *
         * Some files carry random garbage in the preview tags, so like
         * [de.stefan_oltmann.kim.format.tiff.TiffReader] for thumbnails,
         * out-of-bounds ranges and data without the JPEG signature are
         * rejected with NULL instead of returning unusable bytes. The
         * content length of stream readers is only a hint that may
         * understate the data, so the real read decides.
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

            if (startIndex < 0 || length <= 0)
                return null

            randomAccessByteReader.moveTo(startIndex.toInt())

            val previewBytes = randomAccessByteReader.readBytes(length)

            /* A short read means the preview range does not exist. */
            if (previewBytes.size != length)
                return null

            if (!previewBytes.startsWith(MediaFormatMagicNumbers.jpeg))
                return null

            return previewBytes
        }
    }
}
