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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

/**
 * Values of the FujiFilm ColorMode tag (0x1210).
 */
public enum class FujiFilmColorMode(
    public val value: Int,
    public val description: String
) {

    STANDARD(0x0, "Standard"),
    CHROME(0x10, "Chrome"),
    B_AND_W(0x30, "B & W");

    public companion object {

        public fun fromValue(value: Int): FujiFilmColorMode? =
            entries.firstOrNull { it.value == value }
    }
}
