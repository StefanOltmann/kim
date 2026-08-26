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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.xmp.XMPRegionArea
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class XmpReaderTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @Test
    fun testReadAcdSeeXmpFile() {

        val xmp = KimTestData.getXmp("acdsee_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_664_279_361_000,
                rating = ExifRating.THREE_STARS,
                keywords = setOf("Braut", "Bräutigam", "Paar", "After-Party")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadDigikamXmpFile() {

        val xmp = KimTestData.getXmp("digikam_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_664_279_361_000,
                description = "Standard caption",
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.2193388,
                    longitude = 8.239984883333333
                ),
                locationShown = LocationShown(
                    name = null,
                    street = "Wahnbek",
                    city = "Rastede",
                    state = "Niedersachsen",
                    country = "Germany"
                ),
                rating = ExifRating.FOUR_STARS,
                keywords = setOf("Pflanze", "Ecke", "MacBook"),
                faces = mapOf(
                    "MacBook" to XMPRegionArea(0.581172, 0.66247, 0.583093, 0.502398)
                )
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadExiftoolXmpFile() {

        val xmp = KimTestData.getXmp("exiftool_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                takenDate = 1_540_041_598_000,
                description = "orange fox walking on street",
                rating = ExifRating.THREE_STARS,
                keywords = setOf(
                    "\"fuchs\"",
                    "<HALLO>",
                    "fox",
                    "fuchs",
                    "fuchs = \"süß\"",
                    "süßer fuchs",
                    "was solls"
                ),
                faces = mapOf(
                    "Swiper" to XMPRegionArea(0.404336, 0.422313, 0.124503, 0.240097)
                ),
                personsInImage = setOf("Swiper")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadMylioXmpFile() {

        val xmp = KimTestData.getXmp("mylio_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_456_064_625_420,
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.21939166666667,
                    longitude = 8.239661666666667
                ),
                title = "sample title",
                description = "This is the description",
                rating = ExifRating.REJECTED,
                keywords = setOf("animal", "bird"),
                faces = mapOf(
                    "Eye Left" to XMPRegionArea(0.295179, 0.278880, 0.033245, 0.05),
                    "Eye Right" to XMPRegionArea(0.814990, 0.472579, 0.033245, 0.05),
                    "Nothing" to XMPRegionArea(0.501552, 0.905484, 0.033245, 0.05)
                ),
                personsInImage = setOf("Eye Left", "Eye Right", "Nothing")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadNarrativeXmpFile() {

        val xmp = KimTestData.getXmp("narrative_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                orientation = TiffOrientation.ROTATE_RIGHT,
                rating = ExifRating.FOUR_STARS,
                keywords = emptySet(),
                faces = emptyMap(),
                personsInImage = emptySet()
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadNarrativeFromMylioXmpFile() {

        val xmp = KimTestData.getXmp("narrative_from_mylio_sample.xmp")

        assertEquals(
            expected = MetadataSummary(
                takenDate = 1_540_041_598_620,
                description = "orange fox walking on street",
                rating = ExifRating.FIVE_STARS,
                keywords = setOf(
                    "\"fuchs\"",
                    "<HALLO>",
                    "fox",
                    "fuchs",
                    "fuchs = \"süß\"",
                    "süßer fuchs",
                    "was solls"
                ),
                faces = mapOf(
                    "Swiper" to XMPRegionArea(0.404336, 0.422313, 0.124503, 0.240097)
                ),
                personsInImage = setOf("Swiper")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    /**
     * Regression test: a DateTimeOriginal with a negative UTC offset must not
     * be dropped.
     */
    @Test
    fun testReadTakenDateWithNegativeUtcOffset() {

        /* language=XML */
        val xmp = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <rdf:Description rdf:about=""
                        xmlns:exif="http://ns.adobe.com/exif/1.0/"
                      exif:DateTimeOriginal="2023-05-12T18:04:00-05:00"/>
                  </rdf:RDF>
                </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        val summary = XmpReader.readMetadata(xmp)

        /* 2023-05-12T18:04:00 interpreted in the test timezone (GMT+02:00). */
        assertEquals(
            expected = 1_683_907_440_000,
            actual = summary.takenDate
        )
    }

    /**
     * Regression test: corrupt XMP with coordinates far outside the
     * valid range must not flow into the summary. The write path rejects
     * such coordinates, so the read path must do the same.
     */
    @Test
    fun testReadInvalidGpsCoordinatesYieldsNull() {

        val xmp = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:exif="http://ns.adobe.com/exif/1.0/"
                    exif:GPSLatitude="400,999N"
                    exif:GPSLongitude="8,14.3990930E"/>
              </rdf:RDF>
            </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        assertNull(XmpReader.readMetadata(xmp).gpsCoordinates)
    }
}
