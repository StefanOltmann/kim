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

import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit
import platform.zlib.inflateReset
import platform.zlib.z_stream

private const val OUTPUT_BUFFER_LENGTH = 4096

@OptIn(ExperimentalForeignApi::class)
internal actual fun decompressBytesPlatform(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): ByteArray {

    memScoped {

        /* Create a zlib stream structure */
        val stream = alloc<z_stream>()

        /* Initialize the zlib stream and check the return code. */
        val inflateInitResult = inflateInit(stream.ptr)

        if (inflateInitResult != Z_OK)
            throw ImageReadException("inflateInit failed: $inflateInitResult")

        /* Set the input buffer and its length. */
        stream.next_in = byteArray.refTo(0).getPointer(this).reinterpret()
        stream.avail_in = byteArray.size.toUInt()

        val outputBuffer = ByteArray(OUTPUT_BUFFER_LENGTH)

        val byteWriter = ByteArrayByteWriter()

        var totalBytesWritten = 0L

        try {
            while (true) {

                /* Set the output buffer and its length */
                stream.next_out = outputBuffer.refTo(0).getPointer(this).reinterpret()
                stream.avail_out = OUTPUT_BUFFER_LENGTH.toUInt()

                /* Decompress the data */
                val result = inflate(stream.ptr, Z_NO_FLUSH)

                if (result != Z_OK && result != Z_STREAM_END) {

                    /* An error occurred during decompression */
                    throw ImageReadException("Decompression error: $result")
                }

                val bytesWritten = OUTPUT_BUFFER_LENGTH - stream.avail_out.toInt()

                /*
                 * Abort before the untrusted data grows the output beyond
                 * the limit, so it can never be allocated completely.
                 */
                totalBytesWritten += bytesWritten

                if (totalBytesWritten > maxOutputByteCount)
                    throw ImageReadException(
                        "Decompressed data exceeds $maxOutputByteCount bytes."
                    )

                byteWriter.write(outputBuffer.copyOf(bytesWritten))

                /*
                 * The end of a compressed member was reached. Concatenated
                 * members are legal: reset the stream and continue with
                 * the remaining input.
                 */
                if (result == Z_STREAM_END) {

                    if (stream.avail_in > 0u) {

                        inflateReset(stream.ptr)

                        continue
                    }

                    break
                }
            }

        } finally {
            /* Clean up the zlib stream */
            inflateEnd(stream.ptr)
        }

        return@decompressBytesPlatform byteWriter.toByteArray()
    }
}
