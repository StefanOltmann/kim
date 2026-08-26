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
package de.stefan_oltmann.kim.apple

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.readFileAsByteArray
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/**
 * Extra object to be aligned with the other modules.
 *
 * Like [Kim], this API only throws [ImageReadException] on failure.
 */
public object KimApple {

    @Throws(ImageReadException::class)
    public fun readMetadata(data: NSData): MediaMetadata? = tryWithImageReadException {

        return@tryWithImageReadException Kim.readMetadata(
            ByteArrayByteReader(convertDataToByteArray(data))
        )
    }

    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): MediaMetadata? = tryWithImageReadException {

        val fileBytes = readFileAsByteArray(path) ?: return@tryWithImageReadException null

        return@tryWithImageReadException Kim.readMetadata(ByteArrayByteReader(fileBytes))
    }
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(data: NSData): MediaMetadata? =
    KimApple.readMetadata(data)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: String): MediaMetadata? =
    KimApple.readMetadata(path)

@OptIn(ExperimentalForeignApi::class)
private fun convertDataToByteArray(data: NSData): ByteArray {

    val length = data.length

    if (length == 0UL)
        return ByteArray(0)

    if (length > Int.MAX_VALUE.toULong())
        throw ImageReadException("NSData length $length exceeds Int.MAX_VALUE")

    val src = checkNotNull(data.bytes) {
        "NSData bytes is null for length $length"
    }

    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(
                __dst = it.addressOf(0),
                __src = src,
                __n = length
            )
        }
    }
}
