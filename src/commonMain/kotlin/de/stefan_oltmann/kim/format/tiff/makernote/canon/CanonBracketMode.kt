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
 * Values of the Canon BracketMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonBracketMode(
    public val value: Int,
    public val displayName: String
) {

    OFF(0, "Off"),
    AEB(1, "AEB"),
    FEB(2, "FEB"),
    ISO(3, "ISO"),
    WB(4, "WB");

    public companion object {

        public fun fromValue(value: Int): CanonBracketMode? =
            entries.firstOrNull { it.value == value }
    }
}
