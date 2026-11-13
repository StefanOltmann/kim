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
package de.stefan_oltmann.kim.format.webp

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunk
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkExif
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests the deletion of WebP metadata on hostile heads: the operation
 * discards the metadata chunks, so their payloads do not need to be
 * parseable - a malformed XMP chunk or a discarded ICC profile is
 * dropped like a valid one. The EXIF chunk is the one exception: the
 * rotation has to be read from it, so a corrupt payload fails the
 * operation when the orientation is preserved.
 */
class WebPDeleteMetadataTest {

    /**
     * A WebP whose EXIF chunk payload is not TIFF data, built by
     * injecting the hostile chunk into the committed original.
     */
    private fun webPWithMalformedExif(): ByteArray =
        webPWithInjectedChunk(WebPChunkType.EXIF, byteArrayOf(1, 2, 3))

    /**
     * A WebP whose XMP chunk payload is not valid UTF-8, built by
     * injecting the hostile chunk into the committed original.
     */
    private fun webPWithMalformedXmp(): ByteArray =
        webPWithInjectedChunk(WebPChunkType.XMP, byteArrayOf(0xFF.toByte(), 0xFE.toByte()))

    /**
     * A WebP whose ICCP chunk payload is not a parseable ICC profile,
     * built by injecting the hostile chunk into the committed original.
     */
    private fun webPWithCorruptIccp(): ByteArray =
        webPWithInjectedChunk(WebPChunkType.ICCP, byteArrayOf(1, 2, 3))

    /**
     * Builds the committed original WebP with one hostile chunk of the
     * given type injected behind the image header.
     */
    private fun webPWithInjectedChunk(
        chunkType: WebPChunkType,
        payload: ByteArray
    ): ByteArray {

        val originalChunks = WebPImageParser.readChunks(
            ByteArrayByteReader(
                Resource("de/stefan_oltmann/kim/updates_webp/original.webp").readBytes()
            ),
            stopAfterMetadataRead = false
        )

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = originalChunks + WebPChunk(chunkType, payload),
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )

