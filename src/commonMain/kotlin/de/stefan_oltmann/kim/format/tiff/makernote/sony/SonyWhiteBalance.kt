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
 * Values of the Sony WhiteBalance tag (0x0115).
 */
public enum class SonyWhiteBalance(
    public val value: Int,
    public val description: String
) {

    AUTO(0x0, "Auto"),
    COLOR_TEMPERATURE_COLOR_FILTER(0x1, "Color Temperature/Color Filter"),
    DAYLIGHT(0x10, "Daylight"),
    CLOUDY(0x20, "Cloudy"),
    SHADE(0x30, "Shade"),
    TUNGSTEN(0x40, "Tungsten"),
    FLASH(0x50, "Flash"),
    FLUORESCENT(0x60, "Fluorescent"),
    CUSTOM(0x70, "Custom"),
    UNDERWATER(0x80, "Underwater");

    public companion object {

        public fun fromValue(value: Int): SonyWhiteBalance? =
            entries.firstOrNull { it.value == value }
    }
}
