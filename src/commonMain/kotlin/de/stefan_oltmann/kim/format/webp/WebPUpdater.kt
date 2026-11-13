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
import de.stefan_oltmann.kim.common.startsWithNullable
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.minimalOrientationExifBytes
import de.stefan_oltmann.kim.format.exifBytesWithThumbnail
import de.stefan_oltmann.kim.format.tiffOutputSetWithoutThumbnail
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.updatedExifBytes
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunk
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkExif
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter

internal object WebPUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        val chunks = WebPImageParser.readChunks(byteReader, stopAfterMetadataRead = false)

        val metadata = WebPImageParser.parseMetadataFromChunks(chunks)

        val updatedXmp = XmpWriter.updateXmp(metadata.xmp, updates, true)

        val exifBytes = metadata.updatedExifBytes(updates)

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            xmp = updatedXmp
        )
    }

    @Throws(ImageWriteException::class)
    override fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        keepIccProfile: Boolean,
        keepOrientationFlag: Boolean
    ) = tryWithImageWriteException {

        /*
         * The chunks the operation discards are skipped before their
         * payload is parsed: the metadata content does not need to be
         * parseable for a deletion, so a malformed XMP chunk or a
         * discarded ICC profile cannot fail the write. The EXIF chunk is
         * the one exception when the orientation is preserved - its
         * payload must be parseable, so the rotation can be read from it.
         */
        val skippedChunkTypes = buildList {

            add(WebPChunkType.XMP)

            if (!keepIccProfile)
                add(WebPChunkType.ICCP)

            if (!keepOrientationFlag)
                add(WebPChunkType.EXIF)
        }

        val chunks = WebPImageParser.readChunks(
            byteReader,
            stopAfterMetadataRead = false,
            skipChunkTypes = skippedChunkTypes
        )

        /*
         * With orientation preservation the EXIF chunk is replaced in
         * place by a minimal chunk carrying only the orientation value.
         * A list drained completely is rejected by the writer's emptiness
         * guard.
         */
        val orientation = if (keepOrientationFlag)
            WebPImageParser.parseMetadataFromChunks(chunks)
                .findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        else
            null

        val orientationExifBytes = minimalOrientationExifBytes(orientation)

        val modifiedChunks: List<WebPChunk> = chunks.map { chunk ->
            when {
                chunk is WebPChunkExif && orientationExifBytes != null ->
                    WebPChunkExif(orientationExifBytes)

                chunk.type == WebPChunkType.EXIF || chunk.type == WebPChunkType.XMP -> null

                !keepIccProfile && chunk.type == WebPChunkType.ICCP -> null

                else -> chunk
            }
        }.filterNotNull()

        /*
         * The VP8X flags are rebuilt by the writer from the final chunk
         * list, so the cleared EXIF/XMP/ICCP flags need no handling here.
         */
        WebPWriter.writeImage(
            chunks = modifiedChunks,
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWithNullable(MediaFormatMagicNumbers.webP))
            throw ImageWriteException("Provided input bytes are not WebP!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = WebPImageParser.readChunks(byteReader, stopAfterMetadataRead = false)

        val metadata = WebPImageParser.parseMetadataFromChunks(chunks)

        val exifBytes = metadata.exifBytesWithThumbnail(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            /*  No change to XMP */
            xmp = null
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }

    /**
     * The thumbnail lives in the EXIF chunk: the rewrite removes the
     * thumbnail directory (IFD1) from the parsed set and replaces the
     * chunk with the shrunk one, while a file without a thumbnail is
     * returned unchanged.
     */
    override fun deleteThumbnail(bytes: ByteArray): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWithNullable(MediaFormatMagicNumbers.webP))
            throw ImageWriteException("Provided input bytes are not WebP!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = WebPImageParser.readChunks(byteReader, stopAfterMetadataRead = false)

        val metadata = WebPImageParser.parseMetadataFromChunks(chunks)

        val exifBytes = metadata.tiffOutputSetWithoutThumbnail()?.toTiffBytes()
            ?: return bytes

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            /*  No change to XMP */
            xmp = null
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
