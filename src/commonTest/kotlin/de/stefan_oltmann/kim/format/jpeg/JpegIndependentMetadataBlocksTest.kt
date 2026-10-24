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
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.containsBytes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests that metadata blocks Kim does not understand as part of the first
 * block survive an update byte-exact.
 *
 * Kim reads only the first EXIF block and the first XMP packet. A second,
 * *independent* block (one that starts with its own TIFF byte order
 * marker, respectively its own complete packet) belongs to other tools -
 * deleting it would destroy data Kim never interpreted. Continuations of
 * the first block are not independent: they are consumed by the stitch
 * and replaced by the rewrite.
 */
class JpegIndependentMetadataBlocksTest {

    /**
     * Builds the segment payload of an independent EXIF block: a complete
     * minimal TIFF whose IFD0 carries a single Make entry with an inline
     * value ("Kim\0" or "M2\0" fit the 4 value bytes).
     */
    private fun buildIndependentExifPayload(model: String): ByteArray {

        require(model.length <= 3) { "Model too long for this fixture: $model" }

        val modelBytes = model.encodeToByteArray() + byteArrayOf(0)

        val bytes = mutableListOf<Byte>()

        /* TIFF header: little endian, TIFF magic, IFD0 at offset 8. */
        bytes.addAll(byteArrayOf(0x49, 0x49, 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00).toList())

        /* IFD0 entry count: 1. */
        bytes.addAll(byteArrayOf(0x01, 0x00).toList())

        /* Make entry: ASCII, inline value. */
        bytes.addAll(byteArrayOf(0x0F, 0x01, 0x02, 0x00).toList())
        bytes.addAll(byteArrayOf(modelBytes.size.toByte(), 0, 0, 0).toList())
        bytes.addAll(modelBytes.toList())

        /* No next IFD. */
        bytes.addAll(byteArrayOf(0x00, 0x00, 0x00, 0x00).toList())

        return "Exif\u0000\u0000".encodeToByteArray() + bytes.toByteArray()
    }

    /**
     * Builds a minimal JPEG around the given header segments: image data
     * is a bare SOS marker followed by the EOI marker.
     */
    private fun buildJpeg(vararg segments: JFIFPieceSegment): ByteArray {

        val bytes = mutableListOf<Byte>()

        bytes.addAll(byteArrayOf(0xFF.toByte(), 0xD8.toByte()).toList())

        for (segment in segments) {

            bytes.addAll(byteArrayOf(0xFF.toByte(), 0xE1.toByte()).toList())

            val length = segment.segmentBytes.size + 2

            bytes.addAll(byteArrayOf((length shr 8).toByte(), length.toByte()).toList())
            bytes.addAll(segment.segmentBytes.toList())
        }

        bytes.addAll(byteArrayOf(0xFF.toByte(), 0xDA.toByte(), 0x00, 0x02).toList())
        bytes.addAll(byteArrayOf(0xFF.toByte(), 0xD9.toByte()).toList())

        return bytes.toByteArray()
    }

    @Test
    fun testUpdatePreservesIndependentSecondExifBlock() {

        val firstExif = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = buildIndependentExifPayload("Kim")
        )

