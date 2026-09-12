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

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

private const val ZLIB_BUFFER_SIZE: Int = 1024

internal actual fun compress(input: String): ByteArray {

    val deflater = Deflater()
    val inputBytes = input.encodeToByteArray()

    deflater.setInput(inputBytes)
    deflater.finish()

    val outputStream = ByteArrayOutputStream(inputBytes.size)

    val buffer = ByteArray(ZLIB_BUFFER_SIZE)

    try {

        while (!deflater.finished()) {

            val count = deflater.deflate(buffer)

            outputStream.write(buffer, 0, count)
        }

        return outputStream.toByteArray()

    } finally {

        /* Releases the native zip structure, also when deflating throws. */
        deflater.end()
    }
}

internal actual fun decompressBytes(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): ByteArray {

    /* An empty stream cannot be valid zlib data. */
    if (byteArray.isEmpty())
        throw ImageReadException("Unexpected end of compressed data.")

    val outputStream = ByteArrayOutputStream()

    val buffer = ByteArray(ZLIB_BUFFER_SIZE)

    var inputOffset = 0

    /*
     * Concatenated zlib members are legal: when one member ends and more
     * input remains, a fresh inflater continues with the rest.
     */
    while (inputOffset < byteArray.size) {

        val inflater = Inflater()

        inputOffset += inflateMember(inflater, byteArray, inputOffset, outputStream, buffer, maxOutputByteCount)
    }

    return outputStream.toByteArray()
}

/**
 * Inflates one zlib member starting at [inputOffset] and appends its
 * output to [outputStream], enforcing the output budget.
 *
 * Returns the number of input bytes the member actually consumed, so the
 * caller can continue with a fresh inflater for a concatenated member.
 */
private fun inflateMember(
    inflater: Inflater,
    input: ByteArray,
    inputOffset: Int,
    outputStream: ByteArrayOutputStream,
    buffer: ByteArray,
    maxOutputByteCount: Int
): Int {

    try {

        inflater.setInput(input, inputOffset, input.size - inputOffset)

        while (true) {

            val count = inflater.inflate(buffer)

            if (count > 0) {

                /*
                 * Abort before the untrusted data grows the output beyond
                 * the limit, so it can never be allocated completely.
                 */
                if (outputStream.size() + count > maxOutputByteCount)
                    throw ImageReadException(
                        "Decompressed data exceeds $maxOutputByteCount bytes."
                    )

                outputStream.write(buffer, 0, count)
                continue
            }

            /* The inflater made no progress, so one of these must apply. */
            if (inflater.finished())
                return input.size - inputOffset - inflater.remaining

            if (inflater.needsInput())
                throw ImageReadException("Unexpected end of compressed data.")

            if (inflater.needsDictionary())
                throw ImageReadException("Compressed data requires a preset dictionary.")

            throw ImageReadException("The inflater could not make progress.")
        }

    } catch (ex: DataFormatException) {
        throw ImageReadException("Failed to decompress the data.", ex)
    } finally {
        inflater.end()
    }
}
