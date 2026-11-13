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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.ADOBE_IDENTIFIER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.COM_MARKER_1
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.DHT_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.ICC_PROFILE_IDENTIFIER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP0_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP14_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP2_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the set of APP segments [Kim.deleteMetadata] keeps in a JPEG
 * file. The caller must be able to rely on the result being clean of
 * metadata.
 *
 * The known healthy ICC profile in front of the image data is the only
 * APP payload that survives in front of the image data: the known
 * healthy ICC profile and the Adobe APP14 segment. Every other APP
 * segment carries metadata, whether it is EXIF, XMP, IPTC, a vendor
 * block like the SCALADO APP4 segments of media_41 (115 KB with an
 * embedded XMP) or the Nikon Image Info of media_34, or the JFIF block.
 * The ICC profile goes too when the caller drops it with
 * keepIccProfile = false; the APP14 segment stays in every mode, since
 * its ColorTransform flag tells the decoder how to interpret the DCT
 * data and there is no fallback for it. What the ExifTool binary
 * removes for the corpus with "exiftool -all=", this delete removes as
 * well - beside the documented ICC exception, which ExifTool only
 * removes with "--icc_profile:all". Behind the image data nothing is
 * kept at all.
 */
class JpegDeleteMetadataKeepSetTest {

    /**
     * The header keeps the Adobe APP14 segment and the ICC chunks: the
     * ColorTransform flag of the APP14 segment tells the decoder how to
     * interpret the DCT data, and losing it renders Adobe RGB and CMYK
     * files with wrong colors. The JFIF APP0 segment, the foreign APP7
     * payload, the comment and the EXIF segment are metadata and go.
     */
    @Test
    fun testDeleteMetadataKeepsAdobeApp14InTheHeader() {

        val app0Bytes = segmentBytes(JPEG_APP0_MARKER, JFIF_PAYLOAD_BYTES)

        val foreignApp7Bytes = segmentBytes(0xFFE7, FOREIGN_PAYLOAD_BYTES)

        val commentBytes = segmentBytes(COM_MARKER_1, COMMENT_PAYLOAD_BYTES)

        val exifBytes = segmentBytes(0xFFE1, EXIF_IDENTIFIER_BYTES)

        val app14Bytes = segmentBytes(JPEG_APP14_MARKER, ADOBE_PAYLOAD_BYTES)

        val entropyBytes = byteArrayOf(0x11, 0x22, 0x33)

        val inputBytes = SOI + app0Bytes + foreignApp7Bytes + commentBytes +
            exifBytes + app14Bytes + SCAN_HEADER_BYTES + entropyBytes +
            EOI + TRAILER_BYTES

        val expectedBytes = SOI + app14Bytes + SCAN_HEADER_BYTES + entropyBytes + EOI

        val deletedBytes = Kim.deleteMetadata(inputBytes)

        assertContentEquals(
            expected = expectedBytes,
            actual = deletedBytes,
            message = "The Adobe APP14 segment is decoding structure and must survive."
        )
    }

    /**
     * Behind the image data every APP segment and comment goes - the
     * APPn position between the scans is one no decoder reads, so even
     * an ICC chunk there is not the known profile the keep-rule
     * protects, and the Adobe APP14 segment is not the decoding
     * directive the decoder reads in front of the image data. The
     * decoding structures (the DHT segment) keep their bytes.
     */
    @Test
    fun testDeleteMetadataRemovesAppAndCommentSegmentsBetweenScans() {

        val entropy1Bytes = byteArrayOf(0x11, 0x22, 0x33)

        val app0Bytes = segmentBytes(JPEG_APP0_MARKER, JFIF_PAYLOAD_BYTES)

        val iptcBytes = segmentBytes(0xFFED, PHOTOSHOP_PAYLOAD_BYTES)

        val iccChunkBytes = segmentBytes(
            JPEG_APP2_MARKER,
            ICC_PROFILE_IDENTIFIER + byteArrayOf(0x01, 0x01) + FOREIGN_PAYLOAD_BYTES
        )

        val app14Bytes = segmentBytes(JPEG_APP14_MARKER, ADOBE_PAYLOAD_BYTES)

        val commentBytes = segmentBytes(COM_MARKER_1, COMMENT_PAYLOAD_BYTES)

        val dhtBytes = segmentBytes(DHT_MARKER, byteArrayOf(0x00, 0x01))

        val entropy2Bytes = byteArrayOf(0x44)

        val inputBytes = SOI + SCAN_HEADER_BYTES + entropy1Bytes +
            app0Bytes + iptcBytes + iccChunkBytes + app14Bytes + commentBytes + dhtBytes +
            SCAN_HEADER_BYTES + entropy2Bytes + EOI

        val expectedBytes = SOI + SCAN_HEADER_BYTES + entropy1Bytes +
            dhtBytes + SCAN_HEADER_BYTES + entropy2Bytes + EOI

        val deletedBytes = Kim.deleteMetadata(inputBytes)

        assertContentEquals(
            expected = expectedBytes,
            actual = deletedBytes,
            message = "Every APP and COM segment between the scans must go."
        )
    }

    /**
     * The whole corpus pins the keep-set: after the deletion only ICC
     * PROFILE chunks of one complete healthy run and the Adobe APP14
     * segments remain, and the read no longer finds EXIF, IPTC or XMP
     * in any file. The four files the full scan rejects end inside their
     * image data and fail the deletion.
     */
    @Test
    fun testDeleteMetadataLeavesOnlyTheKnownIccInTheCorpus() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            if (index in UNREADABLE_CORPUS_INDEXES) {

                assertFailsWith<ImageWriteException>(
                    message = "media_$index must fail the deletion."
                ) {
                    Kim.deleteMetadata(bytes)
                }

                continue
            }

