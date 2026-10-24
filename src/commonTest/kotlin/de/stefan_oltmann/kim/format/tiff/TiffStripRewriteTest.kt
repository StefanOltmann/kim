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
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
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
            /* TIFF header, little endian */
            "49492a00" +
                /* IFD0 offset */
                "08000000" +

                /*
                 * IFD0 with the classic minimal strip image field set.
                 * Entry count.
                 */
                "0900" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* ImageLength = 4 */
                "0101" + "0400" + "01000000" + "04000000" +
                /* BitsPerSample = 8 */
                "0201" + "0300" + "01000000" + "08000000" +
                /* Compression = none */
                "0301" + "0300" + "01000000" + "01000000" +
                /* Photometric = black is zero */
                "0601" + "0300" + "01000000" + "01000000" +
                /* StripOffsets = 122 */
                "1101" + "0400" + "01000000" + "7a000000" +
                /* SamplesPerPixel = 1 */
                "1501" + "0300" + "01000000" + "01000000" +
                /* RowsPerStrip = 4 */
                "1601" + "0400" + "01000000" + "04000000" +
                /* StripByteCounts = 16 */
                "1701" + "0400" + "01000000" + "10000000" +
                /* next IFD */
                "00000000" +

                /* strip bytes, never captured */
                "00112233445566778899aabbccddeeff"
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
            /* TIFF header, little endian */
            "49492a00" +
                /* IFD0 offset */
                "08000000" +
                /* IFD0 entry count */
                "0300" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* SubIFDs -> 50 */
                "4a01" + "0400" + "01000000" + "32000000" +
                /* ImageLength = 4 */
                "0101" + "0400" + "01000000" + "04000000" +
                /* next IFD = 68 */
                "44000000" +
                /* sub-IFD entry count */
                "0100" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* sub-IFD next */
                "00000000" +
                /* IFD1 entry count */
                "0100" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* IFD1 next */
                "00000000"
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        assertFailsWith<ImageWriteException> {
            tiffContents.createOutputSet()
        }
    }

    /**
     * Regression test: when a partially failed read drops the SubIFDs
     * pointer field, the already-read sub-IFDs survive with their folded
     * directory types. The refusal must be structural and key on those
     * types - a pointer-only check would let the rewrite restructure the
     * file and displace the real chain directories.
     */
    @Test
    fun testCreateOutputSetRefusesSubIfdDirectoriesWithoutPointer() {

        /*
         * The directory state a partially failed read leaves behind: a
         * SubIFD-derived directory without any pointer field.
         */
        val subIfdDirectory = TiffDirectory(
            type = TiffConstants.EXIF_SUB_IFD0,
            entries = emptyList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN,
            isSubIfdChild = true
        )

        val tiffContents = TiffContents(
            header = TiffHeader(ByteOrder.LITTLE_ENDIAN, 42, 8),
            directories = listOf(subIfdDirectory),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )

        assertFailsWith<ImageWriteException> {
            tiffContents.createOutputSet()
        }
    }

    /**
     * Regression test: a spec-legal root chain IFD0 -> IFD1 -> IFD2 must
     * stay rewritable. The chain successor numbering shares its type ids
     * with the folded SubIFD number space (IFD2 == EXIF_SUB_IFD1), so a
     * type-keyed refusal rejected chain files that carry no SubIFDs
     * pointer at all. The refusal must key on the SubIFDs provenance of
     * the directory, not on the type id.
     */
    @Test
    fun testChainIfdWithoutSubIfdsStaysRewritable() {

        /*
         * IFD0 (ImageWidth, next -> IFD1), IFD1 (ImageWidth, next -> IFD2)
         * and IFD2 (ImageWidth, no successor).
         */
        val tiffBytes = convertHexStringToByteArray(
            /* TIFF header, little endian */
            "49492a00" +
                /* IFD0 offset */
                "08000000" +
                /* IFD0 entry count */
                "0100" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* next IFD = 26 */
                "1a000000" +
                /* IFD1 entry count */
                "0100" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* next IFD = 44 */
                "2c000000" +
                /* IFD2 entry count */
                "0100" +
                /* ImageWidth = 4 */
                "0001" + "0400" + "01000000" + "04000000" +
                /* next IFD */
                "00000000"
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(tiffBytes))

        assertEquals(3, tiffContents.directories.size)

        val outputSet = tiffContents.createOutputSet()

        /* The rewrite keeps every directory of the chain. */
        assertEquals(3, outputSet.getDirectories().size)
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
     * Regression test: a striped TIFF whose last strip is truncated cannot
     * be captured for a rewrite. The lenient read path degrades the strip
     * to NULL, so `createOutputSet` would emit a structurally valid TIFF
     * whose IFD references no image data at all. The read must refuse like
     * the tiled path does.
     *
     * The fixture is the corpus GeoTIFF, whose single strip occupies the
     * file end: its IFD declares StripOffsets 444 with StripByteCounts 14
     * (as ExifTool reports), so removing the trailing bytes leaves the
     * declared strip extending past the file end while every metadata
     * field stays intact.
     */
    @Test
    fun testTruncatedStripImageByteCaptureFailsTheRead() {

        val tiffBytes = KimTestData.getBytesOf(KimTestData.GEOTIFF_PIXEL_SCALING_INDEX)

        val truncatedBytes = tiffBytes.copyOfRange(0, tiffBytes.size - STRIP_TRUNCATION_BYTES)

        /* The metadata view is unaffected by the missing strip bytes. */
        assertNotNull(TiffReader.read(ByteArrayByteReader(truncatedBytes)))

        assertFailsWith<ImageReadException> {
            TiffReader.read(
                ByteArrayByteReader(truncatedBytes),
                readTiffImageBytes = true
            )
        }
    }

    /**
     * Regression test: a strip group of count 0 declares no image data
     * at all. Capturing it as an empty, non-null array made the rewrite
     * emit a degenerate StripByteCounts = 0 / StripOffsets pair instead
     * of dropping the counterpart fields like the no-capture path does.
     */
    @Test
    fun testZeroCountStripGroupCapturesNoImageBytes() {

        val tiffContents = TiffReader.read(
            ByteArrayByteReader(zeroCountStripTiffBytes()),
            readTiffImageBytes = true
        )

        val ifd0 = assertNotNull(
            tiffContents.directories.firstOrNull { directory ->
                directory.type == TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
            }
        )

        assertNull(ifd0.tiffImageBytes)

        /* The counterpart fields drop with the absent image bytes. */
        val outputSet = tiffContents.createOutputSet()

        assertNull(outputSet.findField(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS.tag))
        assertNull(outputSet.findField(TiffTag.TIFF_TAG_ROWS_PER_STRIP.tag))
    }

    /**
     * The minimal tiled TIFF fixture: IFD0 with the tile image field
     * set and tile bytes behind it.
     */
    private fun tiledTiffBytes(): ByteArray = convertHexStringToByteArray(
        /* TIFF header, little endian */
        "49492a00" +
            /* IFD0 offset */
            "08000000" +

            /*
             * IFD0 with the minimal tile image field set.
             * Entry count.
             */
            "0a00" +
            /* ImageWidth = 4 */
            "0001" + "0400" + "01000000" + "04000000" +
            /* ImageLength = 4 */
            "0101" + "0400" + "01000000" + "04000000" +
            /* BitsPerSample = 8 */
            "0201" + "0300" + "01000000" + "08000000" +
            /* Compression = none */
            "0301" + "0300" + "01000000" + "01000000" +
            /* Photometric = black is zero */
            "0601" + "0300" + "01000000" + "01000000" +
            /* TileWidth = 4 */
            "4201" + "0400" + "01000000" + "04000000" +
            /* TileLength = 4 */
            "4301" + "0400" + "01000000" + "04000000" +
            /* TileOffsets = 122 */
            "4401" + "0400" + "01000000" + "7a000000" +
            /* TileByteCounts = 16 */
            "4501" + "0400" + "01000000" + "10000000" +
            /* SamplesPerPixel = 1 */
            "5101" + "0300" + "01000000" + "01000000" +
            /* next IFD */
            "00000000" +

            /* tile bytes, never captured */
            "00112233445566778899aabbccddeeff"
    )

    /**
     * The minimal striped TIFF fixture whose strip group carries count 0:
     * StripOffsets and StripByteCounts exist but describe no strip.
     */
    private fun zeroCountStripTiffBytes(): ByteArray = convertHexStringToByteArray(
        /* TIFF header, little endian */
        "49492a00" +
            /* IFD0 offset */
            "08000000" +

            /* IFD0 with a count-0 strip group. Entry count. */
            "0800" +
            /* ImageWidth = 4 */
            "0001" + "0300" + "01000000" + "04000000" +
            /* ImageLength = 4 */
            "0101" + "0300" + "01000000" + "04000000" +
            /* BitsPerSample = 8 */
            "0201" + "0300" + "01000000" + "08000000" +
            /* Compression = none */
            "0301" + "0300" + "01000000" + "01000000" +
            /* Photometric = black is zero */
            "0601" + "0300" + "01000000" + "01000000" +
            /* StripOffsets, LONG, count 0 */
            "1101" + "0400" + "00000000" + "00000000" +
            /* SamplesPerPixel = 1 */
            "1501" + "0300" + "01000000" + "01000000" +
            /* StripByteCounts, LONG, count 0 */
            "1701" + "0400" + "00000000" + "00000000" +
            /* next IFD */
            "00000000"
    )

    companion object {

        /*
         * The corpus GeoTIFF's strip occupies the last 14 bytes, so
         * removing the trailing 8 leaves it short of its declared
         * StripByteCounts.
         */
        private const val STRIP_TRUNCATION_BYTES = 8
    }
}
