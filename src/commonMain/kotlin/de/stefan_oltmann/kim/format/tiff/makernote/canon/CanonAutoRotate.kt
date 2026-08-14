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
 * Values of the Canon AutoRotate tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAutoRotate(
    public val value: Int,
    public val displayName: String
) {

    N_A(1, "n/a"),
    NONE(0, "None"),
    ROTATE_90_CW(1, "Rotate 90 CW"),
    ROTATE_180(2, "Rotate 180"),
    ROTATE_270_CW(3, "Rotate 270 CW");

    public companion object {

        public fun fromValue(value: Int): CanonAutoRotate? =
            entries.firstOrNull { it.value == value }
    }
}
