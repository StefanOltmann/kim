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
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Round-trips the representative update set through write and parse on
 * every target. The JVM-only [XmpWriterTest] pins the exact serialized
 * bytes; this suite pins the semantic content, so the native targets -
 * whose serializer assigns different namespace prefixes - are covered
 * as well.
 */
class XmpWriterRoundTripTest {

    private val updates = setOf(
        MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
        MetadataUpdate.TakenDate(1_690_889_862_000L), // 01.08.2023 13:37:42
        MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661)),
        MetadataUpdate.Rating(ExifRating.THREE_STARS),
        MetadataUpdate.Keywords(setOf("fox", "fuchs", "<swiper>")),
        MetadataUpdate.Persons(setOf("John"))
    )

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    @Test
    fun testWrittenXmpParsesBackWithAllUpdates() {

        val xmpMeta = XMPMetaFactory.create()

        val serialized = XmpWriter.updateXmp(
            xmpMeta = xmpMeta,
            updates = updates,
            writePackageWrapper = true
        )

        val summary = XmpReader.readMetadata(serialized)

        assertEquals(TiffOrientation.ROTATE_RIGHT, summary.orientation)

        assertEquals(1_690_889_862_000L, summary.takenDate)

        assertNotNull(summary.gpsCoordinates)

        assertEquals(53.219391, summary.gpsCoordinates.latitude, absoluteTolerance = 0.0001)
        assertEquals(8.239661, summary.gpsCoordinates.longitude, absoluteTolerance = 0.0001)

        assertEquals(ExifRating.THREE_STARS, summary.rating)

        assertEquals(
            expected = setOf("fox", "fuchs", "<swiper>"),
            actual = summary.keywords
        )

        assertEquals(
            expected = setOf("John"),
            actual = summary.personsInImage
        )

        assertTrue(summary.flagged != true, "An unflagged write must not set the flag.")
    }
}
