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
package de.stefan_oltmann.kim.format.tiff.makernote.canon

/**
 * Values of the Canon AFPointsInFocus tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAFPointsInFocus(
    public val value: Int,
    public val displayName: String
) {

    NONE_MF(0x3000, "None (MF)"),
    RIGHT(0x3001, "Right"),
    CENTER(0x3002, "Center"),
    CENTER_RIGHT(0x3003, "Center+Right"),
    LEFT(0x3004, "Left"),
    LEFT_RIGHT(0x3005, "Left+Right"),
    LEFT_CENTER(0x3006, "Left+Center"),
    ALL(0x3007, "All");

    public companion object {

        public fun fromValue(value: Int): CanonAFPointsInFocus? =
            entries.firstOrNull { it.value == value }
    }
}
