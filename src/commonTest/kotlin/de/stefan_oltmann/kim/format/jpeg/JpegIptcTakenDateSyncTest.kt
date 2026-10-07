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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The IPTC datasets 2:055 (DateCreated) and 2:060 (TimeCreated)
 * represent the taken date like EXIF DateTimeOriginal and XMP
 * exif:DateTimeOriginal do. A taken-date update that leaves them
 * behind lets every IPTC-aware tool display a different capture date
 * than the EXIF and XMP copies - like ExifTool's MWG mapping, they
 * must be rewritten with the new date.
 */
class JpegIptcTakenDateSyncTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    @Test
    fun testTakenDateUpdateWritesIptcDateCreatedAndTimeCreated() {

        val original = KimTestData.getBytesOf(1)

        /* 2024-03-15T14:30:00Z, which is 16:30:00 in GMT+02:00. */
        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.TakenDate(1710513000000L))
        )

        val records = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc).records

        assertEquals(
            expected = "20240315",
            actual = records.first { it.iptcType == IptcTypes.DATE_CREATED }.value
        )

        assertEquals(
            expected = "163000+0200",
            actual = records.first { it.iptcType == IptcTypes.TIME_CREATED }.value
        )
    }

    @Test
    fun testTakenDateRemovalRemovesIptcDateCreatedAndTimeCreated() {

        /* The ExifTool MWG-written fixture carries 2:055 and 2:060. */
        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_iptc_taken_date.jpg"
        ).readBytes()

        val withoutDate = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.TakenDate(null))
        )

        val metadata = assertNotNull(Kim.readMetadata(withoutDate))

        val records = assertNotNull(metadata.iptc).records

        assertTrue(records.none { it.iptcType == IptcTypes.DATE_CREATED })
        assertTrue(records.none { it.iptcType == IptcTypes.TIME_CREATED })

        /* The EXIF copy is removed along with the IPTC datasets. */
        assertNull(
            metadata.findTiffField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
        )
    }
}
