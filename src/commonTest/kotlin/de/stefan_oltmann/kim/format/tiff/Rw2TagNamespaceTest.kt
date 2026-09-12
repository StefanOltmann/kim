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
import de.stefan_oltmann.kim.format.tiff.constant.PanasonicRawTag
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * RW2 and RWL files use their own tag namespace in IFD0: the low tag
 * numbers carry Panasonic RAW tags like Compression and RawDataOffset,
 * which do not exist in the standard TIFF table. Like ExifTool, the
 * Panasonic table must take precedence there, or the tags are reported
 * under misleading standard names.
 */
class Rw2TagNamespaceTest {

    @Test
    fun testRw2Ifd0ResolvesPanasonicTagNames() {

        val metadata = assertNotNull(
            Kim.readMetadata(KimTestData.getBytesOf(KimTestData.RW2_TEST_IMAGE_INDEX))
        )

        val ifd0 = assertNotNull(metadata.exif).directories.first()

        val compression = assertNotNull(
            ifd0.findField(PanasonicRawTag.COMPRESSION),
            "The IFD0 of the RW2 must contain the Panasonic Compression tag."
        )

        assertEquals("Compression", compression.tagInfo?.name)

        val rawDataOffset = assertNotNull(
            ifd0.findField(PanasonicRawTag.RAW_DATA_OFFSET),
            "The IFD0 of the RW2 must contain the Panasonic RawDataOffset tag."
        )

        assertEquals("RawDataOffset", rawDataOffset.tagInfo?.name)

        val stripOffsets = assertNotNull(
            ifd0.findField(PanasonicRawTag.STRIP_OFFSETS),
            "The IFD0 of the RW2 must contain the Panasonic StripOffsets tag."
        )

        assertEquals("StripOffsets", stripOffsets.tagInfo?.name)
    }
}
