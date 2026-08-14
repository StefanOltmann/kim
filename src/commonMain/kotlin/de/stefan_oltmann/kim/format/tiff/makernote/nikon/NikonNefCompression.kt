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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

/**
 * Values of the Nikon NEFCompression tag.
 */
public enum class NikonNefCompression(
    public val value: Int,
    public val description: String
) {

    LOSSY_TYPE_1(1, "Lossy (type 1)"),
    UNCOMPRESSED(2, "Uncompressed"),
    LOSSLESS(3, "Lossless"),
    LOSSY_TYPE_2(4, "Lossy (type 2)"),
    STRIPED_PACKED_12_BITS(5, "Striped packed 12 bits"),
    UNCOMPRESSED_REDUCED_TO_12_BIT(6, "Uncompressed (reduced to 12 bit)"),
    UNPACKED_12_BITS(7, "Unpacked 12 bits"),
    SMALL(8, "Small"),
    PACKED_12_BITS(9, "Packed 12 bits"),
    PACKED_14_BITS(10, "Packed 14 bits"),
    HIGH_EFFICIENCY(13, "High Efficiency"),
    HIGH_EFFICIENCY_STAR(14, "High Efficiency*");

    public companion object {

        public fun fromValue(value: Int): NikonNefCompression? =
            entries.firstOrNull { it.value == value }
    }
}
