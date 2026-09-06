/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.format.jpeg.segment.App13Segment
import de.stefan_oltmann.kim.format.jpeg.segment.AppnSegment
import de.stefan_oltmann.kim.format.jpeg.segment.GenericSegment
import de.stefan_oltmann.kim.format.jpeg.segment.JfifSegment
import de.stefan_oltmann.kim.format.jpeg.segment.Segment
import de.stefan_oltmann.kim.format.jpeg.segment.SofnSegment
import de.stefan_oltmann.kim.format.jpeg.segment.UnknownSegment
import de.stefan_oltmann.kim.format.jpeg.xmp.JpegXmpParser
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Parses the metadata of JPEG files.
 */
public object JpegImageParser : ImageParser {

    private const val XMP_META_CLOSE = "</x:xmpmeta>"

    private val attributeFormRegex = Regex(
        """xmpNote:HasExtendedXMP\s*=\s*["']([0-9A-Fa-f]{32})["']"""
    )

    private val elementFormRegex = Regex(
        """<xmpNote:HasExtendedXMP>\s*([0-9A-Fa-f]{32})\s*</xmpNote:HasExtendedXMP>"""
    )

    private const val RDF_CLOSE_TAG = "</rdf:RDF>"

    public fun getImageSize(byteReader: ByteReader): ImageSize? {

        val magicNumberBytes = byteReader.readBytes(MediaFormatMagicNumbers.jpeg.size).toList()

        /* Not a JPEG, so there is no image size to report. */
        if (magicNumberBytes != MediaFormatMagicNumbers.jpeg)
            return null

        /*
         * Counted in Long space, so streams larger than the signed Int
         * range cannot wrap the counter and silently disable the
         * truncation checks below.
         */
        var readBytesCount = magicNumberBytes.size.toLong()

        val scanner = JpegMarkerScanner(byteReader)

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            readBytesCount += scan.consumedBytes.size

            if (scan.marker == JpegConstants.SOS_MARKER || scan.marker == JpegConstants.EOI_MARKER)
                break

            /* If we don't have anough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - readBytesCount < 2)
                break

            /* Note: Segment length includes size bytes */
            val segmentLength =
                byteReader.read2BytesAsInt("segmentLength", JpegConstants.JPEG_BYTE_ORDER) - 2

            readBytesCount += 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /* A zero content length is an empty segment, which is spec-legal. */
            if (segmentLength < 0 || segmentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            /* We are only looking for a SOF segment. */
            if (!JpegConstants.SOFN_MARKERS.contains(scan.marker)) {

                byteReader.skipBytes("skip segment", segmentLength)

                readBytesCount += segmentLength.toLong()

                continue
            }

            /* Skip precision */
            byteReader.skipBytes("Precision", 1)

            val height = byteReader.read2BytesAsInt("Height", JpegConstants.JPEG_BYTE_ORDER)
            val width = byteReader.read2BytesAsInt("Width", JpegConstants.JPEG_BYTE_ORDER)

            return ImageSize(width, height)

        } while (true)

        return null
    }

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            val segments = readSegments(
                byteReader,
                JpegConstants.SOFN_MARKERS +
                    listOf(JpegConstants.JPEG_APP1_MARKER, JpegConstants.JPEG_APP13_MARKER)
            )

