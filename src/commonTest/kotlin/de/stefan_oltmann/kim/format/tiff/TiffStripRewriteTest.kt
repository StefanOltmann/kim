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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertFailsWith
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

    /**
     * A TIFF with a SubIFDs pointer and a chain IFD1 cannot be
     * restructured faithfully: the writer never re-emits tag 0x014A,
     * and the sub-IFD occupies the IFD1 directory type, so
     * createOutputSet silently displaced the real chain IFD1 - its
     * thumbnail and fields were lost unheard of. The conversion must
     * refuse the file instead.
     */
    @Test
    fun testSubIfdRewriteFailsInsteadOfRestructuring() {

        /*
         * IFD0 (ImageWidth, SubIFDs -> 50, ImageLength, next -> 68),
         * a sub-IFD at 50 and the real chain IFD1 at 68.
         */
        val tiffBytes = convertHexStringToByteArray(
            "49492a00" + // TIFF header, little endian
                "08000000" + // IFD0 offset
                "0300" + // IFD0 entry count
                "0001" + "0400" + "01000000" + "04000000" + // ImageWidth = 4
                "4a01" + "0400" + "01000000" + "32000000" + // SubIFDs -> 50
                "0101" + "0400" + "01000000" + "04000000" + // ImageLength = 4
                "44000000" + // next IFD = 68
                "0100" + // sub-IFD entry count
                "0001" + "0400" + "01000000" + "04000000" + // ImageWidth = 4
                "00000000" + // sub-IFD next
                "0100" + // IFD1 entry count
                "0001" + "0400" + "01000000" + "04000000" + // ImageWidth = 4
                "00000000" // IFD1 next
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        assertFailsWith<ImageWriteException> {
            tiffContents.createOutputSet()
        }
    }

    /**
     * Regression test: a tiled TIFF read with
     * `readTiffImageBytes = true` cannot capture its image data, because
     * the tile capture was never implemented. The read must fail instead
     * of succeeding without the bytes - a rewrite via `createOutputSet`
     * would otherwise emit a structurally valid TIFF whose IFD
     * references no image data at all.
     */
    @Test
    fun testTiledTiffImageByteCaptureFailsTheRead() {

        assertFailsWith<ImageReadException> {
            TiffReader.read(
                ByteArrayByteReader(tiledTiffBytes()),
                readTiffImageBytes = true
            )
        }
    }

    /**
     * Regression test: the tile capture was never implemented, so the
     * writer can only drop the tile field group - the rewrite would emit
     * a structurally valid TIFF whose IFD references no image data at
     * all. Like the SubIFDs pointer, the conversion refuses the file
     * instead of corrupting it, no matter which read flag was used.
     */
    @Test
    fun testCreateOutputSetRefusesTiledDirectories() {

        val tiffBytes = tiledTiffBytes()

        val tiffContents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        assertFailsWith<ImageWriteException> {
            tiffContents.createOutputSet()
        }
    }

    /**
     * The minimal tiled TIFF fixture: IFD0 with the tile image field
     * set and tile bytes behind it.
     */
    private fun tiledTiffBytes(): ByteArray = convertHexStringToByteArray(
        "49492a00" + // TIFF header, little endian
            "08000000" + // IFD0 offset

            /* IFD0 with the minimal tile image field set. */
            "0a00" + // entry count
            "0001" + "0400" + "01000000" + "04000000" + // ImageWidth = 4
            "0101" + "0400" + "01000000" + "04000000" + // ImageLength = 4
            "0201" + "0300" + "01000000" + "08000000" + // BitsPerSample = 8
            "0301" + "0300" + "01000000" + "01000000" + // Compression = none
            "0601" + "0300" + "01000000" + "01000000" + // Photometric = black is zero
            "4201" + "0400" + "01000000" + "04000000" + // TileWidth = 4
            "4301" + "0400" + "01000000" + "04000000" + // TileLength = 4
            "4401" + "0400" + "01000000" + "7a000000" + // TileOffsets = 122
            "4501" + "0400" + "01000000" + "10000000" + // TileByteCounts = 16
            "5101" + "0300" + "01000000" + "01000000" + // SamplesPerPixel = 1
            "00000000" + // next IFD

            "00112233445566778899aabbccddeeff" // tile bytes, never captured
    )
}
