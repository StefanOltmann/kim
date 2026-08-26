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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the serial key derivation against ExifTool's SerialKey
 * function in Nikon.pm, which is the reference implementation.
 *
 * A missing serial yields no key. A fully numeric serial is used as the
 * key itself. Alphanumeric serials must NOT be trimmed to their numeric
 * prefix. A model ending in D50 uses the fixed key 0x22, all others 0x60.
 */
class NikonDecryptorTest {

    @Test
    fun testNullSerialYieldsNoKey() {
        assertEquals(null, NikonDecryptor.serialKey(null, "NIKON D850"))
    }

    @Test
    fun testNumericSerialIsUsedAsKey() {

        assertEquals(3506117, NikonDecryptor.serialKey("3506117", "NIKON D850"))
        assertEquals(1, NikonDecryptor.serialKey("1", "NIKON Z 9"))
    }

    /**
     * The previous implementation trimmed non-digit suffixes and derived
     * the wrong key from the numeric prefix. ExifTool only accepts fully
     * numeric serials and falls back to the fixed keys otherwise.
     */
    @Test
    fun testAlphanumericSerialFallsBackToFixedKeys() {

        assertEquals(
            expected = 0x60,
            actual = NikonDecryptor.serialKey("123ABC", "NIKON D850")
        )

        assertEquals(
            expected = 0x60,
            actual = NikonDecryptor.serialKey("ABC123", "NIKON Z 6_2")
        )
    }

    @Test
    fun testD50ModelUsesFixedKey() {

        assertEquals(0x22, NikonDecryptor.serialKey("ABC123", "NIKON D50"))
        assertEquals(0x22, NikonDecryptor.serialKey("D50X-2", "D50"))

        /* A D500 or D5 is NOT a D50. */
        assertEquals(0x60, NikonDecryptor.serialKey("ABC123", "NIKON D500"))
        assertEquals(0x60, NikonDecryptor.serialKey("ABC123", "NIKON D5"))
    }
}
