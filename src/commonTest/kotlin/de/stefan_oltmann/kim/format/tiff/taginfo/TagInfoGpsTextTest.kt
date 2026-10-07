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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the charset dispatch of text fields with the 8-byte encoding
 * prefix, like the EXIF UserComment.
 */
class TagInfoGpsTextTest {

    /**
     * Builds an undefined-type text field carrying the given raw bytes.
     */
    private fun undefinedField(
        bytes: ByteArray,
        byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
    ): TiffField =
        TiffField(
            offset = 0,
            tag = ExifTag.EXIF_TAG_USER_COMMENT.tag,
            directoryType = ExifTag.EXIF_TAG_USER_COMMENT.directoryType?.typeId ?: 0,
            fieldType = FieldTypeUndefined,
            count = bytes.size,
            localValue = null,
            valueOffset = 0,
            valueBytes = bytes,
            byteOrder = byteOrder,
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

    /**
     * The write policy forbids writing data that no longer represents the
     * input: a character beyond Latin-1 must fail the write like in
     * ByteWriter.writeString - a silent '?' placeholder would destroy the
     * text without any error.
     */
    @Test
    fun testEncodeValueRejectsNonLatin1Characters() {

        assertFailsWith<ImageWriteException> {
            GpsTag.GPS_TAG_GPS_PROCESSING_METHOD.encodeValue("\u4eac")
        }
    }

    /**
     * Latin-1 text keeps encoding as the ASCII charset prefix followed by
     * the single-byte characters.
     */
    @Test
    fun testEncodeValueKeepsLatin1Text() {

        assertContentEquals(
            expected = byteArrayOf(
                0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00, /* "ASCII" */
                0x63, 0x61, 0x66, 0xE9.toByte() /* "café" in Latin-1 */
            ),
            actual = GpsTag.GPS_TAG_GPS_PROCESSING_METHOD.encodeValue("café")
        )
    }

    /**
     * BOM-less UTF-16 follows the byte order of the surrounding TIFF
     * structure - the convention BOM-less writers use. A big-endian
     * field carrying big-endian units must not decode as byte-swapped
     * mojibake through a fixed little-endian fallback.
     */
    @Test
    fun testValueDecodesBomLessUtf16InTheFieldByteOrder() {

        /* "Hi" in UTF-16 big endian, no BOM, on an MM field. */
        val bigEndianBytes =
            "UNICODE".encodeToByteArray() + byteArrayOf(0) +
                byteArrayOf(0x00, 0x48, 0x00, 0x69)

        assertEquals(
            "Hi",
            ExifTag.EXIF_TAG_USER_COMMENT.getValue(undefinedField(bigEndianBytes))
        )

        /* "Hi" in UTF-16 little endian, no BOM, on an II field. */
        val littleEndianBytes =
            "UNICODE".encodeToByteArray() + byteArrayOf(0) +
                byteArrayOf(0x48, 0x00, 0x69, 0x00)

        assertEquals(
            "Hi",
            ExifTag.EXIF_TAG_USER_COMMENT.getValue(
                undefinedField(littleEndianBytes, ByteOrder.LITTLE_ENDIAN)
            )
        )
    }

    /**
     * A byte order mark always decides, independent of the field byte
     * order - a big-endian BOM on a little-endian field still decodes
     * big endian.
     */
    @Test
    fun testValueDecodesUtf16WithByteOrderMark() {

        val bytes =
            "UNICODE".encodeToByteArray() + byteArrayOf(0) +
                byteArrayOf(0xFE.toByte(), 0xFF.toByte(), 0x00, 0x48, 0x00, 0x69)

        assertEquals(
            "Hi",
            ExifTag.EXIF_TAG_USER_COMMENT.getValue(
                undefinedField(bytes, ByteOrder.LITTLE_ENDIAN)
            )
        )
    }
}
