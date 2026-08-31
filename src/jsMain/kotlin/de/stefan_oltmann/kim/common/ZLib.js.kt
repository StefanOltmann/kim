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

internal actual fun compress(input: String): ByteArray =
    Pako.deflate(input).toByteArray()

internal actual fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): String =
    try {

        /* Without options pako returns the raw bytes instead of a string. */
        val rawBytes = Pako.inflate(byteArray.toUint8Array())

        /*
         * Abort before hostile data is decoded further, so it can never
         * grow the output beyond the limit.
         */
        if (rawBytes.length > maxOutputByteCount)
            throw ImageReadException(
                "Decompressed data exceeds $maxOutputByteCount bytes."
            )

        rawBytes.toByteArray().decodeToString()

    } catch (ex: ImageReadException) {

        throw ex

    } catch (ex: Throwable) {

        /*
         * Kotlin-thrown errors carry their cause, while foreign JS
         * throwables are not Throwable instances (the instanceof check
         * above rethrows them) and are handled by the dynamic catch.
         */
        throw ImageReadException("Failed to decompress the data.", ex)

    } catch (ex: dynamic) {

        /*
         * Attention: Foreign JS throwables are not Throwable instances,
         * so pako's errors can only be caught without a type check.
         */
        throw ImageReadException("Failed to decompress the data: $ex")
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
    fun inflate(data: Uint8Array): Uint8Array
}
