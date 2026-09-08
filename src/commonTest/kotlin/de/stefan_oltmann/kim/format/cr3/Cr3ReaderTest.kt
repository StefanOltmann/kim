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
package de.stefan_oltmann.kim.format.cr3

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BaseMediaFileFormatImageParser
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertFailsWith

class Cr3ReaderTest {

    private fun box(type: String, payload: ByteArray): ByteArray {

        val size = payload.size + 8

        return byteArrayOf(
            (size shr 24).toByte(),
            (size shr 16).toByte(),
            (size shr 8).toByte(),
            size.toByte()
        ) + type.encodeToByteArray() + payload
    }

    private fun uuidBytes(hexString: String): ByteArray =
        ByteArray(hexString.length / 2) { index ->
            hexString.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }

    /**
     * A present but corrupt CMT box cannot be read cleanly. Per the strict
     * read policy the read must fail instead of returning the remaining
     * metadata without any signal: sidecar writers consume this result and
     * would silently lose the vendor data.
     */
    @Test
    fun testCorruptCmtBoxFailsTheRead() {

        /* Not a TIFF structure, so the CMT parse cannot succeed. */
        val corruptCmt1 = box("CMT1", byteArrayOf(1, 2, 3))

        val exifUuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_EXIF_UUID) + corruptCmt1
        )

        val moovBox = box("moov", exifUuidBox)

        val ftypBox = box("ftyp", "crx ".encodeToByteArray() + "0000".encodeToByteArray())

        val bytes = ftypBox + moovBox

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * A metadata box inside the moov that cannot even be scanned (its
     * header claims fewer bytes than the box header itself) must fail the
     * read. Returning the remaining metadata without any signal would let
     * sidecar writers drop the vendor data silently.
     */
    @Test
    fun testCorruptMetadataSubBoxFailsTheRead() {

        /* A box header claiming a size smaller than itself. */
        val corruptChild = byteArrayOf(
            0, 0, 0, 6,
            0x66, 0x72, 0x65, 0x65, // "free"
            1, 2
        )

        val exifUuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_EXIF_UUID) + corruptChild
        )

        val moovBox = box("moov", exifUuidBox)

        val ftypBox = box("ftyp", "crx ".encodeToByteArray() + "0000".encodeToByteArray())

        val bytes = ftypBox + moovBox

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }
}
