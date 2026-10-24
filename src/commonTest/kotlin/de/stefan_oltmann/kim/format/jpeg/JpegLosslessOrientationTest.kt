/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests the lossless orientation swap: an orientation-only update
 * patches the orientation value byte directly inside the EXIF segment
 * instead of re-serializing the whole EXIF data.
 *
 * The patch position comes solely from the strictly parsed orientation
 * field, and the swap only touches a byte that no other parsed field
 * claims - the MakerNote value bytes above all.
 */
class JpegLosslessOrientationTest {

    /**
     * The swap keeps the EXIF block byte-identical except the one
     * orientation value byte and leaves the MakerNote untouched. Every
     * corpus file whose orientation entry supports the swap must keep
     * taking the lossless path - the guard must not push files into the
     * rewrite.
     */
    @Test
    fun testCorpusOrientationUpdateSwapsOnlyOneByte() {

        for (index in losslessJpegIndices) {

            val originalBytes = KimTestData.getBytesOf(index)

            val updatedBytes = Kim.update(
                bytes = originalBytes,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
            )

            val originalExif = firstExifPayload(originalBytes)

            val updatedExif = firstExifPayload(updatedBytes)

            assertEquals(
                expected = originalExif.size,
                actual = updatedExif.size,
                message = "media_$index: the lossless swap must not resize the EXIF block."
            )

            assertTrue(
                countDifferentBytes(originalExif, updatedExif) <= 1,
                "media_$index: the lossless swap must not change more than the orientation byte."
            )

            assertEquals(
                expected = TiffOrientation.ROTATE_RIGHT.value.toShort(),
                actual = Kim.readMetadata(updatedBytes)
                    ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION),
                message = "media_$index: the update must apply the orientation."
            )

            /* Files without a MakerNote have nothing to preserve. */
            if (findMakerNoteField(originalBytes) != null)
                assertMakerNoteUnchanged(originalBytes, updatedBytes, "media_$index")
        }
    }

    /**
     * Only the spec's SHORT entry with one value can swap losslessly.
     * A nonconformant LONG-typed Orientation entry must take the
     * rewrite, which rebuilds the entry correctly instead of patching
     * one byte of a 4-byte value.
     */
    @Test
    fun testLongTypedOrientationEntryTakesTheRewrite() {

        /* SOI + APP1 with an EXIF whose Orientation is typed LONG. */
        val bytes = byteArrayOf(
            /* SOI */
            0xFF.toByte(), 0xD8.toByte(),
            /* APP1 */
            0xFF.toByte(), 0xE1.toByte(),
            /* Segment length 34. */
            0x00, 0x22,
            /* "Exif\0\0" */
            0x45, 0x78, 0x69, 0x66, 0x00, 0x00,
            /* TIFF header, little-endian. */
            0x49, 0x49, 0x2A, 0x00,
            /* IFD0 offset. */
            8, 0, 0, 0,
            /* Entry count. */
            1, 0,
            /* Orientation tag. */
            0x12, 0x01,
            /* Type LONG (nonconformant). */
            4, 0,
            /* Count 1. */
            1, 0, 0, 0,
            /* Value 6. */
            6, 0, 0, 0,
            /* No next IFD. */
            0, 0, 0, 0,
            /* SOS */
            0xFF.toByte(), 0xDA.toByte(),
            0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00,
            /* Entropy-coded data. */
            0x12, 0x34,
            /* EOI */
            0xFF.toByte(), 0xD9.toByte()
        )

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertEquals(
            expected = 6.toShort(),
            actual = Kim.readMetadata(updatedBytes)
                ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        )
    }

    /**
     * A segmented EXIF can hold the orientation entry in the first
     * segment while its value bytes lie in the continuation segment. The
     * in-place swap cannot reach behind the first segment: the update
     * rewrites the EXIF data instead, which applies the orientation and
     * keeps the MakerNote byte-exact at its offset.
     *
     * The fixture is media_42 with its EXIF segment split into two APP1
     * segments right behind the orientation entry's count field. Every
     * EXIF byte is identical to media_42's, only the Exif header
     * repeats. ExifTool reads the file with a "multi-segment EXIF" note.
     */
    @Test
    fun testOrientationValueInTheContinuationSegmentTakesTheRewrite() {

        val originalBytes = KimTestData.getBytesOf("exif_orientation_split.jpg")

        val updatedBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertEquals(
            expected = TiffOrientation.ROTATE_RIGHT.value.toShort(),
            actual = Kim.readMetadata(updatedBytes)
                ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION),
            message = "The rewrite must apply the orientation."
        )

        assertMakerNoteUnchanged(originalBytes, updatedBytes, "the split fixture")
    }

    /**
     * A crafted file can make the MakerNote value range start at the
     * IFD0 orientation entry's value byte, so both structures claim that
     * byte. The swap must never patch into the MakerNote: the update
     * rewrites the EXIF data - which anchors the MakerNote at its
     * offset - or fails loudly. A silently changed MakerNote byte is the
     * one outcome that must never happen.
     *
     * The fixture is media_42 with the MakerNote value offset patched to
     * the orientation value byte. ExifTool flags the crafted offset as a
     * suspicious MakerNote offset and still reads the remaining tags.
     */
    @Test
    fun testOrientationByteClaimedByTheMakerNoteIsNeverPatched() {

        val originalBytes = KimTestData.getBytesOf("makernote_overlapping_orientation.jpg")

        val updatedBytes = try {
            Kim.update(
                bytes = originalBytes,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
            )
        } catch (ex: ImageWriteException) {

            /*
             * A precise write failure is allowed when the crafted
             * MakerNote offset cannot be honored by a rewrite. A wrapped
             * index error would mean the swap patched bytes blindly.
             */
            assertFalse(
                hasIndexOutOfBoundsCause(ex),
                "The update failed with a wrapped index error: ${ex.message}"
            )

            return
        }

        assertEquals(
            expected = TiffOrientation.ROTATE_RIGHT.value.toShort(),
            actual = Kim.readMetadata(updatedBytes)
                ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION),
            message = "The rewrite must apply the orientation."
        )

        assertMakerNoteUnchanged(originalBytes, updatedBytes, "the MakerNote fixture")
    }

    /**
     * Asserts that the MakerNote of the updated file keeps its value
     * bytes and its offset.
     */
    private fun assertMakerNoteUnchanged(
        originalBytes: ByteArray,
        updatedBytes: ByteArray,
        fileDescription: String
    ) {

        val originalField = assertNotNull(
            findMakerNoteField(originalBytes),
            "$fileDescription must contain a MakerNote."
        )

        val updatedField = assertNotNull(
            findMakerNoteField(updatedBytes),
            "The update of $fileDescription dropped the MakerNote."
        )

        assertContentEquals(
            expected = originalField.valueBytes,
            actual = updatedField.valueBytes,
            message = "The MakerNote bytes of $fileDescription changed on update."
        )

        assertEquals(
            expected = originalField.valueOffset,
            actual = updatedField.valueOffset,
            message = "The MakerNote offset of $fileDescription changed on update."
        )
    }

    /**
     * Returns the MakerNote field of the given bytes, or null when
     * the file does not contain a MakerNote.
     */
    private fun findMakerNoteField(bytes: ByteArray): TiffField? {

        val contents = assertNotNull(Kim.readMetadata(bytes)?.exif)

        return TiffDirectory.findTiffField(contents.directories, ExifTag.EXIF_TAG_MAKER_NOTE)
    }

    /**
     * Returns the payload of the first APP1 segment that carries the
     * EXIF identifier.
     */
    private fun firstExifPayload(jpegBytes: ByteArray): ByteArray {

        var offset = JpegConstants.SOI.size

        while (offset + SEGMENT_HEADER_BYTES <= jpegBytes.size) {

            val marker = (jpegBytes[offset].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 1].toInt() and 0xFF)

            val segmentLength = (jpegBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 3].toInt() and 0xFF)

            if (marker == JpegConstants.JPEG_APP1_MARKER) {

                val payload = jpegBytes.copyOfRange(
                    offset + SEGMENT_HEADER_BYTES,
                    offset + SEGMENT_MARKER_BYTES + segmentLength
                )

                if (payload.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE))
                    return payload
            }

            offset += SEGMENT_MARKER_BYTES + segmentLength
        }

        fail("The file must contain an EXIF segment.")
    }

    /**
     * Counts the positions at which the two equally sized byte arrays
     * differ.
     */
    private fun countDifferentBytes(first: ByteArray, second: ByteArray): Int =
        first.indices.count { index -> first[index] != second[index] }

    /**
     * Whether the cause chain of the given exception contains an index
     * error, which signals a blind out-of-bounds patch rather than a
     * precise write failure.
     */
    private fun hasIndexOutOfBoundsCause(exception: Throwable): Boolean {

        var cause: Throwable? = exception.cause

        while (cause != null) {
            if (cause is IndexOutOfBoundsException)
                return true
            cause = cause.cause
        }

        return false
    }

    private companion object {

        const val SEGMENT_MARKER_BYTES = 2

        const val SEGMENT_LENGTH_FIELD_BYTES = 2

        const val SEGMENT_HEADER_BYTES = SEGMENT_MARKER_BYTES + SEGMENT_LENGTH_FIELD_BYTES

        /*
         * The corpus JPEGs whose IFD0 holds the spec's SHORT orientation
         * entry with one value - the files the lossless swap supports.
         */
        val losslessJpegIndices: Set<Int> = setOf(
            1, 2, 15, 20, 21, 23, 25, 26, 28, 29, 30, 31,
            34, 36, 37, 38, 39, 40, 41, 42, 43, 46, 48, 49, 50
        )
    }
}
