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
 * Values of the Sony CreativeStyle tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#CreativeStyle
 */
@Suppress("MaxLineLength")
public enum class SonyCreativeStyle(
    public val value: String,
    public val displayName: String
) {

    ADOBE_RGB("AdobeRGB", "Adobe RGB"),
    AUTUMN_LEAVES("Autumnleaves", "Autumn Leaves"),
    B_W("BW", "B&W"),
    CLEAR("Clear", "Clear"),
    DEEP("Deep", "Deep"),
    FL("FL", "FL"),
    IN("IN", "IN"),
    LANDSCAPE("Landscape", "Landscape"),
    LIGHT("Light", "Light"),
    NEUTRAL("Neutral", "Neutral"),
    NIGHT_VIEW_PORTRAIT("Nightview", "Night View/Portrait"),
    NONE("None", "None"),
    PORTRAIT("Portrait", "Portrait"),
    REAL("Real", "Real"),
    SH("SH", "SH"),
    SEPIA("Sepia", "Sepia"),
    STANDARD("Standard", "Standard"),
    SUNSET("Sunset", "Sunset"),
    VIVID_2("VV2", "Vivid 2"),
    VIVID("Vivid", "Vivid");

    public companion object {

        public fun fromValue(value: String): SonyCreativeStyle? =
            entries.firstOrNull { it.value == value }
    }
}
