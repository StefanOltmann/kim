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
         *
         * Concatenated zlib members are legal: when pako ends a member
         * while input remains, a fresh inflater continues with the rest.
         */
        val collected = mutableListOf<ByteArray>()

        var budgetUsed = 0

        var offset = 0

        while (offset < byteArray.size) {

            val inflater = Pako.Inflate()

            var memberEnded = false

            while (offset < byteArray.size && !memberEnded) {

                val chunkLength = minOf(INFLATE_INPUT_CHUNK_SIZE, byteArray.size - offset)

                inflater.push(byteArray.toUint8Array(offset, chunkLength))

                offset += chunkLength

                /*
                 * The budget check uses the member's own counter: pako
                 * resets it when it continues with a concatenated member,
                 * so it can only under-count within a single push, whose
                 * output is bounded by the chunk size. The exact total is
                 * enforced at the member boundary below.
                 */
                if (budgetUsed + inflater.strm.total_out > maxOutputByteCount)
                    throw ImageReadException(
                        "Decompressed data exceeds $maxOutputByteCount bytes."
                    )

                memberEnded = inflater.ended
            }

            if (!memberEnded) {

                /* The input is exhausted - finalize the member. */
                inflater.push(Uint8Array(0), end = true)

                if (budgetUsed + inflater.strm.total_out > maxOutputByteCount)
                    throw ImageReadException(
                        "Decompressed data exceeds $maxOutputByteCount bytes."
                    )
            }

            val result = inflater.result
                ?: throw ImageReadException("Failed to decompress the data.")

            budgetUsed += result.length

            if (budgetUsed > maxOutputByteCount)
                throw ImageReadException(
                    "Decompressed data exceeds $maxOutputByteCount bytes."
                )

            collected.add(result.toByteArray())
        }

        val rawBytes = ByteArray(budgetUsed)

        var position = 0

        for (member in collected) {

            member.copyInto(rawBytes, position)

            position += member.size
        }

        rawBytes.decodeToString()

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
        val ended: Boolean
        val strm: ZStream
        val result: Uint8Array?

        fun push(data: Uint8Array, end: Boolean = definedExternally)
    }

    class ZStream {
        val total_out: Int
    }
}
