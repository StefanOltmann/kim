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
package de.stefan_oltmann.kim.format.tiff.makernote.pentax

import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Reads the MakerNote of Pentax and Samsung cameras.
 */
internal object PentaxMakerNoteHandler : MakerNoteHandler() {

    private const val PENTAX_MAKER_NOTE_SIGNATURE = "PENTAX \u0000"
    private const val SAMSUNG_MAKER_NOTE_SIGNATURE = "SAMSUNG\u0000"

    /**
     * Reads the MakerNote of a Pentax or Samsung camera.
     *
     * Pentax and Samsung MakerNotes start with a signature, a byte
     * order marker and the IFD entries.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, PENTAX_MAKER_NOTE_SIGNATURE) &&
            !readMakerNoteSignature(byteReader, makerNoteValueOffset, SAMSUNG_MAKER_NOTE_SIGNATURE)
        )
            return

        val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

        val ifdOffset = PENTAX_MAKER_NOTE_SIGNATURE.length + 4

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_PENTAX,
            addDirectory = addDirectory
        )
    }
}

