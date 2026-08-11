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
package de.stefan_oltmann.kim.model

import de.stefan_oltmann.kim.common.KimValueFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MetadataSummaryTest {

    @Test
    fun testMegaPixelCount() {

        assertEquals(0, MetadataSummary().megaPixelCount)

        assertEquals(
            expected = 1,
            actual = MetadataSummary(
                widthPx = 1000,
                heightPx = 1000
            ).megaPixelCount
        )

        assertEquals(
            expected = 12,
            actual = MetadataSummary(
                widthPx = 4000,
                heightPx = 3000
            ).megaPixelCount
        )
    }

    @Test
    fun testLocationDisplay() {

        /* No location at all. */
        assertNull(MetadataSummary().locationDisplay)

        /* GPS coordinates only. */
        assertEquals(
            expected = "GPS: 53.21939, 8.23966",
            actual = MetadataSummary(
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.2193897123,
                    longitude = 8.2396611123
                )
            ).locationDisplay
        )

        /* LocationShown takes precedence over GPS. */
        assertEquals(
            expected = "Times Square, USA",
            actual = MetadataSummary(
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.2193897123,
                    longitude = 8.2396611123
                ),
                locationShown = LocationShown(
                    name = "Times Square",
                    street = null,
                    city = null,
                    state = null,
                    country = "USA"
                )
            ).locationDisplay
        )
    }

    @Test
    fun testCameraAndLensNames() {

        /* Nothing to show. */
        assertNull(MetadataSummary().cameraName)
        assertNull(MetadataSummary().lensName)
        assertNull(MetadataSummary().cameraAndLensName)

        /* Only the make. */
        assertEquals(
            expected = "Sony",
            actual = MetadataSummary(cameraMake = "SONY").cameraName
        )

        /* Make and model. */
        assertEquals(
            expected = "Canon EOS R5",
            actual = MetadataSummary(
                cameraMake = "Canon",
                cameraModel = "EOS R5"
            ).cameraName
        )

        /* If the model starts with the make, the make is not duplicated. */
        assertEquals(
            expected = "Fujifilm X-T5",
            actual = MetadataSummary(
                cameraMake = "Fujifilm",
                cameraModel = "Fujifilm X-T5"
            ).cameraName
        )

        /* Lens name, without camera prefix. */
        assertEquals(
            expected = "24-70mm",
            actual = MetadataSummary(
                cameraMake = "Canon",
                cameraModel = "EOS R5",
                lensMake = "Canon",
                lensModel = "Canon EOS R5 24-70mm"
            ).lensName
        )

        /* Combined name. */
        assertEquals(
            expected = "Canon EOS R5 | 24-70mm",
            actual = MetadataSummary(
                cameraMake = "Canon",
                cameraModel = "EOS R5",
                lensMake = "Canon",
                lensModel = "Canon EOS R5 24-70mm"
            ).cameraAndLensName
        )
    }

    @Test
    fun testOrientedSize() {

        /* No dimensions. */
        assertNull(MetadataSummary().orientedSize)

        /* Standard orientation keeps the dimensions. */
        assertEquals(
            expected = ImageSize(4000, 3000),
            actual = MetadataSummary(
                widthPx = 4000,
                heightPx = 3000,
                orientation = TiffOrientation.STANDARD
            ).orientedSize
        )

        /* Rotated images have flipped dimensions. */
        assertEquals(
            expected = ImageSize(3000, 4000),
            actual = MetadataSummary(
                widthPx = 4000,
                heightPx = 3000,
                orientation = TiffOrientation.ROTATE_RIGHT
            ).orientedSize
        )
    }

    @Test
    fun testIsEmpty() {

        assertTrue(MetadataSummary().isEmpty())
        assertTrue(MetadataSummary.emptySummary.isEmpty())

        assertFalse(MetadataSummary(title = "Hello").isEmpty())
        assertFalse(MetadataSummary(flagged = true).isEmpty())
    }

    @Test
    fun testMergeWithNull() {

        val summary = MetadataSummary(title = "Hello")

        /* Merging null returns the same instance. */
        assertSame(summary, summary.merge(null))
    }

    @Test
    fun testMergeFillsNullFieldsOnly() {

        val base = MetadataSummary(
            title = "Title",
            widthPx = 1000,
            flagged = true
        )

        val other = MetadataSummary(
            title = "Other Title",
            widthPx = 2000,
            heightPx = 500,
            description = "Description",
            keywords = setOf("one")
        )

        val merged = base.merge(other)

        /* Existing values win. */
        assertEquals("Title", merged.title)
        assertEquals(1000, merged.widthPx)

        /* Missing values are filled. */
        assertEquals(500, merged.heightPx)
        assertEquals("Description", merged.description)
        assertEquals(setOf("one"), merged.keywords)

        /* Flags are OR-combined. */
        assertTrue(merged.flagged)
    }

    @Test
    fun testMergeFallsBackToOther() {

        val merged = MetadataSummary().merge(
            MetadataSummary(
                mediaFormat = MediaFormat.PNG,
                title = "Other Title",
                keywords = setOf("one"),
                personsInImage = setOf("Alice")
            )
        )

        assertEquals(MediaFormat.PNG, merged.mediaFormat)
        assertEquals("Other Title", merged.title)
        assertEquals(setOf("one"), merged.keywords)
        assertEquals(setOf("Alice"), merged.personsInImage)
    }

    @Test
    fun testMergeWithThumbnail() {

        val thumbnailBytes = byteArrayOf(1, 2, 3)

        val merged = MetadataSummary().merge(
            MetadataSummary(
                thumbnailImageSize = ImageSize(160, 120),
                thumbnailBytes = thumbnailBytes
            )
        )

        assertEquals(ImageSize(160, 120), merged.thumbnailImageSize)
        assertTrue(thumbnailBytes.contentEquals(merged.thumbnailBytes))
    }

    @Test
    fun testFormatFileLength() {

        assertEquals("500 B", KimValueFormatter.formatFileLength(500))
        assertEquals("2 KB", KimValueFormatter.formatFileLength(2000))
        assertEquals("1.5 MB", KimValueFormatter.formatFileLength(1_500_000))
        assertEquals("2 GB", KimValueFormatter.formatFileLength(2_000_000_000))
    }
}
