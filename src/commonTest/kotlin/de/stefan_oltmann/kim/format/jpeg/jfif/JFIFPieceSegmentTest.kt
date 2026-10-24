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
package de.stefan_oltmann.kim.format.jpeg.jfif

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Pins the segment length bookkeeping at the single choke point that
 * serializes metadata segments: a payload that does not fit the 16-bit
 * length field must be rejected instead of being written with a wrapped
 * length that makes the JPEG unreadable.
 */
class JFIFPieceSegmentTest {

    /**
     * A payload beyond the 16-bit length field must fail the construction
     * with an [ImageWriteException] naming the size.
     */
    @Test
    fun testOversizedSegmentPayloadIsRejected() {

        val oversizedPayload = ByteArray(JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT + 1)

        assertFailsWith<ImageWriteException> {
            JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, oversizedPayload)
        }
    }

    /**
     * The largest payload that still fits the length field must be
     * accepted and serialized with a length field of 0xFFFF.
     */
    @Test
    fun testMaximumSegmentPayloadSerializesTheLengthField() {

        val segmentBytes = ByteArray(JpegConstants.MAX_PAYLOAD_BYTES_PER_SEGMENT)

        val segment = JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentBytes)

        val writer = ByteArrayByteWriter()

        segment.write(writer)

        val bytes = writer.toByteArray()

        assertEquals(
            expected = SEGMENT_HEADER_BYTES + segmentBytes.size,
            actual = bytes.size
        )

        /* The length field covers itself and the payload: 0xFFFF. */
        assertContentEquals(
            expected = byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
            actual = bytes.copyOfRange(SEGMENT_MARKER_BYTES, SEGMENT_HEADER_BYTES)
        )
    }

    companion object {

        private const val SEGMENT_MARKER_BYTES = 2

        private const val SEGMENT_HEADER_BYTES = 4
    }
}
