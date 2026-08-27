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

import de.stefan_oltmann.kim.common.convertHexStringToByteArray

/**
 * Test data with a broken MakerNote.
 *
 * The MakerNote contains a FocusInfoIFD pointer (0x2050) to corrupted
 * data with a 0xFFFF entry count, which cannot be parsed as an IFD.
 */
internal object BrokenMakerNoteTiff {

    /**
     * Builds a minimal little-endian TIFF with an Olympus MakerNote
     * whose FocusInfoIFD pointer points to corrupted data.
     *
     * The MakerNote field points to the maker note data at the given
     * value offset.
     */
    fun buildTiff(makerNoteValueOffsetHex: String): ByteArray =
        convertHexStringToByteArray(
            "49492a00" + // TIFF header
                "08000000" + // IFD0 offset

                /* IFD0: Make and ExifIFD pointer. */
                "0200" + // entry count
                "0f010200" + "08000000" + "26000000" + // Make, ASCII, 8, at 38
                "69870400" + "01000000" + "2e000000" + // ExifIFD, LONG, 1, at 46
                "00000000" + // next IFD

                "4f4c594d50555300" + // "OLYMPUS\0"

                /* ExifIFD: MakerNote field. */
                "0100" + // entry count
                "7c920700" + "64000000" + makerNoteValueOffsetHex + // MakerNote, UNDEFINED, 100
                "00000000" + // next IFD

                /* MakerNote: signature, byte order, version and one sub-IFD pointer. */
                "4f4c594d50555300" + // "OLYMPUS\0"
                "4949" + // little-endian
                "3031" + // version "01"
                "0100" + // entry count
                "50200400" + "01000000" + "1e000000" + // FocusInfoIFD, LONG, 1, offset 30
                "00000000" + // next IFD

                /* Corrupted sub-directory: 0xFFFF entry count. */
                "ffff0000" +
                "00000000" + "00000000" + "00000000" + "00000000" + "00000000" +
                "00000000" + "00000000" + "00000000" + "00000000" + "00000000" +
                "00000000" + "00000000" + "00000000" + "00000000" + "00000000" +
                "00000000" + "00000000" + "00000000" + "00000000" + "00000000" +
                "00000000" + "00000000" + "00000000" + "00000000" + "00000000"
        )

    /**
     * Builds a minimal JPEG whose APP1 EXIF segment contains the
     * TIFF with the broken MakerNote.
     */
    fun buildJpeg(): ByteArray {

        val tiffBytes = buildTiff(makerNoteValueOffsetHex = "40000000")

        val exifPayload = convertHexStringToByteArray("457869660000") + tiffBytes

        val app1Length = 2 + exifPayload.size

        val sos = byteArrayOf(
            0xff.toByte(), // SOS
            0xda.toByte(),
            0x00, 0x08, // segment length
            0x01, 0x01, // one component
            0x00, 0x00, // spectral start and end
            0x3f.toByte(), 0x00 // approximation
        )

        val imageData = convertHexStringToByteArray(
            "112233445566778899aabbccddeeff" +
                "112233445566778899aabbccddeeff01"
        )

        val bytes = ByteArray(4 + app1Length + sos.size + imageData.size + 2)

        var pos = 0
        bytes[pos++] = 0xff.toByte() // SOI
        bytes[pos++] = 0xd8.toByte()
        bytes[pos++] = 0xff.toByte() // APP1
        bytes[pos++] = 0xe1.toByte()
        bytes[pos++] = (app1Length shr 8).toByte()
        bytes[pos++] = app1Length.toByte()
        exifPayload.copyInto(bytes, pos)
        pos += exifPayload.size
        sos.copyInto(bytes, pos)
        pos += sos.size
        imageData.copyInto(bytes, pos)
        pos += imageData.size
        bytes[pos++] = 0xff.toByte() // EOI
        bytes[pos] = 0xd9.toByte()

        return bytes
    }
}
