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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests reading the display size of the track header box.
 */
class TrackHeaderBoxTest {

    @Test
    fun testTrackHeaderBox() {

        /* A version 0 track header of a 1920 x 1080 video track. */
        val payload = ByteArray(84)

        putFixedPointDisplaySize(payload, offset = 76, pixels = 1920)
        putFixedPointDisplaySize(payload, offset = 80, pixels = 1080)

        val box = TrackHeaderBox(
            offset = 0,
            size = 92,
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.version)
        assertEquals(1920, box.width)
        assertEquals(1080, box.height)
    }

    @Test
    fun testTrackHeaderBoxVersion1() {

        /*
         * Version 1 stores the time and duration fields as 64-bit values,
         * which shifts the size fields by 8 bytes to the end of the
         * payload.
         */
        val payload = ByteArray(96)

        payload[0] = 1

        putFixedPointDisplaySize(payload, offset = 88, pixels = 3840)
        putFixedPointDisplaySize(payload, offset = 92, pixels = 2160)

        val box = TrackHeaderBox(
            offset = 0,
            size = 104,
            largeSize = null,
            payload = payload
        )

        assertEquals(1, box.version)
        assertEquals(3840, box.width)
        assertEquals(2160, box.height)
    }

    @Test
    fun testTrackHeaderBoxRejectsUnknownVersion() {

        val payload = ByteArray(84)

        payload[0] = 2

        assertFailsWith<ImageReadException> {
            TrackHeaderBox(offset = 0, size = 92, largeSize = null, payload = payload)
        }
    }

    @Test
    fun testTrackHeaderBoxRejectsTruncatedPayload() {

        assertFailsWith<ImageReadException> {
            TrackHeaderBox(offset = 0, size = 92, largeSize = null, payload = ByteArray(10))
        }
    }

    /**
     * Writes one size field of a track header as a 16.16 fixed point value
     * in big endian, like the ISO base media file format stores it.
     */
    private fun putFixedPointDisplaySize(payload: ByteArray, offset: Int, pixels: Int) {

        val value = pixels shl 16

        payload[offset] = (value shr 24).toByte()
        payload[offset + 1] = (value shr 16).toByte()
        payload[offset + 2] = (value shr 8).toByte()
        payload[offset + 3] = value.toByte()
    }
}
