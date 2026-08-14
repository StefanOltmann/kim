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
 * Values of the FujiFilm PictureMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmPictureMode(
    public val value: Int,
    public val displayName: String
) {

    AUTO(0x0, "Auto"),
    PORTRAIT(0x1, "Portrait"),
    LANDSCAPE(0x2, "Landscape"),
    MACRO(0x3, "Macro"),
    SPORTS(0x4, "Sports"),
    NIGHT_SCENE(0x5, "Night Scene"),
    PROGRAM_AE(0x6, "Program AE"),
    NATURAL_LIGHT(0x7, "Natural Light"),
    ANTI_BLUR(0x8, "Anti-blur"),
    BEACH_SNOW(0x9, "Beach & Snow"),
    SUNSET(0xa, "Sunset"),
    MUSEUM(0xb, "Museum"),
    PARTY(0xc, "Party"),
    FLOWER(0xd, "Flower"),
    TEXT(0xe, "Text"),
    NATURAL_LIGHT_FLASH(0xf, "Natural Light & Flash"),
    BEACH(0x10, "Beach"),
    SNOW(0x11, "Snow"),
    FIREWORKS(0x12, "Fireworks"),
    UNDERWATER(0x13, "Underwater"),
    PORTRAIT_WITH_SKIN_CORRECTION(0x14, "Portrait with Skin Correction"),
    PANORAMA(0x16, "Panorama"),
    NIGHT_TRIPOD(0x17, "Night (tripod)"),
    PRO_LOW_LIGHT(0x18, "Pro Low-light"),
    PRO_FOCUS(0x19, "Pro Focus"),
    PORTRAIT_2(0x1a, "Portrait 2"),
    DOG_FACE_DETECTION(0x1b, "Dog Face Detection"),
    CAT_FACE_DETECTION(0x1c, "Cat Face Detection"),
    HDR(0x30, "HDR"),
    ADVANCED_FILTER(0x40, "Advanced Filter"),
    APERTURE_PRIORITY_AE(0x100, "Aperture-priority AE"),
    SHUTTER_SPEED_PRIORITY_AE(0x200, "Shutter speed priority AE"),
    MANUAL(0x300, "Manual");

    public companion object {

        public fun fromValue(value: Int): FujiFilmPictureMode? =
            entries.firstOrNull { it.value == value }
    }
}
