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
package de.stefan_oltmann.kim.format.jxl.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box

/**
 * JPEG XL brob box for brotli compressed Exif or XMP.
 *
 * Attention: A payload shorter than the 4-byte type field throws
 * [ImageReadException]. This is intentional: the brob may wrap Exif
 * or XMP metadata that an update would drop, so failing the read
 * informs the app upfront that editing is unsafe.
 */
public class CompressedBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.BROB, offset, size, largeSize, payload) {

    public val actualType: BoxType =
        if (payload.size < BMFFConstants.TYPE_LENGTH)
            throw ImageReadException(
                "Truncated brob box: payload is ${payload.size} bytes, " +
                    "expected at least ${BMFFConstants.TYPE_LENGTH}."
            )
        else
            BoxType.of(payload.take(BMFFConstants.TYPE_LENGTH).toByteArray())
}
