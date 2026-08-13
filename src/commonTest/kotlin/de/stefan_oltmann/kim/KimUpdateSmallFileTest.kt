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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KimUpdateSmallFileTest {

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Regression test: updating a tiny JPEG must not append zero padding for
     * the unfilled final read chunk.
     */
    @Test
    fun testUpdateSmallJpegDoesNotGrowByZeroPadding() {

        /* A tiny truncated JPEG: header segments, SOS and image data, no EOI. */
        val smallJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffda0008" + "010100003f00" + // SOS, one component
                "112233445566778899aabbccddeeff" + // image data
                "112233445566778899aabbccddeeff01"
        )

        assertTrue(
            smallJpeg.size < 100,
            "Test JPEG must be tiny, but is ${smallJpeg.size} bytes."
        )

        val updated = Kim.update(
            bytes = smallJpeg,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
        )

        /*
         * The update adds XMP and EXIF segments. Zero padding of the final
         * read chunk would grow the output by several kilobytes.
         */
        assertTrue(
            updated.size < 3000,
            "Output must not contain zero padding, but is ${updated.size} bytes."
        )
    }

    /**
     * A truncated JPEG that ends before the SOS marker must be rejected,
     * because writing the output would silently destroy the image data.
     */
    @Test
    fun testUpdateTruncatedJpegWithoutSosIsRejected() {

        val truncatedJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffe1000a" + "457869660000" // APP1 EXIF header only
        )

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = truncatedJpeg,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
            )
        }
    }

    /**
     * A truncated JPEG that ends before the SOS marker must be rejected by
     * the deleteMetadata API as well.
     */
    @Test
    fun testDeleteMetadataTruncatedJpegWithoutSosIsRejected() {

        val truncatedJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" // APP0 JFIF
        )

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(truncatedJpeg)
        }
    }

    /**
     * A JPEG whose EOI marker appears before the SOS marker is truncated and
     * must be rejected instead of being rewritten as a header-only file.
     */
    @Test
    fun testUpdateJpegWithEoiBeforeSosIsRejected() {

        val eoiBeforeSosJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffd9" // EOI
        )

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = eoiBeforeSosJpeg,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
            )
        }
    }

    /**
     * A JPEG whose EOI marker appears before the SOS marker must be rejected
     * by the deleteMetadata API as well.
     */
    @Test
    fun testDeleteMetadataJpegWithEoiBeforeSosIsRejected() {

        val eoiBeforeSosJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffd9" // EOI
        )

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(eoiBeforeSosJpeg)
        }
    }
}
