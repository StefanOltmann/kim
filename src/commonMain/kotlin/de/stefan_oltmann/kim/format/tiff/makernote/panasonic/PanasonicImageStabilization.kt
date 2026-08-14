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
 * Values of the Panasonic ImageStabilization tag.
 */
public enum class PanasonicImageStabilization(
    public val value: Int,
    public val description: String
) {

    ON_OPTICAL(2, "On, Optical"),
    OFF(3, "Off"),
    ON_MODE_2(4, "On, Mode 2"),
    ON_OPTICAL_PANNING(5, "On, Optical Panning"),
    ON_BODY_ONLY(6, "On, Body-only"),
    ON_BODY_ONLY_PANNING(7, "On, Body-only Panning"),
    DUAL_IS(9, "Dual IS"),
    DUAL_IS_PANNING(10, "Dual IS Panning"),
    DUAL2_IS(11, "Dual2 IS"),
    DUAL2_IS_PANNING(12, "Dual2 IS Panning");

    public companion object {

        public fun fromValue(value: Int): PanasonicImageStabilization? =
            entries.firstOrNull { it.value == value }
    }
}
