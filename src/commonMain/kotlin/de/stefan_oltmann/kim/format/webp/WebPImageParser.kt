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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.webp.WebPConstants.CHUNK_SIZE_LENGTH
import de.stefan_oltmann.kim.format.webp.WebPConstants.RIFF_SIGNATURE
import de.stefan_oltmann.kim.format.webp.WebPConstants.TPYE_LENGTH
import de.stefan_oltmann.kim.format.webp.WebPConstants.WEBP_BYTE_ORDER
import de.stefan_oltmann.kim.format.webp.WebPConstants.WEBP_SIGNATURE
import de.stefan_oltmann.kim.format.webp.chunk.ImageSizeAware
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunk
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkExif
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8L
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8X
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkXmp
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.model.MediaFormat
import kotlin.jvm.JvmStatic

/**
 * Parses the metadata of WebP files.
 */
public object WebPImageParser : ImageParser {

    /*
     * The highest byte index any ImageSizeAware chunk parses is 9,
     * so this prefix is sufficient to determine the image size.
     */
    private const val SIZE_HEADER_BYTES: Int = 16

    /* The "RIFF" signature plus the 4-byte size field. */
    private const val RIFF_PREFIX_LENGTH: Int = TPYE_LENGTH + CHUNK_SIZE_LENGTH