            parseMetadata(segments)
        }

    /**
     * Parses the metadata from the given JPEG header segments.
     *
     * This allows callers that already read the header segments to parse the
     * metadata without a second traversal of the file.
     */
    internal fun parseMetadata(segments: List<JFIFPieceSegment>): MediaMetadata =
        parseMetadata(segments.mapNotNull { segment ->
            toSegment(segment.marker, segment.segmentBytes)
        })

    private fun parseMetadata(segments: List<Segment>): MediaMetadata {

        val imageSize = getImageSize(segments)

        val exifBytes = getExifBytes(segments)

        val exif = exifBytes?.let { getExif(it) }

        val iptc = getIptc(segments)

        val xmp = getXmpXml(segments)

        return MediaMetadata(
            mediaFormat = MediaFormat.JPEG,
            imageSize = imageSize,
            exif = exif,
            exifBytes = exifBytes,
            iptc = iptc,
            xmp = xmp
        )
    }

    /**
     * Maps the given marker and segment bytes to the corresponding segment
     * type, or NULL for segments that are not relevant for metadata parsing.
     */
    private fun toSegment(marker: Int, segmentBytes: ByteArray): Segment? =
        when (marker) {
            JpegConstants.JPEG_APP1_MARKER -> AppnSegment(marker, segmentBytes)
            JpegConstants.JPEG_APP13_MARKER -> App13Segment(marker, segmentBytes)

            /*
             * An APP0 without the JFIF identifier is a spec-legal JFXX
             * extension or vendor segment. It must be treated as unknown,
             * so files carrying it stay updatable like they are readable.
             */
            JpegConstants.JFIF_MARKER ->
                if (segmentBytes.startsWith(JpegConstants.JFIF0_SIGNATURE) ||
                    segmentBytes.startsWith(JpegConstants.JFIF0_SIGNATURE_ALTERNATIVE)
                )
                    JfifSegment(marker, segmentBytes)
                else
                    UnknownSegment(marker, segmentBytes)

            else ->
                when {

                    JpegConstants.SOFN_MARKERS.binarySearch(marker) >= 0 ->
                        SofnSegment(marker, segmentBytes)

                    marker >= JpegConstants.JPEG_APP1_MARKER &&
                        marker <= JpegConstants.JPEG_APP15_MARKER ->
                        UnknownSegment(marker, segmentBytes)

                    else -> null
                }
        }

    private fun getImageSize(segments: List<Segment>): ImageSize? {

        val sofnSegment = segments.filterIsInstance<SofnSegment>()

        val firstSegment = sofnSegment.firstOrNull() ?: return null

        return ImageSize(firstSegment.width, firstSegment.height)
    }

    /*
     * Attention: A corrupt EXIF segment deliberately fails the whole
     * pipeline. Degrading to NULL here would make a subsequent rewrite
     * silently drop all EXIF data of the file, while other tools may
     * still be able to read or repair it. This is a different level than
     * skipping a single invalid GPS value.
     */
    private fun getExif(bytes: ByteArray): TiffContents? {

        val exifByteReader = ByteArrayByteReader(bytes)

        val contents = TiffReader.read(exifByteReader)

        return contents
    }

    private fun getExifBytes(segments: List<Segment>): ByteArray? {

        val exifSegments = segments
            .filterIsInstance<GenericSegment>()
            .filter { it.segmentBytes.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE) }

        if (exifSegments.isEmpty())
            return null

        /*
         * Always take the first APP1 EXIF segment and ignore all others.
         * This seems to be the way ExifTool handles this, too.
         * Trying to merge different EXIF segments will most likely lead
         * to inconsistencies.
         */
        val firstSegment = exifSegments.first()

        return firstSegment.segmentBytes.getRemainingBytes(JpegConstants.EXIF_IDENTIFIER_CODE.size)
    }

    private fun getXmpXml(segments: List<Segment>): String? {

        val xmpSegments = segments
            .filterIsInstance<AppnSegment>()
            .filter { segment -> JpegXmpParser.isXmpJpegSegment(segment.segmentBytes) }

        val extendedSegments = segments
            .filterIsInstance<AppnSegment>()
            .filter { segment -> JpegXmpParser.isExtendedXmpJpegSegment(segment.segmentBytes) }

        if (xmpSegments.isEmpty())
            return null

        /*
         * XMP larger than one segment is split into multiple APP1 segments,
         * so we concatenate the segments until the XMP is complete.
         *
         * Some files in our test repo have multiple XMP strings.
         * This seems to be an error, because it's the same content, but only formatted.
         * We do here what ExifTool does on "exiftool -xmp -b photo.jpg > photo.xmp"
         * and take the first complete packet by ignoring the rest.
         *
         * Attention: Invalid XMP segments deliberately fail the whole
         * pipeline instead of degrading to NULL. Degrading would make a
         * subsequent rewrite silently drop all XMP data of the file,
         * while other tools may still be able to read or repair it.
         */
        val xmp = StringBuilder()

        for (segment in xmpSegments) {

            xmp.append(JpegXmpParser.parseXmpJpegSegment(segment.segmentBytes))

            /* Stop when we find the first complete packet. */
            if (xmp.toString().contains(XMP_META_CLOSE))
                break
        }

        if (xmp.isBlank())
            return null

        return mergeExtendedXmp(xmp.toString(), extendedSegments)
    }

    /**
     * Merges Adobe extended XMP data into the main packet, exactly like
     * ExifTool does by default: the main packet carries an
     * "xmpNote:HasExtendedXMP" property with the GUID of the extended data,
     * and only extension segments whose GUID matches are reassembled.
     *
     * The reassembled data is verified against its MD5 digest, because a
     * mismatch would mean silent metadata loss on a subsequent rewrite -
     * and Kim must never destroy or misrepresent metadata.
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun mergeExtendedXmp(
        mainPacket: String,
        extendedSegments: List<AppnSegment>
    ): String {

        val guid = findHasExtendedXmpGuid(mainPacket) ?: return mainPacket

        if (extendedSegments.isEmpty())
            throw ImageReadException(
                "The XMP packet references extended data (GUID $guid), " +
                    "but no extended XMP segments exist."
            )

        val fragments = extendedSegments.map { segment ->
            JpegXmpParser.parseExtendedXmpJpegSegment(segment.segmentBytes)
        }

        /* Segments of foreign GUIDs belong to another packet and are ignored. */
        val matchingFragments = fragments.filter { fragment ->
            fragment.guid.equals(guid, ignoreCase = true)
        }

        if (matchingFragments.isEmpty())
            throw ImageReadException(
                "The XMP packet references extended data (GUID $guid), " +
                    "but no extended XMP segments with this GUID exist."
            )

        val declaredLength = matchingFragments.first().totalLength

        val mismatchedLength = matchingFragments.firstOrNull { fragment ->
            fragment.totalLength != declaredLength
        }

        if (mismatchedLength != null)
            throw ImageReadException(
                "Inconsistent extended XMP total length: ${mismatchedLength.totalLength} " +
                    "(expected $declaredLength)."
            )

        val extendedData = ByteArrayByteWriter()

        for (fragment in matchingFragments)
            extendedData.write(fragment.data)

        val extendedBytes = extendedData.toByteArray()

        if (extendedBytes.size != declaredLength)
            throw ImageReadException(
                "Incomplete extended XMP: got ${extendedBytes.size} of $declaredLength bytes."
            )

        val actualDigest = Md5.digest(extendedBytes).toHexString(HexFormat.UpperCase)

        if (!actualDigest.equals(guid, ignoreCase = true))
            throw ImageReadException(
                "The MD5 checksum of the extended XMP data does not match the GUID " +
                    "$guid declared by the main packet."
            )

        return injectExtendedDescriptions(mainPacket, extendedBytes.decodeToString(), guid)
    }

    /**
     * Extracts the value of the "xmpNote:HasExtendedXMP" property from the
     * raw packet text. Both serialization forms that writers emit are
     * recognized: the shorthand attribute form and the element form.
     */
    private fun findHasExtendedXmpGuid(packet: String): String? {

        attributeFormRegex.find(packet)?.let { return it.groupValues[1] }

        return elementFormRegex.find(packet)?.groupValues?.get(1)
    }

    /**
     * Inserts the rdf:Description elements of the extended data into the
     * main packet, so both parse as one metadata tree afterwards.
     */
    private fun injectExtendedDescriptions(
        mainPacket: String,
        extendedXml: String,
        guid: String
    ): String {

        val innerStart = locateTagEnd(extendedXml, "<rdf:RDF")
        val innerEnd = extendedXml.indexOf(RDF_CLOSE_TAG, innerStart)

        if (innerStart == -1 || innerEnd == -1)
            throw ImageReadException(
                "The extended XMP data referenced by GUID $guid has no RDF content."
            )

        val descriptions = extendedXml.substring(innerStart, innerEnd)

        val insertionPoint = mainPacket.lastIndexOf(RDF_CLOSE_TAG)

        if (insertionPoint == -1)
            throw ImageReadException(
                "The main XMP packet referencing extended data (GUID $guid) has no RDF content."
            )

        return mainPacket.substring(0, insertionPoint) + descriptions +
            mainPacket.substring(insertionPoint)
    }

    /**
     * Returns the index behind the '>' of the given tag's first occurrence.
     */
    private fun locateTagEnd(xml: String, tagName: String): Int {

        val tagStart = xml.indexOf(tagName)

        if (tagStart == -1)
            return -1

        val tagEnd = xml.indexOf('>', tagStart)

        return if (tagEnd == -1) -1 else tagEnd + 1
    }

    private fun getIptc(segments: List<Segment>): IptcMetadata? {

        /*
         * The Photoshop data may span multiple APP13 segments.
         * Consecutive segments form one data stream: the first segment begins
         * with an image resource block, the following segments continue
         * mid-resource and start with the Photoshop identifier only.
         * Every segment starts with the Photoshop identifier.
         */
        var photoshopData = ByteArrayByteWriter()

        for (segment in segments.filterIsInstance<App13Segment>()) {

            if (!segment.isPhotoshopJpegSegment())
                continue

            val segmentData = segment.segmentBytes.getRemainingBytes(JpegConstants.APP13_IDENTIFIER.size)

            if (isNewPhotoshopStream(segmentData)) {

                val parsed = parsePhotoshopData(photoshopData.toByteArray())

                /*
                 * Take the first stream that actually carries IPTC
                 * records. A stream without records must not shadow a
                 * later, real IPTC stream of the same file.
                 */
                if (parsed != null && parsed.records.isNotEmpty())
                    return parsed

                photoshopData = ByteArrayByteWriter()
            }

            photoshopData.write(segmentData)
        }

        return parsePhotoshopData(photoshopData.toByteArray())
    }

    /**
     * Checks if the given segment data starts a new Photoshop data stream.
     *
     * A new stream begins with an image resource block. Continuation segments
     * of a split stream start mid-resource.
     */
    private fun isNewPhotoshopStream(segmentData: ByteArray): Boolean =
        segmentData.size >= JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_LENGTH &&
            segmentData.toInt(0, JpegConstants.JPEG_BYTE_ORDER) == JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT

    /**
     * Parses the given concatenated Photoshop data.
     *
     * Returns NULL only when there is no data at all.
     *
     * Attention: A corrupt Photoshop stream deliberately fails the whole
     * pipeline instead of degrading to NULL. Degrading would make a
     * subsequent rewrite silently drop all IPTC data of the file, while
     * other tools may still be able to read or repair it. This is a
     * different level than skipping a single invalid record.
     */
    private fun parsePhotoshopData(iptcBytes: ByteArray): IptcMetadata? {

        if (iptcBytes.isEmpty())
            return null

        return IptcParser.parseIptc(iptcBytes, startsWithApp13Header = false)
    }

    /**
     * Reads the header segments that match the given markers.
     */
    private fun readSegments(byteReader: ByteReader, markers: List<Int>): List<JFIFPieceSegment> {

        val (segments, _) = JpegUtils.readSegments(byteReader) { marker -> marker in markers }

        return segments
    }
}
