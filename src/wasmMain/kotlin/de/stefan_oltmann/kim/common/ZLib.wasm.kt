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
internal actual fun decompressBytes(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): ByteArray =
    try {
        decompressBytesWithBudget(byteArray, maxOutputByteCount, ::PakoInflater)

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

private class PakoInflater : WebInflater {

    private val inflater = Pako.Inflate()

    override val ended: Boolean
        get() = inflater.ended

    override val totalOut: Int
        get() = inflater.strm.total_out

    override val result: ByteArray?
        get() = inflater.result?.toByteArray()

    override fun push(chunk: ByteArray) {
        inflater.push(chunk.toUint8Array())
    }

    override fun pushEnd() {
        inflater.push(Uint8Array(0), end = true)
    }
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
