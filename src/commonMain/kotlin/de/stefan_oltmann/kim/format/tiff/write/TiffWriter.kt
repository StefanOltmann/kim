/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.tiff.write

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.HEX_RADIX
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_VERSION
import de.stefan_oltmann.kim.output.BinaryByteWriter
import de.stefan_oltmann.kim.output.BinaryByteWriter.Companion.createBinaryByteWriter
import de.stefan_oltmann.kim.output.ByteWriter

/**
 * Writes TIFF data from scratch, which changes the file size.
 *
 * Like ExifTool, unreferenced space of the original EXIF block is not
 * preserved, so blocks with large camera padding shrink considerably.
 *
 * The MakerNote value keeps its original offset, because some
 * MakerNotes store absolute offsets inside their data that would break
 * when the MakerNote is moved. Items that do not fit into the space
 * before the MakerNote are written behind it.
 */
public class TiffWriter(
    public val byteOrder: ByteOrder
) {

    /**
     * Writes the given [outputSet] as a new TIFF structure.
     *
     * Unreferenced space of the original EXIF block is dropped. The
     * MakerNote value keeps its original offset and bytes, so MakerNotes
     * with absolute internal offsets stay valid.
     */
    public fun write(
        byteWriter: ByteWriter,
        outputSet: TiffOutputSet
    ) {

        val offsetItems = createOffsetItems(outputSet)

        val outputItems = outputSet.getOutputItems(offsetItems)

        val makerNoteField = outputSet.findMakerNoteField()

        calcNewOffsets(outputItems, makerNoteField)

        offsetItems.writeOffsetsToOutputFields()

        /* Items are written in offset order, so the output stays ordered. */
        val sortedItems = outputItems.sortedBy { it.offset }

        val binaryByteWriter = createBinaryByteWriter(byteWriter, byteOrder)

        writeInternal(
            bos = binaryByteWriter,
            outputItems = sortedItems,
            offsetToFirstIFD = outputSet.getOrCreateRootDirectory().offset
        )
    }

    private fun calcNewOffsets(
        outputItems: List<TiffOutputItem>,
        makerNoteField: TiffOutputField?
    ) {

        val makerNoteItem = makerNoteField?.separateValue
        val makerNoteAnchor = makerNoteField?.originalOffset

        var makerNotePending = makerNoteItem != null

        var offset: Int = TIFF_HEADER_SIZE

        for (outputItem in outputItems) {

            if (outputItem === makerNoteItem) {

                if (makerNotePending && makerNoteAnchor != null) {

                    /*
                     * Keep the MakerNote at its original offset at all
                     * costs. Some MakerNotes store absolute offsets inside
                     * their data - a moved MakerNote means corrupted
                     * vendor data. If the original offset can no longer
                     * be honored, fail the write instead of writing a
                     * corrupt file.
                     */
                    if (makerNoteAnchor > offset) {
                        offset = makerNoteAnchor
                    } else if (offset > makerNoteAnchor) {
                        throw ImageWriteException(
                            "The MakerNote would have to move from 0x" +
                                makerNoteAnchor.toString(HEX_RADIX) + " to 0x" + offset.toString(HEX_RADIX) +
                                "; rewriting would corrupt vendor-specific offsets."
                        )
                    }
                }

                continue
            }

            val itemLength = outputItem.getItemLength()

            val nextOffset = offset + itemLength + imageDataPaddingLength(itemLength)

            /*
             * An item that would overlap the anchored MakerNote region is
             * written behind the MakerNote: pad the gap with zeros and
             * place the MakerNote at its anchor now.
             */
            val overlapsMakerNoteRegion = makerNotePending &&
                makerNoteAnchor != null && nextOffset > makerNoteAnchor

            if (overlapsMakerNoteRegion && makerNoteItem != null) {

                if (offset > makerNoteAnchor)
                    throw ImageWriteException(
                        "The MakerNote would have to move from 0x" +
                            makerNoteAnchor.toString(HEX_RADIX) + " to 0x" + offset.toString(HEX_RADIX) +
                            "; rewriting would corrupt vendor-specific offsets."
                    )

                makerNoteItem.offset = makerNoteAnchor

                val makerNoteLength = makerNoteItem.getItemLength()

                offset = makerNoteAnchor + makerNoteLength +
                    imageDataPaddingLength(makerNoteLength)

                makerNotePending = false
            }

            outputItem.offset = offset

            offset += itemLength + imageDataPaddingLength(itemLength)
        }

        /*
         * Final safety net: the MakerNote must never move. If a future
         * change to this calculation ever assigns it a different position,
         * fail the write instead of emitting a file with corrupted
         * vendor-specific offsets.
         */
        if (makerNoteItem != null && makerNoteAnchor != null &&
            makerNoteItem.offset != makerNoteAnchor)
            throw ImageWriteException(
                "The MakerNote moved from 0x${makerNoteAnchor.toString(HEX_RADIX)} " +
                    "to 0x${makerNoteItem.offset.toString(HEX_RADIX)}."
            )
    }

    private fun writeInternal(
        bos: BinaryByteWriter,
        outputItems: List<TiffOutputItem>,
        offsetToFirstIFD: Int
    ) {

        writeImageFileHeader(bos, offsetToFirstIFD)

        var position = TIFF_HEADER_SIZE

        for (outputItem in outputItems) {

            /* Fill the gap to the next item with zeros. */
            repeat(outputItem.offset - position) {
                bos.write(0)
            }

            outputItem.writeItem(bos)

            val length = outputItem.getItemLength()

            val remainder = imageDataPaddingLength(length)

            repeat(remainder) {
                bos.write(0)
            }

            position = outputItem.offset + length + remainder
        }
    }

    private fun createOffsetItems(
        outputSet: TiffOutputSet
    ): TiffOffsetItems {

        val directories = outputSet.getDirectories()

        if (directories.isEmpty())
            throw ImageWriteException("No directories.")

        /* Directories */
        var exifDirectory: TiffOutputDirectory? = null
        var gpsDirectory: TiffOutputDirectory? = null
        var interoperabilityDirectory: TiffOutputDirectory? = null

        /* Offsets */
        var exifDirectoryOffsetField: TiffOutputField? = null
        var gpsDirectoryOffsetField: TiffOutputField? = null
        var interoperabilityDirectoryOffsetField: TiffOutputField? = null

        val directoryIndices = mutableListOf<Int>()
        val directoryTypeMap = mutableMapOf<Int, TiffOutputDirectory>()

        for (directory in directories) {

            val dirType = directory.type

            directoryTypeMap[dirType] = directory

            if (dirType < 0) {

                when (dirType) {

                    TiffConstants.TIFF_DIRECTORY_EXIF -> {

                        if (exifDirectory != null)
                            throw ImageWriteException("More than one EXIF directory.")

                        exifDirectory = directory
                    }

                    TiffConstants.TIFF_DIRECTORY_GPS -> {

                        if (gpsDirectory != null)
                            throw ImageWriteException("More than one GPS directory.")

                        gpsDirectory = directory
                    }

                    TiffConstants.TIFF_DIRECTORY_INTEROP -> {

                        if (interoperabilityDirectory != null)
                            throw ImageWriteException("More than one Interoperability directory.")

                        interoperabilityDirectory = directory
                    }

                    else -> throw ImageWriteException("Unknown directory: $dirType")
                }

            } else {

                if (directoryIndices.contains(dirType))
                    throw ImageWriteException("More than one directory with index: $dirType")

                directoryIndices.add(dirType)
            }

            val fieldTags = mutableSetOf<Int>()

            for (field in directory.getFields()) {

                if (fieldTags.contains(field.tag))
                    throw ImageWriteException("Tag ${field.tagFormatted} appears twice in directory.")

                fieldTags.add(field.tag)

                when (field.tag) {

                    ExifTag.EXIF_TAG_EXIF_OFFSET.tag -> {

                        if (exifDirectoryOffsetField != null)
                            throw ImageWriteException("More than one Exif directory offset field.")

                        exifDirectoryOffsetField = field
                    }

                    ExifTag.EXIF_TAG_INTEROP_OFFSET.tag -> {

                        if (interoperabilityDirectoryOffsetField != null)
                            throw ImageWriteException("More than one Interoperability dir offset field.")

                        interoperabilityDirectoryOffsetField = field
                    }

                    ExifTag.EXIF_TAG_GPSINFO.tag -> {

                        if (gpsDirectoryOffsetField != null)
                            throw ImageWriteException("More than one GPS directory offset field.")

                        gpsDirectoryOffsetField = field
                    }
                }
            }
        }

        if (directoryIndices.isEmpty())
            throw ImageWriteException("Missing root directory.")

        /*
         * "Normal" TIFF directories should have continous indices starting with 0
         * like 0, 1, 2... and so on.
         */
        directoryIndices.sort()

        var previousDirectory: TiffOutputDirectory? = null

        for (index in directoryIndices) {

            /* set up chain of directory references for "normal" directories. */
            val directory = directoryTypeMap[index]

            previousDirectory?.setNextDirectory(directory)

            previousDirectory = directory
        }

        val rootDirectory = directoryTypeMap[TiffConstants.TIFF_DIRECTORY_TYPE_IFD0]
            ?: throw ImageWriteException("Root directory is missing.")

        if (interoperabilityDirectory == null && interoperabilityDirectoryOffsetField != null)
            throw ImageWriteException(
                "Output set has interoperability dir offset field, but no interoperability dir"
            )

        val tiffOffsetItems = TiffOffsetItems(byteOrder)

        if (interoperabilityDirectory != null) {

            if (exifDirectory == null)
                exifDirectory = outputSet.addExifDirectory()

            /* Create offset if missing */
            if (interoperabilityDirectoryOffsetField == null) {

                interoperabilityDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_INTEROP_OFFSET, byteOrder)

                exifDirectory.add(interoperabilityDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(
                TiffOffsetItem(
                    interoperabilityDirectory,
                    interoperabilityDirectoryOffsetField
                )
            )
        }

        /* Make sure offset fields and offset directories correspond. */
        if (exifDirectory == null && exifDirectoryOffsetField != null)
            throw ImageWriteException("Output set has EXIF directory offset field, but no EXIF directory")

        if (exifDirectory != null) {

            /* Create offset if missing */
            if (exifDirectoryOffsetField == null) {

                exifDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_EXIF_OFFSET, byteOrder)

                rootDirectory.add(exifDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(exifDirectory, exifDirectoryOffsetField))
        }

        if (gpsDirectory == null && gpsDirectoryOffsetField != null)
            throw ImageWriteException("Output set has GPS directory offset field, but no GPS directory")

        if (gpsDirectory != null) {

            /* Create offset if missing */
            if (gpsDirectoryOffsetField == null) {

                gpsDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_GPSINFO, byteOrder)

                rootDirectory.add(gpsDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(gpsDirectory, gpsDirectoryOffsetField))
        }

        return tiffOffsetItems
    }

    private fun writeImageFileHeader(
        binaryByteWriter: BinaryByteWriter,
        offsetToFirstIFD: Int = TIFF_HEADER_SIZE
    ) {

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            binaryByteWriter.write('I'.code)
            binaryByteWriter.write('I'.code)
        } else {
            binaryByteWriter.write('M'.code)
            binaryByteWriter.write('M'.code)
        }

        binaryByteWriter.write2Bytes(TIFF_VERSION)
        binaryByteWriter.write4Bytes(offsetToFirstIFD)
    }

    private companion object {

        /*
         * Attention: the TIFF 6.0 specification (Adobe, 1992) aligns all
         * values on word boundaries, where a word is 2 bytes. Padding to
         * 4-byte boundaries (inherited from Apache Commons Imaging) is not
         * required by the spec and wastes space, so items are padded to
         * even offsets only, like ExifTool does.
         */
        private const val WORD_SIZE = 2

        private fun imageDataPaddingLength(dataLength: Int): Int =
            (WORD_SIZE - dataLength % WORD_SIZE) % WORD_SIZE
    }
}
