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
package de.stefan_oltmann.kim.format.gif.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GifChunkApplicationExtensionTest {

    /**
     * Regression test: sub chunk sizes of 128 to 255 are legal, but the
     * size byte was read as a signed value, so such extensions failed
     * with an "Invalid size" error.
     */
    @Test
    fun testLargeFirstSubChunkSizeIsAccepted() {

        val identifier = "TESTAPP1".encodeToByteArray()

        val firstSubChunkData = ByteArray(200)

        identifier.copyInto(firstSubChunkData)

        val chunk = GifChunkApplicationExtension(
            header = byteArrayOf(0x21, 0xFF.toByte()),
            subChunks = listOf(byteArrayOf(200.toByte()) + firstSubChunkData)
        )

        assertEquals("TESTAPP1", chunk.applicationIdentifier)
    }

    /**
     * The application identifier needs 8 bytes, so smaller first sub
     * chunks must still be rejected.
     */
    @Test
    fun testSmallFirstSubChunkSizeIsRejected() {

        assertFailsWith<ImageReadException> {
            GifChunkApplicationExtension(
                header = byteArrayOf(0x21, 0xFF.toByte()),
                subChunks = listOf(byteArrayOf(4, 1, 2, 3, 4))
            )
        }
    }
}
