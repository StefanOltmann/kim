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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteWriter

/**
 * Scans for JPEG markers, encapsulating the fill-byte walk that all JPEG
 * segment readers share. Centralizing this walk removes the drift risk of
 * five previously independent copies of the most off-by-one-prone loop of
 * the format.
 */
internal class JpegMarkerScanner(
    private val byteReader: ByteReader,
    private val keepConsumedBytes: Boolean = true,
    private val maxRetainedBytes: Int = Int.MAX_VALUE
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

        var consumedCount = 1

        val consumed = mutableListOf(previous)

        while (true) {

            val current = byteReader.readByte() ?: return null

            /*
             * Callers that only need the count must not accumulate an
             * unbounded gap of junk bytes (e.g. a truncated file without
             * any further marker). Callers that retain the bytes bound
             * the retention, because a gap beyond the bound is hostile
             * input the caller's budget would reject only after the
             * whole gap was buffered.
             */
            if (keepConsumedBytes) {

                if (consumedCount > maxRetainedBytes)
                    throw ImageReadException(
                        "The JPEG marker scan retains more than " +
                            "$maxRetainedBytes bytes without finding a marker."
                    )

                consumed.add(current)
            }

            consumedCount++

            val isMarker =
                previous == FILL_BYTE &&
                    current != FILL_BYTE &&
                    !(zeroIsFillByte && current == ZERO_BYTE)

            if (isMarker) {

                val marker = (previous.toInt() and 0xFF) shl 8 or (current.toInt() and 0xFF)

                val markerBytes = byteArrayOf(previous, current)

                val consumedBytes =
                    if (keepConsumedBytes) consumed.toByteArray() else ByteArray(0)

                return Scan(
                    marker = marker,
                    markerBytes = markerBytes,
                    consumedBytes = consumedBytes,
                    consumedCount = consumedCount
                )
            }

            previous = current
        }
    }

    /**
     * Consumes bytes up to the next marker and transfers every byte that
     * is not part of the marker to [gapWriter] in bounded chunks, so a
     * caller can stream the gap - megabytes of entropy coded data
     * included - without buffering it as a whole.
     *
     * The marker's own 2 bytes are returned in the [Scan] untransferred:
     * the caller decides their fate, writes them for the markers it
     * keeps, and skips them for the markers it drops. The [Scan]'s
     * [Scan.consumedBytes] is always empty, because the gap went to the
     * writer. NULL is returned when the stream ends without a marker,
     * after every consumed byte was transferred.
     *
     * The detection and the zeroIsFillByte tolerance are the ones of
     * [nextMarker], so both walks cannot drift apart.
     *
     * @param gapWriter The writer that receives every gap byte that is
     * not part of the returned marker.
     * @param zeroIsFillByte Whether a 0x00 behind a 0xFF is treated as more
     * fill (the spec-conformant reading used by all metadata scanners) or as
     * the second byte of a marker (the legacy tolerance of the rewriter).
     */
    fun transferToNextMarker(gapWriter: ByteWriter, zeroIsFillByte: Boolean): Scan? {

        val gapBytes = ByteArray(DEFAULT_BUFFER_SIZE)

        var gapCount = 0

        fun flushGapBytes() {

            if (gapCount > 0) {
                gapWriter.write(gapBytes.copyOf(gapCount))
                gapCount = 0
            }
        }

        fun appendGapByte(byte: Byte) {

            if (gapCount == gapBytes.size)
                flushGapBytes()

            gapBytes[gapCount] = byte

            gapCount++
        }

        var previous = byteReader.readByte() ?: return null

        var consumedCount = 1

        while (true) {

            val current = byteReader.readByte()
                ?: run {
                    /* The stream end without a marker leaves every read byte in the gap. */
                    flushGapBytes()
                    appendGapByte(previous)
                    flushGapBytes()
                    return null
                }

            consumedCount++

            val isMarker =
                previous == FILL_BYTE &&
                    current != FILL_BYTE &&
                    !(zeroIsFillByte && current == ZERO_BYTE)

            if (isMarker) {

                flushGapBytes()

                val marker =
                    (previous.toInt() and 0xFF) shl 8 or (current.toInt() and 0xFF)

                return Scan(
                    marker = marker,
                    markerBytes = byteArrayOf(previous, current),
                    consumedBytes = ByteArray(0),
                    consumedCount = consumedCount
                )
            }

            /*
             * The previous byte is only a marker candidate: it is written
             * as gap data here, one iteration behind the read position,
             * so the marker's own first byte is never written.
             */
            appendGapByte(previous)

            previous = current
        }
    }

    /**
     * A found marker and every byte that was consumed to reach it.
     */
    class Scan(
        val marker: Int,
        val markerBytes: ByteArray,
        val consumedBytes: ByteArray,
        val consumedCount: Int
    )

    private companion object {

        private const val FILL_BYTE: Byte = 0xFF.toByte()

        private const val ZERO_BYTE: Byte = 0
    }
}
