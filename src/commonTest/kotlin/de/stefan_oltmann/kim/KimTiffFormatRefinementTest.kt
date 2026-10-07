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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * NEF, ARW and DNG files carry the plain TIFF magic, so the header
 * detection labels them TIFF. The reported format of the metadata is
 * refined from the parsed structure - the DNGVersion tag marks a DNG,
 * the vendor maker notes mark NEF and ARW - like ExifTool reports the
 * specific format for the same bytes.
 */
class KimTiffFormatRefinementTest {

    @Test
    fun testNefIsRefinedFromTheTiffDetection() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.NEF_TEST_IMAGE_INDEX)
        )

        assertEquals(MediaFormat.NEF, metadata?.mediaFormat)
    }

    @Test
    fun testArwIsRefinedFromTheTiffDetection() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.ARW_TEST_IMAGE_INDEX)
        )

        assertEquals(MediaFormat.ARW, metadata?.mediaFormat)
    }

    @Test
    fun testDngIsRefinedFromTheTiffDetection() {

        for (index in intArrayOf(
            KimTestData.DNG_CR2_TEST_IMAGE_INDEX,
            KimTestData.DNG_RAF_TEST_IMAGE_INDEX,
            KimTestData.DNG_NEF_TEST_IMAGE_INDEX,
            KimTestData.DNG_ARW_TEST_IMAGE_INDEX,
            KimTestData.DNG_RW2_TEST_IMAGE_INDEX,
            KimTestData.DNG_ORF_TEST_IMAGE_INDEX
        )) {

            val metadata = Kim.readMetadata(KimTestData.getBytesOf(index))

            assertEquals(
                expected = MediaFormat.DNG,
                actual = metadata?.mediaFormat,
                "media_$index must be refined to DNG."
            )
        }
    }

    @Test
    fun testPlainTiffStaysTiff() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.TIFF_NONE_TEST_IMAGE_INDEX)
        )

        assertEquals(MediaFormat.TIFF, metadata?.mediaFormat)
    }

    @Test
    fun testMagicDetectedFormatsAreNotRefined() {

        /* CR2 has its own magic; the refinement must not touch it. */
        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.CR2_TEST_IMAGE_INDEX)
        )

        assertEquals(MediaFormat.CR2, metadata?.mediaFormat)
    }
}
