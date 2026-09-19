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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.gif.chunk.GifChunk
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkCommentExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkHeader
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkImageData
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkImageDescriptor
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkLogicalScreenDescriptor
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkPlainTextExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkTerminator
import de.stefan_oltmann.kim.format.gif.chunk.joinGifSubChunks
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.readByte
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.transferExactly
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.output.ByteWriter
import kotlin.jvm.JvmStatic

/**
 * Parses the metadata of GIF files.
 */
public object GifImageParser : ImageParser {

    private val metadataChunkTypes = listOf(
        GifChunkType.HEADER,
        GifChunkType.IMAGE_DESCRIPTOR,
        GifChunkType.APPLICATION_EXTENSION
    )

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            val chunks = readChunks(byteReader, metadataChunkTypes)

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            return@tryWithImageReadException parseMetadataFromChunks(chunks)
        }

    /**
     * Assembles the metadata from the chunks of a GIF file.
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun parseMetadataFromChunks(chunks: List<GifChunk>): MediaMetadata = tryWithImageReadException {

        require(chunks.isNotEmpty()) {
            "Given chunk list was empty."
        }

        val headerChunk = chunks.filterIsInstance<GifChunkHeader>().firstOrNull()

        checkNotNull(headerChunk) {
            "Did not find mandatory header chunk. " +
                "Found chunk types: ${chunks.map { it.type }}"
        }

        val firstImageDescriptorChunk = chunks.filterIsInstance<GifChunkImageDescriptor>().firstOrNull()

        checkNotNull(firstImageDescriptorChunk) {
            "Did not find mandatory image descriptor chunk. " +
                "Found chunk types: ${chunks.map { it.type }}"
        }

        /*
         * The logical screen canvas is what reference parsers report. The
         * first frame's descriptor is only a crop of that canvas, so it is
         * just the fallback for files without a usable screen descriptor.
         */
        val imageSize = chunks
            .filterIsInstance<GifChunkLogicalScreenDescriptor>()
            .firstOrNull()
            ?.canvasSize
            ?: firstImageDescriptorChunk.imageSize

        val xmp = parseXmp(chunks)

        return@tryWithImageReadException MediaMetadata(
            mediaFormat = MediaFormat.GIF,
            imageSize = imageSize,
            exif = null,
            exifBytes = null, // GIF does not support EXIF data
            iptc = null, // GIF does not support IPTC data
            xmp = xmp
        )
    }

    /**
     * Returns the XMP of the given GIF chunks, or NULL for GIF87A files
     * that cannot store XMP.
     */
    internal fun parseXmp(chunks: List<GifChunk>): String? {

        val headerChunk = chunks.filterIsInstance<GifChunkHeader>().firstOrNull()
            ?: return null

        /* Only GIF89A supports XMP metadata */
        if (headerChunk.version != GifVersion.GIF89A)
            return null

        return getXmpXml(chunks)
    }

    private fun getXmpXml(chunks: List<GifChunk>): String? = chunks
        .filterIsInstance<GifChunkApplicationExtension>()
        .firstOrNull { it.isXmpExtension }
        ?.parseAsXmpOrThrow()

    /**
     * Reads the chunks of a whole GIF file.
     *
     * With a non-NULL filter only chunks of the listed types are returned;
     * skipped chunks are still consumed, so the reader stays in sync.
     */
    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<GifChunkType>?
    ): List<GifChunk> {

        val chunks = mutableListOf<GifChunk>()

        readHeaderChunks(byteReader, chunks, chunkTypeFilter)

        /* Read remaining chunks */
        byteReader.walkGifBlocks(
            onImageBlock = {
                chunks.addAll(readImageChunks(byteReader, chunkTypeFilter))
                false
            },
            onExtensionBlock = { extensionLabel ->
                val chunk = readExtensionChunk(byteReader, extensionLabel)
                if (keepChunk(chunkTypeFilter, chunk.type))
                    chunks.add(chunk)
                false
            },
            onTrailerBlock = {
                if (keepChunk(chunkTypeFilter, GifChunkType.TERMINATOR))
                    chunks.add(GifChunkTerminator(byteArrayOf(GifConstants.GIF_TERMINATOR)))
                true
            }
        )

        return chunks
    }

    /**
     * Reads the GIF chunks up to the first image descriptor, so the image
     * data can be streamed afterwards without buffering the whole file.
     *
     * The image separator byte of the first image is consumed by the reader
     * but belongs to the streamed image data, so callers must write it to
     * the output before streaming.
     *
     * Returns the chunks and whether an image was found at all. Files
     * without an image (only extensions and the trailer) cannot be updated.
     */
    internal fun readChunksBeforeImage(byteReader: ByteReader): Pair<List<GifChunk>, Boolean> {

        val chunks = mutableListOf<GifChunk>()

        readHeaderChunks(byteReader, chunks, chunkTypeFilter = null)

        /* Read extension chunks until the first image starts. */
        var foundImage = false

        byteReader.walkGifBlocks(
            onImageBlock = {
                foundImage = true
                true
            },
            onExtensionBlock = { extensionLabel ->
                chunks.add(readExtensionChunk(byteReader, extensionLabel))
                false
            },
            onTrailerBlock = {
                chunks.add(GifChunkTerminator(byteArrayOf(GifConstants.GIF_TERMINATOR)))
                true
            }
        )

        return chunks to foundImage
    }

    /**
     * Reads the mandatory GIF header and logical screen descriptor plus
     * the optional global color table, adding each to [chunks].
     */
    private fun readHeaderChunks(
        byteReader: ByteReader,
        chunks: MutableList<GifChunk>,
        chunkTypeFilter: List<GifChunkType>?
    ) {

        /* Read header chunk */
        val headerBytes = byteReader.readBytes(6)

        if (keepChunk(chunkTypeFilter, GifChunkType.HEADER))
            chunks.add(GifChunkHeader(headerBytes))

        /* Read logical screen descriptor chunk */
        val logicalScreenDescriptorBytes = byteReader.readBytes(7)
        val logicalScreenDescriptorChunk = GifChunkLogicalScreenDescriptor(logicalScreenDescriptorBytes)

        if (keepChunk(chunkTypeFilter, GifChunkType.LOGICAL_SCREEN_DESCRIPTOR))
            chunks.add(logicalScreenDescriptorChunk)

        /* Read global color table chunk if present */
        if (logicalScreenDescriptorChunk.globalColorTableFlag) {

            val globalColorTableSize = gifColorTableSizeBytes(logicalScreenDescriptorChunk.globalColorTableSize)

            val globalColorTableBytes = byteReader.readBytes(globalColorTableSize)

            if (keepChunk(chunkTypeFilter, GifChunkType.GLOBAL_COLOR_TABLE))
                chunks.add(GifChunk(GifChunkType.GLOBAL_COLOR_TABLE, globalColorTableBytes))
        }
    }

    /**
     * Whether a chunk of the given type passes the filter. A NULL filter
     * keeps every chunk.
     */
    private fun keepChunk(
        chunkTypeFilter: List<GifChunkType>?,
        type: GifChunkType
    ): Boolean =
        chunkTypeFilter?.contains(type) ?: true

    private fun readImageChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<GifChunkType>?
    ): List<GifChunk> {

        val chunks = mutableListOf<GifChunk>()

        /* Read image descriptor */
        val imageDescriptorBytes = byteReader.readBytes("image descriptor", 9)

        val imageDescriptorChunk = GifChunkImageDescriptor(
            byteArrayOf(GifConstants.IMAGE_SEPARATOR) + imageDescriptorBytes
        )

        if (keepChunk(chunkTypeFilter, GifChunkType.IMAGE_DESCRIPTOR))
            chunks.add(imageDescriptorChunk)

        /* Read local color table if present */
        if (imageDescriptorChunk.localColorTableFlag) {

            val localColorTableSize = gifColorTableSizeBytes(imageDescriptorChunk.localColorTableSize)

            val localColorTableBytes = byteReader.readBytes("local color table", localColorTableSize)

            if (keepChunk(chunkTypeFilter, GifChunkType.LOCAL_COLOR_TABLE))
                chunks.add(GifChunk(GifChunkType.LOCAL_COLOR_TABLE, localColorTableBytes))
        }

        /* Read image data */
        val lzwMinimumCodeSize = byteReader.readByte("LZW minimum code size")
        val subChunks = byteReader.parseGifSubChunksUntilEmpty("image data")

        if (keepChunk(chunkTypeFilter, GifChunkType.IMAGE_DATA))
            chunks.add(GifChunkImageData(lzwMinimumCodeSize, subChunks))

        return chunks
    }

    /**
     * Reads the extension chunk at the given label. The complete
     * sub-block chain is consumed in any case, otherwise the stream
     * position desyncs and payload bytes are later misinterpreted as
     * top-level blocks - which can silently corrupt rewritten files.
     *
     * Unknown labels are kept as an opaque block, so their content is
     * never destroyed by a rewrite.
     */
    internal fun readExtensionChunk(
        byteReader: ByteReader,
        extensionLabel: Byte
    ): GifChunk =
        when (extensionLabel) {

            GifConstants.GRAPHICS_CONTROL_EXTENSION_LABEL -> {

                val graphicsControlExtensionBytes = byteReader.readBytes("graphics control extension", 6)

                GifChunk(
                    GifChunkType.GRAPHICS_CONTROL_EXTENSION,
                    byteArrayOf(
                        GifConstants.EXTENSION_INTRODUCER,
                        GifConstants.GRAPHICS_CONTROL_EXTENSION_LABEL
                    ) + graphicsControlExtensionBytes
                )
            }

            GifConstants.APPLICATION_EXTENSION_LABEL ->
                GifChunkApplicationExtension(
                    byteArrayOf(
                        GifConstants.EXTENSION_INTRODUCER,
                        GifConstants.APPLICATION_EXTENSION_LABEL
                    ),
                    byteReader.parseGifSubChunksUntilEmpty("application extension")
                )

            GifConstants.COMMENT_EXTENSION_LABEL ->
                GifChunkCommentExtension(
                    byteArrayOf(GifConstants.EXTENSION_INTRODUCER, GifConstants.COMMENT_EXTENSION_LABEL),
                    byteReader.parseGifSubChunksUntilEmpty("comment extension")
                )

            GifConstants.PLAIN_TEXT_EXTENSION_LABEL ->
                GifChunkPlainTextExtension(
                    byteArrayOf(GifConstants.EXTENSION_INTRODUCER, GifConstants.PLAIN_TEXT_EXTENSION_LABEL),
                    byteReader.parseGifSubChunksUntilEmpty("plain text extension")
                )

            else ->
                GifChunk(
                    GifChunkType.UNKNOWN_EXTENSION,
                    joinGifSubChunks(
                        byteArrayOf(GifConstants.EXTENSION_INTRODUCER, extensionLabel),
                        byteReader.parseGifSubChunksUntilEmpty("unknown extension")
                    )
                )
        }

    internal fun ByteReader.parseGifSubChunksUntilEmpty(
        fieldName: String
    ): List<ByteArray> {

        val subChunks = mutableListOf<ByteArray>()

        while (true) {

            val subChunkSize = this.readByteAsInt()

            /* Break at the end of sub chunks */
            if (subChunkSize == 0)
                break

            val subChunkBytes = this.readBytes("$fieldName sub chunk", subChunkSize)

            subChunks.add(byteArrayOf(subChunkSize.toByte()) + subChunkBytes)
        }

        return subChunks
    }
}

