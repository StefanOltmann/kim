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
 * Values of the Panasonic ImageQuality tag.
 */
public enum class PanasonicImageQuality(
    public val value: Int,
    public val description: String
) {

    TIFF(1, "TIFF"),
    HIGH(2, "High"),
    NORMAL(3, "Normal"),
    VERY_HIGH(6, "Very High"),
    RAW(7, "RAW"),
    MOTION_PICTURE(9, "Motion Picture"),
    FULL_HD_MOVIE(11, "Full HD Movie"),
    MOVIE_4K(12, "4k Movie");

    public companion object {

        public fun fromValue(value: Int): PanasonicImageQuality? =
            entries.firstOrNull { it.value == value }
    }
}
