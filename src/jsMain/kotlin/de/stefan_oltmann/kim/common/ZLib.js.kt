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

import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

/*
 * The input is fed in bounded slices, so the output size can be checked
 * between pushes and hostile data cannot expand to unbounded memory.
 */
private const val INPUT_SLICE_LENGTH: Int = 64 * 1024

internal actual fun compress(input: String): ByteArray =
    Pako.deflate(input).toByteArray()

/*
 * Attention: Foreign JS throwables are not Exception subclasses, so the
 * boundary must catch Throwable to uphold the API contract that only
 * ImageException types escape.
 */
internal actual fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String =
    try {
        decompressBounded(byteArray, maxOutputByteCount)
    } catch (ex: ImageReadException) {
        throw ex
    } catch (ex: Throwable) {
        throw ImageReadException("Failed to decompress the data.", ex)
    }

/**
 * Feeds the input in bounded slices to pako's streaming inflate, so the
 * output size can be checked between pushes and hostile data cannot
 * expand to unbounded memory.
 *
 * The output is collected as raw bytes and decoded as a whole at the end,
 * because a multi-byte UTF-8 sequence split across two output chunks
 * would be corrupted by a per-chunk decode.
 */
private fun decompressBounded(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String {

    /* An empty stream cannot be valid zlib data. */
    if (byteArray.isEmpty())
        throw ImageReadException("Unexpected end of compressed data.")

    val inflater = Pako.Inflate()

    val byteWriter = ByteArrayByteWriter()

    var producedBytes = 0

    /*
     * The default onData handler collects chunks for the final result.
     * Overriding it lets us count the output while inflating and abort
     * immediately once hostile data exceeds the limit. The exception
     * propagates through pako's push call up to the caller.
     */
    inflater.asDynamic().onData = { chunk: dynamic ->

        val bytes = (chunk as Uint8Array).toByteArray()

        producedBytes += bytes.size

        if (producedBytes > maxOutputByteCount)
            throw ImageReadException(
                "Decompressed data exceeds $maxOutputByteCount bytes."
            )

        byteWriter.write(bytes)
    }

    var consumed = 0

    while (consumed < byteArray.size) {

        val sliceEnd = minOf(consumed + INPUT_SLICE_LENGTH, byteArray.size)

        inflater.push(
            byteArray.copyOfRange(consumed, sliceEnd).toUint8Array(),
            sliceEnd == byteArray.size
        )

        if (inflater.err != 0)
            throw ImageReadException(inflater.msg.ifBlank { "Failed to decompress the data." })

        consumed = sliceEnd
    }

    /*
     * A stream that ends without its final block never triggers pako's
     * onEnd handler and leaves ended=false. Returning the partial output
     * would silently lose data.
     */
    if (!inflater.ended)
        throw ImageReadException("Unexpected end of compressed data.")

    /* The output was collected through the onData handler above. */
    return byteWriter.toByteArray().decodeToString()
}

private fun Uint8Array.toByteArray(): ByteArray =
    Int8Array(buffer, byteOffset, length).unsafeCast<ByteArray>()

private fun ByteArray.toUint8Array(): Uint8Array {
    val int8array = unsafeCast<Int8Array>()
    return Uint8Array(int8array.buffer, int8array.byteOffset, int8array.length)
}

@Suppress("UnusedPrivateMember", "UnusedParameter") // False positive
@JsModule("pako")
@JsNonModule
private external object Pako {

    fun deflate(data: String): Uint8Array

    fun inflate(data: Uint8Array, options: dynamic): String

    /**
     * Streaming variant of pako's inflate. The emitted output pieces are
     * delivered through the [onData] handler, which is assigned dynamically
     * by the decompression code.
     */
    @Suppress("UnusedPrivateMember") // Bound by the JS runtime.
    class Inflate {

        val ended: Boolean

        val err: Int

        val msg: String

        fun push(data: Uint8Array, end: Boolean)
    }
}
