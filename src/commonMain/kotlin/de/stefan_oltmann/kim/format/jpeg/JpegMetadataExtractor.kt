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
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.input.ByteReader

/**
 * Extracts the metadata bytes of JPEG files.
 */
public object JpegMetadataExtractor : MetadataExtractor {

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

        val scanner = JpegMarkerScanner(byteReader)

        /*
         * Counted in Long space, so streams larger than the signed Int
         * range cannot wrap the counter and silently disable the
         * truncation checks below.
         */
        var readBytesCount = 0L

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            for (consumedByte in scan.consumedBytes)
                bytes.add(consumedByte)

            readBytesCount += scan.consumedBytes.size

            if (scan.marker == SOS_MARKER || scan.marker == EOI_MARKER)
                break

            val segmentLengthFirstByte = byteReader.readByte() ?: break
            val segmentLengthSecondByte = byteReader.readByte() ?: break

            bytes.add(segmentLengthFirstByte)
            bytes.add(segmentLengthSecondByte)

            readBytesCount += 2

            /* Next 2-bytes are <segment-size>: [high-byte] [low-byte] */
            var segmentLength: Int = byteArrayOf(segmentLengthFirstByte, segmentLengthSecondByte)
                .toUInt16(ByteOrder.BIG_ENDIAN)

            /* Segment length includes size bytes, so subtract two */
            segmentLength -= 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /* A zero content length is an empty segment, which is spec-legal. */
            if (segmentLength < 0 || segmentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            val segmentBytes = byteReader.readBytes(segmentLength)

            if (segmentBytes.size != segmentLength)
                throw ImageReadException("Incomplete read: ${segmentBytes.size} != $segmentLength")

            bytes.addAll(segmentBytes.asList())

        } while (true)
    }
}
