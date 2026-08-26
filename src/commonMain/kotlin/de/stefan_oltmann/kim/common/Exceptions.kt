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

/**
 * Base exception for all image related failures.
 *
 * This is the only exception hierarchy that escapes the public Kim API,
 * so callers can catch [ImageException] or one of its subclasses to
 * handle every read and write failure.
 */
public open class ImageException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * Indicates that an image could not be read or parsed.
 */
public class ImageReadException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

/**
 * Indicates that an image could not be written.
 */
public open class ImageWriteException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

/**
 * We need to ensure that every Exception that can occur is wrapped
 * into an ImageReadException, because on Kotlin/Native this is the expected exception type.
 *
 * Fatal VM errors like [OutOfMemoryError] or [StackOverflowError] are
 * deliberately not wrapped: masking them as ordinary parse failures
 * would hide broken virtual machine state from the caller.
 */
internal inline fun <R> tryWithImageReadException(block: () -> R): R =
    try {
        block()
    } catch (ex: ImageReadException) {
        /* Don't wrap another ImageReadException. */
        throw ex
    } catch (ex: Exception) {
        throw ImageReadException("Failed to read image.", ex)
    }

/**
 * We need to ensure that everything is wrapped into an ImageWriteException,
 * because on Kotlin/Native this is the expected exception type.
 *
 * Fatal VM errors are deliberately not wrapped. See
 * [tryWithImageReadException].
 */
internal inline fun <R> tryWithImageWriteException(block: () -> R): R =
    try {
        block()
    } catch (ex: ImageWriteException) {
        /* Don't wrap another ImageWriteException. */
        throw ex
    } catch (ex: Exception) {
        throw ImageWriteException("Failed to write image.", ex)
    }
