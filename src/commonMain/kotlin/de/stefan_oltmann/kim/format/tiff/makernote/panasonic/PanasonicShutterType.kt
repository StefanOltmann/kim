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
 * Values of the Panasonic ShutterType tag.
 */
public enum class PanasonicShutterType(
    public val value: Int,
    public val description: String
) {

    MECHANICAL(0, "Mechanical"),
    ELECTRONIC(1, "Electronic"),
    HYBRID(2, "Hybrid");

    public companion object {

        public fun fromValue(value: Int): PanasonicShutterType? =
            entries.firstOrNull { it.value == value }
    }
}
