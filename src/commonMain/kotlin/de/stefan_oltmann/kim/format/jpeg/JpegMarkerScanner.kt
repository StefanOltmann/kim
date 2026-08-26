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

import de.stefan_oltmann.kim.input.ByteReader

/**
 * Scans for JPEG markers, encapsulating the fill-byte walk that all JPEG
 * segment readers share. Centralizing this walk removes the drift risk of
 * five previously independent copies of the most off-by-one-prone loop of
 * the format.
 */
internal class JpegMarkerScanner(
    private val byteReader: ByteReader
) {

    /**
     * Consumes bytes up to and including the next marker.
     *
     * Returns NULL when the stream ends before a marker was found.
     *
     * @param zeroIsFillByte Whether a 0x00 behind a 0xFF is treated as more
     * fill (the spec-conformant reading used by all metadata scanners) or as
     * the second byte of a marker (the legacy tolerance of the rewriter).
     */
    fun nextMarker(zeroIsFillByte: Boolean): Scan? {

        var previous = byteReader.readByte() ?: return null

        val consumed = mutableListOf(previous)

        while (true) {

            val current = byteReader.readByte() ?: return null

            consumed.add(current)

            val isMarker =
                previous == FILL_BYTE &&
                    current != FILL_BYTE &&
                    !(zeroIsFillByte && current == ZERO_BYTE)

            if (isMarker)
                return Scan(
                    marker = (previous.toInt() and 0xFF) shl 8 or (current.toInt() and 0xFF),
                    markerBytes = byteArrayOf(previous, current),
                    consumedBytes = consumed.toByteArray()
                )

            previous = current
        }
    }

    /**
     * A found marker and every byte that was consumed to reach it.
     */
    class Scan(
        val marker: Int,
        val markerBytes: ByteArray,
        val consumedBytes: ByteArray
    )

    private companion object {

        private const val FILL_BYTE: Byte = 0xFF.toByte()

        private const val ZERO_BYTE: Byte = 0
    }
}
