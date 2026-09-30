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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JpegOrientationOffsetFinderTest {

    val expectedMap: Map<Int, Long> = mapOf<Int, Long>(
        1 to 72,
        2 to 72,
        15 to 85,
        20 to 84,
        21 to 55,
        23 to 43,
        25 to 7068,
        26 to 3447,
        28 to 66,
        29 to 114,
        30 to 54,
        31 to 54,
        34 to 72,
        36 to 73,
        37 to 114,
        38 to 102,
        39 to 66,
        40 to 54,
        41 to 54,
        42 to 66,
        43 to 66,
        46 to 49,
        48 to 55,
        49 to 54,
        50 to 54
    )

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testFindOrientationOffset() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            /* Broken files are rejected by the segment length validation. */
            if (rejectedJpegIds.contains(index)) {

                assertFailsWith<ImageReadException> {
                    JpegOrientationOffsetFinder.findOrientationOffset(ByteArrayByteReader(bytes))
                }

                continue
            }

            val byteReader = ByteArrayByteReader(bytes)

            val orientationOffset = JpegOrientationOffsetFinder.findOrientationOffset(byteReader)

            assertEquals(
                expected = expectedMap[index],
                actual = orientationOffset
            )
        }
    }

    /**
     * Only the spec's SHORT entry with one value can swap losslessly.
     * A nonconformant LONG-typed Orientation entry must report no
     * offset, so the update falls back to the rewrite that rebuilds
     * the entry correctly, instead of patching one byte of a 4-byte
     * value.
     */
    @Test
    fun testLongTypedOrientationEntryFallsBackToRewrite() {

        /* SOI + APP1 with an EXIF whose Orientation is typed LONG. */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), // SOI
            0xFF.toByte(), 0xE1.toByte(), // APP1
            0x00, 0x22,                   // Segment length 34.
            0x45, 0x78, 0x69, 0x66, 0x00, 0x00, // "Exif\0\0"
            0x49, 0x49, 0x2A, 0x00,       // TIFF header, little-endian.
            8, 0, 0, 0,                   // IFD0 offset.
            1, 0,                         // Entry count.
            0x12, 0x01,                   // Orientation tag.
            4, 0,                         // Type LONG (nonconformant).
            1, 0, 0, 0,                   // Count 1.
            6, 0, 0, 0,                   // Value 6.
            0, 0, 0, 0,                   // No next IFD.
            0xFF.toByte(), 0xDA.toByte(), // SOS
            0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00,
            0x12, 0x34,                   // Entropy-coded data.
            0xFF.toByte(), 0xD9.toByte()  // EOI
        )

        assertEquals(
            expected = null,
            actual = JpegOrientationOffsetFinder.findOrientationOffset(
                ByteArrayByteReader(bytes)
            )
        )

        /* The update still applies the orientation through the rewrite. */
        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertEquals(
            expected = 6.toShort(),
            actual = Kim.readMetadata(updatedBytes)
                ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        )
    }

    private companion object {

        /* Media 45 and 47 contain invalid segment lengths. */
        private val rejectedJpegIds = setOf(45, 47)
    }
}
