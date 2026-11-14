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
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
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
 * There are exactly seven kinds of garbage that may be dropped silently:
 *
 * 1. Corrupt embedded thumbnails and preview images: they are always
 *    restorable from the primary image data, so dropping them is not real
 *    data loss. This holds only for formats Kim can rewrite: the preview
 *    extractors of the read-only RAW formats (CR3, CR2, RAF, NEF, ARW,
 *    RW2, ORF, DNG) deliberately fail on broken previews instead, because
 *    there the preview is the only way applications can display the file.
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
 * 6. Ordinary fields inside metadata-bearing directories whose value is
 *    not completely readable: an impossible count or value offset
 *    (corruption), or a value that the end of the data cuts off (a
 *    boundary truncation). ExifTool drops such a field with a warning
 *    ("Error reading value for ...") while the rest of the file reads,
 *    and a value that is not completely present cannot be returned
 *    completely. This exception never covers the MakerNote or the offset
 *    fields that carry the Exif, GPS and Interop sub-IFDs - a rewrite
 *    cannot afford to lose those, so they still fail the read.
 *
 * 7. Uninterpretable text values (UserComment, GPS text) with a malformed
 *    encoding or a hostile non-byte type: instead of failing the read,
 *    the decoded text is dropped - not the bytes - and the value degrades
 *    to its raw bytes on the field object. A rewrite writes the field
 *    from those bytes, byte-exact. ExifTool reports the same bytes with a
 *    warning ("Invalid EXIF text encoding for UserComment").
 *
 * Dropping a MakerNote, EXIF, IPTC, or XMP content is real data loss and
 * must fail the read instead. Stopping a parse at the exact boundary where
 * the file's bytes end inside a structure is clean handling, not a
 * degradation - provided everything before the boundary is returned
 * completely, nothing is fabricated from the incomplete remainder, and the
 * raw bytes survive any rewrite byte-exact.
 *
 * # Blocks that are not interpreted on purpose
 *
 * The Print Image Matching block (EXIF tag 0xC4A5) and the Multi-Picture
 * Format block (the JPEG APP2 "MPF\0" segment) are legacy structures that
 * many cameras write faulty. Interpreting them fails whole photos for
 * garbage the camera itself produced, so Kim deliberately leaves them
 * unparsed: they are transferred byte-exact on write, and
 * [deleteMetadata] removes them with the rest of the metadata.
 *
 * # Read/update symmetry
 *
 * Reading a file is a promise that editing is possible. If a file contains
 * metadata that an update would replace or drop but Kim cannot parse, then
 * [readMetadata][readMetadata] must throw [ImageReadException] instead of
 * reporting partial success - otherwise the failure only surfaces when the
 * user tries to edit, after the app already showed the photo.
 *
 * Kim only ever modifies files it can properly read. [update] requires a
 * readable file and will fail if the file or its existing metadata cannot
 * be parsed. [deleteMetadata] requires a readable file structure and - for
 * the orientation preservation that is on by default - a parseable EXIF
 * payload, since the rotation has to be read from it before the removal;
 * every other metadata content is removed without being parsed, so a
 * corrupt XMP payload is dropped like a valid one instead of blocking the
 * cleanup. Structurally invalid files are never touched in any way.
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

    /**
     * Reads all metadata of the image.
     *
     * @param bytes The original file bytes to read.
     *
     * @return The parsed metadata, or NULL when the bytes are empty.
     */
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
     *
     * @param bytes The original file bytes to read.
     * @param readTrailerMetadata Whether the APP1 EXIF and XMP segments
     *        behind the JPEG image data are read as well.
     *
     * @return The parsed metadata, or NULL when the bytes are empty.
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
     *
     * @param byteReader The original file, streamed.
     *
     * @return The parsed metadata, or NULL for unknown bytes.
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
     *
     * @param byteReader The original file, streamed.
     * @param readTrailerMetadata Whether the APP1 EXIF and XMP segments
     *        behind the JPEG image data are read as well.
     *
     * @return The parsed metadata, or NULL for unknown bytes.
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
            val metadata =
                if (readTrailerMetadata && mediaFormat == MediaFormat.JPEG)
                    JpegImageParser.parseMetadata(detection.reader, readTrailerMetadata = true)
                else
                    imageParser.parseMetadata(byteReader = detection.reader)

            /*
             * NEF, ARW and DNG files carry the plain TIFF magic, so the
             * header detection labels them TIFF. The parsed structure
             * identifies them - the DNGVersion tag marks a DNG, the
             * vendor maker notes mark NEF and ARW - like ExifTool
             * reports the specific format for the same bytes.
             */
            val reportedFormat =
                if (mediaFormat == MediaFormat.TIFF)
                    refineTiffFormat(metadata) ?: mediaFormat
                else
                    mediaFormat

            return@use metadata.withMediaFormat(mediaFormat = reportedFormat)
        }
    }

    /**
     * Refines a TIFF-detected metadata result to the specific TIFF-based
     * format, or returns NULL when the structure identifies no specific
     * format: the DNGVersion tag marks a DNG, a Nikon or Sony maker note
     * marks NEF or ARW.
     */
    private fun refineTiffFormat(metadata: MediaMetadata): MediaFormat? {

        val exif = metadata.exif ?: return null

        if (metadata.findTiffField(TiffTag.TIFF_TAG_DNG_VERSION) != null)
            return MediaFormat.DNG

        val hasMakerNote = exif.makerNoteDirectory != null

        val make = metadata.findStringValue(TiffTag.TIFF_TAG_MAKE)

        if (hasMakerNote && make?.startsWith("NIKON", ignoreCase = true) == true)
            return MediaFormat.NEF

        if (hasMakerNote && make?.startsWith("SONY", ignoreCase = true) == true)
            return MediaFormat.ARW

        return null
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
     *
     * @param byteReader The original file, streamed.
     *
     * @return The detected media format and its metadata bytes - the
     *         bytes are empty for the formats listed above.
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
     * Returns NULL when the file carries no preview at all. A preview
     * that exists but is broken (wrong marker, truncated data, content
     * that is not a JPEG) fails with [ImageReadException] instead:
     * RAW files are never written by this library, so their preview is
     * the only way applications can display them. The "corrupt previews
     * are restorable from the primary image" whitelist of [readMetadata]
     * therefore does not apply here - silently returning NULL would hide
     * a damaged file behind an empty image slot. This holds for every
     * RAW format (CR3, CR2, RAF, NEF, ARW, RW2, ORF, DNG).
     *
     * Attention: The given [ByteReader] is closed by this call, including
     * the stream below it, and must not be used afterwards.
     *
     * @param byteReader The original file, streamed.
     *
     * @return The preview image bytes, or NULL when the file carries no
     *         preview at all.
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
     *
     * @param bytes The original file bytes to modify.
     * @param update The change to apply.
     *
     * @return The modified file bytes.
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
     *
     * @param bytes The original file bytes to modify.
     * @param updates The changes to apply. Must not be empty.
     *
     * @return The modified file bytes.
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
     *
     * @param byteReader The original file, streamed.
     * @param byteWriter The destination the modified file is written to.
     * @param update The change to apply.
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
     * Attention: JPEG metadata segments behind the image data (see
     * [readMetadata][readMetadata]) are left untouched: the trailer can
     * belong to another tool, so an update rewrites the header metadata
     * only and copies the trailer verbatim. A stale trailer copy stays
     * visible through `readTrailerMetadata = true`.
     *
     * Attention: JPEG metadata segments between the scans of a
     * progressive file are left untouched as well, byte-exact where they
     * sit - the region behind the first SOS marker is transferred without
     * interpretation, like the ExifTool binary keeps those segments on an
     * update. The fresh header copy the update writes comes first in the
     * file, so readers take it over the stale between-scan copy. A
     * [deleteMetadata] call removes the stale segments with the rest.
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
     *
     * @param byteReader The original file, streamed.
     * @param byteWriter The destination the modified file is written to.
     * @param updates The changes to apply. Must not be empty.
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

        /*
         * The combined position-and-location update writes both values as
         * part of one logical change, so a plain GpsCoordinates or
         * LocationShown update of the same set must not overwrite either
         * half - which one wins would otherwise depend on the set's
         * iteration order.
         */
        val effectiveUpdates: Set<MetadataUpdate> =
            if (updates.none { it is MetadataUpdate.GpsCoordinatesAndLocationShown })
                updates
            else
                updates.filterNot {
                    it is MetadataUpdate.GpsCoordinates || it is MetadataUpdate.LocationShown
                }.toSet()

        val detection = detectFormatAndReplayHeader(byteReader)

        if (detection.mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        /*
         * GIF can carry XMP but has no EXIF, IPTC or thumbnail concept, so
         * its updater answers those calls itself with a targeted error.
         */
        val updater = MetadataUpdater.forFormat(detection.mediaFormat)
            ?: throw ImageWriteException("Can't embed metadata into ${detection.mediaFormat}.")

        updater.update(detection.reader, byteWriter, effectiveUpdates)
    }

    /**
     * Removes all metadata of the file, keeping healthy ICC profiles that
     * affect how the image is displayed. For JPG, APP2 segments that do
     * not carry a healthy ICC profile are deleted as well, and a faulty
     * profile lets its chunks go instead of staying in the file.
     *
     * With [keepIccProfile] = false the ICC profile goes with the rest of
     * the metadata, so the result is clean of metadata entirely. Formats
     * without an ICC concept (GIF, JPEG XL) ignore the parameter.
     *
     * The Adobe APP14 segment stays in every mode: its ColorTransform
     * flag tells the decoder how to interpret the DCT data, and losing
     * it renders Adobe RGB and CMYK files with wrong colors - ExifTool
     * protects the segment from "-all=" for the same reason.
     *
     * With [keepOrientationFlag] an explicitly stored Exif orientation
     * value (1-8) survives the removal: the metadata container carrying
     * it is replaced in place by a minimal structure carrying only that
     * value, so the stored rotation is not lost with the rest of the
     * metadata. PNG also reads the orientation from the legacy text
     * representation ("Raw profile type exif") and modernizes it into
     * an eXIf chunk.
     *
     * A legacy representation behind the image data cannot be
     * modernized, because the stream has already passed the IDAT
     * chunks: the deletion drops it like every metadata chunk, and the
     * rotation it may carry is lost.
     *
     * An absent orientation writes nothing - per the specification an
     * absent tag means 1, and the deletion fabricates no tag the file
     * never stated. The metadata container carrying the orientation
     * must be parseable, so a corrupt EXIF payload fails the operation
     * instead of losing the rotation silently. Formats without an
     * orientation concept (GIF) ignore the parameter.
     *
     * For JPG the metadata segments behind the image data are removed as
     * well, and the file ends at the structural EOI marker: every byte
     * behind it belongs to other tools and is discarded. A JPG that ends
     * inside its image data without the structural EOI marker fails the
     * operation, because its image data cannot be separated from the
     * metadata behind it. For PNG the file ends at the structural IEND
     * marker the same way: every byte behind it is discarded. A PNG
     * whose bytes end before the structural IEND marker is truncated
     * and fails the operation, because the rewrite would produce an
     * incomplete file.
     *
     * Display chunks like gAMA, sRGB and pHYs survive the deletion:
     * removing them changes color rendering and display size. ExifTool's
     * "-all=" removes them as well - a divergence Kim does not copy.
     *
     * The file's structure must be readable: truncated or structurally
     * corrupt files fail and are left untouched. The metadata content
     * itself does not need to be parseable, since it is removed anyway -
     * a corrupt XMP payload is dropped like a valid one. Discarded
     * blocks are skipped before their payloads are read, verified or
     * decompressed, so an unwanted compressed block is removed without
     * inflating it first. The EXIF payload is the one exception when
     * [keepOrientationFlag] preserves the rotation (the default): the
     * orientation has to be read from it, so a corrupt EXIF payload
     * fails the operation.
     *
     * @param bytes The original file bytes to modify.
     * @param keepIccProfile Whether the ICC profile stays (the default)
     *        or is removed with the rest of the metadata.
     * @param keepOrientationFlag Whether the Exif orientation survives
     *        in a minimal structure (the default) or is removed with the
     *        rest of the metadata.
     *
     * @return The modified file bytes.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        bytes: ByteArray,
        keepIccProfile: Boolean = true,
        keepOrientationFlag: Boolean = true
    ): ByteArray = tryWithImageWriteException {

        val byteWriter = ByteArrayByteWriter()

        deleteMetadata(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            keepIccProfile = keepIccProfile,
            keepOrientationFlag = keepOrientationFlag
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }

    /**
     * Removes all metadata of the file, keeping healthy ICC profiles that
     * affect how the image is displayed. For JPG, APP2 segments that do
     * not carry a healthy ICC profile are deleted as well, and a faulty
     * profile lets its chunks go instead of staying in the file.
     *
     * With [keepIccProfile] = false the ICC profile goes with the rest of
     * the metadata, so the result is clean of metadata entirely. Formats
     * without an ICC concept (GIF, JPEG XL) ignore the parameter.
     *
     * The Adobe APP14 segment stays in every mode: its ColorTransform
     * flag tells the decoder how to interpret the DCT data, and losing
     * it renders Adobe RGB and CMYK files with wrong colors - ExifTool
     * protects the segment from "-all=" for the same reason.
     *
     * With [keepOrientationFlag] an explicitly stored Exif orientation
     * value (1-8) survives the removal: the metadata container carrying
     * it is replaced in place by a minimal structure carrying only that
     * value, so the stored rotation is not lost with the rest of the
     * metadata. PNG also reads the orientation from the legacy text
     * representation ("Raw profile type exif") and modernizes it into
     * an eXIf chunk.
     *
     * A legacy representation behind the image data cannot be
     * modernized, because the stream has already passed the IDAT
     * chunks: the deletion drops it like every metadata chunk, and the
     * rotation it may carry is lost.
     *
     * An absent orientation writes nothing - per the specification an
     * absent tag means 1, and the deletion fabricates no tag the file
     * never stated. The metadata container carrying the orientation
     * must be parseable, so a corrupt EXIF payload fails the operation
     * instead of losing the rotation silently. Formats without an
     * orientation concept (GIF) ignore the parameter.
     *
     * For JPG the metadata segments behind the image data are removed as
     * well, and the file ends at the structural EOI marker: every byte
     * behind it belongs to other tools and is discarded. A JPG that ends
     * inside its image data without the structural EOI marker fails the
     * operation, because its image data cannot be separated from the
     * metadata behind it. For PNG the file ends at the structural IEND
     * marker the same way: every byte behind it is discarded. A PNG
     * whose bytes end before the structural IEND marker is truncated
     * and fails the operation, because the rewrite would produce an
     * incomplete file.
     *
     * Display chunks like gAMA, sRGB and pHYs survive the deletion:
     * removing them changes color rendering and display size. ExifTool's
     * "-all=" removes them as well - a divergence Kim does not copy.
     *
     * The file's structure must be readable: truncated or structurally
     * corrupt files fail and are left untouched. The metadata content
     * itself does not need to be parseable, since it is removed anyway -
     * a corrupt XMP payload is dropped like a valid one. Discarded
     * blocks are skipped before their payloads are read, verified or
     * decompressed, so an unwanted compressed block is removed without
     * inflating it first. The EXIF payload is the one exception when
     * [keepOrientationFlag] preserves the rotation (the default): the
     * orientation has to be read from it, so a corrupt EXIF payload
     * fails the operation.
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
     *
     * @param byteReader The original file, streamed.
     * @param byteWriter The destination the modified file is written to.
     * @param keepIccProfile Whether the ICC profile stays (the default)
     *        or is removed with the rest of the metadata.
     * @param keepOrientationFlag Whether the Exif orientation survives
     *        in a minimal structure (the default) or is removed with the
     *        rest of the metadata.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        keepIccProfile: Boolean = true,
        keepOrientationFlag: Boolean = true
    ): Unit = tryWithImageWriteException {

        val detection = detectFormatAndReplayHeader(byteReader)

        if (detection.mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        val updater = MetadataUpdater.forFormat(detection.mediaFormat)
            ?: throw ImageWriteException("Can't delete metadata of ${detection.mediaFormat}.")

        updater.deleteMetadata(detection.reader, byteWriter, keepIccProfile, keepOrientationFlag)
    }

    /**
     * Replaces the embedded thumbnail of the file with the given bytes.
     *
     * Attention: The thumbnail is embedded into the EXIF data, whose
     * tightest container bound is the JPEG APP1 segment at about 65 KB.
     * Thumbnails exceeding that shared bound, or bytes without the JPEG
     * SOI marker, are rejected with an [ImageWriteException] - on every
     * format alike. The bytes are otherwise embedded as-is.
     *
     * @param bytes The original file bytes to modify.
     * @param thumbnailBytes The JPEG bytes to embed as the new thumbnail.
     *
     * @return The modified file bytes.
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

    /**
     * Removes the embedded thumbnail of the file, keeping every other
     * metadata block: the EXIF, XMP, IPTC and ICC data stay untouched.
     *
     * A file without an embedded thumbnail is returned unchanged, so the
     * operation is idempotent. GIF files carry no thumbnail concept and
     * are returned unchanged as well.
     *
     * Attention: The thumbnail lives inside the EXIF data, so formats
     * without an EXIF concept have nothing to remove.
     *
     * @param bytes The original file bytes to modify.
     *
     * @return The modified file bytes - unchanged when the file carries
     *         no embedded thumbnail.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteThumbnail(bytes: ByteArray): ByteArray = tryWithImageWriteException {

        val mediaFormat = MediaFormat.detect(bytes)

        if (mediaFormat == null)
            throw ImageWriteException("Unknown or unsupported file format.")

        val updater = MetadataUpdater.forFormat(mediaFormat)
            ?: throw ImageWriteException("Can't delete thumbnail of $mediaFormat.")

        return@tryWithImageWriteException updater.deleteThumbnail(bytes)
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
