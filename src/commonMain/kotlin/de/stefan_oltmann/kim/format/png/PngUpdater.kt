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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.icc.IccProfileParser
import de.stefan_oltmann.kim.format.minimalOrientationExifBytes
import de.stefan_oltmann.kim.format.exifBytesWithThumbnail
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.iptc.createIptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.withIptcDigestResource
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiffOutputSetWithoutThumbnail
import de.stefan_oltmann.kim.format.updatedExifBytes
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.format.png.chunk.PngChunk
import de.stefan_oltmann.kim.format.png.chunk.PngChunkExif
import de.stefan_oltmann.kim.format.png.chunk.PngChunkIccp
import de.stefan_oltmann.kim.format.png.chunk.PngTextChunk
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object PngUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        /*
         * The update rewrites metadata it finds before the image data.
         * Metadata behind the IDAT is invisible to it, so the write fails
         * instead of silently destroying it. A deletion asked for the
         * removal and keeps skipping.
         */
        PngWriter.writeImageStreaming(
            byteReader = byteReader,
            byteWriter = byteWriter,
            failOnStaleMetadata = true,
            skippedChunkTypes = emptyList()
        ) { chunks, outputWriter ->

            val metadata = PngImageParser.parseMetadataFromChunks(chunks)

            val xmpMeta = XMPMetaFactory.parseOrCreate(metadata.xmp)

            var iptcWithDigest: IptcMetadata? = null

            val iptc = createIptcMetadata(metadata.iptc, updates)

            /*
             * When the rewrite replaces the IPTC text chunk, the
             * xmpNote:IPTCDigest of the XMP must be updated to the
             * digest of the new IPTC data, like ExifTool maintains it.
             * It is only synced when the file declares the digest - a
             * digest that a tool invented for IPTC the file never
             * declared as synced would be misleading. Unchanged IPTC
             * keeps its existing (still valid) digest.
             */
            val carriedIptcDigestResource = metadata.iptc?.nonIptcBlocks
                ?.any { it.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST } == true

            val declaredIptcDigest = carriedIptcDigestResource ||
                xmpMeta.getIptcDigest() != null

            if (iptc != null && declaredIptcDigest) {

                val digestBytes =
                    Md5.digest(IptcWriter.writeIptcBlockData(iptc.records, iptc.foreignDatasets))

                xmpMeta.setIptcDigest(digestBytes.toHex())

                if (carriedIptcDigestResource)
                    iptcWithDigest = iptc.withIptcDigestResource(digestBytes)
            }

            val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

            val exifBytes = metadata.updatedExifBytes(updates)

            /*
             * An update that changes an IPTC-representable field rewrites
             * the IPTC text chunk like the JPEG path rewrites the APP13
             * segment: the affected records carry the new values so
             * ExifTool and GIMP read the same data as from XMP and EXIF,
             * which is exactly the copies-drift-apart state this API must
             * prevent.
             */
            val iptcBytes = (iptcWithDigest ?: iptc)
                ?.let { blockData -> IptcWriter.writeIptcResourceBlocks(blockData) }

            val removeStaleIptc = iptcBytes != null

            PngWriter.writeImage(
                chunks = chunks,
                byteWriter = outputWriter,
                exifBytes = exifBytes,
                iptcBytes = iptcBytes,
                xmp = updatedXmp
            )

            /*
             * Behind the image data only stale duplicates of the rewritten
             * metadata are removed. Comments and tIME chunks are user data
             * unrelated to the change and must survive an update.
             */
            object : StaleChunkFilter {

                override fun isStale(chunkType: PngChunkType, keyword: String?): Boolean =
                    (exifBytes != null &&
                        (
                            chunkType == PngChunkType.EXIF ||
                                chunkType == PngChunkType.ZXIF ||
                                keyword == PngConstants.EXIF_KEYWORD
                            )) ||
                        (keyword == PngConstants.XMP_KEYWORD) ||
                        (removeStaleIptc && keyword == PngConstants.IPTC_KEYWORD)

                override fun failWhenStale(chunkType: PngChunkType, keyword: String?): Boolean =
                    /*
                     * Once the IPTC store was rewritten in front of the
                     * image data - where ExifTool relocates it, too - a
                     * trailing chunk is a stale duplicate of the same
                     * store, because the first chunk is authoritative.
                     * A file whose only IPTC store sat behind the image
                     * data was never seen by the streaming update, so
                     * merging is impossible and the write fails like it
                     * does for trailing EXIF and XMP instead of silently
                     * destroying the records the update does not touch.
                     */
                    !(
                        removeStaleIptc &&
                            metadata.iptc != null &&
                            keyword == PngConstants.IPTC_KEYWORD
                        )
            }
        }
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
         * payload is read: the metadata content does not need to be
         * parseable for a deletion, so a malformed or duplicate chunk
         * cannot fail the write, and a discarded profile is never
         * decompressed. The ICC profile is only skipped when it is
         * discarded - a kept profile is parsed like the read parses it,
         * because it affects how the image is displayed and stays in the
         * file. With orientation preservation the EXIF containers and
         * the legacy text Exif chunks are parsed as well, since the
         * rotation has to be known before they go.
         */
        val skippedChunkTypes = buildList {

            /* The time carries no orientation and is always dropped. */
            add(PngChunkType.TIME)

            if (!keepIccProfile)
                add(PngChunkType.ICCP)

            if (!keepOrientationFlag) {
                add(PngChunkType.EXIF)
                add(PngChunkType.ZXIF)
                add(PngChunkType.TEXT)
                add(PngChunkType.ZTXT)
                add(PngChunkType.ITXT)
            }
        }

        PngWriter.writeImageStreaming(
            byteReader = byteReader,
            byteWriter = byteWriter,
            failOnStaleMetadata = false,
            skippedChunkTypes = skippedChunkTypes
        ) { chunks, outputWriter ->

            /*
             * A kept ICC profile whose content cannot be parsed is dropped
             * like JPEG drops faulty profiles: the deletion never
             * interprets ICC content, so garbage goes with the rest of
             * the metadata instead of failing the operation or staying
             * in the file.
             */
            val healthyChunks = chunks.filterNot { chunk ->
                chunk is PngChunkIccp && !IccProfileParser.isParseable(chunk.profileBytes)
            }

            /*
             * With orientation preservation the metadata chunks before
             * the image data were parsed, so the rotation is known here:
             * the EXIF container is replaced in place by a minimal chunk
             * carrying only the orientation value, and every other
             * metadata chunk goes. An absent or unrotated (1) orientation
             * writes nothing - per the specification an absent tag means
             * 1. The legacy text Exif chunks are modernized the same way.
             */
            val orientation = if (keepOrientationFlag)
                PngImageParser.parseMetadataFromChunks(healthyChunks)
                    .findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
            else
                null

            val orientationExifBytes = minimalOrientationExifBytes(orientation)

            val writtenChunks = buildList<PngChunk> {

                var orientationWritten = false

                for (chunk in healthyChunks) {

                    val isExifSource = chunk is PngChunkExif ||
                        (chunk is PngTextChunk && chunk.getKeyword() == PngConstants.EXIF_KEYWORD)

                    when {
                        isExifSource && !orientationWritten && orientationExifBytes != null -> {
                            add(PngChunkExif(PngChunkType.EXIF, orientationExifBytes, 0))
                            orientationWritten = true
                        }

                        chunk.type !in StaleChunkFilter.METADATA_CHUNK_TYPES -> add(chunk)
                    }
                }
            }

            PngWriter.writeImage(
                chunks = writtenChunks,
                byteWriter = outputWriter
            )

            StaleChunkFilter.ALL_METADATA
        }
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.png))
            throw ImageWriteException("Provided input bytes are not PNG!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val metadata = PngImageParser.parseMetadataFromChunks(chunks)

        val exifBytes = metadata.exifBytesWithThumbnail(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            iptcBytes = null,
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

        if (!bytes.startsWith(MediaFormatMagicNumbers.png))
            throw ImageWriteException("Provided input bytes are not PNG!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val metadata = PngImageParser.parseMetadataFromChunks(chunks)

        val exifBytes = metadata.tiffOutputSetWithoutThumbnail()?.toTiffBytes()
            ?: return bytes

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            iptcBytes = null,
            /*  No change to XMP */
            xmp = null
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
