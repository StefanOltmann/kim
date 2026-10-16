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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The Print Image Matching block is offered by many cameras, but plenty
 * of camera models write it faulty. Kim does not interpret the block:
 * a faulty block must not fail the read, must stay byte-exact while the
 * file is processed, and must vanish with the whole EXIF on
 * deleteMetadata.
 */
class PrintImBlockTest {

    /**
     * media_97 carries a block that declares 276 entries without
     * carrying their bytes - ExifTool reports "Bad PrintIM size" for
     * it. The read must not fail the whole photo because of that.
     */
    @Test
    fun testBrokenPrintImBlockDoesNotFailTheRead() {

        val metadata = assertNotNull(
            Kim.readMetadata(KimTestData.getBytesOf(KimTestData.JPEG_BROKEN_PRINTIM_INDEX)),
            "The faulty PrintIM block must not fail the read."
        )

        val exif = assertNotNull(metadata.exif, "media_97 must carry EXIF.")

        val printImField = assertNotNull(
            TiffDirectory.findTiffField(exif.directories, ExifTag.EXIF_TAG_PRINT_IM),
            "The faulty block must still be preserved."
        )

        /* The faulty block is not interpreted, so it is kept as-is. */
        assertEquals(260, printImField.valueBytes.size)

        assertContentEquals(
            expected = PRINT_IM_SIGNATURE,
            actual = printImField.valueBytes.copyOf(8),
            message = "The PrintIM signature must survive the read."
        )

        /* The faulty entry count (276) is kept exactly as the camera stored it. */
        assertEquals(0x14.toByte(), printImField.valueBytes[14])
        assertEquals(0x01.toByte(), printImField.valueBytes[15])
    }

    /**
     * The block is opaque: a metadata update rewrites the EXIF around it
     * without changing a single byte of the faulty block.
     */
    @Test
    fun testBrokenPrintImBlockSurvivesUpdateByteExact() {

        val bytes = KimTestData.getBytesOf(KimTestData.JPEG_BROKEN_PRINTIM_INDEX)

        val originalField = assertNotNull(
            findPrintImField(bytes),
            "media_97 must contain the PrintIM block."
        )

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
        )

        val updatedField = assertNotNull(
            findPrintImField(updatedBytes),
            "The update dropped the PrintIM block."
        )

        assertContentEquals(
            expected = originalField.valueBytes,
            actual = updatedField.valueBytes,
            message = "The PrintIM block bytes changed on update."
        )
    }

    /**
     * deleteMetadata discards the whole EXIF that carries the PrintIM
     * block, so the block must vanish from the output whether it was
     * valid or faulty.
     */
    @Test
    fun testDeleteMetadataRemovesPrintImBlock() {

        /* media_20 has an intact block, media_97 a faulty one. */
        for (index in INTACT_AND_BROKEN_PRINTIM_INDEXES) {

            val deletedBytes = Kim.deleteMetadata(KimTestData.getBytesOf(index))

            assertNotNull(
                Kim.readMetadata(deletedBytes),
                "Deletion of media_$index must produce a readable file."
            )

            assertEquals(
                expected = -1,
                actual = indexOfPrintImSignature(deletedBytes),
                message = "media_$index still carries the PrintIM block after deletion."
            )
        }
    }

    /**
     * Returns the PrintIM field of the given bytes, or NULL when the
     * file does not contain the 0xC4A5 field.
     */
    private fun findPrintImField(bytes: ByteArray): TiffField? {

        val contents = assertNotNull(Kim.readMetadata(bytes)?.exif)

        return TiffDirectory.findTiffField(contents.directories, ExifTag.EXIF_TAG_PRINT_IM)
    }

    /** The block is tracked by its raw signature, not by any parse. */
    private fun indexOfPrintImSignature(bytes: ByteArray): Int =

        (0..bytes.size - PRINT_IM_SIGNATURE.size).firstOrNull { offset ->
            (0 until PRINT_IM_SIGNATURE.size).all { index ->
                bytes[offset + index] == PRINT_IM_SIGNATURE[index]
            }
        } ?: -1

    private companion object {

        /* "PrintIM\0" - the signature of every PrintIM block. */
        val PRINT_IM_SIGNATURE = byteArrayOf(
            0x50, 0x72, 0x69, 0x6E, 0x74, 0x49, 0x4D, 0x00
        )

        /* media_20 has an intact block, media_97 a faulty one. */
        val INTACT_AND_BROKEN_PRINTIM_INDEXES = listOf(
            20,
            KimTestData.JPEG_BROKEN_PRINTIM_INDEX
        )

        /* 2022-09-26T12:38:48Z: an arbitrary fixed taken date. */
        const val TEST_TAKEN_DATE_MILLIS = 1664195928773L
    }
}
