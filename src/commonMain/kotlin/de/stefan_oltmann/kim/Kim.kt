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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.arw.ArwPreviewExtractor
import de.stefan_oltmann.kim.format.cr2.Cr2PreviewExtractor
import de.stefan_oltmann.kim.format.cr3.Cr3PreviewExtractor
import de.stefan_oltmann.kim.format.dng.DngPreviewExtractor
import de.stefan_oltmann.kim.format.gif.GifMetadataExtractor
import de.stefan_oltmann.kim.format.gif.GifUpdater
import de.stefan_oltmann.kim.format.jpeg.JpegMetadataExtractor
import de.stefan_oltmann.kim.format.jpeg.JpegUpdater
import de.stefan_oltmann.kim.format.jxl.JxlUpdater
import de.stefan_oltmann.kim.format.nef.NefPreviewExtractor
import de.stefan_oltmann.kim.format.png.PngMetadataExtractor
import de.stefan_oltmann.kim.format.png.PngUpdater
import de.stefan_oltmann.kim.format.raf.RafMetadataExtractor
import de.stefan_oltmann.kim.format.raf.RafPreviewExtractor
import de.stefan_oltmann.kim.format.rw2.Rw2PreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.webp.WebPUpdater
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.input.PrePendingByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter

/**
 * Main entry point for reading and writing image metadata.
 *
 * All read operations throw [ImageReadException] and all write operations
 * throw [ImageWriteException] on failure. These are the only exception
 * types that escape this API.
 */
public object Kim {

    internal var underUnitTesting: Boolean = false

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(bytes: ByteArray): MediaMetadata? =
        if (bytes.isEmpty())
            null
        else
            readMetadata(ByteArrayByteReader(bytes))

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        byteReader: ByteReader
    ): MediaMetadata? = tryWithImageReadException {

        byteReader.use {

            val headerBytes = it.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

            val mediaFormat = MediaFormat.detect(headerBytes) ?: return@use null

            val imageParser = ImageParser.forFormat(mediaFormat)
                ?: return@use MediaMetadata.createEmpty(mediaFormat)

            val newReader = PrePendingByteReader(it, headerBytes.toList())

            /*
             * We re-apply the MediaFormat here, because we don't want to report
             * "TIFF" for every TIFF-based RAW format like CR2.
             */
            return@use imageParser
                .parseMetadata(byteReader = newReader)
                .withMediaFormat(mediaFormat = mediaFormat)
        }
    }

    /**
     * Determines the file type based on file header and returns metadata bytes.
     *
     * Cloud services can not reliably tell the mime type, so we must determine it.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun extractMetadataBytes(
        byteReader: ByteReader
    ): Pair<MediaFormat?, ByteArray> = tryWithImageReadException {

        byteReader.use {

            val headerBytes = it.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

            val mediaFormat = MediaFormat.detect(headerBytes)

            val newReader = PrePendingByteReader(it, headerBytes.toList())

            return@use when (mediaFormat) {
                MediaFormat.JPEG -> mediaFormat to JpegMetadataExtractor.extractMetadataBytes(newReader)
                MediaFormat.PNG -> mediaFormat to PngMetadataExtractor.extractMetadataBytes(newReader)
                MediaFormat.RAF -> mediaFormat to RafMetadataExtractor.extractMetadataBytes(newReader)
                MediaFormat.GIF -> mediaFormat to GifMetadataExtractor.extractMetadataBytes(newReader)
                else -> mediaFormat to byteArrayOf()
            }
        }
    }

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        byteReader.use {

            val headerBytes = it.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

            val mediaFormat = MediaFormat.detect(headerBytes)

            val prePendingByteReader = PrePendingByteReader(it, headerBytes.toList())

            if (mediaFormat == MediaFormat.RAF)
                return@use RafPreviewExtractor.extractPreviewImage(prePendingByteReader)

            if (mediaFormat == MediaFormat.CR3)
                return@use Cr3PreviewExtractor.extractPreviewImage(prePendingByteReader)

            val reader = DefaultRandomAccessByteReader(prePendingByteReader)

            val tiffContents = TiffReader.read(reader)

            /**
             * *Note:* Olympus ORF is currently unsupported because the preview offset
             * is burried in the Olympus MakerNotes, which are currently not interpreted.
             */
            return@use when (mediaFormat) {

                MediaFormat.CR2 -> Cr2PreviewExtractor.extractPreviewImage(tiffContents, reader)

                MediaFormat.RW2 -> Rw2PreviewExtractor.extractPreviewImage(tiffContents, reader)

                MediaFormat.TIFF -> {

                    /* It can now be DNG, NEF or ARW. */
                    DngPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                    NefPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                    ArwPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                }

                else -> null
            }
        }
    }

    /**
     * Updates the file with the desired change.
     *
     * **Note**: This method is provided for convenience, but it's not recommended for
     * very large image files that should not be entirely loaded into memory.
     * Currently, the update logic reads the entire file, which may not be efficient
     * for large files. Please be aware that this behavior is subject to change in
     * future updates.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        bytes: ByteArray,
        update: MetadataUpdate
    ): ByteArray = tryWithImageWriteException {

        val byteArrayByteWriter = ByteArrayByteWriter()

        update(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteArrayByteWriter,
            update = update
        )

        return@tryWithImageWriteException byteArrayByteWriter.toByteArray()
    }

    /**
     * Updates the file with the desired change.
     *
     * **Note**: We don't have an good API for single-shot write all fields right now.
     * So this is inefficent at this time as it reads the whole file in.
     *
     * But this already represents the planned future API for streaming updates.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ): Unit = tryWithImageWriteException {

        val headerBytes = byteReader.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val mediaFormat = MediaFormat.detect(headerBytes)

        val prePendingByteReader = PrePendingByteReader(byteReader, headerBytes.toList())

        return@tryWithImageWriteException when (mediaFormat) {
            MediaFormat.JPEG -> JpegUpdater.update(prePendingByteReader, byteWriter, update)
            MediaFormat.PNG -> PngUpdater.update(prePendingByteReader, byteWriter, update)
            MediaFormat.WEBP -> WebPUpdater.update(prePendingByteReader, byteWriter, update)
            MediaFormat.JXL -> JxlUpdater.update(prePendingByteReader, byteWriter, update)
            MediaFormat.GIF -> GifUpdater.update(prePendingByteReader, byteWriter, update)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't embed metadata into $mediaFormat.")
        }
    }

    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        val mediaFormat = MediaFormat.detect(bytes)

        return@tryWithImageWriteException when (mediaFormat) {
            MediaFormat.JPEG -> JpegUpdater.updateThumbnail(bytes, thumbnailBytes)
            MediaFormat.PNG -> PngUpdater.updateThumbnail(bytes, thumbnailBytes)
            MediaFormat.WEBP -> WebPUpdater.updateThumbnail(bytes, thumbnailBytes)
            MediaFormat.JXL -> JxlUpdater.updateThumbnail(bytes, thumbnailBytes)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't embed thumbnail into $mediaFormat.")
        }
    }
}
