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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the shared enforcement point of the strict read policy for
 * embedded XMP packets.
 */
class XmpPacketValidationTest {

    @Test
    fun testNullPacketPassesThrough() {
        assertNull(requireValidXmpPacket(null, "The WebP XMP chunk"))
    }

    @Test
    fun testValidPacketPassesThrough() {

        val packet = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <rdf:Description rdf:about=""/>
                  </rdf:RDF>
                </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        assertEquals(packet, requireValidXmpPacket(packet, "The WebP XMP chunk"))
    }

    @Test
    fun testPacketWithoutXmpmetaElementIsRejected() {

        assertFailsWith<ImageReadException> {
            requireValidXmpPacket("<html><body>Not XMP</body></html>", "The JXL XML box")
        }
    }

    /**
     * A packet must carry both the opening and the closing element. A
     * packet cut off between them is incomplete: silently returning it
     * would hand sidecar writers metadata that only looks complete.
     */
    @Test
    fun testTruncatedPacketIsRejected() {

        val truncated = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF"

        val exception = assertFailsWith<ImageReadException> {
            requireValidXmpPacket(truncated, "The CR3 XMP UUID box")
        }

        assertTrue(
            exception.message?.contains("closing", ignoreCase = true) == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * The container check is deliberately shape-based: a minimal XMP
     * envelope passes as long as both elements are present. Whether the
     * packet parses as full XMP is decided by the conversion and update
     * layers.
     */
    @Test
    fun testMinimalEnvelopePassesContainerCheck() {

        val envelope = "<x:xmpmeta></x:xmpmeta>"

        assertEquals(envelope, requireValidXmpPacket(envelope, "The CR3 XMP UUID box"))
    }

    /**
     * A bare rdf:RDF root without the recommended x:xmpmeta envelope is
     * a complete, xmpcore-parseable packet the XMP specification allows,
     * so the container check must accept it.
     */
    @Test
    fun testBareRdfPacketPassesContainerCheck() {

        /* language=XML */
        val packet = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" dc:title="x"/>
            </rdf:RDF>
        """.trimIndent()

        assertEquals(
            packet,
            requireValidXmpPacket(packet, "The test chunk")
        )
    }

    /**
     * A bare RDF packet cut off before its closing element is truncated
     * content and must fail like its enveloped twin.
     */
    @Test
    fun testTruncatedBareRdfPacketFailsContainerCheck() {

        /* language=XML */
        val packet = """
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about=""
        """.trimIndent()

        assertFailsWith<ImageReadException> {
            requireValidXmpPacket(packet, "The test chunk")
        }
    }
}
