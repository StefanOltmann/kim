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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import java.io.IOException
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/*
 * The test is placed in jvmTest, because JvmInputStreamByteReader is a
 * JVM-only reader. The analyzer must work with this reader without
 * seek or reset, so the streamed walk must produce the same segments
 * as the byte-array walk - for a known content length and for a stream
 * whose length nobody provides.
 */
class JpegSegmentAnalyzerStreamingTest {

    private val jpegBytes: ByteArray =
        KimTestData.getBytesOf(2)

    /**
     * The full scan over a streamed file reports the same segments as
     * the walk over the byte array: the analyzer works forward-only and
     * never seeks back.
     */
    @Test
    fun testFullScanOverStreamMatchesTheByteArrayWalk() {

        val streamedInfos = JpegSegmentAnalyzer.findSegmentInfos(
            JvmInputStreamByteReader(
                inputStream = jpegBytes.inputStream(),
                contentLength = jpegBytes.size.toLong()
            ),
            fullScan = true
        )

        val byteArrayInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(jpegBytes),
            fullScan = true
        )

        assertEquals(byteArrayInfos, streamedInfos)
    }

    /**
     * The full scan never consults the content length, so a stream whose
     * length nobody provides (a non-positive hint) produces the same
     * segments as the walk over the byte array.
     */
    @Test
    fun testFullScanOverUnknownLengthStreamMatchesTheByteArrayWalk() {

        val streamedInfos = JpegSegmentAnalyzer.findSegmentInfos(
            JvmInputStreamByteReader(
                inputStream = jpegBytes.inputStream(),
                contentLength = -1
            ),
            fullScan = true
        )

        val byteArrayInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(jpegBytes),
            fullScan = true
        )

        assertEquals(byteArrayInfos, streamedInfos)
    }

    /**
     * The default scan streams the image data behind the SOS marker to
     * the file end: a stream with a length hint produces the same
     * segments as the walk over the byte array.
     */
    @Test
    fun testDefaultScanOverStreamMatchesTheByteArrayWalk() {

        val streamedInfos = JpegSegmentAnalyzer.findSegmentInfos(
            JvmInputStreamByteReader(
                inputStream = jpegBytes.inputStream(),
                contentLength = jpegBytes.size.toLong()
            )
        )

        val byteArrayInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(jpegBytes)
        )

        assertEquals(byteArrayInfos, streamedInfos)
    }

    /**
     * A stream without a length hint reports an unbounded content
     * length: the default scan must stream to the stream end instead of
     * failing over the unknown byte count, and report the true SOS size
     * with the EOI check at the stream end - like the walk over the byte
     * array.
     */
    @Test
    fun testDefaultScanOverUnboundedStreamMatchesTheByteArrayWalk() {

        val streamedInfos = JpegSegmentAnalyzer.findSegmentInfos(
            JvmInputStreamByteReader(
                inputStream = jpegBytes.inputStream(),
                contentLength = -1
            )
        )

        val byteArrayInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(jpegBytes)
        )

        assertEquals(byteArrayInfos, streamedInfos)
    }

    /**
     * A genuine I/O failure of the underlying stream stays
     * distinguishable from a structural error: the failure surfaces as
     * [ImageReadException] with the original exception preserved as the
     * cause, so the true failure remains diagnosable.
     */
    @Test
    fun testUnderlyingIoFailureKeepsTheOriginalCause() {

        val failingStream = object : InputStream() {

            override fun read(): Int =
                throw IOException("The stream broke.")

            override fun read(b: ByteArray, off: Int, len: Int): Int =
                throw IOException("The stream broke.")
        }

        val exception = assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                JvmInputStreamByteReader(
                    inputStream = failingStream,
                    contentLength = -1
                ),
                fullScan = true
            )
        }

        /*
         * The generic wrap message carries the original failure as the
         * cause, so callers can tell I/O failures from structural ones.
         */
        assertContains(
            exception.message ?: "",
            "Failed to read image."
        )

        val cause = assertNotNull(exception.cause)

        assertContains(cause.message ?: "", "The stream broke.")
    }

    /**
     * A structural error of the JPEG itself is the opposite case: the
     * exception carries the specific message and no cause, so a caller
     * can tell the two failure classes apart by the preserved cause.
     */
    @Test
    fun testStructuralFailureCarriesNoCause() {

        /* The file ends inside its image data without the EOI marker. */
        val truncatedBytes = jpegBytes.copyOfRange(0, jpegBytes.size - 50)

        val exception = assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                JvmInputStreamByteReader(
                    inputStream = truncatedBytes.inputStream(),
                    contentLength = truncatedBytes.size.toLong()
                ),
                fullScan = true
            )
        }

        assertContains(
            exception.message ?: "",
            "The full scan requires the file to end with the structural EOI marker."
        )

        assertNull(
            exception.cause,
            "A structural error must not carry a foreign cause."
        )
    }
}
