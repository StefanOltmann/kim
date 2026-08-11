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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class MetadataSummaryConverterTest {

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testReadMetadataFromBytes() {

        val metadataMap = mutableMapOf<String, MetadataSummary>()

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val bytes = if (index > KimTestData.HIGHEST_JPEG_INDEX)
                KimTestData.getBytesOf(index)
            else
                KimTestData.getHeaderBytesOf(index)

            /* Skip HEIC as it's not supported right now. */
            if (index == KimTestData.HEIC_TEST_IMAGE_INDEX)
                continue

            /* Broken files are rejected by the segment length validation. */
            if (rejectedJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(bytes)
                }

                continue
            }

            val summary = Kim.readMetadata(bytes)?.convertToSummary()

            assertNotNull(summary)

            metadataMap[KimTestData.getFileName(index)] = summary
        }

        assertEquals(
            expected = KimTestData.getMetadataCsvString(),
            actual = createCsvString(metadataMap)
        )
    }

    private fun createCsvString(metadataMap: Map<String, MetadataSummary>): String {

        val stringBuilder = StringBuilder()

        stringBuilder.appendLine(
            "name;mediaFormat;widthPx;heightPx;orientation;takenDate;latitude;longitude;" +
                "locationName;location;city;state;country;" +
                "cameraMake;cameraModel;lensMake;lensModel;iso;exposureTime;fNumber;focalLength;" +
                "title;description;flagged;rating;keywords;personsInAlbums;" +
                "thumbnailImageSize;thumbnailBytes.size;orientedSize.width;orientedSize.height"
        )

        for (entry in metadataMap.entries) {

            val name = entry.key
            val metadata = entry.value

            stringBuilder.appendLine(
                "$name;${metadata.mediaFormat};${metadata.widthPx};${metadata.heightPx};" +
                    "${metadata.orientation};${metadata.takenDate};" +
                    "${metadata.gpsCoordinates?.latitude};${metadata.gpsCoordinates?.longitude};" +
                    "${metadata.locationShown?.name};${metadata.locationShown?.street};" +
                    "${metadata.locationShown?.city};${metadata.locationShown?.state};" +
                    "${metadata.locationShown?.country};" +
                    "${metadata.cameraMake};${metadata.cameraModel};${metadata.lensMake};" +
                    "${metadata.lensModel};${metadata.iso};${metadata.exposureTime};" +
                    "${metadata.fNumber};${metadata.focalLength};" +
                    "${metadata.title};${metadata.description};" +
                    "${metadata.flagged};${metadata.rating?.value};" +
                    "${metadata.keywords};${metadata.personsInImage};" +
                    "${metadata.thumbnailImageSize};${metadata.thumbnailBytes?.size};" +
                    "${metadata.orientedSize?.width};${metadata.orientedSize?.height}"
            )
        }

        return stringBuilder.toString()
    }

    private companion object {

        /* Media 44, 45 and 47 contain invalid segment lengths. */
        private val rejectedJpegIds = setOf(44, 45, 47)
    }
}
