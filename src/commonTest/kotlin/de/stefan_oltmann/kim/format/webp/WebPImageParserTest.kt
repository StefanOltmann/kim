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

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.ImageSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebPImageParserTest {

    /**
     * Legacy WebP files (VP8/VP8L) must stop reading after
     * the image chunk when only metadata is needed.
     */
    @Test
    fun testReadChunksStopsAfterVp8Chunk() {

        /* A valid minimal VP8 key frame header. */
        val vp8Payload = byteArrayOf(
            0x10, 0x00, 0x00, 0x9D.toByte(), 0x01, 0x2A,
            0x64, 0x00, // width 100
            0x64, 0x00 // height 100
        )

        val vp8Chunk = "VP8 ".encodeToByteArray() +
            intToBytesLE(vp8Payload.size) +
            vp8Payload

        /* Junk after the image chunk, inside the declared RIFF length. */
        val junk = ByteArray(1000)

        val file = "RIFF".encodeToByteArray() +
            intToBytesLE(vp8Chunk.size + junk.size + "WEBP".length) +
            "WEBP".encodeToByteArray() +
            vp8Chunk +
            junk

        val countingReader = CountingByteReader(ByteArrayByteReader(file))

        val chunks = WebPImageParser.readChunks(
            byteReader = countingReader,
            stopAfterMetadataRead = true
        )

        assertEquals(1, chunks.size)
        assertEquals(WebPChunkType.VP8, chunks.single().type)

        /* The trailing junk must not be read. */
        assertTrue(countingReader.bytesRead < file.size)
    }

    /**
     * In extended files (VP8X) the metadata chunks follow
     * the image chunk and must not be skipped.
     */
    @Test
    fun testReadChunksReadsMetadataAfterVp8ChunkInExtendedFile() {

        val vp8xPayload = WebPChunkVP8X.createBytes(
            hasIcc = false,
            hasAlpha = false,
            hasExif = true,
            hasXmp = true,
            hasAnimation = false,
            imageSize = ImageSize(100, 100)
        )

        val vp8xChunk = "VP8X".encodeToByteArray() +
            intToBytesLE(vp8xPayload.size) +
            vp8xPayload

        val vp8Payload = byteArrayOf(
            0x10, 0x00, 0x00, 0x9D.toByte(), 0x01, 0x2A,
            0x64, 0x00, // width 100
            0x64, 0x00 // height 100
        )

        val vp8Chunk = "VP8 ".encodeToByteArray() +
            intToBytesLE(vp8Payload.size) +
            vp8Payload

        /* A minimal valid TIFF. */
        val exifPayload = convertHexStringToByteArray(
            "49492a0008000000" + "0100" + "01000100010000002a000000" + "00000000"
        )

        val exifChunk = "EXIF".encodeToByteArray() +
            intToBytesLE(exifPayload.size) +
            exifPayload

        val file = "RIFF".encodeToByteArray() +
            intToBytesLE(vp8xChunk.size + vp8Chunk.size + exifChunk.size + "WEBP".length) +
            "WEBP".encodeToByteArray() +
            vp8xChunk +
            vp8Chunk +
            exifChunk

        val chunks = WebPImageParser.readChunks(
            byteReader = ByteArrayByteReader(file),
            stopAfterMetadataRead = true
        )

        assertEquals(3, chunks.size)
        assertEquals(WebPChunkType.EXIF, chunks.last().type)
    }

    private fun intToBytesLE(value: Int): ByteArray = byteArrayOf(
        value.toByte(),
        (value shr 8).toByte(),
        (value shr 16).toByte(),
        (value shr 24).toByte()
    )

    private class CountingByteReader(
        private val delegate: ByteReader
    ) : ByteReader {

        var bytesRead: Long = 0
            private set

        override val contentLength: Long = delegate.contentLength

        override fun readByte(): Byte? {

            val byte = delegate.readByte()

            if (byte != null)
                bytesRead++

            return byte
        }

        override fun readBytes(count: Int): ByteArray {

            val bytes = delegate.readBytes(count)

            bytesRead += bytes.size

            return bytes
        }

        override fun close() = delegate.close()
    }
}
