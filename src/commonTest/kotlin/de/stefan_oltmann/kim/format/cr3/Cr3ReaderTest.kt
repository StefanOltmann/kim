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
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.BmffTestBoxes.box
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Cr3ReaderTest {

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

    /**
     * XMP that cannot be parsed must fail the read like on WebP and JXL:
     * sidecar writers would otherwise embed a corrupt packet.
     */
    @Test
    fun testCorruptXmpUuidBoxFailsTheRead() {

        val corruptXmpBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_XMP_UUID) + "no xmp here".encodeToByteArray()
        )

        val moovBox = box("moov", byteArrayOf())

        val ftypBox = box("ftyp", "crx ".encodeToByteArray() + "0000".encodeToByteArray())

        val bytes = ftypBox + moovBox + corruptXmpBox

        assertFailsWith<ImageReadException> {
            BaseMediaFileFormatImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }
    }

    /**
     * The metadata sub-boxes must carry absolute file positions, so a
     * caller can map every box back to its bytes inside the CR3 file.
     */
    @Test
    fun testFindMetadataSubBoxesLocatesTheCmtBoxes() {

        val bytes = KimTestData.getBytesOf(KimTestData.CR3_TEST_IMAGE_INDEX)

        val allBoxes = BoxReader.readAllBoxes(ByteArrayByteReader(bytes))

        val subBoxes = Cr3Reader.findMetadataSubBoxes(allBoxes)

        val subBoxTypes = subBoxes.map { it.type }

        assertTrue(
            BoxType.CMT1 in subBoxTypes && BoxType.CMT2 in subBoxTypes,
            "Unexpected sub-boxes: $subBoxTypes"
        )

        for (subBox in subBoxes) {

            assertTrue(
                subBox.offset > 0 && subBox.offset + subBox.actualLength <= bytes.size,
                "Sub-box $subBox is not within the file."
            )
        }

        /* CMT1 holds IFD0 as a plain TIFF structure. */
        val cmt1 = subBoxes.first { it.type == BoxType.CMT1 }

        val tiffMagic = cmt1.payload.copyOf(TIFF_MAGIC_LENGTH)

        assertTrue(
            tiffMagic.contentEquals(byteArrayOf(0x49, 0x49, 0x2A, 0)) ||
                tiffMagic.contentEquals(byteArrayOf(0x4D, 0x4D, 0, 0x2A)),
            "CMT1 does not start with a TIFF magic number."
        )
    }

    private companion object {
        const val TIFF_MAGIC_LENGTH = 4
    }
}
