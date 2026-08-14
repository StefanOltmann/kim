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
 * Values of the Sony Teleconverter tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#Teleconverter
 */
@Suppress("MaxLineLength")
public enum class SonyTeleconverter(
    public val value: Int,
    public val displayName: String
) {

    NONE(0x0, "None"),
    MINOLTA_SONY_AF_1_4X_APO_D_0X04(0x4, "Minolta/Sony AF 1.4x APO (D) (0x04)"),
    MINOLTA_SONY_AF_2X_APO_D_0X05(0x5, "Minolta/Sony AF 2x APO (D) (0x05)"),
    MINOLTA_SONY_AF_2X_APO_D(0x48, "Minolta/Sony AF 2x APO (D)"),
    MINOLTA_AF_2X_APO_II(0x50, "Minolta AF 2x APO II"),
    MINOLTA_AF_2X_APO(0x60, "Minolta AF 2x APO"),
    MINOLTA_SONY_AF_1_4X_APO_D(0x88, "Minolta/Sony AF 1.4x APO (D)"),
    MINOLTA_AF_1_4X_APO_II(0x90, "Minolta AF 1.4x APO II"),
    MINOLTA_AF_1_4X_APO(0xa0, "Minolta AF 1.4x APO");

    public companion object {

        public fun fromValue(value: Int): SonyTeleconverter? =
            entries.firstOrNull { it.value == value }
    }
}
