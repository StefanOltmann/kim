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
package de.stefan_oltmann.kim.format.png.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MAX_DECOMPRESSED_BYTE_COUNT
import de.stefan_oltmann.kim.common.decompressBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.png.PngChunkType
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader

/**
 * The EXIF chunk of a PNG file, in the raw `eXIf` variant or in the
 * compressed `zxIf` variant.
 */
public class PngChunkExif(
    chunkType: PngChunkType,
    bytes: ByteArray,
    crc: Int
) : PngChunk(chunkType, bytes, crc) {

    private val preparedTiffBytes: ByteArray = prepareTiffBytes(bytes)

    public val tiffContents: TiffContents = TiffReader.read(preparedTiffBytes)

    /**
     * The TIFF bytes of this chunk: the decompressed data for `zxIf`,
     * the chunk payload unchanged for `eXIf`.
     */
    public val exifBytes: ByteArray =
        if (type == PngChunkType.ZXIF) preparedTiffBytes else bytes

    public constructor(bytes: ByteArray, crc: Int) : this(PngChunkType.EXIF, bytes, crc)

    internal companion object {

        /*
         * Some writers add the JPEG style header although the PNG
         * specification defines the chunk as raw TIFF bytes. ExifTool
         * warns "Improper Exif00 header in EXIF chunk" for those and
         * still reads them.
         */
        val IMPROPER_HEADER: ByteArray = "Exif\u0000\u0000".encodeToByteArray()

        /*
         * zxIf header: one NUL byte that marks the chunk as compressed,
         * followed by the uncompressed byte count as a big endian uint32.
         */
        const val ZXIF_HEADER_LENGTH: Int = 5

        private const val ZERO_BYTE: Byte = 0

        /**
         * Normalizes the chunk payload to raw TIFF bytes: decompresses
         * the zxIf layout, strips the improper JPEG style header and
         * leaves standard TIFF bytes unchanged.
         *
         * A payload that fits none of the layouts fails the read, per
         * the strict read policy in the [de.stefan_oltmann.kim.Kim]
         * documentation.
         */
        internal fun prepareTiffBytes(bytes: ByteArray): ByteArray =
            when {
                bytes.isEmpty() ->
                    bytes

                bytes[0] == ZERO_BYTE ->
                    decompressZxIf(bytes)

                bytes.startsWith(IMPROPER_HEADER) ->
                    bytes.copyOfRange(IMPROPER_HEADER.size, bytes.size)

                else ->
                    bytes
            }

        private fun decompressZxIf(bytes: ByteArray): ByteArray {

            if (bytes.size <= ZXIF_HEADER_LENGTH)
                throw ImageReadException(
                    "The zxIf chunk carries no compressed data: ${bytes.size} bytes."
                )

            val declaredByteCount =
                ((bytes[1].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[3].toInt() and 0xFF) shl 8) or
                    (bytes[4].toInt() and 0xFF)

            if (declaredByteCount <= 0 || declaredByteCount > MAX_DECOMPRESSED_BYTE_COUNT)
                throw ImageReadException(
                    "The zxIf chunk declares an invalid uncompressed size: $declaredByteCount."
                )

            val decompressedBytes = decompressBytes(
                bytes.copyOfRange(ZXIF_HEADER_LENGTH, bytes.size)
            )

            if (decompressedBytes.size != declaredByteCount)
                throw ImageReadException(
                    "The zxIf chunk declares $declaredByteCount bytes, " +
                        "but decompressed to ${decompressedBytes.size}."
                )

            return decompressedBytes
        }
    }
}
