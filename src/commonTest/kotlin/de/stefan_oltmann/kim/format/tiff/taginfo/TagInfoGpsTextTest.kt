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
package de.stefan_oltmann.kim.format.tiff.taginfo

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the charset dispatch of text fields with the 8-byte encoding
 * prefix, like the EXIF UserComment.
 */
class TagInfoGpsTextTest {

    /**
     * Builds an undefined-type text field carrying the given raw bytes.
     */
    private fun undefinedField(bytes: ByteArray): TiffField =
        TiffField(
            offset = 0,
            tag = ExifTag.EXIF_TAG_USER_COMMENT.tag,
            directoryType = ExifTag.EXIF_TAG_USER_COMMENT.directoryType?.typeId ?: 0,
            fieldType = FieldTypeUndefined,
            count = bytes.size,
            localValue = null,
            valueOffset = 0,
            valueBytes = bytes,
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

    /**
     * The charset code 0x03 announces UTF-8: decoding the payload as
     * Latin-1 renders every multi-byte sequence as mojibake, so the
     * prefix must select the UTF-8 decoder.
     */
    @Test
    fun testValueDecodesUtf8CharsetPrefix() {

        val payload = "Hüße テスト".encodeToByteArray()

        val bytes = "UTF-8".encodeToByteArray() + byteArrayOf(0) + byteArrayOf(0, 0) + payload

        assertEquals(
            "Hüße テスト",
            ExifTag.EXIF_TAG_USER_COMMENT.getValue(undefinedField(bytes))
        )
    }

    /**
     * The ASCII prefix keeps its Latin-1 decoding, like before.
     */
    @Test
    fun testValueDecodesAsciiCharsetPrefix() {

        val bytes = "ASCII".encodeToByteArray() + byteArrayOf(0) + byteArrayOf(0, 0) +
            "plain".encodeToByteArray()

        assertEquals(
            "plain",
            ExifTag.EXIF_TAG_USER_COMMENT.getValue(undefinedField(bytes))
        )
    }
}
