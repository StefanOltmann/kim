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
 * Values of the Canon RawJpgSize tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonRawJpgSize(
    public val value: Int,
    public val displayName: String
) {

    N_A(1, "n/a"),
    LARGE(0, "Large"),
    MEDIUM(1, "Medium"),
    SMALL(2, "Small"),
    MEDIUM_1(5, "Medium 1"),
    MEDIUM_2(6, "Medium 2"),
    MEDIUM_3(7, "Medium 3"),
    POSTCARD(8, "Postcard"),
    WIDESCREEN(9, "Widescreen"),
    MEDIUM_WIDESCREEN(10, "Medium Widescreen"),
    SMALL_1(14, "Small 1"),
    SMALL_2(15, "Small 2"),
    SMALL_3(16, "Small 3"),
    VALUE_640X480_MOVIE(128, "640x480 Movie"),
    MEDIUM_MOVIE(129, "Medium Movie"),
    SMALL_MOVIE(130, "Small Movie"),
    VALUE_1280X720_MOVIE(137, "1280x720 Movie"),
    VALUE_1920X1080_MOVIE(142, "1920x1080 Movie"),
    VALUE_4096X2160_MOVIE(143, "4096x2160 Movie");

    public companion object {

        public fun fromValue(value: Int): CanonRawJpgSize? =
            entries.firstOrNull { it.value == value }
    }
}
