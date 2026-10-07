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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcBlock
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PngWriterTest {

    /* language=XML */
    private val expectedXmp = """
        <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                    xmlns:exif="http://ns.adobe.com/exif/1.0/"
                  exif:DateTimeOriginal="2020-10-05T13:37:42"
                  xmp:Rating="3"/>
              </rdf:RDF>
            </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    /**
     * The iTXt payload is keyword, NUL, compression flag,
     * compression method, empty language tag, empty translated keyword
     * and the text - the layout every common writer emits (verified
     * against the ExifTool reference dumps of media_51 to media_53).
     * Repeating the keyword in the translated-keyword field made every
     * chunk written here carry a spurious translated keyword.
     */
    @Test
    fun testWrittenXmpItxtUsesEmptyTranslatedKeyword() {

        val chunkBytes = firstChunkOf(
            bytes = writeImageWithXmp(),
            chunkType = "iTXt"
        )

        val keyword = "XML:com.adobe.xmp".encodeToByteArray()

        /* The keyword must appear exactly once, at the start. */
        assertContentEquals(keyword, chunkBytes.copyOfRange(0, keyword.size))
        assertEquals(
            1,
            chunkBytes.toList().windowed(keyword.size).count { it.toByteArray().contentEquals(keyword) }
        )

        /*
         * The keyword terminator plus the two flags and the two empty
         * string terminators collapse into five NUL bytes.
         */
        var nulCount = 0
        var position = keyword.size

        while (chunkBytes[position] == 0.toByte()) {
            nulCount++
            position++
        }

        assertEquals(5, nulCount)
        assertEquals('<'.code.toByte(), chunkBytes[position])
    }

    /**
     * Reads a big endian int at the given offset.
     */
    private fun readBigEndianInt(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)

    /**
     * Writes a minimal PNG carrying XMP through the writer.
     */
    private fun writeImageWithXmp(): ByteArray {

        val ihdrChunk = PngImageParser.readChunks(
            ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)
            ),
            listOf(PngChunkType.IHDR)
        ).single()

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = listOf(ihdrChunk),
            byteWriter = byteWriter,
            exifBytes = null,
            iptcBytes = null,
            xmp = "<x:xmpmeta><rdf:RDF/></x:xmpmeta>"
        )

        return byteWriter.toByteArray()
    }

    /**
     * Returns the payload of the first chunk with the given type.
     */
    private fun firstChunkOf(bytes: ByteArray, chunkType: String): ByteArray {

        var position = PNG_SIGNATURE_LENGTH

        while (position + PNG_CHUNK_HEADER_LENGTH <= bytes.size) {

            val length = readBigEndianInt(bytes, position)

            val type = bytes.decodeToString(position + 4, position + 8)

            if (type == chunkType)
                return bytes.copyOfRange(position + 8, position + 8 + length)

            position += PNG_CHUNK_HEADER_LENGTH + length + PNG_CRC_LENGTH
        }

        error("No $chunkType chunk found.")
    }

    /**
     * Tests that there is no loss if writing
     * the PNG chunks again without any change.
     *
     * This basically tests if write order is
     * kept and CRC calculation is correct.
     */
    @Test
    fun testNoChange() {

        for (index in KimTestData.pngPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val byteWriter = ByteArrayByteWriter()

            PngWriter.writeImage(
                byteReader = byteReader,
                byteWriter = byteWriter,
                exifBytes = null,
                iptcBytes = null,
                xmp = null
            )

            val newBytes = byteWriter.toByteArray()

            assertContentEquals(
                expected = bytes,
                actual = newBytes
            )
        }
    }

    /**
     * The new metadata chunks are inserted behind the mandatory IHDR
     * chunk. A chunk list without an IHDR would complete the write
     * without emitting the requested metadata - a silent no-op of the
     * caller's request - so the write is refused instead.
     */
    @Test
    fun testWriteImageRejectsMissingIhdr() {

        val bytes = KimTestData.getBytesOf(KimTestData.pngPhotoIds.first())

        val chunksWithoutIhdr = PngImageParser.readChunks(
            byteReader = ByteArrayByteReader(bytes),
            chunkTypeFilter = null
        ).filterNot { it.type == PngChunkType.IHDR }

        val exception = assertFailsWith<ImageWriteException> {
            PngWriter.writeImage(
                chunks = chunksWithoutIhdr,
                byteWriter = ByteArrayByteWriter(),
                exifBytes = null,
                iptcBytes = null,
                xmp = null
            )
        }

        assertTrue(exception.message?.contains("IHDR") == true)
    }

    /**
     * A chunk list with two IHDR chunks would emit the new metadata
     * twice. The duplicate is rejected like the missing header.
     */
    @Test
    fun testWriteImageRejectsDuplicateIhdr() {

        val bytes = KimTestData.getBytesOf(KimTestData.pngPhotoIds.first())

        val chunks = PngImageParser.readChunks(
            byteReader = ByteArrayByteReader(bytes),
            chunkTypeFilter = null
        )

        val duplicated = chunks.take(1) + chunks

        assertFailsWith<ImageWriteException> {
            PngWriter.writeImage(
                chunks = duplicated,
                byteWriter = ByteArrayByteWriter(),
                exifBytes = null,
                iptcBytes = null,
                xmp = null
            )
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testUpdateMetadata() {

        for (index in KimTestData.pngPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val oldMetadata = Kim.readMetadata(bytes)

            assertNotNull(oldMetadata)

            val oldXmp = oldMetadata.xmp

            assertNotEquals(expectedXmp, oldXmp)

            val tiffOutputSet = oldMetadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifDirectory = tiffOutputSet.getOrCreateExifDirectory()

            /*
             * Note: We write a different date to EXIF than to XMP
             * to see which viewer gives priority to which field.
             */
            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2023:08:01 08:00:00")

            val exifBytesWriter = ByteArrayByteWriter()

            val writer = TiffWriter(byteOrder = tiffOutputSet.byteOrder)

            writer.write(exifBytesWriter, tiffOutputSet)

            val exifBytes: ByteArray = exifBytesWriter.toByteArray()

            val byteWriter = ByteArrayByteWriter()

            val newIptcBlock = IptcBlock(
                blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
                blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
                blockData = IptcWriter.writeIptcBlockData(
                    listOf(
                        IptcRecord(IptcTypes.KEYWORDS, "Äußerst schön")
                    )
                )
            )

            val iptcBytes = IptcWriter.writeIptcBlocks(
                blocks = listOf(newIptcBlock),
                includeApp13Identifier = false
            )

            PngWriter.writeImage(
                byteReader = ByteArrayByteReader(bytes),
                byteWriter,
                exifBytes,
                iptcBytes,
                expectedXmp
            )

            val newBytes = byteWriter.toByteArray()

            val actualMetadata = Kim.readMetadata(newBytes)

            assertNotNull(actualMetadata)
            assertNotNull(actualMetadata.exif)
            assertNotNull(actualMetadata.xmp)

            assertEquals(
                expected = expectedXmp,
                actual = actualMetadata.xmp
            )

            ModifiedBytesVerifier.verify(index, "png", newBytes)
        }
    }

    private companion object {

        /* PNG signature, the 8-byte chunk header and the CRC field. */
        private const val PNG_SIGNATURE_LENGTH: Int = 8
        private const val PNG_CHUNK_HEADER_LENGTH: Int = 8
        private const val PNG_CRC_LENGTH: Int = 4
    }
}
