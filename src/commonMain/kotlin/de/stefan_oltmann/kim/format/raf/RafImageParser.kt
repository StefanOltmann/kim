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
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.JpegImageParser
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.model.MediaFormat

/**
 * Parses the metadata of Fuji RAF files.
 */
public object RafImageParser : ImageParser {

    /**
     * The RAF file contains a JPEG with EXIF metadata.
     * We just have to find it and read the data from there it.
     */
    @Throws(ImageReadException::class)
    @Suppress("ComplexCondition", "LoopWithTooManyJumpStatements")
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            byteReader.readAndVerifyBytes(
                "RAF magic number",
                MediaFormatMagicNumbers.raf.toByteArray()
            )

            byteReader.skipBytes("86 header bytes", RafMetadataExtractor.REMAINING_HEADER_BYTE_COUNT)

            val offset = byteReader.read4BytesAsInt("JPEG offset", ByteOrder.BIG_ENDIAN)

            @Suppress("MagicNumber")
            val remainingBytesToOffset = offset -
                (RafMetadataExtractor.REMAINING_HEADER_BYTE_COUNT + MediaFormatMagicNumbers.raf.size + 4)

            byteReader.skipBytes("Skip JPEG offset", remainingBytesToOffset)

            return@tryWithImageReadException JpegImageParser
                .parseMetadata(byteReader)
                .withMediaFormat(mediaFormat = MediaFormat.RAF)
        }
}
