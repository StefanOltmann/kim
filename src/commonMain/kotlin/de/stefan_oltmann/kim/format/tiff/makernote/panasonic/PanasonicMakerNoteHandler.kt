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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_PANASONIC_TIME_INFO
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteBlobPointer
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Reads the MakerNote of Panasonic cameras.
 */
internal object PanasonicMakerNoteHandler : MakerNoteHandler() {

    private const val PANASONIC_MAKER_NOTE_SIGNATURE = "Panasonic\u0000\u0000\u0000"

    /**
     * The binary sub-directories of the Panasonic MakerNote.
     */
    private val BLOB_POINTERS: List<MakerNoteBlobPointer> = listOf(
        MakerNoteBlobPointer(0x004e, TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO, PanasonicFaceDetInfoTag.ALL, 1),
        MakerNoteBlobPointer(0x0061, TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO, PanasonicFaceRecInfoTag.ALL, 1),
        MakerNoteBlobPointer(0x2003, TIFF_MAKER_NOTE_PANASONIC_TIME_INFO, PanasonicTimeInfoTag.ALL, 1)
    )

    /**
     * Reads the MakerNote of a Panasonic camera.
     *
     * Panasonic MakerNotes start with a signature, the entry count
     * and then the IFD entries directly.
     *
     * The value offsets are relative to the start of the Exif data
     * the MakerNote is embedded in, so the base is zero.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, PANASONIC_MAKER_NOTE_SIGNATURE))
            return

        val ifdOffset = PANASONIC_MAKER_NOTE_SIGNATURE.length

        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_PANASONIC,
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

