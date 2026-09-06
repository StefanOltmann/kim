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
package de.stefan_oltmann.kim.format.webp

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8L
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebPWriterTest {

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

    /**
     * Tests that there is no loss if writing
     * the WebP chunks again without any change.
     */
    @Test
    fun testNoChange() {

        for (index in KimTestData.webpPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val byteWriter = ByteArrayByteWriter()

            WebPWriter.writeImage(
                byteReader = byteReader,
                byteWriter = byteWriter,
                exifBytes = null,
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
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateMetadata() {

        for (index in KimTestData.webpPhotoIds) {

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

            WebPWriter.writeImage(
                byteReader = ByteArrayByteReader(bytes),
                byteWriter,
                exifBytes,
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

            ModifiedBytesVerifier.verify(index, "webp", newBytes)
        }
    }

    /**
     * The WebP container spec expects EXIF to precede XMP. Writing new
     * EXIF while the file already carries XMP must insert the EXIF chunk
     * before the existing XMP chunk instead of appending it behind.
     */
    @Test
    fun testExifIsInsertedBeforeExistingXmp() {

        val bytes = KimTestData.getBytesOf(KimTestData.WEBP_TEST_IMAGE_INDEX)

        val oldMetadata = Kim.readMetadata(bytes)

        val exifBytes = assertNotNull(oldMetadata?.exifBytes)

        val byteWriter = ByteArrayByteWriter()

        /* The XMP parameter stays NULL, so the existing XMP chunk is kept. */
        WebPWriter.writeImage(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            xmp = null
        )

        val newBytes = byteWriter.toByteArray()

        val exifIndex = newBytes.indexOfBytes("EXIF".encodeToByteArray())

        val xmpIndex = newBytes.indexOfBytes("XMP ".encodeToByteArray())

        assertTrue(
            exifIndex in 0 until xmpIndex,
            "Expected EXIF before XMP, but was EXIF at $exifIndex and XMP at $xmpIndex"
        )
    }

    /**
     * Returns the first index of the needle bytes, or -1.
     */
    private fun ByteArray.indexOfBytes(needle: ByteArray): Int {

        for (offset in 0..size - needle.size)
            if (copyOfRange(offset, offset + needle.size).contentEquals(needle))
                return offset

        return -1
    }

    /**
     * The VP8X header created for a legacy VP8L bitstream must declare the alpha
     * flag that the bitstream carries, or decoders may drop the transparency.
     */
    @Test
    fun testLegacyVp8lWithAlphaGetsAlphaFlag() {

        /* A 1x1 VP8L bitstream with the alpha-is-used bit set. */
        val chunks = listOf(
            WebPChunkVP8L(byteArrayOf(0x2F, 0x00, 0x00, 0x00, 0x10))
        )

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )

        val vp8x = readVp8xChunk(byteWriter.toByteArray())

        assertTrue(vp8x.hasAlpha)
    }

    /**
     * A legacy VP8L file without transparency must not get an alpha flag.
     */
    @Test
    fun testLegacyVp8lWithoutAlphaGetsNoAlphaFlag() {

        /* A 1x1 VP8L bitstream without the alpha-is-used bit. */
        val chunks = listOf(
            WebPChunkVP8L(byteArrayOf(0x2F, 0x00, 0x00, 0x00, 0x00))
        )

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )

        val vp8x = readVp8xChunk(byteWriter.toByteArray())

        assertFalse(vp8x.hasAlpha)
    }

    /**
     * The RIFF size field must match the content length, which is computed
     * up front so the file is not buffered a second time in memory.
     */
    @Test
    fun testRiffSizeMatchesContentLength() {

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = listOf(WebPChunkVP8L(byteArrayOf(0x2F, 0x00, 0x00, 0x00, 0x10))),
            byteWriter = byteWriter,
            exifBytes = byteArrayOf(0x49, 0x49, 0x2A, 0),
            xmp = "<x:xmpmeta/>"
        )

        val bytes = byteWriter.toByteArray()

        val riffSize = bytes.toIntLittleEndian(4)

        assertEquals(bytes.size - 8, riffSize)
    }

    private fun readVp8xChunk(bytes: ByteArray): WebPChunkVP8X {

        val chunks = WebPImageParser.readChunks(
            byteReader = ByteArrayByteReader(bytes),
            stopAfterMetadataRead = false
        )

        return chunks.filterIsInstance<WebPChunkVP8X>().first()
    }

    @Suppress("MagicNumber")
    private fun ByteArray.toIntLittleEndian(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
            (this[offset + 1].toInt() and 0xFF shl 8) or
            (this[offset + 2].toInt() and 0xFF shl 16) or
            (this[offset + 3].toInt() and 0xFF shl 24)
}
