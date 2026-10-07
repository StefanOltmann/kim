/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes

/**
 * ISO/IEC 14496-12 hdlr box.
 */
public class HandlerReferenceBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.HDLR, offset, size, largeSize, payload) {

    /** The box version. */
    public val version: Int

    /** The box flags. */
    public val flags: ByteArray

    /** The four-character kind of the track, like "vide" for video or "pict" for images. */
    public val handlerType: String

    /** A free-text name, often empty in real files. */
    public val name: String

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        byteReader.skipBytes("pre-defined", PRE_DEFINED_LENGTH)

        handlerType = byteReader.readBytes("handlerType", HANDLER_TYPE_LENGTH).decodeToString()

        byteReader.skipBytes("reserved", RESERVED_LENGTH)

        /*
         * ISO/IEC 14496-12 writes the name NUL-terminated, QuickTime
         * writes a Pascal string: one length byte plus that many bytes,
         * without a terminator - the form ffmpeg writes for MOV files.
         * A NUL byte decides; only a field without one whose first byte
         * matches the exact remaining count is read as a Pascal string.
         * The length byte is unsigned, so the high bit must not turn it
         * into a negative number for names longer than 127 bytes.
         */
        val nameField = payload.copyOfRange(NAME_FIELD_OFFSET, payload.size)

        val terminatorIndex = nameField.indexOf(0)

        name =
            when {
                terminatorIndex >= 0 ->
                    nameField.decodeToString(0, terminatorIndex)

                nameField.isNotEmpty() &&
                    (nameField[0].toInt() and UNSIGNED_BYTE_MASK) == nameField.size - 1 ->
                    nameField.decodeToString(1, nameField.size)

                else ->
                    throw ImageReadException("No bytes for name, never reached terminator byte.")
            }
    }

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "handlerType=$handlerType " +
            "name=$name"

    private companion object {

        /* The pre-defined field is 4 bytes */
        const val PRE_DEFINED_LENGTH = 4

        /* The handler type is 4 bytes */
        const val HANDLER_TYPE_LENGTH = 4

        /* The reserved field is 12 bytes */
        const val RESERVED_LENGTH = 12

        /* Version, flags, pre-defined, handler type and reserved. */
        const val NAME_FIELD_OFFSET =
            1 + FLAGS_LENGTH + PRE_DEFINED_LENGTH + HANDLER_TYPE_LENGTH + RESERVED_LENGTH

        /* The Pascal length byte is unsigned. */
        const val UNSIGNED_BYTE_MASK = 0xFF
    }
}
