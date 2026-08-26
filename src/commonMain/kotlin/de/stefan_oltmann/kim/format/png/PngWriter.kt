/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.format.png.PngConstants.PNG_BYTE_ORDER
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.format.png.chunk.PngChunk
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.format.png.chunk.PngTextChunk
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.transferExactly
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeInt

/**
 * Writes PNG files.
 */
public object PngWriter {

    /* A chunk header consists of the 4-byte data length and the 4-byte type. */
    private const val CHUNK_HEADER_LENGTH: Int = 2 * PngConstants.TPYE_LENGTH

    private const val CRC_LENGTH: Long = 4L

    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        iptcBytes: ByteArray?,
        xmp: String?
    ): Unit = writeImage(
        chunks = PngImageParser.readChunks(byteReader, null),
        byteWriter = byteWriter,
        exifBytes = exifBytes,
        iptcBytes = iptcBytes,
        xmp = xmp
    )

    public fun writeImage(
        chunks: List<PngChunk>,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        iptcBytes: ByteArray?,
        xmp: String?
    ) {

        val modifiedChunks = chunks.toMutableList()

        /*
         * Delete old chunks that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedChunks.removeAll {
                it.type == PngChunkType.EXIF ||
                    it is PngTextChunk && it.getKeyword() == PngConstants.EXIF_KEYWORD
            }

        if (iptcBytes != null)
            modifiedChunks.removeAll { it is PngTextChunk && it.getKeyword() == PngConstants.IPTC_KEYWORD }

        if (xmp != null)
            modifiedChunks.removeAll { it is PngTextChunk && it.getKeyword() == PngConstants.XMP_KEYWORD }

        /*
         * Write the new file
         */

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        for (chunk in modifiedChunks) {

            writeChunk(byteWriter, chunk.type, chunk.bytes)

            /* Write new metadata chunks right after the header. */
            if (PngChunkType.IHDR == chunk.type) {

                if (exifBytes != null)
                    writeChunk(byteWriter, PngChunkType.EXIF, exifBytes)

                if (iptcBytes != null)
                    writeIptcChunk(byteWriter, iptcBytes)

                if (xmp != null)
                    writeXmpChunk(byteWriter, xmp)
            }
        }
    }

    /**
     * Streams a PNG from the given reader to the given writer, so the
     * updateComputer can rewrite the header chunks once the image data
     * starts.
     *
     * The updateComputer receives the chunks before the first IDAT chunk and
     * the output writer, and must write the complete header (signature and
     * all chunks) to it. It returns which trailing chunks count as stale
     * duplicates of what it rewrote. The image data behind the first IDAT
     * chunk is then streamed in bounded chunks, so the whole file never has
     * to be buffered in memory.
     */
    internal fun writeImageStreaming(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updateComputer: (List<PngChunk>, ByteWriter) -> StaleChunkFilter
    ) {

        val pendingImageDataHeader = ByteArrayByteWriter()

        val chunks = PngImageParser.readChunksUntilImageData(byteReader, pendingImageDataHeader)

        val imageDataHeader = pendingImageDataHeader.toByteArray()

        /*
         * A metadata-only or truncated file has no image data to stream.
         * Rejecting here keeps the output empty, instead of failing with
         * an opaque error after the update computer already wrote the
         * rewritten header.
         */
        if (imageDataHeader.isEmpty())
            throw ImageWriteException("PNG file has no image data.")

        /*
         * The reader sits inside the open IDAT chunk behind its header, so
         * its remaining data and CRC belong to the streamed image data.
         */
        val imageDataLength =
            imageDataHeader.toInt(0, PNG_BYTE_ORDER) + CRC_LENGTH

        val staleFilter = updateComputer(chunks, byteWriter)

        byteWriter.write(imageDataHeader)

        byteReader.transferExactly(byteWriter, imageDataLength)

        /*
         * From here on every chunk boundary is intact, so stale metadata
         * chunks can be dropped while streaming the tail.
         */
        copyChunksSkippingMetadata(byteReader, byteWriter, staleFilter)
    }

    /**
     * Streams the remaining chunks to the given writer, dropping exactly
     * those that [staleFilter] identifies as stale duplicates of rewritten
     * metadata. All other chunks - including unknown ones and text chunks
     * unrelated to the change - stream through untouched.
     *
     * Attention: A candidate chunk whose content cannot be parsed is
     * preserved, because Kim must never destroy data it does not
     * understand.
     */
    private fun copyChunksSkippingMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        staleFilter: StaleChunkFilter
    ) {

        while (true) {

            val header = byteReader.readBytes(CHUNK_HEADER_LENGTH)

            /* A truncated chunk header at the end of the stream. */
            if (header.size < CHUNK_HEADER_LENGTH) {

                byteWriter.write(header)

                return
            }

            val dataLength = header.toInt(0, PNG_BYTE_ORDER)

            if (dataLength < 0)
                throw ImageReadException("Invalid PNG chunk length: $dataLength")

            val chunkType = PngChunkType.of(
                header.copyOfRange(PngConstants.TPYE_LENGTH, CHUNK_HEADER_LENGTH)
            )

            if (StaleChunkFilter.isMetadataChunkType(chunkType)) {

                /*
                 * Buffered once, so the filter can inspect the content of
                 * text chunks. transferExactly fails loudly on truncation,
                 * like the pure streaming variant did.
                 */
                val payloadWriter = ByteArrayByteWriter()

                byteReader.transferExactly(payloadWriter, dataLength.toLong())

                val crcWriter = ByteArrayByteWriter()

                byteReader.transferExactly(crcWriter, CRC_LENGTH)

                val isStale = try {
                    staleFilter.isStale(chunkType, keywordOf(chunkType, payloadWriter.toByteArray()))
                } catch (_: ImageReadException) {
                    false
                }

                if (!isStale) {

                    byteWriter.write(header)
                    byteWriter.write(payloadWriter.toByteArray())
                    byteWriter.write(crcWriter.toByteArray())
                }

            } else {

                val totalLength = dataLength + CRC_LENGTH

                byteWriter.write(header)

                byteReader.transferExactly(byteWriter, totalLength)
            }
        }
    }

    /**
     * Returns the keyword of a text chunk payload, or NULL for chunk types
     * without a keyword.
     */
    private fun keywordOf(chunkType: PngChunkType, data: ByteArray): String? =
        when (chunkType) {
            PngChunkType.TEXT -> PngChunkText(chunkType, data, 0).getKeyword()
            PngChunkType.ZTXT -> PngChunkZtxt(data, 0).getKeyword()
            PngChunkType.ITXT -> PngChunkItxt(data, 0).getKeyword()
            else -> null
        }

    public fun writeImage(
        chunks: List<PngChunk>,
        byteWriter: ByteWriter,
    ) {

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        for (chunk in chunks)
            writeChunk(byteWriter, chunk.type, chunk.bytes)
    }

    private fun writeChunk(
        byteWriter: ByteWriter,
        chunkType: PngChunkType,
        data: ByteArray?
    ) {

        val dataLength = data?.size ?: 0

        byteWriter.writeInt(dataLength, PNG_BYTE_ORDER)
        byteWriter.write(chunkType.bytes)

        if (data != null)
            byteWriter.write(data)

        val crc1 = startPartialCrc(chunkType.bytes)

        val crc2 = if (data == null)
            crc1
        else
            continuePartialCrc(crc1, data)

        val crc = finishPartialCrc(crc2).toInt()

        byteWriter.writeInt(crc, PNG_BYTE_ORDER)
    }

    /**
     * XMP is often uncompressed (see GIMP for example).
     * For better compatibility we also write it without compression.
     * The chunk type iTXT is the standard for this, because XMP is UTF-8.
     */
    private fun writeXmpChunk(byteWriter: ByteWriter, xmpXml: String) {

        /*
         * Keyword:            1-79 bytes (character string)
         * Null separator:     1 byte
         * Compression flag:   1 byte
         * Compression method: 1 byte
         * Language tag:       0 or more bytes (character string)
         * Null separator:     1 byte
         * Translated keyword: 0 or more bytes
         * Null separator:     1 byte
         * Text:               0 or more bytes
         */

        val writer = ByteArrayByteWriter()

        /* XMP keyword */
        writer.write(PngConstants.XMP_KEYWORD.encodeToByteArray())
        writer.write(0)

        /* No compression and no language tag */
        writer.write(0) // No compression
        writer.write(0) // No compression method
        writer.write(0) // No language tag

        /* XMP keyword - null-terminated */
        writer.write(PngConstants.XMP_KEYWORD.encodeToByteArray())
        writer.write(0)

        /* XMP bytes */
        writer.write(xmpXml.encodeToByteArray())

        writeChunk(byteWriter, PngChunkType.ITXT, writer.toByteArray())
    }

    /**
     * Write non-standard IPTC in TEXT chunk the same way as ExifTool does it.
     *
     * Note that a lot of tools like Apple Preview will not be able to read this,
     * but at least ExifTool and GIMP will.
     */
    @Suppress("UnusedPrivateMember", "kotlin:S1144")
    private fun writeIptcChunk(byteWriter: ByteWriter, iptcBytes: ByteArray) {

        /*
         * Keyword:        1-79 bytes (character string)
         * Null separator: 1 byte
         * Text:           n bytes
         */

        val writer = ByteArrayByteWriter()

        /* IPTC keyword */
        writer.write(PngConstants.IPTC_KEYWORD.encodeToByteArray())
        writer.write(0)

        val sizeAsText =
            iptcBytes.size.toString().padStart(
                PngConstants.TXT_SIZE_LENGTH,
                PngConstants.TXT_SIZE_PAD
            )

        @Suppress("MultilineRawStringIndentation")
        val textToWrite = "\n" + """
            |IPTC profile
            |$sizeAsText
            |${iptcBytes.toHex()}""".trimMargin()

        writer.write(textToWrite.encodeToByteArray())

        writeChunk(byteWriter, PngChunkType.TEXT, writer.toByteArray())
    }
}
