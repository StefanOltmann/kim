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

import de.stefan_oltmann.kim.ktor.KimKtor
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Host tests for the kotlinx-io backed reader: the contentLength hint
 * must never gate the reads.
 */
class KotlinIoSourceByteReaderHostTest {

    private val jpegBytes = byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), // SOI + APP0.
        0, 2, 0x4A, 0x46, 0x49, 0x46, 0, 1, // Length 2, "JFIF", version...
        0xFF.toByte(), 0xD9.toByte() // EOI.
    )

    /**
     * A contentLength hint of 0 (chunked HTTP without a Content-Length
     * header) must not gate the reads: the data comes from the source,
     * not from the hint.
     */
    @Test
    fun testContentLengthZeroDoesNotGateReads() {

        val buffer = Buffer().apply { write(jpegBytes) }

        val reader = KotlinIoSourceByteReader(buffer, contentLength = 0)

        val read = reader.readBytes(jpegBytes.size)

        assertEquals(jpegBytes.size.toLong(), read.size.toLong())
    }

    /**
     * A contentLength hint beyond the real data must produce a short
     * read instead of propagating an EOF error from the source.
     */
    @Test
    fun testOverstatedContentLengthShortReads() {

        val buffer = Buffer().apply { write(jpegBytes) }

        val reader = KotlinIoSourceByteReader(buffer, contentLength = 100)

        assertContentEquals(jpegBytes, reader.readBytes(jpegBytes.size))
    }

    /**
     * End-to-end: the Ktor entry point with a zero length hint must read
     * metadata from the buffer instead of reporting null.
     */
    @Test
    fun testKimKtorWithZeroLengthHintReadsMetadata() {

        val bytes = KimTestData.getBytesOf(2) // A plain JPEG test image.

        val metadata = KimKtor.readMetadata(
            Buffer().apply { write(bytes) },
            contentLength = 0
        )

        assertNotNull(metadata)
    }
}
