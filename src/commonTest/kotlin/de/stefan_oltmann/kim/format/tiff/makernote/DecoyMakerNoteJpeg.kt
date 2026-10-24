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
 * Test data with a decoy MakerNote tag.
 *
 * The IFD0 carries a 0x927C entry next to the real MakerNote in the EXIF
 * directory, so tests can pin that the write anchor follows the real
 * MakerNote, which the reader resolves inside the EXIF directory only.
 */
internal object DecoyMakerNoteJpeg {

    /**
     * Builds a minimal JPEG whose APP1 EXIF segment contains a TIFF with
     * a decoy 0x927C entry in IFD0 and the real MakerNote at offset 0x58
     * of the EXIF directory.
     */
    fun build(): ByteArray {

        val tiffBytes = convertHexStringToByteArray(
            /* TIFF header */
            "49492a00" +
                /* IFD0 offset */
                "08000000" +

                /*
                 * IFD0: Make, ExifIFD pointer and the decoy 0x927C entry.
                 * Entry count.
                 */
                "0300" +
                /* Make, ASCII, 8, at 0x32 */
                "0f010200" + "08000000" + "32000000" +
                /* ExifIFD, LONG, 1, at 0x3A */
                "69870400" + "01000000" + "3a000000" +
                /* Decoy, UNDEFINED, 12, at 0x4C */
                "7c920700" + "0c000000" + "4c000000" +
                /* next IFD */
                "00000000" +

                /* "KIMTEST\0" */
                "4b494d5445535400" +

                /*
                 * ExifIFD: the real MakerNote field.
                 * Entry count.
                 */
                "0100" +
                /* MakerNote, UNDEFINED, 16, at 0x58 */
                "7c920700" + "10000000" + "58000000" +
                /* next IFD */
                "00000000" +

                /* Decoy value: "DECOY-VALUE!" */
                "4445434f592d56414c554521" +
                /* Real MakerNote value: "REALMAKERNOTE123" */
                "5245414c4d414b45524e4f5445313233"
        )

        val exifPayload = convertHexStringToByteArray("457869660000") + tiffBytes

        val app1Length = 2 + exifPayload.size

        val sos = byteArrayOf(
            /* SOS */
            0xff.toByte(),
            0xda.toByte(),
            /* segment length */
            0x00, 0x08,
            /* one component */
            0x01, 0x01,
            /* spectral start and end */
            0x00, 0x00,
            /* approximation */
            0x3f.toByte(), 0x00
        )

        val imageData = convertHexStringToByteArray(
            "112233445566778899aabbccddeeff" +
                "112233445566778899aabbccddeeff01"
        )

        val bytes = ByteArray(4 + app1Length + sos.size + imageData.size + 2)

        var pos = 0
        /* SOI */
        bytes[pos++] = 0xff.toByte()
        bytes[pos++] = 0xd8.toByte()
        /* APP1 */
        bytes[pos++] = 0xff.toByte()
        bytes[pos++] = 0xe1.toByte()
        bytes[pos++] = (app1Length shr 8).toByte()
        bytes[pos++] = app1Length.toByte()
        exifPayload.copyInto(bytes, pos)
        pos += exifPayload.size
        sos.copyInto(bytes, pos)
        pos += sos.size
        imageData.copyInto(bytes, pos)
        pos += imageData.size
        /* EOI */
        bytes[pos++] = 0xff.toByte()
        bytes[pos] = 0xd9.toByte()

        return bytes
    }
}
