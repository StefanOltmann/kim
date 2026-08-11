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
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
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

    public fun getImageSize(byteReader: ByteReader): ImageSize? {

        val magicNumberBytes = byteReader.readBytes(MediaFormatMagicNumbers.jpeg.size).toList()

        /* Not a JPEG, so there is no image size to report. */
        if (magicNumberBytes != MediaFormatMagicNumbers.jpeg)
            return null

        var readBytesCount = magicNumberBytes.size

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            var segmentIdentifier = byteReader.readByte() ?: break
            var segmentType = byteReader.readByte() ?: break

            readBytesCount += 2

            /*
             * Find the segment marker. Markers are zero or more 0xFF bytes, followed by
             * a 0xFF and then a byte not equal to 0x00 or 0xFF.
             */
            while (
                segmentIdentifier != JpegMetadataExtractor.SEGMENT_IDENTIFIER ||
                segmentType == JpegMetadataExtractor.SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                readBytesCount += 1

                segmentType = nextSegmentType
            }

            if (
                segmentType == JpegMetadataExtractor.SEGMENT_START_OF_SCAN ||
                segmentType == JpegMetadataExtractor.MARKER_END_OF_IMAGE
            )
                break

            /* If we don't have anough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - readBytesCount < 2)
                break

            /* Note: Segment length includes size bytes */
            val segmentLength =
                byteReader.read2BytesAsInt("segmentLength", JpegConstants.JPEG_BYTE_ORDER) - 2

            readBytesCount += 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /* Reject invalid segment lengths */
            if (segmentLength <= 0 || segmentLength > remainingByteCount)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            /* We are only looking for a SOF segment. */
            if (!JpegConstants.SOFN_MARKER_BYTES.contains(segmentType)) {

                byteReader.skipBytes("skip segment", segmentLength)

                readBytesCount += segmentLength

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

            val imageSize = getImageSize(segments)

            val exifBytes = getExifBytes(segments)

            val exif = exifBytes?.let { getExif(it) }

            val iptc = getIptc(segments)

            val xmp = getXmpXml(segments)

            return@tryWithImageReadException MediaMetadata(
                mediaFormat = MediaFormat.JPEG,
                imageSize = imageSize,
                exif = exif,
                exifBytes = exifBytes,
                iptc = iptc,
                xmp = xmp
            )
        }

    private fun getImageSize(segments: List<Segment>): ImageSize? {

        val sofnSegment = segments.filterIsInstance<SofnSegment>()

        val firstSegment = sofnSegment.firstOrNull() ?: return null

        return ImageSize(firstSegment.width, firstSegment.height)
    }

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
         */
        val xmp = StringBuilder()

        for (segment in xmpSegments) {

            xmp.append(JpegXmpParser.parseXmpJpegSegment(segment.segmentBytes))

            /* Stop when we find the first complete packet. */
            if (xmp.toString().contains(XMP_META_CLOSE))
                break
        }

        return xmp.toString().ifBlank { null }
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

                /* Take the first valid stream. */
                if (parsed != null)
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
     * Returns null if the data cannot be parsed.
     */
    private fun parsePhotoshopData(iptcBytes: ByteArray): IptcMetadata? {

        if (iptcBytes.isEmpty())
            return null

        return try {
            IptcParser.parseIptc(iptcBytes, startsWithApp13Header = false)
        } catch (_: ImageReadException) {
            null
        }
    }

    private fun keepMarker(marker: Int, markers: List<Int>?): Boolean =
        markers?.contains(marker) ?: false

    private fun readSegments(byteReader: ByteReader, markers: List<Int>): List<Segment> {

        val segments = mutableListOf<Segment>()

        val visitor: JpegVisitor = object : JpegVisitor {

            /* Don't read actual image data. */
            override fun beginSOS(): Boolean = false

            override fun visitSOS(marker: Int, markerBytes: ByteArray, imageData: ByteArray) =
                error("Should not be called.")

            // return false to exit traversal.
            override fun visitSegment(
                marker: Int,
                markerBytes: ByteArray,
                segmentLength: Int,
                segmentLengthBytes: ByteArray,
                segmentBytes: ByteArray
            ): Boolean {

                if (marker == JpegConstants.EOI_MARKER)
                    return false

                if (!keepMarker(marker, markers))
                    return true

                when (marker) {
                    JpegConstants.JPEG_APP1_MARKER -> segments.add(AppnSegment(marker, segmentBytes))
                    JpegConstants.JPEG_APP13_MARKER -> segments.add(App13Segment(marker, segmentBytes))
                    JpegConstants.JFIF_MARKER -> segments.add(JfifSegment(marker, segmentBytes))
                    else ->
                        when {

                            JpegConstants.SOFN_MARKERS.binarySearch(marker) >= 0 ->
                                segments.add(SofnSegment(marker, segmentBytes))

                            marker >= JpegConstants.JPEG_APP1_MARKER &&
                                marker <= JpegConstants.JPEG_APP15_MARKER ->
                                segments.add(UnknownSegment(marker, segmentBytes))
                        }
                }

                return true
            }
        }

        JpegUtils.traverseJFIF(byteReader, visitor)

        return segments
    }
}
