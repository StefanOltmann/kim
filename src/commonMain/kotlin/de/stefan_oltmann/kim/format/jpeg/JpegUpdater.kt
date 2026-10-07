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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.iptc.createIptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.withIptcDigestResource
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.isExifUpdate
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object JpegUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        JpegRewriter.updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

            val kimMetadata = JpegImageParser.parseMetadata(segments)

            val xmpMeta: XMPMeta = XMPMetaFactory.parseOrCreate(kimMetadata.xmp)

            var iptcWithDigest: IptcMetadata? = null

            val iptc = createIptcMetadata(kimMetadata.iptc, updates)

            /*
             * When the rewrite replaces the IPTC block, the Photoshop
             * IPTCDigest resource (0x0425, the MWG sync indicator) and
             * the xmpNote:IPTCDigest of the XMP must be updated to the
             * digest of the new IPTC data, like ExifTool maintains them.
             * Both are only synced when the file carried the resource -
             * a digest that a tool invented for IPTC the file never
             * declared as synced would be misleading. Unchanged IPTC
             * keeps its existing (still valid) digest.
             */
            val carriedIptcDigestResource = kimMetadata.iptc?.nonIptcBlocks
                ?.any { it.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST } == true

            /*
             * MWG-style writers may declare the digest only in the XMP
             * (xmpNote:IPTCDigest) without carrying the Photoshop 0x0425
             * resource. Whenever the file declares a digest in either
             * place, both markers must be refreshed together - a stale
             * xmpNote digest makes digest-aware tools report the stores
             * as out of sync although they agree.
             */
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

            val exifUpdates = updates.filter(MetadataUpdate::isExifUpdate)

            /*
             * A sole orientation update can be applied losslessly by swapping a
             * single byte in the EXIF segment. Any other EXIF update requires a
             * rewrite, which then also applies the orientation.
             */
            val onlyOrientation = exifUpdates.singleOrNull() as? MetadataUpdate.Orientation

            /*
             * Note: a successful tryLosslessOrientationUpdate() replaces the
             * EXIF segment in "segments" in place, so the rewrite writes the
             * modified segment.
             */
            val losslessOrientationApplied =
                onlyOrientation != null && tryLosslessOrientationUpdate(segments, onlyOrientation.tiffOrientation)

            val outputSet = if (exifUpdates.isEmpty() || losslessOrientationApplied)
                null
            else
                createExifOutputSet(kimMetadata.exif, exifUpdates)

            val updatedSegments =
                JpegRewriter.applyMetadataUpdates(segments, updatedXmp, outputSet, iptcWithDigest ?: iptc)

            JpegRewriter.writeSegments(outputWriter, updatedSegments)
        }
    }

    @Throws(ImageWriteException::class)
    override fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) = tryWithImageWriteException {

        JpegRewriter.updateMetadataStreaming(byteReader, byteWriter) { segments, outputWriter ->

            /*
             * Remove the EXIF, XMP, IPTC and comment segments. The ICC
             * segment is kept, because it affects how the image is displayed.
             */
            val segmentsWithoutMetadata = segments.filterNot { segment ->
                segment.isExifSegment() || segment.isIptcSegment() || segment.isXmpSegment() ||
                    segment.marker == JpegConstants.COM_MARKER_1
            }

            JpegRewriter.writeSegments(outputWriter, segmentsWithoutMetadata)
        }
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.jpeg))
            throw ImageWriteException("Provided input bytes are not JPEG!")

        val metadata = JpegImageParser.parseMetadata(ByteArrayByteReader(bytes))

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadata(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        return byteWriter.toByteArray()
    }

    /**
     * Creates the output set with the given EXIF-applicable updates applied.
     */
    private fun createExifOutputSet(
        exif: TiffContents?,
        exifUpdates: List<MetadataUpdate>
    ): TiffOutputSet {

        val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

        for (update in exifUpdates)
            outputSet.applyUpdate(update)

        return outputSet
    }

    /**
     * Applies the orientation losslessly by swapping the orientation value
     * byte in the EXIF segment of the given segments, if an orientation
     * field exists.
     *
     * Returns whether the swap was performed.
     */
    private fun tryLosslessOrientationUpdate(
        segments: MutableList<JFIFPieceSegment>,
        tiffOrientation: TiffOrientation
    ): Boolean {

        val exifSegmentIndex = segments.indexOfFirst(JFIFPieceSegment::isExifSegment)

        if (exifSegmentIndex == -1)
            return false

        val exifSegment = segments[exifSegmentIndex]

        /*
         * The offset finder works on the raw file bytes, so the SOI and the
         * segments up to and including the EXIF segment are rebuilt. This is
         * cheap, because only the header is involved.
         */
        val headerByteWriter = ByteArrayByteWriter()

        headerByteWriter.write(JpegConstants.SOI)

        for (segment in segments.take(exifSegmentIndex + 1))
            segment.write(headerByteWriter)

        val headerBytes = headerByteWriter.toByteArray()

        /*
         * Attention: A corrupt EXIF segment deliberately fails the update
         * instead of falling back to the full rewrite. The rewrite would
         * silently drop all unreadable EXIF data, which must never happen.
         */
        val orientationOffset = JpegOrientationOffsetFinder
            .findOrientationOffset(ByteArrayByteReader(headerBytes))
            ?: return false

        val exifContentOffset = headerBytes.size - exifSegment.segmentBytes.size

        val relativeOffset = (orientationOffset - exifContentOffset).toInt()

        val patchedExifBytes = exifSegment.segmentBytes.copyOf()

        patchedExifBytes[relativeOffset] = tiffOrientation.value.toByte()

        segments[exifSegmentIndex] = JFIFPieceSegment(exifSegment.marker, patchedExifBytes)

        return true
    }

}
