/*
 * Copyright 2026 Stefan Oltmann
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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.COM_MARKER_1
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.DHP_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.DHT_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.DNL_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EXP_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP1_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.RST3_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOF2_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.TEM_MARKER
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests the full scan mode of [JpegSegmentAnalyzer.findSegmentInfos]
 * against the committed fixture `jpeg_with_comment_before_eoi.jpg`.
 *
 * The fixture is a real test image (media_20.jpg) whose comment segment
 * was written by ExifTool 12.70 and then relocated byte-exact behind the
 * image data. Behind the structural EOI marker follow the ASCII payload
 * "KIM_TEST_DATA" and one extra EOI marker. ExifTool 12.70 validates the
 * fixture: it reports `Comment = KIM_TEST_DATA` for the relocated
 * segment behind the image data and the payload as an unknown 15 byte
 * trailer at offset 0x31f9. GitHub issue 41 works with exactly this
 * scenario: the default scan must keep reporting the SOS segment up to
 * the file end without an EOI, while the full scan must locate the COM
 * segment between the scans and the structural EOI in front of the
 * trailing payload.
 */
class JpegSegmentAnalyzerFullScanTest {

    private val fixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/jpeg_with_comment_before_eoi.jpg").readBytes()

    /**
     * The full scan reports the COM segment between the image data and
     * the EOI marker, and the EOI marker in front of the trailing
     * payload. The payload and the extra EOI behind it are outside the
     * reported ranges of the primary image.
     */
    @Test
    fun testFullScanReportsCommentAndEoiBehindImageData() {

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(fixtureBytes),
            fullScan = true
        )

        val eoiInfo = segmentInfos.last()

        assertEquals(EOI_MARKER, eoiInfo.marker, "The scan must end on the structural EOI marker.")
        assertEquals(FIXTURE_EOI_OFFSET, eoiInfo.offset, "The structural EOI sits in front of the payload.")
        assertEquals(2L, eoiInfo.length)

        /*
         * The relocated COM segment with the 13 ASCII characters is the
         * entry directly before the EOI marker.
         */
        val comInfo = segmentInfos[segmentInfos.lastIndex - 1]

        assertEquals(COM_MARKER_1, comInfo.marker)
        assertEquals(FIXTURE_COM_OFFSET, comInfo.offset)
        assertEquals(FIXTURE_COM_LENGTH, comInfo.length)

        assertEquals(comInfo.offset + comInfo.length, eoiInfo.offset)

        assertContentEquals(
            expected = SENTINEL,
            actual = fixtureBytes.copyOfRange(
                comInfo.offset.toInt() + 4,
                comInfo.offset.toInt() + 4 + SENTINEL.size
            ),
            message = "The reported COM segment must carry the comment payload."
        )

        assertContentEquals(
            expected = EOI_BYTES,
            actual = fixtureBytes.copyOfRange(
                eoiInfo.offset.toInt(),
                eoiInfo.offset.toInt() + 2
            )
        )

