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
 * Values of the FujiFilm DynamicRangeSetting tag.
 */
public enum class FujiFilmDynamicRangeSetting(
    public val value: Int,
    public val description: String
) {

    AUTO(0x0, "Auto"),
    MANUAL(0x1, "Manual"),
    STANDARD(0x100, "Standard (100%)"),
    WIDE_1(0x200, "Wide1 (230%)"),
    WIDE_2(0x201, "Wide2 (400%)"),
    FILM_SIMULATION(0x8000, "Film Simulation");

    public companion object {

        public fun fromValue(value: Int): FujiFilmDynamicRangeSetting? =
            entries.firstOrNull { it.value == value }
    }
}
