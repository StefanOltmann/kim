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
package de.stefan_oltmann.kim.common

/**
 * True when the bytes start with the UTF-16 big endian byte order mark
 * (FE FF). The little endian mark is the reversed FF FE.
 */
internal fun ByteArray.startsWithUtf16BigEndianBom(): Boolean =
    size >= 2 && this[0] == 0xFE.toByte() && this[1] == 0xFF.toByte()

/**
 * Decodes UTF-16 code units to a String. Surrogate pairs pass through
 * as-is, so supplementary characters survive the conversion.
 *
 * A trailing odd byte, as it can happen in corrupt files, is ignored.
 */
internal fun ByteArray.decodeUtf16BytesToString(littleEndian: Boolean): String {

    val charCount = size / 2

    val chars = CharArray(charCount)

    for (index in 0 until charCount) {

        val low = this[index * 2].toInt() and 0xFF

        val high = this[index * 2 + 1].toInt() and 0xFF

        chars[index] =
            if (littleEndian)
                ((high shl UTF16_HIGH_BYTE_SHIFT) or low).toChar()
            else
                ((low shl UTF16_HIGH_BYTE_SHIFT) or high).toChar()
    }

    return chars.concatToString()
}

/**
 * UTF-16 stores the high byte of each code unit 8 bits ahead of the low
 * byte.
 */
private const val UTF16_HIGH_BYTE_SHIFT = 8
