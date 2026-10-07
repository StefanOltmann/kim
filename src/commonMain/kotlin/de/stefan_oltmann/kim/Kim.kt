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
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
import de.stefan_oltmann.kim.format.arw.ArwPreviewExtractor
import de.stefan_oltmann.kim.format.cr2.Cr2PreviewExtractor
import de.stefan_oltmann.kim.format.cr3.Cr3PreviewExtractor
import de.stefan_oltmann.kim.format.dng.DngPreviewExtractor
import de.stefan_oltmann.kim.format.gif.GifMetadataExtractor
import de.stefan_oltmann.kim.format.jpeg.JpegImageParser
import de.stefan_oltmann.kim.format.jpeg.JpegMetadataExtractor
import de.stefan_oltmann.kim.format.nef.NefPreviewExtractor
import de.stefan_oltmann.kim.format.orf.OrfPreviewExtractor
import de.stefan_oltmann.kim.format.png.PngMetadataExtractor
import de.stefan_oltmann.kim.format.raf.RafMetadataExtractor
import de.stefan_oltmann.kim.format.raf.RafPreviewExtractor
import de.stefan_oltmann.kim.format.rw2.Rw2PreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
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
import kotlin.coroutines.cancellation.CancellationException

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
 * There are exactly five kinds of garbage that may be dropped silently:
 *
 * 1. Corrupt embedded thumbnails and preview images: they are always
 *    restorable from the primary image data, so dropping them is not real
 *    data loss.
 *
 * 2. GPS coordinates that were read cleanly but lie outside the valid
 *    range: they are physically meaningless.
 *
 * 3. Illegal EXIF date/time values and GPS data with a wrong type or
 *    unknown references: the values exist but are unusable, so the derived
 *    summary omits them instead of failing the whole read. This drop
 *    happens at summary level only - the raw values stay untouched on the
 *    metadata object, so nothing is lost for tools that parse them
 *    themselves.
 *
 * 4. Orphan Adobe extended-XMP segments in JPEG files whose GUID is not
 *    referenced by any standard packet: their content is undecodable
 *    without the lost main packet (ExifTool ignores them as well), so the
 *    read skips them and an XMP-writing update removes their bytes. This
 *    only covers the unreferenced extension chunks - a truncated packet
 *    that IS referenced fails the read like any other unreadable content.
 *
 * 5. Adobe's legal partial date forms ("2023", "2023-05") in XMP: they
 *    are valid values, but the summary's epoch-millis model cannot
 *    represent them without fabricating a month or day, so the derived
 *    summary omits them. Like category 3, this drop happens at summary
 *    level only - the raw packet stays untouched on the metadata object.
 *
 * Dropping a MakerNote, EXIF, IPTC, or XMP content is real data loss and
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
 * XMP packet cannot be parsed, the conversion fails with
 * [ImageReadException][de.stefan_oltmann.kim.common.ImageReadException] by
 * default; with `ignoreBrokenXmp = true` the summary omits the XMP-derived
 * fields instead. The raw packet itself stays fully available on the
 * metadata object in both cases, so nothing is lost for sidecar writers,
 * which never consume the summary.
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

    /**
     * The time zone all date and time conversions use: the explicitly
     * pinned [defaultTimeZone], or the platform default when none was
     * set.
     */
    internal val effectiveTimeZone: TimeZone
        get() = defaultTimeZone ?: TimeZone.currentSystemDefault()

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(bytes: ByteArray): MediaMetadata? =
        readMetadata(bytes = bytes, readTrailerMetadata = false)

    /**
     * Reads all metadata of the image.
     *
     * With `readTrailerMetadata = true` the APP1 EXIF and XMP segments
     * that some tools write behind the JPEG image data are reported as
     * well. Without the flag the read stops at the image data, which
     * keeps the historical behavior for files whose trailer belongs to
     * another tool.
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        bytes: ByteArray,
        readTrailerMetadata: Boolean
    ): MediaMetadata? =
        if (bytes.isEmpty())
            null
        else
            readMetadata(
                byteReader = ByteArrayByteReader(bytes),
                readTrailerMetadata = readTrailerMetadata
            )

    /**
     * Reads all metadata of the image.
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(byteReader: ByteReader): MediaMetadata? =
        readMetadata(byteReader = byteReader, readTrailerMetadata = false)

    /**
     * Reads all metadata of the image.
     *
     * With `readTrailerMetadata = true` the APP1 EXIF and XMP segments
     * that some tools write behind the JPEG image data are reported as
     * well. Formats without a trailer concept ignore the flag.
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        byteReader: ByteReader,
        readTrailerMetadata: Boolean
    ): MediaMetadata? = tryWithImageReadException {

        byteReader.use {

            val detection = detectFormatAndReplayHeader(it)

            val mediaFormat = detection.mediaFormat

            if (mediaFormat == null) {

                /*
                 * A BigTIFF header is not unknown bytes: the documented
                 * rule is that it keeps failing the read, which
                 * TiffReader cannot enforce when the facade never
                 * forwards the file to it.
                 */
                TiffReader.rejectBigTiffHeader(detection.headerBytes)

                return@use null
            }

            val imageParser = ImageParser.forFormat(mediaFormat)
                ?: return@use MediaMetadata.createEmpty(mediaFormat)

            /*
             * We re-apply the MediaFormat here, because we don't want to report
             * "TIFF" for every TIFF-based RAW format like CR2.
             */
            return@use (
                if (readTrailerMetadata && mediaFormat == MediaFormat.JPEG)
                    JpegImageParser.parseMetadata(detection.reader, readTrailerMetadata = true)
                else
                    imageParser.parseMetadata(byteReader = detection.reader)
                ).withMediaFormat(mediaFormat = mediaFormat)
        }
    }

    /**
     * Determines the file type based on file header and returns metadata bytes.
     *
     * Cloud services can not reliably tell the mime type, so we must determine it.
     *
     * Attention: Only JPEG, PNG, RAF, and GIF provide metadata bytes here.
     * Every other supported format (CR3, HEIC, AVIF, JXL, WebP, TIFF-based
     * RAW, ...) yields an empty array, so callers cannot distinguish
     * "format has no metadata" from "metadata bytes not provided". Use
     * [readMetadata] for a format independent metadata view.
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun extractMetadataBytes(
        byteReader: ByteReader
    ): Pair<MediaFormat?, ByteArray> = tryWithImageReadException {

        byteReader.use {

            val detection = detectFormatAndReplayHeader(it)

            if (detection.mediaFormat == null) {

                /* Same rule as in readMetadata: BigTIFF fails, the rest is unknown. */
                TiffReader.rejectBigTiffHeader(detection.headerBytes)

                return@use null to byteArrayOf()
            }

            val mediaFormat = detection.mediaFormat

            return@use when (mediaFormat) {
                MediaFormat.JPEG -> mediaFormat to JpegMetadataExtractor.extractMetadataBytes(detection.reader)
                MediaFormat.PNG -> mediaFormat to PngMetadataExtractor.extractMetadataBytes(detection.reader)
                MediaFormat.RAF -> mediaFormat to RafMetadataExtractor.extractMetadataBytes(detection.reader)
                MediaFormat.GIF -> mediaFormat to GifMetadataExtractor.extractMetadataBytes(detection.reader)
                else -> mediaFormat to byteArrayOf()
            }
        }
    }

    /**
     * Extracts the embedded preview image of the file.
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        byteReader.use {

            val detection = detectFormatAndReplayHeader(it)

            val mediaFormat = detection.mediaFormat

            return@use when (mediaFormat) {

                MediaFormat.RAF ->
                    RafPreviewExtractor.extractPreviewImage(detection.reader)

                MediaFormat.CR3 ->
                    Cr3PreviewExtractor.extractPreviewImage(detection.reader)

                MediaFormat.CR2,
                MediaFormat.RW2,
                MediaFormat.ORF,
                MediaFormat.TIFF -> {

                    val reader = DefaultRandomAccessByteReader(detection.reader)

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
                null -> {
                    TiffReader.read(DefaultRandomAccessByteReader(detection.reader))

                    null
                }

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
     * IPTC, and XMP are updated together in the same write. The storages
     * duplicate the same logical values, so updating only one of them would
     * let the copies drift apart - see [de.stefan_oltmann.kim.format.MetadataUpdater].
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
     *
     * Attention: The given [ByteReader] and [ByteWriter] are not closed by
     * this call; the caller owns and closes both.
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
     * IPTC, and XMP are updated together in the same write. The storages
     * duplicate the same logical values, so updating only one of them would
     * let the copies drift apart - see [de.stefan_oltmann.kim.format.MetadataUpdater].
     *
     * Attention: The given [ByteReader] and [ByteWriter] are not closed by
     * this call; the caller owns and closes both.
     *
     * Attention: The source file is always left untouched, but some
     * formats stream their image data to the [ByteWriter] before the
     * write is known to succeed. When this call throws
     * [ImageWriteException], any bytes already written are incomplete
     * and must be discarded - stage the output in a temporary buffer or
     * file and publish it only after the call returned normally.
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

        val detection = detectFormatAndReplayHeader(byteReader)

        if (detection.mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        /*
         * GIF can carry XMP but has no EXIF, IPTC or thumbnail concept, so
         * its updater answers those calls itself with a targeted error.
         */
        val updater = MetadataUpdater.forFormat(detection.mediaFormat)
            ?: throw ImageWriteException("Can't embed metadata into ${detection.mediaFormat}.")

        updater.update(detection.reader, byteWriter, updates)
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
     *
     * Attention: The given [ByteReader] and [ByteWriter] are not closed by
     * this call; the caller owns and closes both.
     *
     * Attention: The source file is always left untouched, but some
     * formats stream their image data to the [ByteWriter] before the
     * write is known to succeed. When this call throws
     * [ImageWriteException], any bytes already written are incomplete
     * and must be discarded - stage the output in a temporary buffer or
     * file and publish it only after the call returned normally.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ): Unit = tryWithImageWriteException {

        val detection = detectFormatAndReplayHeader(byteReader)

        if (detection.mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        val updater = MetadataUpdater.forFormat(detection.mediaFormat)
            ?: throw ImageWriteException("Can't delete metadata of ${detection.mediaFormat}.")

        updater.deleteMetadata(detection.reader, byteWriter)
    }

    /**
     * Replaces the embedded thumbnail of the file with the given bytes.
     *
     * Attention: The thumbnail is embedded into the EXIF data. On JPEG
     * it must fit into a single APP1 segment of about 65 KB; on PNG and
     * WebP the eXIf chunk bound applies. Thumbnails that exceed the
     * format's limit are rejected with an [ImageWriteException]. The
     * bytes are embedded as-is: they should be a JPEG image, since EXIF
     * thumbnails are JPEG, but only non-emptiness is validated here.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        val mediaFormat = MediaFormat.detect(bytes)

        if (mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        val updater = MetadataUpdater.forFormat(mediaFormat)
            ?: throw ImageWriteException("Can't embed thumbnail into $mediaFormat.")

        return@tryWithImageWriteException updater.updateThumbnail(bytes, thumbnailBytes)
    }

    /*
     * A single broken tag must not abort the preview fallback chain of
     * TIFF-family files, so extractor failures degrade to NULL here.
     * A cancellation is not a broken format: swallowing it would turn a
     * cancelled call into a neutral "no preview", so it propagates.
     */
    private fun extractPreviewOrNull(
        extractor: TiffPreviewExtractor,
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? =
        try {
            extractor.extractPreviewImage(tiffContents, randomAccessByteReader)
        } catch (ex: CancellationException) {
            throw ex
        } catch (_: Exception) {
            null
        }

    /**
     * The outcome of reading the head of a stream: the detected media
     * format - NULL for unknown bytes - the consumed header bytes, so
     * callers can classify special unknown signatures like BigTIFF, and
     * a reader that replays the consumed bytes, so the format parsers
     * see the complete stream again.
     */
    private class DetectedFormat(
        val mediaFormat: MediaFormat?,
        val headerBytes: ByteArray,
        val reader: ByteReader
    )

    /**
     * Reads the head of the stream and detects the media format from it.
     */
    private fun detectFormatAndReplayHeader(
        byteReader: ByteReader
    ): DetectedFormat {

        val headerBytes = byteReader.readBytes(MediaFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val mediaFormat = MediaFormat.detect(headerBytes)

        return DetectedFormat(
            mediaFormat = mediaFormat,
            headerBytes = headerBytes,
            reader = PrePendingByteReader(byteReader, headerBytes.toList())
        )
    }
}
