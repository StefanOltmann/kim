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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.gif.chunk.GifChunk
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkApplicationExtension
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkHeader
import de.stefan_oltmann.kim.format.gif.chunk.GifChunkImageDescriptor
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.transferExactly
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeString

/**
 * Writes GIF files.
 */
public object GifWriter {

    /* Left, top, width, height and the packed field. */
    private const val IMAGE_DESCRIPTOR_LENGTH: Int = 9

    /* The application identifier of a GIF application extension is 8 bytes. */
    private const val APPLICATION_IDENTIFIER_LENGTH: Int = 8

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
        failOnTrailingXmp: Boolean = false,
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

        /*
         * The first image block is still open, so it must be completed
         * before the following blocks can be told apart.
         */
        copyImageData(byteReader, byteWriter)

        /*
         * The walker rejects unknown introducers and unexpected EOFs, so
         * a rewrite can never emit a shifted or silently truncated file.
         */
        byteReader.walkGifBlocks(
            onImageBlock = {
                byteWriter.write(GifConstants.IMAGE_SEPARATOR)
                copyImageData(byteReader, byteWriter)
                false
            },
            onExtensionBlock = { extensionLabel ->
                copyExtensionBlock(byteReader, byteWriter, extensionLabel, failOnTrailingXmp)
                false
            },
            onTrailerBlock = {
                byteWriter.write(GifConstants.GIF_TERMINATOR)
                true
            }
        )
    }

    /**
     * Copies an image block body including its local color table and LZW
     * image data sub-blocks verbatim, so the next block boundary stays
     * aligned. The caller has written the image separator already.
     */
    private fun copyImageData(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) {

        /* Left, top, width, height and the packed field. */
        val descriptorBytes = byteReader.readBytes(IMAGE_DESCRIPTOR_LENGTH)

        /*
         * A truncated image descriptor means the file is corrupt and must
         * not be rewritten, because that would silently drop the image.
         */
        if (descriptorBytes.size < IMAGE_DESCRIPTOR_LENGTH)
            throw ImageReadException(
                "Truncated GIF image descriptor: ${descriptorBytes.size} of " +
                    "$IMAGE_DESCRIPTOR_LENGTH bytes."
            )

        val imageDescriptorChunk = GifChunkImageDescriptor(
            byteArrayOf(GifConstants.IMAGE_SEPARATOR) + descriptorBytes
        )

        byteWriter.write(descriptorBytes)

        /* Bit 7 signals a local color table, bits 0 to 2 its size. */
        if (imageDescriptorChunk.localColorTableFlag) {

            val localColorTableSize = gifColorTableSizeBytes(imageDescriptorChunk.localColorTableSize)

            byteReader.transferExactly(byteWriter, localColorTableSize.toLong())
        }

        /* The LZW minimum code size byte precedes the sub-block chain. */
        val lzwMinimumCodeSize = byteReader.readByte()

        if (lzwMinimumCodeSize != null)
            byteWriter.write(lzwMinimumCodeSize)

        byteReader.transferGifSubBlocks(byteWriter)
    }

    /**
     * Copies an extension block, dropping comment extensions and XMP
     * application extensions, which carry user-editable metadata. All
     * other blocks, including unknown extensions, stream through
     * untouched, so stale metadata cannot survive an update or deletion
     * while nothing the caller asked to keep is destroyed.
     */
    private fun copyExtensionBlock(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        extensionLabel: Byte,
        failOnTrailingXmp: Boolean
    ) {

        when (extensionLabel) {

            GifConstants.COMMENT_EXTENSION_LABEL -> byteReader.transferGifSubBlocks(byteWriter = null)

            GifConstants.APPLICATION_EXTENSION_LABEL ->
                copyApplicationExtensionBlock(byteReader, byteWriter, failOnTrailingXmp)

            else -> {

                byteWriter.write(GifConstants.EXTENSION_INTRODUCER)
                byteWriter.write(extensionLabel)

                byteReader.transferGifSubBlocks(byteWriter)
            }
        }
    }

    /**
     * Copies an application extension, dropping the XMP one. The first
     * sub-block carries the application identifier, which decides between
     * the XMP extension that must be dropped and extensions such as the
     * NETSCAPE looping extension that must be kept.
     */
    private fun copyApplicationExtensionBlock(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        failOnTrailingXmp: Boolean
    ) {

        byteWriter.write(GifConstants.EXTENSION_INTRODUCER)
        byteWriter.write(GifConstants.APPLICATION_EXTENSION_LABEL)

        val firstSubBlockSizeByte = byteReader.readByte()
            ?: throw ImageReadException("Unexpected end of file behind a GIF application extension.")

        val firstSubBlockSize = firstSubBlockSizeByte.toUInt8()

        /* An empty chain cannot identify an application, so it is kept. */
        if (firstSubBlockSize == 0) {

            byteWriter.write(firstSubBlockSizeByte)

            return
        }

        val identifierBytes =
            byteReader.readBytes(minOf(firstSubBlockSize, APPLICATION_IDENTIFIER_LENGTH))

        val isXmpExtension =
            identifierBytes.size == APPLICATION_IDENTIFIER_LENGTH &&
                identifierBytes.decodeToString() == GifConstants.XMP_APPLICATION_IDENTIFIER

        /*
         * The identifier bytes were consumed in both branches, so only the
         * remainder of the first sub-block is transferred here.
         */
        val remainingFirstSubBlockLength = (firstSubBlockSize - identifierBytes.size).toLong()

        if (isXmpExtension) {

            /*
             * XMP behind the first frame was never seen by the update
             * computer (it only receives the chunks before the first
             * image), so dropping it here would silently destroy the
             * metadata. An update must fail loudly instead; a deletion
             * asked for the removal and keeps skipping. Byte-array callers
             * simply discard their buffered output; streaming callers must
             * discard what was written so far.
             */
            if (failOnTrailingXmp)
                throw ImageWriteException(
                    "The update cannot merge the XMP application extension " +
                        "behind the first frame. The source file was not " +
                        "modified, but the output written so far is incomplete " +
                        "and must be discarded."
                )

            byteReader.transferExactly(null, remainingFirstSubBlockLength)
            byteReader.transferGifSubBlocks(byteWriter = null)

        } else {

            byteWriter.write(firstSubBlockSizeByte)
            byteWriter.write(identifierBytes)
            byteReader.transferExactly(byteWriter, remainingFirstSubBlockLength)
            byteReader.transferGifSubBlocks(byteWriter)
        }
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

        /*
         * The XMP is anchored ahead of the first image descriptor. A GIF
         * without image data therefore has no place for it - and because
         * all chunks are in memory here, the failure is detected before
         * any output is written.
         */
        if (xmp != null && modifiedChunks.none { it.type == GifChunkType.IMAGE_DESCRIPTOR })
            throw ImageWriteException(
                "GIF file has no image data, so the requested XMP cannot be written."
            )

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
