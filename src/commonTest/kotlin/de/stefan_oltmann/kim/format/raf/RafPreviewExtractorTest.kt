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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RafPreviewExtractorTest {

    private fun rafBytesWithJpegHeaderAt(offset: Int, length: Int): ByteArray {

        val bytes = ByteArray(92)

        MediaFormatMagicNumbers.raf.toByteArray().copyInto(bytes)

        /* JPEG offset and length, big-endian, behind the 68 header bytes. */
        bytes[84] = (offset ushr 24).toByte()
        bytes[85] = (offset ushr 16).toByte()
        bytes[86] = (offset ushr 8).toByte()
        bytes[87] = offset.toByte()
        bytes[88] = (length ushr 24).toByte()
        bytes[89] = (length ushr 16).toByte()
        bytes[90] = (length ushr 8).toByte()
        bytes[91] = length.toByte()

        return bytes
    }

    /**
     * Hostile preview offsets and lengths must degrade to NULL instead
     * of crashing or returning garbage.
     */
    @Test
    fun testExtractPreviewImageDegradesToNullForHostileRanges() {

        fun readerOf(vararg bytes: Byte): ByteReader = ByteArrayByteReader(bytes)

        /* Offset beyond the file. */
        assertNull(
            RafPreviewExtractor.extractPreviewImage(
                readerOf(*rafBytesWithJpegHeaderAt(1000, 16))
            )
        )

        /* Length beyond the file. */
        assertNull(
            RafPreviewExtractor.extractPreviewImage(
                readerOf(*rafBytesWithJpegHeaderAt(88, 1000))
            )
        )

        /* Offset and length that overflow when summed. */
        assertNull(
            RafPreviewExtractor.extractPreviewImage(
                readerOf(*rafBytesWithJpegHeaderAt(Int.MAX_VALUE, 100))
            )
        )
    }

    /**
     * A valid in-file preview range without JPEG data degrades to NULL.
     */
    @Test
    fun testExtractPreviewImageDegradesToNullWithoutJpegData() {

        assertEquals(
            null,
            RafPreviewExtractor.extractPreviewImage(
                ByteArrayByteReader(rafBytesWithJpegHeaderAt(88, 4))
            )
        )
    }
}
