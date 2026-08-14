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
 * Values of the Sony LongExposureNoiseReduction tag.
 */
public enum class SonyLongExposureNoiseReduction(
    public val value: Int,
    public val description: String
) {

    OFF(0x0, "Off"),
    ON_UNUSED(0x1, "On (unused)"),
    ON_DARK_SUBTRACTED(0x10001, "On (dark subtracted)"),
    OFF_65535(0xffff0000.toInt(), "Off (65535)"),
    ON_65535(0xffff0001.toInt(), "On (65535)"),
    NOT_AVAILABLE(0xffffffff.toInt(), "n/a");

    public companion object {

        public fun fromValue(value: Int): SonyLongExposureNoiseReduction? =
            entries.firstOrNull { it.value == value }
    }
}
