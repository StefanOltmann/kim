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
 * Values of the Panasonic AFAssistLamp tag.
 */
public enum class PanasonicAfAssistLamp(
    public val value: Int,
    public val description: String
) {

    FIRED(1, "Fired"),
    ENABLED_BUT_NOT_USED(2, "Enabled but Not Used"),
    DISABLED_BUT_REQUIRED(3, "Disabled but Required"),
    DISABLED_AND_NOT_REQUIRED(4, "Disabled and Not Required");

    public companion object {

        public fun fromValue(value: Int): PanasonicAfAssistLamp? =
            entries.firstOrNull { it.value == value }
    }
}
