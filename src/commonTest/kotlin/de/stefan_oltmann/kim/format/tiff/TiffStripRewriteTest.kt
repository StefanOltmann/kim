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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertNull

/**
 * The EXIF rewrite never captures strip based image data, so the strip
 * field group must be dropped as a whole: a surviving RowsPerStrip
 * would remain as a dangling reference without its offset and length
 * counterparts.
 */
class TiffStripRewriteTest {

    /**
     * Regression test: reading a strip based TIFF without
     * `readTiffImageBytes` (the production default on every EXIF read
     * path) and rewriting it dropped StripOffsets and StripByteCounts
     * but kept RowsPerStrip as a hollow reference.
     */
    @Test
    fun testRewriteDropsRowsPerStripWithUnresolvedStripData() {

        val tiffBytes = convertHexStringToByteArray(
            "49492a00" + // TIFF header, little endian
                "08000000" + // IFD0 offset

                /* IFD0 with the classic minimal strip image field set. */
                "0900" + // entry count
                "0001" + "0400" + "01000000" + "04000000" + // ImageWidth = 4
                "0101" + "0400" + "01000000" + "04000000" + // ImageLength = 4
                "0201" + "0300" + "01000000" + "08000000" + // BitsPerSample = 8
                "0301" + "0300" + "01000000" + "01000000" + // Compression = none
                "0601" + "0300" + "01000000" + "01000000" + // Photometric = black is zero
                "1101" + "0400" + "01000000" + "7a000000" + // StripOffsets = 122
                "1501" + "0300" + "01000000" + "01000000" + // SamplesPerPixel = 1
                "1601" + "0400" + "01000000" + "04000000" + // RowsPerStrip = 4
                "1701" + "0400" + "01000000" + "10000000" + // StripByteCounts = 16
                "00000000" + // next IFD

                "00112233445566778899aabbccddeeff" // strip bytes, never captured
        )

        /*
         * Arrange: read like every EXIF read path does, without the
         * strip image bytes.
         */
        val tiffContents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        val outputSet = tiffContents.createOutputSet()

        val byteWriter = ByteArrayByteWriter()

        /* Act: rewrite the file. */
        TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)

        val rewritten = TiffReader.read(ByteArrayByteReader(byteWriter.toByteArray()))

        val ifd0 = rewritten.directories.first()

        /* Assert: the strip group is gone as a whole. */
        assertNull(ifd0.findField(TiffTag.TIFF_TAG_STRIP_OFFSETS))
        assertNull(ifd0.findField(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS))
        assertNull(ifd0.findField(TiffTag.TIFF_TAG_ROWS_PER_STRIP))
    }
}
