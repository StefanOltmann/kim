/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class JpegUpdaterTest : AbstractUpdaterTest("jpg") {

    private val timestamp = 1_689_166_125_401 // 2023:07:12 14:48:45 in GMT+02:00

    private val title = "Süße Vögelchen"

    private val description = "Äußerst süße Vögel fliegen durch die Lüfte."

    private val originalBytes = Resource("de/stefan_oltmann/kim/updates_jpg/original.jpg").readBytes()

    /**
     * Verifies that a single update call applies the updates to EXIF, IPTC and
     * XMP simultaneously.
     */
    @Test
    fun testUpdateMultipleFieldsSimultaneouslyUpdatesAllFormats() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            updates = setOf(
                MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
                MetadataUpdate.TakenDate(timestamp),
                MetadataUpdate.Title(title),
                MetadataUpdate.Description(description),
                MetadataUpdate.Keywords(setOf("hello", "test")),
                MetadataUpdate.Rating(ExifRating.FOUR_STARS)
            )
        )

        val metadata = Kim.readMetadata(newBytes)!!

        /* EXIF */

        assertEquals(
            TiffOrientation.ROTATE_RIGHT.value.toShort(),
            metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        )

        assertEquals(
            "2023:07:12 14:48:45",
            metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
        )

        assertEquals(
            description,
            metadata.findStringValue(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)
        )

        /* IPTC */

        val iptcRecords = metadata.iptc?.records

        assertEquals(
            title,
            iptcRecords?.firstOrNull { record -> record.iptcType == IptcTypes.OBJECT_NAME }?.value
        )

        assertEquals(
            description,
            iptcRecords?.firstOrNull { record -> record.iptcType == IptcTypes.CAPTION_ABSTRACT }?.value
        )

        assertEquals(
            setOf("hello", "test"),
            iptcRecords
                ?.filter { record -> record.iptcType == IptcTypes.KEYWORDS }
                ?.map { record -> record.value }
                ?.toSet()
        )

        /* XMP */

        val xmpMeta = XMPMetaFactory.parseFromString(metadata.xmp!!)

        assertEquals(title, xmpMeta.getTitle())
        assertEquals(description, xmpMeta.getDescription())
        assertEquals(ExifRating.FOUR_STARS.value, xmpMeta.getRating())
        assertEquals(setOf("hello", "test"), xmpMeta.getKeywords())
    }

    /**
     * Verifies that an orientation update combined with an IPTC-only update
     * still performs the lossless single-byte orientation swap and does not
     * rewrite the EXIF data.
     */
    @Test
    fun testUpdateOrientationWithTitleKeepsLosslessExifSwap() {

        val rotatedRightBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val combinedBytes = Kim.update(
            bytes = originalBytes,
            updates = setOf(
                MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
                MetadataUpdate.Title(title)
            )
        )

        assertContentEquals(exifPayload(rotatedRightBytes), exifPayload(combinedBytes))

        val metadata = Kim.readMetadata(combinedBytes)!!

        assertEquals(
            TiffOrientation.ROTATE_RIGHT.value.toShort(),
            metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        )

        assertEquals(
            title,
            metadata.iptc?.records?.firstOrNull { record -> record.iptcType == IptcTypes.OBJECT_NAME }?.value
        )
    }

    /**
     * Verifies that deleting the metadata removes the EXIF, XMP, IPTC and
     * comment segments, but keeps the ICC segment that affects how the
     * image is displayed.
     */
    @Test
    fun testDeleteMetadataKeepsIccSegment() {

        /* media_1.jpg contains EXIF, IPTC, XMP and an ICC profile. */
        val newBytes = Kim.deleteMetadata(KimTestData.getBytesOf(1))

        val markers = segmentMarkers(newBytes)

        /* EXIF and XMP live in APP1 segments, IPTC in APP13. */
        assertFalse(JpegConstants.JPEG_APP1_MARKER in markers)
        assertFalse(JpegConstants.JPEG_APP13_MARKER in markers)

        /* Comments are metadata, too. */
        assertFalse(JpegConstants.COM_MARKER_1 in markers)

        /* The ICC profile affects the display and must be kept. */
        assertTrue(containsIccProfile(newBytes))
    }

    /**
     * A spec-legal APP0 segment without the JFIF identifier (JFXX
     * thumbnail extension or vendor data) must survive an update
     * byte-identically instead of failing the rewrite, matching the
     * read path that accepts such files (read/update symmetry).
     */
    @Test
    fun testUpdatePreservesNonJfifApp0Segment() {

        /* "JFXX\0" identifier plus a 10-byte extension payload. */
        val jfxxSegmentBytes = byteArrayOf(
            0xFF.toByte(), 0xE0.toByte(),
            0x00, 0x11,
            0x4A, 0x46, 0x58, 0x58, 0x00,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        )

        val bytesWithJfxx = insertSegmentAfterJfif(originalBytes, jfxxSegmentBytes)

        val newBytes = Kim.update(
            bytes = bytesWithJfxx,
            updates = setOf(MetadataUpdate.Title(title))
        )

        assertTrue(newBytes.containsSegment(jfxxSegmentBytes))
    }

    /**
     * An empty segment (length field 2) is spec-legal per ITU-T T.81
     * B.1.1.4 and must survive reading and rewriting byte-identically
     * instead of failing the whole file.
     */
    @Test
    fun testUpdatePreservesEmptySegment() {

        /* A COM segment with an empty payload. */
        val emptyCom = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x02)

        val bytesWithEmptyCom = insertSegmentAfterJfif(originalBytes, emptyCom)

        /* Reading must not reject the empty segment. */
        assertNotNull(Kim.readMetadata(bytesWithEmptyCom))

        val newBytes = Kim.update(
            bytes = bytesWithEmptyCom,
            updates = setOf(MetadataUpdate.Title(title))
        )

        assertTrue(newBytes.containsSegment(emptyCom))
    }

    /**
     * Inserts the given segment bytes after the first header segment that
     * follows the SOI marker.
     */
    private fun insertSegmentAfterJfif(
        jpegBytes: ByteArray,
        segment: ByteArray
    ): ByteArray {

        /* SOI occupies 2 bytes; the length field of the next segment follows. */
        val firstSegmentLength = (jpegBytes[SEGMENT_HEADER_BYTES].toInt() and 0xFF) shl 8 or
            (jpegBytes[SEGMENT_HEADER_BYTES + 1].toInt() and 0xFF)

        val insertIndex = SEGMENT_HEADER_BYTES + firstSegmentLength

        return jpegBytes.copyOfRange(0, insertIndex) +
            segment +
            jpegBytes.copyOfRange(insertIndex, jpegBytes.size)
    }

    /**
     * Returns whether the given segment bytes appear anywhere in the JPEG bytes.
     */
    private fun ByteArray.containsSegment(segment: ByteArray): Boolean {

        for (offset in 0..size - segment.size)
            if (copyOfRange(offset, offset + segment.size).contentEquals(segment))
                return true

        return false
    }

    /**
     * Returns the markers of all segments before the image data.
     */
    private fun segmentMarkers(jpegBytes: ByteArray): Set<Int> {

        val markers = mutableSetOf<Int>()

        var offset = JpegConstants.SOI.size

        while (offset + SEGMENT_HEADER_BYTES <= jpegBytes.size) {

            val marker = (jpegBytes[offset].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 1].toInt() and 0xFF)

            if (marker == JpegConstants.SOS_MARKER || marker == JpegConstants.EOI_MARKER)
                break

            val segmentLength = (jpegBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 3].toInt() and 0xFF)

            markers.add(marker)

            offset += SEGMENT_MARKER_BYTES + segmentLength
        }

        return markers
    }

    /**
     * Returns whether the JPEG bytes contain an APP2 ICC profile segment.
     */
    private fun containsIccProfile(jpegBytes: ByteArray): Boolean {

        var offset = JpegConstants.SOI.size

        while (offset + SEGMENT_HEADER_BYTES <= jpegBytes.size) {

            val marker = (jpegBytes[offset].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 1].toInt() and 0xFF)

            if (marker == JpegConstants.SOS_MARKER || marker == JpegConstants.EOI_MARKER)
                break

            val segmentLength = (jpegBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 3].toInt() and 0xFF)

            if (marker == JpegConstants.JPEG_APP2_MARKER) {

                val payload = jpegBytes.copyOfRange(
                    offset + SEGMENT_MARKER_BYTES + SEGMENT_LENGTH_FIELD_BYTES,
                    offset + SEGMENT_MARKER_BYTES + segmentLength
                )

                if (payload.startsWith(ICC_PROFILE_SIGNATURE))
                    return true
            }

            offset += SEGMENT_MARKER_BYTES + segmentLength
        }

        return false
    }

    /**
     * Returns the payload of the EXIF APP1 segment of the given JPEG bytes.
     */
    private fun exifPayload(jpegBytes: ByteArray): ByteArray {

        var offset = JpegConstants.SOI.size

        while (offset + SEGMENT_HEADER_BYTES <= jpegBytes.size) {

            val marker = (jpegBytes[offset].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 1].toInt() and 0xFF)

            val segmentLength = (jpegBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 3].toInt() and 0xFF)

            if (marker == JpegConstants.JPEG_APP1_MARKER) {

                val payload = jpegBytes.copyOfRange(
                    offset + SEGMENT_MARKER_BYTES + SEGMENT_LENGTH_FIELD_BYTES,
                    offset + SEGMENT_MARKER_BYTES + segmentLength
                )

                if (payload.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE))
                    return payload
            }

            offset += SEGMENT_MARKER_BYTES + segmentLength
        }

        fail("JPEG bytes contain no EXIF segment.")
    }

    private companion object {

        const val SEGMENT_MARKER_BYTES = 2

        const val SEGMENT_LENGTH_FIELD_BYTES = 2

        const val SEGMENT_HEADER_BYTES = SEGMENT_MARKER_BYTES + SEGMENT_LENGTH_FIELD_BYTES

        /* The "ICC_PROFILE" signature plus the null byte of APP2 segments. */
        val ICC_PROFILE_SIGNATURE: ByteArray = "ICC_PROFILE\u0000".encodeToByteArray()
    }
}
