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
package de.stefan_oltmann.kim.format.tiff.makernote.leica

import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Reads the MakerNote of Leica cameras.
 */
internal object LeicaMakerNoteHandler : MakerNoteHandler() {

    private const val LEICA_MAKER_NOTE_SIGNATURE = "LEICA\u0000"

    /**
     * The signature of the Leica MakerNote form whose IFD follows the
     * 8-byte signature directly, like the Leica C writes it.
     */
    private const val LEICA_8_BYTE_SIGNATURE = "LEICA\u0000\u0000\u0000"

    /**
     * Reads the MakerNote of a Leica camera.
     *
     * The "LEICA\0\0\0" form (like the Leica C) carries its IFD
     * directly behind the 8-byte signature with no byte order marker:
     * the order is the one whose entry count is sane. Its value offsets
     * address the embedded TIFF header. The "LEICA\0" form carries an
     * explicit byte order marker behind the signature.
     *
     * Attention: The Panasonic-derived Leica form (like the Leica T
     * writes it) is not interpreted - no sample was validated against
     * the reference decoder, and guessing layouts is how the vendor
     * handlers diverged from the reference in the first place.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (readMakerNoteSignature(byteReader, makerNoteValueOffset, LEICA_8_BYTE_SIGNATURE)) {

            val byteOrder = detectMakerNoteByteOrder(
                byteReader = byteReader,
                entryCountOffset = makerNoteValueOffset + LEICA_8_BYTE_SIGNATURE.length
            ) ?: return

            readMakerNoteDirectory(
                byteReader = byteReader,
                directoryOffset = makerNoteValueOffset + LEICA_8_BYTE_SIGNATURE.length,
                /* Leica value offsets address the embedded TIFF header. */
                valueOffsetBase = 0,
                byteOrder = byteOrder,
                directoryType = TiffConstants.TIFF_MAKER_NOTE_LEICA,
                addDirectory = addDirectory
            )

            return
        }

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, LEICA_MAKER_NOTE_SIGNATURE))
            return

        val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

        val ifdOffset = LEICA_MAKER_NOTE_SIGNATURE.length + 2

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_LEICA,
            addDirectory = addDirectory
        )
    }
}
