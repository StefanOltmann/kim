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
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BOX_HEADER_LENGTH
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.TYPE_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
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
    /* Version, flags and entry count are identical for co64 and stco. */
    private const val CHUNK_OFFSET_SKIP_BYTES = 8

    /* Third-party muxers write 32-bit chunk offsets in an stco box. */
    private val STCO_BOX_TYPE: BoxType = BoxType.of("stco".encodeToByteArray())

    /* Skip unknown bytes */
    private const val PRVW_UNKNOWN_BYTES = 8

    /* Skip size */
    private const val PRVW_SIZE_BYTES = 4

    /* The 64-bit largesize field extends the box header. */
    private const val LARGE_SIZE_FIELD_LENGTH = 8L

    /* The vendor UUID at the start of every UUID box payload. */
    private const val UUID_LENGTH_BYTES = 16

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

            val header = readTopLevelBoxHeader(byteReader, position) ?: break

            when (header.type) {

                BoxType.MOOV -> {

                    /*
                     * The movie box is small metadata, so buffering it is
                     * fine - unlike the mdat that follows it. A stream that
                     * claims more bytes than it delivers (e.g. an aborted
                     * transfer) fails the moov read; without a parsed moov
                     * no preview window can ever be located, so the preview
                     * degrades to NULL right here - continuing the box walk
                     * on the desynced reader position would only produce a
                     * spurious error. A physically truncated file fails
                     * loudly in the box header validation like every other
                     * corrupt input.
                     */
                    movieBox = try {
                        val payload = header.readData(byteReader)

                        MovieBox(
                            header.boxOffset,
                            header.size,
                            header.largeSize,
                            payload,
                            depth = 1
                        )
                    } catch (_: ImageReadException) {
                        return@tryWithImageReadException null
                    }
                }

                BoxType.MDAT -> {

                    /*
                     * The window is known once the movie box was parsed,
                     * which Canon CR3 guarantees happens before the mdat.
                     */
                    val window = movieBox?.let(::computePreviewWindow)

                    if (window == null) {

                        byteReader.skipBytes("mdat data", header.dataSize)

                    } else {

                        val (windowOffset, windowLength) = window

                        val dataStart = header.boxOffset + header.headerLength

                        val relativeOffset = windowOffset - dataStart

                        /*
                         * Hostile or corrupt files can declare offsets that
                         * reach beyond this mdat; such windows are not
                         * captured here. The arithmetic runs in Long space,
                         * so huge deltas cannot wrap into the valid range.
                         */
                        if (relativeOffset >= 0 &&
                            relativeOffset + windowLength <= header.dataSize
                        ) {

                            byteReader.skipBytes("", relativeOffset)

                            previewBytes = byteReader.readBytes("preview jpeg", windowLength)

                            byteReader.skipBytes(
                                "mdat tail",
                                (header.dataSize - relativeOffset - windowLength)
                            )

                        } else {

                            byteReader.skipBytes("mdat data", header.dataSize)
                        }
                    }
                }

                else -> byteReader.skipBytes("box data", header.dataSize)
            }

            position = header.boxOffset + header.size
        }

        /* Only real JPEGs are previews - like in the other extractors. */
        val preview = previewBytes?.takeIf { it.startsWith(MediaFormatMagicNumbers.jpeg) }

        return@tryWithImageReadException preview
    }

    /**
     * One parsed top-level box header.
     *
     * All sizes stay in Long space: ISOBMFF sizes are unsigned 32- or
     * 64-bit values, so a box of 2 GiB and above must not truncate into a
     * negative data size during the skip arithmetic.
     */
    private class TopLevelBoxHeader(
        val boxOffset: Long,
        val headerLength: Long,
        val size: Long,
        val largeSize: Long?,
        val dataSize: Long,
        val type: BoxType
    ) {

        /**
         * Reads the box data as one bounded buffer. Only call this for
         * boxes that are small metadata, never for the mdat.
         */
        fun readData(byteReader: ByteReader): ByteArray {

            if (dataSize > Int.MAX_VALUE)
                throw ImageReadException(
                    "Box $type is too large to buffer: $dataSize bytes."
                )

            return byteReader.readBytes("box data", dataSize.toInt())
        }
    }

    /**
     * Reads one top-level box header at [position].
     *
     * Returns NULL when fewer bytes than a box header remain, which is the
     * clean end of the box walk.
     */
    private fun readTopLevelBoxHeader(
        byteReader: ByteReader,
        position: Long
    ): TopLevelBoxHeader? {

        val available = byteReader.contentLength - position

        /* Enough bytes for a box header must remain. */
        if (available < BOX_HEADER_LENGTH)
            return null

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
                headerLength += LARGE_SIZE_FIELD_LENGTH
            }
        }

        if (size !in 1..available)
            throw ImageReadException("Box $type has an invalid size: $size.")

        return TopLevelBoxHeader(
            boxOffset = boxOffset,
            headerLength = headerLength,
            size = size,
            largeSize = largeSize,
            dataSize = size - headerLength,
            type = type
        )
    }

    /**
     * Computes the absolute offset and the length of the full-size preview
     * JPEG from the sample size and chunk offset boxes of the movie box.
     *
     * Returns NULL when the structure is missing one of them.
     */
    private fun computePreviewWindow(movieBox: MovieBox): Pair<Long, Int>? =
        try {
            computePreviewWindowFrom(movieBox)
        } catch (_: ImageReadException) {

            /*
             * A sample table that cannot be parsed is a structural miss,
             * not a corrupt file: the preview degrades to NULL like every
             * other missing structure instead of failing the read.
             */
            null
        }

    private fun computePreviewWindowFrom(movieBox: MovieBox): Pair<Long, Int>? {

        val firstTrack = movieBox.boxes.filterIsInstance<TrackBox>().firstOrNull()
        firstTrack ?: return null

        val mediaBox = firstTrack.mediaBox

        val mediaInformationContainer = mediaBox.boxes.find { it.type == BoxType.MINF }
            ?: return null

        val minfBoxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(mediaInformationContainer.payload)
        )

        val sampleTableBox = minfBoxes.find { it.type == BoxType.STBL }
            ?: return null

        val stblBoxes = BoxReader.readAllBoxes(
            byteReader = ByteArrayByteReader(sampleTableBox.payload)
        )

        val sampleSizesBox = stblBoxes.find { it.type == BoxType.STSZ }
            ?: return null

        val chunkOffsetBox = stblBoxes.find { it.type == BoxType.CO64 }
            ?: stblBoxes.find { it.type == STCO_BOX_TYPE }
            ?: return null

        val stszReader = ByteArrayByteReader(sampleSizesBox.payload)

        stszReader.skipBytes("", STSZ_SKIP_BYTES)

        val length = stszReader.read4BytesAsInt("length", ByteOrder.BIG_ENDIAN)

        val chunkOffsetReader = ByteArrayByteReader(chunkOffsetBox.payload)

        chunkOffsetReader.skipBytes("", CHUNK_OFFSET_SKIP_BYTES)

        /*
         * Chunk offsets are absolute positions in the file, so the preview
         * bytes can be read directly during the mdat stream - no need to
         * hold the mdat itself in memory. co64 carries them as 64-bit
         * values, stco as unsigned 32-bit values.
         */
        val offset = if (chunkOffsetBox.type == BoxType.CO64)
            chunkOffsetReader.read8BytesAsLong("offset", ByteOrder.BIG_ENDIAN)
        else
            chunkOffsetReader.read4BytesAsInt("offset", ByteOrder.BIG_ENDIAN)
                .toLong() and 0xFFFFFFFFL

        if (offset < 0 || length <= 0)
            return null

        return offset to length
    }

    /**
     * Extracts an JPG with an resoltion of 1620 x 1080
     *
     * The box walk streams like in [extractFullSizePreviewImage]: the mdat
     * payload is skipped in bounded chunks and only the payload of the
     * Canon preview UUID box is buffered, so memory stays flat no matter
     * how large the video data of the CR3 is.
     *
     * See https://github.com/lclevy/canon_cr3?tab=readme-ov-file#prvw-preview
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractSmallPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        var previewBytes: ByteArray? = null

        var position = 0L

        while (previewBytes == null) {

            val header = readTopLevelBoxHeader(byteReader, position) ?: break

            if (header.type == BoxType.UUID) {

                /*
                 * A box too short to carry the vendor UUID is malformed;
                 * skipping it whole keeps the walk alive and degrades the
                 * preview to NULL, exactly like the box object filter did
                 * for such boxes before.
                 */
                if (header.dataSize < UUID_LENGTH_BYTES) {

                    byteReader.skipBytes("uuid box data", header.dataSize)
                } else {

                    /*
                     * The first payload bytes identify the vendor extension.
                     * Only the Canon preview extension is buffered; every
                     * other UUID box is skipped, exactly like the box object
                     * filter did before.
                     */
                    val uuidBytes = byteReader.readBytes("uuid", UUID_LENGTH_BYTES)

                    previewBytes =
                        if (uuidBytes.toHex() == Cr3Reader.CR3_PREVIEW_UUID)
                            parsePrvwPreview(
                                readPreviewPayload(
                                    byteReader = byteReader,
                                    dataLength = header.dataSize - UUID_LENGTH_BYTES
                                )
                            )
                        else
                            byteReader.skipBytes(
                                "uuid box data",
                                header.dataSize - UUID_LENGTH_BYTES
                            ).let { null }
                }
            } else {

                byteReader.skipBytes("box data", header.dataSize)
            }

            position = header.boxOffset + header.size
        }

        return@tryWithImageReadException previewBytes
    }

    /**
     * Buffers the preview payload behind the UUID. A payload that cannot
     * fit into a buffer is corrupt for a preview box and fails the read.
     */
    private fun readPreviewPayload(
        byteReader: ByteReader,
        dataLength: Long
    ): ByteArray {

        if (dataLength < 0 || dataLength > Int.MAX_VALUE)
            throw ImageReadException(
                "The CR3 preview box is too large to buffer: $dataLength bytes."
            )

        return byteReader.readBytes("preview payload", dataLength.toInt())
    }

    /**
     * Parses the preview JPEG out of a Canon PRVW UUID box payload.
     *
     * A wrong marker fails the read loudly; a truncated or non-JPEG
     * payload degrades to NULL like in the other extractors.
     */
    private fun parsePrvwPreview(data: ByteArray): ByteArray? {

        val payloadReader = ByteArrayByteReader(data)

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
            jpegSize > data.size - PRVW_BYTES_BEFORE_JPEG
        )
            return null

        val jpegBytes = payloadReader.readBytes("jpegBytes", jpegSize)

        /* Only real JPEGs are previews - like in the other extractors. */
        if (!jpegBytes.startsWith(MediaFormatMagicNumbers.jpeg))
            return null

        return jpegBytes
    }
}
