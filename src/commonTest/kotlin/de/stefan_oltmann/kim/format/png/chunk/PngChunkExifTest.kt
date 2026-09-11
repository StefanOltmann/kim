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
package de.stefan_oltmann.kim.format.png.chunk

import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests reading the EXIF chunk of PNG files.
 *
 * The chunk carries raw TIFF bytes per the PNG specification, but some
 * writers add the JPEG style "Exif\0\0" header in front of it. ExifTool
 * warns "Improper Exif00 header in EXIF chunk" for those and still reads
 * them.
 */
class PngChunkExifTest {

    private val tiffBytes: ByteArray
        get() = KimTestData.getHeaderExifBytesOf(52)

    @Test
    fun testReadsExifWithoutHeader() {

        val chunk = PngChunkExif(tiffBytes, crc = 0)

        assertNotNull(chunk.tiffContents)
    }

    @Test
    fun testReadsExifWithImproperHeader() {

        val chunk = PngChunkExif(
            "Exif\u0000\u0000".encodeToByteArray() + tiffBytes,
            crc = 0
        )

        val chunkWithoutHeader = PngChunkExif(tiffBytes, crc = 0)

        /*
         * The toString covers the whole parsed structure - TiffContents
         * equality compares the embedded thumbnail bytes by reference.
         */
        assertEquals(
            expected = chunkWithoutHeader.tiffContents.toString(),
            actual = chunk.tiffContents.toString()
        )

        assertEquals(
            expected = chunkWithoutHeader.tiffContents.directories.size,
            actual = chunk.tiffContents.directories.size
        )
    }
}
