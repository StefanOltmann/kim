/*
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
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

/*
 * FIXME On native the result is slightly different,
 *   because MicrosoftPhoto gets a new prefix.
 *   The problem must lie in XMP Core.
 *   We need to investigate this further.
 */
class XmpWriterTest {

    private val updates = setOf(
        MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
        MetadataUpdate.TakenDate(1_690_889_862_000L), // 01.08.2023 13:37:42
        MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661)),
        MetadataUpdate.Rating(ExifRating.THREE_STARS),
        MetadataUpdate.Keywords(setOf("fox", "fuchs", "<swiper>")),
        // MetadataUpdate.Faces(mapOf("John" to RegionArea(0.2, 0.3, 0.4, 0.5))),
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
    fun testWriteAcdSeeXmpFile(): Unit =
        doCompare("acdsee_sample")

    @Test
    fun testWriteDigiKamXmpFile(): Unit =
        doCompare("digikam_sample")

    @Test
    fun testWriteExifToolXmpFile(): Unit =
        doCompare("exiftool_sample")

    @Test
    fun testWriteMylioXmpFile(): Unit =
        doCompare("mylio_sample")

    @Test
    fun testWriteNarrativeFromMylioXmpFile(): Unit =
        doCompare("narrative_from_mylio_sample")

    @Test
    fun testWriteNarrativeXmpFile(): Unit =
        doCompare("narrative_sample")

    @OptIn(ExperimentalStdlibApi::class)
    private fun doCompare(baseFileName: String) {

        val originalXmp = KimTestData.getXmp("$baseFileName.xmp")

        val xmpMeta = XMPMetaFactory.parseFromString(originalXmp)

        val actualXmp = XmpWriter.updateXmp(
            xmpMeta = xmpMeta,
            updates = updates,
            writePackageWrapper = true
        )

        val expectedXmp = KimTestData.getXmp("${baseFileName}_mod.xmp")

        val equals = expectedXmp.contentEquals(actualXmp)

        if (!equals) {

            Path("build/${baseFileName}_mod.xmp")
                .writeBytes(actualXmp.encodeToByteArray())

            fail("Photo $baseFileName has not the expected bytes!")
        }
    }
}
