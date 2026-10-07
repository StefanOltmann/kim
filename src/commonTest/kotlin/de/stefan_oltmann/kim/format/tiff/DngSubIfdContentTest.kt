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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * media_66 carries five SubIFDs: the raw at index 0 and the tiled
 * lossy-JPEG preview (NewSubfileType = 1) at index 4, per the ExifTool
 * reference dump. Both must be reachable under their own directory
 * types.
 */
class DngSubIfdContentTest {

    @Test
    fun testSubIfd0CarriesTheRawAndSubIfd4ThePreview() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(66)))

        val subIfd0 = assertNotNull(metadata.findTiffDirectory(TiffConstants.EXIF_SUB_IFD0))

        assertEquals(0, subIfd0.findField(TiffTag.TIFF_TAG_NEW_SUBFILE_TYPE)?.toShort())

        val subIfd4 = assertNotNull(metadata.findTiffDirectory(TiffConstants.EXIF_SUB_IFD4))

        assertEquals(1, subIfd4.findField(TiffTag.TIFF_TAG_NEW_SUBFILE_TYPE)?.toShort())
    }
}
