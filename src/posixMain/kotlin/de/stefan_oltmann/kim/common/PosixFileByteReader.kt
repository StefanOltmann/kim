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

import de.stefan_oltmann.kim.input.ByteReader
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind

/*
 * TIFF-family RAW files routinely reach hundreds of megabytes, so the
 * file readers stream in bounded chunks instead of buffering the file
 * whole - like the JVM and Android facades.
 */
private const val READ_CHUNK_SIZE_BYTES: Int = 64 * 1024

/**
 * Reads a file sequentially through the POSIX stdio API in bounded
 * chunks, so callers stream large files instead of buffering them whole
 * (see [readFileAsByteArray] for the byte-array variant).
 *
 * The [contentLength] hint comes from the file size, so the size is
 * exact for regular files. Every read is decided by the real end of
 * data: a read beyond it returns a short array per the ByteReader
 * end-of-data contract.
 *
 * The reader must be closed, which releases the file handle.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class PosixFileByteReader(
    private val filePath: String
) : ByteReader {

    private val file: CPointer<FILE> = fopen(filePath, "rb")
        ?: throw ImageReadException(
            "Failed to open file: $filePath (${posixErrorMessage()})"
        )

    override val contentLength: Long

    init {

        /* Move the cursor to the end of the file to determine its size. */
        if (fseek(file, 0, SEEK_END) != 0) {

            fclose(file)

            throw ImageReadException(
                "Failed to seek to the end of file: $filePath (${posixErrorMessage()})"
            )
        }

        val fileSize = ftell(file)

        /*
         * ftell reports failures as negative (for example unseekable
         * streams); the long width differs per platform (32-bit on
         * Windows), so files beyond that range fail the check as well.
         */
        if (fileSize < 0) {

            fclose(file)

            throw ImageReadException(
                "File is unseekable: $filePath (${posixErrorMessage()})"
            )
        }

        rewind(file)

        contentLength = fileSize.convert<Long>()
    }

    override fun readByte(): Byte? {

        val buffer = ByteArray(1)

        val bytesReadCount: ULong = fread(
            buffer.refTo(0),
            1.toULong(),
            1.toULong(),
            file
        )

        return if (bytesReadCount == 1.toULong()) buffer[0] else null
    }

    override fun readBytes(count: Int): ByteArray {

        if (count < 0)
            throw ImageReadException(
                "Cannot read a negative number of bytes: $count (file $filePath)"
            )

        val result = ByteArray(count)

        var totalRead = 0

        while (totalRead < count) {

            val chunkSize = minOf(count - totalRead, READ_CHUNK_SIZE_BYTES)

            val bytesReadCount: ULong = fread(
                result.refTo(totalRead),
                1.toULong(),
                chunkSize.toULong(),
                file
            )

            /* The real end of the data was reached. */
            if (bytesReadCount == 0.toULong())
                break

            totalRead += bytesReadCount.convert<Int>()
        }

        /* A short array reports the end of the data, never zero padding. */
        return if (totalRead == count) result else result.copyOf(totalRead)
    }

    override fun close() {
        fclose(file)
    }
}
