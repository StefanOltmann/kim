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

import de.stefan_oltmann.kim.format.jpeg.JpegConstants.COM_MARKER_1
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.RST3_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests the transfer mode of [JpegMarkerScanner.transferToNextMarker].
 *
 * The mode is the delete walk's engine: it transfers every consumed gap
 * byte to a writer so the entropy coded data behind a scan header
 * survives byte-exact, while the marker that ends the run is returned
 * untransferred, so the caller decides its fate - exactly one marker
 * detection walk for the whole codebase.
 */
class JpegMarkerScannerTransferTest {

    /**
     * The gap bytes are transferred and the returned marker's own 2
     * bytes are not part of the gap: the caller writes them when it
     * keeps the marker, so a byte can never be written twice.
     */
    @Test
    fun testTransferWritesTheGapWithoutTheMarkerBytes() {

        val scanner = JpegMarkerScanner(
            ByteArrayByteReader(byteArrayOf(0x11, 0x22, 0xFF.toByte(), 0xFE.toByte(), 0x33)),
            keepConsumedBytes = false
        )

        val gapWriter = ByteArrayByteWriter()

        val scan = assertNotNull(
            scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true)
        )

        assertEquals(COM_MARKER_1, scan.marker)

        assertEquals(4, scan.consumedCount)

        assertContentEquals(
            expected = byteArrayOf(0x11, 0x22),
            actual = gapWriter.toByteArray(),
            message = "The marker bytes must not be part of the transferred gap."
        )

        /*
         * Behind the marker the walk continues: the byte that follows is
         * transferred and the stream end ends the scan without a marker.
         */
        assertNull(scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true))

        assertContentEquals(
            expected = byteArrayOf(0x11, 0x22, 0x33),
            actual = gapWriter.toByteArray()
        )
    }

    /**
     * The byte stuffing FF 00 and the fill byte runs inside the gap are
     * data and are transferred byte-exact, and the fill run in front of
     * a marker keeps its bytes while the marker's own FF is excluded.
     */
    @Test
    fun testTransferKeepsByteStuffingAndFillBytesInTheGap() {

        val scanner = JpegMarkerScanner(
            ByteArrayByteReader(
                byteArrayOf(
                    0x11,
                    0xFF.toByte(),
                    0x00,
                    0x22,
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xDA.toByte()
                )
            ),
            keepConsumedBytes = false
        )

        val gapWriter = ByteArrayByteWriter()

        val scan = assertNotNull(
            scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true)
        )

        assertEquals(SOS_MARKER, scan.marker)

        assertEquals(8, scan.consumedCount)

        assertContentEquals(
            expected = byteArrayOf(
                0x11,
                0xFF.toByte(),
                0x00,
                0x22,
                0xFF.toByte(),
                0xFF.toByte()
            ),
            actual = gapWriter.toByteArray(),
            message = "The stuffing and the fill run are data, the marker's FF is not."
        )
    }

    /**
     * A restart marker ends the run like every marker: its bytes are
     * returned untransferred, so the caller decides to keep them - the
     * delete walk writes them through as part of the entropy data.
     */
    @Test
    fun testTransferReturnsRestartMarkersUntransferred() {

        val scanner = JpegMarkerScanner(
            ByteArrayByteReader(byteArrayOf(0x11, 0xFF.toByte(), 0xD3.toByte(), 0x22)),
            keepConsumedBytes = false
        )

        val gapWriter = ByteArrayByteWriter()

        val scan = assertNotNull(
            scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true)
        )

        assertEquals(RST3_MARKER, scan.marker)

        assertContentEquals(
            expected = byteArrayOf(0x11),
            actual = gapWriter.toByteArray()
        )

        /* The caller keeps the marker by writing its bytes itself. */
        gapWriter.write(scan.markerBytes)

        assertNull(scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true))

        assertContentEquals(
            expected = byteArrayOf(0x11, 0xFF.toByte(), 0xD3.toByte(), 0x22),
            actual = gapWriter.toByteArray()
        )
    }

    /**
     * A stream that ends without a marker transfers every consumed byte
     * and returns NULL, so no byte of the gap is lost at the end.
     */
    @Test
    fun testTransferFlushesTheGapAtTheStreamEnd() {

        val scanner = JpegMarkerScanner(
            ByteArrayByteReader(byteArrayOf(0x11, 0x22, 0x33)),
            keepConsumedBytes = false
        )

        val gapWriter = ByteArrayByteWriter()

        assertNull(scanner.transferToNextMarker(gapWriter, zeroIsFillByte = true))

        assertContentEquals(
            expected = byteArrayOf(0x11, 0x22, 0x33),
            actual = gapWriter.toByteArray()
        )
    }

    /**
     * The zeroIsFillByte tolerance applies like in the retaining scan:
     * with the rewriter's tolerance a 0x00 behind a 0xFF is a marker
     * byte, so the pair is returned untransferred instead of being
     * written as stuffing.
     */
    @Test
    fun testTransferHonorsTheZeroIsFillByteTolerance() {

        val scanner = JpegMarkerScanner(
            ByteArrayByteReader(byteArrayOf(0x11, 0xFF.toByte(), 0x00, 0x22)),
            keepConsumedBytes = false
        )

        val gapWriter = ByteArrayByteWriter()

        val scan = assertNotNull(
            scanner.transferToNextMarker(gapWriter, zeroIsFillByte = false)
        )

        assertEquals(0xFF00, scan.marker)

        assertContentEquals(
            expected = byteArrayOf(0x11),
            actual = gapWriter.toByteArray()
        )
    }
}
