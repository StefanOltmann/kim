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
 * Values of the Sony WhiteBalance tag (0xb054).
 */
public enum class SonyWhiteBalance2(
    public val value: Int,
    public val description: String
) {

    AUTO(0, "Auto"),
    CUSTOM(4, "Custom"),
    DAYLIGHT(5, "Daylight"),
    CLOUDY(6, "Cloudy"),
    COOL_WHITE_FLUORESCENT(7, "Cool White Fluorescent"),
    DAY_WHITE_FLUORESCENT(8, "Day White Fluorescent"),
    DAYLIGHT_FLUORESCENT(9, "Daylight Fluorescent"),
    INCANDESCENT_2(10, "Incandescent2"),
    WARM_WHITE_FLUORESCENT(11, "Warm White Fluorescent"),
    INCANDESCENT(14, "Incandescent"),
    FLASH(15, "Flash"),
    UNDERWATER_1(17, "Underwater 1 (Blue Water)"),
    UNDERWATER_2(18, "Underwater 2 (Green Water)"),
    UNDERWATER_AUTO(19, "Underwater Auto");

    public companion object {

        public fun fromValue(value: Int): SonyWhiteBalance2? =
            entries.firstOrNull { it.value == value }
    }
}
