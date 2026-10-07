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
package de.stefan_oltmann.kim.format.printim

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.decodeStrictUtf8
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.tiff.TiffContents
import kotlin.jvm.JvmStatic
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toUInt8

/**
 * Parses the Print Image Matching block: "PrintIM\0", the 4-character
 * version, two reserved bytes and the little-endian entry count,
 * followed by fixed 6-byte entries of a uint16 tag and an int32 value.
 */
public object PrintImParser {

    private val SIGNATURE: ByteArray = byteArrayOf(
        0x50, 0x72, 0x69, 0x6E, 0x74, 0x49, 0x4D, 0x00
    )

    private const val VERSION_OFFSET = 8

    private const val VERSION_SIZE = 4

    private const val COUNT_OFFSET = 14

    private const val ENTRY_OFFSET = 16

    private const val ENTRY_SIZE = 6

    private const val MAX_ENTRY_COUNT = 4096

    private const val BYTE_SHIFT_8 = 8

    private const val BYTE_SHIFT_16 = 16

    private const val BYTE_SHIFT_24 = 24

    /** The EXIF tag whose undefined value carries the PrintIM block. */
    public const val PRINTIM_TAG: Int = 0xC4A5

    @JvmStatic
    public fun parse(bytes: ByteArray): PrintImDirectory =

        tryWithImageReadException {

            if (!bytes.startsWith(SIGNATURE))
                throw ImageReadException("The PrintIM block lacks the 'PrintIM' signature.")

            if (bytes.size < ENTRY_OFFSET)
                throw ImageReadException(
                    "The PrintIM block is ${bytes.size} bytes, smaller than its header."
                )

            val entryCount =
                bytes[COUNT_OFFSET].toUInt8() or
                    (bytes[COUNT_OFFSET + 1].toUInt8() shl BYTE_SHIFT_8)

            if (entryCount > MAX_ENTRY_COUNT ||
                ENTRY_OFFSET + entryCount * ENTRY_SIZE > bytes.size
            )
                throw ImageReadException(
                    "The PrintIM block declares an invalid entry count of $entryCount."
                )

            PrintImDirectory(
                version = bytes.slice(
                    VERSION_OFFSET,
                    VERSION_SIZE
                ).decodeStrictUtf8("The PrintIM version"),
                entries = (0 until entryCount).map { index ->

                    val entryOffset = ENTRY_OFFSET + index * ENTRY_SIZE

                    PrintImEntry(
                        tag = bytes[entryOffset].toUInt8() or
                            (bytes[entryOffset + 1].toUInt8() shl BYTE_SHIFT_8),
                        value = readInt32(bytes, entryOffset + 2)
                    )
                }
            )
        }

    /**
     * Parses the PrintIM block from the EXIF tag 0xC4A5 of the given
     * contents, or NULL when the file carries none.
     */
    public fun parseFrom(contents: TiffContents): PrintImDirectory? =

        contents.directories
            .asSequence()
            .flatMap { directory -> directory.entries.asSequence() }
            .firstOrNull { field -> field.tag == PRINTIM_TAG }
            ?.let { field -> parse(field.valueBytes) }

    @Suppress("MagicNumber")
    private fun readInt32(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toUInt8()) or
            (bytes[offset + 1].toUInt8() shl BYTE_SHIFT_8) or
            (bytes[offset + 2].toUInt8() shl BYTE_SHIFT_16) or
            (bytes[offset + 3].toUInt8() shl BYTE_SHIFT_24)
}
