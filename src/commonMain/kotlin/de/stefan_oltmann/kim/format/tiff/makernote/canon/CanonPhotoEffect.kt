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
 * Values of the Canon PhotoEffect tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonPhotoEffect(
    public val value: Int,
    public val displayName: String
) {

    OFF(0, "Off"),
    VIVID(1, "Vivid"),
    NEUTRAL(2, "Neutral"),
    SMOOTH(3, "Smooth"),
    SEPIA(4, "Sepia"),
    B_W(5, "B&W"),
    CUSTOM(6, "Custom"),
    MY_COLOR_DATA(100, "My Color Data");

    public companion object {

        public fun fromValue(value: Int): CanonPhotoEffect? =
            entries.firstOrNull { it.value == value }
    }
}
