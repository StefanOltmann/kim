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
 * Values of the FujiFilm Sharpness tag.
 */
public enum class FujiFilmSharpness(
    public val value: Int,
    public val description: String
) {

    SOFTEST(0x0, "-4 (softest)"),
    VERY_SOFT(0x1, "-3 (very soft)"),
    SOFT(0x2, "-2 (soft)"),
    NORMAL(0x3, "0 (normal)"),
    HARD(0x4, "+2 (hard)"),
    VERY_HARD(0x5, "+3 (very hard)"),
    HARDEST(0x6, "+4 (hardest)"),
    MEDIUM_SOFT(0x82, "-1 (medium soft)"),
    MEDIUM_HARD(0x84, "+1 (medium hard)"),
    FILM_SIMULATION(0x8000, "Film Simulation"),
    NOT_AVAILABLE(0xffff, "n/a");

    public companion object {

        public fun fromValue(value: Int): FujiFilmSharpness? =
            entries.firstOrNull { it.value == value }
    }
}
