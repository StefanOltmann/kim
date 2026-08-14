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
 * Values of the Canon AFPoint tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAFPoint(
    public val value: Int,
    public val displayName: String
) {

    MANUAL_AF_POINT_SELECTION(0x2005, "Manual AF point selection"),
    NONE_MF(0x3000, "None (MF)"),
    AUTO_AF_POINT_SELECTION(0x3001, "Auto AF point selection"),
    RIGHT(0x3002, "Right"),
    CENTER(0x3003, "Center"),
    LEFT(0x3004, "Left"),
    AUTO_AF_POINT_SELECTION_2(0x4001, "Auto AF point selection"),
    FACE_DETECT(0x4006, "Face Detect");

    public companion object {

        public fun fromValue(value: Int): CanonAFPoint? =
            entries.firstOrNull { it.value == value }
    }
}
