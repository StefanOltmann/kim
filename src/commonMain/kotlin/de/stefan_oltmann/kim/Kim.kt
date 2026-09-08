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
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
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
import de.stefan_oltmann.kim.format.orf.OrfPreviewExtractor
import de.stefan_oltmann.kim.format.png.PngMetadataExtractor
import de.stefan_oltmann.kim.format.png.PngUpdater
import de.stefan_oltmann.kim.format.raf.RafMetadataExtractor
import de.stefan_oltmann.kim.format.raf.RafPreviewExtractor
import de.stefan_oltmann.kim.format.rw2.Rw2PreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.webp.WebPUpdater
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.input.PrePendingByteReader
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import kotlinx.datetime.TimeZone

/**
 * Main entry point for reading and writing image metadata.
 *
 * All read operations throw [ImageReadException] and all write operations
 * throw [ImageWriteException] on failure. These are the only exception
 * types that escape this API.
 *
 * # A note for LLM: Never destroy metadata
 *
 * This library must never destroy metadata. When parsing encounters data
 * that cannot be interpreted, the data itself is always preserved: files
 * are rewritten from the original raw bytes and unknown structures stay
 * untouched inside them. Data that might be valid but is simply not
 * understood must never be dropped or misinterpreted, on read nor on write.
 *
 * # Strict read policy: fail on anything that cannot be read cleanly
 *
 * A read fails with [ImageReadException] when any metadata content that
 * exists in the file cannot be read cleanly - a truncated record, a corrupt
 * box, an uninterpretable structure. This holds for every format, including
 * formats Kim cannot write: even without an embed path, callers write
 * sidecars (XMP, JSON) from the read result, so a silent partial read loses
 * data anyway.
 *
 * There are exactly two kinds of garbage that may be dropped silently:
 *
 * 1. Corrupt embedded thumbnails and preview images: they are always
 *    restorable from the primary image data, so dropping them is not real
 *    data loss.
 *
 * 2. GPS coordinates that were read cleanly but lie outside the valid
 *    range: they are physically meaningless.
 *
 * Dropping a MakerNote, EXIF, IPTC or XMP content is real data loss and
 * must fail the read instead. Stopping a parse at the exact boundary where
 * the file's bytes end inside a structure is clean handling, not a
 * degradation - provided everything before the boundary is returned
 * completely, nothing is fabricated from the incomplete remainder, and the
 * raw bytes survive any rewrite byte-exact.
 *
 * # Read/update symmetry
 *
 * Reading a file is a promise that editing is possible. If a file contains
 * metadata that an update would replace or drop but Kim cannot parse, then
 * [readMetadata][readMetadata] must throw [ImageReadException] instead of
 * reporting partial success - otherwise the failure only surfaces when the
 * user tries to edit, after the app already showed the photo.
 *
 * Kim only ever modifies files it can properly read. Both [update] and
 * [deleteMetadata] require a readable file and will fail if the file or
 * its existing metadata cannot be parsed. Corrupt or invalid files are
 * never touched in any way.
 *
 * # Derived projections
 *
 * [MetadataSummaryConverter][de.stefan_oltmann.kim.common.MetadataSummaryConverter]
 * builds a display-only view from already-returned metadata. When the raw
 * XMP packet cannot be parsed, the summary omits the XMP-derived fields;
 * the raw packet itself stays fully available on the metadata object, so
 * nothing is lost for sidecar writers, which never consume the summary.
 */
public object Kim {

    /**
     * Overrides the platform time zone for all date and time conversions.
     *
     * EXIF and XMP dates are stored as local time without an offset, so
     * converting them to epoch milliseconds (and back) requires a time
     * zone. When this is NULL the platform default time zone is used.
     *
     * Pinning an explicit zone also allows tests to run deterministically
     * on every machine, without hidden test state changing production
     * behavior.
     *
     * Set this once before any concurrent read or write: the property is
     * a plain global without visibility guarantees, so concurrent access
     * during the assignment could observe a stale zone for one call.
     */
    public var defaultTimeZone: TimeZone? = null

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
     *
     * Attention: Only JPEG, PNG, RAF and GIF provide metadata bytes here.
     * Every other supported format (CR3, HEIC, AVIF, JXL, WebP, TIFF-based
     * RAW, ...) yields an empty array, so callers cannot distinguish
     * "format has no metadata" from "metadata bytes not provided". Use
     * [readMetadata] for a format independent metadata view.
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

