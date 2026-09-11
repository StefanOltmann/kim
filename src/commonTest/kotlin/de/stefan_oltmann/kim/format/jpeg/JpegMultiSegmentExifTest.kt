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
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests EXIF that is split across multiple APP1 segments.
 *
 * EXIF larger than the ~64 KB limit of one APP1 segment is split across
 * consecutive APP1 segments by some writers: every part repeats the
 * "Exif\0\0" header, but only the first part starts with a TIFF byte
 * order marker. ExifTool stitches those parts into one EXIF block and
 * warns "File contains multi-segment EXIF".
 *
 * The test file was written with the ExifTool binary: a copy of media_1
 * whose UserComment was extended until the EXIF block exceeded the
 * segment limit, so ExifTool itself performed the split.
 */
class JpegMultiSegmentExifTest {

    /**
     * The exact UserComment written by ExifTool into the test file.
     */
    private val expectedUserComment: String =
        "KimExifRef0123456789 ".repeat(3400)

    @Test
    fun testReadsExifSplitAcrossTwoApp1Segments() {

        val bytes = KimTestData.getBytesOf("multi_segment_exif.jpg")

        val metadata = assertNotNull(Kim.readMetadata(bytes))

        val exif = assertNotNull(metadata.exif)

        val ifd0 = exif.directories.first()

        /*
         * The values that ExifTool reports for this file, including the
         * tags that land in the continuation segment behind the one that
         * carries the IFD entries.
         */
        assertEquals("Canon", ifd0.findField(TiffTag.TIFF_TAG_MAKE)?.value.toString())
        assertEquals("Canon EOS 70D", ifd0.findField(TiffTag.TIFF_TAG_MODEL)?.value.toString())

        val userComment = exif.directories
            .firstNotNullOfOrNull { directory -> directory.findField(ExifTag.EXIF_TAG_USER_COMMENT) }

        assertEquals(
            expected = expectedUserComment,
            actual = userComment?.value.toString()
        )
    }
}
