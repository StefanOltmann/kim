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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteBlobPointer
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Reads the MakerNote of FujiFilm cameras.
 */
internal object FujiFilmMakerNoteHandler : MakerNoteHandler() {

    private const val FUJIFILM_MAKER_NOTE_SIGNATURE = "FUJIFILM"
    private const val FUJIFILM_MAKER_NOTE_VERSION_LENGTH = 4

    /**
     * The binary sub-directories of the FujiFilm MakerNote.
     */
    private val BLOB_POINTERS: List<MakerNoteBlobPointer> = listOf(
        MakerNoteBlobPointer(
            0x102b,
            TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS,
            FujiFilmPrioritySettingsTag.ALL,
            1
        ),
        MakerNoteBlobPointer(
            0x102d,
            TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
            FujiFilmFocusSettingsTag.ALL,
            1
        ),
        MakerNoteBlobPointer(
            0x102e,
            TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS,
            FujiFilmAFCSettingsTag.ALL,
            1
        ),
        MakerNoteBlobPointer(
            0x1103,
            TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS,
            FujiFilmDriveSettingsTag.ALL,
            1
        )
    )

    /**
     * Reads the MakerNote of a FujiFilm camera.
     *
     * FujiFilm MakerNotes contain the version bytes and the IFD
     * directly after the signature.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, FUJIFILM_MAKER_NOTE_SIGNATURE))
            return

        /*
         * Skip version (4 bytes).
         * The IFD starts immediately after the version bytes.
         * Fuji MakerNote IFD uses little-endian byte order.
         */
        byteReader.skipBytes("version", FUJIFILM_MAKER_NOTE_VERSION_LENGTH)

        /* IFD starts at offset 12 from the beginning of MakerNote data */
        val ifdOffset = FUJIFILM_MAKER_NOTE_SIGNATURE.length + FUJIFILM_MAKER_NOTE_VERSION_LENGTH

        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = ByteOrder.LITTLE_ENDIAN,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_FUJIFILM,
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
        }
    }
}

