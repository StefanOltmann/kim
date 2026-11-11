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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.COM_MARKER_1
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.DHT_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP1_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests that [Kim.deleteMetadata] cleans a JPEG through the structural
 * EOI marker (GitHub issue 41).
 *
 * The metadata segments behind the image data are removed like the header
 * segments, the compressed image data stays byte-exact, and every byte
 * behind the structural EOI marker is discarded, so a caller can rely on
 * the result being clean of metadata. A file whose bytes end without the
 * structural EOI marker cannot provide that guarantee and fails the
 * operation.
 */
class JpegDeleteMetadataBehindImageDataTest {

    /**
     * The committed fixture of the full scan test: a real image whose
     * comment segment sits behind the image data, followed by a trailing
     * payload and one extra EOI marker.
     */
    private val commentFixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/jpeg_with_comment_before_eoi.jpg").readBytes()

    /**
     * The committed fixture of the trailer test: a real image whose EXIF
     * and XMP were relocated byte-exact behind the EOI marker, followed
     * by nothing - the trailer is the file's last content.
     */
    private val trailerFixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/jpeg_with_metadata_behind_eoi.jpg").readBytes()

    /**
     * The deletion removes the comment behind the image data and the
     * trailing payload, and keeps every other byte of the file: the
     * synthetic file carries no header metadata, so the result is the
     * input truncated behind the structural EOI marker.
     */
    @Test
    fun testDeleteMetadataRemovesCommentBehindImageDataAndTrailer() {

        val entropyBytes = byteArrayOf(0x11, 0x22, 0x33)

        val commentBytes = segmentBytes(COM_MARKER_1, SENTINEL)

        val trailerBytes = SENTINEL + EOI

        val inputBytes =
            SOI + SCAN_HEADER_BYTES + entropyBytes + commentBytes + EOI + trailerBytes

        val expectedBytes = SOI + SCAN_HEADER_BYTES + entropyBytes + EOI

        val deletedBytes = Kim.deleteMetadata(inputBytes)

        assertContentEquals(
            expected = expectedBytes,
            actual = deletedBytes,
            message = "The deletion must keep the image data and end at the structural EOI marker."
        )
    }

    /**
     * A progressive file can carry metadata segments between its scans.
     * The deletion removes exactly those segments, keeps the scan headers
     * and the entropy coded data byte-exact, keeps the decoding
     * structures between the scans (the DHT segment), and ends the file
     * at the structural EOI marker in front of the trailing payload.
     */
    @Test
    fun testDeleteMetadataRemovesSegmentsBetweenProgressiveScans() {

        val entropy1Bytes = byteArrayOf(0x11, 0x22, 0x33)

        val app1Bytes = segmentBytes(JPEG_APP1_MARKER, SENTINEL)

        val commentBytes = segmentBytes(COM_MARKER_1, SENTINEL)

        val dhtBytes = segmentBytes(DHT_MARKER, byteArrayOf(0x00, 0x01))

        val entropy2Bytes = byteArrayOf(0x44)

        val trailerBytes = SENTINEL + EOI

        val inputBytes = SOI + SCAN_HEADER_BYTES + entropy1Bytes +
            app1Bytes + commentBytes + dhtBytes + SCAN_HEADER_BYTES + entropy2Bytes +
            EOI + trailerBytes

        val expectedBytes = SOI + SCAN_HEADER_BYTES + entropy1Bytes +
            dhtBytes + SCAN_HEADER_BYTES + entropy2Bytes + EOI

        val deletedBytes = Kim.deleteMetadata(inputBytes)

        assertContentEquals(
            expected = expectedBytes,
            actual = deletedBytes,
            message = "The scans and decoding structures must survive byte-exact, " +
                "the metadata between them must go."
        )
    }

    /**
     * The committed comment fixture is cleaned to its structural EOI
     * marker: the relocated comment and the trailing payload are gone,
     * while the file stays readable and its reported ranges end exactly
     * at the file end.
     */
    @Test
    fun testDeleteMetadataCleansTheCommentFixtureThroughEoi() {

        /*
         * The fixture really carries the scenario: the comment payload
         * sits behind the image data, and one copy of the payload sits
         * behind the structural EOI marker as the trailer.
         */
        val fixtureSegmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(commentFixtureBytes),
            fullScan = true
        )

        val comInfo = fixtureSegmentInfos.last { it.marker == COM_MARKER_1 }

        assertContentEquals(
            expected = SENTINEL,
            actual = commentFixtureBytes.copyOfRange(
                comInfo.offset.toInt() + 4,
                comInfo.offset.toInt() + 4 + SENTINEL.size
            ),
            message = "The fixture's comment behind the image data must carry the sentinel."
        )

        val fixtureEoiInfo = fixtureSegmentInfos.last()

        assertTrue(
            lastIndexOf(commentFixtureBytes, SENTINEL) > fixtureEoiInfo.offset,
            "The fixture must carry the sentinel payload behind the structural EOI marker."
        )

