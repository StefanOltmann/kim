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
 * Values of the FujiFilm Saturation tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmSaturation(
    public val value: Int,
    public val displayName: String
) {

    NORMAL(0x0, "0 (normal)"),
    MEDIUM_HIGH(0x80, "+1 (medium high)"),
    VERY_HIGH(0xc0, "+3 (very high)"),
    HIGHEST(0xe0, "+4 (highest)"),
    HIGH(0x100, "+2 (high)"),
    MEDIUM_LOW(0x180, "-1 (medium low)"),
    LOW(0x200, "Low"),
    NONE_B_W(0x300, "None (B&W)"),
    B_W_RED_FILTER(0x301, "B&W Red Filter"),
    B_W_YELLOW_FILTER(0x302, "B&W Yellow Filter"),
    B_W_GREEN_FILTER(0x303, "B&W Green Filter"),
    B_W_SEPIA(0x310, "B&W Sepia"),
    LOW_2(0x400, "-2 (low)"),
    VERY_LOW(0x4c0, "-3 (very low)"),
    LOWEST(0x4e0, "-4 (lowest)"),
    ACROS(0x500, "Acros"),
    ACROS_RED_FILTER(0x501, "Acros Red Filter"),
    ACROS_YELLOW_FILTER(0x502, "Acros Yellow Filter"),
    ACROS_GREEN_FILTER(0x503, "Acros Green Filter"),
    FILM_SIMULATION(0x8000, "Film Simulation");

    public companion object {

        public fun fromValue(value: Int): FujiFilmSaturation? =
            entries.firstOrNull { it.value == value }
    }
}
