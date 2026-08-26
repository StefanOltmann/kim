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
package de.stefan_oltmann.kim.format.webp

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.webp.WebPConstants.WEBP_BYTE_ORDER
import de.stefan_oltmann.kim.format.webp.chunk.ImageSizeAware
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunk
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8L
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.jvm.JvmStatic

/**
 * Writes WebP files.
 */
public object WebPWriter {

    @JvmStatic
    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ): Unit = writeImage(
        chunks = WebPImageParser.readChunks(byteReader, false),
        byteWriter = byteWriter,
        exifBytes = exifBytes,
        xmp = xmp
    )

    @JvmStatic
    public fun writeImage(
        chunks: List<WebPChunk>,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        if (chunks.isEmpty())
            throw ImageWriteException("No chunks to write!")

        val modifiedChunks = chunks.toMutableList()

        /*
         * Delete old chunks that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.EXIF }

        if (xmp != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.XMP }

        /*
         * The metadata flags are derived from the chunks that will
         * actually be written - not from the old header flags, which can
         * be stale when a file claims EXIF or XMP without carrying the
         * corresponding chunk.
         */
        val hasExifChunk = exifBytes != null ||
            modifiedChunks.any { it.type == WebPChunkType.EXIF }

        val hasXmpChunk = xmp != null ||
            modifiedChunks.any { it.type == WebPChunkType.XMP }

        val headerChunk = modifiedChunks.first()

        /**
         * To write Exif & XMP we require the WebP file to have
         * a VP8X header with the correct marks set.
         *
         * If it already has one, we correct the header.
         * If it's missing the header we add it.
         */
        if (headerChunk is WebPChunkVP8X) {

            val replacementChunk = WebPChunkVP8X(
                bytes = WebPChunkVP8X.createBytes(
                    hasIcc = headerChunk.hasIcc,
                    hasAlpha = headerChunk.hasAlpha,
                    hasExif = hasExifChunk,
                    hasXmp = hasXmpChunk,
                    hasAnimation = headerChunk.hasAnimation,
                    imageSize = headerChunk.imageSize
                )
            )

            modifiedChunks.set(
                index = 0,
                element = replacementChunk
            )

        } else {

            /* Must be VP8 or VP8L */
            if (headerChunk !is ImageSizeAware)
                throw ImageWriteException("Illegal header chunk: $headerChunk")

            /*
             * A legacy VP8L bitstream can carry transparency, which the
             * generated VP8X header must declare or decoders may drop the
             * alpha channel. A legacy VP8 cannot have alpha.
             */
            val hasAlpha = (headerChunk as? WebPChunkVP8L)?.hasAlpha == true

            modifiedChunks.add(
                index = 0,
                element = WebPChunkVP8X(
                    bytes = WebPChunkVP8X.createBytes(
                        hasIcc = false,
                        hasAlpha = hasAlpha,
                        hasExif = exifBytes != null,
                        hasXmp = xmp != null,
                        hasAnimation = false,
                        imageSize = headerChunk.imageSize
                    )
                )
            )
        }

        val xmpBytes = xmp?.encodeToByteArray()

        /*
         * The RIFF size field sits at the start of the file, so the content
         * length must be computed up front. This keeps the file from being
         * buffered a second time in memory.
         */
        val contentLength = WebPConstants.WEBP_SIGNATURE.size +
            modifiedChunks.sumOf { webpChunkLength(it.bytes.size) } +
            (exifBytes?.let { webpChunkLength(it.size) } ?: 0) +
            (xmpBytes?.let { webpChunkLength(it.size) } ?: 0)

        byteWriter.write(WebPConstants.RIFF_SIGNATURE)

        byteWriter.writeInt(contentLength, WEBP_BYTE_ORDER)

        byteWriter.write(WebPConstants.WEBP_SIGNATURE)

        /*
         * First write all other chunks in the original order.
         */
        for (chunk in modifiedChunks)
            byteWriter.writeWebpChunk(chunk.type, chunk.bytes)

        /*
         * A major design flaw of WebP is that is specifies the metadata chunks to come last.
         *
         * This is what the documentation says:
         * "All chunks SHOULD be placed in the same order as listed above. If a chunk appears in
         * the wrong place, the file is invalid, but readers MAY parse the file, ignoring the
         * chunks that are out of order."
         *
         * See https://developers.google.com/speed/webp/docs/riff_container#extended_file_format
         */

        if (exifBytes != null)
            byteWriter.writeWebpChunk(WebPChunkType.EXIF, exifBytes)

        if (xmpBytes != null)
            byteWriter.writeWebpChunk(WebPChunkType.XMP, xmpBytes)
    }

    /**
     * Writes a chunk with its FourCC, size and padding byte.
     */
    private fun ByteWriter.writeWebpChunk(chunkType: WebPChunkType, data: ByteArray) {

        write(chunkType.bytes)

        writeInt(data.size, WEBP_BYTE_ORDER)

        write(data)

        /*
         * If chunk size is odd, a single padding byte (which MUST be 0
         * to conform with RIFF) is added.
         */
        if (data.size % 2 != 0)
            write(0)
    }

    /**
     * The length of a chunk on disk, including its header and the RIFF
     * padding byte for odd payloads.
     */
    private fun webpChunkLength(dataLength: Int): Int =
        WebPConstants.CHUNK_HEADER_LENGTH + dataLength + dataLength % 2
}
