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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

/**
 * Values of the Sony ExposureMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#ExposureMode
 */
@Suppress("MaxLineLength")
public enum class SonyExposureMode(
    public val value: Int,
    public val displayName: String
) {

    PROGRAM_AE(0x0, "Program AE"),
    PORTRAIT(0x1, "Portrait"),
    BEACH(0x2, "Beach"),
    SPORTS(0x3, "Sports"),
    SNOW(0x4, "Snow"),
    LANDSCAPE(0x5, "Landscape"),
    AUTO(0x6, "Auto"),
    APERTURE_PRIORITY_AE(0x7, "Aperture-priority AE"),
    SHUTTER_SPEED_PRIORITY_AE(0x8, "Shutter speed priority AE"),
    NIGHT_SCENE_TWILIGHT(0x9, "Night Scene / Twilight"),
    HI_SPEED_SHUTTER(0xa, "Hi-Speed Shutter"),
    TWILIGHT_PORTRAIT(0xb, "Twilight Portrait"),
    SOFT_SNAP_PORTRAIT(0xc, "Soft Snap/Portrait"),
    FIREWORKS(0xd, "Fireworks"),
    SMILE_SHUTTER(0xe, "Smile Shutter"),
    MANUAL(0xf, "Manual"),
    HIGH_SENSITIVITY(0x12, "High Sensitivity"),
    MACRO(0x13, "Macro"),
    ADVANCED_SPORTS_SHOOTING(0x14, "Advanced Sports Shooting"),
    UNDERWATER(0x1d, "Underwater"),
    FOOD(0x21, "Food"),
    SWEEP_PANORAMA(0x22, "Sweep Panorama"),
    HANDHELD_NIGHT_SHOT(0x23, "Handheld Night Shot"),
    ANTI_MOTION_BLUR(0x24, "Anti Motion Blur"),
    PET(0x25, "Pet"),
    BACKLIGHT_CORRECTION_HDR(0x26, "Backlight Correction HDR"),
    SUPERIOR_AUTO(0x27, "Superior Auto"),
    BACKGROUND_DEFOCUS(0x28, "Background Defocus"),
    SOFT_SKIN(0x29, "Soft Skin"),
    VALUE_3D_IMAGE(0x2a, "3D Image"),
    N_A(0xffff, "n/a");

    public companion object {

        public fun fromValue(value: Int): SonyExposureMode? =
            entries.firstOrNull { it.value == value }
    }
}
