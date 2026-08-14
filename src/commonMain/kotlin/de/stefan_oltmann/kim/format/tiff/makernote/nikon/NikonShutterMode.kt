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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

/**
 * Values of the Nikon ShutterMode tag.
 */
public enum class NikonShutterMode(
    public val value: Int,
    public val description: String
) {

    MECHANICAL(0, "Mechanical"),
    ELECTRONIC(16, "Electronic"),
    ELECTRONIC_FRONT_CURTAIN(48, "Electronic Front Curtain"),
    ELECTRONIC_MOVIE(64, "Electronic (Movie)"),
    AUTO_MECHANICAL(80, "Auto (Mechanical)"),
    AUTO_ELECTRONIC_FRONT_CURTAIN(81, "Auto (Electronic Front Curtain)"),
    ELECTRONIC_HIGH_SPEED(96, "Electronic (High Speed)");

    public companion object {

        public fun fromValue(value: Int): NikonShutterMode? =
            entries.firstOrNull { it.value == value }
    }
}
