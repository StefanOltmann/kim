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

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

internal actual fun compress(input: String): ByteArray =
    Pako.deflate(input).toByteArray()

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String =
    try {

        /*
         * The input is fed in bounded chunks, so the accumulated output can
         * be checked between pushes. A whole-buffer inflate grows the full
         * output in memory before any size check can run, which lets a few
         * KB of hostile input exhaust the memory on this target.
         */
        val inflater = Pako.Inflate()

        var offset = 0

        while (offset < byteArray.size) {

            val chunkLength = minOf(INFLATE_INPUT_CHUNK_SIZE, byteArray.size - offset)

            inflater.push(byteArray.toUint8Array(offset, chunkLength))

            if (inflater.strm.total_out > maxOutputByteCount)
                throw ImageReadException(
                    "Decompressed data exceeds $maxOutputByteCount bytes."
                )

            offset += chunkLength
        }

        inflater.push(Uint8Array(0), end = true)

        val rawBytes = inflater.result
            ?: throw ImageReadException("Failed to decompress the data.")

        rawBytes.toByteArray().decodeToString()

    } catch (ex: ImageReadException) {

        throw ex

    } catch (ex: Throwable) {

        /*
         * Attention: Foreign JS throwables are not Exception subclasses,
         * so pako's errors must be caught as Throwable to uphold the
         * contract that only ImageException types escape this module.
         */
        throw ImageReadException("Failed to decompress the data.", ex)
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

private fun ByteArray.toUint8Array(offset: Int, length: Int): Uint8Array {
    val result = Uint8Array(length)
    for (index in 0 until length)
        result[index] = this[offset + index]
    return result
}

internal const val INFLATE_INPUT_CHUNK_SIZE: Int = 8192

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedPrivateMember", "UnusedParameter") // False positive
@JsModule("pako")
private external object Pako {
    fun deflate(data: String): Uint8Array
    fun inflate(data: Uint8Array): Uint8Array

    /**
     * The incremental stream interface. The accumulated output is visible
     * in [chunks] between pushes, which is what the decompression budget
     * checks against.
     */
    class Inflate {
        val strm: ZStream
        val result: Uint8Array?

        fun push(data: Uint8Array, end: Boolean = definedExternally)
    }

    class ZStream {
        val total_out: Int
    }
}
