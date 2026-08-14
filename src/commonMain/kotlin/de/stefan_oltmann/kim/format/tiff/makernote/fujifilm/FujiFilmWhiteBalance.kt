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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

/**
 * Values of the FujiFilm WhiteBalance tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmWhiteBalance(
    public val value: Int,
    public val displayName: String
) {

    AUTO(0x0, "Auto"),
    AUTO_WHITE_PRIORITY(0x1, "Auto (white priority)"),
    AUTO_AMBIANCE_PRIORITY(0x2, "Auto (ambiance priority)"),
    DAYLIGHT(0x100, "Daylight"),
    CLOUDY(0x200, "Cloudy"),
    DAYLIGHT_FLUORESCENT(0x300, "Daylight Fluorescent"),
    DAY_WHITE_FLUORESCENT(0x301, "Day White Fluorescent"),
    WHITE_FLUORESCENT(0x302, "White Fluorescent"),
    WARM_WHITE_FLUORESCENT(0x303, "Warm White Fluorescent"),
    LIVING_ROOM_WARM_WHITE_FLUORESCENT(0x304, "Living Room Warm White Fluorescent"),
    INCANDESCENT(0x400, "Incandescent"),
    FLASH(0x500, "Flash"),
    UNDERWATER(0x600, "Underwater"),
    CUSTOM(0xf00, "Custom"),
    CUSTOM2(0xf01, "Custom2"),
    CUSTOM3(0xf02, "Custom3"),
    CUSTOM4(0xf03, "Custom4"),
    CUSTOM5(0xf04, "Custom5"),
    KELVIN(0xff0, "Kelvin");

    public companion object {

        public fun fromValue(value: Int): FujiFilmWhiteBalance? =
            entries.firstOrNull { it.value == value }
    }
}
