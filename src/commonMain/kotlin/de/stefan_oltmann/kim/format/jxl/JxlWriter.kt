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
package de.stefan_oltmann.kim.format.jxl

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.jxl.box.JxlParticalCodestreamBox
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.copyRemainingTo
import de.stefan_oltmann.kim.input.transferExactly
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.output.writeLong
import kotlin.jvm.JvmStatic

/**
 * Writes JPEG XL files.
 */
public object JxlWriter {

    /*
     * As a safety measure we don't want to write uncompressed boxes to
     * a file that already has compressed boxes. This might cause data loss.
     */
    private const val BROB_WARNING =
        "This file contains compressed data we can't yet read. " +
            "Writing to this file will result in data loss. " +
            "Please only update uncompressed metadata for now."

    @JvmStatic
    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ): Unit = writeImage(
        boxes = BoxReader.readBoxes(byteReader, false),
        byteWriter = byteWriter,
        exifBytes = exifBytes,
        xmp = xmp
    )

    /**
     * Streams a JPEG XL file from the given reader to the given writer, so
     * the updateComputer can rewrite the header boxes once the image data
     * starts.
     *
     * The updateComputer receives the boxes before the image data and the
     * output writer, and must write the complete header (all boxes) to it.
     * The image data behind the cut box is then streamed in bounded chunks,
     * so the whole file never has to be buffered in memory.
     *
     * Exif and xml boxes behind the cut box are dropped, because the
     * updateComputer cannot see them. Keeping them would leave stale
     * metadata in the file after an update or a metadata deletion.
     */
    internal fun writeImageStreaming(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updateComputer: (List<Box>, ByteWriter) -> Unit
    ) {

        val boxes = BoxReader.readBoxes(byteReader, stopBeforeImageData = true)

        updateComputer(boxes, byteWriter)

        val cutBox = boxes.lastOrNull()

        /*
         * The cut box is either the second JXLP box with an empty payload
         * or a JXLC box, because its content is streamed here.
         */
        val isCutBox =
            cutBox != null &&
                (cutBox.type == BoxType.JXLP || cutBox.type == BoxType.JXLC) &&
                cutBox.payload.isEmpty()

        if (isCutBox) {

            /* NULL means the cut box extends to the end of the stream. */
            val remainingPayloadLength: Long? = when (cutBox.size) {

                0L -> null

                1L -> requireNotNull(cutBox.largeSize) - 2 * BMFFConstants.BOX_HEADER_LENGTH

                else -> cutBox.size - BMFFConstants.BOX_HEADER_LENGTH
            }

            if (remainingPayloadLength == null) {

                byteReader.copyRemainingTo(byteWriter)

            } else {

                byteReader.transferExactly(byteWriter, remainingPayloadLength)

                copyBoxesSkippingMetadata(byteReader, byteWriter)
            }

        } else {

            byteReader.copyRemainingTo(byteWriter)
        }
    }

    /**
     * Streams the remaining boxes to the given writer, dropping Exif and
     * xml boxes, so stale metadata behind the codestream cannot survive an
     * update or a metadata deletion.
     *
     * Only these recognized metadata types are dropped, because the
     * caller asked for their replacement or removal. All other boxes,
     * including unknown ones, stream through untouched.
     */
    private fun copyBoxesSkippingMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) {

        while (true) {

            val header = byteReader.readBytes(BMFFConstants.BOX_HEADER_LENGTH)

            /* A truncated box header at the end of the stream. */
            if (header.size < BMFFConstants.BOX_HEADER_LENGTH) {

                byteWriter.write(header)

                return
            }

            val boxType = BoxType.of(
                header.copyOfRange(BMFFConstants.TYPE_LENGTH, BMFFConstants.BOX_HEADER_LENGTH)
            )

            val size = header.toInt(0, BMFF_BYTE_ORDER).toLong()

            /*
             * Sizes of 2^31 bytes and above cannot be represented by the
             * signed read count, so such boxes must be rejected instead of
             * producing a corrupted copy.
             */
            if (size < 0)
                throw ImageReadException("Box $boxType has an invalid size: $size.")

            var extraHeader = ByteArray(0)

            /* NULL means the box extends to the end of the stream. */
            val payloadLength: Long? = when (size) {

                0L -> null

                /* A size of one stores the real size in the following 8 bytes. */
                1L -> {

                    val largeSizeBytes = byteReader.readBytes(Long.SIZE_BYTES)

                    if (largeSizeBytes.size < Long.SIZE_BYTES) {

                        byteWriter.write(header)
                        byteWriter.write(largeSizeBytes)

                        return
                    }

                    extraHeader = largeSizeBytes

                    var largeSize = 0L

                    for (index in largeSizeBytes.indices)
                        largeSize = largeSize shl 8 or (largeSizeBytes[index].toLong() and 0xFF)

                    /*
                     * A largesize below both headers cannot hold any
                     * payload and is rejected through the negative
                     * payload length check below.
                     */
                    largeSize - 2 * BMFFConstants.BOX_HEADER_LENGTH
                }

                else -> size - BMFFConstants.BOX_HEADER_LENGTH
            }

            /*
             * A size below the header length cannot hold any payload.
             * Such malformed boxes behind the codestream would otherwise
             * desync the raw copy of the remaining file, so they are
             * rejected here instead of being treated as zero-length.
             */
            if (payloadLength != null && payloadLength < 0)
                throw ImageReadException(
                    "Box $boxType declares a size smaller than its header."
                )

            /*
             * A brob box stores its actual content type in its first
             * payload bytes, so they are inspected to decide whether the
             * box carries metadata. The bytes are consumed from the reader
             * in every branch, so the payload lengths below must account
             * for them.
             */
            val brobTypeBytes: ByteArray? =
                if (boxType == BoxType.BROB)
                    byteReader.readBytes(BMFFConstants.TYPE_LENGTH)
                else
                    null

            /* A truncated brob box at the end of the stream. */
            if (brobTypeBytes != null && brobTypeBytes.size < BMFFConstants.TYPE_LENGTH) {

                byteWriter.write(header)
                byteWriter.write(extraHeader)
                byteWriter.write(brobTypeBytes)

                return
            }

            val wrappedType: BoxType? = brobTypeBytes?.let { BoxType.of(it) }

            val isMetadataBox = boxType == BoxType.EXIF ||
                boxType == BoxType.XML ||
                wrappedType == BoxType.EXIF ||
                wrappedType == BoxType.XML

            if (isMetadataBox) {

                /* A metadata box that extends to the end of the stream. */
                if (payloadLength == null)
                    return

                /*
                 * The box header and the brob type field are already
                 * consumed, so only the remaining payload is skipped.
                 */
                val consumedLength = brobTypeBytes?.size ?: 0

                byteReader.transferExactly(
                    byteWriter = null,
                    count = payloadLength - consumedLength
                )

            } else {

                byteWriter.write(header)
                byteWriter.write(extraHeader)

                if (brobTypeBytes != null)
                    byteWriter.write(brobTypeBytes)

                /* The brob type field is part of the payload, but was read separately above. */
                val remainingPayloadLength = payloadLength?.minus(brobTypeBytes?.size ?: 0)

                if (remainingPayloadLength == null) {

                    byteReader.copyRemainingTo(byteWriter)

                    return

                } else {

                    byteReader.transferExactly(byteWriter, remainingPayloadLength)
                }
            }
        }
    }

    @JvmStatic
    public fun writeImage(
        boxes: List<Box>,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        val modifiedBoxes = boxes.toMutableList()

        /*
         * Security check first
         *
         * TODO Remove this once we have brotli support.
         */

        val compressedBoxes = modifiedBoxes.filterIsInstance<CompressedBox>()

        if (compressedBoxes.isNotEmpty()) {

            if (exifBytes != null && compressedBoxes.any { it.actualType == BoxType.EXIF })
                throw ImageWriteException(BROB_WARNING)

            if (xmp != null && compressedBoxes.any { it.actualType == BoxType.XML })
                throw ImageWriteException(BROB_WARNING)
        }

        /*
         * Delete old boxes that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedBoxes.removeAll { it.type == BoxType.EXIF }

        if (xmp != null)
            modifiedBoxes.removeAll { it.type == BoxType.XML }

        /*
         * Write the new file
         *
         * We look first if there is a JXLP header box.
         * If so, this is the right place to insert metadata after.
         * Otherwise we insert right after FTYP.
         */
        val jxlpHeaderBox =
            modifiedBoxes.filterIsInstance<JxlParticalCodestreamBox>().firstOrNull { it.isHeader }

        var metadataAnchorIndex =
            if (jxlpHeaderBox != null)
                modifiedBoxes.indexOf(jxlpHeaderBox)
            else
                modifiedBoxes.indexOfFirst { it.type == BoxType.FTYP }

        val hasMetadata = exifBytes != null || xmp != null

        /*
         * A file without any anchor box must not silently lose the freshly
         * written metadata, so it falls back to inserting behind the first
         * box. Without any box at all there is nothing to attach the
         * metadata to, so the update fails instead of dropping it.
         */
        if (hasMetadata && metadataAnchorIndex < 0) {

            if (modifiedBoxes.isEmpty())
                throw ImageWriteException("No box found to attach the metadata to.")

            metadataAnchorIndex = 0
        }

        for ((index, box) in modifiedBoxes.withIndex()) {

            /*
             * The size field is a 32-bit integer, so larger boxes must be
             * rejected instead of corrupting the written size.
             */
            if (box.size > Int.MAX_VALUE)
                throw ImageWriteException("Box ${box.type} is too large: ${box.size} bytes.")

            byteWriter.writeInt(
                box.size.toInt(),
                BMFFConstants.BMFF_BYTE_ORDER
            )

            byteWriter.write(box.type.bytes)

            box.largeSize?.let {
                byteWriter.writeLong(
                    box.largeSize,
                    BMFFConstants.BMFF_BYTE_ORDER
                )
            }

            byteWriter.write(box.payload)

            val shouldInsertMetadata = hasMetadata && index == metadataAnchorIndex

            if (shouldInsertMetadata) {

                if (exifBytes != null) {

                    val size = BMFFConstants.BOX_HEADER_LENGTH + 4 + exifBytes.size

                    byteWriter.writeInt(size, BMFFConstants.BMFF_BYTE_ORDER)
                    byteWriter.write(BoxType.EXIF.bytes)

                    /*
                     * The TIFF header offset. The new Exif data starts
                     * right after the offset field, so it's always zero.
                     */
                    byteWriter.writeInt(0, BMFFConstants.BMFF_BYTE_ORDER)

                    byteWriter.write(exifBytes)
                }

                if (xmp != null) {

                    val xmpBytes = xmp.encodeToByteArray()

                    val size = BMFFConstants.BOX_HEADER_LENGTH + xmpBytes.size

                    byteWriter.writeInt(size, BMFFConstants.BMFF_BYTE_ORDER)

                    byteWriter.write(BoxType.XML.bytes)

                    byteWriter.write(xmpBytes)
                }
            }
        }
    }
}
