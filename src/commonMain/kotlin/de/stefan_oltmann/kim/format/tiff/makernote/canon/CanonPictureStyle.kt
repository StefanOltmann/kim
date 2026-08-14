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
 * Values of the Canon PictureStyle tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#PictureStyle
 */
@Suppress("MaxLineLength")
public enum class CanonPictureStyle(
    public val value: Int,
    public val displayName: String
) {

    NONE(0x0, "None"),
    USER_DEF_1(0x21, "User Def. 1"),
    LANDSCAPE(0x83, "Landscape"),
    STANDARD(0x1, "Standard"),
    USER_DEF_2(0x22, "User Def. 2"),
    NEUTRAL(0x84, "Neutral"),
    PORTRAIT(0x2, "Portrait"),
    USER_DEF_3(0x23, "User Def. 3"),
    FAITHFUL(0x85, "Faithful"),
    HIGH_SATURATION(0x3, "High Saturation"),
    PC_1(0x41, "PC 1"),
    MONOCHROME(0x86, "Monochrome"),
    ADOBE_RGB(0x4, "Adobe RGB"),
    PC_2(0x42, "PC 2"),
    AUTO(0x87, "Auto"),
    LOW_SATURATION(0x5, "Low Saturation"),
    PC_3(0x43, "PC 3"),
    FINE_DETAIL(0x88, "Fine Detail"),
    CM_SET_1(0x6, "CM Set 1"),
    STANDARD_2(0x81, "Standard"),
    N_A(0xff, "n/a"),
    CM_SET_2(0x7, "CM Set 2"),
    PORTRAIT_2(0x82, "Portrait"),
    N_A_2(0xffff, "n/a");

    public companion object {

        public fun fromValue(value: Int): CanonPictureStyle? =
            entries.firstOrNull { it.value == value }
    }
}
