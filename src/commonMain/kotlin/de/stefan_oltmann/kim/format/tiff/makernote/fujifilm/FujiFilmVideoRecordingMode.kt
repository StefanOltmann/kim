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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

/**
 * Values of the FujiFilm VideoRecordingMode tag.
 */
public enum class FujiFilmVideoRecordingMode(
    public val value: Int,
    public val description: String
) {

    NORMAL(0x0, "Normal"),
    F_LOG(0x10, "F-log"),
    HLG(0x20, "HLG"),
    F_LOG2(0x30, "F-log2");

    public companion object {

        public fun fromValue(value: Int): FujiFilmVideoRecordingMode? =
            entries.firstOrNull { it.value == value }
    }
}
