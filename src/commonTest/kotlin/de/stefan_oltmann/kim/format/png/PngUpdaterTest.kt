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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PngUpdaterTest : AbstractUpdaterTest("png") {

    private val originalBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/updates_png/original.png").readBytes()

    /**
     * Regression test: a chunk whose data does not match its stored CRC is
     * corrupt and must be rejected on read. Ignoring the mismatch would let
     * a rewrite emit the corrupted chunk with a fresh, valid CRC, hiding
     * the corruption from all further tools.
     */
    @Test
    fun testReadMetadataRejectsChunkWithCorruptData() {

        val corrupted = originalBytes.copyOf()

        /* Flip one data byte of the IHDR chunk, which follows the signature. */
        val dataOffset = PNG_SIGNATURE_LENGTH + CHUNK_HEADER_LENGTH

        corrupted[dataOffset] = (corrupted[dataOffset].toInt() xor 0x55).toByte()

        val exception = assertFailsWith<ImageReadException> {
            Kim.readMetadata(corrupted)
        }

        assertTrue(
            exception.message?.contains("CRC mismatch") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Regression test: a flipped bit in the stored CRC field itself must be
     * detected just like corrupted chunk data.
     */
    @Test
    fun testReadMetadataRejectsChunkWithCorruptCrcField() {

        val corrupted = originalBytes.copyOf()

        /* Flip one byte of the stored CRC of the IHDR chunk. */
        val crcOffset = PNG_SIGNATURE_LENGTH + CHUNK_HEADER_LENGTH + IHDR_DATA_LENGTH

        corrupted[crcOffset] = (corrupted[crcOffset].toInt() xor 0x55).toByte()

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(corrupted)
        }
    }

    /**
     * Verifies that deleting the metadata removes the EXIF and text chunks,
     * but keeps the iCCP chunk that affects how the image is displayed.
     */
    @Test
    fun testDeleteMetadataKeepsIccChunk() {

        val newBytes = Kim.deleteMetadata(originalBytes)

        val chunkTypes = chunkTypes(newBytes)

        /* The ICC profile affects the display and must be kept. */
        assertTrue("iCCP" in chunkTypes)

        /* The EXIF chunk must be removed. */
        assertFalse("eXIf" in chunkTypes)

        /* The text chunks carry XMP, IPTC and comments. */
        assertFalse("tEXt" in chunkTypes)
        assertFalse("zTXt" in chunkTypes)
        assertFalse("iTXt" in chunkTypes)

        /* The modification time is metadata, too. */
        assertFalse("tIME" in chunkTypes)
    }

    /**
     * Metadata chunks placed behind the image data must be removed by
     * deleteMetadata as well, whatever position the encoder chose.
     */
    @Test
    fun testDeleteMetadataRemovesTrailingMetadataChunks() {

        val deletedBytes = Kim.deleteMetadata(createPngWithTrailingMetadata())

        val chunkTypes = chunkTypes(deletedBytes)

        assertFalse("eXIf" in chunkTypes)
        assertFalse("tEXt" in chunkTypes)

        /* The image data itself must survive. */
        assertTrue("IDAT" in chunkTypes)
        assertTrue("IEND" in chunkTypes)
    }

    /**
     * An Exif chunk behind the image data must not survive an update,
     * otherwise the file ends up with two conflicting Exif chunks.
     */
    @Test
    fun testUpdateDoesNotDuplicateTrailingExifChunk() {

        val updatedBytes = Kim.update(
            bytes = createPngWithTrailingMetadata(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertEquals(1, chunkTypeCount(updatedBytes, "eXIf"))
    }

    /**
     * Regression test: a user comment behind the image data is unrelated
     * to any metadata change and must survive an update. The previous
     * blanket filter deleted every text chunk behind IDAT.
     */
    @Test
    fun testUpdatePreservesUnrelatedTrailingComment() {

        val updatedBytes = Kim.update(
            bytes = createPngWithTrailingMetadata(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val comments = findChunkData(updatedBytes, "tEXt")

        assertEquals(1, comments.size)
        assertTrue(comments.single().decodeToString().contains(STALE_TEXT))

        /* The modification time is unrelated metadata as well. */
        assertTrue(chunkTypes(updatedBytes).contains("tIME"))
    }

    /**
     * A trailing XMP chunk of which the update wrote a fresh copy must be
     * removed, so no stale duplicate survives.
     */
    @Test
    fun testUpdateRemovesStaleTrailingXmp() {

        val updatedBytes = Kim.update(
            bytes = createPngWithTrailingXmp(),
            update = MetadataUpdate.Title("New title")
        )

        val xmpChunks = findChunkData(updatedBytes, "iTXt")

        assertEquals(1, xmpChunks.size)
        assertFalse(xmpChunks.single().decodeToString().contains(STALE_XMP))
        assertTrue(Kim.readMetadata(updatedBytes)?.xmp?.contains("New title") == true)
    }

    /**
     * Regression test: a PNG without image data cannot be updated and must
     * be rejected with a clear message before anything was written,
     * instead of an opaque failure after the header was already streamed.
     */
    @Test
    fun testUpdateMetadataOnlyPngIsRejectedCleanly() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = byteWriter.toByteArray(),
                update = MetadataUpdate.Title("New title")
            )
        }

        assertTrue(
            exception.message?.contains("no image data") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Returns the types of all chunks of the given PNG bytes.
     */
    private fun chunkTypes(pngBytes: ByteArray): Set<String> {

        val chunkTypes = mutableSetOf<String>()

        var offset = PngConstants.PNG_SIGNATURE.size

        while (offset + 12 <= pngBytes.size) {

            val length = (pngBytes[offset].toInt() and 0xFF) shl 24 or
                (pngBytes[offset + 1].toInt() and 0xFF) shl 16 or
                (pngBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (pngBytes[offset + 3].toInt() and 0xFF)

            val chunkType = pngBytes.copyOfRange(offset + 4, offset + 8).decodeToString()

            chunkTypes.add(chunkType)

            if (chunkType == "IEND")
                break

            offset += 12 + length
        }

        return chunkTypes
    }

    /**
     * Returns how often a chunk of the given type occurs, so duplicated
     * chunks behind the image data can be detected.
     */
    private fun chunkTypeCount(pngBytes: ByteArray, chunkTypeName: String): Int {

        var count = 0

        var offset = PngConstants.PNG_SIGNATURE.size

        while (offset + 12 <= pngBytes.size) {

            val length = (pngBytes[offset].toInt() and 0xFF) shl 24 or
                (pngBytes[offset + 1].toInt() and 0xFF) shl 16 or
                (pngBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (pngBytes[offset + 3].toInt() and 0xFF)

            val chunkType = pngBytes.copyOfRange(offset + 4, offset + 8).decodeToString()

            if (chunkType == chunkTypeName)
                count++

            if (chunkType == "IEND")
                break

            offset += 12 + length
        }

        return count
    }

    /**
     * Builds a minimal PNG whose Exif and text chunks sit behind the image
     * data, a legal ancillary placement that some encoders use.
     */
    private fun createPngWithTrailingMetadata(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        /* 1x1 pixel, 8 bit RGBA, no interlace. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IDAT",
            data = byteArrayOf(1, 2, 3, 4)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "eXIf",
            data = STALE_EXIF_BYTES
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "tEXt",
            data = "Comment\u0000$STALE_TEXT".encodeToByteArray()
        )

        /* Year 2024, May 12th, 18:04:00. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "tIME",
            data = byteArrayOf(0x07, 0xE8.toByte(), 5, 12, 18, 4, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IEND",
            data = byteArrayOf()
        )

        return byteWriter.toByteArray()
    }

    /**
     * Builds a minimal PNG with an XMP iTXt chunk behind the image data.
     */
    private fun createPngWithTrailingXmp(): ByteArray {

        /* Keyword, null, no compression, no method, empty language tag,
         * empty translated keyword - each null-terminated. */
        val xmpPayload =
            PngConstants.XMP_KEYWORD.encodeToByteArray() +
                byteArrayOf(0, 0, 0, 0, 0) +
                STALE_XMP.encodeToByteArray()

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

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

    private companion object {

        /* A minimal big-endian TIFF header, so the payload looks like EXIF. */
        val STALE_EXIF_BYTES: ByteArray =
            byteArrayOf(0x49, 0x49, 0x2A, 0, 8, 0, 0, 0)

        const val STALE_TEXT: String = "stale text"

        const val STALE_XMP: String = "<x:xmpmeta>stale</x:xmpmeta>"

        /* The 8-byte PNG signature at the start of every file. */
        private const val PNG_SIGNATURE_LENGTH: Int = 8

        /* The 4-byte length field plus the 4-byte type field. */
        private const val CHUNK_HEADER_LENGTH: Int = 8

        /* The IHDR chunk always carries exactly 13 data bytes. */
        private const val IHDR_DATA_LENGTH: Int = 13
    }
}
