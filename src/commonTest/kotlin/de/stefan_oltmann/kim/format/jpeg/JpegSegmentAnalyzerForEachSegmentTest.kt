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
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the incremental enumeration of [JpegSegmentAnalyzer.forEachSegment].
 *
 * The walk reports the same entries as [JpegSegmentAnalyzer.findSegmentInfos]
 * in the same order, and returning false from the callback stops the walk
 * behind the last reported entry, so no byte behind it is read - a
 * bounded range enumeration with early stop.
 */
class JpegSegmentAnalyzerForEachSegmentTest {

    /**
     * The committed fixture of the full scan test: a real image whose
     * comment segment sits behind the image data, followed by a trailing
     * payload and one extra EOI marker.
     */
    private val fixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/jpeg_with_comment_before_eoi.jpg").readBytes()

    /**
     * The entries reported to the callback in the default mode are
     * exactly the entries of the list API: the list variant delegates
     * to the same walk.
     */
    @Test
    fun testForEachSegmentReportsTheDefaultScanEntries() {

        val expectedInfos =
            JpegSegmentAnalyzer.findSegmentInfos(ByteArrayByteReader(fixtureBytes))

        val reportedInfos = mutableListOf<JpegSegmentAnalyzer.JpegSegmentInfo>()

        JpegSegmentAnalyzer.forEachSegment(
            ByteArrayByteReader(fixtureBytes)
        ) { segmentInfo ->
            reportedInfos.add(segmentInfo)
            true
        }

        assertEquals(expectedInfos, reportedInfos)
    }

    /**
     * The entries reported to the callback in the full scan mode are
     * exactly the entries of the list API: the list variant delegates
     * to the same walk.
     */
    @Test
    fun testForEachSegmentReportsTheFullScanEntries() {

        val expectedInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(fixtureBytes),
            fullScan = true
        )

        val reportedInfos = mutableListOf<JpegSegmentAnalyzer.JpegSegmentInfo>()

        JpegSegmentAnalyzer.forEachSegment(
            ByteArrayByteReader(fixtureBytes),
            fullScan = true
        ) { segmentInfo ->
            reportedInfos.add(segmentInfo)
            true
        }

        assertEquals(expectedInfos, reportedInfos)
    }

    /**
     * Returning false behind the SOS entry stops the walk: no entry
     * behind it is reported and the reader is not read behind the last
     * reported entry, so the caller bounds the enumeration without
     * paying for the entropy data behind the scan header.
     */
    @Test
    fun testForEachSegmentStopsReadingBehindTheLastReportedSegment() {

        val writer = ByteArrayByteWriter()

        writer.write(SOI_BYTES)

        /* The SOS marker with its 2 length bytes, then the 6 scan header bytes. */
        writer.write(SCAN_HEADER_BYTES)

        /*
         * The entropy coded data the early stop must not walk: the run
         * carries no 0xFF bytes, so it cannot form markers of its own.
         */
        writer.write(ByteArray(ENTROPY_BYTE_COUNT) { 0x11 })

        writer.write(COM_BYTES)
        writer.write(EOI_BYTES)
        writer.write(TRAILER_BYTES)

        val countingReader =
            CountingByteReader(ByteArrayByteReader(writer.toByteArray()))

        val reportedInfos = mutableListOf<JpegSegmentAnalyzer.JpegSegmentInfo>()

        JpegSegmentAnalyzer.forEachSegment(
            countingReader,
            fullScan = true
        ) { segmentInfo ->

            reportedInfos.add(segmentInfo)

            /* The walk continues only behind the SOI entry. */
            segmentInfo.marker == SOI_MARKER
        }

        assertEquals(
            expected = listOf(
                JpegSegmentAnalyzer.JpegSegmentInfo(0L, SOI_MARKER, 2L),
                JpegSegmentAnalyzer.JpegSegmentInfo(2L, SOS_MARKER, 10L)
            ),
            actual = reportedInfos,
            message = "The walk must stop behind the declined SOS entry."
        )

        /*
         * The stopped walk consumed the SOI (2 bytes), the SOS marker
         * (2 bytes) and its length field (2 bytes): the 1000 entropy
         * bytes behind the scan header must remain unread.
         */
        assertEquals(
            expected = 6L,
            actual = countingReader.consumedByteCount,
            message = "The walk must not read the entropy data behind the SOS entry."
        )
    }

    /**
     * A reader that counts the bytes actually consumed, so the test can
     * verify the walk stops reading behind the last reported entry.
     */
    private class CountingByteReader(
        private val delegate: ByteReader
    ) : ByteReader {

        var consumedByteCount: Long = 0L
            private set

        override val contentLength: Long
            get() = delegate.contentLength

        override fun readByte(): Byte? {

            val byte = delegate.readByte()

            if (byte != null)
                consumedByteCount++

            return byte
        }

        override fun readBytes(count: Int): ByteArray {

            val bytes = delegate.readBytes(count)

            consumedByteCount += bytes.size

            return bytes
        }

        override fun close(): Unit = delegate.close()
    }

    private companion object {

        val SOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        val EOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

        /** The COM segment carrying the one payload byte "A". */
        val COM_BYTES: ByteArray =
            byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x03, 0x41)

        /** The SOS marker with its 2 length bytes and the 6 byte scan header. */
        val SCAN_HEADER_BYTES: ByteArray = byteArrayOf(
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

        /**
         * The size of the entropy coded data of the synthetic file, so
         * a walk that ignores the early stop consumes far more than the
         * 6 bytes the stopped walk consumes.
         */
        const val ENTROPY_BYTE_COUNT: Int = 1000

        /** The payload behind the structural EOI marker. */
        val TRAILER_BYTES: ByteArray =
            "KIM_TEST_DATA".encodeToByteArray() + EOI_BYTES
    }
}
