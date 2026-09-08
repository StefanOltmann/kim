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
package de.stefan_oltmann.kim.format.tiff.makernote

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MakerNoteDispatchTest {

    /**
     * The Leica and Sigma handler implementations existed without being
     * reachable: the dispatch table never routed their brands to them, so
     * the MakerNote of such cameras stayed uninterpreted.
     */
    @Test
    fun testLeicaMakerNoteIsDispatched() {

        val bytes = buildTiffWithBrandMakerNote(
            make = "Leica\u0000",
            signature = "LEICA\u0000",
            withByteOrder = true
        )

        val makerNoteDirectory = TiffReader.read(bytes).makerNoteDirectory

        assertNotNull(makerNoteDirectory)

        assertEquals(TiffConstants.TIFF_MAKER_NOTE_LEICA, makerNoteDirectory.type)
    }

    @Test
    fun testSigmaMakerNoteIsDispatched() {

        val bytes = buildTiffWithBrandMakerNote(
            make = "SIGMA\u0000",
            signature = "SIGMA\u0000",
            withByteOrder = false
        )

        val makerNoteDirectory = TiffReader.read(bytes).makerNoteDirectory

        assertNotNull(makerNoteDirectory)

        assertEquals(TiffConstants.TIFF_MAKER_NOTE_SIGMA, makerNoteDirectory.type)
    }

    /**
     * Builds a minimal TIFF whose IFD0 carries the given make and whose
     * Exif IFD carries a MakerNote with the given vendor signature and a
     * single trivial IFD entry.
     */
    private fun buildTiffWithBrandMakerNote(
        make: String,
        signature: String,
        withByteOrder: Boolean
    ): ByteArray {

        val out = ByteArrayByteWriter()

        /*
         * MakerNote: signature (+ optional byte order marker) and one IFD
         * with a single inline LONG entry.
         */
        val makerNote = signature.encodeToByteArray() +
            (if (withByteOrder) byteArrayOf(0x49, 0x49) else byteArrayOf()) +
            byteArrayOf(1, 0) + // Entry count.
            byteArrayOf(1, 0, 4, 0, 1, 0, 0, 0, 42, 0, 0, 0) + // Tag 1, LONG, value 42.
            byteArrayOf(0, 0, 0, 0) // No next IFD.

        val ifd0Offset = 8
        val ifd0Size = 2 + 2 * ENTRY_LENGTH + 4
        val makeDataOffset = ifd0Offset + ifd0Size
        val exifIfdOffset = makeDataOffset + make.length
        val exifIfdSize = 2 + ENTRY_LENGTH + 4
        val makerNoteOffset = exifIfdOffset + exifIfdSize

        out.write(byteArrayOf(0x49, 0x49, 0x2A, 0x00)) // TIFF header.
        out.writeInt(ifd0Offset, ByteOrder.LITTLE_ENDIAN)

        /* IFD0: Make + Exif IFD pointer. */
        out.write2BytesAsInt(2, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(TiffTag.TIFF_TAG_MAKE.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_ASCII, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(make.length, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makeDataOffset, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(ExifTag.EXIF_TAG_EXIF_OFFSET.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_LONG, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(1, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(exifIfdOffset, ByteOrder.LITTLE_ENDIAN)

        out.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next IFD.

        out.write(make.encodeToByteArray())

        /* Exif IFD: MakerNote pointing at the vendor blob. */
        out.write2BytesAsInt(1, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(ExifTag.EXIF_TAG_MAKER_NOTE.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_UNDEFINED, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makerNote.size, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makerNoteOffset, ByteOrder.LITTLE_ENDIAN)

        out.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next IFD.

        out.write(makerNote)

        return out.toByteArray()
    }

    private companion object {

        private const val ENTRY_LENGTH: Int = 12

        private const val TYPE_ASCII: Int = 2

        private const val TYPE_LONG: Int = 4

        private const val TYPE_UNDEFINED: Int = 7
    }
}
