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
package de.stefan_oltmann.kim.android

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.input.AndroidInputStreamByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.OutputStreamByteWriter
import java.io.File
import java.io.InputStream

/**
 * Extra object to have a nicer API for Java projects.
 *
 * Like [Kim], this API only throws [ImageReadException] and
 * [ImageWriteException] on failure.
 */
public object KimAndroid {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(inputStream: InputStream, length: Long): MediaMetadata? =
        Kim.readMetadata(
            byteReader = AndroidInputStreamByteReader(
                inputStream = inputStream,
                contentLength = length
            )
        )

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): MediaMetadata? =
        readMetadata(File(path))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(file: File): MediaMetadata? = tryWithImageReadException {

        if (!file.exists())
            throw ImageReadException("File does not exist: $file")

        return@tryWithImageReadException readMetadata(
            inputStream = file.inputStream().buffered(),
            length = file.length()
        )
    }

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        context: Context,
        uri: String,
        length: Long? = null
    ): MediaMetadata? =
        Kim.readMetadata(
            byteReader = createByteReader(
                contentResolver = context.contentResolver,
                uriString = uri,
                length = length
            )
        )

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        contentResolver: ContentResolver,
        uri: String,
        length: Long? = null
    ): MediaMetadata? =
        Kim.readMetadata(
            byteReader = createByteReader(
                contentResolver = contentResolver,
                uriString = uri,
                length = length
            )
        )

    @Throws(ImageReadException::class)
    public fun createByteReader(
        contentResolver: ContentResolver,
        uriString: String,
        length: Long? = null
    ): ByteReader =
        createByteReader(
            contentResolver = contentResolver,
            uri = Uri.parse(uriString),
            length = length
        )

    @Throws(ImageReadException::class)
    public fun createByteReader(
        contentResolver: ContentResolver,
        uri: Uri,
        length: Long? = null
    ): ByteReader = tryWithImageReadException {

        /*
         * On Android 10 (API 29) and above, we must use ContentResolver
         * due to Scoped Storage restrictions. For older versions, we can
         * directly access the file system using file paths.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            /*
             * If a length was provided we use that,
             * otherwise we receive it from the contentResolver.
             */
            val contentLength: Long? = length ?: contentResolver.getFileSize(uri)

            if (contentLength == null)
                throw ImageReadException("Unable to get file size for URI $uri")

            val inputStream = contentResolver.openInputStream(uri)

            if (inputStream == null)
                throw ImageReadException("Unable to open input stream for URI $uri")

            return@tryWithImageReadException AndroidInputStreamByteReader(inputStream, contentLength)
        }

        /*
         * Fall back to the old way
         */

        val pathname = uri.path

        if (pathname == null)
            throw ImageReadException("Unable to find path for URI $uri")

        val file = File(pathname)

        if (!file.exists())
            throw ImageReadException("File does not exist: $file")

        return@tryWithImageReadException AndroidInputStreamByteReader(
            inputStream = file.inputStream(),
            contentLength = length ?: file.length()
        )
    }

    @Throws(ImageWriteException::class)
    public fun createByteWriter(
        contentResolver: ContentResolver,
        uriString: String
    ): ByteWriter =
        createByteWriter(
            contentResolver = contentResolver,
            uri = Uri.parse(uriString)
        )

    @Throws(ImageWriteException::class)
    public fun createByteWriter(
        contentResolver: ContentResolver,
        uri: Uri
    ): ByteWriter = tryWithImageWriteException {

        /*
         * On Android 10 (API 29) and above, we must use ContentResolver
         * due to Scoped Storage restrictions. For older versions, we can
         * directly access the file system using file paths.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val outputStream = contentResolver.openOutputStream(uri)

            if (outputStream == null)
                throw ImageWriteException("Unable to open ouput stream for URI $uri")

            return@tryWithImageWriteException OutputStreamByteWriter(outputStream)
        }

        /*
         * Fall back to the old way
         */

        val pathname = uri.path

        if (pathname == null)
            throw ImageWriteException("Unable to find path for URI $uri")

        val file = File(pathname)

        if (!file.exists())
            throw ImageWriteException("File does not exist: $file")

        return@tryWithImageWriteException OutputStreamByteWriter(
            outputStream = file.outputStream()
        )
    }
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(inputStream: InputStream, length: Long): MediaMetadata? =
    KimAndroid.readMetadata(inputStream, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: String): MediaMetadata? =
    KimAndroid.readMetadata(path)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(file: File): MediaMetadata? =
    KimAndroid.readMetadata(file)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(
    context: Context,
    uri: String,
    length: Long? = null
): MediaMetadata? =
    KimAndroid.readMetadata(context, uri, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(
    contentResolver: ContentResolver,
    uri: String,
    length: Long? = null
): MediaMetadata? =
    KimAndroid.readMetadata(contentResolver, uri, length)
