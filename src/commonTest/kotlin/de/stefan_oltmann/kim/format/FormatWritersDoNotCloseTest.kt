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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.format.gif.GifWriter
import de.stefan_oltmann.kim.format.jxl.JxlWriter
import de.stefan_oltmann.kim.format.png.PngWriter
import de.stefan_oltmann.kim.format.webp.WebPWriter
import de.stefan_oltmann.kim.format.webp.chunk.WebPChunkVP8
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression tests: the format writers must not close the caller's
 * ByteWriter, so callers can keep using or wrap their writer.
 */
class FormatWritersDoNotCloseTest {

    @Test
    fun testGifWriterDoesNotCloseByteWriter() {

        val byteWriter = TrackingByteWriter()

        GifWriter.writeImage(
            chunks = emptyList(),
            byteWriter = byteWriter,
            xmp = null
        )

        assertEquals(0, byteWriter.closeCount)
    }

    @Test
    fun testPngWriterDoesNotCloseByteWriter() {

        val byteWriter = TrackingByteWriter()

        PngWriter.writeImage(
            chunks = emptyList(),
            byteWriter = byteWriter
        )

        assertEquals(0, byteWriter.closeCount)
    }

    @Test
    fun testWebPWriterDoesNotCloseByteWriter() {

        val byteWriter = TrackingByteWriter()

        val vp8Chunk = WebPChunkVP8(
            byteArrayOf(
                0x10, 0x00, 0x00, 0x9D.toByte(), 0x01, 0x2A,
                0x64, 0x00,
                0x64, 0x00
            )
        )

        WebPWriter.writeImage(
            chunks = listOf(vp8Chunk),
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )

        assertEquals(0, byteWriter.closeCount)
    }

    @Test
    fun testJxlWriterDoesNotCloseByteWriter() {

        val byteWriter = TrackingByteWriter()

        JxlWriter.writeImage(
            boxes = emptyList(),
            byteWriter = byteWriter,
            exifBytes = null,
            xmp = null
        )

        assertEquals(0, byteWriter.closeCount)
    }

    private class TrackingByteWriter : ByteWriter {

        var closeCount: Int = 0
            private set

        private val delegate = ByteArrayByteWriter()

        override fun write(byte: Byte) = delegate.write(byte)

        override fun write(byte: Int) = delegate.write(byte)

        override fun write(byteArray: ByteArray) = delegate.write(byteArray)

        override fun flush() = delegate.flush()

        override fun close() {
            closeCount++
            delegate.close()
        }
    }
}
