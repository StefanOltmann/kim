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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataExtractor
import de.stefan_oltmann.kim.format.png.chunk.PngChunkExif
import de.stefan_oltmann.kim.input.ByteReader

/**
 * Extracts the metadata bytes of PNG files.
 */
public object PngMetadataExtractor : MetadataExtractor {

    private const val INT32_BYTE_SIZE: Int = 4

    /* Length of black pixel chunk data. */
    private val fakeImageDataChunkLength: List<Byte> = listOf(
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0C.toByte()
    )

    /* Only black pixels chunk data */
    private val fakeImageDataChunkData: List<Byte> = listOf(
        0x08.toByte(), 0xD7.toByte(), 0x63.toByte(), 0x60.toByte(), 0x60.toByte(), 0x60.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x00.toByte(), 0x01.toByte()
    )

    /* CRC of a black pixels chunk data */
    private val fakeImageDataChunkCrc: List<Byte> = listOf(
        0x27.toByte(), 0x34.toByte(), 0x27.toByte(), 0x0A.toByte()
    )

    /* The end marker chunk has a zero length. */
    private val imageEndChunkLength: List<Byte> = listOf(
        0.toByte(), 0.toByte(), 0.toByte(), 0.toByte()
    )

    /*
     * CRC of the "IEND" chunk type bytes. Derived from the CRC algorithm itself,
     * so a hardcoded literal can never diverge from it again.
     */
    @Suppress("MagicNumber")
    private val imageEndChunkCrc: List<Byte> = run {

        val crc = PngCrc.finishPartialCrc(PngCrc.startPartialCrc(PngChunkType.IEND.bytes)).toInt()

        listOf(
            (crc ushr 24 and 0xFF).toByte(),
            (crc ushr 16 and 0xFF).toByte(),
            (crc ushr 8 and 0xFF).toByte(),
            crc.and(0xFF).toByte()
        )
    }

    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod", "LoopWithTooManyJumpStatements")
    override fun extractMetadataBytes(
        byteReader: ByteReader
    ): ByteArray = tryWithImageReadException {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = byteReader.readBytes(MediaFormatMagicNumbers.png.size).toList()

        /* Ensure it's actually a PNG. */
        require(magicNumberBytes == MediaFormatMagicNumbers.png) {
            "PNG magic number mismatch: ${magicNumberBytes.toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        /*
         * A chunk has this structure:
         *
         * 4 bytes - length of chunk data (unsigned, but always within 31 bytes)
         * 4 bytes - chunk type, ASCII representation like "IHDR" or "IEND"
         * * bytes - chunk data, variable length (see first 4 bytes)
         * 4 bytes - CRC calculated from type and data
         *
         * Even the file start and end markers are chunks.
         */
        while (true) {

            val chunkDataLengthBytes = byteReader.readBytes(INT32_BYTE_SIZE)

            val chunkDataLength = chunkDataLengthBytes.toInt(0, PngConstants.PNG_BYTE_ORDER)

            /* If the number is negative we have an integer overflow. */
            if (chunkDataLength < 0)
                throw ImageReadException("PNG chunk length exceeds maximum")

            val chunkTypeBytes = byteReader.readBytes(INT32_BYTE_SIZE)

            val chunkType = PngChunkType.of(chunkTypeBytes)

            /*
             * We replace the first image data chunk with a fake black pixel
             * chunk data and quit. Valid PNGs require one image data block.
             */
            if (chunkType == PngChunkType.IDAT) {

                bytes.addAll(fakeImageDataChunkLength)
                bytes.addAll(PngChunkType.IDAT.bytes.toList())
                bytes.addAll(fakeImageDataChunkData)
                bytes.addAll(fakeImageDataChunkCrc)

                break
            }

            /* Break if we reached the end marker. */
            if (chunkType == PngChunkType.IEND)
                break

            bytes.addAll(chunkDataLengthBytes.toList())
            bytes.addAll(chunkTypeBytes.toList())

            /* Chunk data. */
            byteReader.readAndAddBytes(bytes, chunkDataLength)

            /* CRC bytes. */
            byteReader.readAndAddBytes(bytes, INT32_BYTE_SIZE)
        }

        /* Write the end tag. */
        bytes.addAll(imageEndChunkLength)
        bytes.addAll(PngChunkType.IEND.bytes.toList())
        bytes.addAll(imageEndChunkCrc)

        return@tryWithImageReadException bytes.toByteArray()
    }

    internal fun extractExifBytes(reader: ByteReader): ByteArray? {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = reader.readBytes(MediaFormatMagicNumbers.png.size).toList()

        /* Ensure it's actually a PNG. */
        require(magicNumberBytes == MediaFormatMagicNumbers.png) {
            "PNG magic number mismatch: ${magicNumberBytes.toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        /*
         * A chunk has this structure:
         *
         * 4 bytes - length of chunk data (unsigned, but always within 31 bytes)
         * 4 bytes - chunk type, ASCII representation like "IHDR" or "IEND"
         * * bytes - chunk data, variable length (see first 4 bytes)
         * 4 bytes - CRC calculated from type and data
         *
         * Even the file start and end markers are chunks.
         */
        while (true) {

            val chunkDataLength = reader.readBytes(INT32_BYTE_SIZE).toInt(0, PngConstants.PNG_BYTE_ORDER)

            /* If the number is negative we have an integer overflow. */
            if (chunkDataLength < 0)
                throw ImageReadException("PNG chunk length exceeds maximum")

            val chunkTypeBytes = reader.readBytes(INT32_BYTE_SIZE)

            val chunkType = PngChunkType.of(chunkTypeBytes)

            /* Break if we reached the image data or end marker. */
            if (chunkType == PngChunkType.IDAT || chunkType == PngChunkType.IEND)
                return null

            val chunkBytes = reader.readBytes(chunkDataLength)

            if (chunkType == PngChunkType.EXIF)
                return chunkBytes

            /* The compressed variant is reported as the TIFF bytes it holds. */
            if (chunkType == PngChunkType.ZXIF)
                return PngChunkExif.prepareTiffBytes(chunkBytes)

            reader.readBytes(INT32_BYTE_SIZE)
        }
    }

    private fun ByteReader.readAndAddBytes(
        byteList: MutableList<Byte>,
        count: Int
    ): ByteArray {
        val bytes = readBytes(count)
        byteList.addAll(bytes.toList())
        return bytes
    }
}
