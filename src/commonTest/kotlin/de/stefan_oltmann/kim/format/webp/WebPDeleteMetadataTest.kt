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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunk
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
