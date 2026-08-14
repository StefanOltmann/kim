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
 * Values of the FujiFilm HighlightTone tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmHighlightTone(
    public val value: Int,
    public val displayName: String
) {

    HARDEST(0xffffffc0.toInt(), "+4 (hardest)"),
    VERY_HARD(0xffffffd0.toInt(), "+3 (very hard)"),
    HARD(0xffffffe0.toInt(), "+2 (hard)"),
    MEDIUM_HARD(0xfffffff0.toInt(), "+1 (medium hard)"),
    NORMAL(0x0, "0 (normal)"),
    MEDIUM_SOFT(0x10, "-1 (medium soft)"),
    SOFT(0x20, "-2 (soft)");

    public companion object {

        public fun fromValue(value: Int): FujiFilmHighlightTone? =
            entries.firstOrNull { it.value == value }
    }
}
