/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.bmff.box.FileTypeBox
import de.stefan_oltmann.kim.format.bmff.box.HandlerReferenceBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInfoEntryBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInformationBox
import de.stefan_oltmann.kim.format.bmff.box.ItemLocationBox
import de.stefan_oltmann.kim.format.bmff.box.MediaBox
import de.stefan_oltmann.kim.format.bmff.box.MediaDataBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBoxTopLevel
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.PrimaryItemBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
import de.stefan_oltmann.kim.format.bmff.box.TrackHeaderBox
import de.stefan_oltmann.kim.format.bmff.box.UserDataBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.format.jxl.box.JxlParticalCodestreamBox
import de.stefan_oltmann.kim.format.jxl.box.XmlBox
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.read8BytesAsLong
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Reads ISOBMFF boxes.
 */
public object BoxReader {

    /*
     * Real files nest container boxes only a few levels deep
     * (moov > trak > mdia > meta), so this limit only rejects hostile input.
     */
    private const val MAX_BOX_DEPTH: Int = 16

    /** Chunk size for reading possibly-truncated box payloads. */
    private const val READ_CHUNK_SIZE: Long = 64 * 1024

    /**
     * @param byteReader The reader as source for the bytes
     * @param stopAfterMetadataRead If reading the file for metadata on the highest level we
     * want to stop reading after the top-level meta boxes to prevent reading the whole image data
     * block in. For iPhone HEIC this is possible, but Samsung HEIC has "meta" coming after "mdat"
     * @param stopBeforeImageData If reading a JPEG XL file for an update we want to stop reading
     * before the image data starts, so the image data can be streamed without buffering the whole
     * file. The first JXLP box contains the codestream header, so every following JXLP box is
     * image data. The cut box is returned with an empty payload, because its content is streamed
     * by the caller.
     * @param positionOffset The position where to start reading boxes
     * @param offsetShift The shift to apply to the reported box offsets
     * @param updatePosition A callback to report the position when reading has finished
     * @param parentBoxType can be used to specify the type of the parent box - used when traversing
     * through sub boxes. This can change the logic for parsing boxes as "meta" boxes within a sub
     * box need to be treated differently to "meta" boxes at the top level.
     * @param depth The nesting level of this box within its containers, used to bound the
     * recursion for hostile files that nest container boxes arbitrarily.
     */
    @Suppress("NestedBlockDepth")
    public fun readBoxes(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false,
        stopBeforeImageData: Boolean = false,
        positionOffset: Long = 0,
        offsetShift: Long = 0,
        updatePosition: ((Long) -> Unit)? = null,
        parentBoxType: BoxType? = null,
        depth: Int = 0
    ): List<Box> {

        if (depth >= MAX_BOX_DEPTH)
            throw ImageReadException("Boxes are nested too deeply: $depth levels.")

        var haveSeenJxlHeaderBox = false

        var haveSeenTopLevelMetaBox = false

        var haveSeenXmpDataInUuid = false

        var haveSeenJxlpBox = false

        val boxes = mutableListOf<Box>()

        var position: Long = positionOffset

        while (true) {

            val available = byteReader.contentLength - position

            /*
             * Check if there are enough bytes for another box.
             * If so, we at least need the 8 header bytes.
             */
            if (available < BMFFConstants.BOX_HEADER_LENGTH)
                break

            val offset: Long = position

            /* Note: The length includes the 8 header bytes. */
            val size: Long =
                byteReader.read4BytesAsInt("length", BMFF_BYTE_ORDER).toLong()

            val type = BoxType.of(
                byteReader.readBytes("type", BMFFConstants.TYPE_LENGTH)
            )

            position += BMFFConstants.BOX_HEADER_LENGTH

            /*
             * If we read an JXL file and we already have seen the header,
             * all reamining JXLP boxes are image data that we can skip.
             */
            if (stopAfterMetadataRead && type == BoxType.JXLP && haveSeenJxlHeaderBox)
                break

            var largeSize: Long? = null

            val actualLength: Long = when (size) {

                /* A vaule of zero indicates that it's the last box. */
                0L -> available

                /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
                1L -> {
                    largeSize = byteReader.read8BytesAsLong("length", BMFF_BYTE_ORDER)
                    largeSize
                }

                /* Keep the length we already read. */
                else -> size
            }

            /*
             * Sizes of 2^31 bytes and above cannot be represented by the
             * signed read count, so such boxes must be rejected instead of
             * producing a corrupted read.
             */
            if (actualLength <= 0)
                throw ImageReadException("Box $type has an invalid size: $size.")

            /*
             * A box smaller than its own header would rewind the metadata
             * scan position and re-parse consumed bytes as boxes. The
             * streaming writer rejects the same input.
             */
            if (actualLength < BMFFConstants.BOX_HEADER_LENGTH)
                throw ImageReadException(
                    "Box $type declares a size smaller than its header: $size."
                )

            /*
             * The first JXLP box contains the codestream header, so every
             * following JXLP box is image data. It is returned with an empty
             * payload, because the caller streams its content.
             */
            if (stopBeforeImageData && type == BoxType.JXLP && haveSeenJxlpBox) {

                boxes.add(Box(type, offset, size, largeSize, ByteArray(0)))

                break
            }

            /*
             * A JXLC box carries the complete codestream. Like a following
             * JXLP box it is pure image data for an update: it is cut here
             * with an empty payload, so its content is streamed by the
             * caller instead of buffering the whole codestream in memory.
             */
            if (stopBeforeImageData && type == BoxType.JXLC) {

                boxes.add(Box(type, offset, size, largeSize, ByteArray(0)))

                break
            }

            val nextBoxOffset = offset + actualLength

            @Suppress("MagicNumber")
            if (size == 1L)
                position += 8

            val remainingBytesToReadInThisBox = nextBoxOffset - position

            /*
             * The payload is read into memory, so boxes larger than
             * Int.MAX_VALUE bytes must be rejected instead of overflowing
             * the read count.
             */
            if (remainingBytesToReadInThisBox > Int.MAX_VALUE)
                throw ImageReadException(
                    "Box $type is too large: $remainingBytesToReadInThisBox bytes."
                )

            /*
             * Attention: When the reader retains every consumed byte (the
             * metadata path wraps the stream in a CopyByteReader, because
             * meta boxes after the mdat box need already-read regions for
             * their extent re-reads), a large mdat payload must not ALSO
             * be kept inside its box object - that would hold the whole
             * image data twice. Nothing reads the box payload in that
             * mode, so it is dropped immediately.
             */
            var payloadTruncated = false

            val bytes: ByteArray = when {

                type == BoxType.MDAT &&
                    stopAfterMetadataRead &&
                    byteReader is SelfRetainingByteReader -> {

                    val retained = readPayloadUpToEof(
                        byteReader,
                        remainingBytesToReadInThisBox.toInt()
                    )

                    payloadTruncated = retained.size < remainingBytesToReadInThisBox

                    /* The reader itself retains the bytes. */
                    ByteArray(0)
                }

                stopAfterMetadataRead -> {

                    val payload = readPayloadUpToEof(
                        byteReader,
                        remainingBytesToReadInThisBox.toInt()
                    )

                    payloadTruncated = payload.size < remainingBytesToReadInThisBox

                    payload
                }

                else ->
                    byteReader.readBytes("data", remainingBytesToReadInThisBox.toInt())
            }

            position += remainingBytesToReadInThisBox

            val globalOffset = offset + offsetShift

            val box = when (type) {
                /* Generic EIC/ISO 14496-12 boxes. */
                BoxType.FTYP -> FileTypeBox(globalOffset, size, largeSize, bytes)
                BoxType.META -> if (parentBoxType == null) {
                    MetaBoxTopLevel(globalOffset, size, largeSize, bytes, depth + 1)
                } else {
                    MetaBox(globalOffset, size, largeSize, bytes, depth + 1)
                }

                BoxType.HDLR -> HandlerReferenceBox(globalOffset, size, largeSize, bytes)
                BoxType.IINF -> ItemInformationBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.INFE -> ItemInfoEntryBox(globalOffset, size, largeSize, bytes)
                BoxType.ILOC -> ItemLocationBox(globalOffset, size, largeSize, bytes)
                BoxType.PITM -> PrimaryItemBox(globalOffset, size, largeSize, bytes)
                BoxType.MDAT -> MediaDataBox(globalOffset, size, largeSize, bytes)
                BoxType.MOOV -> MovieBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.TRAK -> TrackBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.TKHD -> TrackHeaderBox(globalOffset, size, largeSize, bytes)
                BoxType.MDIA -> MediaBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.UUID -> UuidBox(globalOffset, size, largeSize, bytes)
                BoxType.UDTA -> UserDataBox(globalOffset, size, largeSize, bytes, depth + 1)
                /* JXL boxes */
                BoxType.EXIF -> ExifBox(globalOffset, size, largeSize, bytes)
                BoxType.XML -> XmlBox(globalOffset, size, largeSize, bytes)
                BoxType.JXLP -> JxlParticalCodestreamBox(globalOffset, size, largeSize, bytes)
                BoxType.BROB -> CompressedBox(globalOffset, size, largeSize, bytes)
                /* Unknown box */
                else -> Box(type, globalOffset, size, largeSize, bytes)
            }

            boxes.add(box)

            /*
             * An interrupted recording cuts the file inside a box payload
             * while the header still declares the full size. In the
             * read-metadata path the boxes parsed so far are returned and
             * reading ends here - there cannot be any further boxes after
             * a truncation.
             */
            if (payloadTruncated)
                break

            if (type == BoxType.JXLP)
                haveSeenJxlpBox = true

            if (stopAfterMetadataRead) {

                /* Metadata is here for most HEIC & AVIF */
                if (type == BoxType.META && parentBoxType == null) {
                    haveSeenTopLevelMetaBox = true

                    box as MetaBoxTopLevel

                    /*
                     * If this box references XMP data, we can break. If it's missing XMP, we should
                     * continue reading the file to search for an XMP UUID box (or break now if
                     * we've already seen it)
                     */
                    if (box.referencesXmp || haveSeenXmpDataInUuid) {
                        break
                    }
                }

                /* Some store XMP data in a UUID box instead */
                if (type == BoxType.UUID) {
                    box as UuidBox

                    /*
                     * If this box contains XMP, we can break as soon as we also find the top-level
                     *  META box (or break now if we've already seen it)
                     */
                    if (box.isXmp) {
                        haveSeenXmpDataInUuid = true
                        if (haveSeenTopLevelMetaBox) break
                    }
                }

                /*
                 * When parsing JXL we need to take a note that we saw the header.
                 * This is usually the first JXLP box.
                 */
                if (type == BoxType.JXLP) {

                    box as JxlParticalCodestreamBox

                    if (box.isHeader)
                        haveSeenJxlHeaderBox = true
                }
            }
        }

        updatePosition?.let { it(position) }

        return boxes
    }

    /**
     * Reads exactly [count] bytes, or everything up to the end of the
     * stream. A result shorter than [count] means the stream ended inside
     * the box payload - an interrupted recording.
     *
     * Only used in the read-metadata path; write paths read via
     * [ByteReader.readBytes] and fail loudly on truncation.
     */
    private fun readPayloadUpToEof(byteReader: ByteReader, count: Int): ByteArray {

        val writer = ByteArrayByteWriter()

        var remaining = count.toLong()

        while (remaining > 0) {

            val chunk = byteReader.readBytes(minOf(remaining, READ_CHUNK_SIZE).toInt())

            if (chunk.isEmpty())
                break

            writer.write(chunk)

            remaining -= chunk.size
        }

        return writer.toByteArray()
    }
}
