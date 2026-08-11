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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * The test is placed in jvmTest, because the reader lives in jvmMain.
 */
class JvmInputStreamByteReaderTest {

    @Test
    fun testReadByte() {

        val reader = JvmInputStreamByteReader(
            inputStream = ByteArrayInputStream(byteArrayOf(1, 2)),
            contentLength = 2
        )

        assertEquals(1.toByte(), reader.readByte())
        assertEquals(2.toByte(), reader.readByte())

        /* The end of the stream. */
        assertNull(reader.readByte())
    }

    @Test
    fun testReadBytes() {

        val reader = JvmInputStreamByteReader(
            inputStream = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)),
            contentLength = 4
        )

        assertContentEquals(
            expected = byteArrayOf(1, 2),
            actual = reader.readBytes(2)
        )

        /* Reads beyond the end return a short array. */
        assertContentEquals(
            expected = byteArrayOf(3, 4),
            actual = reader.readBytes(10)
        )
    }

    @Test
    fun testClose() {

        val closed = AtomicBoolean(false)

        val stream = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun close() {
                closed.set(true)
            }
        }

        JvmInputStreamByteReader(stream, 1).close()

        assertTrue(closed.get())
    }

    @Test
    fun testOutputStreamByteWriter() {

        val outputStream = ByteArrayOutputStream()

        val writer = de.stefan_oltmann.kim.output.OutputStreamByteWriter(outputStream)

        writer.write(1.toByte())
        writer.write(2)
        writer.write(byteArrayOf(3, 4))
        writer.flush()

        assertContentEquals(byteArrayOf(1, 2, 3, 4), outputStream.toByteArray())

        writer.close()
    }
}
