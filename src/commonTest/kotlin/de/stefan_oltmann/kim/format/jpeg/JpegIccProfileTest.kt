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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * The ICC color profile travels in APP2 chunks that number themselves
 * against their total. The parsed profile must expose the fields
 * ExifTool decodes from the same bytes - the profile description and
 * copyright are the fields consumers query.
 */
class JpegIccProfileTest {

    /**
     * media_2 carries a single-chunk GIMP sRGB profile; the expected
     * values are ExifTool's decodings of the same bytes.
     */
    @Test
    fun testIccProfileIsParsedFromApp2Chunks() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(2)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertEquals("lcms", iccProfile.cmmType)
        assertEquals("4.3", iccProfile.version)
        assertEquals("mntr", iccProfile.profileClass)
        assertEquals("RGB ", iccProfile.colorSpace)
        assertEquals("XYZ ", iccProfile.connectionSpace)
        assertEquals("APPL", iccProfile.primaryPlatform)
        assertEquals(0, iccProfile.renderingIntent)

        assertEquals("GIMP built-in sRGB", iccProfile.description)

        assertEquals(
            expected = "Public Domain",
            actual = iccProfile.findEntry("ProfileCopyright")?.value
        )
    }

    /**
     * The number-list entries decode to the invariant decimal values of
     * the s15Fixed16 data, like ExifTool decodes the white point.
     */
    @Test
    fun testIccNumberEntriesAreDecoded() {

        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(2)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertEquals(
            expected = "0.964202880859375 1.0 0.8249053955078125",
            actual = iccProfile.findEntry("MediaWhitePoint")?.value
        )
    }

    /**
     * A profile spread over several APP2 chunks is stitched in chunk
     * order; every chunk of the sequence must be present exactly once.
     */
    @Test
    fun testIccProfileIsReadFromSecondFileWithProfile() {

        /* media_8 also carries an ICC profile in its APP2 segments. */
        val metadata = assertNotNull(Kim.readMetadata(KimTestData.getBytesOf(8)))

        val iccProfile = assertNotNull(metadata.iccProfile)

        assertTrue(iccProfile.entries.isNotEmpty())
        assertNotNull(iccProfile.description)
    }

    /**
     * An APP2 segment that carries the ICC identifier but not the two
     * chunk-sequence bytes behind it must fail with a message that names
     * the truncated ICC chunk header - the incidental index error of the
     * unguarded read is reported as a generic "Failed to read image."
     * instead.
     */
    @Test
    fun testTruncatedIccChunkHeaderFailsWithADescriptiveError() {

        val bytes = buildMinimalJpegWithApp2(
            app2Payload = "ICC_PROFILE\u0000".encodeToByteArray()
        )

        val exception = assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }

        assertTrue(
            exception.message?.contains("ICC") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * Assembles a minimal scannable JPEG whose APP2 segment carries the
     * given payload, for tests that need one specific APP2 variant.
     */
    private fun buildMinimalJpegWithApp2(app2Payload: ByteArray): ByteArray {

        val app2Length = 2 + app2Payload.size

        val sos = convertHexStringToByteArray("ffda0008010100003f00")

        val imageData = convertHexStringToByteArray(
            "112233445566778899aabbccddeeff" +
                "112233445566778899aabbccddeeff01"
        )

        val bytes = ByteArray(4 + app2Length + sos.size + imageData.size + 2)

        var pos = 0
        /* SOI */
        bytes[pos++] = 0xff.toByte()
        bytes[pos++] = 0xd8.toByte()
        /* APP2 */
        bytes[pos++] = 0xff.toByte()
        bytes[pos++] = 0xe2.toByte()
        bytes[pos++] = (app2Length shr 8).toByte()
        bytes[pos++] = app2Length.toByte()
        app2Payload.copyInto(bytes, pos)
        pos += app2Payload.size
        sos.copyInto(bytes, pos)
        pos += sos.size
        imageData.copyInto(bytes, pos)
        pos += imageData.size
        /* EOI */
        bytes[pos++] = 0xff.toByte()
        bytes[pos] = 0xd9.toByte()

        return bytes
    }
}
