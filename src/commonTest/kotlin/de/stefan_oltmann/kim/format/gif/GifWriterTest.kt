/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class GifWriterTest {

    /* language=XML */
    private val expectedXmp = """
        <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
          <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
            <rdf:Description rdf:about=""
                xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                xmlns:exif="http://ns.adobe.com/exif/1.0/"
              exif:DateTimeOriginal="2020-10-05T13:37:42"
              xmp:Rating="3"/>
          </rdf:RDF>
        </x:xmpmeta>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Tests that there is no loss if writing
     * the GIF chunks again without any change.
     */
    @Test
    fun testNoChange() {

        val bytes = KimTestData.getBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val byteWriter = ByteArrayByteWriter()

        GifWriter.writeImage(
            byteReader = byteReader,
            byteWriter = byteWriter,
            xmp = null
        )

        val newBytes = byteWriter.toByteArray()

        assertContentEquals(
            expected = bytes,
            actual = newBytes
        )
    }

    @Test
    fun testUpdateMetadata() {

        val bytes = KimTestData.getBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)

        val oldMetadata = Kim.readMetadata(bytes)

        assertNotNull(oldMetadata)

        val oldXmp = oldMetadata.xmp

        assertNotEquals(expectedXmp, oldXmp)

        val byteWriter = ByteArrayByteWriter()

        GifWriter.writeImage(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter,
            expectedXmp
        )

        val newBytes = byteWriter.toByteArray()

        val actualMetadata = Kim.readMetadata(newBytes)

        assertNotNull(actualMetadata)
        assertNotNull(actualMetadata.xmp)

        assertEquals(
            expected = expectedXmp,
            actual = actualMetadata.xmp
        )

        ModifiedBytesVerifier.verify(KimTestData.GIF_TEST_IMAGE_INDEX, "gif", newBytes)
    }

    /**
     * The XMP payload must be written in size-prefixed
     * sub-blocks, so that a strict GIF89a sub-block walker can extract it.
     */
    @Test
    fun testWriteXmpUsesSubBlockFraming() {

        val xmp = buildString {

            append("""<x:xmpmeta xmlns:x="adobe:ns:meta/">""")
            append("""<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""")

            repeat(20) { index ->
                append("""<rdf:Description rdf:about="" xmp:Rating="$index"/>""")
            }

            append("</rdf:RDF></x:xmpmeta>")
        }

        assertTrue(
            xmp.length > GifConstants.GIF_MAX_SUB_BLOCK_SIZE,
            "Test XMP must span multiple sub-blocks, but is ${xmp.length} chars."
        )

        val chunks = GifImageParser.readChunks(
            byteReader = ByteArrayByteReader(
                KimTestData.getHeaderBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)
            ),
            chunkTypeFilter = null
        )

        val byteWriter = ByteArrayByteWriter()

        GifWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            xmp = xmp
        )

        val extractedXmp = walkApplicationExtensionPayload(byteWriter.toByteArray())

        assertEquals(xmp, extractedXmp)
    }

    /**
     * Walks the GIF like a strict GIF89a parser and extracts the XMP payload
     * of the first application extension by following the sub-block sizes.
     */
    @Suppress("MagicNumber")
    private fun walkApplicationExtensionPayload(gif: ByteArray): String {

        val xmpStart = "<x:xmpmeta"
        val xmpEnd = "</x:xmpmeta>"

        var index = 0

        while (index < gif.size - 2) {

            if (gif[index].toInt() != 0x21 || (gif[index + 1].toInt() and 0xFF) != 0xFF) {

                index++

                continue
            }

            /* Skip the extension introducer, label and application block. */
            index += 2

            val blockSize = gif[index++].toInt()

            index += blockSize

            /* Walk the sub-blocks. */
            val payload = StringBuilder()

            while (true) {

                val subBlockSize = gif[index++].toInt() and 0xFF

                if (subBlockSize == 0)
                    break

                payload.append(gif.copyOfRange(index, index + subBlockSize).decodeToString())

                index += subBlockSize
            }

            val content = payload.toString()

            return xmpStart +
                content.substringAfter(xmpStart).substringBefore(xmpEnd) +
                xmpEnd
        }

        fail("No application extension found.")
    }
}
