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
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_APP1_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOF0_MARKER
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the TEM marker handling of every JPEG marker walk.
 *
 * TEM (FF 01) is a standalone marker without a length word and without
 * payload per ITU-T T.81 table B.3, reserved for temporary private use
 * in arithmetic coding. No mainstream encoder emits it, but it is legal
 * structure: the ExifTool reference reads no length word for it anywhere
 * (the markerLenBytes table in ExifTool.pm), so every marker walk here
 * must scan over it without interpreting the bytes behind it as a
 * segment length.
 */
class JpegTemMarkerTest {

    /**
     * The header segment reader must scan over a TEM marker in the
     * header and still find the APP1 segment and the SOS marker behind
     * it. The TEM marker itself carries no payload and is not retained,
     * so a rewrite drops it like the redundant fill bytes.
     */
    @Test
    fun testReadSegmentsScansOverTemMarkerInHeader() {

        val (segments, sosMarkerBytes) =
            JpegUtils.readSegments(ByteArrayByteReader(buildJpegWithExif(withTemMarker = true)))

        assertNotNull(sosMarkerBytes, "The scan must end at the SOS marker.")

        assertContentEquals(EXIF_SCAN_MARKER_BYTES, sosMarkerBytes)

        assertEquals(1, segments.size)

        val app1Segment = segments.single()

        assertEquals(JPEG_APP1_MARKER, app1Segment.marker)
        assertContentEquals(EXIF_PAYLOAD, app1Segment.segmentBytes)
    }

    /**
     * The public metadata read must not break when a TEM marker sits in
     * the header: the EXIF segment behind it is read cleanly.
     */
    @Test
    fun testReadMetadataScansOverTemMarkerInHeader() {

        val metadata = Kim.readMetadata(buildJpegWithExif(withTemMarker = true))

        val orientation = metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

        assertEquals(1.toShort(), orientation)
    }

    /**
     * The rewrite must tolerate a TEM marker in the header: the marker
     * is dropped like the fill bytes and the metadata update still
     * applies to the APP1 segment behind it.
     */
    @Test
    fun testUpdateRoundTripsFileWithTemMarkerInHeader() {

        val updatedBytes = Kim.update(
            bytes = buildJpegWithExif(withTemMarker = true),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val orientation = Kim.readMetadata(updatedBytes)
            ?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

        assertEquals(TiffOrientation.ROTATE_RIGHT.value.toShort(), orientation)
    }

    /**
     * The image size scan must find the SOF segment behind a TEM marker
     * in the header instead of reading the SOF marker bytes as a segment
     * length.
     */
    @Test
    fun testGetImageSizeScansOverTemMarkerInHeader() {

        val bytes = buildJpegWithSof(withTemMarker = true)

        assertEquals(
            expected = ImageSize(width = 8, height = 2),
            actual = JpegImageParser.getImageSize(ByteArrayByteReader(bytes))
        )
    }

    /**
     * The orientation offset finder must find the EXIF segment behind a
     * TEM marker in the header: the found offset is exactly the offset
     * of the same file without the marker plus the 2 marker bytes, so
     * the marker must neither derail the scan nor shift it by anything
     * other than its own size.
     */
    @Test
    fun testFindOrientationOffsetScansOverTemMarkerInHeader() {

        val expected = JpegOrientationOffsetFinder.findOrientationOffset(
            ByteArrayByteReader(buildJpegWithExif(withTemMarker = false))
        )

        val actual = JpegOrientationOffsetFinder.findOrientationOffset(
            ByteArrayByteReader(buildJpegWithExif(withTemMarker = true))
        )

        assertNotNull(expected, "The reference file must provide an orientation offset.")

        assertEquals(
            expected = expected + TEM_MARKER_BYTE_COUNT,
            actual = actual,
            message = "The TEM marker must shift the found offset by exactly its own size."
        )
    }

    /**
     * The metadata byte extractor must retain the header bytes with the
     * TEM marker byte-exact: the marker bytes stay in the prefix, but
     * the bytes behind the marker must not be read as a segment length.
     */
    @Test
    fun testMetadataExtractorRetainsTemMarkerByteExact() {

        val bytes = buildJpegWithExif(withTemMarker = true)

        assertContentEquals(
            expected = bytes,
            actual = JpegMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
        )
    }

    /**
     * Builds a minimal JPEG whose header carries one APP1 EXIF segment
     * with an Orientation entry typed SHORT with value 1, followed by a
     * minimal scan and the EOI marker.
     */
    private fun buildJpegWithExif(withTemMarker: Boolean): ByteArray {

        val writer = ByteArrayByteWriter()

        writer.write(SOI_BYTES)

        if (withTemMarker)
            writer.write(TEM_BYTES)

        writeSegment(writer, JPEG_APP1_MARKER, EXIF_PAYLOAD)

        writer.write(SCAN_HEADER_BYTES)

        writer.write(byteArrayOf(0x12, 0x34))

        writer.write(EOI_BYTES)

        return writer.toByteArray()
    }

    /**
     * Builds a minimal JPEG whose header carries one SOF0 segment with
     * the size 8x2, followed by a minimal scan and the EOI marker.
     */
    private fun buildJpegWithSof(withTemMarker: Boolean): ByteArray {

        val writer = ByteArrayByteWriter()

        writer.write(SOI_BYTES)

        if (withTemMarker)
            writer.write(TEM_BYTES)

        writeSegment(
            writer,
            SOF0_MARKER,
            byteArrayOf(
                /* Precision. */
                8,
                /* Height. */
                0, 2,
                /* Width. */
                0, 8,
                /* Component count. */
                1
            )
        )

        writer.write(SCAN_HEADER_BYTES)

        writer.write(byteArrayOf(0x12, 0x34))

        writer.write(EOI_BYTES)

        return writer.toByteArray()
    }

    private companion object {

        val SOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        val EOI_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

        /** The standalone TEM marker, which has no length word. */
        val TEM_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0x01)

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

        /** The bytes the readSegments contract returns for the SOS marker. */
        val EXIF_SCAN_MARKER_BYTES: ByteArray = byteArrayOf(0xFF.toByte(), 0xDA.toByte())

        /** The TEM marker's size in bytes. */
        const val TEM_MARKER_BYTE_COUNT: Long = 2

        /**
         * The APP1 payload: the EXIF header, then a TIFF structure with
         * one IFD0 entry, the Orientation tag typed SHORT with value 1.
         */
        val EXIF_PAYLOAD: ByteArray = byteArrayOf(
            /* "Exif\0\0" */
            0x45, 0x78, 0x69, 0x66, 0x00, 0x00,
            /* TIFF header, little-endian. */
            0x49, 0x49, 0x2A, 0x00,
            /* IFD0 offset. */
            8, 0, 0, 0,
            /* Entry count. */
            1, 0,
            /* Orientation tag. */
            0x12, 0x01,
            /* Type SHORT. */
            3, 0,
            /* Count 1. */
            1, 0, 0, 0,
            /* Value 1. */
            1, 0, 0, 0,
            /* No next IFD. */
            0, 0, 0, 0
        )
    }
}
