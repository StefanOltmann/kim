/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

internal expect fun ByteArray.decodeLatin1BytesToString(): String

internal expect fun String.encodeToLatin1Bytes(): ByteArray

/* The highest code point a single Latin-1 byte represents. */
private const val MAX_LATIN1_CHAR_CODE: Int = 0xFF

/**
 * Throws an [ImageWriteException] naming the first character that has no
 * single-byte Latin-1 representation.
 *
 * This is the single authority of the write policy for single-byte text:
 * only Latin-1 maps a character to its own byte, and truncating anything
 * beyond it - to the low byte or a '?' placeholder - would silently emit
 * data that no longer represents the input. The Latin-1 decoding paths
 * keep their lossy behavior; only write paths call this check.
 */
internal fun String.requireLatin1Encodable() {

    val invalidChar = firstOrNull { char -> char.code > MAX_LATIN1_CHAR_CODE }

    if (invalidChar != null)
        throw ImageWriteException(
            "The character U+${invalidChar.code.toString(HEX_RADIX).uppercase()} cannot be " +
                "written as a single byte."
        )
}
