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
 * Values of the Sony FocusStatus tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html
 */
@Suppress("MaxLineLength")
public enum class SonyFocusStatus(
    public val value: Int,
    public val displayName: String
) {

    MANUAL_NOT_CONFIRMED_0(0, "Manual - Not confirmed (0)"),
    MANUAL_NOT_CONFIRMED_4(4, "Manual - Not confirmed (4)"),
    AF_C_CONFIRMED(16, "AF-C - Confirmed"),
    AF_C_NOT_CONFIRMED(24, "AF-C - Not Confirmed"),
    AF_S_CONFIRMED(64, "AF-S - Confirmed");

    public companion object {

        public fun fromValue(value: Int): SonyFocusStatus? =
            entries.firstOrNull { it.value == value }
    }
}
