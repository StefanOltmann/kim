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

import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * The test is placed in jvmTest, because JvmInputStreamByteReader is a
 * JVM-only reader. The issue 41 author asked for the analyzer to work
 * with this reader without seek or reset, so the streamed walk must
 * produce the same segments as the byte-array walk - for a known
 * content length and for a stream whose length nobody provides.
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
}
