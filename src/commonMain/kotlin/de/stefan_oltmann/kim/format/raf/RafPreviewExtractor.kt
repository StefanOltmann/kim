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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * Extracts the preview image of Fuji RAF files.
 *
 * NULL means the file carries no preview. A preview whose header declares
 * an unusable range or whose payload is broken fails with
 * [ImageReadException]: RAW files are never written by this library and
 * their preview is the only way applications can display them.
 */
public object RafPreviewExtractor {

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractPreviewImage(
        reader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        reader.readAndVerifyBytes(
            "RAF magic number",
            MediaFormatMagicNumbers.raf.toByteArray()
        )

        reader.skipBytes("68 header bytes", RafEmbeddedJpeg.REMAINING_HEADER_BYTE_COUNT)

        val offset = reader.read4BytesAsInt("JPEG offset", ByteOrder.BIG_ENDIAN)

        val length = reader.read4BytesAsInt("JPG length", ByteOrder.BIG_ENDIAN)

        /*
         * Hostile headers must not trigger huge or negative skips: a
         * range that leaves the file is a broken preview, which fails
         * the extraction (see Kim.extractPreviewImage) instead of
         * hiding the damaged file behind NULL.
         */
        if (offset <= 0 || length <= 0 || offset.toLong() + length > reader.contentLength)
            throw ImageReadException(
                "The RAF preview range is invalid: offset=$offset, " +
                    "length=$length, content=${reader.contentLength}."
            )

        @Suppress("MagicNumber")
        val remainingBytesToOffset = offset -
            (RafEmbeddedJpeg.REMAINING_HEADER_BYTE_COUNT + MediaFormatMagicNumbers.raf.size + 8)

        if (remainingBytesToOffset < 0)
            throw ImageReadException(
                "The RAF preview offset is inconsistent with the header: " +
                    "offset=$offset."
            )

        reader.skipBytes("Skip JPEG offset", remainingBytesToOffset)

        val previewBytes = reader.readBytes(length)

        if (!previewBytes.startsWith(MediaFormatMagicNumbers.jpeg))
            throw ImageReadException("The RAF preview payload is not a JPEG.")

        return previewBytes
    }
}
