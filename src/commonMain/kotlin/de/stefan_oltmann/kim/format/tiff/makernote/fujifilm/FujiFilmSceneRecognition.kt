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
 * Values of the FujiFilm SceneRecognition tag.
 */
public enum class FujiFilmSceneRecognition(
    public val value: Int,
    public val description: String
) {

    UNRECOGNIZED(0x0, "Unrecognized"),
    PORTRAIT_IMAGE(0x100, "Portrait Image"),
    NIGHT_PORTRAIT(0x103, "Night Portrait"),
    BACKLIT_PORTRAIT(0x105, "Backlit Portrait"),
    LANDSCAPE_IMAGE(0x200, "Landscape Image"),
    NIGHT_SCENE(0x300, "Night Scene"),
    MACRO(0x400, "Macro");

    public companion object {

        public fun fromValue(value: Int): FujiFilmSceneRecognition? =
            entries.firstOrNull { it.value == value }
    }
}
