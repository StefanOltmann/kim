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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.jpeg.xmp.ExtendedXmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExtendedXmpTest {

    /**
     * Extended XMP data referenced by the main packet must be merged back
     * in on read, so the properties of both packets are visible.
     */
    @Test
    fun testReadMetadataMergesExtendedXmp() {

        val extendedXml =
            MINIMAL_HEADER +
                "<rdf:Description rdf:about=\"\" " +
                "xmlns:custom=\"http://example.com/custom/\">" +
                "<custom:Extra>EXTENDED_VALUE</custom:Extra>" +
                "</rdf:Description>" +
                MINIMAL_FOOTER

        val guid = digestAsGuid(extendedXml)

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(guid),
            extensionPayloads = listOf(buildExtensionPayload(guid, extendedXml))
        )

        val xmp = Kim.readMetadata(jpegBytes)?.xmp

        assertNotNull(xmp)
        assertFalse(!xmp.contains("Main Title"))
        assertFalse(!xmp.contains("EXTENDED_VALUE"))
    }

    /**
     * A main packet that references extended data without the matching
     * segments existing must fail loudly instead of silently dropping the
     * referenced properties - a rewrite would destroy them otherwise.
     */
    @Test
    fun testReadMetadataRejectsMissingExtendedXmp() {

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(GUID),
            extensionPayloads = emptyList()
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(jpegBytes)
        }
    }

    /**
     * Extension segments with a foreign GUID belong to another packet and
     * do not satisfy the reference of the main packet.
     */
    @Test
    fun testReadMetadataRejectsForeignGuidExtensions() {

        val foreignGuid = "1234567890ABCDEF1234567890ABCDEF"

        val extendedXml =
            MINIMAL_HEADER +
                "<rdf:Description rdf:about=\"\" " +
                "xmlns:custom=\"http://example.com/custom/\">" +
                "<custom:Extra>EXTENDED_VALUE</custom:Extra>" +
                "</rdf:Description>" +
                MINIMAL_FOOTER

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(GUID),
            extensionPayloads = listOf(buildExtensionPayload(foreignGuid, extendedXml))
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(jpegBytes)
        }
    }

    /**
     * Tampered extended data must be rejected via its MD5 checksum instead
     * of being merged silently.
     */
    @Test
    fun testReadMetadataRejectsCorruptExtendedXmp() {

        val extendedXml =
            MINIMAL_HEADER +
                "<rdf:Description rdf:about=\"\" " +
                "xmlns:custom=\"http://example.com/custom/\">" +
                "<custom:Extra>EXTENDED_VALUE</custom:Extra>" +
                "</rdf:Description>" +
                MINIMAL_FOOTER

        val tamperedXml = extendedXml.replace("EXTENDED_VALUE", "TAMPERED__VALUE")

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(digestAsGuid(extendedXml)),
            extensionPayloads = listOf(
                buildExtensionPayload(digestAsGuid(extendedXml), tamperedXml)
            )
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(jpegBytes)
        }
    }

    /**
     * A single XMP schema that exceeds one full segment cannot stay in the
     * main packet. It is moved to the extended data completely, which has
     * unlimited size, and the main packet keeps only the GUID reference.
     */
    @Test
    fun testPartitionMovesSingleHugeSchemaToExtendedData() {

        val hugeValue = "x".repeat(JpegConstants.MAX_XMP_BYTES_PER_SEGMENT + 100)

        val hugeXmp =
            """<?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>""" +
                """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about="" xmlns:custom="http://example.com/custom/">""" +
                "<custom:Big>$hugeValue</custom:Big>" +
                "</rdf:Description>" +
                "</rdf:RDF></x:xmpmeta>" +
                """<?xpacket end="w"?>"""

        val partitioned = ExtendedXmpWriter.partition(hugeXmp)

        /* The main packet fits into a single segment and carries only the reference. */
        assertTrue(partitioned.mainPacketXml.encodeToByteArray().size <= JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
        assertTrue(partitioned.mainPacketXml.contains("HasExtendedXMP"))
        assertFalse(partitioned.mainPacketXml.contains(hugeValue))

        /* The extension segments reassemble to the complete extended data. */
        assertFalse(partitioned.extensionSegmentPayloads.isEmpty())

        val reassembled = ByteArrayByteWriter()

        for (payload in partitioned.extensionSegmentPayloads)
            reassembled.write(payload.copyOfRange(EXTENDED_XMP_HEADER_BYTES, payload.size))

        val extendedBytes = reassembled.toByteArray()

        assertTrue(extendedBytes.decodeToString().contains(hugeValue))

        assertEquals(
            digestAsGuid(extendedBytes.decodeToString()),
            extractGuidFromMainPacket(partitioned.mainPacketXml)
        )
    }

    /**
     * An oversized packet whose properties use legal RDF node elements
     * other than rdf:Description cannot be split into main and extended
     * data. It must fail the write instead of being written as a nearly
     * empty packet, which would destroy all XMP properties.
     */
    @Test
    fun testPartitionRejectsUnrecognizedNodeElements() {

        val hugeValue = "x".repeat(JpegConstants.MAX_XMP_BYTES_PER_SEGMENT + 100)

        val hugeXmp =
            """<?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>""" +
                """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<photo:Image rdf:about="" xmlns:photo="http://ns.adobe.com/photo/1.0/">""" +
                "<photo:Big>$hugeValue</photo:Big>" +
                "</photo:Image>" +
                "</rdf:RDF></x:xmpmeta>" +
                """<?xpacket end="w"?>"""

        assertFailsWith<ImageWriteException> {
            ExtendedXmpWriter.partition(hugeXmp)
        }
    }

    /**
     * deleteMetadata removes standard and extended XMP segments alike.
     */
    @Test
    fun testDeleteMetadataRemovesExtendedXmpSegments() {

        val extendedXml =
            MINIMAL_HEADER +
                "<rdf:Description rdf:about=\"\" " +
                "xmlns:custom=\"http://example.com/custom/\">" +
                "<custom:Extra>EXTENDED_VALUE</custom:Extra>" +
                "</rdf:Description>" +
                MINIMAL_FOOTER

        val guid = digestAsGuid(extendedXml)

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(guid),
            extensionPayloads = listOf(buildExtensionPayload(guid, extendedXml))
        )

        val cleanedBytes = Kim.deleteMetadata(jpegBytes)

        assertFalse(cleanedBytes.decodeToString().contains("Main Title"))
        assertFalse(cleanedBytes.decodeToString().contains("HasExtendedXMP"))
    }

    /**
     * A packet that fits into a single segment but still carries the
     * reference of a previous extended-XMP write must have that stale
     * reference removed. Writing it verbatim produces a file that
     * references extended data that does not exist, so no subsequent
     * read of it succeeds.
     */
    @Test
    fun testUpdateXmpXmlRemovesStaleExtendedXmpReference() {

        /* Standard XMP without any extended reference. */
        val plainPacket = """
            <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
               <dc:title><rdf:Alt><rdf:li xml:lang="x-default">Main Title</rdf:li></rdf:Alt></dc:title>
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = plainPacket,
            extensionPayloads = emptyList()
        )

        /* A packet as a reader round-trips it: it still contains the
           reference of a previous extended-XMP write. */
        val packetWithStaleReference = buildMainPacket(GUID)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(jpegBytes),
            byteWriter = byteWriter,
            xmpXml = packetWithStaleReference
        )

        val newBytes = byteWriter.toByteArray()

        assertFalse(newBytes.decodeToString().contains(GUID))

        /* The output must remain readable. */
        assertNotNull(Kim.readMetadata(newBytes))
    }

    /**
     * Adobe extended XMP chunks are placed at their 4-byte offset inside
     * the complete data. Fragments that arrive out of file order must be
     * assembled by offset, and a gap between chunks must fail loudly
     * instead of producing silently shifted data.
     */
    @Test
    fun testExtendedXmpAssemblesByChunkOffset() {

        val partOne =
            """<rdf:Description rdf:about="" xmlns:custom="http://example.com/custom">""" +
                "<custom:Part>ONE</custom:Part></rdf:Description>"

        val partTwo =
            """<rdf:Description rdf:about="" xmlns:custom2="http://example.com/custom2">""" +
                "<custom2:Part>TWO</custom2:Part></rdf:Description>"

        val extendedXml = MINIMAL_HEADER + partOne + partTwo + MINIMAL_FOOTER

        val extendedBytes = extendedXml.encodeToByteArray()

        val firstChunk = extendedBytes.copyOfRange(0, 100)

        val secondChunk = extendedBytes.copyOfRange(100, extendedBytes.size)

        val guid = digestAsGuid(extendedXml)

        /* The chunks are written in reverse file order. */
        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(guid),
            extensionPayloads = listOf(
                buildExtensionPayload(
                    guid,
                    secondChunk.decodeToString(),
                    chunkOffset = 100,
                    totalLength = extendedBytes.size
                ),
                buildExtensionPayload(
                    guid,
                    firstChunk.decodeToString(),
                    chunkOffset = 0,
                    totalLength = extendedBytes.size
                )
            )
        )

        val metadata = assertNotNull(Kim.readMetadata(jpegBytes))

        val xmp = assertNotNull(metadata.xmp)

        assertTrue(xmp.contains("ONE"))
        assertTrue(xmp.contains("TWO"))
    }

    /**
     * A gap between the chunk offsets means the extended data is
     * incomplete. The read must fail loudly instead of merging shifted
     * fragments.
     */
    @Test
    fun testReadMetadataRejectsGapBetweenChunkOffsets() {

        val extendedXml = MINIMAL_HEADER +
            "<rdf:Description rdf:about=\"\" xmlns:custom=\"http://example.com/custom\">" +
            "<custom:Part>ONE</custom:Part></rdf:Description>" +
            MINIMAL_FOOTER

        val guid = digestAsGuid(extendedXml)

        val jpegBytes = createJpegWithExtendedXmp(
            mainPacket = buildMainPacket(guid),
            /* The chunk claims offset 200 behind a 50-byte first chunk,
               leaving a gap of 150 bytes. */
            extensionPayloads = listOf(
                buildExtensionPayload(guid, extendedXml, chunkOffset = 200)
            )
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(jpegBytes)
        }
    }

    /**
     * A third-party writer may place the stale extended-XMP reference in
     * the same rdf:Description element as real properties. The surgical
     * reference removal must preserve those siblings, and the
     * regeneration must reference the reassembled extended data.
     */
    @Test
    fun testPartitionKeepsSiblingsOfStaleReferenceBlock() {

        val hugeValue = "x".repeat(JpegConstants.MAX_XMP_BYTES_PER_SEGMENT + 100)

        /* One description carries both the stale reference (attribute
           form) and a real property. */
        val hugeXmp =
            """<?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>""" +
                """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about="" xmlns:custom="http://example.com/custom" """ +
                """xmlns:xmpNote="http://ns.adobe.com/xmp/note/" """ +
                """xmpNote:HasExtendedXMP="$GUID">""" +
                "<custom:Big>$hugeValue</custom:Big>" +
                "<custom:Headline>KEEP</custom:Headline>" +
                "</rdf:Description>" +
                "</rdf:RDF></x:xmpmeta>" +
                """<?xpacket end="w"?>"""

        val partitioned = ExtendedXmpWriter.partition(hugeXmp)

        /* The stale reference is regenerated with a fresh GUID. */
        assertTrue(partitioned.mainPacketXml.contains("HasExtendedXMP"))
        assertFalse(partitioned.mainPacketXml.contains(GUID))

        /* The sibling property must survive in the extended data. */
        val extendedData = ByteArrayByteWriter()

        for (payload in partitioned.extensionSegmentPayloads)
            extendedData.write(payload.copyOfRange(EXTENDED_XMP_HEADER_BYTES, payload.size))

        assertTrue(extendedData.toByteArray().decodeToString().contains("KEEP"))
    }

    /*
     * ------------------------------------------------------------------
     * Fixture helpers
     * ------------------------------------------------------------------
     */

    private fun buildMainPacket(guid: String): String {

        /*
         * Uses the shorthand attribute form of the extended XMP reference,
         * so both serialization forms are covered by the tests.
         */
        return """
            <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
               <dc:title><rdf:Alt><rdf:li xml:lang="x-default">Main Title</rdf:li></rdf:Alt></dc:title>
              </rdf:Description>
              <rdf:Description rdf:about=""
               xmlns:xmpNote="http://ns.adobe.com/xmp/note/"
               xmpNote:HasExtendedXMP="$guid"/>
             </rdf:RDF>
            </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()
    }

    /**
     * Builds an extended XMP APP1 segment payload:
     * identifier, GUID, total length, chunk.
     */
    private fun buildExtensionPayload(
        guid: String,
        chunkData: String,
        chunkOffset: Int = 0,
        totalLength: Int = chunkData.encodeToByteArray().size
    ): ByteArray {

        val extendedBytes = chunkData.encodeToByteArray()

        val writer = ByteArrayByteWriter()

        writer.write(convertHexStringToByteArray(EXTENDED_XMP_IDENTIFIER_HEX))
        writer.write(guid.encodeToByteArray())
        writer.write(
            byteArrayOf(
                (totalLength ushr 24).toByte(),
                (totalLength ushr 16).toByte(),
                (totalLength ushr 8).toByte(),
                totalLength.toByte()
            )
        )
        writer.write(
            byteArrayOf(
                (chunkOffset ushr 24).toByte(),
                (chunkOffset ushr 16).toByte(),
                (chunkOffset ushr 8).toByte(),
                chunkOffset.toByte()
            )
        )
        writer.write(extendedBytes)

        return writer.toByteArray()
    }

    /**
     * Builds a minimal JPEG with SOI, one standard XMP segment, the given
     * extension segments, and a minimal scan.
     */
    private fun createJpegWithExtendedXmp(
        mainPacket: String,
        extensionPayloads: List<ByteArray>
    ): ByteArray {

        val bytes = ByteArrayByteWriter()

        bytes.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte())) /* SOI */

        val xmpPayload =
            convertHexStringToByteArray(XMP_IDENTIFIER_HEX) + mainPacket.encodeToByteArray()

        writeSegment(bytes, 0xE1, xmpPayload)

        for (payload in extensionPayloads)
            writeSegment(bytes, 0xE1, payload)

        /* SOS with minimal scan data. */
        bytes.write(byteArrayOf(0xFF.toByte(), 0xDA.toByte(), 0, 8, 1, 1, 0, 0, 63.toByte(), 0))
        bytes.write(byteArrayOf(0x11, 0x22, 0x33, 0x44))
        bytes.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte())) /* EOI */

        return bytes.toByteArray()
    }

    private fun writeSegment(writer: ByteArrayByteWriter, marker: Int, payload: ByteArray) {

        writer.write(byteArrayOf(0xFF.toByte(), marker.toByte()))

        val length = payload.size + 2

        writer.write(byteArrayOf((length ushr 8).toByte(), length.toByte()))
        writer.write(payload)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun digestAsGuid(text: String): String =
        Md5.digest(text.encodeToByteArray()).toHexString(HexFormat.UpperCase)

    private fun extractGuidFromMainPacket(mainPacketXml: String): String {

        val openTag = "<xmpNote:HasExtendedXMP>"
        val closeTag = "</xmpNote:HasExtendedXMP>"

        val start = mainPacketXml.indexOf(openTag) + openTag.length
        val end = mainPacketXml.indexOf(closeTag)

        return mainPacketXml.substring(start, end)
    }

    companion object {

        /** Identifier (35) + GUID (32) + total length (4) + offset (4). */
        private const val EXTENDED_XMP_HEADER_BYTES: Int = 75

        private const val GUID: String = "00112233445566778899AABBCCDDEEFF"

        /* "http://ns.adobe.com/xap/1.0/\0" */
        private const val XMP_IDENTIFIER_HEX: String =
            "687474703A2F2F6E732E61646F62652E636F6D2F7861702F312E302F00"

        /* "http://ns.adobe.com/xmp/extension/\0" */
        private const val EXTENDED_XMP_IDENTIFIER_HEX: String =
            "687474703A2F2F6E732E61646F62652E636F6D2F786D702F657874656E73696F6E2F00"

        private const val MINIMAL_HEADER: String =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">"""

        private const val MINIMAL_FOOTER: String = "</rdf:RDF></x:xmpmeta>"
    }
}
