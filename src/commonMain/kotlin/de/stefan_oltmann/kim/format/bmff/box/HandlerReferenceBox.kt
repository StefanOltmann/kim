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

import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.readNullTerminatedString
import de.stefan_oltmann.kim.input.skipBytes

/**
 * EIC/ISO 14496-12 hdlr box.
 */
public class HandlerReferenceBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.HDLR, offset, size, largeSize, payload) {

    public val version: Int

    public val flags: ByteArray

    public val handlerType: String

    public val name: String

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        byteReader.skipBytes("pre-defined", PRE_DEFINED_LENGTH)

        handlerType = byteReader.readBytes("handlerType", HANDLER_TYPE_LENGTH).decodeToString()

        byteReader.skipBytes("reserved", RESERVED_LENGTH)

        name = byteReader.readNullTerminatedString("name")
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
    }
}
