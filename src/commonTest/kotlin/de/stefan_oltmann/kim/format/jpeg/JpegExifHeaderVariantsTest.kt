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
import de.stefan_oltmann.kim.common.MetadataSummaryConverter
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests EXIF segments whose identifier deviates from the spec.
 *
 * Some cameras write APP1 segments where the second NUL byte of the
 * "Exif\0\0" identifier was replaced by another byte, or where up to
 * four garbage bytes precede the identifier. ExifTool warns
 * "Incorrect EXIF segment identifier" (respectively "Unknown garbage at
 * start of EXIF segment") for those and still reads them.
 *
 * The test file is a copy of media_1 in which the second NUL byte of the
 * identifier was replaced by a space. The ExifTool binary still reports
 * Make "Canon" and Model "Canon EOS 70D" for it.
 */
class JpegExifHeaderVariantsTest {

    @Test
    fun testReadsExifWithMissingSecondIdentifierNull() {

        val bytes = KimTestData.getBytesOf("exif_header_variant.jpg")

        val metadata = assertNotNull(Kim.readMetadata(bytes))

        val ifd0 = assertNotNull(metadata.exif).directories.first()

        /* The values the ExifTool binary reports for this file. */
        assertEquals("Canon", ifd0.findField(TiffTag.TIFF_TAG_MAKE)?.value.toString())
        assertEquals("Canon EOS 70D", ifd0.findField(TiffTag.TIFF_TAG_MODEL)?.value.toString())
    }

    /**
     * The update must apply the orientation to the variant segment like
     * ExifTool does, keeping the EXIF data intact and exactly one EXIF
     * segment in the file. The lossless swap may keep the variant header,
     * a rewrite replaces it with a clean one - the outcome is the same.
     */
    @Test
    fun testUpdateAppliesOrientationToVariantSegment() {

        val bytes = KimTestData.getBytesOf("exif_header_variant.jpg")

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* Exactly one APP1 EXIF segment may remain: no stale duplicates. */
        val cleanHeader = "Exif\u0000\u0000".encodeToByteArray()

        val variantHeader = "Exif\u0000 ".encodeToByteArray()

        var exifSegmentCount = 0

        for (index in 0..updatedBytes.size - 10) {

            val isApp1Marker =
                updatedBytes[index] == 0xFF.toByte() && updatedBytes[index + 1] == 0xE1.toByte()

            if (!isApp1Marker)
                continue

            val header = updatedBytes.copyOfRange(index + 4, index + 10)

            if (header.contentEquals(cleanHeader) || header.contentEquals(variantHeader))
                exifSegmentCount++
        }

        assertEquals(1, exifSegmentCount)

        val metadata = assertNotNull(Kim.readMetadata(updatedBytes))

        val summary = MetadataSummaryConverter.convertToSummary(metadata)

        assertEquals(TiffOrientation.ROTATE_RIGHT, summary.orientation)

        val ifd0 = assertNotNull(metadata.exif).directories.first()

        assertEquals("Canon", ifd0.findField(TiffTag.TIFF_TAG_MAKE)?.value.toString())
    }

    /**
     * The deletion must remove the variant EXIF segment like the clean
     * one: the tolerant header match is shared between the read and the
     * delete decision, so a variant header can never survive the
     * deletion of the metadata the read still parses.
     */
    @Test
    fun testDeleteMetadataRemovesVariantExifSegment() {

        val deletedBytes = Kim.deleteMetadata(
            bytes = KimTestData.getBytesOf("exif_header_variant.jpg")
        )

        assertNull(
            assertNotNull(Kim.readMetadata(deletedBytes)).exif,
            "The variant EXIF segment must not survive the deletion."
        )
    }

    /**
     * The garbage-prefix tolerance of the identifier: up to four bytes
     * in front of "Exif\0" have been seen in files of real cameras. The
     * read parses such a segment, so the deletion must remove it too -
     * a stricter delete match would leave metadata behind that the read
     * still reports.
     */
    @Test
    fun testReadAndDeleteHandleGarbagePrefixedExifIdentifier() {

        val writer = ByteArrayByteWriter()

        writer.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))

        /* The APP1 segment carries two garbage bytes in front of Exif\0\0. */
        writer.write(
            byteArrayOf(0xFF.toByte(), 0xE1.toByte())
        )

        val exifPayloadBytes = byteArrayOf(
            0x41,
            0x42,
            0x45,
            0x78,
            0x69,
            0x66,
            0x00,
            0x00
        ) + MINIMAL_TIFF_BYTES

        writer.write(
            byteArrayOf(
                0x00,
                (exifPayloadBytes.size + 2).toByte()
            )
        )

        writer.write(exifPayloadBytes)

        writer.write(
            byteArrayOf(
                0xFF.toByte(),
                0xDA.toByte(),
                0x00,
                0x08,
                0x01,
                0x01,
                0x00,
                0x00,
                0x3F,
                0x00
            )
        )

        writer.write(byteArrayOf(0x11, 0x22))

        writer.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte()))

        val bytes = writer.toByteArray()

        /* The read parses the garbage-prefixed EXIF segment. */
        val metadata = assertNotNull(Kim.readMetadata(ByteArrayByteReader(bytes)))

        assertNotNull(metadata.exif, "The garbage-prefixed EXIF segment must be read.")

        /* The deletion removes it: no EXIF stays readable afterwards. */
        val deletedBytes = Kim.deleteMetadata(bytes)

        assertNull(
            assertNotNull(Kim.readMetadata(ByteArrayByteReader(deletedBytes))).exif,
            "The garbage-prefixed EXIF segment must not survive the deletion."
        )
    }

    private companion object {

        /**
         * A minimal valid little-endian TIFF: the byte order marker, the
         * IFD offset 8, an empty IFD0 and no next IFD.
         */
        val MINIMAL_TIFF_BYTES: ByteArray = byteArrayOf(
            0x49,
            0x49,
            0x2A,
            0x00,
            0x08,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        )
    }
}
