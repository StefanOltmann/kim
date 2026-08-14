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
 * Values of the Panasonic BurstMode tag.
 */
public enum class PanasonicBurstMode(
    public val value: Int,
    public val description: String
) {

    OFF(0, "Off"),
    ON(1, "On"),
    AUTO_EXPOSURE_BRACKETING(2, "Auto Exposure Bracketing (AEB)"),
    FOCUS_BRACKETING(3, "Focus Bracketing"),
    UNLIMITED(4, "Unlimited"),
    WHITE_BALANCE_BRACKETING(8, "White Balance Bracketing"),
    ON_WITH_FLASH(17, "On (with flash)"),
    APERTURE_BRACKETING(18, "Aperture Bracketing");

    public companion object {

        public fun fromValue(value: Int): PanasonicBurstMode? =
            entries.firstOrNull { it.value == value }
    }
}
