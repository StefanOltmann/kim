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
public enum class SonyAFPoint2(
    public val value: Int,
    public val displayName: String
) {

    UPPER_LEFT(0, "Upper-left"),
    LEFT(1, "Left"),
    LOWER_LEFT(2, "Lower-left"),
    FAR_LEFT(3, "Far Left"),
    TOP_HORIZONTAL(4, "Top (horizontal)"),
    NEAR_RIGHT(5, "Near Right"),
    CENTER_HORIZONTAL(6, "Center (horizontal)"),
    NEAR_LEFT(7, "Near Left"),
    BOTTOM_HORIZONTAL(8, "Bottom (horizontal)"),
    TOP_VERTICAL(9, "Top (vertical)"),
    CENTER_VERTICAL(10, "Center (vertical)"),
    BOTTOM_VERTICAL(11, "Bottom (vertical)"),
    FAR_RIGHT(12, "Far Right"),
    UPPER_RIGHT(13, "Upper-right"),
    RIGHT(14, "Right"),
    LOWER_RIGHT(15, "Lower-right"),
    UPPER_MIDDLE(16, "Upper-middle"),
    LOWER_MIDDLE(17, "Lower-middle"),
    NONE(255, "(none)");

    public companion object {

        public fun fromValue(value: Int): SonyAFPoint2? =
            entries.firstOrNull { it.value == value }
    }
}
