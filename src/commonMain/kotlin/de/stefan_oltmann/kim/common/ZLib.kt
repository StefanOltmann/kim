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

/*
 * Compressed chunks of untrusted files must not expand to gigabytes of
 * memory during metadata parsing (zlib allows expansion ratios beyond
 * 1000:1), so every platform aborts decompression once the output
 * exceeds this limit.
 */
internal const val MAX_DECOMPRESSED_BYTE_COUNT: Int = 8 * 1024 * 1024

internal expect fun compress(input: String): ByteArray

/**
 * Decompresses the given zlib data.
 *
 * Aborts with an [ImageReadException] when the output exceeds
 * [maxOutputByteCount], so hostile input cannot exhaust the memory.
 */
internal expect fun decompress(
    byteArray: ByteArray,
    maxOutputByteCount: Int = MAX_DECOMPRESSED_BYTE_COUNT
): String
