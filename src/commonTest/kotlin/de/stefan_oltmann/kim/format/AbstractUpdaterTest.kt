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
package de.stefan_oltmann.kim.format

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

abstract class AbstractUpdaterTest(
    val format: String,
    val testThumbnail: Boolean = true
) {

    private val keywordWithUmlauts = "Äußerst öffentlich"

    private val titleWithUmlauts = "Süße Vögelchen"

    private val descriptionWithUmlauts = "Äußerst süße Vögel fliegen durch die Lüfte."

    private val crashBuildingGps = GpsCoordinates(
        latitude = 53.219391,
        longitude = 8.239661
    )

    private val crashBuildingLocation = LocationShown(
        name = "//CRASH",
        street = "Schafjückenweg 2",
        city = "Rastede",
        state = "Niedersachsen",
        country = "Deutschland"
    )

    private val timestamp = 1_689_166_125_401 // 2023:07:12 12:48:45

    private val resourcePath: String = "de/stefan_oltmann/kim/updates_$format"

    private val originalBytes = Resource("$resourcePath/original.$format").readBytes()

    private val noMetadataBytes = Resource("$resourcePath/no_metadata.$format").readBytes()

    private val thumbnailBytes = Resource("$resourcePath/../testdata/test_thumb.jpg").readBytes()

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {

        /*
         * Reset the override, so it cannot leak into test classes that
         * exercise the platform default time zone.
         */
        Kim.defaultTimeZone = null
    }

    @Test
    fun testUpdateOrientation() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        compare("rotated_right.$format", newBytes)
    }

    @Test
    fun testUpdateOrientationOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        compare("rotated_right.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateTakenDate() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.TakenDate(timestamp)
        )

        compare("new_taken_date.$format", newBytes)
    }

    @Test
    fun testUpdateTakenDateOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.TakenDate(timestamp)
        )

        compare("new_taken_date.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateGpsCoordinates() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.GpsCoordinates(crashBuildingGps)
        )

        compare("new_gps_coordinates.$format", newBytes)
    }

    @Test
    fun testUpdateGpsCoordinatesOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.GpsCoordinates(crashBuildingGps)
        )

        compare("new_gps_coordinates.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateLocationShown() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.LocationShown(crashBuildingLocation)
        )

        compare("new_location_shown.$format", newBytes)
    }

    @Test
    fun testUpdateLocationShownOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.LocationShown(crashBuildingLocation)
        )

        compare("new_location_shown.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateGpsCoordinatesAndLocationShown() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.GpsCoordinatesAndLocationShown(
                gpsCoordinates = crashBuildingGps,
                locationShown = crashBuildingLocation
            )
        )

        compare("new_gps_coordinates_and_location_shown.$format", newBytes)
    }

    @Test
    fun testUpdateGpsCoordinatesAndLocationShownOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.GpsCoordinatesAndLocationShown(
                gpsCoordinates = crashBuildingGps,
                locationShown = crashBuildingLocation
            )
        )

        compare("new_gps_coordinates_and_location_shown.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateTitle() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Title(titleWithUmlauts)
        )

        compare("new_title.$format", newBytes)
    }

    @Test
    fun testUpdateTitleOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Title(titleWithUmlauts)
        )

        compare("new_title.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateDescription() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Description(descriptionWithUmlauts)
        )

        compare("new_description.$format", newBytes)
    }

    @Test
    fun testUpdateDescriptionOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Description(descriptionWithUmlauts)
        )

        compare("new_description.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateFlagged() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Flagged(true)
        )

        compare("new_flagged.$format", newBytes)
    }

    @Test
    fun testUpdateFlaggedOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Flagged(true)
        )

        compare("new_flagged.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateRating() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Rating(ExifRating.FOUR_STARS)
        )

        compare("new_rating.$format", newBytes)
    }

    @Test
    fun testUpdateRatingOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Rating(ExifRating.FOUR_STARS)
        )

        compare("new_rating.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateKeywords() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts))
        )

        compare("new_keywords.$format", newBytes)
    }

    @Test
    fun testUpdateKeywordsOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts))
        )

        compare("new_keywords.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdatePersons() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Persons(setOf("Swiper", "Dora"))
        )

        compare("new_persons.$format", newBytes)
    }

    @Test
    fun testUpdatePersonsOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Persons(setOf("Swiper", "Dora"))
        )

        compare("new_persons.no_metadata.$format", newBytes)
    }

    @Test
    fun testUpdateMultipleFieldsSimultaneously() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            updates = setOf(
                MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
                MetadataUpdate.TakenDate(timestamp),
                MetadataUpdate.GpsCoordinates(crashBuildingGps),
                MetadataUpdate.LocationShown(crashBuildingLocation),
                MetadataUpdate.Title(titleWithUmlauts),
                MetadataUpdate.Description(descriptionWithUmlauts),
                MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts)),
                MetadataUpdate.Rating(ExifRating.FOUR_STARS),
                MetadataUpdate.Persons(setOf("Swiper", "Dora"))
            )
        )

        compare("new_multiple_updates.$format", newBytes)
    }

    @Test
    fun testUpdateMultipleFieldsSimultaneouslyOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            updates = setOf(
                MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
                MetadataUpdate.TakenDate(timestamp),
                MetadataUpdate.GpsCoordinates(crashBuildingGps),
                MetadataUpdate.LocationShown(crashBuildingLocation),
                MetadataUpdate.Title(titleWithUmlauts),
                MetadataUpdate.Description(descriptionWithUmlauts),
                MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts)),
                MetadataUpdate.Rating(ExifRating.FOUR_STARS),
                MetadataUpdate.Persons(setOf("Swiper", "Dora"))
            )
        )

        compare("new_multiple_updates.no_metadata.$format", newBytes)
    }

    @Test
    fun testDeleteMetadata() {

        val newBytes = Kim.deleteMetadata(originalBytes)

        compare("deleted_metadata.$format", newBytes)
    }

    @Test
    fun testDeleteMetadataRemovesAllMetadata() {

        val newBytes = Kim.deleteMetadata(originalBytes)

        val metadata = Kim.readMetadata(newBytes)!!

        assertNull(metadata.exif)
        assertNull(metadata.exifBytes)
        assertNull(metadata.iptc)
        assertNull(metadata.xmp)
    }

    @Test
    fun testDeleteMetadataKeepsImageSize() {

        val originalMetadata = Kim.readMetadata(originalBytes)!!

        val newBytes = Kim.deleteMetadata(originalBytes)

        val metadata = Kim.readMetadata(newBytes)!!

        assertEquals(originalMetadata.imageSize, metadata.imageSize)
    }

    @Test
    fun testDeleteMetadataIsIdempotent() {

        val once = Kim.deleteMetadata(originalBytes)

        val twice = Kim.deleteMetadata(once)

        assertContentEquals(once, twice)
    }

    @Test
    fun testUpdateWithEmptySetIsRejected() {

        /*
         * An update call without any updates makes no sense, so the library
         * must refuse it instead of silently returning the unchanged file.
         */
        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = originalBytes,
                updates = emptySet()
            )
        }
    }

    @Test
    fun testUpdateThumbnail() {

        if (!testThumbnail) {

            assertFailsWith<ImageWriteException> {
                Kim.updateThumbnail(
                    bytes = originalBytes,
                    thumbnailBytes = thumbnailBytes
                )
            }

            return
        }

        val newBytes = Kim.updateThumbnail(
            bytes = originalBytes,
            thumbnailBytes = thumbnailBytes
        )

        compare("new_thumbnail.$format", newBytes)
    }

    @Test
    fun testUpdateThumbnailOnEmptyImage() {

        if (!testThumbnail) {

            assertFailsWith<ImageWriteException> {
                Kim.updateThumbnail(
                    bytes = noMetadataBytes,
                    thumbnailBytes = thumbnailBytes
                )
            }

            return
        }

        val newBytes = Kim.updateThumbnail(
            bytes = noMetadataBytes,
            thumbnailBytes = thumbnailBytes
        )

        compare("new_thumbnail.no_metadata.$format", newBytes)
    }

    private fun compare(fileName: String, actualBytes: ByteArray) {

        val updateDir = Path("build/updates_$format")

        val resource = Resource("$resourcePath/$fileName")

        /*
         * Write the reference image if it does not exist.
         */
        if (!resource.exists()) {

            SystemFileSystem.createDirectories(updateDir)

            Path(updateDir, fileName)
                .writeBytes(actualBytes)

            fail("Reference image $fileName does not exist.")
        }

        val expectedBytes = resource.readBytes()

        if (!expectedBytes.contentEquals(actualBytes)) {

            SystemFileSystem.createDirectories(updateDir)

            Path(updateDir, fileName)
                .writeBytes(actualBytes)

            /* Also write a string representation to make differences easier to inspect. */
            Path(updateDir, "$fileName.txt")
                .writeBytes(Kim.readMetadata(actualBytes).toString().encodeToByteArray())

            fail("Photo $fileName does not match the reference image.")
        }

        /*
         * Make sure the string representation of the metadata matches the committed golden.
         */
        val actualStringRepresentation = Kim.readMetadata(actualBytes).toString()

        val expectedResource = Resource("$resourcePath/$fileName.txt")

        /*
         * Write the reference text dump if it does not exist.
         */
        if (!expectedResource.exists()) {

            SystemFileSystem.createDirectories(updateDir)

            Path(updateDir, "$fileName.txt")
                .writeBytes(actualStringRepresentation.encodeToByteArray())

            fail("Reference text dump $fileName.txt does not exist.")
        }

        if (actualStringRepresentation != expectedResource.readText()) {

            SystemFileSystem.createDirectories(updateDir)

            Path(updateDir, "$fileName.txt")
                .writeBytes(actualStringRepresentation.encodeToByteArray())

            fail("Photo $fileName does not match the expected string representation.")
        }
    }
}
