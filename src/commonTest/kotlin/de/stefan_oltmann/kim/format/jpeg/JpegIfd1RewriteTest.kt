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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * IFD1 consists exclusively of thumbnail fields. When the thumbnail
 * bytes cannot be captured (some cameras write garbage at the
 * JPEGInterchangeFormat offset), the rewrite must drop the whole
 * directory instead of emitting a hollow IFD1 whose Compression field
 * references a thumbnail that no longer exists.
 */
class JpegIfd1RewriteTest {

    /**
     * Regression test: media_9 carries an IFD1 whose
     * JPEGInterchangeFormat offset points at non-JPEG bytes. The EXIF
     * rewrite dropped the offset and length fields but kept the rest of
     * the IFD1, producing a hollow directory that validators flag.
     */
    @Test
    fun testRewriteDoesNotProduceAHollowIfd1() {

        val original = KimTestData.getBytesOf(index = 9)

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.TakenDate(1700000000000L))
        )

        val metadata = Kim.readMetadata(updated)

        val exif = assertNotNull(metadata?.exif)

        val ifd1Directories = exif.directories
            .filter { it.type == TiffDirectoryType.TIFF_DIRECTORY_IFD1.typeId }

        for (ifd1 in ifd1Directories) {

            val hasCompression = ifd1.findField(TiffTag.TIFF_TAG_COMPRESSION) != null

            val hasThumbnail = ifd1.findField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT) != null

            assertTrue(
                !hasCompression || hasThumbnail,
                "IFD1 carries thumbnail framing without the thumbnail itself: $ifd1"
            )
        }
    }
}
