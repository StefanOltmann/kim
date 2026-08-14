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
 * Values of the Canon PanoramaFrameNumber tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonPanoramaFrameNumber(
    public val value: Int,
    public val displayName: String
) {

    LEFT_TO_RIGHT(0, "Left to Right"),
    RIGHT_TO_LEFT(1, "Right to Left"),
    BOTTOM_TO_TOP(2, "Bottom to Top"),
    TOP_TO_BOTTOM(3, "Top to Bottom"),
    VALUE_2X2_MATRIX_CLOCKWISE(4, "2x2 Matrix (Clockwise)");

    public companion object {

        public fun fromValue(value: Int): CanonPanoramaFrameNumber? =
            entries.firstOrNull { it.value == value }
    }
}
