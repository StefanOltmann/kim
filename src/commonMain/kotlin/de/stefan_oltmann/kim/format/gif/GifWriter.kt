/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.gif.chunk.GifChunk
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkHeader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.copyRemainingTo
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeString

/**
 * Writes GIF files.
 */
public object GifWriter {

    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmp: String?
    ): Unit = writeImage(
        chunks = GifImageParser.readChunks(byteReader, null),
        byteWriter = byteWriter,
        xmp = xmp
    )

    /**
     * Streams a GIF from the given reader to the given writer, so the
     * updateComputer can rewrite the header chunks once the first image
     * starts.
     *
     * The updateComputer receives the chunks before the first image and the
     * output writer, and must write the complete header (all chunks) to it.
     * The image data behind the first image separator is then streamed in
     * bounded chunks, so the whole file never has to be buffered in memory.
     */
    internal fun writeImageStreaming(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updateComputer: (List<GifChunk>, ByteWriter) -> Unit
    ) {

        val (chunks, foundImage) = GifImageParser.readChunksBeforeImage(byteReader)

        /*
         * A file without an image is not a valid GIF. Writing the header
         * would hide the problem, so the update is rejected instead.
         */
        if (!foundImage)
            throw ImageWriteException("GIF file has no image data.")

        updateComputer(chunks, byteWriter)

        /*
         * The image separator byte was consumed while looking for the first
         * image and belongs to the streamed image data.
         */
        byteWriter.write(GifConstants.IMAGE_SEPARATOR)

        byteReader.copyRemainingTo(byteWriter)
    }

    public fun writeImage(
        chunks: List<GifChunk>,
        byteWriter: ByteWriter,
        xmp: String? = null
    ) {

        var xmpWritten = false
        val modifiedChunks = chunks.toMutableList()

        /* Delete old chunks that are going to be replaced */
        if (xmp != null) {

            modifiedChunks.removeAll {
                it is GifChunkApplicationExtension &&
                    it.applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER
            }

            upgradeGif87aHeader(modifiedChunks)
        }

        for (chunk in modifiedChunks) {

            /* Write new metadata chunk right before the first image descriptor */
            if (GifChunkType.IMAGE_DESCRIPTOR == chunk.type && xmp != null && !xmpWritten) {
                writeXmpChunk(byteWriter, xmp)
                xmpWritten = true
            }

            byteWriter.write(chunk.bytes)
        }
    }

    /**
     * Replaces a GIF87a header chunk with a GIF89a one, because the XMP
     * application extension is a GIF89a feature.
     */
    internal fun upgradeGif87aHeader(chunks: MutableList<GifChunk>) {

        val headerIndex = chunks.indexOfFirst { it.type == GifChunkType.HEADER }

        if (headerIndex == -1)
            return

        val headerChunk = chunks[headerIndex] as GifChunkHeader

        if (headerChunk.version == GifVersion.GIF87A)
            chunks[headerIndex] = GifChunkHeader(
                GifConstants.GIF_SIGNATURE + GifVersion.GIF89A.bytes
            )
    }

    internal fun writeXmpChunk(byteWriter: ByteWriter, xmpXml: String) {

        byteWriter.write(GifConstants.EXTENSION_INTRODUCER)
        byteWriter.write(GifConstants.APPLICATION_EXTENSION_LABEL)
        byteWriter.write((GifConstants.XMP_APPLICATION_IDENTIFIER + GifConstants.XMP_APPLICATION_CODE).length)
        byteWriter.writeString(GifConstants.XMP_APPLICATION_IDENTIFIER)
        byteWriter.writeString(GifConstants.XMP_APPLICATION_CODE)

        /*
         * The XMP payload is written in size-prefixed sub-blocks of at most
         * 255 bytes, as required by the GIF89a application extension format.
         */
        val xmpBytes = xmpXml.encodeToByteArray()

        for (offset in xmpBytes.indices step GifConstants.GIF_MAX_SUB_BLOCK_SIZE) {

            val endIndex = minOf(offset + GifConstants.GIF_MAX_SUB_BLOCK_SIZE, xmpBytes.size)

            writeSubBlock(byteWriter, xmpBytes.copyOfRange(offset, endIndex))
        }

        /*
         * The magic trailer starts with 0xFF and is therefore a valid
         * 255-byte sub-block by itself.
         */
        val magicTrailer = ByteArray(256) { (0xFF - it).toByte() }
        byteWriter.write(magicTrailer)
        byteWriter.write(GifConstants.BLOCK_TERMINATOR)
    }

    private fun writeSubBlock(byteWriter: ByteWriter, data: ByteArray) {

        byteWriter.write(data.size)
        byteWriter.write(data)
    }
}
