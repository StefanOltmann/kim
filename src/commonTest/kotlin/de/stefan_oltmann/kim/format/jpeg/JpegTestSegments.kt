/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Writes a JPEG segment: the 0xFF marker prefix, a big-endian length that
 * includes the two length bytes, then the payload.
 */
internal fun writeSegment(
    byteWriter: ByteArrayByteWriter,
    marker: Int,
    payload: ByteArray
) {

    byteWriter.write(byteArrayOf(0xFF.toByte(), marker.toByte()))

    val length = payload.size + 2

    byteWriter.write(byteArrayOf((length ushr 8).toByte(), length.toByte()))
    byteWriter.write(payload)
}

/**
 * Builds a minimal JPEG with SOI, one minimal scan and no header
 * segments, used as the base image for rewriter tests. Every
 * entropy-coded byte stays below 0x80, so none can be mistaken for a
 * marker.
 */
internal fun bareJpeg(): ByteArray {

    val bytes = ByteArrayByteWriter()

    /* SOI */
bytes.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))

    /* SOS with minimal scan data. */
    bytes.write(byteArrayOf(0xFF.toByte(), 0xDA.toByte(), 0, 8, 1, 1, 0, 0, 63.toByte(), 0))
    bytes.write(byteArrayOf(0x11, 0x22, 0x33, 0x44))
    /* EOI */
bytes.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte()))

    return bytes.toByteArray()
}
