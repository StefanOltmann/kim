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
 * Values of the Nikon ISOExpansion2 tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html
 */
@Suppress("MaxLineLength")
public enum class NikonISOExpansion2(
    public val value: Int,
    public val displayName: String
) {

    OFF(0x0, "Off"),
    HI_0_3(0x101, "Hi 0.3"),
    HI_0_5(0x102, "Hi 0.5"),
    HI_0_7(0x103, "Hi 0.7"),
    HI_1_0(0x104, "Hi 1.0"),
    HI_1_3(0x105, "Hi 1.3"),
    HI_1_5(0x106, "Hi 1.5"),
    HI_1_7(0x107, "Hi 1.7"),
    HI_2_0(0x108, "Hi 2.0"),
    LO_0_3(0x201, "Lo 0.3"),
    LO_0_5(0x202, "Lo 0.5"),
    LO_0_7(0x203, "Lo 0.7"),
    LO_1_0(0x204, "Lo 1.0");

    public companion object {

        public fun fromValue(value: Int): NikonISOExpansion2? =
            entries.firstOrNull { it.value == value }
    }
}
