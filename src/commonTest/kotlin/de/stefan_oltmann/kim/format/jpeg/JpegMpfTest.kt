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
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The Multi-Picture Format index travels in the "MPF\0" APP2 segment.
 * The parsed index must expose the fields ExifTool decodes from the
 * same bytes.
 */
class JpegMpfTest {

    /**
     * media_15 bundles three images; the expected values are ExifTool's
     * decodings of the same bytes.
     */
    @Test
    fun testMpfIsParsedFromTheApp2Segment() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(15)))

        val mpf = assertNotNull(metadata.mpf)

        assertEquals("0100", mpf.version)
        assertEquals(3, mpf.numberOfImages)
    }

    /**
     * Every line follows the regular "<key> = <value>" pattern of the
     * other metadata sections.
     */
    @Test
    fun testToStringUsesTheKeyEqualsValuePattern() {

        val toString = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(15))).mpf.toString()

        assertTrue("MPFVersion = 0100" in toString)
        assertTrue("NumberOfImages = 3" in toString)

        assertFalse(toString.contains(" : "), "No field may use the colon pattern: $toString")
    }
}
