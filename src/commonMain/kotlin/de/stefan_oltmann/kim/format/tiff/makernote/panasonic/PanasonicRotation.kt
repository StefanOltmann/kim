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
 * Values of the Panasonic Rotation tag.
 */
public enum class PanasonicRotation(
    public val value: Int,
    public val description: String
) {

    HORIZONTAL(1, "Horizontal (normal)"),
    ROTATE_180(3, "Rotate 180"),
    ROTATE_90_CW(6, "Rotate 90 CW"),
    ROTATE_270_CW(8, "Rotate 270 CW");

    public companion object {

        public fun fromValue(value: Int): PanasonicRotation? =
            entries.firstOrNull { it.value == value }
    }
}
