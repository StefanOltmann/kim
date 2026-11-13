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
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests the deletion of PNG metadata on hostile heads: the operation
 * discards the metadata chunks, so their payloads do not need to be
 * parseable - a malformed or duplicate chunk is dropped like a valid
 * one, and a discarded profile is never decompressed. Only a chunk the
 * deletion keeps is parsed like the read does.
 */
class PngDeleteMetadataTest {

    /**
     * The payload of a malformed iCCP chunk: keyword, NUL terminator,
     * compression method 0 - then bytes that are not zlib data.
     */
    private val malformedIccpPayload: ByteArray =
        "test\u0000\u0000".encodeToByteArray() + byteArrayOf(1, 2, 3)

    /**
     * The payload of an iCCP chunk whose profile inflates beyond the
     * per-operation decompression limit.
     */
    private val bombIccpPayload: ByteArray =
        "test\u0000\u0000".encodeToByteArray() +
            Resource("de/stefan_oltmann/kim/testdata/decompression_bomb.zlib").readBytes()

    /**
     * The committed reproducer carries two iCCP chunks whose profiles
     * each inflate to 4 MiB. Deletion discards them, so both are skipped
     * without decompression: the write succeeds and the file is clean of
     * ICC chunks, with the image data intact.
     */
    @Test
    fun testDeleteMetadataSkipsDuplicateIccpChunksWhenDiscarded() {

        val bytes = Resource(
            "de/stefan_oltmann/kim/testdata/png_with_duplicate_iccp_chunks.png"
        ).readBytes()

        val newBytes = Kim.deleteMetadata(bytes, keepIccProfile = false)

        assertEquals(listOf("IHDR", "IDAT", "IEND"), storedChunkTypes(newBytes))
    }

    /**
     * A malformed iCCP chunk whose payload is not zlib data is dropped
     * like a valid one: the deletion does not parse the content it
     * discards, so the payload cannot fail the write.
     */
    @Test
    fun testDeleteMetadataSkipsMalformedIccpWhenDiscarded() {

        val newBytes = Kim.deleteMetadata(
            buildPng(listOf("iCCP" to malformedIccpPayload)),
            keepIccProfile = false
        )

        assertEquals(listOf("IHDR", "IDAT", "IEND"), storedChunkTypes(newBytes))
    }

    /**
     * A discarded iCCP chunk is never decompressed: its payload is a
     * decompression bomb beyond the per-operation limit, and the
     * deletion must not trip over what it drops.
     */
    @Test
    fun testDeleteMetadataSkipsIccpDecompressionBombWhenDiscarded() {

        val newBytes = Kim.deleteMetadata(
            buildPng(listOf("iCCP" to bombIccpPayload)),
            keepIccProfile = false
        )

        assertEquals(listOf("IHDR", "IDAT", "IEND"), storedChunkTypes(newBytes))
    }

    /**
     * The structural IEND marker ends the file: a well-formed chunk
     * appended behind it belongs to other tools and is discarded, like
     * the bytes behind the JPEG structural EOI marker.
     */
    @Test
    fun testDeleteMetadataDiscardsChunksBehindIEND() {

        val baseBytes = buildPng(emptyList())

        val trailingWriter = ByteArrayByteWriter()

        writeChunk(
            byteWriter = trailingWriter,
            typeName = "ukN1",
            data = byteArrayOf(0x11, 0x22, 0x33)
        )

        val newBytes = Kim.deleteMetadata(baseBytes + trailingWriter.toByteArray())

        assertContentEquals(baseBytes, newBytes)
    }

    /**
     * Trailing bytes behind the structural IEND marker that are not even
     * chunk-framed cannot fail the deletion either: they belong to other
     * tools and are discarded with the rest of the tail.
     */
    @Test
    fun testDeleteMetadataDiscardsGarbageBehindIEND() {

        val baseBytes = buildPng(emptyList())

        val newBytes = Kim.deleteMetadata(
            baseBytes + "KIM_TEST_DATA".encodeToByteArray()
        )

        assertContentEquals(baseBytes, newBytes)
    }

