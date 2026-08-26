/*
 * Copyright 2026 Stefan Oltmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may obtain a copy of the License at
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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Splits oversized XMP packets using the Adobe extended XMP scheme, exactly
 * like ExifTool and the Adobe SDK do it: the main packet keeps as many whole
 * "rdf:Description" blocks as fit into a single APP1 segment and references
 * the remaining data through "xmpNote:HasExtendedXMP"; the moved blocks are
 * serialized into extension segments that carry the MD5 GUID of the extended
 * data, its total length and one chunk of it each.
 *
 * This is the mechanism specified by Adobe (XMP specification, 2008) and is
 * read back by ExifTool, Photoshop and Lightroom. Naive byte splitting of a
 * packet across standard XMP segments would produce truncated XML that no
 * third-party reader accepts.
 */
internal object ExtendedXmpWriter {

    private const val RDF_OPEN_TAG = "<rdf:RDF"

    private const val RDF_CLOSE_TAG = "</rdf:RDF>"

    private const val DESCRIPTION_OPEN_TAG = "<rdf:Description"

    /*
     * A stale "xmpNote:HasExtendedXMP" reference from a previous write must
     * not survive: Kim regenerates it whenever extended data is written,
     * like ExifTool, which deletes the tag because "we create it as needed".
     */
    private const val STALE_REFERENCE_MARKER = "xmpNote:HasExtendedXMP"

    /*
     * Minimal self-contained wrapper for the extended data. The moved
     * rdf:Description blocks declare their own namespaces, so no further
     * declarations are required here.
     */
    private const val MINIMAL_HEADER =
        """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">"""

    private const val MINIMAL_FOOTER = "</rdf:RDF></x:xmpmeta>"

    /**
     * Splits the given XMP packet. Packets that already fit into a single
     * segment are returned unchanged without extension segments.
     */
    fun partition(xmpXml: String): PartitionedXmp {

        if (xmpXml.encodeToByteArray().size <= JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
            return PartitionedXmp(xmpXml, emptyList())

        val headerEnd = locateTagEnd(xmpXml, RDF_OPEN_TAG)
        val footerStart = xmpXml.indexOf(RDF_CLOSE_TAG)

        if (headerEnd == -1 || footerStart == -1 || headerEnd > footerStart)
            throw ImageWriteException(
                "The XMP packet has an unexpected structure and cannot be split " +
                    "into main and extended data."
            )

        val header = xmpXml.substring(0, headerEnd)
        val footer = xmpXml.substring(footerStart)
        val content = xmpXml.substring(headerEnd, footerStart)

        /* Block boundaries are the starts of successive rdf:Description elements.
         * RDF/XML never nests descriptions, so scanning only for starts is safe
         * and cannot be confused by markup inside attribute or text values. */
        val blocks = splitDescriptions(content).filter { block ->
            !block.contains(STALE_REFERENCE_MARKER)
        }

        val referenceDescriptionTemplate =
            buildReferenceDescription("0".repeat(JpegConstants.EXTENDED_XMP_GUID_LENGTH))

        /*
         * Like ExifTool, the smallest descriptions are kept in the main packet
         * first, so as much as possible remains readable by simple tools.
         */
        val sortedBySize = blocks.sortedBy { it.encodeToByteArray().size }

        val fixedMainBytes =
            (header + footer + referenceDescriptionTemplate).encodeToByteArray().size

        var usedBytes = fixedMainBytes

        var keepCount = 0

        for (block in sortedBySize) {

            val blockSize = block.encodeToByteArray().size

            if (usedBytes + blockSize > JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
                break

            usedBytes += blockSize
            keepCount++
        }

        val keptBlocks = sortedBySize.take(keepCount)

        val movedBlocks = blocks.filterNot { block -> block in keptBlocks }

        /*
         * Only the main packet must fit into a single segment. The extended
         * data is chunked into arbitrary byte ranges and reassembled before
         * parsing, so moving everything out is always possible. Old readers
         * that ignore extended XMP see an almost empty main packet then,
         * but they would truncate or reject oversized metadata anyway.
         */
        if (movedBlocks.isEmpty()) {
            /* Dropping the stale reference made everything fit again. */
            return PartitionedXmp(header + blocks.joinToString("") + footer, emptyList())
        }

        val extendedXml = MINIMAL_HEADER + movedBlocks.joinToString("") + MINIMAL_FOOTER

        val extendedBytes = extendedXml.encodeToByteArray()

        val guid = digestAsGuid(extendedBytes)

        val mainPacketXml =
            header + keptBlocks.joinToString("") + buildReferenceDescription(guid) + footer

        return PartitionedXmp(mainPacketXml, createExtensionPayloads(guid, extendedBytes))
    }

    /**
     * Returns the index behind the '>' of the given tag's first occurrence.
     */
    private fun locateTagEnd(xml: String, tagName: String): Int {

        val tagStart = xml.indexOf(tagName)

        if (tagStart == -1)
            return -1

        val tagEnd = xml.indexOf('>', tagStart)

        return if (tagEnd == -1) -1 else tagEnd + 1
    }

    /**
     * Splits the content between the rdf:RDF tags into blocks that each start
     * with an rdf:Description element.
     */
    private fun splitDescriptions(content: String): List<String> {

        val blocks = mutableListOf<String>()

        var blockStart = content.indexOf(DESCRIPTION_OPEN_TAG)

        while (blockStart != -1) {

            val nextBlockStart = content.indexOf(DESCRIPTION_OPEN_TAG, blockStart + 1)

            val blockEnd = if (nextBlockStart == -1) content.length else nextBlockStart

            blocks.add(content.substring(blockStart, blockEnd))

            blockStart = nextBlockStart
        }

        return blocks
    }

    /**
     * Builds the rdf:Description element that links the main packet to its
     * extended data through the given GUID, using the same serialization
     * form as ExifTool.
     */
    private fun buildReferenceDescription(guid: String): String =
        """<rdf:Description rdf:about="" xmlns:xmpNote="${JpegConstants.XMP_NOTE_NAMESPACE}">""" +
            "<xmpNote:HasExtendedXMP>$guid</xmpNote:HasExtendedXMP>" +
            "</rdf:Description>"

    /**
     * Wraps the extended data chunks into ready-to-write segment payloads:
     * identifier, GUID, total length and one chunk of the data each.
     */
    private fun createExtensionPayloads(guid: String, extendedBytes: ByteArray): List<ByteArray> {

        val payloads = mutableListOf<ByteArray>()

        var offset = 0

        do {

            val chunkEnd = minOf(
                offset + JpegConstants.MAX_EXTENDED_XMP_BYTES_PER_SEGMENT,
                extendedBytes.size
            )

            val payloadWriter = ByteArrayByteWriter()

            payloadWriter.write(JpegConstants.EXTENDED_XMP_IDENTIFIER)
            payloadWriter.write(guid.encodeToByteArray())
            payloadWriter.write(extendedBytes.size.toBytes(ByteOrder.BIG_ENDIAN))
            payloadWriter.write(extendedBytes.copyOfRange(offset, chunkEnd))

            payloads.add(payloadWriter.toByteArray())

            offset = chunkEnd
        } while (offset < extendedBytes.size)

        return payloads
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun digestAsGuid(bytes: ByteArray): String =
        Md5.digest(bytes).toHexString(HexFormat.UpperCase)
}
