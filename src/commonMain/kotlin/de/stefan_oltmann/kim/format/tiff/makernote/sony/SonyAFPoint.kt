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
 * Values of the Sony AFPoint tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html
 */
@Suppress("MaxLineLength")
public enum class SonyAFPoint(
    public val value: Int,
    public val displayName: String
) {

    TOP_RIGHT(0, "Top-right"),
    BOTTOM_RIGHT(1, "Bottom-right"),
    BOTTOM(2, "Bottom"),
    MIDDLE_HORIZONTAL(3, "Middle Horizontal"),
    CENTER_VERTICAL(4, "Center Vertical"),
    TOP(5, "Top"),
    TOP_LEFT(6, "Top-left"),
    BOTTOM_LEFT(7, "Bottom-left");

    public companion object {

        public fun fromValue(value: Int): SonyAFPoint? =
            entries.firstOrNull { it.value == value }
    }
}
