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
 * Values of the Panasonic FocusMode tag.
 */
public enum class PanasonicFocusMode(
    public val value: Int,
    public val description: String
) {

    AUTO(1, "Auto"),
    MANUAL(2, "Manual"),
    AUTO_FOCUS_BUTTON(4, "Auto, Focus button"),
    AUTO_CONTINUOUS(5, "Auto, Continuous"),
    AF_S(6, "AF-S"),
    AF_C(7, "AF-C"),
    AF_F(8, "AF-F");

    public companion object {

        public fun fromValue(value: Int): PanasonicFocusMode? =
            entries.firstOrNull { it.value == value }
    }
}
