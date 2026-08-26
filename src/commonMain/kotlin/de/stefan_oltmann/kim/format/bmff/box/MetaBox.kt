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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes

/**
 * EIC/ISO 14496-12 meta box
 *
 * The Meta Box is a container for several metadata boxes.
 */
public open class MetaBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray,
    depth: Int = 0
) : Box(BoxType.META, offset, size, largeSize, payload), BoxContainer {

    public val version: Int

    public val flags: ByteArray

    /* Mandatory boxes in META */
    public val handlerReferenceBox: HandlerReferenceBox

    final override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", FLAGS_LENGTH)

        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            positionOffset = 4,
            offsetShift = offset + 8,
            parentBoxType = type,
            depth = depth
        )

        /* Find & set mandatory box */
        handlerReferenceBox = boxes.filterIsInstance<HandlerReferenceBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: meta has no hdlr box.")
    }

    override fun toString(): String =
        "$type Box version=$version flags=${flags.toHex()} boxes=${boxes.map { it.type }}"
}
