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
package de.stefan_oltmann.kim.format.png

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the compressed `zxIf` EXIF chunk against the committed fixture
 * `png_with_zxif_chunk.png`.
 *
 * The fixture is derived from the real test image media_53.png whose
 * eXIf chunk was written by ExifTool 13.59 and then converted to the
 * compressed layout. ExifTool reads the fixture and reports exactly the
 * same tags as the eXIf variant, so the expected values below are
 * ExifTool-verified.
 */
class PngChunkZxIfTest {

    private val fixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/png_with_zxif_chunk.png").readBytes()

    @Test
    fun testReadExifFromCompressedZxIfChunk() {

        val metadata = assertNotNull(Kim.readMetadata(fixtureBytes))

        assertEquals("Canon", metadata.findStringValue(TiffTag.TIFF_TAG_MAKE))
        assertEquals("Canon EOS R", metadata.findStringValue(TiffTag.TIFF_TAG_MODEL))
    }

    @Test
    fun testUpdateReplacesZxIfChunkWithExifChunk() {

        val updatedBytes = Kim.update(
            bytes = fixtureBytes,
            updates = setOf(MetadataUpdate.Orientation(TiffOrientation.UPSIDE_DOWN))
        )

        val chunkTypes = readChunkTypes(updatedBytes)

        /*
         * The compressed chunk must not survive next to the rewritten
         * EXIF, or the file would carry both versions.
         */
        assertFalse(chunkTypes.contains("zxIf"), "Chunk types: $chunkTypes")
        assertTrue(chunkTypes.contains("eXIf"), "Chunk types: $chunkTypes")

        /* The rewritten standard chunk carries the updated orientation. */
        val metadata = assertNotNull(Kim.readMetadata(updatedBytes))

        assertEquals(
            TiffOrientation.UPSIDE_DOWN.value,
            metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt()
        )
    }

    /**
     * A compressed chunk behind the image data was never seen by the
     * rewrite, so the update must fail instead of keeping it as a stale
     * duplicate next to the rewritten EXIF.
     */
    @Test
    fun testUpdateFailsOnZxIfChunkBehindImageData() {

        val relocated = relocateZxIfChunkBehindImageData(fixtureBytes)

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = relocated,
                updates = setOf(MetadataUpdate.Orientation(TiffOrientation.UPSIDE_DOWN))
            )
        }

        assertContains(exception.message ?: "", "behind the image")
    }

    /**
     * Rebuilds the fixture with the zxIf chunk placed behind the image
     * data. The chunk content itself stays byte-exact - only its
     * position in the file changes.
     */
    private fun relocateZxIfChunkBehindImageData(pngBytes: ByteArray): ByteArray {

        val chunkRanges = splitChunkRanges(pngBytes)

        val zxIfRange = chunkRanges.first { isChunkType(pngBytes, it, "zxIf") }

        val lastIdatRange = chunkRanges.last { isChunkType(pngBytes, it, "IDAT") }

        val reordered = mutableListOf<ByteArray>()

        reordered.add(pngBytes.copyOfRange(0, 8)) // PNG signature

        for (range in chunkRanges) {

            if (range == zxIfRange)
                continue

            reordered.add(pngBytes.copyOfRange(range.first, range.last))

            if (range == lastIdatRange)
                reordered.add(pngBytes.copyOfRange(zxIfRange.first, zxIfRange.last))
        }

        return reordered.reduce { acc, bytes -> acc + bytes }
    }

    @Suppress("MagicNumber")
    private data class ChunkRange(val first: Int, val last: Int)

    @Suppress("MagicNumber")
    private fun splitChunkRanges(pngBytes: ByteArray): List<ChunkRange> {

        val ranges = mutableListOf<ChunkRange>()

        var position = 8

        while (position + 8 <= pngBytes.size) {

            val length = readBigEndianInt(pngBytes, position)

            ranges.add(ChunkRange(position, position + 12 + length))

            position += 12 + length
        }

        return ranges
    }

    private fun isChunkType(pngBytes: ByteArray, range: ChunkRange, type: String): Boolean =

        pngBytes.decodeToString(range.first + 4, range.first + 8) == type

    private fun readChunkTypes(pngBytes: ByteArray): List<String> =

        splitChunkRanges(pngBytes).map { range ->
            pngBytes.decodeToString(range.first + 4, range.first + 8)
        }

    @Suppress("MagicNumber")
    private fun readBigEndianInt(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
}
