/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readRemainingBytes

/**
 * EIC/ISO 14496-12 UUID box
 *
 * The UUID box is a container for several sub boxes.
 */
public class UuidBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.UUID, offset, size, largeSize, payload) {

    public val uuid: ByteArray

    public val uuidAsHex: String

    public val data: ByteArray

    init {

        val byteReader = ByteArrayByteReader(payload)

        uuid = byteReader.readBytes(UUID_LENGTH)
        uuidAsHex = uuid.toHex()

        data = byteReader.readRemainingBytes()
    }

    public val isXmp: Boolean get() = uuidAsHex == BMFFConstants.XMP_UUID

    override fun toString(): String =
        "Box '$type' @$offset uuid=$uuidAsHex (${actualLength} bytes)"

    private companion object {

        /* A UUID is always 16 bytes */
        const val UUID_LENGTH = 16
    }
}
