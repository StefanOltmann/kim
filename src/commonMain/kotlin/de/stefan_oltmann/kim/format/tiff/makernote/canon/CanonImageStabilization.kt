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
 * Values of the Canon ImageStabilization tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonImageStabilization(
    public val value: Int,
    public val displayName: String
) {

    OFF(0, "Off"),
    ON(1, "On"),
    SHOOT_ONLY(2, "Shoot Only"),
    PANNING(3, "Panning"),
    DYNAMIC(4, "Dynamic"),
    OFF_2(256, "Off (2)"),
    ON_2(257, "On (2)"),
    SHOOT_ONLY_2(258, "Shoot Only (2)"),
    PANNING_2(259, "Panning (2)"),
    DYNAMIC_2(260, "Dynamic (2)");

    public companion object {

        public fun fromValue(value: Int): CanonImageStabilization? =
            entries.firstOrNull { it.value == value }
    }
}
