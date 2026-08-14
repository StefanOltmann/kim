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
 * Values of the Panasonic CameraOrientation tag.
 */
public enum class PanasonicCameraOrientation(
    public val value: Int,
    public val description: String
) {

    NORMAL(0, "Normal"),
    ROTATE_CW(1, "Rotate CW"),
    ROTATE_180(2, "Rotate 180"),
    ROTATE_CCW(3, "Rotate CCW"),
    TILT_UPWARDS(4, "Tilt Upwards"),
    TILT_DOWNWARDS(5, "Tilt Downwards");

    public companion object {

        public fun fromValue(value: Int): PanasonicCameraOrientation? =
            entries.firstOrNull { it.value == value }
    }
}
