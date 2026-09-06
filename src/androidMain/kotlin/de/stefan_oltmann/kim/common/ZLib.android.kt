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
    val inputBytes = input.toByteArray()

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

internal actual fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String {

    val inflater = Inflater()

    try {

        val outputStream = ByteArrayOutputStream()

        val buffer = ByteArray(ZLIB_BUFFER_SIZE)

        inflater.setInput(byteArray)

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
                break

            if (inflater.needsInput()) {

                /*
                 * The data ended without a final block. Returning the
                 * partial output would silently lose data.
                 */
                throw ImageReadException("Unexpected end of compressed data.")
            }

            if (inflater.needsDictionary())
                throw ImageReadException("Compressed data requires a preset dictionary.")

            throw ImageReadException("The inflater could not make progress.")
        }

        /*
         * Decode explicitly as UTF-8, matching all other platforms. The
         * platform default charset differs between systems (for example
         * windows-1252 on Windows JDKs below 18) and would corrupt
         * multi-byte characters depending on the machine.
         */
        return outputStream.toByteArray().decodeToString()

    } catch (ex: DataFormatException) {
        throw ImageReadException("Failed to decompress the data.", ex)
    } finally {
        inflater.end()
    }
}
