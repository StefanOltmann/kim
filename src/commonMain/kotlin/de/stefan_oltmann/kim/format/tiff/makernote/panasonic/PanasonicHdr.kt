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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

/**
 * Values of the Panasonic HDR tag.
 */
public enum class PanasonicHdr(
    public val value: Int,
    public val description: String
) {

    OFF(0, "Off"),
    EV_1(100, "1 EV"),
    EV_2(200, "2 EV"),
    EV_3(300, "3 EV"),
    EV_1_AUTO(32868, "1 EV (Auto)"),
    EV_2_AUTO(32968, "2 EV (Auto)"),
    EV_3_AUTO(33068, "3 EV (Auto)");

    public companion object {

        public fun fromValue(value: Int): PanasonicHdr? =
            entries.firstOrNull { it.value == value }
    }
}
