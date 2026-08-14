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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

/**
 * Values of the Sony AFPointSelected tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html
 */
@Suppress("MaxLineLength")
public enum class SonyAFPointSelected(
    public val value: Int,
    public val displayName: String
) {

    AUTO(0, "Auto"),
    CENTER(1, "Center"),
    TOP(2, "Top"),
    UPPER_RIGHT(3, "Upper-right"),
    RIGHT(4, "Right"),
    LOWER_RIGHT(5, "Lower-right"),
    BOTTOM(6, "Bottom"),
    LOWER_LEFT(7, "Lower-left"),
    LEFT(8, "Left"),
    UPPER_LEFT(9, "Upper-left");

    public companion object {

        public fun fromValue(value: Int): SonyAFPointSelected? =
            entries.firstOrNull { it.value == value }
    }
}
