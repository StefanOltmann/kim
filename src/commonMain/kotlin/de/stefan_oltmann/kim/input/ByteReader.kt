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
package de.stefan_oltmann.kim.input

/**
 * Reads bytes from a media file.
 *
 * # End-of-data contract
 *
 * The reading functions report the end of the data in three ways, each
 * suited to its use:
 *
 * - [readByte] returns NULL.
 * - [readBytes] returns a short array (possibly empty), never padded
 *   with zero bytes, because zero would be parsed as data.
 * - The field based extensions in `ByteReaderExtensions` throw an
 *   [de.stefan_oltmann.kim.common.ImageReadException] when a requested
 *   structure cannot be read completely.
 *
 * A fourth form exists in `ByteReaderExtensions`: [readByteAsInt]
 * returns the `-1` sentinel at the end of the data, so every caller
 * must check for it explicitly.
 */
public interface ByteReader : AutoCloseable {

    public val contentLength: Long

    /**
     * Whether this reader retains the bytes it reads for its own later
     * use. Payload-handling strategies can consult this to avoid
     * buffering the same bytes a second time.
     */
    public val isRetaining: Boolean
        get() = false

    /**
     * Returns the next Byte, if any.
     */
    public fun readByte(): Byte?

    public fun readBytes(count: Int): ByteArray

}
