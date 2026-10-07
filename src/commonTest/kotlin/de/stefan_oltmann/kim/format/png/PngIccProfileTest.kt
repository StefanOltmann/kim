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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The iCCP chunk carries the ICC color profile zlib compressed behind
 * a keyword. The parsed profile must expose the fields ExifTool
 * decodes from the same bytes.
 */
class PngIccProfileTest {

    /**
     * media_51 carries the GIMP sRGB profile written by GIMP itself;
     * the expected description is ExifTool's decoding of the same
     * bytes.
     */
    @Test
    fun testIccProfileIsParsedFromIccpChunk() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(51)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertEquals("GIMP built-in sRGB", iccProfile.description)
        assertEquals("mntr", iccProfile.profileClass)
        assertEquals("RGB ", iccProfile.colorSpace)
    }
}
