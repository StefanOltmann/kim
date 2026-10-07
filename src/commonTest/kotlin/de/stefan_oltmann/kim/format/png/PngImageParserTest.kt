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
package de.stefan_oltmann.kim.format.png

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PngImageParserTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testExtractPngText() {

        val index = KimTestData.PNG_TEST_IMAGE_INDEX

        val bytes = KimTestData.getHeaderBytesOf(index)

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(PngChunkType.IHDR, PngChunkType.TEXT, PngChunkType.ZTXT, PngChunkType.ITXT, PngChunkType.EXIF)
        )

        assertNotNull(chunks)
        assertEquals(4, chunks.size)

        val exifTxtChunk = chunks[1] as? PngChunkZtxt
        val iptcTxtChunk = chunks[2] as? PngChunkZtxt
        val xmpTxtChunk = chunks[3] as? PngChunkItxt

        assertNotNull(exifTxtChunk)
        assertEquals("Raw profile type exif", exifTxtChunk.keyword)

        assertNotNull(iptcTxtChunk)
        assertEquals("Raw profile type iptc", iptcTxtChunk.keyword)

        assertNotNull(xmpTxtChunk)
        assertEquals("XML:com.adobe.xmp", xmpTxtChunk.keyword)

        val expectedExif = KimTestData.getHeaderTextFile(index, "exif")
        val actualExif = exifTxtChunk.text

        assertEquals(expectedExif, actualExif, "EXIF is different.")

        val expectedIptc = KimTestData.getHeaderTextFile(index, "iptc")
        val actualIptc = iptcTxtChunk.text

        assertEquals(expectedIptc, actualIptc, "IPTC is different.")

        val expectedXmp = KimTestData.getHeaderTextFile(index, "xmp")
        val actualXmp = xmpTxtChunk.text

        assertEquals(expectedXmp, actualXmp, "XMP is different.")
    }

    /**
     * A text chunk that claims to be a "Raw profile type" profile but
     * carries no readable profile is metadata content that cannot be
     * read cleanly. Per the strict read policy the read fails instead
     * of silently dropping the chunk - a later update would rewrite
     * the file without it unheard of.
     */
    @Test
    fun testParseMetadataRejectsGarbageRawProfileChunks() {

        val ihdrChunk = PngImageParser.readChunks(
            ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)
            ),
            listOf(PngChunkType.IHDR)
        ).single()

        val garbageExifChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type exif\u000045786966zzffd9".encodeToByteArray(),
            crc = 0
        )

        val garbageIptcChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type iptc\u00003842494dzz".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, garbageExifChunk))
        }

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, garbageIptcChunk))
        }
    }

    /**
     * A "Raw profile type exif" chunk with valid hex that does not end
     * at the JPEG EOI marker is a truncated record. Per the strict read
     * policy the read fails instead of silently dropping the EXIF
     * content.
     */
    @Test
    fun testTruncatedExifTextChunkFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        val truncatedChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type exif\u00004578696600000002000a00ff".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    /**
     * A "Raw profile type iptc" chunk with an odd number of hex digits
     * cannot be converted to bytes completely - it is truncated. Per the
     * strict read policy the read fails instead of silently dropping the
     * IPTC content.
     */
    @Test
    fun testTruncatedIptcTextChunkFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        val truncatedChunk = PngChunkText(
            PngChunkType.TEXT,
            "Raw profile type iptc\u00003842494d1c021".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    /**
     * The iTXt chunk text is UTF-8 by PNG spec: a truncated multi-byte
     * sequence inside an XMP packet cannot be read cleanly, so the parse
     * fails instead of fabricating U+FFFD into the title.
     */
    @Test
    fun testTruncatedUtf8InXmpItxtFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        /*
         * The packet is structurally complete; only the title's final
         * character is a 2-byte UTF-8 lead byte without its continuation.
         */
        val packet = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about="" dc:title="H">
                </rdf:Description>
              </rdf:RDF>
            </x:xmpmeta>
        """.trimIndent().encodeToByteArray()

        val titleStart = packet.decodeToString().indexOf("dc:title=\"H>\"") + 11

        val iTxtPayload = "XML:com.adobe.xmp".encodeToByteArray() + ByteArray(5) +
            packet.copyOfRange(0, titleStart + 1) + byteArrayOf(0xC3.toByte()) +
            packet.copyOfRange(titleStart + 2, packet.size)
            packet.copyOfRange(0, titleStart + 1) + byteArrayOf(0xC3.toByte()) +
            packet.copyOfRange(titleStart + 2, packet.size)

        assertFailsWith<ImageReadException> {
            val truncatedChunk = PngChunkItxt(iTxtPayload, crc = 0)

            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    /**
     * An iTXt chunk with the XMP keyword whose packet is cut between
     * the opening and the closing element is truncated content. Per
     * the strict read policy the read fails instead of returning a
     * packet that only the update path will reject.
     */
    @Test
    fun testTruncatedXmpTextChunkFailsTheRead() {

        val ihdrChunk = readIhdrChunk()

        val truncatedChunk = PngChunkItxt(
            "XML:com.adobe.xmp\u0000\u0000\u0000\u0000\u0000<x:xmpmeta><rdf:RDF".encodeToByteArray(),
            crc = 0
        )

        assertFailsWith<ImageReadException> {
            PngImageParser.parseMetadataFromChunks(listOf(ihdrChunk, truncatedChunk))
        }
    }

    private fun readIhdrChunk() =
        PngImageParser.readChunks(
            ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)
            ),
            listOf(PngChunkType.IHDR)
        ).single()

    /**
     * A "Raw profile type exif" chunk whose hex digits are upper case is
     * just as complete as its lower case twin when it ends at the JPEG
     * EOI marker, so it must be read instead of being reported as
     * truncated.
     */
    @Test
    fun testUppercaseExifTextChunkIsRead() {

        val ihdrChunk = readIhdrChunk()

        val profileText =
            KimTestData.getHeaderTextFile(KimTestData.PNG_TEST_IMAGE_INDEX, "exif")

        val uppercaseExifChunk = PngChunkText(
            PngChunkType.TEXT,
            ("Raw profile type exif\u0000" + profileText.uppercase()).encodeToByteArray(),
            crc = 0
        )

        val metadata = PngImageParser.parseMetadataFromChunks(
            listOf(ihdrChunk, uppercaseExifChunk)
        )

        assertNotNull(metadata.exif, "The upper case EXIF was not read.")
    }

    /**
     * A PNG with two EXIF chunks is read like ExifTool reads it: the
     * first chunk is authoritative, later ones are ignored without
     * merging. The committed fixture carries a second eXIf chunk whose
     * Orientation differs (8 vs 1), so the assertion pins that the
     * first chunk wins. The fixture was ExifTool-verified to report
     * exactly that.
     */
    @Test
    fun testDuplicateExifChunksFirstChunkWins() {

        val bytes = Resource("de/stefan_oltmann/kim/testdata/png_with_duplicate_exif_chunks.png")
            .readBytes()

        val metadata = assertNotNull(Kim.readMetadata(bytes))

        assertEquals("Canon", metadata.findStringValue(TiffTag.TIFF_TAG_MAKE))
        assertEquals(1, metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
    }

    /**
     * An update consolidates duplicate EXIF chunks like ExifTool's
     * write does: all old EXIF chunks are deleted and a single rewritten
     * one is emitted.
     */
    @Test
    fun testUpdateConsolidatesDuplicateExifChunks() {

        val bytes = Resource("de/stefan_oltmann/kim/testdata/png_with_duplicate_exif_chunks.png")
            .readBytes()

        val updatedBytes = Kim.update(
            bytes = bytes,
            updates = setOf(MetadataUpdate.Orientation(TiffOrientation.UPSIDE_DOWN))
        )

        var eXIfCount = 0
        var zxIfCount = 0
        var position = 8

        while (position + 8 <= updatedBytes.size) {

            val length = readBigEndianInt(updatedBytes, position)

            val type = updatedBytes.decodeToString(position + 4, position + 8)

            when (type) {
                "eXIf" -> eXIfCount++
                "zxIf" -> zxIfCount++
            }

            position += 12 + length

            if (type == "IEND")
                break
        }

        assertEquals(1, eXIfCount, "The update must consolidate to a single eXIf chunk.")
        assertEquals(0, zxIfCount, "The update must not keep compressed duplicates.")

        val metadata = assertNotNull(Kim.readMetadata(updatedBytes))

        assertEquals(
            TiffOrientation.UPSIDE_DOWN.value,
            metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt()
        )
    }

    @Suppress("MagicNumber")
    private fun readBigEndianInt(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)

}

