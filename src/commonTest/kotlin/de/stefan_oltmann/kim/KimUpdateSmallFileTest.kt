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
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KimUpdateSmallFileTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    /**
     * Regression test: updating a tiny JPEG must not append zero padding for
     * the unfilled final read chunk.
     */
    @Test
    fun testUpdateSmallJpegDoesNotGrowByZeroPadding() {

        /* A tiny truncated JPEG: header segments, SOS and image data, no EOI. */
        val smallJpeg = convertHexStringToByteArray(
            /* SOI */
            "ffd8" +
                /* APP0 JFIF */
                "ffe00010" + "4a46494600010100000100010000" +
                /* SOS, one component */
                "ffda0008" + "010100003f00" +
                /* image data */
                "112233445566778899aabbccddeeff" +
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
            /* SOI */
            "ffd8" +
                /* APP0 JFIF */
                "ffe00010" + "4a46494600010100000100010000" +
                /* APP1 EXIF header only */
                "ffe1000a" + "457869660000"
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
            /* SOI */
            "ffd8" +
                /* APP0 JFIF */
                "ffe00010" + "4a46494600010100000100010000"
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
            /* SOI */
            "ffd8" +
                /* APP0 JFIF */
                "ffe00010" + "4a46494600010100000100010000" +
                /* EOI */
                "ffd9"
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
            /* SOI */
            "ffd8" +
                /* APP0 JFIF */
                "ffe00010" + "4a46494600010100000100010000" +
                /* EOI */
                "ffd9"
        )

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(eoiBeforeSosJpeg)
        }
    }

    /**
     * A PNG truncated inside its first IDAT chunk must be rejected, because
     * streaming the image data would otherwise end early and the output
     * would declare more IDAT bytes than were written.
     */
    @Test
    fun testUpdateTruncatedPngIsRejected() {

        val pngBytes = KimTestData.getBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)

        /* The first IDAT payload spans bytes 18604 to 26795. */
        val truncatedPng = pngBytes.copyOfRange(0, PNG_TRUNCATION_OFFSET)

        assertTrue(truncatedPng.size < pngBytes.size)

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = truncatedPng,
                updates = setOf(MetadataUpdate.Title("test"))
            )
        }
    }

    /**
     * A PNG truncated inside its first IDAT chunk must be rejected by the
     * deleteMetadata API as well.
     */
    @Test
    fun testDeleteMetadataTruncatedPngIsRejected() {

        val pngBytes = KimTestData.getBytesOf(KimTestData.PNG_TEST_IMAGE_INDEX)

        /* The first IDAT payload spans bytes 18604 to 26795. */
        val truncatedPng = pngBytes.copyOfRange(0, PNG_TRUNCATION_OFFSET)

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(truncatedPng)
        }
    }

    /**
     * A GIF whose image data sub-block chain ends early must be rejected,
     * because the output would silently lose the rest of the image data.
     */
    @Test
    fun testUpdateGifWithTruncatedImageDataIsRejected() {

        val truncatedGif = convertHexStringToByteArray(
            /* GIF89a */
            "474946383961" +
                /* Logical screen descriptor, no global color table */
                "01000100000000" +
                /* Image separator */
                "2c" +
                /* Image descriptor, no local color table */
                "00000000" + "0100" + "0100" + "00" +
                /* LZW minimum code size */
                "02" +
                /* Sub-block announcing 2 data bytes, but only 1 follows */
                "02" + "44"
        )

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = truncatedGif,
                updates = setOf(MetadataUpdate.Title("test"))
            )
        }
    }

    /**
     * A GIF that ends without its trailer byte must be rejected by the
     * deleteMetadata API as well.
     */
    @Test
    fun testDeleteMetadataGifWithoutTrailerIsRejected() {

        val gifWithoutTrailer = convertHexStringToByteArray(
            /* GIF89a */
            "474946383961" +
                /* Logical screen descriptor, no global color table */
                "01000100000000" +
                /* Image separator */
                "2c" +
                /* Image descriptor, no local color table */
                "00000000" + "0100" + "0100" + "00" +
                /* LZW minimum code size */
                "02" +
                /* Complete image data sub-block */
                "02" + "4401" +
                /* Block terminator, but no GIF trailer (0x3B) */
                "00"
        )

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(gifWithoutTrailer)
        }
    }

    private companion object {

        /*
         * media_51.png has its first IDAT payload between the offsets
         * 18604 and 26795, so cutting at this offset lands inside it.
         */
        private const val PNG_TRUNCATION_OFFSET: Int = 20000
    }
}
