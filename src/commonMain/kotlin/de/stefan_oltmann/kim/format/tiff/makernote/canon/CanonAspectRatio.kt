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
 * Values of the Canon AspectRatio tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAspectRatio(
    public val value: Int,
    public val displayName: String
) {

    VALUE_3_2(0, "3:2"),
    VALUE_1_1(1, "1:1"),
    VALUE_4_3(2, "4:3"),
    VALUE_16_9(7, "16:9"),
    VALUE_4_5(8, "4:5"),
    VALUE_3_2_APS_H_CROP(12, "3:2 (APS-H crop)"),
    VALUE_3_2_APS_C_CROP(13, "3:2 (APS-C crop)"),
    VALUE_4_3_CROP(258, "4:3 crop");

    public companion object {

        public fun fromValue(value: Int): CanonAspectRatio? =
            entries.firstOrNull { it.value == value }
    }
}
