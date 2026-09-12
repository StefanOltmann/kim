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
package de.stefan_oltmann.kim.format.tiff.write

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The TIFF and EXIF specifications recommend that values of multi-byte
 * field types start at an even offset. ExifTool's validator flags odd
 * value offsets, so the rewrite must realign the values it lays out -
 * while the MakerNote still stays at its exact original offset.
 */
class TiffEvenValueOffsetsTest {

    /**
     * Regression test: media_23's MakerNote anchor is odd, and every
     * value laid out behind the anchored MakerNote inherited the odd
     * base offset.
     */
    @Test
    fun testRewrittenExifUsesEvenValueOffsets() {

        val updatedBytes = Kim.update(
            bytes = KimTestData.getBytesOf(index = 23),
            updates = setOf(MetadataUpdate.TakenDate(1700000000000L))
        )

        val metadata = assertNotNull(Kim.readMetadata(updatedBytes))

        val exif = assertNotNull(metadata.exif)

        for (directory in exif.directories) {

            for (field in directory.entries) {

                val valueOffset = field.valueOffset ?: continue

                /*
                 * The MakerNote keeps its exact original offset at all
                 * costs - even when that offset is odd.
                 */
                if (field.tag == ExifTag.EXIF_TAG_MAKER_NOTE.tag)
                    continue

                assertTrue(
                    valueOffset % 2 == 0,
                    "Odd value offset $valueOffset for ${field.tagFormatted} " +
                        "in directory type ${directory.type}."
                )
            }
        }
    }
}
