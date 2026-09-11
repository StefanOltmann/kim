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
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
