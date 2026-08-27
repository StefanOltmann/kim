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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.perror
import platform.posix.rewind

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal fun readFileAsByteArray(filePath: String): ByteArray? = memScoped {

    /* Note: Mode "rb" is for reading binary files. */
    val file: CPointer<FILE>? = fopen(filePath, "rb")

    if (file == null) {
        perror("Failed to open file: $filePath")
        return@readFileAsByteArray null
    }

    try {

        /* Move the cursor to the end of the file to determine its size. */
        if (fseek(file, 0, SEEK_END) != 0) {
            perror("Failed to seek to the end of file: $filePath")
            return null
        }

        val fileSize = ftell(file)

        /*
         * ftell reports failures as -1 (for example unseekable streams),
         * and files larger than the array index range cannot be read into
         * a single array anyway.
         */
        if (fileSize < 0L || fileSize > Int.MAX_VALUE.toLong()) {
            perror("File is unseekable or too large: $filePath ($fileSize bytes)")
            return@readFileAsByteArray null
        }

        rewind(file)

        val byteCount = fileSize.convert<Int>()

        val buffer = ByteArray(byteCount)

        val bytesReadCount: ULong = fread(
            buffer.refTo(0),
            1.toULong(), // Number of items
            fileSize.toULong(), // Size to read
            file
        )

        if (bytesReadCount != fileSize.toULong()) {
            perror("Did not read file completely: $bytesReadCount != $fileSize")
            return@readFileAsByteArray null
        }

        return@readFileAsByteArray buffer

    } finally {
        fclose(file)
    }
}
