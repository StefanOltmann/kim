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
package de.stefan_oltmann.kim.format.tiff.makernote.olympus

import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Reads the MakerNote of Olympus cameras.
 */
internal object OlympusMakerNoteHandler : MakerNoteHandler() {

    private const val OLYMPUS_MAKER_NOTE_SIGNATURE = "OLYMPUS\u0000"

    /*
     * OM System bodies kept the Olympus MakerNote layout after the
     * rebrand, but write this longer signature - the IFD offset below is
     * therefore computed from the matched signature's length.
     */
    private const val OM_SYSTEM_MAKER_NOTE_SIGNATURE = "OM SYSTEM\u0000"

    private const val OLYMPUS_MAKER_NOTE_VERSION_LENGTH = 2

    /**
     * The sub-IFD pointers of the Olympus MakerNote with the
     * directory type of the referenced sub-directory.
     */
    private val SUB_IFD_POINTERS: List<Pair<TagInfo, Int>> = listOf(
        OlympusTag.EQUIPMENT_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT,
        OlympusTag.CAMERA_SETTINGS_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS,
        OlympusTag.RAW_DEVELOPMENT_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT,
        OlympusTag.RAW_DEV_2_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEV_2,
        OlympusTag.IMAGE_PROCESSING_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING,
        OlympusTag.FOCUS_INFO_IFD to TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    /**
     * Reads the MakerNote of an Olympus camera.
     *
     * Olympus MakerNotes start with a signature, a byte order marker
     * and a version, followed by the IFD. OM System bodies write the
     * longer "OM SYSTEM\0" signature with the same layout after the
     * rebrand.
     *
     * The MakerNote contains several sub-IFDs (Equipment, CameraSettings,
     * RawDevelopment, ImageProcessing, FocusInfo), whose offsets are
     * relative to the start of the MakerNote. They are read as
     * additional directories.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        val signatureLength =
            when {
                readMakerNoteSignature(
                    byteReader = byteReader,
                    makerNoteValueOffset = makerNoteValueOffset,
                    signature = OLYMPUS_MAKER_NOTE_SIGNATURE
                ) -> OLYMPUS_MAKER_NOTE_SIGNATURE.length

                readMakerNoteSignature(
                    byteReader = byteReader,
                    makerNoteValueOffset = makerNoteValueOffset,
                    signature = OM_SYSTEM_MAKER_NOTE_SIGNATURE
                ) -> OM_SYSTEM_MAKER_NOTE_SIGNATURE.length

                else -> return
            }

        val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

        /* Skip the version bytes. */
        byteReader.skipBytes("Olympus MakerNote version", OLYMPUS_MAKER_NOTE_VERSION_LENGTH)

        val ifdOffset = signatureLength + 2 + OLYMPUS_MAKER_NOTE_VERSION_LENGTH

        val makerNoteDirectory = readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_OLYMPUS,
            addDirectory = addDirectory
        ) ?: return

        readMakerNoteIfdSubDirectories(
            byteReader = byteReader,
            directory = makerNoteDirectory,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = byteOrder,
            subIfdPointers = SUB_IFD_POINTERS,
            addDirectory = addDirectory
        )
    }
}

