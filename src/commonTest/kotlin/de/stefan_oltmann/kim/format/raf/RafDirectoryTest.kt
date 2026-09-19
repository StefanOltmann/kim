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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RafDirectoryTest {

    /**
     * The directory of the test photo is verified against the section
     * boundaries of the real file: the JPEG ends at its EOI marker, the
     * CFA header ends where the CFA block starts and the CFA block ends
     * at the end of the file.
     */
    @Test
    fun testReadDirectoryOfTestPhoto() {

        val bytes = KimTestData.getBytesOf(KimTestData.RAF_TEST_IMAGE_INDEX)

        val directory = RafImageParser.readDirectory(ByteArrayByteReader(bytes))

        assertEquals(expected = 148L, actual = directory.jpegImageOffset)
        assertEquals(expected = 5598622L, actual = directory.jpegImageLength)
        assertEquals(expected = 5599136L, actual = directory.cfaHeaderOffset)
        assertEquals(expected = 22112L, actual = directory.cfaHeaderLength)
        assertEquals(expected = 5621248L, actual = directory.cfaOffset)
        assertEquals(expected = 30754528L, actual = directory.cfaLength)

        /* The sections tile the file without overlapping each other. */
        assertEquals(
            expected = directory.cfaOffset,
            actual = directory.cfaHeaderOffset + directory.cfaHeaderLength
        )

        assertEquals(
            expected = bytes.size.toLong(),
            actual = directory.cfaOffset + directory.cfaLength
        )
    }

    /**
     * A hostile JPEG offset that cannot point into the file must be
     * rejected with a targeted message.
     */
    @Test
    fun testReadDirectoryRejectsOutOfRangeJpegOffset() {

        /*
         * RAF magic + 68 header bytes + the six directory entries, so all
         * entries can be read before the range check rejects them.
         */
        val bytes = "FUJIFILMCCD-RAW ".encodeToByteArray() +
            ByteArray(RafEmbeddedJpeg.REMAINING_HEADER_BYTE_COUNT + DIRECTORY_BYTE_COUNT)

        /* A JPEG offset of 0x01000000 cannot point into this short file. */
        bytes[DIRECTORY_JPEG_OFFSET_ENTRY] = 1

        val exception = assertFailsWith<ImageReadException> {
            RafImageParser.readDirectory(ByteArrayByteReader(bytes))
        }

        assertTrue(
            exception.message?.contains("out of range") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    /**
     * A truncated file whose sections reach past its end must be rejected
     * as well, so a caller can trust every section range.
     */
    @Test
    fun testReadDirectoryRejectsTruncatedFile() {

        val bytes = KimTestData.getBytesOf(KimTestData.RAF_TEST_IMAGE_INDEX).copyOf(200)

        val exception = assertFailsWith<ImageReadException> {
            RafImageParser.readDirectory(ByteArrayByteReader(bytes))
        }

        assertTrue(
            exception.message?.contains("out of range") == true,
            "Unexpected message: ${exception.message}"
        )
    }

    private companion object {

        /* The directory table holds six entries of four bytes each. */
        const val DIRECTORY_BYTE_COUNT = 24

        /* The JPEG image offset is the first directory entry. */
        const val DIRECTORY_JPEG_OFFSET_ENTRY = 84
    }
}
