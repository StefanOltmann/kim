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
 * Values of the Panasonic WhiteBalance tag.
 */
public enum class PanasonicWhiteBalance(
    public val value: Int,
    public val description: String
) {

    AUTO(1, "Auto"),
    DAYLIGHT(2, "Daylight"),
    CLOUDY(3, "Cloudy"),
    INCANDESCENT(4, "Incandescent"),
    MANUAL(5, "Manual"),
    FLASH(8, "Flash"),
    BLACK_AND_WHITE(10, "Black & White"),
    MANUAL_2(11, "Manual 2"),
    SHADE(12, "Shade"),
    KELVIN(13, "Kelvin"),
    MANUAL_3(14, "Manual 3"),
    MANUAL_4(15, "Manual 4"),
    AUTO_COOL(19, "Auto (cool)");

    public companion object {

        public fun fromValue(value: Int): PanasonicWhiteBalance? =
            entries.firstOrNull { it.value == value }
    }
}