        return byteWriter.toByteArray()
    }

    /**
     * Two EXIF chunks produce exactly one minimal EXIF chunk, carrying
     * the orientation of the first one - the read path is first-wins,
     * and the output can never hold double Exif.
     */
    @Test
    fun testWebPWithTwoExifChunksWritesExactlyOneMinimalExif() {

        /*
         * The original carries its own EXIF chunk without an orientation
         * tag - it is filtered out, so the two controlled sources remain.
         */
        val originalChunks = WebPImageParser.readChunks(
            ByteArrayByteReader(
                Resource("de/stefan_oltmann/kim/updates_webp/original.webp").readBytes()
            ),
            stopAfterMetadataRead = false
        ).filterNot { it.type == WebPChunkType.EXIF }

        val fixture = webPWithAppendedChunks(
            originalChunks +
                WebPChunkExif(exifBytesWith(6)) +
                WebPChunkExif(exifBytesWith(5))
        )

        /* Precondition: the fixture really carries both chunks. */
        assertEquals(2, storedChunkTypes(fixture).count { it == "EXIF" })

        val newBytes = Kim.deleteMetadata(fixture)

        val chunkTypes = storedChunkTypes(newBytes)

        assertEquals(1, chunkTypes.count { it == "EXIF" })

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
    }

    /**
     * Builds a WebP whose chunk list is the committed original plus the
     * given raw chunks, appended behind the image data. The RIFF wrapper
     * is assembled by hand, because the writer collapses EXIF chunks to
     * the one carried in exifBytes - which is exactly the duplication
     * under test.
     */
    private fun webPWithAppendedChunks(chunks: List<WebPChunk>): ByteArray {

        val chunkWriter = ByteArrayByteWriter()

        for (chunk in chunks) {

            chunkWriter.write(chunk.type.bytes)

            chunkWriter.writeInt(chunk.bytes.size, ByteOrder.LITTLE_ENDIAN)

            chunkWriter.write(chunk.bytes)

            if (chunk.bytes.size % 2 != 0)
                chunkWriter.write(0)
        }

        val chunkBlock = chunkWriter.toByteArray()

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write("RIFF".encodeToByteArray())

        byteWriter.writeInt(4 + chunkBlock.size, ByteOrder.LITTLE_ENDIAN)

        byteWriter.write("WEBP".encodeToByteArray())
        byteWriter.write(chunkBlock)

        return byteWriter.toByteArray()
    }

    /**
     * Builds EXIF bytes carrying a Make field and the given orientation,
     * so the test can prove that everything besides the orientation is
     * gone afterwards.
     */
    private fun exifBytesWith(orientation: Short): ByteArray {

        val outputSet = TiffOutputSet()

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_MAKE, "Canon")

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_ORIENTATION, orientation)

        return outputSet.toTiffBytes()
    }

    /**
     * The WebP chunk type names of the file in order. The hostile chunks
     * under test are gone from the deleted output, so the clean file
     * parses without trouble.
     */
    private fun storedChunkTypes(webPBytes: ByteArray): List<String> =

        WebPImageParser.readChunks(
            ByteArrayByteReader(webPBytes),
            stopAfterMetadataRead = false
        ).map { it.type.name }

    /**
     * A malformed EXIF chunk fails the deletion: the rotation has to be
     * read from the EXIF content before it is removed, so the payload
     * must be parseable even though the chunk itself is discarded.
     */
    @Test
    fun testDeleteMetadataFailsOnMalformedExifWhenDiscarded() {

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(webPWithMalformedExif())
        }
    }

    /**
     * A malformed XMP chunk is dropped like a valid one: the deletion
     * does not parse the content it discards.
     */
    @Test
    fun testDeleteMetadataSkipsMalformedXmpWhenDiscarded() {

        val newBytes = Kim.deleteMetadata(webPWithMalformedXmp())

        val chunkTypes = storedChunkTypes(newBytes)

        assertEquals(0, chunkTypes.count { it == "XMP" })

        /* The VP8X header chunk carries the image size and must survive. */
        assertTrue(chunkTypes.contains("VP8X"), "The image data must survive.")
    }

    /**
     * A corrupt ICCP chunk is dropped like a valid one when the profile
     * goes with the rest of the metadata: the deletion does not parse
     * the content it discards.
     */
    @Test
    fun testDeleteMetadataSkipsCorruptIccpWhenDiscarded() {

        val newBytes = Kim.deleteMetadata(
            bytes = webPWithCorruptIccp(),
            keepIccProfile = false
        )

        val chunkTypes = storedChunkTypes(newBytes)

        assertEquals(0, chunkTypes.count { it == "ICCP" })

        /* The VP8X header chunk carries the image size and must survive. */
        assertTrue(chunkTypes.contains("VP8X"), "The image data must survive.")
    }

    /**
     * A corrupt ICCP chunk that the deletion keeps stays in the file
     * byte-exact: the deletion never interprets the ICC content, so the
     * corruption is preserved instead of hidden, and the operation
     * succeeds.
     */
    @Test
    fun testDeleteMetadataKeepsCorruptIccpByteExact() {

        val corruptPayload = byteArrayOf(1, 2, 3)

        val newBytes = Kim.deleteMetadata(webPWithInjectedChunk(WebPChunkType.ICCP, corruptPayload))

        val iccpPayloads = WebPImageParser.readChunks(
            ByteArrayByteReader(newBytes),
            stopAfterMetadataRead = false
        ).filter { it.type == WebPChunkType.ICCP }.map { it.bytes.toList() }

        assertTrue(
            corruptPayload.toList() in iccpPayloads,
            "The injected corrupt ICCP chunk must survive the deletion."
        )
    }
}
