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
 * Values of the Sony ColorMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#ColorMode
 */
@Suppress("MaxLineLength")
public enum class SonyColorMode(
    public val value: Int,
    public val displayName: String
) {

    STANDARD(0x0, "Standard"),
    VIVID(0x1, "Vivid"),
    PORTRAIT(0x2, "Portrait"),
    LANDSCAPE(0x3, "Landscape"),
    SUNSET(0x4, "Sunset"),
    NIGHT_VIEW_PORTRAIT(0x5, "Night View/Portrait"),
    B_W(0x6, "B&W"),
    ADOBE_RGB(0x7, "Adobe RGB"),
    NEUTRAL(0xc, "Neutral"),
    CLEAR(0xd, "Clear"),
    DEEP(0xe, "Deep"),
    LIGHT(0xf, "Light"),
    AUTUMN_LEAVES(0x10, "Autumn Leaves"),
    SEPIA(0x11, "Sepia"),
    FL(0x12, "FL"),
    VIVID_2(0x13, "Vivid 2"),
    IN(0x14, "IN"),
    SH(0x15, "SH"),
    FL2(0x16, "FL2"),
    FL3(0x17, "FL3"),
    NEUTRAL_2(0x64, "Neutral"),
    CLEAR_2(0x65, "Clear"),
    DEEP_2(0x66, "Deep"),
    LIGHT_2(0x67, "Light"),
    NIGHT_VIEW(0x68, "Night View"),
    AUTUMN_LEAVES_2(0x69, "Autumn Leaves"),
    OFF(0xff, "Off"),
    N_A(0x0, "n/a");

    public companion object {

        public fun fromValue(value: Int): SonyColorMode? =
            entries.firstOrNull { it.value == value }
    }
}
