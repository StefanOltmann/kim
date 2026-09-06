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
package de.stefan_oltmann.kim.output

import de.stefan_oltmann.kim.common.ImageWriteException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BufferByteWriterTest {

    /**
     * A single-byte write past the buffer capacity must fail loudly,
     * because silently growing a fixed buffer would corrupt the layout
     * the caller computed.
     */
    @Test
    fun testSingleByteWriteBeyondCapacityThrows() {

        val writer = BufferByteWriter(ByteArray(4), index = 0)

        writer.write(1)
        writer.write(2)
        writer.write(3)
        writer.write(4)

        assertFailsWith<ImageWriteException> {
            writer.write(5)
        }
    }

    /**
     * An array write that would cross the buffer capacity must fail
     * loudly and must not partially modify the buffer.
     */
    @Test
    fun testArrayWriteBeyondCapacityThrows() {

        val buffer = ByteArray(4)

        val writer = BufferByteWriter(buffer, index = 2)

        assertFailsWith<ImageWriteException> {
            writer.write(byteArrayOf(1, 2, 3))
        }

        /* The check runs before the copy, so the buffer is untouched. */
        assertEquals(0, buffer[2])
    }
}
