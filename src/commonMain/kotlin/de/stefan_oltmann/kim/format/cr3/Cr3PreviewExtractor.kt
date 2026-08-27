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
package de.stefan_oltmann.kim.format.cr3

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BOX_HEADER_LENGTH
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.TYPE_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.read8BytesAsLong
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * Extracts preview images from Canon CR3 files.
 */
public object Cr3PreviewExtractor {

    /*
     * Skip one version byte, 3 bytes flags, 4 bytes sample size
     * and 4 bytes sample count.
     */
    private const val STSZ_SKIP_BYTES = 12

    /* Skip one version byte, 3 bytes flags and 4 bytes entry count */
    private const val CO64_SKIP_BYTES = 8

    /* Skip unknown bytes */
    private const val PRVW_UNKNOWN_BYTES = 8

    /* Skip size */
    private const val PRVW_SIZE_BYTES = 4

    /* Skip not interesting bytes */
    private const val PRVW_HEADER_BYTES = 12

    /*
     * Everything consumed before the JPEG bytes start: unknown bytes,
     * size, marker, header and the JPEG size field itself.
     */
    private const val PRVW_BYTES_BEFORE_JPEG =
        PRVW_UNKNOWN_BYTES + PRVW_SIZE_BYTES +
            4 /* marker */ + PRVW_HEADER_BYTES + 4 /* JPEG size field */

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? =
        extractFullSizePreviewImage(byteReader)

    /**
     * Extracts an preview image at full resolution.
     *
     * The mdat payload is never buffered: the movie box is parsed first
     * (Canon CR3 places it before the mdat), the absolute preview window
     * is computed from stsz/co64, and exactly those bytes are captured
     * while the mdat is streamed past in bounded chunks. Memory stays
     * flat no matter how large the video data of the CR3 is.
     *
     * See https://github.com/lclevy/canon_cr3/blob/master/readme.md
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractFullSizePreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        var movieBox: MovieBox? = null

        var previewBytes: ByteArray? = null

        var position = 0L

        while (previewBytes == null) {

            val available = byteReader.contentLength - position

            /* Enough bytes for a box header must remain. */
            if (available < BOX_HEADER_LENGTH)
                break

            val boxOffset = position

            var headerLength = BOX_HEADER_LENGTH.toLong()

            var largeSize: Long? = null

            var size = byteReader.read4BytesAsInt("length", BMFF_BYTE_ORDER).toLong()

            val typeBytes = byteReader.readBytes("type", TYPE_LENGTH)

            val type = BoxType.of(typeBytes)

            when (size) {

                0L -> size = available // The last box extends to the end of the file.

                1L -> {
                    size = byteReader.read8BytesAsLong("largesize", BMFF_BYTE_ORDER)
                    largeSize = size

                    /* The 64-bit largesize field extends the box header. */
                    val LARGE_SIZE_FIELD_LENGTH = 8L

                    headerLength += LARGE_SIZE_FIELD_LENGTH
                }
            }

            if (size !in 1..available)
                throw ImageReadException("Box $type has an invalid size: $size.")

            val dataSize = (size - headerLength).toInt()

            when (type) {

                BoxType.MOOV -> {

                    /*
                     * The movie box is small metadata, so buffering it is
                     * fine - unlike the mdat that follows it.
                     */
                    val payload = byteReader.readBytes("moov", dataSize)

                    movieBox = MovieBox(boxOffset, size, largeSize, payload, depth = 1)
                }

                BoxType.MDAT -> {

                    /*
                     * The window is known once the movie box was parsed,
                     * which Canon CR3 guarantees happens before the mdat.
                     */
                    val window = movieBox?.let(::computePreviewWindow)

                    if (window == null) {

                        byteReader.skipBytes("mdat data", dataSize.toLong())

                    } else {

                        val (windowOffset, windowLength) = window

                        val dataStart = boxOffset + headerLength

                        val relativeOffset = windowOffset - dataStart

                        /*
                         * Hostile or corrupt files can declare offsets that
                         * reach beyond this mdat; such windows are not
                         * captured here. The arithmetic runs in Long space,
                         * so huge deltas cannot wrap into the valid range.
                         */
                        if (relativeOffset >= 0 &&
                            relativeOffset + windowLength <= dataSize
                        ) {

                            byteReader.skipBytes("", relativeOffset)

                            previewBytes = byteReader.readBytes("preview jpeg", windowLength)

                            byteReader.skipBytes(
                                "mdat tail",
                                (dataSize - relativeOffset - windowLength)
                            )

                        } else {

                            byteReader.skipBytes("mdat data", dataSize.toLong())
                        }
                    }
                }

                else -> byteReader.skipBytes("box data", dataSize.toLong())
            }