            val deletedBytes = Kim.deleteMetadata(bytes)

            val (segments, sosMarkerBytes) =
                JpegUtils.readSegments(ByteArrayByteReader(deletedBytes))

            assertNotNull(
                sosMarkerBytes,
                "media_$index must stay a file with image data."
            )

            for (segment in segments) {

                if (!segment.isAppSegment())
                    continue

                val isKeptStructure =
                    (segment.marker == JPEG_APP2_MARKER &&
                        segment.segmentBytes.startsWith(ICC_PROFILE_IDENTIFIER)) ||
                        (segment.marker == JPEG_APP14_MARKER &&
                            segment.segmentBytes.startsWith(ADOBE_IDENTIFIER))

                assertTrue(
                    isKeptStructure,
                    "media_$index keeps a foreign " +
                        "${JpegConstants.markerDescription(segment.marker)} " +
                        "with payload ${segment.segmentBytes.toList().take(16)}"
                )
            }

            /*
             * The kept ICC chunks form one complete healthy run: the
             * assembler the read uses must accept every APP2 segment
             * that survived.
             */
            val app2Segments =
                segments.filter { segment -> segment.marker == JPEG_APP2_MARKER }

            if (app2Segments.isNotEmpty()) {

                val chunks = assertNotNull(
                    JpegIccChunkAssembler.collectFirstRun(segments),
                    "media_$index keeps ICC chunks that do not assemble."
                )

                assertEquals(
                    expected = app2Segments.size,
                    actual = chunks.size,
                    message = "media_$index keeps ICC chunks outside the first run."
                )
            }

            val metadata = assertNotNull(
                Kim.readMetadata(deletedBytes),
                "media_$index must stay readable."
            )

            assertNull(metadata.exif, "media_$index keeps readable EXIF.")
            assertNull(metadata.iptc, "media_$index keeps readable IPTC.")
            assertNull(metadata.xmp, "media_$index keeps readable XMP.")
        }
    }

    /**
     * The ICC profile is the only APP payload the default deletion keeps
     * beside the Adobe APP14 segment: media_34 carries both, and with
     * the profile dropped only the decoding directive survives - not the
     * ICC chunks.
     */
    @Test
    fun testDeleteMetadataKeepsIccChunksByDefaultAndDropsOnRequest() {

        val deletedBytes = Kim.deleteMetadata(KimTestData.getBytesOf(34))

        val (defaultSegments, _) =
            JpegUtils.readSegments(ByteArrayByteReader(deletedBytes))

        assertTrue(
            defaultSegments.any { segment -> segment.isAppSegment() },
            "The default deletion must keep the ICC chunks of media_34."
        )

        val iccDroppedBytes =
            Kim.deleteMetadata(bytes = KimTestData.getBytesOf(34), keepIccProfile = false)

        val (droppedSegments, _) =
            JpegUtils.readSegments(ByteArrayByteReader(iccDroppedBytes))

        assertTrue(
            droppedSegments.all { segment ->
                !segment.isAppSegment() || segment.marker == JPEG_APP14_MARKER
            },
            "The requested deletion must drop every APP segment but the " +
                "Adobe APP14 decoding directive."
        )
    }

    /**
     * Returns the complete bytes of a length-prefixed JPEG segment with
     * the given marker and payload.
     */
    private fun segmentBytes(marker: Int, payload: ByteArray): ByteArray {

        val length = payload.size + 2

        return byteArrayOf(
            (marker ushr 8).toByte(),
            marker.toByte(),
            (length ushr 8).toByte(),
            length.toByte()
        ) + payload
    }

    private companion object {

        /** The indexes the full scan rejects: truncated or corrupt files. */
        val UNREADABLE_CORPUS_INDEXES = 44..47

        /** The 14 byte JFIF APP0 payload: identifier, version, density. */
        val JFIF_PAYLOAD_BYTES: ByteArray = byteArrayOf(
            0x4A,
            0x46,
            0x49,
            0x46,
            0x00,
            0x01,
            0x01,
            0x00,
            0x00,
            0x01,
            0x00,
            0x01,
            0x00,
            0x00
        )

        /** The Adobe APP14 payload: identifier, version, transform flag. */
        val ADOBE_PAYLOAD_BYTES: ByteArray = byteArrayOf(
            0x41,
            0x64,
            0x6F,
            0x62,
            0x65,
            0x00,
            0x64,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        )

        val FOREIGN_PAYLOAD_BYTES: ByteArray = "FOREIGN".encodeToByteArray()

        val COMMENT_PAYLOAD_BYTES: ByteArray = "COMMENT".encodeToByteArray()

        val PHOTOSHOP_PAYLOAD_BYTES: ByteArray = "Photoshop 3.0".encodeToByteArray()

        val EXIF_IDENTIFIER_BYTES: ByteArray = byteArrayOf(
            0x45,
            0x78,
            0x69,
            0x66,
            0x00,
            0x00
        )

        /** The SOS marker with its 2 length bytes, then the 8 scan header bytes. */
        val SCAN_HEADER_BYTES: ByteArray = byteArrayOf(
            0xFF.toByte(),
            0xDA.toByte(),
            0x00,
            0x08,
            0x01,
            0x01,
            0x00,
            0x00,
            0x3F,
            0x00
        )

        /** The payload the trailer carries behind the structural EOI marker. */
        val TRAILER_BYTES: ByteArray =
            "KIM_TEST_DATA".encodeToByteArray() + EOI
    }
}