        /*
         * The ASCII payload followed by one extra EOI marker is all that
         * is behind the reported ranges of the primary image.
         */
        assertContentEquals(
            expected = SENTINEL + EOI_BYTES,
            actual = fixtureBytes.copyOfRange(
                eoiInfo.offset.toInt() + 2,
                fixtureBytes.size
            )
        )
    }

    /**
     * The default scan keeps the documented behavior on the same file
     * (GitHub issue 41): the SOS segment reaches to the end of the file
     * and no EOI is reported, because the final two bytes are the extra
     * payload's trailing EOI and not the structural EOI.
     */
    @Test
    fun testDefaultScanUnchangedForFileWithTrailingData() {

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(ByteArrayByteReader(fixtureBytes))

        /*
         * The default scan only reports an EOI marker that is physically
         * the last one in the file: here that marker is the payload's
         * extra EOI, so the structural EOI and the trailing payload stay
         * hidden inside the SOS range. This is exactly the defect of
         * GitHub issue 41 that the full scan resolves.
         */
        val eoiInfo = segmentInfos.last()
        val eoiEntries = segmentInfos.filter { it.marker == EOI_MARKER }

        assertEquals(1, eoiEntries.size, "The default scan reports one EOI marker.")

        assertEquals(
            eoiInfo.offset,
            eoiEntries.single().offset,
            "Only the physically last EOI marker is reported, not the structural one."
        )

        val sosInfo = segmentInfos[segmentInfos.lastIndex - 1]

        assertEquals(SOS_MARKER, sosInfo.marker)

        assertEquals(
            expected = fixtureBytes.size.toLong() - sosInfo.offset - 2,
            actual = sosInfo.length,
            message = "The default SOS segment must reach to the final EOI marker."
        )
    }

    /**
     * The TEM marker is a standalone marker without a length field and
     * without payload, like SOI and EOI (GitHub issue 41 follow-up).
     * In the header the default scan must not read the bytes behind it
     * as a segment length but report the marker as a two byte entry.
     */
    @Test
    fun testDefaultScanReportsTemMarkerInHeaderWithoutLength() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        val temOffset: Long = writer.toByteArray().size.toLong()

        writer.write(TEM_BYTES)

        val sosOffset: Long = writer.toByteArray().size.toLong()

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22))

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray())
        )

        val markers = segmentInfos.map { it.marker }

        assertEquals(listOf(SOI_MARKER, TEM_MARKER, SOS_MARKER, EOI_MARKER), markers)

        assertEquals(TEM_MARKER, segmentInfos[1].marker)
        assertEquals(temOffset, segmentInfos[1].offset)
        assertEquals(2L, segmentInfos[1].length)

        val sosInfo = segmentInfos[2]

        assertEquals(SOS_MARKER, sosInfo.marker)
        assertEquals(sosOffset, sosInfo.offset)

        assertEquals(
            expected = writer.toByteArray().size.toLong() - sosInfo.offset - 2,
            actual = sosInfo.length,
            message = "The default SOS segment must reach to the final EOI marker."
        )

        assertEquals(EOI_MARKER, segmentInfos[3].marker)
        assertEquals(eoiOffset, segmentInfos[3].offset)
    }

    /**
     * A restart marker has no length word anywhere per the ExifTool
     * reference, although the spec places it only inside the entropy
     * coded data: a stray restart marker in the header must be reported
     * as a standalone two byte entry instead of reading the bytes behind
     * it as a segment length.
     */
    @Test
    fun testDefaultScanReportsRestartMarkerInHeaderWithoutLength() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        writer.write(byteArrayOf(0xFF.toByte(), 0xD3.toByte()))

        val sosOffset: Long = writer.toByteArray().size.toLong()

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22))

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray())
        )

        val markers = segmentInfos.map { it.marker }

        assertEquals(
            listOf(SOI_MARKER, RST3_MARKER, SOS_MARKER, EOI_MARKER),
            markers
        )

        assertEquals(RST3_MARKER, segmentInfos[1].marker)
        assertEquals(2L, segmentInfos[1].length)
        assertEquals(RST3_MARKER_OFFSET, segmentInfos[1].offset)

        val sosInfo = segmentInfos[2]

        assertEquals(SOS_MARKER, sosInfo.marker)
        assertEquals(sosOffset, sosInfo.offset)

        assertEquals(
            expected = writer.toByteArray().size.toLong() - sosInfo.offset - 2,
            actual = sosInfo.length,
            message = "The default SOS segment must reach to the final EOI marker."
        )

        assertEquals(EOI_MARKER, segmentInfos[3].marker)
        assertEquals(eoiOffset, segmentInfos[3].offset)
    }

    /**
     * Both scans read the header segments identically: the full scan
     * only changes how the region behind the first SOS marker is walked.
     */
    @Test
    fun testFullScanSharesTheHeaderSegmentPrefix() {

        val defaultInfos = JpegSegmentAnalyzer.findSegmentInfos(ByteArrayByteReader(fixtureBytes))

        val fullInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(fixtureBytes),
            fullScan = true
        )

        /*
         * Only the header segments before the first SOS marker are
         * compared: the SOS entry itself necessarily differs, because
         * the default mode reports it reaching to the end of the file
         * while the full scan reports only its scan header.
         */
        val indexFirstSos = defaultInfos.indexOfFirst { it.marker == SOS_MARKER }

        assertContentEquals(
            expected = defaultInfos.take(indexFirstSos),
            actual = fullInfos.take(indexFirstSos),
            message = "The header segment prefix must be identical in both modes."
        )
    }

    /**
     * The full scan stops at the primary image's structural EOI and
     * reports nothing behind it: the APP1 segments of the committed
     * fixture's trailer are outside the reported ranges. ExifTool 12.70
     * reports the trailer of that fixture as 3206 bytes starting at
     * offset 0x170e, which marks the end of the structural EOI.
     */
    @Test
    fun testFullScanCoversOnlyPrimaryImageRanges() {

        val trailerFixtureBytes: ByteArray =
            Resource("de/stefan_oltmann/kim/testdata/jpeg_with_metadata_behind_eoi.jpg")
                .readBytes()

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(trailerFixtureBytes),
            fullScan = true
        )

        val eoiInfo = segmentInfos.last()

        assertEquals(EOI_MARKER, eoiInfo.marker)
        assertEquals(2L, eoiInfo.length)

        /* ExifTool 12.70 reports the unknown trailer with this size. */
        val trailerBytes: Long = 3206

        assertEquals(
            expected = trailerFixtureBytes.size - trailerBytes,
            actual = eoiInfo.offset + eoiInfo.length,
            message = "The structural EOI must mark the end of the reported ranges."
        )

        assertContentEquals(
            expected = EOI_BYTES,
            actual = trailerFixtureBytes.copyOfRange(
                eoiInfo.offset.toInt(),
                eoiInfo.offset.toInt() + 2
            )
        )
    }

    /**
     * Byte stuffing (FF 00), restart markers (FF D0 up to FF D7) and
     * fill bytes (FF FF) inside the entropy coded data must not fool
     * the full scan: the scan still reaches the real COM segment and
     * the EOI marker behind the image data.
     */
    @Test
    fun testFullScanIgnoresByteStuffingRestartMarkersAndFillBytes() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        val sosOffset: Long = writer.toByteArray().size.toLong()

        writeScanHeader(writer)

        /*
         * Entropy coded data with one stuffed FF 00 pair, one restart
         * marker in the middle and one fill byte run before the COM
         * segment. None of these may end the scan.
         */
        writer.write(byteArrayOf(0x11, 0x22))
        writer.write(byteArrayOf(0xFF.toByte(), 0x00))
        writer.write(byteArrayOf(0x33, 0x44))
        writer.write(byteArrayOf(0xFF.toByte(), 0xD3.toByte()))
        writer.write(byteArrayOf(0x55, 0xFF.toByte(), 0xFF.toByte()))

        val comOffset: Long = writer.toByteArray().size.toLong()

        writeSegment(writer, COM_MARKER_1, SENTINEL)

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        writer.write(SENTINEL)
        writer.write(EOI_BYTES)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray()),
            fullScan = true
        )

        assertEquals(4, segmentInfos.size)

        assertEquals(SOS_MARKER, segmentInfos[1].marker)
        assertEquals(sosOffset, segmentInfos[1].offset)

        assertEquals(
            expected = SCAN_HEADER_LENGTH.toLong(),
            actual = segmentInfos[1].length,
            message = "The SOS entry must cover only its scan header."
        )

        assertEquals(COM_MARKER_1, segmentInfos[2].marker)
        assertEquals(comOffset, segmentInfos[2].offset)
        assertEquals(SENTINEL.size + 4L, segmentInfos[2].length)

        assertEquals(EOI_MARKER, segmentInfos[3].marker)
        assertEquals(eoiOffset, segmentInfos[3].offset)
        assertEquals(2L, segmentInfos[3].length)
    }

    /**
     * The TEM marker is a standalone marker without a length field and
     * without payload, like SOI and EOI (GitHub issue 41 follow-up).
     * Behind the first SOS marker the full scan must not read the bytes
     * behind it as a segment length but report the marker as a two byte
     * entry, and the scan continues behind it up to the structural EOI
     * marker.
     */
    @Test
    fun testFullScanReportsTemMarkerBehindImageDataWithoutLength() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22, 0x33))

        val temOffset: Long = writer.toByteArray().size.toLong()

        writer.write(TEM_BYTES)

        writer.write(byteArrayOf(0x44, 0x55))

        writeSegment(writer, COM_MARKER_1, SENTINEL)

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        writer.write(SENTINEL)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray()),
            fullScan = true
        )

        val markers = segmentInfos.map { it.marker }

        assertEquals(
            listOf(SOI_MARKER, SOS_MARKER, TEM_MARKER, COM_MARKER_1, EOI_MARKER),
            markers
        )

        /*
         * Every reported entry except the SOI is verified: the marker
         * bytes must really sit at the reported offset, so a shifted
         * offset cannot hide behind plausible lengths.
         */
        val bytes = writer.toByteArray()

        for (segmentInfo in segmentInfos.drop(1)) {
            assertContentEquals(
                expected = byteArrayOf(
                    (segmentInfo.marker ushr 8).toByte(),
                    segmentInfo.marker.toByte()
                ),
                actual = bytes.copyOfRange(
                    segmentInfo.offset.toInt(),
                    segmentInfo.offset.toInt() + 2
                ),
                message = "The bytes at ${segmentInfo.offset} must be the reported marker."
            )
        }

        assertEquals(TEM_MARKER, segmentInfos[2].marker)
        assertEquals(temOffset, segmentInfos[2].offset)
        assertEquals(2L, segmentInfos[2].length)

        val eoiInfo = segmentInfos.last()

        assertEquals(EOI_MARKER, eoiInfo.marker)
        assertEquals(eoiOffset, eoiInfo.offset)
        assertEquals(2L, eoiInfo.length)
    }

    /**
     * A progressive JPEG can contain several SOS scans with COM and
     * table segments between them. The full scan reports every scan
     * header and every segment between the scans individually up to the
     * structural EOI marker, and restart markers inside the entropy
     * data stay within the unreported gaps around the entries.
     */
    @Test
    fun testFullScanReportsSegmentsBetweenProgressiveScans() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)
        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22, 0x33))

        writeSegment(writer, COM_MARKER_1, SENTINEL)

        writeSegment(writer, DHT_MARKER, byteArrayOf(0x00, 0x01))

        writeSegment(writer, SOS_MARKER, byteArrayOf(1, 1, 0, 0, 63.toByte(), 0))

        writer.write(byteArrayOf(0x44))

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        writer.write(SENTINEL)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray()),
            fullScan = true
        )

        val markers = segmentInfos.map { it.marker }

        assertEquals(
            listOf(SOI_MARKER, SOS_MARKER, COM_MARKER_1, DHT_MARKER, SOS_MARKER, EOI_MARKER),
            markers
        )

        /*
         * Every reported entry except the SOI is verified: the marker
         * bytes must really sit at the reported offset, so a shifted
         * offset cannot hide behind plausible lengths.
         */
        val bytes = writer.toByteArray()

        for (segmentInfo in segmentInfos.drop(1)) {
            assertContentEquals(
                expected = byteArrayOf(
                    (segmentInfo.marker ushr 8).toByte(),
                    segmentInfo.marker.toByte()
                ),
                actual = bytes.copyOfRange(
                    segmentInfo.offset.toInt(),
                    segmentInfo.offset.toInt() + 2
                ),
                message = "The bytes at ${segmentInfo.offset} must be the reported marker."
            )
        }

        val eoiInfo = segmentInfos.last()

        assertEquals(EOI_MARKER, eoiInfo.marker)
        assertEquals(eoiOffset, eoiInfo.offset)
        assertEquals(2L, eoiInfo.length)

        /* Each SOS entry covers only its scan header, not the entropy data. */
        assertEquals(SCAN_HEADER_LENGTH.toLong(), segmentInfos[1].length)

        /* The second scan header is only 6 payload bytes. */
        assertEquals(6L + 4L, segmentInfos[4].length)
    }

    /**
     * A file whose bytes end inside the image data without the EOI
     * marker cannot provide the structural boundaries the full scan
     * promises and fails the read.
     */
    @Test
    fun testFullScanFailsWithoutEoi() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)
        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22, 0x33))

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(writer.toByteArray()),
                fullScan = true
            )
        }
    }

    /**
     * An SOI marker behind the first SOS marker is no structural JPEG
     * boundary the image data knows: it is a corrupt stream and fails
     * the full scan instead of being scanned over.
     */
    @Test
    fun testFullScanFailsOnSoiInsideImageData() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)
        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22))
        writer.write(SOI_BYTES)
        writer.write(byteArrayOf(0x33))

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(writer.toByteArray()),
                fullScan = true
            )
        }
    }

    /**
     * A segment between the scans whose length field announces more
     * bytes than the file contains fails the full scan.
     */
    @Test
    fun testFullScanFailsOnSegmentTruncatedBetweenScans() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)
        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22))

        /* The length field announces 100 payload bytes that do not exist. */
        writer.write(
            byteArrayOf(
                0xFF.toByte(),
                0xFE.toByte(),
                0x00,
                0x64
            )
        )
        writer.write(byteArrayOf(0x01, 0x02))

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(writer.toByteArray()),
                fullScan = true
            )
        }
    }

    /**
     * Hierarchical JPEG structure: the DHP marker defines the frame
     * progression in the header and the EXP marker expands the reference
     * components between the frames. Both are length-prefixed structure
     * markers: the full scan reports them like every other segment at
     * their byte-exact positions, so the hierarchical structure is
     * addressable for a metadata copy.
     */
    @Test
    fun testFullScanReportsHierarchicalProgressionMarkers() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        val dhpOffset: Long = writer.toByteArray().size.toLong()

        /* The DHP payload: precision, frame height, width and component count. */
        writeSegment(
            writer,
            DHP_MARKER,
            byteArrayOf(0x08, 0x00, 0x10, 0x00, 0x10, 0x01)
        )

        writeSegment(writer, SOF2_MARKER, byteArrayOf(0x08, 0x00, 0x10, 0x00, 0x10))

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22, 0x33))

        val app1Offset: Long = writer.toByteArray().size.toLong()

        writeSegment(writer, JPEG_APP1_MARKER, SENTINEL)

        /*
         * The DNL marker names the scan line count of the finished scan:
         * a scan-continuation case the issue 41 author names explicitly.
         * It is a length-prefixed structure marker between the scans.
         */
        val dnlOffset: Long = writer.toByteArray().size.toLong()

        writeSegment(writer, DNL_MARKER, byteArrayOf(0x00, 0x10))

        val expOffset: Long = writer.toByteArray().size.toLong()

        /* The EXP payload: the horizontal and vertical expansion factors. */
        writeSegment(writer, EXP_MARKER, byteArrayOf(0x11))

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x44, 0x55))

        val comOffset: Long = writer.toByteArray().size.toLong()

        writeSegment(writer, COM_MARKER_1, SENTINEL)

        val eoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        writer.write(SENTINEL)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray()),
            fullScan = true
        )

        assertEquals(
            expected = listOf(
                SOI_MARKER, DHP_MARKER, SOF2_MARKER, SOS_MARKER,
                JPEG_APP1_MARKER, DNL_MARKER, EXP_MARKER, SOS_MARKER,
                COM_MARKER_1, EOI_MARKER
            ),
            actual = segmentInfos.map { it.marker },
            message = "The hierarchical structure must be reported segment by segment."
        )

        /*
         * Every reported entry except the SOI is verified: the marker
         * bytes must really sit at the reported offset, so a shifted
         * offset cannot hide behind plausible lengths.
         */
        val bytes = writer.toByteArray()

        for (segmentInfo in segmentInfos.drop(1)) {
            assertContentEquals(
                expected = byteArrayOf(
                    (segmentInfo.marker ushr 8).toByte(),
                    segmentInfo.marker.toByte()
                ),
                actual = bytes.copyOfRange(
                    segmentInfo.offset.toInt(),
                    segmentInfo.offset.toInt() + 2
                ),
                message = "The bytes at ${segmentInfo.offset} must be the reported marker."
            )
        }

        assertEquals(
            expected = dhpOffset,
            actual = segmentInfos.first { it.marker == DHP_MARKER }.offset
        )

        assertEquals(
            expected = app1Offset,
            actual = segmentInfos.first { it.marker == JPEG_APP1_MARKER }.offset
        )

        assertEquals(
            expected = dnlOffset,
            actual = segmentInfos.first { it.marker == DNL_MARKER }.offset
        )

        assertEquals(
            expected = 6L,
            actual = segmentInfos.first { it.marker == DNL_MARKER }.length,
            message = "The DNL length must cover marker, length field and payload."
        )

        assertEquals(
            expected = expOffset,
            actual = segmentInfos.first { it.marker == EXP_MARKER }.offset
        )

        assertEquals(
            expected = eoiOffset,
            actual = segmentInfos.last().offset
        )

        assertEquals(
            expected = 10L,
            actual = segmentInfos.first { it.marker == DHP_MARKER }.length,
            message = "The DHP length must cover marker, length field and payload."
        )

        assertEquals(
            expected = 5L,
            actual = segmentInfos.first { it.marker == EXP_MARKER }.length,
            message = "The EXP length must cover marker, length field and payload."
        )

        assertEquals(
            expected = comOffset,
            actual = segmentInfos.first { it.marker == COM_MARKER_1 }.offset
        )
    }

    /**
     * Layout 4 of the issue 41 author: another complete image follows
     * the primary image. The full scan stops at the primary image's EOI
     * marker and reports nothing behind it - the second image, whose
     * file also ends with an EOI marker, must not confuse the boundary.
     */
    @Test
    fun testFullScanStopsAtThePrimaryImageBeforeASecondImage() {

        val writer = ByteArrayByteWriter()

        writeSoi(writer)

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x11, 0x22, 0x33))

        val primaryEoiOffset: Long = writer.toByteArray().size.toLong()

        writer.write(EOI_BYTES)

        /* The second image: its own SOI, scan and EOI behind the primary one. */
        val secondImageOffset: Long = writer.toByteArray().size.toLong()

        writeSoi(writer)

        writeScanHeader(writer)

        writer.write(byteArrayOf(0x44, 0x55, 0x66))

        writer.write(EOI_BYTES)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(writer.toByteArray()),
            fullScan = true
        )

        assertEquals(
            expected = listOf(SOI_MARKER, SOS_MARKER, EOI_MARKER),
            actual = segmentInfos.map { it.marker },
            message = "The full scan must stop at the primary image's EOI marker."
        )

        /*
         * The reported EOI is the primary image's one at its byte-exact
         * position, not the second image's trailing EOI marker.
         */
        assertEquals(
            expected = primaryEoiOffset,
            actual = segmentInfos.last().offset
        )

        assertContentEquals(
            expected = EOI_BYTES,
            actual = writer.toByteArray().copyOfRange(
                segmentInfos.last().offset.toInt(),
                segmentInfos.last().offset.toInt() + 2
            )
        )

        /*
         * The second image stays completely outside the reported ranges:
         * its bytes begin behind the primary EOI and are not part of the
         * primary image's structure.
         */
        assertTrue(
            segmentInfos.last().offset + segmentInfos.last().length <= secondImageOffset,
            "The second image must lie completely outside the reported ranges."
        )
    }

    /**
     * Writes the SOI marker into the given writer.
     */
    private fun writeSoi(writer: ByteArrayByteWriter) {
        writer.write(SOI_BYTES)
    }

    /**
     * Writes the SOS marker with the 8 byte scan header the rewriter
     * tests use as well.
     */
    private fun writeScanHeader(writer: ByteArrayByteWriter) {
        writer.write(
            byteArrayOf(
                0xFF.toByte(),
                0xDA.toByte(),
                0x00,
                0x08,
                0x01,
                0x01,
                0x00,
                0x00,
                0x3F,
                0x00
            )
        )
    }

    private companion object {

        /** The comment payload of the committed fixture, as ASCII bytes. */
        val SENTINEL: ByteArray = "KIM_TEST_DATA".encodeToByteArray()

        val SOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        val EOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

        /** The standalone TEM marker, which has no length field. */
        val TEM_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0x01)

        /** The offset of the restart marker the header test writes behind the SOI. */
        const val RST3_MARKER_OFFSET: Long = 2

        /**
         * The offsets and sizes of the committed fixture, which
         * ExifTool 12.70 validated (see the class documentation).
         */
        const val FIXTURE_COM_OFFSET: Long = 12774
        const val FIXTURE_COM_LENGTH: Long = 17
        const val FIXTURE_EOI_OFFSET: Long = 12791

        /** The SOS marker with its 2 length bytes, then the 8 scan header bytes. */
        const val SCAN_HEADER_LENGTH: Int = 10
    }
}
