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
 * Values of the Panasonic VideoBurstResolution tag.
 */
public enum class PanasonicVideoBurstResolution(
    public val value: Int,
    public val description: String
) {

    OFF_OR_4K(1, "Off or 4K"),
    RESOLUTION_6K(4, "6K");

    public companion object {

        public fun fromValue(value: Int): PanasonicVideoBurstResolution? =
            entries.firstOrNull { it.value == value }
    }
}
