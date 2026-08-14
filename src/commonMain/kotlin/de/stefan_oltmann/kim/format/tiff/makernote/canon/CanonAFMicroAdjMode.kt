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
 * Values of the Canon AFMicroAdjMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAFMicroAdjMode(
    public val value: Int,
    public val displayName: String
) {

    DISABLE(0, "Disable"),
    ADJUST_ALL_BY_THE_SAME_AMOUNT(1, "Adjust all by the same amount"),
    ADJUST_BY_LENS(2, "Adjust by lens");

    public companion object {

        public fun fromValue(value: Int): CanonAFMicroAdjMode? =
            entries.firstOrNull { it.value == value }
    }
}
