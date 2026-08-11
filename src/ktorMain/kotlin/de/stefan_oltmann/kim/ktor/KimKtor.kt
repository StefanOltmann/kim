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
package de.stefan_oltmann.kim.ktor

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.input.KotlinIoSourceByteReader
import de.stefan_oltmann.kim.input.KtorByteReadChannelByteReader
import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.Source
import kotlin.jvm.JvmStatic

/**
 * Extra object to have a nicer API for Java projects.
 */
public object KimKtor {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(source: Source, contentLength: Long): MediaMetadata? =
        Kim.readMetadata(KotlinIoSourceByteReader(source, contentLength))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(byteReadChannel: ByteReadChannel, contentLength: Long): MediaMetadata? =
        Kim.readMetadata(KtorByteReadChannelByteReader(byteReadChannel, contentLength))
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(source: Source, contentLength: Long): MediaMetadata? =
    KimKtor.readMetadata(source, contentLength)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(byteReadChannel: ByteReadChannel, contentLength: Long): MediaMetadata? =
    KimKtor.readMetadata(byteReadChannel, contentLength)
