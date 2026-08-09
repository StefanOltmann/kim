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

import de.stefan_oltmann.kim.format.gif.chunk.GifChunk
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeString

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

    public fun writeImage(
        chunks: List<GifChunk>,
        byteWriter: ByteWriter,
        xmp: String? = null
    ) {

        var xmpWritten = false
        val modifiedChunks = chunks.toMutableList()

        /* Delete old chunks that are going to be replaced */
        if (xmp != null)
            modifiedChunks.removeAll {
                it is GifChunkApplicationExtension &&
                    it.applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER
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

    private fun writeXmpChunk(byteWriter: ByteWriter, xmpXml: String) {

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
