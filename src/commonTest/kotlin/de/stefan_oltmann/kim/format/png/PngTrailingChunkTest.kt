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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests the trailing chunk walk of the rewrite: the stale filter works
 * from the keyword of a text chunk alone, so the payload behind the
 * keyword - in particular the compressed body - never influences the
 * outcome.
 */
class PngTrailingChunkTest {

    /**
     * A trailing zTXt chunk whose keyword is intact but whose compressed
     * body is corrupt is copied byte-exact: the rewrite filters trailing
     * chunks by their keyword alone and never decompresses the body, so
     * a corrupt body cannot fail the write of an unrelated comment.
     */
    @Test
    fun testUpdatePreservesTrailingZtxtWithCorruptBody() {

        val updatedBytes = Kim.update(
            bytes = createPngWithTrailingZtxtWithCorruptBody(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val ztxtChunks = findChunkData(updatedBytes, "zTXt")

        assertEquals(1, ztxtChunks.size)

        val payload = ztxtChunks.single().decodeToString()

        assertTrue(
            payload.startsWith("Comment\u0000\u0000"),
            "Unexpected payload: $payload"
        )
    }

    /**
     * Returns the raw data of all chunks of the given type.
     */
    private fun findChunkData(pngBytes: ByteArray, chunkTypeName: String): List<ByteArray> {

        val results = mutableListOf<ByteArray>()

        var offset = PngConstants.PNG_SIGNATURE.size

        while (offset + 12 <= pngBytes.size) {

            val length = (pngBytes[offset].toInt() and 0xFF) shl 24 or
                (pngBytes[offset + 1].toInt() and 0xFF) shl 16 or
                (pngBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (pngBytes[offset + 3].toInt() and 0xFF)

            val chunkType = pngBytes.copyOfRange(offset + 4, offset + 8).decodeToString()

            if (chunkType == chunkTypeName)
                results.add(pngBytes.copyOfRange(offset + 8, offset + 8 + length))

            if (chunkType == "IEND")
                break

            offset += 12 + length
        }

        return results
    }

    /**
     * The stale filter works from the keyword alone, so a trailing XMP
     * packet whose compressed body is corrupt fails the write exactly
     * like an intact one: the packet was never seen by the update, and
     * dropping it would silently destroy its content.
     */
    @Test
    fun testUpdateFailsOnTrailingXmpWithCorruptBody() {

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = createPngWithTrailingXmpWithCorruptBody(),
                update = MetadataUpdate.Title("New title")
            )
        }

        assertEquals(
            expected = "The update cannot merge metadata behind the image " +
                "data. The source file was not modified, but the output " +
                "written so far is incomplete and must be discarded.",
            actual = exception.message
        )
    }

    /**
     * Builds a minimal PNG with a zTXt chunk behind the image data whose
     * keyword is intact but whose compressed body is not zlib data.
     */
    private fun createPngWithTrailingZtxtWithCorruptBody(): ByteArray {

        /* Keyword, NUL terminator, compression method 0 - then garbage. */
        val ztxtPayload =
            "Comment\u0000\u0000".encodeToByteArray() +
                byteArrayOf(1, 2, 3)

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        /* 1x1 pixel, 8 bit RGBA, no interlace. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "zTXt", data = ztxtPayload)

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        return byteWriter.toByteArray()
    }

    /**
     * Builds a minimal PNG with an XMP iTXt chunk behind the image data
     * whose keyword is intact but whose compressed body is not zlib data.
     */
    private fun createPngWithTrailingXmpWithCorruptBody(): ByteArray {

        /*
         * Keyword, NUL, compression flag 1, compression method 0, empty
         * language tag, empty translated keyword - each null-terminated -
         * then a body that is not zlib data.
         */
        val xmpPayload =
            PngConstants.XMP_KEYWORD.encodeToByteArray() +
                byteArrayOf(0, 1, 0, 0, 0) +
                byteArrayOf(1, 2, 3)

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        /* 1x1 pixel, 8 bit RGBA, no interlace. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "iTXt", data = xmpPayload)

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        return byteWriter.toByteArray()
    }

    /**
     * Writes a single well-formed chunk with a correct CRC.
     */
    private fun writeChunk(
        byteWriter: ByteArrayByteWriter,
        typeName: String,
        data: ByteArray
    ) {

        val typeBytes = typeName.encodeToByteArray()

        byteWriter.writeInt(data.size, PngConstants.PNG_BYTE_ORDER)
        byteWriter.write(typeBytes)
        byteWriter.write(data)

        val crc =
            finishPartialCrc(continuePartialCrc(startPartialCrc(typeBytes), data)).toInt()

        byteWriter.writeInt(crc, PngConstants.PNG_BYTE_ORDER)
    }
}
