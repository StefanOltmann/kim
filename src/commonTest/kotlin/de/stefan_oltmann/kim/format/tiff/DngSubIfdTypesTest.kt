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
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * A DNG's SubIFDs pointer carries up to five sub-IFDs: the raw image
 * (index 0) and, at index 4, the lossy-JPEG preview. Folding both into
 * the IFD1 directory type made findTiffDirectory(IFD1) return the wrong
 * directory and mislabeled both in the dumps - like ExifTool, they are
 * reported as their own SubIFD types.
 */
class DngSubIfdTypesTest {

    /**
     * media_66 has no chain IFD1 at all - the thumbnail IFD space must
     * not report one of the sub-IFDs as IFD1.
     */
    @Test
    fun testNoDirectoryIsReportedAsIfd1() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(66)))

        assertNull(metadata.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD1))
    }
}
