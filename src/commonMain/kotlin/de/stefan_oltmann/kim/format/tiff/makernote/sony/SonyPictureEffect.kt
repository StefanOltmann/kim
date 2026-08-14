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
 * Values of the Sony PictureEffect tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#PictureEffect
 */
@Suppress("MaxLineLength")
public enum class SonyPictureEffect(
    public val value: Int,
    public val displayName: String
) {

    OFF(0x0, "Off"),
    TOY_CAMERA(0x1, "Toy Camera"),
    POP_COLOR(0x2, "Pop Color"),
    POSTERIZATION(0x3, "Posterization"),
    POSTERIZATION_B_W(0x4, "Posterization B/W"),
    RETRO_PHOTO(0x5, "Retro Photo"),
    SOFT_HIGH_KEY(0x6, "Soft High Key"),
    PARTIAL_COLOR_RED(0x7, "Partial Color (red)"),
    PARTIAL_COLOR_GREEN(0x8, "Partial Color (green)"),
    PARTIAL_COLOR_BLUE(0x9, "Partial Color (blue)"),
    PARTIAL_COLOR_YELLOW(0xa, "Partial Color (yellow)"),
    HIGH_CONTRAST_MONOCHROME(0xd, "High Contrast Monochrome"),
    TOY_CAMERA_NORMAL(0x10, "Toy Camera (normal)"),
    TOY_CAMERA_COOL(0x11, "Toy Camera (cool)"),
    TOY_CAMERA_WARM(0x12, "Toy Camera (warm)"),
    TOY_CAMERA_GREEN(0x13, "Toy Camera (green)"),
    TOY_CAMERA_MAGENTA(0x14, "Toy Camera (magenta)"),
    SOFT_FOCUS_LOW(0x20, "Soft Focus (low)"),
    SOFT_FOCUS(0x21, "Soft Focus"),
    SOFT_FOCUS_HIGH(0x22, "Soft Focus (high)"),
    MINIATURE_AUTO(0x30, "Miniature (auto)"),
    MINIATURE_TOP(0x31, "Miniature (top)"),
    MINIATURE_MIDDLE_HORIZONTAL(0x32, "Miniature (middle horizontal)"),
    MINIATURE_BOTTOM(0x33, "Miniature (bottom)"),
    MINIATURE_LEFT(0x34, "Miniature (left)"),
    MINIATURE_MIDDLE_VERTICAL(0x35, "Miniature (middle vertical)"),
    MINIATURE_RIGHT(0x36, "Miniature (right)"),
    HDR_PAINTING_LOW(0x40, "HDR Painting (low)"),
    HDR_PAINTING(0x41, "HDR Painting"),
    HDR_PAINTING_HIGH(0x42, "HDR Painting (high)"),
    RICH_TONE_MONOCHROME(0x50, "Rich-tone Monochrome"),
    WATER_COLOR(0x61, "Water Color"),
    WATER_COLOR_2(0x62, "Water Color 2"),
    ILLUSTRATION_LOW(0x70, "Illustration (low)"),
    ILLUSTRATION(0x71, "Illustration"),
    ILLUSTRATION_HIGH(0x72, "Illustration (high)");

    public companion object {

        public fun fromValue(value: Int): SonyPictureEffect? =
            entries.firstOrNull { it.value == value }
    }
}
