/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_INFO3
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_SONY_FACE_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_SETTINGS
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteBlobPointer
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Reads the MakerNote of Sony cameras.
 *
 * Like ExifTool, a block of the MakerNote that cannot be read is
 * skipped instead of rejecting the file: the MakerNote is kept as an
 * opaque binary block and survives the rewrite unchanged, because the
 * writer keeps the whole MakerNote value at its original offset.
 */
@Suppress("MagicNumber")
internal object SonyMakerNoteHandler : MakerNoteHandler() {

    private const val SONY_MAKER_NOTE_SIGNATURE = "SONY DSC \u0000\u0000\u0000"
    private const val SONY_ERICSSON_MAKER_NOTE_SIGNATURE = "SEMC MS\u0000\u0000\u0000\u0000\u0000"

    /**
     * The binary sub-directories of the Sony5 MakerNote.
     */
    private val BLOB_POINTERS: List<MakerNoteBlobPointer> = listOf(
        MakerNoteBlobPointer(0x0010, TIFF_MAKER_NOTE_SONY_CAMERA_INFO3, SonyCameraInfo3Tag.ALL, 1)
    )

    /**
     * Reads the MakerNote of a Sony camera.
     *
     * Sony writes three different MakerNote layouts:
     *
     * - "SEMC MS" signature followed by a TIFF header (SonyEricsson)
     * - "SONY DSC" signature followed by the IFD entries (Sony)
     * - no header at all (Sony5).
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (readMakerNoteSignature(byteReader, makerNoteValueOffset, SONY_ERICSSON_MAKER_NOTE_SIGNATURE)) {

            val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

            /* Skip the TIFF magic and read the offset to the first IFD. */
            byteReader.skipBytes("SonyEricsson MakerNote TIFF magic", 2)

            val ifdOffset = byteReader.read4BytesAsInt("SonyEricsson MakerNote IFD offset", byteOrder)

            val tiffHeaderOffset = makerNoteValueOffset + SONY_ERICSSON_MAKER_NOTE_SIGNATURE.length

            readMakerNoteDirectory(
                byteReader = byteReader,
                directoryOffset = tiffHeaderOffset + ifdOffset,
                valueOffsetBase = tiffHeaderOffset,
                byteOrder = byteOrder,
                directoryType = TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON,
                addDirectory = addDirectory
            )

            return
        }

        if (readMakerNoteSignature(byteReader, makerNoteValueOffset, SONY_MAKER_NOTE_SIGNATURE)) {

            val ifdOffset = SONY_MAKER_NOTE_SIGNATURE.length

            readMakerNoteDirectory(
                byteReader = byteReader,
                directoryOffset = makerNoteValueOffset + ifdOffset,
                valueOffsetBase = 0,
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                directoryType = TiffConstants.TIFF_MAKER_NOTE_SONY,
                addDirectory = addDirectory
            )

            return
        }

        /*
         * Some Sony cameras write the IFD without any header (Sony5 format).
         */
        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset,
            valueOffsetBase = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_SONY5,
            addDirectory = {
                makerNoteDirectory = it
                addDirectory(it)
            }
        )

        makerNoteDirectory?.let { directory ->

            readMakerNoteBlobSubDirectories(
                directory = directory,
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                blobPointers = BLOB_POINTERS,
                addDirectory = addDirectory
            )

            readMoreInfo(
                directory = directory,
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                addDirectory = addDirectory
            )
        }
    }

    /**
     * Reads the MoreInfo data of the Sony5 MakerNote.
     *
     * The data starts with an index of tags and block offsets, followed
     * by the referenced blocks with the camera settings.
     */
    private fun readMoreInfo(
        directory: TiffDirectory,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        val field = directory.entries.find { it.tag == 0x0020 } ?: return

        val blobOffset = getAbsoluteValueOffset(field)

        val blob = field.valueBytes

        if (blob.size < 4)
            return

        val numEntries = blob.toInt16(0, byteOrder)

        if (numEntries <= 0 || numEntries > 50)
            return

        val entries = mutableListOf<Pair<Int, Int>>()

        var pos = 4

        for (index in 0 until numEntries) {

            if (pos + 4 > blob.size)
                return

            entries.add(
                blob.toInt16(pos, byteOrder) to blob.toInt16(pos + 2, byteOrder)
            )

            pos += 4
        }

        val sortedOffsets = entries.map { it.second }.sorted()

        for ((tag, offset) in entries) {

            val tagTable = when (tag) {
                0x0001 -> SonyMoreSettingsTag.ALL
                0x0002 -> SonyFaceInfoATag.ALL
                else -> continue
            }

            val directoryType = if (tag == 0x0001)
                TIFF_MAKER_NOTE_SONY_MORE_SETTINGS
            else
                TIFF_MAKER_NOTE_SONY_FACE_INFO

            val nextOffset = sortedOffsets.firstOrNull { it > offset } ?: blob.size

            if (offset < 0 || offset >= blob.size)
                continue

            val block = blob.copyOfRange(offset, minOf(nextOffset, blob.size))

            try {

                readMakerNoteBlobSubDirectory(
                    blobBytes = block,
                    blobOffset = blobOffset + offset,
                    blobLength = block.size,
                    byteOrder = byteOrder,
                    directoryType = directoryType,
                    tagTable = tagTable,
                    firstTag = 0,
                    offsetBase = 0,
                    byteOffsetMultiplier = 1,
                    addDirectory = addDirectory
                )

            } catch (_: Exception) {
                /*
                 * Skip the unreadable block.
                 *
                 * Like ExifTool, the MakerNote is kept as an opaque
                 * binary block in this case, and the rewrite preserves
                 * it byte for byte.
                 */
            }
        }
    }
}



