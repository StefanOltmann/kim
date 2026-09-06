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
package de.stefan_oltmann.kim.input

import android.os.Build
import java.io.InputStream

/**
 * Provides way to read from Android ContentReolver that
 * should work on all versions.
 */
public open class AndroidInputStreamByteReader(
    private val inputStream: InputStream,
    override val contentLength: Long
) : ByteReader {

    override fun readByte(): Byte? {

        val nextByte = inputStream.read()

        if (nextByte == -1)
            return null

        return nextByte.toByte()
    }

    override fun readBytes(count: Int): ByteArray {
        require(count >= 0) { "Count must not be negative: $count" }


        /*
         * On Android 13 and later use the more efficient API.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return inputStream.readNBytes(count)

        /*
         * Fall back to old API that works on all versions.
         */
        return readBytesLegacy(count)
    }

    /**
     * Reads with the pre-Tiramisu InputStream API.
     *
     * InputStream.read may return fewer bytes than requested even before
     * the end of the stream, so we loop until the request is fulfilled.
     * The result is limited to the bytes actually read at the end of the
     * stream, matching the ByteReader contract. Zero padding would be
     * parsed as data.
     */
    internal fun readBytesLegacy(count: Int): ByteArray {

        val result = ByteArray(count)

        var bytesRead = 0

        while (bytesRead < count) {

            val bytes = inputStream.read(result, bytesRead, count - bytesRead)

            /* End of the stream. */
            if (bytes == -1)
                break

            bytesRead += bytes
        }

        return result.copyOf(bytesRead)
    }

    override fun close(): Unit =
        inputStream.close()
}
