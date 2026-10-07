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
package de.stefan_oltmann.kim.format.jpeg.xmp

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * XMP packets are UTF-8: a truncated or malformed sequence cannot be
 * read cleanly, so the parse fails instead of fabricating U+FFFD
 * replacement characters into titles and descriptions.
 */
class JpegXmpParserStrictUtf8Test {

    @Test
    fun testTruncatedUtf8SequenceFailsTheParse() {

        val identifier = "http://ns.adobe.com/xap/1.0/\u0000".encodeToByteArray()

        /* language=XML */
        val packet = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about="" dc:title="H
        """.trimIndent().encodeToByteArray()

        /*
         * A 2-byte UTF-8 lead byte (0xC3) with the continuation byte
         * replaced by ASCII: the sequence is malformed mid-title.
         */
        val corrupted = packet + byteArrayOf(0xC3.toByte(), 0x28) +
            """
                </rdf:RDF>
                </x:xmpmeta>
            """.trimIndent().encodeToByteArray()

        assertFailsWith<ImageReadException> {
            JpegXmpParser.parseXmpJpegSegment(identifier + corrupted)
        }
    }
}
