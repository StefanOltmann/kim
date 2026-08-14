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
 * Values of the Canon RawJpgQuality tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonRawJpgQuality(
    public val value: Int,
    public val displayName: String
) {

    N_A(1, "n/a"),
    ECONOMY(1, "Economy"),
    NORMAL(2, "Normal"),
    FINE(3, "Fine"),
    RAW(4, "RAW"),
    SUPERFINE(5, "Superfine"),
    CRAW(7, "CRAW"),
    LIGHT_RAW(130, "Light (RAW)"),
    STANDARD_RAW(131, "Standard (RAW)");

    public companion object {

        public fun fromValue(value: Int): CanonRawJpgQuality? =
            entries.firstOrNull { it.value == value }
    }
}
