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
package de.stefan_oltmann.kim.format.printim

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.slice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The PrintIM layout is verified against media_20, whose block
 * ExifTool decodes identically: version "0250" and 20 fixed-size
 * tag/value entries.
 */
class PrintImParserTest {

    private val media20Bytes = Resource(
        "de/stefan_oltmann/kim/testdata/full/media_20.jpg"
    ).readBytes()

    @Test
    fun testParsesVersionAndEntries() {

        val directory = PrintImParser.parse(extractPrintImBytes())

        assertEquals("0250", directory.version)

        assertEquals(20, directory.entries.size)

        /* The first entry: ExifTool reports PrintIM_0x0001 = 1310740. */
        assertEquals(0x0001, directory.entries[0].tag)
        assertEquals(1310740, directory.entries[0].value)

        /* The second entry: ExifTool reports PrintIM_0x0002 = 1. */
        assertEquals(0x0002, directory.entries[1].tag)
        assertEquals(1, directory.entries[1].value)
    }

    @Test
    fun testRejectsMissingSignature() {

        val block = extractPrintImBytes()

        block[0] = 'X'.code.toByte()

        assertFailsWith<ImageReadException> {
            PrintImParser.parse(block)
        }
    }

    @Test
    fun testRejectsEntryCountBeyondTheBlock() {

        val block = extractPrintImBytes()

        /* The count sits at offset 14: 0xFFFF entries cannot fit. */
        block[14] = 0xFF.toByte()
        block[15] = 0xFF.toByte()

        assertFailsWith<ImageReadException> {
            PrintImParser.parse(block)
        }
    }

    /**
     * Cuts the PrintIM block out of the EXIF tag 0xC4A5 of the fixture:
     * it starts at the signature and is 260 bytes long.
     */
    private fun extractPrintImBytes(): ByteArray {

        val start = media20Bytes.indices
            .firstOrNull { index -> matchesSignatureAt(index) }
            ?: throw IllegalStateException("PrintIM signature not found")

        return media20Bytes.slice(start, 260)
    }

    private fun matchesSignatureAt(index: Int): Boolean =

        SIGNATURE_BYTES.indices.all { offset ->
            media20Bytes[index + offset] == SIGNATURE_BYTES[offset]
        }

    private companion object {

        val SIGNATURE_BYTES = byteArrayOf(
            0x50, 0x72, 0x69, 0x6E, 0x74, 0x49, 0x4D, 0x00
        )
    }
}
