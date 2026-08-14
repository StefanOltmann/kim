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
 * Values of the Sony ZoneMatching tag.
 */
public enum class SonyZoneMatching(
    public val value: Int,
    public val description: String
) {

    ISO_SETTING_USED(0, "ISO Setting Used"),
    HIGH_KEY(1, "High Key"),
    LOW_KEY(2, "Low Key");

    public companion object {

        public fun fromValue(value: Int): SonyZoneMatching? =
            entries.firstOrNull { it.value == value }
    }
}
