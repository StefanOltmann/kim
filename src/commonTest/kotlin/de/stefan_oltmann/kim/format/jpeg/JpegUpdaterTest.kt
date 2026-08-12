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
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
    }
}
