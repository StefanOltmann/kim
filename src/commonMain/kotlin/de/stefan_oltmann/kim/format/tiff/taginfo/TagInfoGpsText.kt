/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.decodeUtf16BytesToString
import de.stefan_oltmann.kim.common.encodeToLatin1Bytes
import de.stefan_oltmann.kim.common.isEquals
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.startsWithUtf16BigEndianBom
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined

/**
 * Used by some GPS tags and the EXIF user comment tag,
 * this badly documented value is meant to contain
 * the text encoding in the first 8 bytes followed by
 * the non-null-terminated text in an unknown byte order.
 */
public class TagInfoGpsText(
    tag: Int,
    name: String,
    exifDirectory: TiffDirectoryType?
) : TagInfo(tag, name, FieldTypeUndefined, LENGTH_UNKNOWN, exifDirectory) {

    override fun isText(): Boolean =
        true

    public fun encodeValue(value: Any): ByteArray {

        if (value !is String)
            throw ImageWriteException("GPS text value not String: $value")

        val asciiBytes = value.encodeToLatin1Bytes()

        val result = ByteArray(asciiBytes.size + TEXT_ENCODING_ASCII_BYTES.size)

        TEXT_ENCODING_ASCII_BYTES.copyInto(
            destination = result,
            destinationOffset = 0,
            endIndex = TEXT_ENCODING_ASCII_BYTES.size
        )

        asciiBytes.copyInto(
            destination = result,
            destinationOffset = TEXT_ENCODING_ASCII_BYTES.size,
            endIndex = asciiBytes.size
        )

        return result
    }

    public fun getValue(entry: TiffField): String {

        val fieldType = entry.fieldType

        if (fieldType === FieldTypeAscii)
            return FieldTypeAscii.getValue(entry.valueBytes, entry.byteOrder)

        if (fieldType !== FieldTypeUndefined && fieldType !== FieldTypeByte)
            throw ImageReadException("GPS text field not encoded as bytes.")

        val bytes = entry.valueBytes

        if (bytes.all { it == ZERO_BYTE })
            return ""

        /* Try ASCII with NO prefix. */
        if (bytes.size < TEXT_ENCODING_BYTE_LENGTH)
            return bytes.decodeLatin1BytesToString()

        val encodingPrefixBytes = bytes.slice(
            startIndex = 0,
            count = TEXT_ENCODING_BYTE_LENGTH
        )

        val hasEncoding =
            encodingPrefixBytes.contentEquals(TEXT_ENCODING_ASCII_BYTES) ||
                encodingPrefixBytes.contentEquals(TEXT_ENCODING_UNDEFINED_BYTES)

        if (hasEncoding) {

            val bytesWithoutPrefix = bytes.copyOfRange(
                fromIndex = TEXT_ENCODING_BYTE_LENGTH,
                toIndex = bytes.size
            )

            if (bytesWithoutPrefix.all { it == ZERO_BYTE })
                return ""

            val decodedString = bytesWithoutPrefix.decodeLatin1BytesToString()

            val reEncodedBytes = decodedString.encodeToLatin1Bytes()

            val bytesEqual = bytes.isEquals(
                start = TEXT_ENCODING_BYTE_LENGTH,
                other = reEncodedBytes,
                otherStart = 0,
                length = reEncodedBytes.size
            )

            if (bytesEqual)
                return decodedString
        }

        /*
         * The UTF-16 code is only recognized in upper case: ExifTool notes
         * that Ricoh writes a lower case "Unicode\0" while the text is
         * actually ASCII, so matching it here would decode plain ASCII as
         * UTF-16 garbage.
         */
        if (encodingPrefixBytes.contentEquals(TEXT_ENCODING_UNICODE_BYTES)) {

            val payload = bytes.copyOfRange(
                fromIndex = TEXT_ENCODING_BYTE_LENGTH,
                toIndex = bytes.size
            )

            val decodedString = payload.decodeUtf16BytesToString(
                littleEndian = !payload.startsWithUtf16BigEndianBom()
            )

            /* A terminating NUL character cuts the text like in ASCII. */
            val terminatorIndex = decodedString.indexOf('\u0000')

            return if (terminatorIndex > -1)
                decodedString.take(terminatorIndex)
            else
                decodedString
        }

        return bytes.decodeLatin1BytesToString()
    }

    private companion object {

        private const val ZERO_BYTE: Byte = 0.toByte()

        private const val TEXT_ENCODING_BYTE_LENGTH = 8

        /**
         * Code for US-ASCII.
         *
         * This is a subset of ISO-8859-1 (Latin), so we can use that.
         */
        private val TEXT_ENCODING_ASCII_BYTES =
            byteArrayOf(0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00)

        /**
         * Code for UTF-16. The byte order is signaled by a BOM in the
         * payload and defaults to little endian, like ExifTool assumes.
         */
        private val TEXT_ENCODING_UNICODE_BYTES =
            byteArrayOf(0x55, 0x4E, 0x49, 0x43, 0x4F, 0x44, 0x45, 0x00)

        /*
         * Undefined
         *
         * Try to interpret an undefined text as ISO-8859-1 (Latin)
         */
        private val TEXT_ENCODING_UNDEFINED_BYTES =
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    }
}
