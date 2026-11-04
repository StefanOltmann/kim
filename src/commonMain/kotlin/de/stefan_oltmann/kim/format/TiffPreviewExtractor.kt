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
 *
 * NULL means the file carries no preview (the preview tags are absent or
 * empty). A preview whose tags are filled but whose data is broken fails
 * with [ImageReadException]: RAW files are never written by this library
 * and their preview is the only way applications can display them.
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
         * A preview whose tags are filled is part of the file's content:
         * an out-of-bounds range, a truncated read or data without the
         * JPEG signature is a broken preview and fails the extraction -
         * RAW files are never written by this library and their preview
         * is the only way applications can display them (see
         * [de.stefan_oltmann.kim.Kim.extractPreviewImage]). The content
         * length of stream readers is only a hint that may understate the
         * data, so the real read decides.
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
                throw ImageReadException(
                    "The preview range is invalid: start=$startIndex, length=$length."
                )

            randomAccessByteReader.moveTo(startIndex.toInt())

            val previewBytes = randomAccessByteReader.readBytes(length)

            /* A short read means the preview is truncated. */
            if (previewBytes.size != length)
                throw ImageReadException(
                    "The preview is truncated: ${previewBytes.size} of " +
                        "$length bytes are present."
                )

            if (!previewBytes.startsWith(MediaFormatMagicNumbers.jpeg))
                throw ImageReadException("The preview payload is not a JPEG.")

            return previewBytes
        }
    }
}
