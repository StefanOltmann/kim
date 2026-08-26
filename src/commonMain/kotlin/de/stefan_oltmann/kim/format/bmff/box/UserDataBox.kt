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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader

/**
 * User Data box
 *
 * This box contains multiple UUID boxes.
 */
public class UserDataBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray,
    depth: Int = 0
) : Box(BoxType.UDTA, offset, size, largeSize, payload), BoxContainer {

    override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            positionOffset = 0,
            offsetShift = offset + 8,
            parentBoxType = type,
            depth = depth
        )
    }
}