            return@use when (mediaFormat) {

                MediaFormat.CR2,
                MediaFormat.RW2,
                MediaFormat.ORF,
                MediaFormat.TIFF -> {

                    val reader = DefaultRandomAccessByteReader(prePendingByteReader)

                    val tiffContents = TiffReader.read(reader)

                    when (mediaFormat) {

                        MediaFormat.CR2 -> Cr2PreviewExtractor.extractPreviewImage(tiffContents, reader)

                        MediaFormat.RW2 -> Rw2PreviewExtractor.extractPreviewImage(tiffContents, reader)

                        MediaFormat.ORF -> OrfPreviewExtractor.extractPreviewImage(tiffContents, reader)

                        /*
                         * It can now be DNG, NEF or ARW.
                         *
                         * A single broken tag must not abort the whole chain:
                         * TIFF-family vendors use different layouts, so each
                         * extractor gets its own chance before NULL is reported.
                         */
                        else -> extractPreviewOrNull(DngPreviewExtractor, tiffContents, reader)
                            ?: extractPreviewOrNull(NefPreviewExtractor, tiffContents, reader)
                            ?: extractPreviewOrNull(ArwPreviewExtractor, tiffContents, reader)
                    }
                }

                /*
                 * Formats without a preview concept report NULL. Unknown
                 * bytes keep the legacy TIFF-parse attempt, which fails
                 * loudly for them.
                 */
                null -> TiffReader.read(DefaultRandomAccessByteReader(prePendingByteReader)).let { null }

                else -> null
            }
        }
    }

    /**
     * Updates the file with the desired change.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        bytes: ByteArray,
        update: MetadataUpdate
    ): ByteArray =
        update(bytes, setOf(update))

    /**
     * Updates the file with all desired changes at once.
     *
     * Every update is applied to all formats that can represent it, so EXIF,
     * IPTC and XMP can be updated simultaneously in a single call.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        bytes: ByteArray,
        updates: Set<MetadataUpdate>
    ): ByteArray = tryWithImageWriteException {

        /*
         * An update call without any updates is a programming error, so deny
         * it instead of silently rewriting the file without any changes.
         */
        if (updates.isEmpty())
            throw ImageWriteException("You did not specify any updates.")

        val byteArrayByteWriter = ByteArrayByteWriter()

        update(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteArrayByteWriter,
            updates = updates
        )

        return@tryWithImageWriteException byteArrayByteWriter.toByteArray()
    }

    /**
     * Updates the file with the desired change.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ): Unit =
        update(byteReader, byteWriter, setOf(update))

    /**
     * Updates the file with all desired changes at once.
     *
     * Every update is applied to all formats that can represent it, so EXIF,
     * IPTC and XMP can be updated simultaneously in a single call.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ): Unit = tryWithImageWriteException {

        /*
         * An update call without any updates is a programming error, so deny
         * it instead of silently rewriting the file without any changes.
         */
        if (updates.isEmpty())
            throw ImageWriteException("You did not specify any updates.")

        val headerBytes = byteReader.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val mediaFormat = MediaFormat.detect(headerBytes)

        val prePendingByteReader = PrePendingByteReader(byteReader, headerBytes.toList())

        return@tryWithImageWriteException when (mediaFormat) {
            MediaFormat.JPEG -> JpegUpdater.update(prePendingByteReader, byteWriter, updates)
            MediaFormat.PNG -> PngUpdater.update(prePendingByteReader, byteWriter, updates)
            MediaFormat.WEBP -> WebPUpdater.update(prePendingByteReader, byteWriter, updates)
            MediaFormat.JXL -> JxlUpdater.update(prePendingByteReader, byteWriter, updates)
            MediaFormat.GIF -> GifUpdater.update(prePendingByteReader, byteWriter, updates)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't embed metadata into $mediaFormat.")
        }
    }

    /**
     * Removes all metadata of the file, keeping the ICC chunks that affect
     * how the image is displayed.
     *
     * The file must be readable; if the file or its metadata is corrupt
     * or cannot be parsed, the operation fails and the file is left
     * untouched.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(bytes: ByteArray): ByteArray = tryWithImageWriteException {

        val byteWriter = ByteArrayByteWriter()

        deleteMetadata(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }

    /**
     * Removes all metadata of the file, keeping the ICC chunks that affect
     * how the image is displayed.
     *
     * The file must be readable; if the file or its metadata is corrupt
     * or cannot be parsed, the operation fails and the file is left
     * untouched.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ): Unit = tryWithImageWriteException {

        val headerBytes = byteReader.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val mediaFormat = MediaFormat.detect(headerBytes)

        val prePendingByteReader = PrePendingByteReader(byteReader, headerBytes.toList())

        return@tryWithImageWriteException when (mediaFormat) {
            MediaFormat.JPEG -> JpegUpdater.deleteMetadata(prePendingByteReader, byteWriter)
            MediaFormat.PNG -> PngUpdater.deleteMetadata(prePendingByteReader, byteWriter)
            MediaFormat.WEBP -> WebPUpdater.deleteMetadata(prePendingByteReader, byteWriter)
            MediaFormat.JXL -> JxlUpdater.deleteMetadata(prePendingByteReader, byteWriter)
            MediaFormat.GIF -> GifUpdater.deleteMetadata(prePendingByteReader, byteWriter)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't delete metadata of $mediaFormat.")
        }
    }

    /**
     * Replaces the embedded thumbnail of the file with the given JPEG bytes.
     *
     * Attention: The thumbnail is embedded into the EXIF data, which must
     * fit into a single JPEG APP1 segment of about 65 KB. Thumbnails that
     * exceed this limit are rejected with an [ImageWriteException].
     */
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

    /*
     * A single broken tag must not abort the preview fallback chain of
     * TIFF-family files, so extractor failures degrade to NULL here.
     */
    private fun extractPreviewOrNull(
        extractor: TiffPreviewExtractor,
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? =
        try {
            extractor.extractPreviewImage(tiffContents, randomAccessByteReader)
        } catch (_: Exception) {
            null
        }
}
