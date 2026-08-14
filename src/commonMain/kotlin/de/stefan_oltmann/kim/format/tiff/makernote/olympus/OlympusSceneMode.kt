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
package de.stefan_oltmann.kim.format.tiff.makernote.olympus

/**
 * Values of the Olympus SceneMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Olympus.html#SceneMode
 */
@Suppress("MaxLineLength")
public enum class OlympusSceneMode(
    public val value: Int,
    public val displayName: String
) {

    NORMAL(0x0, "Normal"),
    STANDARD(0x1, "Standard"),
    AUTO(0x2, "Auto"),
    INTELLIGENT_AUTO(0x3, "Intelligent Auto"),
    PORTRAIT(0x4, "Portrait"),
    LANDSCAPE_PORTRAIT(0x5, "Landscape+Portrait"),
    LANDSCAPE(0x6, "Landscape"),
    NIGHT_SCENE(0x7, "Night Scene"),
    NIGHT_PORTRAIT(0x8, "Night+Portrait"),
    SPORT(0x9, "Sport"),
    SELF_PORTRAIT(0xa, "Self Portrait"),
    INDOOR(0xb, "Indoor"),
    BEACH_SNOW(0xc, "Beach & Snow"),
    BEACH(0xd, "Beach"),
    SNOW(0xe, "Snow"),
    SELF_PORTRAIT_SELF_TIMER(0xf, "Self Portrait+Self Timer"),
    SUNSET(0x10, "Sunset"),
    CUISINE(0x11, "Cuisine"),
    DOCUMENTS(0x12, "Documents"),
    CANDLE(0x13, "Candle"),
    FIREWORKS(0x14, "Fireworks"),
    AVAILABLE_LIGHT(0x15, "Available Light"),
    VIVID(0x16, "Vivid"),
    UNDERWATER_WIDE1(0x17, "Underwater Wide1"),
    UNDERWATER_MACRO(0x18, "Underwater Macro"),
    MUSEUM(0x19, "Museum"),
    BEHIND_GLASS(0x1a, "Behind Glass"),
    AUCTION(0x1b, "Auction"),
    SHOOT_SELECT1(0x1c, "Shoot & Select1"),
    SHOOT_SELECT2(0x1d, "Shoot & Select2"),
    UNDERWATER_WIDE2(0x1e, "Underwater Wide2"),
    DIGITAL_IMAGE_STABILIZATION(0x1f, "Digital Image Stabilization"),
    FACE_PORTRAIT(0x20, "Face Portrait"),
    PET(0x21, "Pet"),
    SMILE_SHOT(0x22, "Smile Shot"),
    QUICK_SHUTTER(0x23, "Quick Shutter"),
    HAND_HELD_STARLIGHT(0x2b, "Hand-held Starlight"),
    PANORAMA(0x64, "Panorama"),
    MAGIC_FILTER(0x65, "Magic Filter"),
    HDR(0x67, "HDR");

    public companion object {

        public fun fromValue(value: Int): OlympusSceneMode? =
            entries.firstOrNull { it.value == value }
    }
}
