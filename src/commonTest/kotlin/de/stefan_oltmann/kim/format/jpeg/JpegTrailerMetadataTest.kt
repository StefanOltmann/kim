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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests metadata behind the JPEG image data against the committed
 * fixture `jpeg_with_metadata_behind_eoi.jpg`.
 *
 * The fixture is a real test image (media_20.jpg) whose EXIF and XMP
 * were written by ExifTool 13.59 and whose APP1 segments were then
 * relocated byte-exact behind the EOI marker. ExifTool validated the
 * metadata values before the relocation (Title "Behind EOI", Rating 4,
 * Orientation 3, DateTimeOriginal 2023:05:10 13:37:42); it reports the
 * trailer itself via `exiftool -v3` as
 * "Unknown trailer (3206 bytes at offset 0x170e)".
 */
class JpegTrailerMetadataTest {

    private val fixtureBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/testdata/jpeg_with_metadata_behind_eoi.jpg").readBytes()

    @Test
    fun testTrailerMetadataIsIgnoredByDefault() {

        val metadata = assertNotNull(Kim.readMetadata(fixtureBytes))

        assertNull(metadata.xmp, "The trailer XMP must be ignored without the flag.")
        assertNull(metadata.exif, "The trailer EXIF must be ignored without the flag.")
    }

    @Test
    fun testTrailingXmpIsReadWithTheFlag() {

        val metadata = assertNotNull(
            Kim.readMetadata(
                bytes = fixtureBytes,
                readTrailerMetadata = true
            )
        )

        val xmp = assertNotNull(metadata.xmp, "The trailer XMP was not read.")

        assertContains(xmp, "Behind EOI")
    }

    @Test
    fun testTrailingExifIsReadWithTheFlag() {

        val metadata = assertNotNull(
            Kim.readMetadata(
                bytes = fixtureBytes,
                readTrailerMetadata = true
            )
        )

        val exif = assertNotNull(metadata.exif, "The trailer EXIF was not read.")

        assertEquals(
            TiffOrientation.UPSIDE_DOWN.value,
            exif.directories.first().findField(TiffTag.TIFF_TAG_ORIENTATION)?.toInt()
        )
    }

    /**
     * The retained trailer segments share the size budget of the header
     * segments, so a hostile file of many maximum-sized APP1 segments
     * cannot accumulate memory unboundedly.
     *
     * This test cannot use a committed fixture: a real file that trips
     * the budget would be over 17 MB of garbage that no tool ever
     * produces, so the hostile input is simulated in code.
     */
    @Test
    fun testOversizedTrailerSegmentsFailTheRead() {

        val out = ByteArrayByteWriter()

        out.write(fixtureBytes)

        /*
         * A JPEG segment length field is 16 bits, so each segment carries
         * at most ~64 KB - exceeding the 16 MiB budget takes 257 of them.
         */
        @Suppress("MagicNumber")
        val maxSegmentPayload =
            JpegConstants.XMP_IDENTIFIER +
                "A".repeat(65533 - JpegConstants.XMP_IDENTIFIER.size).encodeToByteArray()

        repeat(258) {
            writeSegment(out, marker = 0xE1, payload = maxSegmentPayload)
        }

        val exception = assertFailsWith<ImageReadException> {
            Kim.readMetadata(
                bytes = out.toByteArray(),
                readTrailerMetadata = true
            )
        }

        assertContains(exception.message ?: "", "exceeds")
    }

    private fun writeSegment(out: ByteArrayByteWriter, marker: Int, payload: ByteArray) {

        out.write(byteArrayOf(0xFF.toByte(), marker.toByte()))

        out.write2BytesAsInt(payload.size + 2, JpegConstants.JPEG_BYTE_ORDER)

        out.write(payload)
    }
}
