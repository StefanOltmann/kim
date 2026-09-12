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
import de.stefan_oltmann.kim.common.toUInt16
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
import de.stefan_oltmann.xmp.XMPException
import de.stefan_oltmann.xmp.XMPMetaFactory

/**
 * Parses the metadata of JPEG files.
 */
public object JpegImageParser : ImageParser {

    private const val XMP_META_CLOSE = "</x:xmpmeta>"

    private const val TRAILER_LENGTH_BYTE_COUNT: Int = 2

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

        /* The consumed bytes are only counted here, so the scanner must not
         * buffer a potentially unbounded inter-marker gap. */
        val scanner = JpegMarkerScanner(byteReader, keepConsumedBytes = false)

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            readBytesCount += scan.consumedCount

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
            if (segmentLength !in 0..remainingByteCount)
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
        parseMetadata(byteReader = byteReader, readTrailerMetadata = false)

    /**
     * Parses the metadata of a JPEG file.
     *
     * With `readTrailerMetadata = true` the APP1 EXIF and XMP segments
     * behind the image data are scanned as well, like ExifTool reads
     * them. The trailer scan happens in the same single pass, so
     * forward-only stream sources work.
     */
    @Throws(ImageReadException::class)
    public fun parseMetadata(
        byteReader: ByteReader,
        readTrailerMetadata: Boolean
    ): MediaMetadata =
        tryWithImageReadException {

            val (segments, endMarkerBytes) = JpegUtils.readSegments(byteReader) { marker ->
                marker in JpegConstants.SOFN_MARKERS +
                    listOf(JpegConstants.JPEG_APP1_MARKER, JpegConstants.JPEG_APP13_MARKER)
            }

            /*
             * When the header scan ended on the SOS marker, the entropy
             * coded image data still lies between the reader and the EOI
             * marker. When it ended on the EOI marker instead, the reader
             * is already behind the image data.
             */
            val trailerSegments =
                if (!readTrailerMetadata)
                    emptyList()
                else
                    readTrailerSegments(
                        byteReader = byteReader,
                        scanThroughImageData = endMarkerBytes != null
                    )

            parseMetadata(segments + trailerSegments)
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
    private fun getExif(bytes: ByteArray): TiffContents {

        val exifByteReader = ByteArrayByteReader(bytes)

        val contents = TiffReader.read(exifByteReader)

        return contents
    }

    private fun getExifBytes(segments: List<Segment>): ByteArray? {

        val exifBytes = ByteArrayByteWriter()

        var haveFirstSegment = false

        for (segment in segments.filterIsInstance<GenericSegment>()) {

            val segmentBytes = segment.segmentBytes

            if (!haveFirstSegment) {

                val headerEnd = JpegUtils.findExifHeaderEnd(segmentBytes)
                    ?: continue

                exifBytes.write(segmentBytes.getRemainingBytes(headerEnd))

                haveFirstSegment = true
                continue
            }

            /*
             * EXIF larger than the ~64 KB limit of one APP1 segment is
             * split across consecutive APP1 segments: every part repeats
             * the "Exif\0\0" header, but only the first part starts with
             * a TIFF byte order marker. ExifTool stitches those parts and
             * warns "File contains multi-segment EXIF".
             *
             * A second, independent EXIF block does start with a byte
             * order marker, so the stitch ends there - mixing separate
             * EXIF blocks would lead to inconsistencies.
             */
            val headerEnd = JpegUtils.findExifHeaderEnd(segmentBytes)

            val isContinuation =
                segment.marker == JpegConstants.JPEG_APP1_MARKER &&
                    headerEnd != null &&
                    !JpegUtils.startsWithTiffByteOrderMarker(segmentBytes, headerEnd)

            if (!isContinuation)
                break

            exifBytes.write(segmentBytes.getRemainingBytes(headerEnd))
        }

        if (!haveFirstSegment)
            return null

        return exifBytes.toByteArray()
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
     * Merges Adobe extended XMP data into the main packet via the XMP
     * library, which validates the chunks for a matching GUID, contiguous
     * offsets, the declared total length and the MD5 digest, so incomplete
     * or tampered data fails the read instead of being merged silently -
     * a rewrite would destroy it otherwise.
     *
     * The consumed "xmpNote:HasExtendedXMP" reference is not part of the
     * returned packet, because it would point at chunks that no longer
     * exist when the merged packet is written back.
     */
    private fun mergeExtendedXmp(
        mainPacket: String,
        extendedSegments: List<AppnSegment>
    ): String =
        try {
            XMPMetaFactory.assemblePacket(
                mainPacket = mainPacket,
                extendedChunks = extendedSegments.map { segment -> segment.segmentBytes }
            )
        } catch (ex: XMPException) {
            throw ImageReadException("Failed to merge the extended XMP data.", ex)
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
     * Reads the APP1 EXIF and XMP segments of the trailer behind the
     * image data, like ExifTool scans them.
     *
     * The scan is deliberately limited to that scope: a SOI marker starts
     * a vendor preview, which is image data of another tool and ends the
     * scan, and every marker whose structure cannot be interpreted fails
     * the read - the flag is an explicit request for the trailer content,
     * so garbage behind the image data is reported instead of skipped.
     */
    private fun readTrailerSegments(
        byteReader: ByteReader,
        scanThroughImageData: Boolean
    ): List<JFIFPieceSegment> {

        val scanner = JpegMarkerScanner(byteReader, keepConsumedBytes = false)

        /*
         * The entropy coded image data may contain restart markers, so the
         * scan runs until the real EOI marker. When the stream ends first,
         * the file simply has no trailer.
         */
        if (scanThroughImageData) {

            @Suppress("LoopWithTooManyJumpStatements")
            while (true) {

                val scan = scanner.nextMarker(zeroIsFillByte = true)
                    ?: return emptyList()

                if (scan.marker == JpegConstants.EOI_MARKER)
                    break
            }
        }

        val trailerSegments = mutableListOf<JFIFPieceSegment>()

        /*
         * The retained trailer segments share the size budget of the
         * header segments, so a hostile file of many small segments
         * cannot accumulate memory unboundedly.
         */
        var retainedTrailerSegmentBytes = 0L

        @Suppress("LoopWithTooManyJumpStatements")
        while (true) {

            val scan = scanner.nextMarker(zeroIsFillByte = true) ?: break

            when (scan.marker) {
                /*
                 * A SOI starts a vendor preview like the Panasonic ones.
                 * That is image data of another tool, so the scan stops
                 * instead of interpreting its bytes as segments.
                 */
                JpegConstants.SOI_MARKER ->
                    break

                /* Restart markers, TEM and duplicate EOI markers carry no payload. */
                JpegConstants.TEM_MARKER,
                in JpegConstants.RST0_MARKER..JpegConstants.RST7_MARKER,
                JpegConstants.EOI_MARKER ->
                    continue

                else -> {

                    val segmentLengthBytes = byteReader.readBytes(TRAILER_LENGTH_BYTE_COUNT)

                    if (segmentLengthBytes.size != TRAILER_LENGTH_BYTE_COUNT)
                        throw ImageReadException("Truncated JPEG trailer segment length.")

                    val segmentContentLength =
                        segmentLengthBytes.toUInt16(JpegConstants.JPEG_BYTE_ORDER) - 2

                    /* A zero content length is an empty segment, which is spec-legal. */
                    if (segmentContentLength < 0)
                        throw ImageReadException(
                            "Illegal JPEG trailer segment length: $segmentContentLength"
                        )

                    val segmentData = byteReader.readBytes(segmentContentLength)

                    if (segmentData.size != segmentContentLength)
                        throw ImageReadException(
                            "Truncated JPEG trailer segment: " +
                                "${segmentData.size} of $segmentContentLength bytes."
                        )

                    /*
                     * Only APP1 carries the EXIF and XMP the flag asks for;
                     * all other segments stream through unread.
                     */
                    if (scan.marker == JpegConstants.JPEG_APP1_MARKER) {

                        retainedTrailerSegmentBytes += segmentContentLength

                        if (retainedTrailerSegmentBytes > JpegUtils.MAX_HEADER_SEGMENT_BYTES)
                            throw ImageReadException(
                                "JPEG trailer exceeds " +
                                    "${JpegUtils.MAX_HEADER_SEGMENT_BYTES} bytes."
                            )

                        trailerSegments.add(
                            JFIFPieceSegment(
                                scan.marker,
                                scan.markerBytes,
                                segmentLengthBytes,
                                segmentData
                            )
                        )
                    }
                }
            }
        }

        return trailerSegments
    }
    /*
     * The header segments are read through JpegUtils.readSegments
     * directly by the callers, so a marker-filtered wrapper would be
     * dead code.
     */
}
