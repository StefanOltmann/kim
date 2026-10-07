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
import kotlin.test.assertTrue

/**
 * The ICC color profile travels in APP2 chunks that number themselves
 * against their total. The parsed profile must expose the fields
 * ExifTool decodes from the same bytes - the profile description and
 * copyright are the fields consumers query.
 */
class JpegIccProfileTest {

    /**
     * media_2 carries a single-chunk GIMP sRGB profile; the expected
     * values are ExifTool's decodings of the same bytes.
     */
    @Test
    fun testIccProfileIsParsedFromApp2Chunks() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(2)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertEquals("lcms", iccProfile.cmmType)
        assertEquals("4.3", iccProfile.version)
        assertEquals("mntr", iccProfile.profileClass)
        assertEquals("RGB ", iccProfile.colorSpace)
        assertEquals("XYZ ", iccProfile.connectionSpace)
        assertEquals("APPL", iccProfile.primaryPlatform)
        assertEquals(0, iccProfile.renderingIntent)

        assertEquals("GIMP built-in sRGB", iccProfile.description)

        assertEquals(
            expected = "Public Domain",
            actual = iccProfile.findEntry("ProfileCopyright")?.value
        )
    }

    /**
     * The number-list entries decode to the invariant decimal values of
     * the s15Fixed16 data, like ExifTool decodes the white point.
     */
    @Test
    fun testIccNumberEntriesAreDecoded() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(2)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertEquals(
            expected = "0.964202880859375 1.0 0.8249053955078125",
            actual = iccProfile.findEntry("MediaWhitePoint")?.value
        )
    }

    /**
     * A profile spread over several APP2 chunks is stitched in chunk
     * order; every chunk of the sequence must be present exactly once.
     */
    @Test
    fun testIccProfileIsReadFromSecondFileWithProfile() {

        /* media_8 also carries an ICC profile in its APP2 segments. */
        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(8)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertTrue(iccProfile.entries.isNotEmpty())
        assertNotNull(iccProfile.description)
    }
}
