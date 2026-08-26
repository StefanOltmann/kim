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

import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CopyByteReaderTest {

    /**
     * The whole point of this reader: consumed bytes stay available for
     * later re-reads on forward-only streams.
     */
    @Test
    fun testConsumedBytesAreRetained() {

        val copyByteReader = CopyByteReader(
            ByteArrayByteReader(byteArrayOf(1, 2, 3))
        )

        assertContentEquals(byteArrayOf(1, 2), copyByteReader.readBytes(2))

        assertContentEquals(byteArrayOf(1, 2), copyByteReader.getBytes())
    }

    @Test
    fun testSingleBytesAreRetained() {

        val copyByteReader = CopyByteReader(
            ByteArrayByteReader(byteArrayOf(7, 8))
        )

        assertEquals(7.toByte(), copyByteReader.readByte())
        assertEquals(8.toByte(), copyByteReader.readByte())

        assertContentEquals(byteArrayOf(7, 8), copyByteReader.getBytes())
    }

    /**
     * Reads beyond the end return short arrays, and only the actually
     * read bytes may end up in the copy.
     */
    @Test
    fun testReadPastEndRetainsOnlyRealBytes() {

        val copyByteReader = CopyByteReader(
            ByteArrayByteReader(byteArrayOf(1, 2))
        )

        assertContentEquals(byteArrayOf(1, 2), copyByteReader.readBytes(10))

        assertContentEquals(byteArrayOf(1, 2), copyByteReader.getBytes())
    }
}
