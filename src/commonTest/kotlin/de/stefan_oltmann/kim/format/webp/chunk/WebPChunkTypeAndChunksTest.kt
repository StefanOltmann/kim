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
package de.stefan_oltmann.kim.format.webp.chunk

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.format.webp.WebPChunkType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WebPChunkTypeAndChunksTest {

    @Test
    fun testChunkTypeOf() {

        assertEquals(WebPChunkType.VP8, WebPChunkType.of("VP8 ".encodeToByteArray()))
        assertEquals(WebPChunkType.VP8L, WebPChunkType.of("VP8L".encodeToByteArray()))
        assertEquals(WebPChunkType.VP8X, WebPChunkType.of("VP8X".encodeToByteArray()))
        assertEquals(WebPChunkType.EXIF, WebPChunkType.of("EXIF".encodeToByteArray()))
        assertEquals(WebPChunkType.XMP, WebPChunkType.of("XMP ".encodeToByteArray()))

        assertEquals("VP8 ", WebPChunkType.VP8.name)
        assertEquals("VP8L", WebPChunkType.VP8L.name)
    }

    @Test
    fun testChunkTypeEquality() {

        val first = WebPChunkType.of("VP8L".encodeToByteArray())
        val second = WebPChunkType.of("VP8L".encodeToByteArray())
        val other = WebPChunkType.of("VP8 ".encodeToByteArray())

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, other)
        assertNotEquals<Any>(first, "VP8L")
        assertEquals(first, first)
    }

    @Test
    fun testChunkTypeRejectsWrongLength() {

        assertFailsWith<IllegalArgumentException> {
            WebPChunkType.of("VP8".encodeToByteArray())
        }
    }

    @Test
    fun testBaseChunkToString() {

        assertEquals(
            expected = "WebPChunk 'VP8L' (3 bytes)",
            actual = WebPChunk(WebPChunkType.VP8L, byteArrayOf(1, 2, 3)).toString()
        )
    }

    @Test
    fun testXmpChunk() {

        val chunk = WebPChunkXmp("<x:xmpmeta>Test</x:xmpmeta>".encodeToByteArray())

        assertEquals("<x:xmpmeta>Test</x:xmpmeta>", chunk.xmp)
    }

    @Test
    fun testExifChunk() {

        val tiffBytes = Resource("de/stefan_oltmann/kim/updates_tif/empty.tif").readBytes()

        val chunk = WebPChunkExif(tiffBytes)

        assertTrue(chunk.tiffContents.directories.isNotEmpty())
    }
}