/**
 * Walks the top-level blocks of a GIF file starting at the next
 * introducer byte and dispatches every block to its handler.
 *
 * The walker owns introducer dispatch, EOF handling and the rejection of
 * unknown introducers, so every GIF read and write follows the same
 * framing rules. A handler returns true to end the walk after its block;
 * the block's own bytes are consumed by the handler.
 */
internal fun ByteReader.walkGifBlocks(
    onImageBlock: () -> Boolean,
    onExtensionBlock: (extensionLabel: Byte) -> Boolean,
    onTrailerBlock: () -> Boolean
) {

    while (true) {

        val stop = when (val introducer = readByte("introducer")) {

            GifConstants.IMAGE_SEPARATOR -> onImageBlock()

            GifConstants.EXTENSION_INTRODUCER -> onExtensionBlock(readByte("extension label"))

            GifConstants.GIF_TERMINATOR -> onTrailerBlock()

            /*
             * Dropping the byte would shift all following data and
             * silently corrupt the file, so unknown structures fail
             * the read like the streaming write path.
             */
            else -> throw ImageReadException(
                "Unknown GIF block introducer: ${introducer.toHex()}"
            )
        }

        if (stop)
            return
    }
}

/**
 * Streams a chain of size-prefixed sub-blocks up to and including the
 * block terminator to the given writer, or skips it when the writer is
 * NULL, so a filtered extension cannot desync the stream.
 */
internal fun ByteReader.transferGifSubBlocks(byteWriter: ByteWriter?) {

    while (true) {

        val sizeByte = readByte()
            ?: throw ImageReadException("Unexpected end of file behind a GIF sub-block chain.")

        byteWriter?.write(sizeByte)

        if (sizeByte == GifConstants.BLOCK_TERMINATOR)
            return

        transferExactly(byteWriter, sizeByte.toUInt8().toLong())
    }
}

/**
 * The byte length of a GIF color table for the given 3-bit size field:
 * 2^(N+1) entries of 3 bytes each.
 */
@Suppress("MagicNumber")
internal fun gifColorTableSizeBytes(colorTableSizeField: Int): Int =
    3 * (1 shl (colorTableSizeField + 1))
