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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.icc.IccProfileParser
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.iptc.createIptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.withIptcDigestResource
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
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
                onlyOrientation != null &&
                    tryLosslessOrientationUpdate(
                        segments = segments,
                        exif = kimMetadata.exif,
                        tiffOrientation = onlyOrientation.tiffOrientation
                    )

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
             * Remove the EXIF, XMP, IPTC and comment segments, and every
             * APP2 segment that does not carry a healthy ICC profile:
             * foreign payloads and faulty profiles are uninterpretable
             * metadata, while the healthy profile affects how the image is
             * displayed and stays.
             */
            val healthyIccSegmentIndexes = findHealthyIccSegmentIndexes(segments)

            val segmentsWithoutMetadata = segments
                .withIndex()
                .filterNot { (index, segment) ->
                    (segment.marker == JpegConstants.JPEG_APP2_MARKER &&
                        index !in healthyIccSegmentIndexes) ||
                        segment.isExifSegment() || segment.isIptcSegment() || segment.isXmpSegment() ||
                        segment.marker == JpegConstants.COM_MARKER_1
                }
                .map { it.value }

            JpegRewriter.writeSegments(outputWriter, segmentsWithoutMetadata)
        }
    }

    /**
     * Returns the list positions of the APP2 segments that assemble into
     * a healthy ICC profile: the chunk sequence is complete and the
     * assembled profile parses. An empty set deletes every APP2 segment,
     * also when a single faulty chunk made the assembled profile
     * unparseable.
     */
    private fun findHealthyIccSegmentIndexes(segments: List<JFIFPieceSegment>): Set<Int> {

        data class IccChunk(
            val position: Int,
            val index: Int,
            val total: Int,
            val payload: ByteArray
        )

        val chunks = mutableListOf<IccChunk>()

        for ((position, segment) in segments.withIndex()) {

            if (segment.marker != JpegConstants.JPEG_APP2_MARKER)
                continue

            val segmentBytes = segment.segmentBytes

            if (!segmentBytes.startsWith(JpegConstants.ICC_PROFILE_IDENTIFIER))
                continue

            if (segmentBytes.size < JpegConstants.ICC_IDENTIFIER_LENGTH +
                JpegConstants.ICC_SEQUENCE_BYTE_COUNT
            )
                return emptySet()

            val index = segmentBytes[JpegConstants.ICC_CHUNK_SEQUENCE_OFFSET].toUInt8()
            val total = segmentBytes[JpegConstants.ICC_CHUNK_SEQUENCE_OFFSET + 1].toUInt8()

            if (total < 1 || index < 1 || index > total)
                return emptySet()

            chunks.add(
                IccChunk(
                    position = position,
                    index = index,
                    total = total,
                    payload = segmentBytes.getRemainingBytes(
                        JpegConstants.ICC_IDENTIFIER_LENGTH + JpegConstants.ICC_SEQUENCE_BYTE_COUNT
                    )
                )
            )
        }

        if (chunks.isEmpty())
            return emptySet()

        /*
         * The chunk sequence must be complete and consistent: a missing,
         * duplicated or out-of-range chunk means corrupt metadata, which
         * deletes the profile instead of keeping a truncated one.
         */
        val total = chunks.first().total

        if (chunks.size != total || chunks.map { chunk -> chunk.index }.sorted() != (1..total).toList())
            return emptySet()

        val profileBytes = ByteArrayByteWriter()

        chunks.sortedBy { chunk -> chunk.index }
            .forEach { chunk -> profileBytes.write(chunk.payload) }

        try {
            IccProfileParser.parse(profileBytes.toByteArray())
        } catch (_: ImageReadException) {
            return emptySet()
        }

        return chunks.map { chunk -> chunk.position }.toSet()
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
     * Applies the orientation losslessly by swapping its value byte in
     * the first EXIF segment, when the parsed IFD0 field is the spec's
     * SHORT entry with one value and that byte lies inside the segment.
     *
     * The strictly parsed field is the source of truth for the patch
     * position, so the swap can only ever hit the byte the read resolved
     * the value from: never a coincidental entry in bytes the parser
     * skipped, and never a byte behind the segment. A byte that another
     * parsed field claims as well - the MakerNote value bytes above all -
     * takes the rewrite instead, which anchors the MakerNote at its
     * original offset.
     *
     * Returns whether the swap was performed.
     */
    private fun tryLosslessOrientationUpdate(
        segments: MutableList<JFIFPieceSegment>,
        exif: TiffContents?,
        tiffOrientation: TiffOrientation
    ): Boolean {

        val exifSegmentIndex = segments.indexOfFirst(JFIFPieceSegment::isExifSegment)

        if (exifSegmentIndex == -1)
            return false

        val exifSegment = segments[exifSegmentIndex]

        val orientationField =
            exif?.findTiffField(TiffTag.TIFF_TAG_ORIENTATION) ?: return false

        /*
         * Only the spec's SHORT entry with one value keeps its value
         * inside the entry. Anything else takes the rewrite path, which
         * rebuilds the entry correctly - patching a byte into a foreign
         * layout would silently corrupt the value.
         */
        if (orientationField.fieldType !== FieldTypeShort ||
            orientationField.count != 1 ||
            orientationField.localValue == null
        )
            return false

        val exifHeaderEnd =
            JpegUtils.findExifHeaderEnd(exifSegment.segmentBytes) ?: return false

        /*
         * The TIFF bytes of the first segment map 1:1 onto the front of
         * the buffer the strict read parsed, so the parsed TIFF offsets
         * address the segment bytes behind the Exif header. Little-endian
         * files store the low value byte first, big-endian files store it
         * one byte later.
         */
        val valueByteInEntry = TiffConstants.TIFF_ENTRY_VALUE_OFFSET +
            if (orientationField.byteOrder == ByteOrder.BIG_ENDIAN) 1 else 0

        val valueByteInTiff = orientationField.offset + valueByteInEntry

        if (isClaimedByAnotherField(exif, orientationField, valueByteInTiff))
            return false

        val valueByteOffset = exifHeaderEnd + valueByteInTiff

        /*
         * A value byte at or behind the first segment's end belongs to a
         * continuation segment: the in-place swap cannot reach it, the
         * rewrite of the EXIF data can.
         */
        if (valueByteOffset >= exifSegment.segmentBytes.size)
            return false

        val patchedExifBytes = exifSegment.segmentBytes.copyOf()

        patchedExifBytes[valueByteOffset] = tiffOrientation.value.toByte()

        segments[exifSegmentIndex] = JFIFPieceSegment(exifSegment.marker, patchedExifBytes)

        return true
    }

    /**
     * Whether the given TIFF-relative byte offset lies inside the entry
     * bytes or the out-of-line value bytes of a parsed field other than
     * [orientationField] itself.
     *
     * A crafted file can overlap the orientation entry with another
     * structure, so a blind swap would silently change that structure -
     * the MakerNote above all, whose value bytes must survive every
     * write byte-exact. A byte that two parsed fields claim takes the
     * rewrite path instead.
     */
    private fun isClaimedByAnotherField(
        exif: TiffContents,
        orientationField: TiffField,
        byteOffset: Int
    ): Boolean {

        val directories = exif.directories +
            listOfNotNull(exif.makerNoteDirectory) +
            exif.makerNoteSubDirectories

        for (directory in directories) {

            for (field in directory.entries) {

                if (field === orientationField)
                    continue

                val entryEnd = field.offset + TiffConstants.TIFF_ENTRY_LENGTH

                if (byteOffset in field.offset until entryEnd)
                    return true

                val valueOffset = field.valueOffset ?: continue

                val valueEnd = valueOffset + field.valueBytes.size

                if (byteOffset in valueOffset until valueEnd)
                    return true
            }
        }

        return false
    }

}
