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
