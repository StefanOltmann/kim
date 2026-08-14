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
 * Values of the Canon ExposureMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonExposureMode(
    public val value: Int,
    public val displayName: String
) {

    EASY(0, "Easy"),
    PROGRAM_AE(1, "Program AE"),
    SHUTTER_SPEED_PRIORITY_AE(2, "Shutter speed priority AE"),
    APERTURE_PRIORITY_AE(3, "Aperture-priority AE"),
    MANUAL(4, "Manual"),
    DEPTH_OF_FIELD_AE(5, "Depth-of-field AE"),
    M_DEP(6, "M-Dep"),
    BULB(7, "Bulb"),
    FLEXIBLE_PRIORITY_AE(8, "Flexible-priority AE");

    public companion object {

        public fun fromValue(value: Int): CanonExposureMode? =
            entries.firstOrNull { it.value == value }
    }
}
