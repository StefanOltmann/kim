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
package de.stefan_oltmann.kim.format.jpeg.segment

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JfifSegmentTest {

    /**
     * A valid JFIF segment without thumbnail:
     * "JFIF\0" + version 1.2 + density units 1 + x/y density 72 + no thumbnail.
     */
    private val jfifBytesWithoutThumbnail: ByteArray =
        "JFIF\u0000".encodeToByteArray() +
            byteArrayOf(1, 2) +
            byteArrayOf(1) +
            byteArrayOf(0, 72, 0, 72) +
            byteArrayOf(0, 0)

    /**
     * A valid JFIF segment with a 2x1 thumbnail:
     * "JFIF\0" + version 1.1 + density units 0 + x/y density 1 + thumbnail 2x1
     * followed by two thumbnail bytes.
     */
    private val jfifBytesWithThumbnail: ByteArray =
        "JFIF\u0000".encodeToByteArray() +
            byteArrayOf(1, 1) +
            byteArrayOf(0) +
            byteArrayOf(0, 1, 0, 1) +
            byteArrayOf(2, 1) +
            byteArrayOf(0xAA.toByte(), 0xBB.toByte())

    @Test
    fun testParseJfifSegmentWithoutThumbnail() {

        val segment = JfifSegment(
            marker = JpegConstants.JFIF_MARKER,
            segmentBytes = jfifBytesWithoutThumbnail
        )

        assertEquals(1, segment.jfifMajorVersion)
        assertEquals(2, segment.jfifMinorVersion)
        assertEquals(1, segment.densityUnits)
        assertEquals(72, segment.xDensity)
        assertEquals(72, segment.yDensity)
        assertEquals(0, segment.xThumbnail)
        assertEquals(0, segment.yThumbnail)
        assertEquals(0, segment.thumbnailSize)

        assertEquals("JFIF (65504)", segment.getDescription())
    }

    @Test
    fun testParseJfifSegmentWithThumbnail() {

        val segment = JfifSegment(
            marker = JpegConstants.JFIF_MARKER,
            segmentBytes = jfifBytesWithThumbnail
        )

        assertEquals(1, segment.jfifMajorVersion)
        assertEquals(1, segment.jfifMinorVersion)
        assertEquals(0, segment.densityUnits)
        assertEquals(1, segment.xDensity)
        assertEquals(1, segment.yDensity)
        assertEquals(2, segment.xThumbnail)
        assertEquals(1, segment.yThumbnail)
        assertEquals(2, segment.thumbnailSize)
    }

    @Test
    fun testParseJfifSegmentWithAlternativeSignature() {

        val segment = JfifSegment(
            marker = JpegConstants.JFIF_MARKER,
            segmentBytes = "JFIF ".encodeToByteArray() +
                byteArrayOf(1, 2) +
                byteArrayOf(1) +
                byteArrayOf(0, 72, 0, 72) +
                byteArrayOf(0, 0)
        )

        assertEquals(1, segment.jfifMajorVersion)
        assertEquals(2, segment.jfifMinorVersion)
    }

    @Test
    fun testParseRejectsInvalidSignature() {

        assertFailsWith<ImageReadException> {
            JfifSegment(
                marker = JpegConstants.JFIF_MARKER,
                segmentBytes = "NOTJFIF\u0000".encodeToByteArray() +
                    byteArrayOf(1, 2, 1, 0, 72, 0, 72, 0, 0)
            )
        }
    }
}
