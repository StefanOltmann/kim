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
 * Values of the FujiFilm AdvancedFilter tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmAdvancedFilter(
    public val value: Int,
    public val displayName: String
) {

    POP_COLOR(0x10000, "Pop Color"),
    HI_KEY(0x20000, "Hi Key"),
    TOY_CAMERA(0x30000, "Toy Camera"),
    MINIATURE(0x40000, "Miniature"),
    DYNAMIC_TONE(0x50000, "Dynamic Tone"),
    PARTIAL_COLOR_RED(0x60001, "Partial Color Red"),
    PARTIAL_COLOR_YELLOW(0x60002, "Partial Color Yellow"),
    PARTIAL_COLOR_GREEN(0x60003, "Partial Color Green"),
    PARTIAL_COLOR_BLUE(0x60004, "Partial Color Blue"),
    PARTIAL_COLOR_ORANGE(0x60005, "Partial Color Orange"),
    PARTIAL_COLOR_PURPLE(0x60006, "Partial Color Purple"),
    SOFT_FOCUS(0x70000, "Soft Focus"),
    LOW_KEY(0x90000, "Low Key"),
    LIGHT_LEAK(0x100000, "Light Leak"),
    EXPIRED_FILM_GREEN(0x130000, "Expired Film Green"),
    EXPIRED_FILM_RED(0x130001, "Expired Film Red"),
    EXPIRED_FILM_NEUTRAL(0x130002, "Expired Film Neutral");

    public companion object {

        public fun fromValue(value: Int): FujiFilmAdvancedFilter? =
            entries.firstOrNull { it.value == value }
    }
}
