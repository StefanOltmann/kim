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

class ItemLocationBoxTest {

    /**
     * A hostile 32-bit item count of 0xFFFFFFFF parses as a negative Int
     * and would turn repeat() into a silent no-op, reporting an empty
     * item list for a structurally broken file. Construction must fail
     * like an unsupported version does.
     */
    @Test
    fun testRejectsNegativeItemCount() {

        /* version 2, no flags, 4-byte offsets/lengths, no base offset,
           itemCount 0xFFFFFFFF and no items. */
        val payload = byteArrayOf(
            2, 0, 0, 0,
            0x44,
            0x00,
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        assertFailsWith<ImageReadException> {
            ItemLocationBox(
                offset = 0,
                size = payload.size.toLong() + 8,
                largeSize = null,
                payload = payload
            )
        }
    }

    /**
     * ISOBMFF 32-bit extent offsets are unsigned. A spec-legal offset
     * with the high bit set (2 GiB and above) must not sign-extend into
     * a negative offset that silently skips the metadata item.
     */
    @Test
    fun testExtentOffsetAboveTwoGigIsReadUnsigned() {

        /* version 2, 4-byte offsets/lengths, one item with one extent
           at offset 0x90000000. */
        val payload = byteArrayOf(
            2, 0, 0, 0,
            0x44,
            0x00,
            0, 0, 0, 1,
            0, 0, 0, 1,
            0, 0,
            0, 0,
            0, 1,
            0x90.toByte(), 0, 0, 0,
            0, 0, 0, 16
        )

        val box = ItemLocationBox(
            offset = 0,
            size = payload.size.toLong() + 8,
            largeSize = null,
            payload = payload
        )

        assertEquals(0x90000000L, box.extents.single().offset)
    }
}
