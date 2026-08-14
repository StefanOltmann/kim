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
 * Values of the Canon FlashModel tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonFlashModel(
    public val value: Int,
    public val displayName: String
) {

    N_A(0, "n/a"),
    SPEEDLITE_540_EZ(4, "Speedlite 540EZ"),
    SPEEDLITE_380_EX(5, "Speedlite 380EX"),
    SPEEDLITE_550_EX(6, "Speedlite 550EX"),
    SPEEDLITE_ST_E2(8, "Speedlite ST-E2"),
    SPEEDLITE_MR_14_EX(9, "Speedlite MR-14EX"),
    SPEEDLITE_580_EX(12, "Speedlite 580EX"),
    SPEEDLITE_430_EX(13, "Speedlite 430EX"),
    SPEEDLITE_580_EX_II(17, "Speedlite 580EX II"),
    SPEEDLITE_430_EX_II(18, "Speedlite 430EX II"),
    SPEEDLITE_600_EX_RT(22, "Speedlite 600EX-RT"),
    SPEEDLITE_600_EX_II_RT(23, "Speedlite 600EX II-RT"),
    SPEEDLITE_90_EX(24, "Speedlite 90EX"),
    SPEEDLITE_430_EX_III_RT(25, "Speedlite 430EX III-RT"),
    SPEEDLITE_EL_1_VER2(31, "Speedlite EL-1 ver2"),
    SPEEDLITE_EL_5(33, "Speedlite EL-5"),
    SPEEDLITE_EL_10(34, "Speedlite EL-10");

    public companion object {

        public fun fromValue(value: Int): CanonFlashModel? =
            entries.firstOrNull { it.value == value }
    }
}
