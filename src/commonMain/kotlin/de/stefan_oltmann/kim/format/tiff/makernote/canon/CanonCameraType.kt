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
 * Values of the Canon CameraType tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonCameraType(
    public val value: Int,
    public val displayName: String
) {

    N_A(0, "n/a"),
    EOS_HIGH_END(248, "EOS High-end"),
    COMPACT(250, "Compact"),
    EOS_MID_RANGE(252, "EOS Mid-range"),
    DV_CAMERA(255, "DV Camera");

    public companion object {

        public fun fromValue(value: Int): CanonCameraType? =
            entries.firstOrNull { it.value == value }
    }
}
