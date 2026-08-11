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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataExtractor
import de.stefan_oltmann.kim.input.ByteReader

/**
 * Extracts the metadata bytes of JPEG files.
 */
public object JpegMetadataExtractor : MetadataExtractor {

    internal const val SEGMENT_IDENTIFIER = 0xFF.toByte()
    internal const val SEGMENT_START_OF_SCAN = 0xDA.toByte()
    internal const val MARKER_END_OF_IMAGE = 0xD9.toByte()

    private const val ADDITIONAL_BYTE_COUNT_AFTER_HEADER: Int = 12

    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    override fun extractMetadataBytes(
        byteReader: ByteReader
    ): ByteArray = tryWithImageReadException {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = byteReader.readBytes(MediaFormatMagicNumbers.jpeg.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == MediaFormatMagicNumbers.jpeg) {
            "JPEG magic number mismatch: ${magicNumberBytes.toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        readSegmentBytesIntoList(byteReader, bytes)

        /*
         * Add some more bytes after the header, so it's recognized
         * by most image viewers as a valid (but broken) file.
         */
        repeat(ADDITIONAL_BYTE_COUNT_AFTER_HEADER) {

            byteReader.readByte()?.let {
                bytes.add(it)
            }
        }

        return@tryWithImageReadException bytes.toByteArray()
    }

    internal fun readSegmentBytesIntoList(
        byteReader: ByteReader,
        bytes: MutableList<Byte>
    ) {

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            var segmentIdentifier = byteReader.readByte() ?: break
            var segmentType = byteReader.readByte() ?: break

            bytes.add(segmentIdentifier)
            bytes.add(segmentType)

            /*
             * Find the segment marker. Markers are zero or more 0xFF bytes, followed by
             * a 0xFF and then a byte not equal to 0x00 or 0xFF.
             */
            while (
                segmentIdentifier != SEGMENT_IDENTIFIER ||
                segmentType == SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                bytes.add(nextSegmentType)

                segmentType = nextSegmentType
            }

            if (segmentType == SEGMENT_START_OF_SCAN || segmentType == MARKER_END_OF_IMAGE)
                break

            val segmentLengthFirstByte = byteReader.readByte() ?: break
            val segmentLengthSecondByte = byteReader.readByte() ?: break

            bytes.add(segmentLengthFirstByte)
            bytes.add(segmentLengthSecondByte)

            /* Next 2-bytes are <segment-size>: [high-byte] [low-byte] */
            var segmentLength: Int = byteArrayOf(segmentLengthFirstByte, segmentLengthSecondByte)
                .toUInt16(ByteOrder.BIG_ENDIAN)

            /* Segment length includes size bytes, so subtract two */
            segmentLength -= 2

            val remainingByteCount = byteReader.contentLength - bytes.size

            /* Reject invalid segment lengths */
            if (segmentLength <= 0 || segmentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            val segmentBytes = byteReader.readBytes(segmentLength)

            if (segmentBytes.size != segmentLength)
                throw ImageReadException("Incomplete read: ${segmentBytes.size} != $segmentLength")

            bytes.addAll(segmentBytes.asList())

        } while (true)
    }
}
