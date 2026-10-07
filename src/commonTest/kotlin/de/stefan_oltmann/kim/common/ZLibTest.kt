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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ZLibTest {

    /**
     * An empty payload is truncated zlib data, not empty output. Every
     * platform must reject it identically, so PNG text chunks cut
     * before their compressed payload fail the read everywhere instead
     * of parsing with an empty text on some targets only.
     */
    @Test
    fun testDecompressRejectsEmptyInput() {

        assertFailsWith<ImageReadException> {
            decompressBytes(ByteArray(0))
        }
    }

    /**
     * A payload shorter than the two-byte zlib header must fail with the
     * documented truncation error instead of an index-out-of-bounds from
     * the header inspection.
     */
    @Test
    fun testDecompressRejectsSingleBytePayload() {

        assertFailsWith<ImageReadException> {
            decompressBytes(byteArrayOf(0x78))
        }
    }

    @Test
    fun testDecompress() {

        for (entry in zlibTestData)
            assertEquals(entry.key, decompressBytes(entry.value).decodeToString())
    }

    /**
     * A payload far larger than the inflater's internal blocks must
     * decompress completely - the compressed golden was produced by the
     * reference zlib.
     */
    @Test
    fun testDecompressLargePayload() {

        val text = "A".repeat(100_000)

        assertEquals(text, decompressBytes(compressedA100K).decodeToString())
    }

    /**
     * The 6-byte period of the umlauts does not divide evenly into the
     * 4096-byte blocks some platform implementations use internally, so
     * multi-byte UTF-8 sequences split at block boundaries and must
     * still decode correctly.
     */
    @Test
    fun testDecompressMultibyteCharactersAcrossBlockBoundaries() {

        val text = "\u00E4\u00F6\u00FC".repeat(2048)

        assertEquals(text, decompressBytes(compressedUmlauts).decodeToString())
    }

    /**
     * Data that ends without a final block must be rejected instead of
     * silently returning the partial output.
     */
    @Test
    fun testDecompressRejectsTruncatedData() {

        /* Cutting the stream in half removes the final block. */
        val truncated =
            compressedA100K.copyOfRange(0, compressedA100K.size / 2)

        assertFailsWith<ImageReadException> {
            decompressBytes(truncated)
        }
    }

    /**
     * Corrupt data must surface as an [ImageReadException] on every
     * platform instead of a platform specific error type.
     */
    @Test
    fun testDecompressRejectsCorruptHeader() {

        /* The zlib header is broken, which every inflater rejects. */
        val corrupted = compressedFox.copyOf()

        corrupted[0] = 0x00

        assertFailsWith<ImageReadException> {
            decompressBytes(corrupted)
        }
    }

    /**
     * The inflater contract is zlib-wrapped streams only. pako
     * auto-detects gzip/raw-deflate unless windowBits is pinned, so a
     * gzip-framed payload must fail on every platform alike - a silently
     * succeeding JS/wasm read would diverge from JVM and native.
     */
    @Test
    fun testDecompressRejectsGzipFramedPayload() {

        /* gzip of "abc" (deflate), framed with the 1F 8B gzip header. */
        val gzipFramed = byteArrayOf(
            0x1F.toByte(), 0x8B.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03,
            0x4B.toByte(), 0x4C.toByte(), 0x4A.toByte(), 0x06, 0x00, 0xC2.toByte(), 0x41.toByte(),
            0x24.toByte(), 0x35.toByte(), 0x03, 0x00, 0x00, 0x00
        )

        assertFailsWith<ImageReadException> {
            decompressBytes(gzipFramed)
        }
    }

    /**
     * Concatenated zlib members are legal and must decode to the
     * concatenation of the member texts. An inflater that stops after the
     * first member would silently drop the rest.
     */
    @Test
    fun testDecompressConcatenatedMembers() {

        val joined = zlibTestData.getValue("Hello, World!") +
            zlibTestData.getValue("I love Kotlin!")

        assertEquals(
            expected = "Hello, World!" + "I love Kotlin!",
            actual = decompressBytes(joined).decodeToString()
        )
    }

    /**
     * Decompression must abort once the output exceeds the limit instead
     * of allocating unbounded memory for hostile input.
     */
    @Test
    fun testDecompressRejectsOutputBeyondTheLimit() {

        assertFailsWith<ImageReadException> {
            decompressBytes(compressedA100K, maxOutputByteCount = 1024)
        }
    }

    /**
     * Output below the limit must be decompressed completely, so the limit
     * does not corrupt legitimate payloads.
     */
    @Test
    fun testDecompressAllowsOutputBelowTheLimit() {

        assertEquals(
            expected = "The quick brown fox jumps over the lazy dog.",
            actual = decompressBytes(compressedFox, maxOutputByteCount = 4096)
                .decodeToString()
        )
    }

    private companion object {

        private val zlibTestData: Map<String, ByteArray> = mapOf(

            "Hello, World!" to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x48.toByte(), 0xcd.toByte(), 0xc9.toByte(),
                0xc9.toByte(), 0xd7.toByte(), 0x51.toByte(), 0x08.toByte(), 0xcf.toByte(), 0x2f.toByte(),
                0xca.toByte(), 0x49.toByte(), 0x51.toByte(), 0x04.toByte(), 0x00.toByte(), 0x1f.toByte(),
                0x9e.toByte(), 0x04.toByte(), 0x6a.toByte()
            ),
            "I love Kotlin!" to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x54.toByte(), 0xc8.toByte(), 0xc9.toByte(),
                0x2f.toByte(), 0x4b.toByte(), 0x55.toByte(), 0xf0.toByte(), 0xce.toByte(), 0x2f.toByte(),
                0xc9.toByte(), 0xc9.toByte(), 0xcc.toByte(), 0x53.toByte(), 0x04.toByte(), 0x00.toByte(),
                0x23.toByte(), 0x7d.toByte(), 0x04.toByte(), 0xd2.toByte()
            ),
            "The quick brown fox jumps over the lazy dog." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
                0x28.toByte(), 0x2c.toByte(), 0xcd.toByte(), 0x4c.toByte(), 0xce.toByte(), 0x56.toByte(),
                0x48.toByte(), 0x2a.toByte(), 0xca.toByte(), 0x2f.toByte(), 0xcf.toByte(), 0x53.toByte(),
                0x48.toByte(), 0xcb.toByte(), 0xaf.toByte(), 0x50.toByte(), 0xc8.toByte(), 0x2a.toByte(),
                0xcd.toByte(), 0x2d.toByte(), 0x28.toByte(), 0x56.toByte(), 0xc8.toByte(), 0x2f.toByte(),
                0x4b.toByte(), 0x2d.toByte(), 0x52.toByte(), 0x28.toByte(), 0x01.toByte(), 0x4a.toByte(),
                0xe7.toByte(), 0x24.toByte(), 0x56.toByte(), 0x55.toByte(), 0x2a.toByte(), 0xa4.toByte(),
                0xe4.toByte(), 0xa7.toByte(), 0xeb.toByte(), 0x01.toByte(), 0x00.toByte(), 0x6b.toByte(),
                0xe4.toByte(), 0x10.toByte(), 0x08.toByte()
            ),
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x05.toByte(), 0xc1.toByte(), 0x81.toByte(), 0x09.toByte(),
                0x40.toByte(), 0x21.toByte(), 0x08.toByte(), 0x05.toByte(), 0xc0.toByte(), 0x55.toByte(),
                0xde.toByte(), 0x00.toByte(), 0xd1.toByte(), 0x24.toByte(), 0x7f.toByte(), 0x89.toByte(),
                0x30.toByte(), 0x89.toByte(), 0x07.toByte(), 0x99.toByte(), 0xa1.toByte(), 0xb6.toByte(),
                0xFF.toByte(), 0xbf.toByte(), 0xfb.toByte(), 0x3c.toByte(), 0xd4.toByte(), 0xc0.toByte(),
                0x9b.toByte(), 0xcf.toByte(), 0x30.toByte(), 0x7d.toByte(), 0x7b.toByte(), 0x20.toByte(),
                0x59.toByte(), 0x18.toByte(), 0xa6.toByte(), 0xd5.toByte(), 0x20.toByte(), 0x7e.toByte(),
                0x52.toByte(), 0xa5.toByte(), 0xb4.toByte(), 0x5e.toByte(), 0x60.toByte(), 0x4c.toByte(),
                0x5e.toByte(), 0xa6.toByte(), 0xf0.toByte(), 0x2c.toByte(), 0xe8.toByte(), 0x66.toByte(),
                0xf5.toByte(), 0x1f.toByte(), 0x55.toByte(), 0x03.toByte(), 0x14.toByte(), 0xf7.toByte()
            ),
            "Compressing and decompressing data using zlib is efficient." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x73.toByte(), 0xce.toByte(), 0xcf.toByte(), 0x2d.toByte(),
                0x28.toByte(), 0x4a.toByte(), 0x2d.toByte(), 0x2e.toByte(), 0xce.toByte(), 0xcc.toByte(),
                0x4b.toByte(), 0x57.toByte(), 0x48.toByte(), 0xcc.toByte(), 0x4b.toByte(), 0x51.toByte(),
                0x48.toByte(), 0x49.toByte(), 0x4d.toByte(), 0x46.toByte(), 0x12.toByte(), 0x49.toByte(),
                0x49.toByte(), 0x2c.toByte(), 0x49.toByte(), 0x54.toByte(), 0x28.toByte(), 0x05.toByte(),
                0x33.toByte(), 0xab.toByte(), 0x72.toByte(), 0x32.toByte(), 0x93.toByte(), 0x14.toByte(),
                0x32.toByte(), 0x8b.toByte(), 0x15.toByte(), 0x52.toByte(), 0xd3.toByte(), 0xd2.toByte(),
                0x32.toByte(), 0x93.toByte(), 0x33.toByte(), 0x53.toByte(), 0xf3.toByte(), 0x4a.toByte(),
                0xf4.toByte(), 0x00.toByte(), 0xa5.toByte(), 0x28.toByte(), 0x16.toByte(), 0x39.toByte()
            ),
            "I love coding and exploring new technologies." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x54.toByte(), 0xc8.toByte(), 0xc9.toByte(),
                0x2f.toByte(), 0x4b.toByte(), 0x55.toByte(), 0x48.toByte(), 0xce.toByte(), 0x4f.toByte(),
                0xc9.toByte(), 0xcc.toByte(), 0x4b.toByte(), 0x57.toByte(), 0x48.toByte(), 0xcc.toByte(),
                0x4b.toByte(), 0x51.toByte(), 0x48.toByte(), 0xad.toByte(), 0x28.toByte(), 0xc8.toByte(),
                0xc9.toByte(), 0x2f.toByte(), 0x02.toByte(), 0xf1.toByte(), 0xf2.toByte(), 0x52.toByte(),
                0xcb.toByte(), 0x15.toByte(), 0x4a.toByte(), 0x52.toByte(), 0x93.toByte(), 0x33.toByte(),
                0xf2.toByte(), 0xf2.toByte(), 0x73.toByte(), 0xf2.toByte(), 0xd3.toByte(), 0x33.toByte(),
                0x53.toByte(), 0x8b.toByte(), 0xf5.toByte(), 0x00.toByte(), 0x77.toByte(), 0xd7.toByte(),
                0x10.toByte(), 0xbb.toByte()
            ),
            "The weather today is sunny and warm." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
                0x28.toByte(), 0x4f.toByte(), 0x4d.toByte(), 0x2c.toByte(), 0xc9.toByte(), 0x48.toByte(),
                0x2d.toByte(), 0x52.toByte(), 0x28.toByte(), 0xc9.toByte(), 0x4f.toByte(), 0x49.toByte(),
                0xac.toByte(), 0x54.toByte(), 0xc8.toByte(), 0x2c.toByte(), 0x56.toByte(), 0x28.toByte(),
                0x2e.toByte(), 0xcd.toByte(), 0xcb.toByte(), 0xab.toByte(), 0x54.toByte(), 0x48.toByte(),
                0xcc.toByte(), 0x4b.toByte(), 0x51.toByte(), 0x28.toByte(), 0x4f.toByte(), 0x2c.toByte(),
                0xca.toByte(), 0xd5.toByte(), 0x03.toByte(), 0x00.toByte(), 0xf5.toByte(), 0x2b.toByte(),
                0x0d.toByte(), 0x24.toByte()
            ),
            "This is a sample sentence for testing purposes." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0xc8.toByte(), 0x2c.toByte(),
                0x56.toByte(), 0x00.toByte(), 0xa2.toByte(), 0x44.toByte(), 0x85.toByte(), 0xe2.toByte(),
                0xc4.toByte(), 0xdc.toByte(), 0x82.toByte(), 0x9c.toByte(), 0x54.toByte(), 0x85.toByte(),
                0xe2.toByte(), 0xd4.toByte(), 0xbc.toByte(), 0x92.toByte(), 0xd4.toByte(), 0xbc.toByte(),
                0xe4.toByte(), 0x54.toByte(), 0x85.toByte(), 0xb4.toByte(), 0xfc.toByte(), 0x22.toByte(),
                0x85.toByte(), 0x92.toByte(), 0xd4.toByte(), 0xe2.toByte(), 0x92.toByte(), 0xcc.toByte(),
                0xbc.toByte(), 0x74.toByte(), 0x85.toByte(), 0x82.toByte(), 0xd2.toByte(), 0xa2.toByte(),
                0x82.toByte(), 0xfc.toByte(), 0xe2.toByte(), 0xd4.toByte(), 0x62.toByte(), 0x3d.toByte(),
                0x00.toByte(), 0x9a.toByte(), 0x72.toByte(), 0x11.toByte(), 0x81.toByte()
            ),
            "The cat in the hat." to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
                0x48.toByte(), 0x4e.toByte(), 0x2c.toByte(), 0x51.toByte(), 0xc8.toByte(), 0xcc.toByte(),
                0x53.toByte(), 0x28.toByte(), 0x01.toByte(), 0x32.toByte(), 0x33.toByte(), 0x12.toByte(),
                0x4b.toByte(), 0xf4.toByte(), 0x00.toByte(), 0x40.toByte(), 0x11.toByte(), 0x06.toByte(),
                0x5d.toByte()
            ),
            /*
             * Multi-byte UTF-8 characters. Decoding the decompressed bytes with
             * a non-UTF-8 platform charset (for example windows-1252) would
             * turn this into mojibake, so this golden pins UTF-8 decoding.
             */
            "Grüße aus Köln" to byteArrayOf(
                0x78.toByte(), 0x9c.toByte(), 0x73.toByte(), 0x2f.toByte(), 0x3a.toByte(), 0xbc.toByte(),
                0xe7.toByte(), 0xf0.toByte(), 0xfc.toByte(), 0x54.toByte(), 0x85.toByte(), 0xc4.toByte(),
                0xd2.toByte(), 0x62.toByte(), 0x05.toByte(), 0xef.toByte(), 0xc3.toByte(), 0xdb.toByte(),
                0x72.toByte(), 0xf2.toByte(), 0x00.toByte(), 0x4b.toByte(), 0x70.toByte(), 0x08.toByte(),
                0x27.toByte()
            )
        )

        private val compressedFox =
            zlibTestData.getValue("The quick brown fox jumps over the lazy dog.")

        /* Produced by the reference zlib. */
        private val compressedA100K = convertHexStringToByteArray(

            "789cedc13101000000c2a06ceb5fca1a1e40010000000000000000000000000000000000000000000000000000000000" +
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000af06e20c346e"
        )

        /* Produced by the reference zlib. */
        private val compressedUmlauts = convertHexStringToByteArray(
            "789cedc4210100300cc030ff3eae61e8acc666632001e935fd6cdbb66ddbb66ddbb66ddbb66ddbb66ddbf6e117289ef9ff"
        )
    }
}
