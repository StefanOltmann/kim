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

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

internal actual fun decompressBytesPlatform(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): ByteArray =
    try {
        decompressBytesWithBudget(byteArray, maxOutputByteCount, ::PakoInflater)

    } catch (ex: ImageReadException) {

        throw ex

    } catch (ex: Throwable) {

        /*
         * Kotlin-thrown errors are Throwable instances and carry their
         * cause. Foreign JS throwables are not, so they fall through to
         * the dynamic catch below.
         */
        throw ImageReadException("Failed to decompress the data.", ex)

    } catch (ex: dynamic) {

        /*
         * Attention: Foreign JS throwables are not Throwable instances,
         * so pako's errors can only be caught without a type check.
         */
        throw ImageReadException("Failed to decompress the data: $ex")
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

private fun ByteArray.toUint8Array(): Uint8Array {
    val int8array = unsafeCast<Int8Array>()
    return Uint8Array(int8array.buffer, int8array.byteOffset, int8array.length)
}

private fun Uint8Array.toByteArray(): ByteArray =
    Int8Array(buffer, byteOffset, length).unsafeCast<ByteArray>()

@Suppress("UnusedPrivateMember", "UnusedParameter") // False positive
@JsModule("pako")
@JsNonModule
private external object Pako {
    /**
     * The incremental stream interface. The accumulated output is visible
     * in [result] between pushes, which is what the decompression budget
     * checks against.
     */
    class Inflate(options: Any = definedExternally) {
        val ended: Boolean
        val strm: ZStream
        val result: Uint8Array?

        fun push(data: Uint8Array, end: Boolean = definedExternally)
    }

    class ZStream {
        val total_out: Int
    }
}
