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
import de.stefan_oltmann.kim.input.readRemainingBytes
import de.stefan_oltmann.kim.model.MetadataUpdate
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

    /**
     * Updates the image at the given URI with a single change.
     *
     * The image data is read into memory first and only written back
     * after all updates were applied successfully, so a parsing failure
     * cannot destroy the original photo. This requires the image data to
     * fit into memory.
     *
     * Attention: A failure while writing the new content (for example a
     * full disk) can still leave a truncated file, because
     * [ContentResolver.openOutputStream] truncates the target on open
     * and does not support atomic replacement.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        contentResolver: ContentResolver,
        uri: String,
        update: MetadataUpdate
    ): Unit = update(contentResolver, uri, setOf(update))

    /**
     * Updates the image at the given URI with all desired changes at once.
     *
     * The image data is read into memory first and only written back
     * after all updates were applied successfully, so a parsing failure
     * cannot destroy the original photo. This requires the image data to
     * fit into memory.
     *
     * Attention: A failure while writing the new content (for example a
     * full disk) can still leave a truncated file, because
     * [ContentResolver.openOutputStream] truncates the target on open
     * and does not support atomic replacement.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        contentResolver: ContentResolver,
        uri: String,
        updates: Set<MetadataUpdate>
    ): Unit = update(contentResolver, Uri.parse(uri), updates)

    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        contentResolver: ContentResolver,
        uri: Uri,
        updates: Set<MetadataUpdate>
    ): Unit = tryWithImageWriteException {

        /*
         * The original is not touched until the complete new content has
         * been computed in memory, so parsing or conversion failures can
         * never destroy it.
         */
        val updatedBytes =
            Kim.update(bytes = readBytes(contentResolver, uri), updates = updates)

        writeBytes(contentResolver, uri, updatedBytes)
    }

    /**
     * Removes all metadata of the image at the given URI, keeping the ICC
     * profile chunks that affect how the image is displayed.
     *
     * The image data is read into memory first and only written back
     * after the removal was prepared successfully, so a parsing failure
     * cannot destroy the original photo. This requires the image data to
     * fit into memory.
     *
     * Attention: A failure while writing the new content (for example a
     * full disk) can still leave a truncated file, because
     * [ContentResolver.openOutputStream] truncates the target on open
     * and does not support atomic replacement.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        contentResolver: ContentResolver,
        uri: String
    ): Unit = deleteMetadata(contentResolver, Uri.parse(uri))

    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        contentResolver: ContentResolver,
        uri: Uri
    ): Unit = tryWithImageWriteException {

        /*
         * The original is not touched until the complete new content has
         * been computed in memory, so parsing failures can never destroy
         * it.
         */
        val cleanedBytes =
            Kim.deleteMetadata(bytes = readBytes(contentResolver, uri))

        writeBytes(contentResolver, uri, cleanedBytes)
    }

    /**
     * Updates the image file with a single change.
     *
     * The new content is written to a temporary file next to the target
     * and moved onto it after the update was applied successfully, so a
     * parsing failure cannot destroy the original photo.
     *
     * This requires the image data to fit into memory.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        file: File,
        update: MetadataUpdate
    ): Unit = update(file, setOf(update))

    /**
     * Updates the image file with all desired changes at once.
     *
     * The new content is written to a temporary file next to the target
     * and moved onto it after the update was applied successfully, so a
     * parsing failure cannot destroy the original photo.
     *
     * This requires the image data to fit into memory.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun update(
        file: File,
        updates: Set<MetadataUpdate>
    ): Unit = tryWithImageWriteException {

        /*
         * The original is not touched until the complete new content has
         * been computed in memory and placed into the temporary file, so
         * failures can never destroy it.
         */
        val updatedBytes = Kim.update(bytes = file.readBytes(), updates = updates)

        atomicReplace(file, updatedBytes)
    }

    /**
     * Removes all metadata of the image file, keeping the ICC profile
     * chunks that affect how the image is displayed.
     *
     * The new content is written to a temporary file next to the target
     * and moved onto it after the removal was prepared successfully, so a
     * parsing failure cannot destroy the original photo.
     *
     * This requires the image data to fit into memory.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(file: File): Unit = tryWithImageWriteException {

        val cleanedBytes = Kim.deleteMetadata(bytes = file.readBytes())

        atomicReplace(file, cleanedBytes)
    }

    /**
     * Replaces the content of the target file with the given bytes by
     * writing them to a temporary sibling first and moving that onto the
     * target afterwards. The original stays intact when anything fails.
     *
     * Attention: This uses java.io deliberately. java.nio.file.Files is
     * only available from API 26, but this library supports API 21.
     */
    private fun atomicReplace(target: File, bytes: ByteArray) {

        val parentDirectory =
            target.parentFile ?: throw ImageWriteException("Target has no parent directory: $target")

        val tempFile = File.createTempFile(target.name, ".tmp", parentDirectory)

        try {

            tempFile.writeBytes(bytes)

            /*
             * On Linux rename(2), which backs File.renameTo on Android,
             * replaces an existing target atomically. The delete fallback
             * covers file systems where the rename cannot overwrite.
             */
            if (!tempFile.renameTo(target)) {

                target.delete()

                if (!tempFile.renameTo(target))
                    throw ImageWriteException("Could not replace $target with the new content.")
            }

        } finally {
            tempFile.delete()
        }
    }

    /**
     * Reads the complete image behind the URI into memory.
     */
    private fun readBytes(
        contentResolver: ContentResolver,
        uri: Uri
    ): ByteArray =
        tryWithImageReadException {
            createByteReader(contentResolver, uri).use { reader ->
                reader.readRemainingBytes()
            }
        }

    /**
     * Replaces the content behind the URI with the given bytes.
     */
    private fun writeBytes(
        contentResolver: ContentResolver,
        uri: Uri,
        bytes: ByteArray
    ): Unit = tryWithImageWriteException {

        val outputStream = contentResolver.openOutputStream(uri)
            ?: throw ImageWriteException("Unable to open output stream for URI $uri")

        outputStream.use {
            it.write(bytes)
            it.flush()
        }
    }

    /**
     * Opens a writer for the given URI that [Kim.update] can stream into.
     *
     * Attention: Opening the underlying stream already truncates the
     * target. If the subsequent update fails midway (for example because
     * the source image is truncated), the original photo is left
     * truncated as well. Prefer [update] or [deleteMetadata], which
     * compute the new content first, unless streaming very large files
     * is worth that risk.
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
