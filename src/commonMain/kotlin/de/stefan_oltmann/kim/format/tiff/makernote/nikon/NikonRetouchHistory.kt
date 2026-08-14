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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

/**
 * Values of the Nikon RetouchHistory tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html#RetouchHistory
 */
@Suppress("MaxLineLength")
public enum class NikonRetouchHistory(
    public val value: Int,
    public val displayName: String
) {

    NONE(0x0, "None"),
    B_W(0x3, "B & W"),
    SEPIA(0x4, "Sepia"),
    TRIM(0x5, "Trim"),
    SMALL_PICTURE(0x6, "Small Picture"),
    D_LIGHTING(0x7, "D-Lighting"),
    RED_EYE(0x8, "Red Eye"),
    CYANOTYPE(0x9, "Cyanotype"),
    SKY_LIGHT(0xa, "Sky Light"),
    WARM_TONE(0xb, "Warm Tone"),
    COLOR_CUSTOM(0xc, "Color Custom"),
    IMAGE_OVERLAY(0xd, "Image Overlay"),
    RED_INTENSIFIER(0xe, "Red Intensifier"),
    GREEN_INTENSIFIER(0xf, "Green Intensifier"),
    BLUE_INTENSIFIER(0x10, "Blue Intensifier"),
    CROSS_SCREEN(0x11, "Cross Screen"),
    QUICK_RETOUCH(0x12, "Quick Retouch"),
    NEF_PROCESSING(0x13, "NEF Processing"),
    DISTORTION_CONTROL(0x17, "Distortion Control"),
    FISHEYE(0x19, "Fisheye"),
    STRAIGHTEN(0x1a, "Straighten"),
    PERSPECTIVE_CONTROL(0x1d, "Perspective Control"),
    COLOR_OUTLINE(0x1e, "Color Outline"),
    SOFT_FILTER(0x1f, "Soft Filter"),
    RESIZE(0x20, "Resize"),
    MINIATURE_EFFECT(0x21, "Miniature Effect"),
    SKIN_SOFTENING(0x22, "Skin Softening"),
    SELECTED_FRAME(0x23, "Selected Frame"),
    COLOR_SKETCH(0x25, "Color Sketch"),
    SELECTIVE_COLOR(0x26, "Selective Color"),
    GLAMOUR(0x27, "Glamour"),
    DRAWING(0x28, "Drawing"),
    POP(0x2c, "Pop"),
    TOY_CAMERA_EFFECT_1(0x2d, "Toy Camera Effect 1"),
    TOY_CAMERA_EFFECT_2(0x2e, "Toy Camera Effect 2"),
    CROSS_PROCESS_RED(0x2f, "Cross Process (red)"),
    CROSS_PROCESS_BLUE(0x30, "Cross Process (blue)"),
    CROSS_PROCESS_GREEN(0x31, "Cross Process (green)"),
    CROSS_PROCESS_YELLOW(0x32, "Cross Process (yellow)"),
    SUPER_VIVID(0x33, "Super Vivid"),
    HIGH_CONTRAST_MONOCHROME(0x34, "High-contrast Monochrome"),
    HIGH_KEY(0x35, "High Key"),
    LOW_KEY(0x36, "Low Key");

    public companion object {

        public fun fromValue(value: Int): NikonRetouchHistory? =
            entries.firstOrNull { it.value == value }
    }
}
