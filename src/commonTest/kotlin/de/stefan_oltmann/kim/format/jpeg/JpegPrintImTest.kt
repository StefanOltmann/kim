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
import kotlin.test.assertNotNull

/**
 * The Print Image Matching block travels in the EXIF tag 0xC4A5. The
 * parsed block must expose the values ExifTool decodes from the same
 * bytes.
 */
class JpegPrintImTest {

    /**
     * media_20 carries a version "0250" block with 20 entries; the
     * expected values are ExifTool's decodings of the same bytes.
     */
    @Test
    fun testPrintImIsParsedFromTheExifTag() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(20)))

        val printIm = assertNotNull(metadata.printIm)

        assertEquals("0250", printIm.version)

        assertEquals(20, printIm.entries.size)

        assertEquals(0x0001, printIm.entries[0].tag)
        assertEquals(1310740, printIm.entries[0].value)
    }
}
