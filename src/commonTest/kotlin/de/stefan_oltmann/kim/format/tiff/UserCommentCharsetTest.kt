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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the UserComment character set handling.
 *
 * The EXIF specification defines an 8-byte character code in front of the
 * UserComment text: "ASCII\0\0\0", "UNICODE\0" (UTF-16), "JIS\0\0\0\0\0"
 * or eight NUL bytes. The test file was written with the ExifTool binary:
 * a copy of media_2 whose UserComment was set to a non-Latin value, so
 * ExifTool encoded it as UTF-16 behind the "UNICODE\0" code. ExifTool
 * reads it back as "Kommentar Unicode Тест".
 */
class UserCommentCharsetTest {

    @Test
    fun testReadsUnicodeUserComment() {

        val bytes = KimTestData.getBytesOf("usercomment_unicode.jpg")

        val metadata = assertNotNull(Kim.readMetadata(bytes))

        val userComment = assertNotNull(metadata.findTiffField(ExifTag.EXIF_TAG_USER_COMMENT))

        /* The value the ExifTool binary reports for this file. */
        assertEquals(
            expected = "Kommentar Unicode Тест",
            actual = userComment.value.toString()
        )
    }

    /**
     * A UserComment that announces the UTF-8 charset but carries a
     * malformed sequence cannot be decoded strictly; the field value
     * degrades to its raw bytes instead of failing the read. The fixture
     * derives from the ExifTool-written unicode variant by patching the
     * charset code and the first payload bytes; the ExifTool binary reads
     * it back with "Invalid EXIF text encoding for UserComment".
     */
    @Test
    fun testReadsMalformedUtf8UserCommentAsRawBytes() {

        val bytes = KimTestData.getBytesOf("usercomment_malformed_utf8.jpg")

        val metadata = assertNotNull(Kim.readMetadata(bytes))

        val userComment = assertNotNull(metadata.findTiffField(ExifTag.EXIF_TAG_USER_COMMENT))

        /* The field keeps the raw bytes; no text is fabricated. */
        assertContentEquals(
            expected = userComment.valueBytes,
            actual = userComment.value as ByteArray
        )
    }
}
