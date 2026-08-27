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
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_FINISH
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.deflate
import platform.zlib.deflateBound
import platform.zlib.deflateEnd
import platform.zlib.deflateInit
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit
import platform.zlib.z_stream

private const val OUTPUT_BUFFER_LENGTH = 4096

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal actual fun compress(input: String): ByteArray {

    memScoped {

        /* Create a zlib stream structure */
        val stream = alloc<z_stream>()

        val inputBuffer = input.encodeToByteArray()

        /* Initialize the zlib stream and check the return code. */
        val initResult = deflateInit(stream.ptr, Z_DEFAULT_COMPRESSION)

        if (initResult != Z_OK)
            throw ImageReadException("deflateInit failed: $initResult")

        try {

            val inputBufferLength = inputBuffer.size
            val outputBufferLength = deflateBound(stream.ptr, inputBufferLength.convert())

            val outputBuffer = ByteArray(outputBufferLength.toInt())

            /* Set the input buffer and its length. */
            if (inputBuffer.isNotEmpty())
                stream.next_in = inputBuffer.refTo(0).getPointer(this).reinterpret()
            else
                stream.next_in = null

            stream.avail_in = inputBufferLength.toUInt()

            /* Set the output buffer and its length. */
            if (outputBuffer.isNotEmpty())
                stream.next_out = outputBuffer.refTo(0).getPointer(this).reinterpret()
            else
                stream.next_out = null

            stream.avail_out = outputBufferLength.convert()

            /* Compress the data and check the return code. */
            val deflateResult = deflate(stream.ptr, Z_FINISH)

            if (deflateResult != Z_OK && deflateResult != Z_STREAM_END)
                throw ImageReadException("deflate failed: $deflateResult")

            /* Get the compressed data length. */
            val compressedDataLength = outputBufferLength - stream.avail_out

            /* Return the compressed data as a ByteArray. */
            return@compress outputBuffer.copyOf(compressedDataLength.toInt())

        } finally {
            /* Clean up the zlib stream. */
            deflateEnd(stream.ptr)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String {

    /* An empty stream cannot be valid zlib data. */
    if (byteArray.isEmpty())
        throw ImageReadException("Unexpected end of compressed data.")

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

        /*
         * The raw blocks are collected first and decoded as a whole at the
         * end, because a multi-byte UTF-8 sequence that is split across two
         * blocks would be corrupted by a per-block decode.
         */
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

                /* The end of the compressed data was reached */
                if (result == Z_STREAM_END)
                    break
            }

        } finally {
            /* Clean up the zlib stream */
            inflateEnd(stream.ptr)
        }

        return@decompress byteWriter.toByteArray().decodeToString()
    }
}