    /*
     * https://developers.google.com/speed/webp/docs/riff_container
     */
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            val chunks = readChunks(
                byteReader = byteReader,
                stopAfterMetadataRead = true
            )

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            parseMetadataFromChunks(chunks)
        }

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun parseMetadataFromChunks(chunks: List<WebPChunk>): MediaMetadata =
        tryWithImageReadException {

            val imageSizeAwareChunk = chunks.filterIsInstance<ImageSizeAware>().firstOrNull()

            checkNotNull(imageSizeAwareChunk) {
                "Did not find a header chunk containing the image size. " +
                    "Found chunk types: ${chunks.map { it.type }}"
            }

            val imageSize = imageSizeAwareChunk.imageSize

            val exifChunk = chunks.filterIsInstance<WebPChunkExif>().firstOrNull()
            val xmpChunk = chunks.filterIsInstance<WebPChunkXmp>().firstOrNull()

            /*
             * Corrupt XMP fails the update path in XMPMetaFactory anyway,
             * so it must fail the read as well (read/update symmetry),
             * like the GIF chunk validation.
             */
            val xmp = xmpChunk?.xmp?.takeIf { it.contains("<x:xmpmeta") }

            if (xmpChunk != null && xmp == null)
                throw ImageReadException("The WebP XMP chunk has no <x:xmpmeta> element.")

            return@tryWithImageReadException MediaMetadata(
                mediaFormat = MediaFormat.WEBP,
                imageSize = imageSize,
                exif = exifChunk?.tiffContents,
                exifBytes = exifChunk?.bytes,
                iptc = null, // not supported by WebP
                xmp = xmp
            )
        }

    /**
     * Reads the chunks of a WebP file.
     *
     * When [stopAfterMetadataRead] is set, only what metadata reading
     * needs is buffered: the size headers of the image chunks and the
     * metadata chunks themselves. The image bitstream of large chunks is
     * skipped in bounded chunks and such chunks are not part of the
     * result, so large images are never fully buffered.
     */
    public fun readChunks(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false
    ): List<WebPChunk> = tryWithImageReadException {

        byteReader.readAndVerifyBytes("RIFF signature", RIFF_SIGNATURE)

        /*
         * The RIFF size field is skipped, because many encoders write a
         * wrong value. An understated one hides metadata chunks appended
         * behind the declared end, which would silently be lost on a
         * rewrite, and an overstated one (up to 0xFFFFFFFF) runs into EOF
         * errors. Chunk iteration therefore runs to the actual end of the
         * content instead of trusting the declared size.
         */
        byteReader.skipBytes("RIFF size", CHUNK_SIZE_LENGTH)

        byteReader.readAndVerifyBytes("WEBP signature", WEBP_SIGNATURE)

        val bytesToRead =
            (byteReader.contentLength - RIFF_PREFIX_LENGTH - WEBP_SIGNATURE.size)
                .coerceAtLeast(0L)

        return readChunksInternal(
            byteReader = byteReader,
            bytesToRead = bytesToRead,
            stopAfterMetadataRead = stopAfterMetadataRead
        )
    }

    private fun readChunksInternal(
        byteReader: ByteReader,
        bytesToRead: Long,
        stopAfterMetadataRead: Boolean
    ): List<WebPChunk> {

        val chunks = mutableListOf<WebPChunk>()

        var bytesReadCount = 0L

        @Suppress("LoopWithTooManyJumpStatements")
        while (bytesReadCount < bytesToRead) {

            val chunkType = WebPChunkType.of(
                byteReader.readBytes("chunk type", TPYE_LENGTH)
            )

            val chunkSize = byteReader.read4BytesAsInt("chunk size", WEBP_BYTE_ORDER)

            if (chunkSize < 0)
                throw ImageReadException("Invalid WebP chunk length: $chunkSize")

            /*
             * For metadata reads only the metadata chunks themselves and
             * the size headers of the image chunks are needed. Everything
             * else is pure image data and skipped in bounded chunks.
             */
            val keepFullPayload =
                !stopAfterMetadataRead ||
                    chunkType == WebPChunkType.EXIF ||
                    chunkType == WebPChunkType.XMP

            val bytes: ByteArray = if (keepFullPayload) {

                byteReader.readBytes("chunk data", chunkSize)

            } else {

                val prefixLength = minOf(chunkSize, SIZE_HEADER_BYTES)

                val prefix = byteReader.readBytes("chunk header", prefixLength)

                byteReader.skipBytes("image data", chunkSize - prefixLength)

                prefix
            }

            /*
             * If chunk size is odd, a single padding byte (which MUST be 0
             * to conform with RIFF) is added between chunks. A nonconformant
             * encoder may omit the pad byte of the final chunk, which then
             * is the end of the file instead of a parse error.
             */
            val hasPadding = chunkSize % 2 != 0

            val paddedEndCount =
                bytesReadCount + TPYE_LENGTH + CHUNK_SIZE_LENGTH + chunkSize + 1

            val hasFinalPadding = hasPadding && paddedEndCount <= bytesToRead

            if (hasFinalPadding)
                byteReader.skipBytes("padding byte", 1)

            bytesReadCount += TPYE_LENGTH + CHUNK_SIZE_LENGTH + chunkSize +
                if (hasFinalPadding) 1 else 0

            /*
             * Skipped image chunks are not part of the result, because
             * they carry no information for metadata parsing.
             */
            val isImageChunk =
                !keepFullPayload &&
                    chunkType != WebPChunkType.VP8 &&
                    chunkType != WebPChunkType.VP8L &&
                    chunkType != WebPChunkType.VP8X

            val chunk = when (chunkType) {
                WebPChunkType.VP8 -> WebPChunkVP8(bytes)
                WebPChunkType.VP8L -> WebPChunkVP8L(bytes)
                WebPChunkType.VP8X -> WebPChunkVP8X(bytes)
                WebPChunkType.EXIF -> WebPChunkExif(bytes)
                WebPChunkType.XMP -> WebPChunkXmp(bytes)
                else -> WebPChunk(chunkType, bytes)
            }

            if (!isImageChunk)
                chunks.add(chunk)

            /*
             * After reading the header we can decide if we need to
             * read the rest of the file for metadata.
             */
            if (stopAfterMetadataRead) {

                /*
                 * Legacy files (without VP8X header) have no metadata chunks:
                 * the VP8/VP8L image chunk is their only chunk.
                 * In extended files the metadata chunks follow the image chunk.
                 */
                val isLegacyImageChunk =
                    (chunkType == WebPChunkType.VP8 || chunkType == WebPChunkType.VP8L) &&
                        chunks.none { it is WebPChunkVP8X }

                if (isLegacyImageChunk)
                    break

                /*
                 * If the header reveals that there will be no EXIF and no XMP
                 * we don't need to read the whole file.
                 */
                if (chunk is WebPChunkVP8X && !chunk.hasExif && !chunk.hasXmp)
                    break
            }
        }

        return chunks
    }
}