            position = boxOffset + size
        }

        /* Only real JPEGs are previews - like in the other extractors. */
        val preview = previewBytes?.takeIf { it.startsWith(MediaFormatMagicNumbers.jpeg) }

        return@tryWithImageReadException preview
    }

    /**
     * Computes the absolute offset and the length of the full-size preview
     * JPEG from the sample size and chunk offset boxes of the movie box.
     *
     * Returns NULL when the structure is missing one of them.
     */
    private fun computePreviewWindow(movieBox: MovieBox): Pair<Long, Int>? {

        val firstTrack = movieBox.boxes.filterIsInstance<TrackBox>().firstOrNull()
            ?: return null

        val mediaBox = firstTrack.mediaBox

        val mediaInformationContainer = mediaBox.boxes.find { it.type == BoxType.MINF }
            ?: return null

        val minfBoxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(mediaInformationContainer.payload),
            stopAfterMetadataRead = false
        )

        val sampleTableBox = minfBoxes.find { it.type == BoxType.STBL }
            ?: return null

        val stblBoxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(sampleTableBox.payload),
            stopAfterMetadataRead = false
        )

        val sampleSizesBox = stblBoxes.find { it.type == BoxType.STSZ }
            ?: return null

        val chunkOffsetBox = stblBoxes.find { it.type == BoxType.CO64 }
            ?: return null

        val stszReader = ByteArrayByteReader(sampleSizesBox.payload)

        stszReader.skipBytes("", STSZ_SKIP_BYTES)

        val length = stszReader.read4BytesAsInt("length", ByteOrder.BIG_ENDIAN)

        val co64Reader = ByteArrayByteReader(chunkOffsetBox.payload)

        co64Reader.skipBytes("", CO64_SKIP_BYTES)

        /*
         * co64 offsets are absolute positions in the file, so the preview
         * bytes can be read directly during the mdat stream - no need to
         * hold the mdat itself in memory.
         */
        val offset = co64Reader.read8BytesAsLong("offset", ByteOrder.BIG_ENDIAN)

        if (offset < 0 || length <= 0)
            return null

        return offset to length
    }

    /**
     * Extracts an JPG with an resoltion of 1620 x 1080
     *
     * See https://github.com/lclevy/canon_cr3?tab=readme-ov-file#prvw-preview
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractSmallPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val previewUuidBox = allBoxes.filterIsInstance<UuidBox>().find {
            it.uuidAsHex == Cr3Reader.CR3_PREVIEW_UUID
        } ?: return@tryWithImageReadException null

        val payloadReader = ByteArrayByteReader(previewUuidBox.data)

        /* Skip unknown bytes */
        payloadReader.skipBytes("", PRVW_UNKNOWN_BYTES)

        /* Skip size */
        payloadReader.skipBytes("size", PRVW_SIZE_BYTES)

        val marker = payloadReader.readBytes("marker", 4).decodeToString()

        if (marker != "PRVW")
            throw ImageReadException("Expected marker PRVW, but got: $marker")

        /* Not interesting bytes */
        payloadReader.skipBytes("header", PRVW_HEADER_BYTES)

        val jpegSize = payloadReader.read4BytesAsInt("jpegSize", ByteOrder.BIG_ENDIAN)

        /*
         * A JPEG size beyond the available bytes means the preview is
         * truncated. The raw read would silently return a short array.
         */
        if (jpegSize <= 0 ||
            jpegSize > previewUuidBox.data.size - PRVW_BYTES_BEFORE_JPEG
        )
            return@tryWithImageReadException null

        val jpegBytes = payloadReader.readBytes("jpegBytes", jpegSize)

        /* Only real JPEGs are previews - like in the other extractors. */
        if (!jpegBytes.startsWith(MediaFormatMagicNumbers.jpeg))
            return@tryWithImageReadException null

        return@tryWithImageReadException jpegBytes
    }
}
