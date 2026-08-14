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
 * Values of the Panasonic FilmMode tag.
 */
public enum class PanasonicFilmMode(
    public val value: Int,
    public val description: String
) {

    NOT_AVAILABLE(0, "n/a"),
    STANDARD_COLOR(1, "Standard (color)"),
    DYNAMIC_COLOR(2, "Dynamic (color)"),
    NATURE_COLOR(3, "Nature (color)"),
    SMOOTH_COLOR(4, "Smooth (color)"),
    STANDARD_B_AND_W(5, "Standard (B&W)"),
    DYNAMIC_B_AND_W(6, "Dynamic (B&W)"),
    SMOOTH_B_AND_W(7, "Smooth (B&W)"),
    NOSTALGIC(10, "Nostalgic"),
    VIBRANT(11, "Vibrant");

    public companion object {

        public fun fromValue(value: Int): PanasonicFilmMode? =
            entries.firstOrNull { it.value == value }
    }
}
