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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertEquals

class TiffReaderTest {

    /**
     * Regression test: entries with a corrupt count that makes the value
     * length negative must be skipped instead of crashing the parser.
     */
    @Test
    fun testReadSkipsEntryWithOverflowingCount() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0300" + // 3 entries
                "00010100ffffffff00000000" + // ImageWidth, BYTE, count 0xFFFFFFFF
                "010103007fffffff00000000" + // ImageLength, SHORT, count 0x7FFFFFFF
                "120103000100000001000000" + // Orientation, SHORT, count 1, value 1
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        val entries = tiffContents.directories.first().entries

        /* The two corrupt entries are skipped. */
        assertEquals(1, entries.size)

        assertEquals(0x0112, entries.single().tag)
        assertEquals("0100", entries.single().valueBytes.toHex())
    }
}
