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
 * Values of the Panasonic BracketSettings tag.
 */
public enum class PanasonicBracketSettings(
    public val value: Int,
    public val description: String
) {

    NO_BRACKET(0, "No Bracket"),
    THREE_IMAGES_SEQUENCE_0(1, "3 Images, Sequence 0/-/+"),
    THREE_IMAGES_SEQUENCE_MINUS(2, "3 Images, Sequence -/0/+"),
    FIVE_IMAGES_SEQUENCE_0(3, "5 Images, Sequence 0/-/+"),
    FIVE_IMAGES_SEQUENCE_MINUS(4, "5 Images, Sequence -/0/+"),
    SEVEN_IMAGES_SEQUENCE_0(5, "7 Images, Sequence 0/-/+"),
    SEVEN_IMAGES_SEQUENCE_MINUS(6, "7 Images, Sequence -/0/+");

    public companion object {

        public fun fromValue(value: Int): PanasonicBracketSettings? =
            entries.firstOrNull { it.value == value }
    }
}
