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
 * Values of the Panasonic SelfTimer tag.
 */
public enum class PanasonicSelfTimer(
    public val value: Int,
    public val description: String
) {

    OFF_0(0, "Off (0)"),
    OFF(1, "Off"),
    TEN_SECONDS(2, "10 s"),
    TWO_SECONDS(3, "2 s"),
    TEN_SECONDS_3_PICTURES(4, "10 s / 3 pictures"),
    TWO_SECONDS_AFTER_SHUTTER_PRESSED(258, "2 s after shutter pressed"),
    TEN_SECONDS_AFTER_SHUTTER_PRESSED(266, "10 s after shutter pressed"),
    THREE_PHOTOS_AFTER_10_S(778, "3 photos after 10 s");

    public companion object {

        public fun fromValue(value: Int): PanasonicSelfTimer? =
            entries.firstOrNull { it.value == value }
    }
}
