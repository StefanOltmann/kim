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

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.png.chunk.PngTextChunk
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object PngUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        PngWriter.writeImageStreaming(byteReader, byteWriter) { chunks, outputWriter ->

            val metadata = PngImageParser.parseMetadataFromChunks(chunks)

            val xmpMeta: XMPMeta = if (metadata.xmp != null)
                XMPMetaFactory.parseFromString(metadata.xmp)
            else
                XMPMetaFactory.create()

            val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

            val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifBytes: ByteArray? = if (outputSet.applyUpdates(updates)) {

                val exifBytesWriter = ByteArrayByteWriter()

                TiffWriter(byteOrder = outputSet.byteOrder).write(exifBytesWriter, outputSet)

                exifBytesWriter.toByteArray()

            } else {
                null
            }

            PngWriter.writeImage(
                chunks = chunks,
                byteWriter = outputWriter,
                exifBytes = exifBytes,
                /*
                 * IPTC is not written because it's not recognized everywhere.
                 * XMP is the better choice. If users demand it we may add it.
                 * The logic is already implemented.
                 */
                iptcBytes = null,
                xmp = updatedXmp
            )

            /*
             * Behind the image data only stale duplicates of the rewritten
             * metadata are removed. Comments and tIME chunks are user data
             * unrelated to the change and must survive an update.
             */
            StaleChunkFilter { chunkType, keyword ->
                (exifBytes != null &&
                    (chunkType == PngChunkType.EXIF || keyword == PngConstants.EXIF_KEYWORD)) ||
                    (keyword == PngConstants.XMP_KEYWORD)
            }
        }
    }

    @Throws(ImageWriteException::class)
    override fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) = tryWithImageWriteException {

        PngWriter.writeImageStreaming(byteReader, byteWriter) { chunks, outputWriter ->

            /*
             * Remove the EXIF chunk and all text chunks, which carry XMP,
             * IPTC and comments. The iCCP chunk is kept, because it affects
             * how the image is displayed.
             */
            val chunksWithoutMetadata = chunks.filterNot { chunk ->
                chunk.type == PngChunkType.EXIF ||
                    chunk is PngTextChunk ||
                    chunk.type == PngChunkType.TIME
            }

            PngWriter.writeImage(
                chunks = chunksWithoutMetadata,
                byteWriter = outputWriter
            )

            StaleChunkFilter.ALL_METADATA
        }
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.png))
            throw ImageWriteException("Provided input bytes are not PNG!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val metadata = PngImageParser.parseMetadataFromChunks(chunks)

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val exifBytesWriter = ByteArrayByteWriter()

        TiffWriter(byteOrder = outputSet.byteOrder).write(exifBytesWriter, outputSet)

        val exifBytes = exifBytesWriter.toByteArray()

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            iptcBytes = null,
            xmp = null // No change to XMP
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
