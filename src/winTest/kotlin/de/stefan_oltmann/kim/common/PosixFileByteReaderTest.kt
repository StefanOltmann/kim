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
package de.stefan_oltmann.kim.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.getenv
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The streaming file reader must deliver the same bytes as an in-memory
 * read and release its file handle on close, so the Apple facade can
 * read 100 MB RAW files without buffering them whole.
 */
class PosixFileByteReaderTest {

    /**
     * The reader reports the file size as the content length hint, reads
     * the complete content sequentially and releases the handle: a
     * second open and read of the same path succeeds.
     */
    @Test
    fun testReadsTheCompleteFileAndReleasesTheHandle() {

        val bytes = ByteArray(3 * READ_CHUNK_SIZE_BYTES + 17) { index -> (index % 251).toByte() }

        val filePath = writeTempFile(bytes)

        val reader = PosixFileByteReader(filePath)

        try {

            assertEquals(bytes.size.toLong(), reader.contentLength)

            assertContentEquals(bytes, reader.readBytes(bytes.size))

        } finally {
            reader.close()
        }

        val secondReader = PosixFileByteReader(filePath)

        try {

            /* The handle of the first reader was closed on Windows. */
            assertContentEquals(bytes, secondReader.readBytes(bytes.size))

        } finally {
            secondReader.close()
        }
    }

    /**
     * A read beyond the end of the data returns the available prefix as
     * a short array - never padded with zero bytes, per the ByteReader
     * end-of-data contract.
     */
    @Test
    fun testShortReadReturnsTheAvailablePrefix() {

        val bytes = byteArrayOf(1, 2, 3, 4, 5)

        val filePath = writeTempFile(bytes)

        val reader = PosixFileByteReader(filePath)

        try {

            assertContentEquals(bytes, reader.readBytes(BEYOND_END_READ_COUNT))

            /* At the end of the data the read is empty. */
            assertEquals(0, reader.readBytes(BEYOND_END_READ_COUNT).size)

        } finally {
            reader.close()
        }
    }

    /**
     * A read of a missing file fails with the concrete reason instead of
     * handing an unusable reader to the caller.
     */
    @Test
    fun testMissingFileFailsTheConstruction() {

        assertFailsWith<ImageReadException> {
            PosixFileByteReader("this-path-does-not-exist.kim")
        }
    }

    /**
     * Writes the bytes to a fresh file below the build directory and
     * returns its path.
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun writeTempFile(bytes: ByteArray): String {

        /*
         * The native test runner runs in its own working directory, so
         * the file goes to the platform temp location.
         */
        val tempBase = getenv("TEMP")?.toKString()
            ?: getenv("TMPDIR")?.toKString()
            ?: "."

        val filePath = tempBase + "/posix-file-byte-reader-test.bin"

        val file = fopen(filePath, "wb")
            ?: error("Cannot open test file: $filePath")

        try {

            val written = bytes.usePinned { pinned ->
                fwrite(
                    pinned.addressOf(0),
                    1.toULong(),
                    bytes.size.toULong(),
                    file
                )
            }

            if (written != bytes.size.toULong())
                error("Cannot write test file: $filePath")

        } finally {
            fclose(file)
        }

        return filePath
    }

    private companion object {

        const val READ_CHUNK_SIZE_BYTES: Int = 64 * 1024

        const val BEYOND_END_READ_COUNT: Int = 100
    }
}
