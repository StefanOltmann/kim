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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.OutputStreamByteWriter
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

    /**
     * A non-positive content length hint means the size is unknown - a
     * provider that does not report a size commonly delivers zero. The
     * reader must report such hints as unbounded, so parsers treat the
     * stream end as the only truncation evidence instead of rejecting
     * valid content as truncated.
     */
    @Test
    fun testNonPositiveContentLengthHintReportsUnbounded() {

        assertEquals(
            Long.MAX_VALUE,
            JvmInputStreamByteReader(ByteArrayInputStream(byteArrayOf(1)), contentLength = 0)
                .contentLength
        )

        assertEquals(
            Long.MAX_VALUE,
            JvmInputStreamByteReader(ByteArrayInputStream(byteArrayOf(1)), contentLength = -1)
                .contentLength
        )

        /* A trustworthy hint passes through unchanged. */
        assertEquals(
            2,
            JvmInputStreamByteReader(ByteArrayInputStream(byteArrayOf(1, 2)), contentLength = 2)
                .contentLength
        )
    }

    /**
     * A streamed rewrite of a valid photo whose reader reports an unknown
     * size (hint 0) must succeed - the stream end decides, never the
     * missing hint.
     */
    @Test
    fun testStreamingUpdateWithUnknownContentLengthSucceeds() {

        val source = ByteArrayInputStream(minimalJpegWithApp0Segment())

        val output = ByteArrayOutputStream()

        Kim.update(
            byteReader = JvmInputStreamByteReader(source, contentLength = 0),
            byteWriter = OutputStreamByteWriter(output),
            updates = setOf(MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT))
        )

        val metadata = Kim.readMetadata(output.toByteArray())

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.of(metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        )
    }

    /**
     * Builds a minimal JPEG with a JFIF APP0 segment in front of the SOS
     * marker and entropy-coded image data whose bytes stay below 0x80, so
     * no image byte can be mistaken for a marker.
     */
    @Suppress("MagicNumber")
    private fun minimalJpegWithApp0Segment(): ByteArray {

        val imageDataSize = 4096

        val writer = de.stefan_oltmann.kim.output.ByteArrayByteWriter()

        writer.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte())) // SOI

        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10,
                0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
                0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
            )
        ) // APP0 JFIF

        /* SOS with minimal parameters and entropy-coded data. */
        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xDA.toByte(), 0x00, 0x08,
                0x01, 0x01, 0x00, 0x00, 0x3F, 0x00
            )
        )

        writer.write(ByteArray(imageDataSize) { index -> (index % 0x7F).toByte() })

        writer.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte())) // EOI

        return writer.toByteArray()
    }
}
