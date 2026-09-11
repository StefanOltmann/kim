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
package de.stefan_oltmann.kim.format.jpeg.iptc

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests APP13 data whose tail cannot be parsed as an 8BIM block.
 *
 * Per the strict read policy such a tail must fail the read instead of
 * being silently dropped: a subsequent update would rebuild the APP13
 * segment from the parsed blocks only, destroying the unparseable part.
 */
class IptcUnparseableTailTest {

    @Test
    fun testUnparseableApp13TailFailsTheRead() {

        /* The valid IPTC block followed by a junk word that is not an
         * 8BIM signature and has no 8BIM behind it either. */
        val bytes = convertHexStringToByteArray(IPTC_HEX) +
            convertHexStringToByteArray("CAFE0000")


        assertFailsWith<ImageReadException> {
            IptcParser.parseIptc(
                bytes = bytes,
                startsWithApp13Header = false
            )
        }
    }

    /**
     * A signature truncated by the end of the file carries no readable
     * content - like every EOF truncation it stops the parse gracefully.
     */
    @Test
    fun testParseToleratesTruncatedBlockSignature() {

        val bytes = convertHexStringToByteArray(IPTC_HEX) +
            convertHexStringToByteArray(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX)
                .copyOfRange(0, 2)

        val metadata = IptcParser.parseIptc(
            bytes = bytes,
            startsWithApp13Header = false
        )

        assertEquals(1, metadata.records.size)
    }
}
