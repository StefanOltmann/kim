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
 * Values of the Canon MultiExposureControl tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonMultiExposureControl(
    public val value: Int,
    public val displayName: String
) {

    ADDITIVE(0, "Additive"),
    AVERAGE(1, "Average"),
    BRIGHT_COMPARATIVE(2, "Bright (comparative)"),
    DARK_COMPARATIVE(3, "Dark (comparative)");

    public companion object {

        public fun fromValue(value: Int): CanonMultiExposureControl? =
            entries.firstOrNull { it.value == value }
    }
}
