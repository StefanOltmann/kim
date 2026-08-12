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
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object GifUpdater : MetadataUpdater {

    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        GifWriter.writeImageStreaming(byteReader, byteWriter) { chunks, outputWriter ->

            val xmp = GifImageParser.parseXmp(chunks)

            val xmpMeta: XMPMeta = if (xmp != null)
                XMPMetaFactory.parseFromString(xmp)
            else
                XMPMetaFactory.create()

            val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

            val modifiedChunks = chunks.toMutableList()

            modifiedChunks.removeAll { chunk ->
                chunk is GifChunkApplicationExtension &&
                    chunk.applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER
            }

            for (chunk in modifiedChunks)
                outputWriter.write(chunk.bytes)

            GifWriter.writeXmpChunk(outputWriter, updatedXmp)
        }
    }

    override fun updateThumbnail(bytes: ByteArray, thumbnailBytes: ByteArray): ByteArray {
        throw ImageWriteException("Can't embed thumbnail into GIF.")
    }
}
