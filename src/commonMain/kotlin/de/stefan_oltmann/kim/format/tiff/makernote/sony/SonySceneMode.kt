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
 * Values of the Sony SceneMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#SceneMode
 */
@Suppress("MaxLineLength")
public enum class SonySceneMode(
    public val value: Int,
    public val displayName: String
) {

    STANDARD(0x0, "Standard"),
    PORTRAIT(0x1, "Portrait"),
    TEXT(0x2, "Text"),
    NIGHT_SCENE(0x3, "Night Scene"),
    SUNSET(0x4, "Sunset"),
    SPORTS(0x5, "Sports"),
    LANDSCAPE(0x6, "Landscape"),
    NIGHT_PORTRAIT(0x7, "Night Portrait"),
    MACRO(0x8, "Macro"),
    SUPER_MACRO(0x9, "Super Macro"),
    AUTO(0x10, "Auto"),
    NIGHT_VIEW_PORTRAIT(0x11, "Night View/Portrait"),
    SWEEP_PANORAMA(0x12, "Sweep Panorama"),
    HANDHELD_NIGHT_SHOT(0x13, "Handheld Night Shot"),
    ANTI_MOTION_BLUR(0x14, "Anti Motion Blur"),
    CONT_PRIORITY_AE(0x15, "Cont. Priority AE"),
    AUTO_2(0x16, "Auto+"),
    VALUE_3D_SWEEP_PANORAMA(0x17, "3D Sweep Panorama"),
    SUPERIOR_AUTO(0x18, "Superior Auto"),
    HIGH_SENSITIVITY(0x19, "High Sensitivity"),
    FIREWORKS(0x1a, "Fireworks"),
    FOOD(0x1b, "Food"),
    PET(0x1c, "Pet"),
    HDR(0x21, "HDR"),
    N_A(0xffff, "n/a");

    public companion object {

        public fun fromValue(value: Int): SonySceneMode? =
            entries.firstOrNull { it.value == value }
    }
}