        val deletedBytes = Kim.deleteMetadata(commentFixtureBytes)

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(deletedBytes),
            fullScan = true
        )

        val eoiInfo = segmentInfos.last()

        assertEquals(
            expected = EOI_MARKER,
            actual = eoiInfo.marker,
            message = "The deleted file must end on the structural EOI marker."
        )

        assertEquals(
            expected = deletedBytes.size.toLong(),
            actual = eoiInfo.offset + eoiInfo.length,
            message = "No byte of the trailer may survive the deletion."
        )

        assertEquals(
            expected = -1,
            actual = indexOf(deletedBytes, SENTINEL),
            message = "The comment payload must not survive the deletion."
        )

        assertNotNull(
            Kim.readMetadata(deletedBytes),
            "The deleted file must stay readable."
        )
    }

    /**
     * The committed trailer fixture is cleaned to its structural EOI
     * marker: the relocated EXIF and XMP of the 3206 byte trailer are
     * gone, while the file stays readable.
     */
    @Test
    fun testDeleteMetadataDiscardsTheTrailerFixture() {

        val trailerMarker = "Behind EOI".encodeToByteArray()

        assertTrue(
            indexOf(trailerFixtureBytes, trailerMarker) > 0,
            "The fixture must carry the trailer's title."
        )

        val deletedBytes = Kim.deleteMetadata(trailerFixtureBytes)

        assertEquals(
            expected = -1,
            actual = indexOf(deletedBytes, trailerMarker),
            message = "The trailer's XMP title must not survive the deletion."
        )

        assertTrue(
            deletedBytes.size < trailerFixtureBytes.size,
            "The deletion must shrink the file by the trailer's size."
        )

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(deletedBytes),
            fullScan = true
        )

        val eoiInfo = segmentInfos.last()

        assertEquals(
            expected = EOI_MARKER,
            actual = eoiInfo.marker,
            message = "The deleted file must end on the structural EOI marker."
        )

        assertEquals(
            expected = deletedBytes.size.toLong(),
            actual = eoiInfo.offset + eoiInfo.length,
            message = "No byte behind the structural EOI marker may survive."
        )

        assertNotNull(
            Kim.readMetadata(deletedBytes),
            "The deleted file must stay readable."
        )
    }

    /**
     * A file whose bytes end inside the entropy coded data without the
     * structural EOI marker cannot show where its image data ends, so
     * the metadata behind the scan cannot be separated from image data
     * and the deletion fails instead of emitting an unverifiable file.
     */
    @Test
    fun testDeleteMetadataRejectsJpegWithoutStructuralEoi() {

        val inputBytes = SOI + SCAN_HEADER_BYTES + byteArrayOf(0x11, 0x22, 0x33)

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(inputBytes)
        }
    }

    /**
     * A segment between the scans whose length field announces more bytes
     * than the file contains is a corrupt structure, and the deletion
     * fails instead of emitting a file around it.
     */
    @Test
    fun testDeleteMetadataRejectsTruncatedSegmentBetweenScans() {

        val truncatedSegmentBytes = byteArrayOf(
            0xFF.toByte(),
            0xFE.toByte(),
            0x00,
            0x64
        )

        val inputBytes = SOI + SCAN_HEADER_BYTES + byteArrayOf(0x11, 0x22) +
            truncatedSegmentBytes + byteArrayOf(0x01, 0x02) + EOI

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(inputBytes)
        }
    }

    /**
     * A crafted scan header can carry marker bytes inside its payload.
     * The scan header must be transferred by its length field, so no
     * byte inside it can end the file early: the payload keeps all its
     * bytes and the walk continues behind it.
     */
    @Test
    fun testDeleteMetadataTransfersScanHeaderPayloadByItsLength() {

        /*
         * The SOS scan header with the length 5 carries the payload
         * "FF D9 00": a walk that interprets the payload as entropy data
         * would take its EOI bytes for the file's structural EOI and
         * truncate the scan header mid-payload.
         */
        val scanHeaderWithEoiPayloadBytes = byteArrayOf(
            0xFF.toByte(),
            0xDA.toByte(),
            0x00,
            0x05,
            0xFF.toByte(),
            0xD9.toByte(),
            0x00
        )

        val commentBytes = segmentBytes(COM_MARKER_1, SENTINEL)

        val inputBytes = SOI + scanHeaderWithEoiPayloadBytes + commentBytes + EOI

        val expectedBytes = SOI + scanHeaderWithEoiPayloadBytes + EOI

        val deletedBytes = Kim.deleteMetadata(inputBytes)

        assertContentEquals(
            expected = expectedBytes,
            actual = deletedBytes,
            message = "The scan header payload must keep all its bytes, the comment must go."
        )
    }

    /**
     * Returns the offset of the first occurrence of the needle, or -1.
     */
    private fun indexOf(bytes: ByteArray, needle: ByteArray): Int {

        for (index in 0..bytes.size - needle.size) {

            if (bytes.copyOfRange(index, index + needle.size).contentEquals(needle))
                return index
        }

        return -1
    }

    /**
     * Returns the offset of the last occurrence of the needle, or -1.
     */
    private fun lastIndexOf(bytes: ByteArray, needle: ByteArray): Int {

        for (index in bytes.size - needle.size downTo 0) {

            if (bytes.copyOfRange(index, index + needle.size).contentEquals(needle))
                return index
        }

        return -1
    }

    /**
     * Returns the complete bytes of a length-prefixed JPEG segment with
     * the given marker and payload: the 2 marker bytes, the 2 length
     * bytes (which count themselves) and the payload.
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

        /** The comment payload the fixtures and the synthetic files carry. */
        val SENTINEL: ByteArray = "KIM_TEST_DATA".encodeToByteArray()

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
    }
}
