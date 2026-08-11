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
package de.stefan_oltmann.kim.jvm

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Extra object to have a nicer API for Java projects.
 *
 * Like [Kim], this API only throws [ImageReadException] on failure.
 */
public object KimJvm {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(inputStream: InputStream, length: Long): MediaMetadata? =
        Kim.readMetadata(JvmInputStreamByteReader(inputStream, length))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): MediaMetadata? =
        readMetadata(File(path))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(file: File): MediaMetadata? = tryWithImageReadException {

        if (!file.exists())
            throw ImageReadException("File does not exist: $file")

        return@tryWithImageReadException readMetadata(file.inputStream().buffered(), file.length())
    }

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: java.nio.file.Path): MediaMetadata? = tryWithImageReadException {

        if (!Files.exists(path))
            throw ImageReadException("File does not exist: $path")

        return@tryWithImageReadException readMetadata(
            inputStream = Files.newInputStream(path, StandardOpenOption.READ).buffered(),
            length = Files.size(path)
        )
    }
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(inputStream: InputStream, length: Long): MediaMetadata? =
    KimJvm.readMetadata(inputStream, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: String): MediaMetadata? =
    KimJvm.readMetadata(path)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(file: File): MediaMetadata? =
    KimJvm.readMetadata(file)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: java.nio.file.Path): MediaMetadata? =
    KimJvm.readMetadata(path)
