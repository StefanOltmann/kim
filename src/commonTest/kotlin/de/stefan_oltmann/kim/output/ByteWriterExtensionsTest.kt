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

import de.stefan_oltmann.kim.common.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteWriterExtensionsTest {

    @Test
    fun testWrite2BytesAsInt() {

        val writer = ByteArrayByteWriter()

        writer.write2BytesAsInt(0x0102, ByteOrder.BIG_ENDIAN)
        writer.write2BytesAsInt(0x0102, ByteOrder.LITTLE_ENDIAN)

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x02, 0x01),
            actual = writer.toByteArray()
        )
    }

    @Test
    fun testWriteInt() {

        val writer = ByteArrayByteWriter()

        writer.writeInt(0x01020304, ByteOrder.BIG_ENDIAN)
        writer.writeInt(0x01020304, ByteOrder.LITTLE_ENDIAN)

        assertContentEquals(
            expected = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x04, 0x03, 0x02, 0x01),
            actual = writer.toByteArray()
        )
    }

    @Test
    fun testWriteLong() {

        val writer = ByteArrayByteWriter()

        writer.writeLong(0x0102030405060708L, ByteOrder.BIG_ENDIAN)
        writer.writeLong(0x0102030405060708L, ByteOrder.LITTLE_ENDIAN)

        val expected = byteArrayOf(
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01
        )

        assertContentEquals(expected, writer.toByteArray())
    }

    @Test
    fun testWriteString() {

        val writer = ByteArrayByteWriter()

        writer.writeString("Hi")

        assertEquals("Hi", writer.toByteArray().decodeToString())
    }
}
