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

import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests that a next-IFD chain that cycles back to an earlier directory
 * terminates instead of spinning forever.
 *
 * A corrupt or hostile TIFF - in any container that embeds TIFF, like
 * JPEG APP1 - can point the next-IFD pointer of the third directory back
 * to the second one. ExifTool registers every processed directory start
 * position and warns "pointer references previous directory" when one is
 * revisited, so the parse terminates.
 *
 * A JUnit timeout guards this test, because the pre-fix behavior is an
 * infinite loop. kotlin.test has no equivalent for the timeout attribute.
 */
class TiffChainCycleTest {

    @Test(timeout = 10_000)
    fun testTerminatesOnNextIfdChainCycle() {

        val bytes = buildChainCycleTiff()

        val contents = TiffReader.read(DefaultRandomAccessByteReader(ByteArrayByteReader(bytes)))

        /* IFD0 and the two chain successors, each visited exactly once. */
        assertEquals(3, contents.directories.size)
    }

    /**
     * Three entry-less directories at offsets 8, 26 and 40 whose next-IFD
     * pointers form the cycle 8 -> 26 -> 40 -> 26.
     */
    private fun buildChainCycleTiff(): ByteArray {

        val bytes = ByteArray(46)

        bytes[0] = 0x49
        bytes[1] = 0x49
        bytes[2] = 0x2A
        bytes[3] = 0x00

        /* IFD0 at offset 8. */
        bytes[4] = 0x08

        /* Directory A at 8: no entries, next directory at 26. */
        bytes[10] = 0x1A

        /* Directory B at 26: no entries, next directory at 40. */
        bytes[28] = 0x28

        /* Directory C at 40: no entries, next directory back at 26. */
        bytes[42] = 0x1A

        return bytes
    }
}
