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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A JPEG can carry multiple independent XMP APP1 packets. A rewrite
 * replaces the first packet - the one Kim reads - while additional
 * packets belong to other tools and must survive byte-exact, exactly
 * like the internal update path preserves them.
 */
class JpegXmpAdditionalPacketsTest {

    @Test
    fun testUpdateXmpXmlPreservesAdditionalPackets() {

        val original = KimTestData.getBytesOf(22)

        /* media_22 carries two independent XMP packets. */
        val secondPacket = assertNotNull(findXmpPackets(original).getOrNull(1))

        val writer = ByteArrayByteWriter()

        val replacementPacket = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
            </x:xmpmeta>
        """.trimIndent()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(original),
            byteWriter = writer,
            xmpXml = replacementPacket
        )

        val rewrittenPackets = findXmpPackets(writer.toByteArray())

        /*
         * The new packet replaces the first position; the additional
         * packet survives behind it, byte-exact.
         */
        assertEquals(2, rewrittenPackets.size)

        assertEquals(
            expected = secondPacket.toList(),
            actual = rewrittenPackets.last().toList()
        )
    }

    @Test
    fun testUpdateXmpXmlRemovesSplitPacketContinuation() {

        /*
         * A packet naively split across two standard APP1 segments: the
         * first carries the packet without its closing element, the
         * second completes it. The read concatenates both, so a rewrite
         * must remove the continuation with the packet it belongs to -
         * keeping it would leave the stale second half of the old
         * packet behind the new one as permanent garbage.
         */
        val firstHalf =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"><rdf:Description rdf:about="" xmp:Rating="3"/>"""

        val secondHalf = "</rdf:RDF></x:xmpmeta>"

        val byteWriter = ByteArrayByteWriter()

        /* SOI */
        byteWriter.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))

        writeSegment(
            byteWriter,
            JpegConstants.JPEG_APP1_MARKER,
            JpegConstants.XMP_IDENTIFIER + firstHalf.encodeToByteArray()
        )

        writeSegment(
            byteWriter,
            JpegConstants.JPEG_APP1_MARKER,
            JpegConstants.XMP_IDENTIFIER + secondHalf.encodeToByteArray()
        )

        /* SOS with minimal scan data and the EOI, like bareJpeg writes them. */
        byteWriter.write(
            bareJpeg().copyOfRange(2, bareJpeg().size)
        )

        val bytes = byteWriter.toByteArray()

        /* Sanity: the split packet reads as one complete packet. */
        val xmp = assertNotNull(Kim.readMetadata(bytes)).xmp

        assertTrue(xmp?.contains("xmp:Rating=\"3\"") == true)

        val writer = ByteArrayByteWriter()

        val replacementPacket = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
            </x:xmpmeta>
        """.trimIndent()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = writer,
            xmpXml = replacementPacket
        )

        val rewrittenPackets = findXmpPackets(writer.toByteArray())

        /* The new packet is the only standard XMP segment left. */
        assertEquals(1, rewrittenPackets.size)

        assertEquals(
            expected = (JpegConstants.XMP_IDENTIFIER + replacementPacket.encodeToByteArray()).toList(),
            actual = rewrittenPackets.single().toList()
        )
    }

    /**
     * Returns the payload of every XMP APP1 segment in marker order.
     */
    private fun findXmpPackets(bytes: ByteArray): List<ByteArray> {

        val packets = mutableListOf<ByteArray>()

        var index = 2

        while (index < bytes.size - 4) {

            if (bytes[index] != 0xFF.toByte() || bytes[index + 1] != 0xE1.toByte()) {
                index++
                continue
            }

            val segmentLength =
                ((bytes[index + 2].toInt() and 0xFF) shl 8) or (bytes[index + 3].toInt() and 0xFF)

            val payload = bytes.copyOfRange(index + 4, index + 2 + segmentLength)

            if (payload.size >= JpegConstants.XMP_IDENTIFIER.size &&
                payload.copyOf(JpegConstants.XMP_IDENTIFIER.size)
                    .contentEquals(JpegConstants.XMP_IDENTIFIER)
            )
                packets.add(payload)

            index += 2 + segmentLength
        }

        return packets
    }
}
