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

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

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
@OptIn(ExperimentalWasmJsInterop::class)
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
@OptIn(ExperimentalWasmJsInterop::class)
private fun decompressBounded(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String {

    /* An empty stream cannot be valid zlib data. */
    if (byteArray.isEmpty())
        throw ImageReadException("Unexpected end of compressed data.")

    val inflater = Inflate()

    var totalOutputBytes = 0

    var processedChunks = 0

    var consumed = 0

    while (consumed < byteArray.size) {

        val sliceEnd = minOf(consumed + INPUT_SLICE_LENGTH, byteArray.size)

        inflater.push(
            byteArray.copyOfRange(consumed, sliceEnd).toUint8Array(),
            sliceEnd == byteArray.size
        )

        if (inflater.err != 0)
            throw ImageReadException(
                inflater.msg?.toString()?.ifBlank { "Failed to decompress the data." }
                    ?: "Failed to decompress the data."
            )

        /*
         * Abort before the untrusted data grows the output beyond the
         * limit, so it can never be allocated completely. The output of
         * the final slice is checked through the result below.
         */
        while (processedChunks < inflater.chunks.length) {

            val chunk = inflater.chunks[processedChunks]!!.unsafeCast<Uint8Array>()

            totalOutputBytes += chunk.length

            if (totalOutputBytes > maxOutputByteCount)
                throw ImageReadException(
                    "Decompressed data exceeds $maxOutputByteCount bytes."
                )

            processedChunks++
        }

        consumed = sliceEnd
    }

    /*
     * A stream that ends without its final block never triggers pako's
     * onEnd handler and leaves ended=false. Returning the partial output
     * would silently lose data.
     */
    if (!inflater.ended)
        throw ImageReadException("Unexpected end of compressed data.")

    /*
     * The flattened full output is exposed as raw bytes, since no string
     * option was set.
     */
    val result = inflater.result.unsafeCast<Uint8Array>()

    if (result.length > maxOutputByteCount)
        throw ImageReadException(
            "Decompressed data exceeds $maxOutputByteCount bytes."
        )

    return result.toByteArray().decodeToString()
}

private fun Uint8Array.toByteArray(): ByteArray =
    ByteArray(length) { this[it] }

private fun ByteArray.toUint8Array(): Uint8Array {
    val result = Uint8Array(size)
    forEachIndexed { index, byte ->
        result[index] = byte
    }
    return result
}

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedPrivateMember", "UnusedParameter") // False positive
@JsModule("pako")
private external object Pako {
    fun deflate(data: String): Uint8Array
    fun inflate(data: Uint8Array, options: JsAny): String
}

/**
 * Streaming variant of pako's inflate, so decompression can be aborted
 * once the output exceeds the configured limit. The emitted output pieces
 * are collected in [chunks] as raw byte arrays.
 */
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedPrivateMember") // Bound by the JS runtime.
@JsModule("pako")
private external class Inflate {

    val chunks: JsArray<JsAny>

    val ended: Boolean

    val err: Int

    val msg: JsString?

    val result: JsAny

    fun push(data: Uint8Array, end: Boolean)
}
