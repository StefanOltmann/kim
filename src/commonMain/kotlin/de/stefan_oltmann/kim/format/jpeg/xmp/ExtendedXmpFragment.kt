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
package de.stefan_oltmann.kim.format.jpeg.xmp

/**
 * A fragment of Adobe extended XMP data from a single APP1 segment.
 *
 * Extended XMP segments carry the identifier
 * "http://ns.adobe.com/xmp/extension/", the 32-character hexadecimal GUID of
 * the complete extended packet, its total size as a 4-byte big-endian value,
 * and one chunk of the packet data.
 */
internal data class ExtendedXmpFragment(
    val guid: String,
    val totalLength: Int,
    val data: ByteArray
) {

    override fun equals(other: Any?): Boolean =
        other is ExtendedXmpFragment &&
            guid == other.guid &&
            totalLength == other.totalLength &&
            data.contentEquals(other.data)

    override fun hashCode(): Int =
        guid.hashCode() * 31 + totalLength
}
