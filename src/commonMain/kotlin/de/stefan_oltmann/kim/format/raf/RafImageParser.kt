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
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            RafEmbeddedJpeg.positionReaderAtJpeg(byteReader)

            return@tryWithImageReadException JpegImageParser
                .parseMetadata(byteReader)
                .withMediaFormat(mediaFormat = MediaFormat.RAF)
        }

    /**
     * Reads the section directory of the RAF header, so callers can locate
     * the embedded JPEG, the CFA header and the CFA raw data block inside
     * the file.
     *
     * **Attention:** Must be public API as this is used by https://stefan-oltmann.de/exif-viewer
     */
    @Throws(ImageReadException::class)
    public fun readDirectory(byteReader: ByteReader): RafDirectory =
        tryWithImageReadException {

            with(byteReader) {

                readAndVerifyBytes(
                    "RAF magic number",
                    MediaFormatMagicNumbers.raf.toByteArray()
                )

                skipBytes("68 header bytes", RafEmbeddedJpeg.REMAINING_HEADER_BYTE_COUNT)

                val directory = RafDirectory(
                    jpegImageOffset = read4BytesAsInt("JPEG image offset", ByteOrder.BIG_ENDIAN).toLong(),
                    jpegImageLength = read4BytesAsInt("JPEG image length", ByteOrder.BIG_ENDIAN).toLong(),
                    cfaHeaderOffset = read4BytesAsInt("CFA header offset", ByteOrder.BIG_ENDIAN).toLong(),
                    cfaHeaderLength = read4BytesAsInt("CFA header length", ByteOrder.BIG_ENDIAN).toLong(),
                    cfaOffset = read4BytesAsInt("CFA offset", ByteOrder.BIG_ENDIAN).toLong(),
                    cfaLength = read4BytesAsInt("CFA length", ByteOrder.BIG_ENDIAN).toLong()
                )

                requireSectionInRange("JPEG image", directory.jpegImageOffset, directory.jpegImageLength, contentLength)
                requireSectionInRange("CFA header", directory.cfaHeaderOffset, directory.cfaHeaderLength, contentLength)
                requireSectionInRange("CFA", directory.cfaOffset, directory.cfaLength, contentLength)

                return@tryWithImageReadException directory
            }
        }
}

/**
 * Rejects a directory entry that cannot point into the file. All three
 * sections are mandatory parts of the format, so a length of zero is a
 * defect as well.
 */
private fun requireSectionInRange(
    name: String,
    offset: Long,
    length: Long,
    contentLength: Long
) {

    if (offset <= 0 || length <= 0 || offset + length > contentLength)
        throw ImageReadException(
            "The RAF $name section is out of range: offset $offset, length $length, file size $contentLength."
        )
}
