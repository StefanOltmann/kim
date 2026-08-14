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
 * Values of the FujiFilm Contrast tags (0x1004 and 0x1006).
 */
public enum class FujiFilmContrast(
    public val value: Int,
    public val description: String
) {

    NORMAL(0x0, "Normal"),
    MEDIUM_HIGH(0x80, "Medium High"),
    HIGH(0x100, "High"),
    MEDIUM_LOW(0x180, "Medium Low"),
    LOW(0x200, "Low"),
    FILM_SIMULATION(0x8000, "Film Simulation");

    public companion object {

        public fun fromValue(value: Int): FujiFilmContrast? =
            entries.firstOrNull { it.value == value }
    }
}
