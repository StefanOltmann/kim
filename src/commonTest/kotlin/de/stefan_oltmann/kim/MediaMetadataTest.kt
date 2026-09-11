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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MetadataSummaryConverter
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MediaMetadataTest {

    /**
     * Regression test based on a fixed small set of test files.
     *
     * Compares the metadata output of every test file against its committed
     * golden dump, so parser regressions across all formats are caught.
     * Mismatching outputs are written to "build/regenerated_txt" to ease
     * updating the goldens after an intentional change.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testToString() {

        val mismatchedIndexes = mutableListOf<Int>()

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val bytes = KimTestData.getBytesOf(index)

            /* Broken files are rejected by the segment length validation. */
            if (KimTestData.brokenJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(bytes)
                }

                continue
            }

            val metadata = Kim.readMetadata(bytes)

            val actualToString = metadata.toString().encodeToByteArray()

            val expectedToString = KimTestData.getToStringText(index)

            if (!expectedToString.contentEquals(actualToString)) {

                mismatchedIndexes.add(index)

                SystemFileSystem.createDirectories(Path("build/regenerated_txt"))

                Path("build/regenerated_txt/media_$index.txt")
                    .writeBytes(actualToString)
            }
        }

        assertTrue(
            mismatchedIndexes.isEmpty(),
            "Metadata output does not match the golden files for media " +
                mismatchedIndexes.joinToString(prefix = "[", postfix = "]") +
                ". The regenerated dumps were written to build/regenerated_txt."
        )
    }

    /**
     * When EXIF and XMP carry different taken dates, the EXIF
     * DateTimeOriginal is the authority (the real capture date,
     * confirmed by the GPSDateStamp) - like ExifTool. A later XMP date
     * from a re-export must not overwrite it.
     */
    @Test
    fun testTakenDatePrefersExifOverXmp() {

        Kim.defaultTimeZone = TimeZone.of("GMT")

        val metadata = requireNotNull(Kim.readMetadata(KimTestData.getBytesOf(73)))

        val summary = MetadataSummaryConverter.convertToSummary(metadata)

        /* 2022-09-26T12:38:48Z (14:38:48+02:00) - das EXIF-Datum, das
         * auch ExifTool als Instant meldet. */
        assertEquals(1664195928773L, summary.takenDate)
    }
}
