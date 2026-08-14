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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

/**
 * Values of the Panasonic ShootingMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Panasonic.html#ShootingMode
 */
@Suppress("MaxLineLength")
public enum class PanasonicShootingMode(
    public val value: Int,
    public val displayName: String
) {

    NORMAL(0x1, "Normal"),
    PORTRAIT(0x2, "Portrait"),
    SCENERY(0x3, "Scenery"),
    SPORTS(0x4, "Sports"),
    NIGHT_PORTRAIT(0x5, "Night Portrait"),
    PROGRAM(0x6, "Program"),
    APERTURE_PRIORITY(0x7, "Aperture Priority"),
    SHUTTER_PRIORITY(0x8, "Shutter Priority"),
    MACRO(0x9, "Macro"),
    SPOT(0xa, "Spot"),
    MANUAL(0xb, "Manual"),
    MOVIE_PREVIEW(0xc, "Movie Preview"),
    PANNING(0xd, "Panning"),
    SIMPLE(0xe, "Simple"),
    COLOR_EFFECTS(0xf, "Color Effects"),
    SELF_PORTRAIT(0x10, "Self Portrait"),
    ECONOMY(0x11, "Economy"),
    FIREWORKS(0x12, "Fireworks"),
    PARTY(0x13, "Party"),
    SNOW(0x14, "Snow"),
    NIGHT_SCENERY(0x15, "Night Scenery"),
    FOOD(0x16, "Food"),
    BABY(0x17, "Baby"),
    SOFT_SKIN(0x18, "Soft Skin"),
    CANDLELIGHT(0x19, "Candlelight"),
    STARRY_NIGHT(0x1a, "Starry Night"),
    HIGH_SENSITIVITY(0x1b, "High Sensitivity"),
    PANORAMA_ASSIST(0x1c, "Panorama Assist"),
    UNDERWATER(0x1d, "Underwater"),
    BEACH(0x1e, "Beach"),
    AERIAL_PHOTO(0x1f, "Aerial Photo"),
    SUNSET(0x20, "Sunset"),
    PET(0x21, "Pet"),
    INTELLIGENT_ISO(0x22, "Intelligent ISO"),
    CLIPBOARD(0x23, "Clipboard"),
    HIGH_SPEED_CONTINUOUS_SHOOTING(0x24, "High Speed Continuous Shooting"),
    INTELLIGENT_AUTO(0x25, "Intelligent Auto"),
    MULTI_ASPECT(0x27, "Multi-aspect"),
    TRANSFORM(0x29, "Transform"),
    FLASH_BURST(0x2a, "Flash Burst"),
    PIN_HOLE(0x2b, "Pin Hole"),
    FILM_GRAIN(0x2c, "Film Grain"),
    MY_COLOR(0x2d, "My Color"),
    PHOTO_FRAME(0x2e, "Photo Frame"),
    MOVIE(0x30, "Movie"),
    HDR(0x33, "HDR"),
    PERIPHERAL_DEFOCUS(0x34, "Peripheral Defocus"),
    HANDHELD_NIGHT_SHOT(0x37, "Handheld Night Shot"),
    VALUE_3D(0x39, "3D"),
    CREATIVE_CONTROL(0x3b, "Creative Control"),
    INTELLIGENT_AUTO_PLUS(0x3c, "Intelligent Auto Plus"),
    PANORAMA(0x3e, "Panorama"),
    GLASS_THROUGH(0x3f, "Glass Through"),
    HDR_2(0x40, "HDR"),
    DIGITAL_FILTER(0x42, "Digital Filter"),
    CLEAR_PORTRAIT(0x43, "Clear Portrait"),
    SILKY_SKIN(0x44, "Silky Skin"),
    BACKLIT_SOFTNESS(0x45, "Backlit Softness"),
    CLEAR_IN_BACKLIGHT(0x46, "Clear in Backlight"),
    RELAXING_TONE(0x47, "Relaxing Tone"),
    SWEET_CHILD_S_FACE(0x48, "Sweet Child\'s Face"),
    DISTINCT_SCENERY(0x49, "Distinct Scenery"),
    BRIGHT_BLUE_SKY(0x4a, "Bright Blue Sky"),
    ROMANTIC_SUNSET_GLOW(0x4b, "Romantic Sunset Glow"),
    VIVID_SUNSET_GLOW(0x4c, "Vivid Sunset Glow"),
    GLISTENING_WATER(0x4d, "Glistening Water"),
    CLEAR_NIGHTSCAPE(0x4e, "Clear Nightscape"),
    COOL_NIGHT_SKY(0x4f, "Cool Night Sky"),
    WARM_GLOWING_NIGHTSCAPE(0x50, "Warm Glowing Nightscape"),
    ARTISTIC_NIGHTSCAPE(0x51, "Artistic Nightscape"),
    GLITTERING_ILLUMINATIONS(0x52, "Glittering Illuminations"),
    CLEAR_NIGHT_PORTRAIT(0x53, "Clear Night Portrait"),
    SOFT_IMAGE_OF_A_FLOWER(0x54, "Soft Image of a Flower"),
    APPETIZING_FOOD(0x55, "Appetizing Food"),
    CUTE_DESSERT(0x56, "Cute Dessert"),
    FREEZE_ANIMAL_MOTION(0x57, "Freeze Animal Motion"),
    CLEAR_SPORTS_SHOT(0x58, "Clear Sports Shot"),
    MONOCHROME(0x59, "Monochrome"),
    CREATIVE_CONTROL_2(0x5a, "Creative Control"),
    HANDHELD_NIGHT_SHOT_2(0x5c, "Handheld Night Shot");

    public companion object {

        public fun fromValue(value: Int): PanasonicShootingMode? =
            entries.firstOrNull { it.value == value }
    }
}
