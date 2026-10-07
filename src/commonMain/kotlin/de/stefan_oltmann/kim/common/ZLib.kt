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

/*
 * Compressed chunks of untrusted files must not expand to gigabytes of
 * memory during metadata parsing (zlib allows expansion ratios beyond
 * 1000:1), so every platform aborts decompression once the output
 * exceeds this limit.
 */
internal const val MAX_DECOMPRESSED_BYTE_COUNT: Int = 8 * 1024 * 1024

/* The zlib header is two bytes: CMF and FDG. */
private const val ZLIB_HEADER_LENGTH: Int = 2

/* The zlib CMF low nibble names the compression method: 8 is deflate. */
private const val ZLIB_DEFLATE_METHOD: Int = 8

/* The zlib header is valid when CMF and FDG together are a multiple of 31. */
private const val ZLIB_HEADER_CHECK_MODULUS: Int = 31

/**
 * Decompresses the given zlib data into raw bytes.
 *
 * Aborts with an [ImageReadException] when the output exceeds
 * [maxOutputByteCount], so hostile input cannot exhaust the memory.
 */
internal expect fun decompressBytesPlatform(
    byteArray: ByteArray,
    maxOutputByteCount: Int
): ByteArray

/**
 * Decompresses the given zlib data into raw bytes.
 *
 * An empty input is truncated zlib data, not empty output, and fails
 * with an [ImageReadException] on every platform - the PNG text chunks
 * are parsed from file-controlled bytes, so a chunk cut before its
 * compressed payload must not succeed here on some targets only.
 */
internal fun decompressBytes(
    byteArray: ByteArray,
    maxOutputByteCount: Int = MAX_DECOMPRESSED_BYTE_COUNT
): ByteArray {

    if (byteArray.size < ZLIB_HEADER_LENGTH)
        throw ImageReadException("Unexpected end of compressed data.")

    /*
     * The zlib header check runs on every platform: pako's incremental
     * inflater auto-detects gzip and raw-deflate unless windowBits is
     * pinned, so without this guard a gzip-framed or raw-deflate payload
     * would decode on JS/wasm while JVM and native reject it - identical
     * input, divergent read outcome. The CMF low nibble must name the
     * deflate method (8) and CMF/FDG together a multiple of 31 - the
     * same checks inflate itself performs.
     */
    val headerByte = byteArray[0].toInt() and 0xFF

    val flagByte = byteArray[1].toInt() and 0xFF

    val isZlibHeader =
        headerByte and 0x0F == ZLIB_DEFLATE_METHOD &&
            ((headerByte shl 8) or flagByte) % ZLIB_HEADER_CHECK_MODULUS == 0

    if (!isZlibHeader)
        throw ImageReadException("The payload is not zlib-compressed data.")

    return decompressBytesPlatform(byteArray, maxOutputByteCount)
}
