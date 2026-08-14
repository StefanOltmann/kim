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
 * Values of the Canon ContinuousDrive tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonContinuousDrive(
    public val value: Int,
    public val displayName: String
) {

    SINGLE(0, "Single"),
    CONTINUOUS(1, "Continuous"),
    MOVIE(2, "Movie"),
    CONTINUOUS_SPEED_PRIORITY(3, "Continuous, Speed Priority"),
    CONTINUOUS_LOW(4, "Continuous, Low"),
    CONTINUOUS_HIGH(5, "Continuous, High"),
    SILENT_SINGLE(6, "Silent Single"),
    CONTINUOUS_HIGH_2(8, "Continuous, High+"),
    SINGLE_SILENT(9, "Single, Silent"),
    CONTINUOUS_SILENT(10, "Continuous, Silent");

    public companion object {

        public fun fromValue(value: Int): CanonContinuousDrive? =
            entries.firstOrNull { it.value == value }
    }
}
