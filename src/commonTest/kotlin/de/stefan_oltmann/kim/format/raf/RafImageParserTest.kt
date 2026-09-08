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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RafImageParserTest {

    /**
     * A hostile JPEG offset that cannot point into the file (zero,
     * negative or beyond the end) must be rejected with a targeted
     * message instead of underflowing into a full-file skip scan.
     */
    @Test
    fun testParseRejectsOutOfRangeJpegOffset() {

        /* RAF magic ("FUJIFILMCCD-RAW ") + 68 header bytes + offset. */
        val header = "FUJIFILMCCD-RAW ".encodeToByteArray() + ByteArray(68 + 4)

        val bytes = header + "jpeg".encodeToByteArray()

        val exception = assertFailsWith<ImageReadException> {
            RafImageParser.parseMetadata(ByteArrayByteReader(bytes))
        }

        assertTrue(
            exception.message?.contains("out of range") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    @Test
    fun testParseMetadataReadsTheEmbeddedJpegExif() {

        val bytes = KimTestData.getBytesOf(KimTestData.RAF_TEST_IMAGE_INDEX)

        val metadata = RafImageParser.parseMetadata(ByteArrayByteReader(bytes))

        assertEquals(MediaFormat.RAF, metadata.mediaFormat)

        assertNotNull(metadata.exif)
    }
}
