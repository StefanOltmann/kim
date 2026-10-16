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
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Kim does not interpret the Multi-Picture Format block: plenty of
 * cameras write it faulty, and interpreting it used to fail whole
 * photos. A faulty block must not fail the read, must stay byte-exact
 * while the file is processed, and must vanish on deleteMetadata.
 */
class MpfBlockTest {

    /**
     * media_98 is media_15 with a corrupted TIFF header inside the MPF
     * block, which ExifTool 12.70 also refuses to decode. The read must
     * not fail the whole photo because of that.
     */
    @Test
    fun testBrokenMpfBlockDoesNotFailTheRead() {

        val metadata = assertNotNull(
            Kim.readMetadata(KimTestData.getBytesOf(KimTestData.JPEG_BROKEN_MPF_INDEX)),
            "The faulty Multi-Picture Format block must not fail the read."
        )

        assertNotNull(
            metadata.findTiffField(TiffTag.TIFF_TAG_MAKE),
            "The EXIF of media_98 must still be readable."
        )
    }

    /**
     * The block is opaque: a metadata update rewrites the EXIF around it
     * without changing a single byte of the faulty APP2 segment.
     */
    @Test
    fun testBrokenMpfSegmentSurvivesUpdateByteExact() {

        val bytes = KimTestData.getBytesOf(KimTestData.JPEG_BROKEN_MPF_INDEX)

        val originalPayload = assertNotNull(findMpfSegmentPayload(bytes))

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
        )

        val updatedPayload = assertNotNull(findMpfSegmentPayload(updatedBytes))

        assertContentEquals(
            expected = originalPayload,
            actual = updatedPayload,
            message = "The Multi-Picture Format segment changed on update."
        )
    }

    /**
     * deleteMetadata removes the Multi-Picture Format index with the
     * other metadata, whether the segment is valid or faulty.
     */
    @Test
    fun testDeleteMetadataRemovesMpfBlock() {

        /* media_15 has an intact block, media_98 a faulty one. */
        for (index in INTACT_AND_BROKEN_MPF_INDEXES) {

            val deletedBytes = Kim.deleteMetadata(KimTestData.getBytesOf(index))

            assertNotNull(
                Kim.readMetadata(deletedBytes),
                "Deletion of media_$index must produce a readable file."
            )

            assertEquals(
                expected = -1,
                actual = indexOfMpfSignature(deletedBytes),
                message = "media_$index still carries the Multi-Picture Format block after deletion."
            )
        }
    }

    /**
     * Returns the length-prefixed payload of the first APP2 segment that
     * starts with the "MPF\0" identifier, or NULL when none exists.
     *
     * The two bytes in front of the payload carry the segment length,
     * including the length field itself.
     */
    private fun findMpfSegmentPayload(bytes: ByteArray): ByteArray? {

        val signatureStart = indexOfMpfSignature(bytes)

        if (signatureStart < 0)
            return null

        val length = ((bytes[signatureStart - 2].toInt() and 0xFF) shl BYTE_SHIFT_8) or
            (bytes[signatureStart - 1].toInt() and 0xFF)

        return bytes.copyOfRange(
            signatureStart,
            signatureStart + length - LENGTH_VALUE_OFFSET
        )
    }

    /** The segment is tracked by its raw signature, not by any parse. */
    private fun indexOfMpfSignature(bytes: ByteArray): Int =

        (0..bytes.size - MPF_SIGNATURE.size).firstOrNull { offset ->
            (0 until MPF_SIGNATURE.size).all { index ->
                bytes[offset + index] == MPF_SIGNATURE[index]
            }
        } ?: -1

    private companion object {

        /* "MPF\0" - the identifier of every Multi-Picture Format segment. */
        val MPF_SIGNATURE = byteArrayOf(0x4D, 0x50, 0x46, 0x00)

        /* media_15 has an intact Multi-Picture Format block. */
        const val INTACT_MPF_INDEX = 15

        val INTACT_AND_BROKEN_MPF_INDEXES = listOf(
            INTACT_MPF_INDEX,
            KimTestData.JPEG_BROKEN_MPF_INDEX
        )

        /* The segment length counts itself, so the payload starts there. */
        const val LENGTH_VALUE_OFFSET = 2

        const val BYTE_SHIFT_8 = 8

        /* 2022-09-26T12:38:48Z: an arbitrary fixed taken date. */
        const val TEST_TAKEN_DATE_MILLIS = 1664195928773L
    }
}
