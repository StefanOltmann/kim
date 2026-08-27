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
package de.stefan_oltmann.kim.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KimValueFormatterTest {

    @Test
    fun testFormatFileLength() {

        assertEquals(
            "60 B",
            KimValueFormatter.formatFileLength(60)
        )

        assertEquals(
            "999 B",
            KimValueFormatter.formatFileLength(999)
        )

        assertEquals(
            "1 KB",
            KimValueFormatter.formatFileLength(1000)
        )

        /* Ensure no fractions! */
        assertEquals(
            "1 KB",
            KimValueFormatter.formatFileLength(1024)
        )

        assertEquals(
            "300 KB",
            KimValueFormatter.formatFileLength(300_000)
        )

        /* Ensure no fractions! */
        assertEquals(
            "301 KB",
            KimValueFormatter.formatFileLength(300_600)
        )

        assertEquals(
            "8 MB",
            KimValueFormatter.formatFileLength(8_000_000)
        )

        /* Only one fraction */
        assertEquals(
            "8.2 MB",
            KimValueFormatter.formatFileLength(8_230_000)
        )

        assertEquals(
            "1 GB",
            KimValueFormatter.formatFileLength(1_000_000_000)
        )

        assertEquals(
            "1.5 GB",
            KimValueFormatter.formatFileLength(1_500_000_000)
        )
    }

    @Test
    fun testCreateCameraOrLensName() {

        assertNull(
            KimValueFormatter.createCameraOrLensName(
                make = null,
                model = null
            )
        )

        assertNull(
            KimValueFormatter.createCameraOrLensName(
                make = "",
                model = ""
            )
        )

        assertNull(
            KimValueFormatter.createCameraOrLensName(
                make = null,
                model = ""
            )
        )

        assertNull(
            KimValueFormatter.createCameraOrLensName(
                make = "",
                model = null
            )
        )

        /*
         * Test that "Canon" is not repeated
         */
        assertEquals(
            "Canon EOS 60D",
            KimValueFormatter.createCameraOrLensName(
                make = "Canon",
                model = "Canon EOS 60D"
            )
        )

        assertEquals(
            "Olympus E-M10",
            KimValueFormatter.createCameraOrLensName(
                make = "OLYMPUS IMAGING CORP.",
                model = "E-M10"
            )
        )

        /**
         * Test that "Olympus" has correct casing in model name
         */
        assertEquals(
            "Olympus M.40-150mm F4.0-5.6 R",
            KimValueFormatter.createCameraOrLensName(
                make = null,
                model = "OLYMPUS M.40-150mm F4.0-5.6 R"
            )
        )

        assertEquals(
            "Sony DSLR-A200",
            KimValueFormatter.createCameraOrLensName(
                make = "SONY",
                model = "DSLR-A200"
            )
        )

        /**
         * Test that "Nikon" has correct casing and is not repeated
         */
        assertEquals(
            "Nikon D800",
            KimValueFormatter.createCameraOrLensName(
                make = "NIKON CORPORATION",
                model = "NIKON D800"
            )
        )

        assertEquals(
            "Fujifilm X-T4",
            KimValueFormatter.createCameraOrLensName(
                make = "FUJIFILM",
                model = "X-T4"
            )
        )
    }

    @Test
    fun testFormatIso() {

        assertEquals(
            "ISO 32",
            KimValueFormatter.formatIso(32)
        )

        assertEquals(
            "ISO 100",
            KimValueFormatter.formatIso(100)
        )

        assertEquals(
            "ISO 400",
            KimValueFormatter.formatIso(400)
        )
    }

    @Test
    @Suppress("LongMethod")
    fun testFormatExposureTime() {

        assertEquals(
            null,
            KimValueFormatter.formatExposureTime(0.0)
        )

        assertEquals(
            null,
            KimValueFormatter.formatExposureTime(-0.5)
        )

        assertEquals(
            "1/8000 s",
            KimValueFormatter.formatExposureTime(0.000125)
        )

        assertEquals(
            "1/4000 s",
            KimValueFormatter.formatExposureTime(0.00025)
        )

        assertEquals(
            "1/2000 s",
            KimValueFormatter.formatExposureTime(0.0005)
        )

        assertEquals(
            "1/1250 s",
            KimValueFormatter.formatExposureTime(0.0008)
        )

        assertEquals(
            "1/1000 s",
            KimValueFormatter.formatExposureTime(0.001)
        )

        assertEquals(
            "1/640 s",
            KimValueFormatter.formatExposureTime(0.0015625)
        )

        assertEquals(
            "1/500 s",
            KimValueFormatter.formatExposureTime(0.002)
        )

        assertEquals(
            "1/400 s",
            KimValueFormatter.formatExposureTime(0.0025)
        )

        assertEquals(
            "1/320 s",
            KimValueFormatter.formatExposureTime(0.003125)
        )

        assertEquals(
            "1/250 s",
            KimValueFormatter.formatExposureTime(0.004)
        )

        assertEquals(
            "1/200 s",
            KimValueFormatter.formatExposureTime(0.005)
        )

        assertEquals(
            "1/125 s",
            KimValueFormatter.formatExposureTime(0.008)
        )

        assertEquals(
            "1/100 s",
            KimValueFormatter.formatExposureTime(0.01)
        )

        assertEquals(
            "1/80 s",
            KimValueFormatter.formatExposureTime(0.0125)
        )

        assertEquals(
            "1/60 s",
            KimValueFormatter.formatExposureTime(0.0166)
        )

        assertEquals(
            "1/40 s",
            KimValueFormatter.formatExposureTime(0.025)
        )

        assertEquals(
            "1/30 s",
            KimValueFormatter.formatExposureTime(0.033)
        )

        assertEquals(
            "1/25 s",
            KimValueFormatter.formatExposureTime(0.04)
        )

        assertEquals(
            "1/15 s",
            KimValueFormatter.formatExposureTime(0.066)
        )

        assertEquals(
            "1/10 s",
            KimValueFormatter.formatExposureTime(0.1)
        )

        assertEquals(
            "1/8 s",
            KimValueFormatter.formatExposureTime(0.125)
        )

        assertEquals(
            "1/4 s",
            KimValueFormatter.formatExposureTime(0.25)
        )

        assertEquals(
            "1/2 s",
            KimValueFormatter.formatExposureTime(0.5)
        )

        assertEquals(
            "1'' s",
            KimValueFormatter.formatExposureTime(1.0)
        )

        assertEquals(
            "1'' 1/8 s",
            KimValueFormatter.formatExposureTime(1.125)
        )

        assertEquals(
            "1'' 1/4 s",
            KimValueFormatter.formatExposureTime(1.25)
        )

        assertEquals(
            "1'' 1/2 s",
            KimValueFormatter.formatExposureTime(1.5)
        )

        assertEquals(
            "2'' s",
            KimValueFormatter.formatExposureTime(2.0)
        )

        assertEquals(
            "2'' 1/2 s",
            KimValueFormatter.formatExposureTime(2.5)
        )

        assertEquals(
            "10'' s",
            KimValueFormatter.formatExposureTime(10.0)
        )

        assertEquals(
            "15'' s",
            KimValueFormatter.formatExposureTime(15.0)
        )

        assertEquals(
            "30'' s",
            KimValueFormatter.formatExposureTime(30.0)
        )
    }

    @Test
    fun testFormatFNumber() {

        assertEquals(
            "ƒ2",
            KimValueFormatter.formatFNumber(2.0)
        )

        assertEquals(
            "ƒ2.8",
            KimValueFormatter.formatFNumber(2.8)
        )

        assertEquals(
            "ƒ8",
            KimValueFormatter.formatFNumber(8.0)
        )
    }

    @Test
    fun testFormatFocalLength() {

        assertEquals(
            "4.2 mm",
            KimValueFormatter.formatFocalLength(4.2)
        )

        assertEquals(
            "18 mm",
            KimValueFormatter.formatFocalLength(18.0)
        )
    }
}
