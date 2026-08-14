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
 * Values of the Sony DynamicRangeOptimizer tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#DynamicRangeOptimizer
 */
@Suppress("MaxLineLength")
public enum class SonyDynamicRangeOptimizer(
    public val value: Int,
    public val displayName: String
) {

    OFF(0x0, "Off"),
    STANDARD(0x1, "Standard"),
    ADVANCED_AUTO(0x2, "Advanced Auto"),
    AUTO(0x3, "Auto"),
    ADVANCED_LV1(0x8, "Advanced Lv1"),
    ADVANCED_LV2(0x9, "Advanced Lv2"),
    ADVANCED_LV3(0xa, "Advanced Lv3"),
    ADVANCED_LV4(0xb, "Advanced Lv4"),
    ADVANCED_LV5(0xc, "Advanced Lv5"),
    LV1(0x10, "Lv1"),
    LV2(0x11, "Lv2"),
    LV3(0x12, "Lv3"),
    LV4(0x13, "Lv4"),
    LV5(0x14, "Lv5"),
    LV6(0x15, "Lv6"),
    LV7(0x16, "Lv7"),
    LV8(0x17, "Lv8");

    public companion object {

        public fun fromValue(value: Int): SonyDynamicRangeOptimizer? =
            entries.firstOrNull { it.value == value }
    }
}
