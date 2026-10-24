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
package de.stefan_oltmann.kim.format.bmff

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Pins the copy semantics of a payload window: materializing it must
 * yield exactly its region of the parent buffer, whatever the box walk
 * read through the parent buffer before.
 */
class PayloadSourceTest {

    @Test
    fun testWindowBytesCopyTheWindowRegion() {

        val parentBuffer = ByteArray(16) { index -> index.toByte() }

        val source = PayloadSource.ofWindow(parentBuffer, offset = 4, length = 6)

        assertEquals(expected = 6, actual = source.length)
        assertContentEquals(
            expected = byteArrayOf(4, 5, 6, 7, 8, 9),
            actual = source.bytes()
        )
    }
}
