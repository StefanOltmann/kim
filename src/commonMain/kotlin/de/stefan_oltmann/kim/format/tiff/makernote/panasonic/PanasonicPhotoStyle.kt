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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

/**
 * Values of the Panasonic PhotoStyle tag.
 */
public enum class PanasonicPhotoStyle(
    public val value: Int,
    public val description: String
) {

    AUTO(0, "Auto"),
    STANDARD_OR_CUSTOM(1, "Standard or Custom"),
    VIVID(2, "Vivid"),
    NATURAL(3, "Natural"),
    MONOCHROME(4, "Monochrome"),
    SCENERY(5, "Scenery"),
    PORTRAIT(6, "Portrait"),
    CINELIKE_D(8, "Cinelike D"),
    CINELIKE_V(9, "Cinelike V"),
    L_MONOCHROME(11, "L. Monochrome"),
    LIKE709(12, "Like709"),
    L_MONOCHROME_D(15, "L. Monochrome D"),
    V_LOG(17, "V-Log"),
    CINELIKE_D2(18, "Cinelike D2");

    public companion object {

        public fun fromValue(value: Int): PanasonicPhotoStyle? =
            entries.firstOrNull { it.value == value }
    }
}
