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

import dev.karmakrafts.kompress.zlib.ZlibCompressor
import dev.karmakrafts.kompress.zlib.ZlibDecompressor

internal expect fun compress(input: String): ByteArray

internal expect fun decompress(byteArray: ByteArray): String

internal fun zlibCompress(input: String): ByteArray =
    ZlibCompressor.compress(input.encodeToByteArray())

internal fun zlibDecompress(byteArray: ByteArray): String =
    ZlibDecompressor.decompress(byteArray).decodeToString()
