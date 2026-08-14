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
 * Values of the FujiFilm CropMode tag.
 */
public enum class FujiFilmCropMode(
    public val value: Int,
    public val description: String
) {

    NOT_AVAILABLE(0, "n/a"),
    FULL_FRAME_ON_GFX(1, "Full-frame on GFX"),
    SPORTS_FINDER_MODE(2, "Sports Finder Mode"),
    ELECTRONIC_SHUTTER_1_25X(4, "Electronic Shutter 1.25x Crop"),
    DIGITAL_TELE_CONV(8, "Digital Tele-Conv");

    public companion object {

        public fun fromValue(value: Int): FujiFilmCropMode? =
            entries.firstOrNull { it.value == value }
    }
}
