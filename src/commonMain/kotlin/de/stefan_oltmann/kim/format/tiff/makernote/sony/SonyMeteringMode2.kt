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
 * Values of the Sony MeteringMode2 tag.
 */
public enum class SonyMeteringMode2(
    public val value: Int,
    public val description: String
) {

    MULTI_SEGMENT(0x100, "Multi-segment"),
    CENTER_WEIGHTED_AVERAGE(0x200, "Center-weighted average"),
    SPOT_STANDARD(0x301, "Spot (Standard)"),
    SPOT_LARGE(0x302, "Spot (Large)"),
    AVERAGE(0x400, "Average"),
    HIGHLIGHT(0x500, "Highlight");

    public companion object {

        public fun fromValue(value: Int): SonyMeteringMode2? =
            entries.firstOrNull { it.value == value }
    }
}
