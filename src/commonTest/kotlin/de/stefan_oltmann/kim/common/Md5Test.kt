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
package de.stefan_oltmann.kim.common

import kotlin.test.Test
import kotlin.test.assertEquals

class Md5Test {

    private fun digestHex(input: String): String =
        Md5.digest(input.encodeToByteArray()).toHexString(HexFormat.UpperCase)

    /**
     * The reference digests are taken from RFC 1321 and must match on every
     * platform, because the GUID of Adobe extended XMP data is defined as
     * this digest.
     */
    @Test
    fun testDigestMatchesReferenceVectors() {

        assertEquals("D41D8CD98F00B204E9800998ECF8427E", digestHex(""))
        assertEquals("900150983CD24FB0D6963F7D28E17F72", digestHex("abc"))
        assertEquals(
            "9E107D9D372BB6826BD81D3542A419D6",
            digestHex("The quick brown fox jumps over the lazy dog")
        )
    }

    /**
     * Inputs longer than one 64-byte block exercise the chunk loop and the
     * multi-block padding path.
     */
    @Test
    fun testDigestOfLongInput() {

        assertEquals(
            MD5_OF_200_TIMES_A,
            digestHex("a".repeat(200))
        )
    }

    private companion object {

        /* MD5 of 200 times the character 'a'. */
        const val MD5_OF_200_TIMES_A: String = "887F30B43B2867F4A9ACCCEEE7D16E6C"
    }
}
