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
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class PngImageParserTest {

    /**
     * The payload of every kept chunk is buffered, so like the JPEG
     * header segment budget, a hostile file of oversized metadata chunks
     * must fail the read instead of accumulating unboundedly in memory.
     */
    @Test
    fun testOversizedMetadataChunksFailAtTheBudget() {

        /*
         * A single text chunk whose declared payload exceeds the 16 MiB
         * budget: the metadata-scoped read must reject it before its
         * payload is allocated. The CRC is irrelevant - it is verified
         * only after the budget passes.
         */
        val chunkType = PngChunkType.TEXT.bytes

        val lengthBytes = byteArrayOf(
            ((17 * 1024 * 1024 shr 24) and 0xFF).toByte(),
            (((17 * 1024 * 1024 shr 16) and 0xFF)).toByte(),
            (((17 * 1024 * 1024 shr 8) and 0xFF)).toByte(),
            ((17 * 1024 * 1024 and 0xFF)).toByte()
        )

        val pngSignature = MediaFormatMagicNumbers.png.toByteArray()

        val bytes = pngSignature + lengthBytes + chunkType +
            ByteArray(1024) +
            byteArrayOf(0, 0, 0, 0) + "IEND".encodeToByteArray() +
            byteArrayOf(0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte())

        assertFailsWith<ImageReadException> {
            PngImageParser.readChunks(
                ByteArrayByteReader(bytes),
                listOf(PngChunkType.TEXT)
            )
        }
    }

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

        assertFailsWith<ImageReadException> {
            val truncatedChunk = PngChunkItxt.of(iTxtPayload, crc = 0)

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

        val truncatedChunk = PngChunkItxt.of(
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
     * The PNG specification allows a chunk type at most once per
     * datastream when its chunk ordering rules say "Multiple: No", and
     * eXIf is one of them. A file with a second eXIf chunk is therefore
     * corrupt and must fail the read.
     *
     * Attention: Kim deliberately diverges from ExifTool here - ExifTool
     * reads the first eXIf chunk and ignores later ones, while Kim
     * rejects the file, like it rejects CRC mismatches that ExifTool
     * only warns about.
     */
    @Test
    fun testRejectsDuplicateExifChunks() {

        val bytes = Resource("de/stefan_oltmann/kim/testdata/png_with_duplicate_exif_chunks.png")
            .readBytes()

        val exception = assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }

        assertEquals(
            expected = "The PNG file contains more than one eXIf chunk, " +
                "which the PNG specification allows only once.",
            actual = exception.message
        )
    }

    /**
     * The update reads the metadata of the source file first, so the
     * same read that rejects a file with duplicate eXIf chunks also
     * rejects the update. The former consolidation of duplicate EXIF
     * chunks no longer exists. Write operations report every failure
     * as ImageWriteException, so the read error arrives as its cause.
     */
    @Test
    fun testUpdateRejectsDuplicateExifChunks() {

        val bytes = Resource("de/stefan_oltmann/kim/testdata/png_with_duplicate_exif_chunks.png")
            .readBytes()

        val exception = assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = bytes,
                updates = setOf(MetadataUpdate.Orientation(TiffOrientation.UPSIDE_DOWN))
            )
        }

        assertEquals(
            expected = "The PNG file contains more than one eXIf chunk, " +
                "which the PNG specification allows only once.",
            actual = assertIs<ImageReadException>(exception.cause).message
        )
    }

    /**
     * The single-occurrence rule is enforced for every chunk type of
     * the specification's chunk ordering rules, whether Kim interprets
     * the chunk or not. The committed reproducer carries eight iCCP
     * chunks whose payloads each expand to 4 MiB, so recognizing the
     * second chunk before its payload is read keeps the read cheap and
     * rejects the hostile file.
     */
    @Test
    fun testRejectsDuplicateIccpChunks() {

        val bytes = Resource(
            "de/stefan_oltmann/kim/testdata/png_with_duplicate_iccp_chunks.png"
        ).readBytes()

        val exception = assertFailsWith<ImageReadException> {
            PngImageParser.readChunks(
                ByteArrayByteReader(bytes),
                listOf(PngChunkType.ICCP)
            )
        }

        assertEquals(
            expected = "The PNG file contains more than one iCCP chunk, " +
                "which the PNG specification allows only once.",
            actual = exception.message
        )
    }

    /**
     * The single-occurrence rule is not limited to the chunk types Kim
     * interprets: two tIME chunks are as invalid as two iCCP chunks and
     * fail the read the same way.
     */
    @Test
    fun testRejectsDuplicateTimeChunks() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(MediaFormatMagicNumbers.png.toByteArray())

        /* 1x1 pixel, 8 bit RGBA, no interlace. */
        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        /* Year 2024, May 12th, 18:04:00. */
        val timePayload = byteArrayOf(0x07, 0xE8.toByte(), 5, 12, 18, 4, 0)

        writeChunk(byteWriter = byteWriter, typeName = "tIME", data = timePayload)
        writeChunk(byteWriter = byteWriter, typeName = "tIME", data = timePayload)
        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val exception = assertFailsWith<ImageReadException> {
            PngImageParser.readChunks(
                ByteArrayByteReader(byteWriter.toByteArray()),
                chunkTypeFilter = null
            )
        }

        assertEquals(
            expected = "The PNG file contains more than one tIME chunk, " +
                "which the PNG specification allows only once.",
            actual = exception.message
        )
    }

    /**
     * The text chunks of one read may decompress to a cumulative budget
     * at most. Each zTXt chunk of the committed fixture stays within the
     * per-operation limit, but their sum of 21 MiB exceeds the budget, so
     * the read fails at the chunk that crosses it.
     */
    @Test
    fun testRejectsZtxtChunksExceedingTheTextBudget() {

        val bytes = Resource(
            "de/stefan_oltmann/kim/testdata/png_with_ztxt_bombs_exceeding_budget.png"
        ).readBytes()

        val exception = assertFailsWith<ImageReadException> {
            PngImageParser.readChunks(
                ByteArrayByteReader(bytes),
                listOf(PngChunkType.ZTXT)
            )
        }

        assertEquals(
            expected = "Decompressed data exceeds 2097152 bytes.",
            actual = exception.message
        )
    }

    /**
     * The control file with two zTXt chunks of the same size stays
     * within the cumulative budget, so both chunks must be read
     * completely - the budget must not reject legitimate heavy files.
     */
    @Suppress("MagicNumber")
    @Test
    fun testReadsZtxtChunksWithinTheTextBudget() {

        val bytes = Resource(
            "de/stefan_oltmann/kim/testdata/png_with_ztxt_bombs_within_budget.png"
        ).readBytes()

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(PngChunkType.ZTXT)
        )

        assertEquals(2, chunks.size)

        val textChunks = chunks.filterIsInstance<PngChunkZtxt>()

        assertEquals(2, textChunks.size)

        for (textChunk in textChunks)
            assertEquals(7 * 1024 * 1024, textChunk.text.length)
    }

    /**
     * The number of stored ancillary chunks of one read is bounded like
     * the chunk cache of libpng: a file of many tiny chunks inflates the
     * heap through object overhead alone, since every stored chunk costs
     * more memory than its payload draws from the byte budgets.
     */
    @Suppress("MagicNumber")
    @Test
    fun testRejectsMoreThanOneThousandStoredChunks() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(MediaFormatMagicNumbers.png.toByteArray())

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        repeat(1001) {
            writeChunk(
                byteWriter = byteWriter,
                typeName = "tEXt",
                data = byteArrayOf(0x6B, 0)
            )
        }

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val exception = assertFailsWith<ImageReadException> {
            PngImageParser.readChunks(
                ByteArrayByteReader(byteWriter.toByteArray()),
                listOf(PngChunkType.TEXT)
            )
        }

        assertEquals(
            expected = "The PNG file contains more than 1000 chunks.",
            actual = exception.message
        )
    }

    /**
     * Exactly 1000 stored chunks are within the limit - the boundary is
     * inclusive, like the chunk cache of libpng.
     */
    @Suppress("MagicNumber")
    @Test
    fun testReadsOneThousandStoredChunks() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(MediaFormatMagicNumbers.png.toByteArray())

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        repeat(1000) {
            writeChunk(
                byteWriter = byteWriter,
                typeName = "tEXt",
                data = byteArrayOf(0x6B, 0)
            )
        }

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(byteWriter.toByteArray()),
            listOf(PngChunkType.TEXT)
        )

        assertEquals(1000, chunks.size)
    }

    /**
     * Critical chunks are not stored in the chunk cache - like libpng,
     * which streams the image data instead of caching it. IDAT chunks do
     * not count against the limit, so files that write one IDAT chunk
     * per scanline still read.
     */
    @Suppress("MagicNumber")
    @Test
    fun testIdatChunksDoNotCountAsStoredChunks() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(MediaFormatMagicNumbers.png.toByteArray())

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        repeat(1001) {
            writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf())
        }

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(byteWriter.toByteArray()),
            chunkTypeFilter = null
        )

        assertEquals(1003, chunks.size)
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
