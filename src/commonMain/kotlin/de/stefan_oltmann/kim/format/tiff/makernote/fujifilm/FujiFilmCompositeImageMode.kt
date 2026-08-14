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
 * Values of the FujiFilm CompositeImageMode tag.
 */
public enum class FujiFilmCompositeImageMode(
    public val value: Int,
    public val description: String
) {

    NOT_AVAILABLE(0, "n/a"),
    PRO_LOW_LIGHT(1, "Pro Low-light"),
    PRO_FOCUS(2, "Pro Focus"),
    PANORAMA(32, "Panorama"),
    HDR(128, "HDR"),
    MULTI_EXPOSURE(1024, "Multi-exposure");

    public companion object {

        public fun fromValue(value: Int): FujiFilmCompositeImageMode? =
            entries.firstOrNull { it.value == value }
    }
}