        val secondExif = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = buildIndependentExifPayload("M2")
        )

        val bytes = buildJpeg(firstExif, secondExif)

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The first block was rebuilt by the update. */
        assertFalse(updatedBytes.containsBytes(firstExif.segmentBytes))

        /* The independent second block survived byte-exact. */
        assertTrue(
            updatedBytes.containsBytes(secondExif.segmentBytes),
            "The independent second EXIF block was removed by the update."
        )
    }

    /**
     * Regression test: the second, independent EXIF block can itself
     * span segments. The removal of the first block stops at the second
     * block's head, but it must stop for good: a continuation segment
     * behind that head belongs to the foreign block, and removing it
     * would truncate that block mid-TIFF - destroying data Kim never
     * interpreted.
     */
    @Test
    fun testUpdatePreservesTheSecondBlockContinuations() {

        val firstExif = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = buildIndependentExifPayload("Kim")
        )

        /* Mid-TIFF bytes of block 1: no byte order marker, so a continuation. */
        val firstContinuation = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = "Exif\u0000\u0000CONT1-DATA".encodeToByteArray()
        )

        val secondExif = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = buildIndependentExifPayload("M2")
        )

        val secondContinuation = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = "Exif\u0000\u0000CONT2-DATA".encodeToByteArray()
        )

        val bytes = buildJpeg(firstExif, firstContinuation, secondExif, secondContinuation)

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The independent second block survived byte-exact ... */
        assertTrue(
            updatedBytes.containsBytes(secondExif.segmentBytes),
            "The independent second EXIF block was removed by the update."
        )

        /* ... together with its own continuation segments. */
        assertTrue(
            updatedBytes.containsBytes(secondContinuation.segmentBytes),
            "The continuation of the independent second EXIF block was removed by the update."
        )

        /* The continuation of the replaced first block is still gone. */
        assertFalse(updatedBytes.containsBytes(firstContinuation.segmentBytes))
    }

    @Test
    fun testUpdatePreservesIndependentSecondXmpPacket() {

        val firstPacket =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about=""/>""" +
                "</rdf:RDF></x:xmpmeta>"

        val secondPacket =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF><rdf:Description rdf:about=""/></rdf:RDF></x:xmpmeta>"""

        val firstXmp = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = JpegConstants.XMP_IDENTIFIER + firstPacket.encodeToByteArray()
        )

        val secondXmp = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = JpegConstants.XMP_IDENTIFIER + secondPacket.encodeToByteArray()
        )

        val bytes = buildJpeg(firstXmp, secondXmp)

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The first packet was rebuilt by the update. */
        assertFalse(updatedBytes.containsBytes(firstXmp.segmentBytes))

        /* The independent second packet survived byte-exact. */
        assertTrue(
            updatedBytes.containsBytes(secondXmp.segmentBytes),
            "The independent second XMP packet was removed by the update."
        )
    }

    /**
     * Regression test: the second, independent XMP packet can itself be
     * split per Adobe extended-XMP - its main packet references the
     * extension chunks by GUID. Those chunks are not orphans of the
     * replaced first packet: they belong to the surviving foreign
     * packet, and dropping them would truncate data Kim never
     * interpreted.
     */
    @Test
    fun testUpdatePreservesTheSecondPacketExtensions() {

        val firstPacket =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about=""/></rdf:RDF></x:xmpmeta>"""

        val guid = "00112233445566778899AABBCCDDEEFF"

        val secondPacket =
            """<x:xmpmeta xmlns:x="adobe:ns:meta/">""" +
                """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">""" +
                """<rdf:Description rdf:about="" xmlns:xmpNote="http://ns.adobe.com/xmp/note/" """ +
                """xmpNote:HasExtendedXMP="$guid"/></rdf:RDF></x:xmpmeta>"""

        val firstXmp = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = JpegConstants.XMP_IDENTIFIER + firstPacket.encodeToByteArray()
        )

        val secondXmp = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = JpegConstants.XMP_IDENTIFIER + secondPacket.encodeToByteArray()
        )

        val extensionData = "EXT-BYTES".encodeToByteArray()

        val secondExtension = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP1_MARKER,
            segmentBytes = JpegConstants.EXTENDED_XMP_IDENTIFIER +
                guid.encodeToByteArray() +
                byteArrayOf(
                    (extensionData.size shr 24).toByte(),
                    (extensionData.size shr 16).toByte(),
                    (extensionData.size shr 8).toByte(),
                    extensionData.size.toByte()
                ) +
                byteArrayOf(0, 0, 0, 0) +
                extensionData
        )

        val bytes = buildJpeg(firstXmp, secondXmp, secondExtension)

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The independent second packet survived byte-exact ... */
        assertTrue(
            updatedBytes.containsBytes(secondXmp.segmentBytes),
            "The independent second XMP packet was removed by the update."
        )

        /* ... together with the extension chunks it references. */
        assertTrue(
            updatedBytes.containsBytes(secondExtension.segmentBytes),
            "The extended-XMP chunks of the surviving second packet were removed by the update."
        )
    }
}
