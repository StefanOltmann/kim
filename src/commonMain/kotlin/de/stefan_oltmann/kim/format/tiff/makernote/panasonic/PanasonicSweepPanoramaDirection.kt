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
 * Values of the Panasonic SweepPanoramaDirection tag.
 */
public enum class PanasonicSweepPanoramaDirection(
    public val value: Int,
    public val description: String
) {

    OFF(0, "Off"),
    LEFT_TO_RIGHT(1, "Left to Right"),
    RIGHT_TO_LEFT(2, "Right to Left"),
    TOP_TO_BOTTOM(3, "Top to Bottom"),
    BOTTOM_TO_TOP(4, "Bottom to Top");

    public companion object {

        public fun fromValue(value: Int): PanasonicSweepPanoramaDirection? =
            entries.firstOrNull { it.value == value }
    }
}
