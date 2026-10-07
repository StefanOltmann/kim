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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.gif.GifUpdater
import de.stefan_oltmann.kim.format.jpeg.JpegUpdater
import de.stefan_oltmann.kim.format.jxl.JxlUpdater
import de.stefan_oltmann.kim.format.png.PngUpdater
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.webp.WebPUpdater
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteWriter

/**
 * Updates the metadata of a media file.
 *
 * **Every update is applied to every metadata storage that can represent it:
 * EXIF, IPTC and XMP are updated together in the same write.** The storages
 * duplicate the same logical values (title, keywords, location, ...), so
 * touching only one of them would let the copies drift apart - one tool would
 * show the new value, every other tool the stale one. Updating all relevant
 * places at once is a core correctness principle of this API, not a
 * convenience.
 */
public interface MetadataUpdater {

    @Throws(ImageWriteException::class)
    public fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    )

    /**
     * Removes all metadata of the file, keeping the ICC chunks that affect
     * how the image is displayed.
     */
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    )

    /**
     * Replaces the embedded thumbnail of the file with the given JPEG bytes.
     *
     * Attention: The thumbnail itself must fit the tightest shared
     * container bound (the JPEG APP1 segment, about 65 KB) and must carry
     * the JPEG SOI marker. Oversized or non-JPEG bytes are rejected with
     * an [ImageWriteException] on every format alike. JPEG's writer keeps
     * its own bound on the total serialized EXIF.
     */
    @Throws(ImageWriteException::class)
    public fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray

    public companion object {

        /**
         * Returns the updater that can rewrite the given media format,
         * or NULL for formats without a write path.
         */
        public fun forFormat(mediaFormat: MediaFormat): MetadataUpdater? =
            when (mediaFormat) {
                MediaFormat.JPEG -> JpegUpdater
                MediaFormat.PNG -> PngUpdater
                MediaFormat.WEBP -> WebPUpdater
                MediaFormat.JXL -> JxlUpdater
                MediaFormat.GIF -> GifUpdater
                else -> null
            }
    }

}

/**
 * Applies the updates to the EXIF of the metadata and returns the new
 * EXIF bytes, or NULL when no update changed the EXIF content - the
 * caller then leaves the stored EXIF bytes untouched.
 *
 * The EXIF read from the file is the starting point, so fields Kim does
 * not model survive the rewrite; a file without EXIF starts from an
 * empty set.
 */
internal fun MediaMetadata.updatedExifBytes(
    updates: Set<MetadataUpdate>
): ByteArray? {

    val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

    return if (outputSet.applyUpdates(updates))
        outputSet.toTiffBytes()
    else
        null
}

/**
 * Replaces the embedded JPEG thumbnail of the EXIF and returns the new
 * EXIF bytes. The EXIF read from the file is the starting point, so
 * fields Kim does not model survive the rewrite; a file without EXIF
 * starts from an empty set.
 *
 * The thumbnail itself must fit the tightest container bound on every
 * format, so oversized thumbnails are rejected uniformly instead of
 * per-format divergence. The total EXIF is deliberately unbounded here:
 * chunk-based containers (PNG/WebP/JXL) legally embed EXIF blocks
 * beyond the JPEG APP1 payload limit, and large pre-existing maker
 * notes must not fail an unrelated thumbnail replacement. JPEG's
 * segment writer keeps its own total bound.
 *
 * @throws ImageWriteException when the thumbnail exceeds the shared
 *         bound.
 */
internal fun MediaMetadata.exifBytesWithThumbnail(
    thumbnailBytes: ByteArray
): ByteArray {

    if (thumbnailBytes.size > MAX_THUMBNAIL_BYTES_EMBEDDABLE_EVERYWHERE)
        throw ImageWriteException(
            "The thumbnail is too large to embed: ${thumbnailBytes.size} bytes " +
                "(maximum $MAX_THUMBNAIL_BYTES_EMBEDDABLE_EVERYWHERE)."
        )

    val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

    outputSet.setThumbnailBytes(thumbnailBytes)

    return outputSet.toTiffBytes()
}

/**
 * The largest thumbnail every writable format can embed: the JPEG APP1
 * payload bound is the tightest container limit.
 */
internal const val MAX_THUMBNAIL_BYTES_EMBEDDABLE_EVERYWHERE: Int = 0xFFFF - 2
