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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class XmpWriterEdgeCasesTest {

    private lateinit var xmpMeta: XMPMeta

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
        xmpMeta = XMPMetaFactory.create()
    }

    private fun apply(update: MetadataUpdate) {
        XmpWriter.updateXmp(
            xmpMeta = xmpMeta,
            update = update,
            writePackageWrapper = false
        )
    }

    @Test
    fun testUpdateRemovesDate() {

        apply(MetadataUpdate.TakenDate(0L))

        assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))

        apply(MetadataUpdate.TakenDate(null))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))
    }

    @Test
    fun testUpdateRemovesGpsCoordinates() {

        apply(
            MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661))
        )

        assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))

        apply(MetadataUpdate.GpsCoordinates(null))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))
    }

    @Test
    fun testUpdateRemovesLocationShown() {

        apply(
            MetadataUpdate.LocationShown(
                LocationShown(
                    name = "Times Square",
                    street = null,
                    city = "New York",
                    state = "NY",
                    country = "USA"
                )
            )
        )

        assertNotNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))

        apply(MetadataUpdate.LocationShown(null))

        assertNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))
    }

    @Test
    fun testUpdateGpsCoordinatesAndLocationShownRemovesBoth() {

        apply(
            MetadataUpdate.GpsCoordinatesAndLocationShown(
                gpsCoordinates = null,
                locationShown = null
            )
        )

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))
        assertNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))
    }

    @Test
    fun testFlaggingResetsRejectedRating() {

        apply(MetadataUpdate.Rating(ExifRating.REJECTED))
        assertEquals(ExifRating.REJECTED.value, xmpMeta.getPropertyInteger(XMP_NS_XMP, "Rating"))

        /* Flagging a rejected photo resets the rating. */
        apply(MetadataUpdate.Flagged(true))

        assertEquals(ExifRating.UNRATED.value, xmpMeta.getPropertyInteger(XMP_NS_XMP, "Rating"))
    }

    @Test
    fun testRejectingRemovesFlag() {

        apply(MetadataUpdate.Flagged(true))
        apply(MetadataUpdate.Orientation(TiffOrientation.STANDARD))

        /* Rejecting a flagged photo removes the flag. */
        apply(MetadataUpdate.Rating(ExifRating.REJECTED))

        assertNull(xmpMeta.getPropertyBoolean(XMP_NS_XMP, "Flagged"))
    }

    @Test
    fun testUpdateWithSystemTimeZone() {

        Kim.underUnitTesting = false

        try {
            apply(MetadataUpdate.TakenDate(0L))

            assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))
        } finally {
            Kim.underUnitTesting = true
        }
    }

    private companion object {

        const val XMP_NS_EXIF = "http://ns.adobe.com/exif/1.0/"
        const val XMP_NS_XMP = "http://ns.adobe.com/xap/1.0/"
        const val XMP_NS_IPTC_EXT = "http://iptc.org/std/Iptc4xmpExt/2008-02-29/"
    }
}
