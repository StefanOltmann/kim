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
import de.stefan_oltmann.kim.format.exifBytesWithThumbnail
import de.stefan_oltmann.kim.format.updatedExifBytes
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.iptc.createIptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.withIptcDigestResource
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
            failOnStaleMetadata = true
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
                     */
                    !(removeStaleIptc && keyword == PngConstants.IPTC_KEYWORD)
            }
        }
    }

    @Throws(ImageWriteException::class)
    override fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) = tryWithImageWriteException {

        PngWriter.writeImageStreaming(
            byteReader = byteReader,
            byteWriter = byteWriter,
            failOnStaleMetadata = false
        ) { chunks, outputWriter ->

            /*
             * Remove the EXIF chunks and all text chunks, which carry XMP,
             * IPTC and comments. The iCCP chunk is kept, because it affects
             * how the image is displayed.
             */
            val chunksWithoutMetadata = chunks.filterNot { chunk ->
                StaleChunkFilter.isMetadataChunkType(chunk.type)
            }

            PngWriter.writeImage(
                chunks = chunksWithoutMetadata,
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
            xmp = null /*  No change to XMP */
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
