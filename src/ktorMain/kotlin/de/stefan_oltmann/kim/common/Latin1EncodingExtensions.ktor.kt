/*
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

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.decode
import kotlinx.io.Buffer

private val decoder = Charsets.ISO_8859_1.newDecoder()

/* Latin-1 covers the Unicode range 0x00 to 0xFF. */
private const val LATIN1_MAX_CHAR_CODE: Int = 0xFF

private const val UNKNOWN_CHAR_BYTE: Byte = 0x3F

internal actual fun ByteArray.decodeLatin1BytesToString(): String {

    val buffer = Buffer()
    buffer.write(this)

    return decoder.decode(buffer)
}

/*
 * Replaces characters outside Latin-1 with a question mark. The Darwin
 * charset encoder throws MalformedInputException for them, so we match the
 * behavior of the JS and WASM actuals instead.
 */
internal actual fun String.encodeToLatin1Bytes(): ByteArray =
    map { char -> if (char.code <= LATIN1_MAX_CHAR_CODE) char.code.toByte() else UNKNOWN_CHAR_BYTE }.toByteArray()
