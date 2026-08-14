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
 * Values of the Canon AutoExposureBracketing tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAutoExposureBracketing(
    public val value: Int,
    public val displayName: String
) {

    ON(1, "On"),
    OFF(0, "Off"),
    ON_SHOT_1(1, "On (shot 1)"),
    ON_SHOT_2(2, "On (shot 2)"),
    ON_SHOT_3(3, "On (shot 3)");

    public companion object {

        public fun fromValue(value: Int): CanonAutoExposureBracketing? =
            entries.firstOrNull { it.value == value }
    }
}
