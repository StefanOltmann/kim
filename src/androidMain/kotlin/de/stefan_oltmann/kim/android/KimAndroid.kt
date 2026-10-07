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

    /**
     * Reads the metadata from a stream.
     *
     * Attention: The stream IS closed by this call, including the stream
     * below it, and must not be used afterwards - like the core [Kim]
     * API documents. The [length] is only a hint and may be 0 for
     * unknown sizes.
     */
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
    ): ByteReader {

        /*
         * The ContentResolver handles content and file URIs on every API
         * level. The old file-path fallback only worked for URIs whose
         * path happens to be a real filesystem path - MediaStore and SAF
         * URIs failed on older devices.
         *
         * The length is resolved before the stream opens, so a failing
         * provider query cannot leak an already-opened stream - the same
         * invariant KimJvm.readMetadataFrom documents.
         */
        return createByteReaderFrom(
            sizeLookup = {
                length ?: (contentResolver.getFileSize(uri) ?: 0L)
            },
            openStream = {
                contentResolver.openInputStream(uri)
                    ?: throw ImageReadException("Unable to open input stream for URI $uri")
            }
        )
    }

    /**
     * Assembles the byte reader with the length resolved first: a
     * failing size lookup must not leak an already-opened stream.
     * Internal for the host tests, which pin the ordering.
     */
    internal fun createByteReaderFrom(
        sizeLookup: () -> Long,
        openStream: () -> InputStream
    ): ByteReader = tryWithImageReadException {

        val length = sizeLookup()

        AndroidInputStreamByteReader(
            inputStream = openStream().buffered(),
            contentLength = length
        )
    }

    /**
     * Opens a writer for the given URI that [Kim.update] can stream into.
     *
     * Attention: Opening the underlying stream already truncates the
     * target. If the subsequent update fails midway (for example because
     * the source image is truncated), the original photo is left
     * truncated as well. Production apps should stream the rewrite into
     * a staging file first, verify it, and only then write it here.
     *
     * The caller is responsible for closing the returned writer.
     */
    @Throws(ImageWriteException::class)
    public fun createByteWriter(
        contentResolver: ContentResolver,
        uriString: String
    ): ByteWriter =
        createByteWriter(
            contentResolver = contentResolver,
            uri = Uri.parse(uriString)
        )

    /**
     * Opens a writer for the given URI that [Kim.update] can stream into.
     *
     * Attention: See the warning in the sibling overload above; opening
     * the stream truncates the target before any byte was written.
     *
     * The caller is responsible for closing the returned writer.
     */
    @Throws(ImageWriteException::class)
    public fun createByteWriter(
        contentResolver: ContentResolver,
        uri: Uri
    ): ByteWriter = tryWithImageWriteException {

        /*
         * The ContentResolver handles content and file URIs on every API
         * level. The old file-path fallback only worked for URIs whose
         * path happens to be a real filesystem path - MediaStore and SAF
         * URIs failed on older devices.
         */

        val outputStream = contentResolver.openOutputStream(uri, "wt")
            ?: throw ImageWriteException("Unable to open ouput stream for URI $uri")

        return@tryWithImageWriteException OutputStreamByteWriter(outputStream)
    }
}

/**
 * Reads the metadata from a stream. The stream is read but NOT closed -
 * closing it stays the caller's responsibility. The [length] is only a
 * hint and may be 0 for unknown sizes.
 */
@Throws(ImageReadException::class)
@Suppress("UnusedReceiverParameter")
/* False positive. */
public fun Kim.readMetadata(inputStream: InputStream, length: Long): MediaMetadata? =
    KimAndroid.readMetadata(inputStream, length)

@Throws(ImageReadException::class)
@Suppress("UnusedReceiverParameter")
/* False positive. */
public fun Kim.readMetadata(path: String): MediaMetadata? =
    KimAndroid.readMetadata(path)

@Throws(ImageReadException::class)
@Suppress("UnusedReceiverParameter")
/* False positive. */
public fun Kim.readMetadata(file: File): MediaMetadata? =
    KimAndroid.readMetadata(file)

@Throws(ImageReadException::class)
@Suppress("UnusedReceiverParameter")
/* False positive. */
public fun Kim.readMetadata(
    context: Context,
    uri: String,
    length: Long? = null
): MediaMetadata? =
    KimAndroid.readMetadata(context, uri, length)

@Throws(ImageReadException::class)
@Suppress("UnusedReceiverParameter")
/* False positive. */
public fun Kim.readMetadata(
    contentResolver: ContentResolver,
    uri: String,
    length: Long? = null
): MediaMetadata? =
    KimAndroid.readMetadata(contentResolver, uri, length)