    /**
     * An update is conservative: the well-formed chunk appended behind
     * the structural IEND marker survives byte-exact, like ExifTool
     * preserves trailing data on targeted edits. The deletion cuts the
     * file at IEND instead.
     */
    @Test
    fun testUpdatePreservesWellFormedChunkBehindIEND() {

        val baseBytes = buildPng(emptyList())

        val trailingWriter = ByteArrayByteWriter()

        writeChunk(
            byteWriter = trailingWriter,
            typeName = "ukN1",
            data = byteArrayOf(0x11, 0x22, 0x33)
        )

        val trailingBytes = trailingWriter.toByteArray()

        val bytesWithTrailingChunk = baseBytes + trailingBytes

        /*
         * Precondition: the fixture carries the chunk behind IEND as its
         * tail - the parser stops at IEND and cannot see behind it, so
         * this is asserted on the raw bytes.
         */
        assertContentEquals(
            trailingBytes,
            bytesWithTrailingChunk.copyOfRange(
                baseBytes.size,
                bytesWithTrailingChunk.size
            )
        )

        val updatedBytes = Kim.update(
            bytes = bytesWithTrailingChunk,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /*
         * The chunk behind IEND survives byte-exact at the end of the
         * output.
         */
        assertContentEquals(
            trailingBytes,
            updatedBytes.copyOfRange(updatedBytes.size - trailingBytes.size, updatedBytes.size)
        )
    }

    /**
     * A malformed eXIf chunk fails the deletion: the rotation has to be
     * read from the EXIF content before it is removed, so the payload
     * must be parseable even though the chunk itself is discarded.
     */
    @Test
    fun testDeleteMetadataFailsOnMalformedExifWhenDiscarded() {

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(
                buildPng(listOf("eXIf" to byteArrayOf(1, 2, 3))),
                keepIccProfile = false
            )
        }
    }

    /**
     * A faulty ICC profile (the chunk decompresses, the content is not a
     * parseable profile) is dropped like JPEG drops faulty profiles: the
     * deletion never interprets ICC content, so a faulty profile lets
     * its chunk go instead of staying in the file or failing the
     * operation.
     */
    @Test
    fun testDeleteMetadataDropsFaultyIccProfile() {

        val newBytes = Kim.deleteMetadata(
            Resource(
                "de/stefan_oltmann/kim/testdata/png_with_faulty_icc_profile.png"
            ).readBytes()
        )

        val chunkTypes = storedChunkTypes(newBytes)

        assertEquals(0, chunkTypes.count { it == "iCCP" })

        assertTrue(chunkTypes.contains("IDAT"), "The image data must survive.")
    }

    /**
     * A malformed iCCP chunk whose payload is not zlib data fails the
     * deletion: the chunk cannot even be framed, which is structural
     * corruption rather than unparseable metadata content.
     */
    @Test
    fun testDeleteMetadataFailsOnMalformedIccpWhenKept() {

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(buildPng(listOf("iCCP" to malformedIccpPayload)))
        }
    }

    /**
     * Builds a minimal PNG: the given chunks sit between the IHDR chunk
     * and the image data of a 1x1 pixel image.
     */
    private fun buildPng(chunks: List<Pair<String, ByteArray>>): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        /* 1x1 pixel, 8 bit RGBA, no interlace. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        for ((typeName, data) in chunks)
            writeChunk(byteWriter = byteWriter, typeName = typeName, data = data)

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        return byteWriter.toByteArray()
    }

    /**
     * Reads the chunk type names of the file in order. The hostile
     * chunks under test are gone from the deleted output, so the clean
     * file parses without trouble.
     */
    private fun storedChunkTypes(pngBytes: ByteArray): List<String> =

        PngImageParser.readChunks(
            ByteArrayByteReader(pngBytes),
            chunkTypeFilter = null
        ).map { it.type.name }

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
