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
package de.stefan_oltmann.kim.input

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ByteArrayByteReaderTest {

    @Test
    fun testMoveToRejectsNegativePosition() {

        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3))

        assertFailsWith<IllegalArgumentException> {
            reader.moveTo(-1)
        }
    }

    @Test
    fun testMoveToAcceptsValidPositions() {

        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3))

        reader.moveTo(0)
        reader.moveTo(3)

        /* Reading at end returns null. */
        assertTrue(reader.readByte() == null)
    }

    @Test
    fun testMoveToRejectsPositionBeyondContent() {

        val reader = ByteArrayByteReader(byteArrayOf(1, 2, 3))

        assertFailsWith<IllegalArgumentException> {
            reader.moveTo(4)
        }
    }
}
