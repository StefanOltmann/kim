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
 * Values of the FujiFilm FaceElementType tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmFaceElementType(
    public val value: Int,
    public val displayName: String
) {

    FACE(0x1, "Face"),
    LEFT_EYE(0x2, "Left Eye"),
    RIGHT_EYE(0x3, "Right Eye"),
    BODY(0x7, "Body"),
    HEAD(0x8, "Head"),
    BOTH_EYES(0x9, "Both Eyes"),
    BIKE(0xb, "Bike"),
    BODY_OF_CAR(0xc, "Body of Car"),
    FRONT_OF_CAR(0xd, "Front of Car"),
    ANIMAL_BODY(0xe, "Animal Body"),
    ANIMAL_HEAD(0xf, "Animal Head"),
    ANIMAL_FACE(0x10, "Animal Face"),
    ANIMAL_LEFT_EYE(0x11, "Animal Left Eye"),
    ANIMAL_RIGHT_EYE(0x12, "Animal Right Eye"),
    BIRD_BODY(0x13, "Bird Body"),
    BIRD_HEAD(0x14, "Bird Head"),
    BIRD_LEFT_EYE(0x15, "Bird Left Eye"),
    BIRD_RIGHT_EYE(0x16, "Bird Right Eye"),
    AIRCRAFT_BODY(0x17, "Aircraft Body"),
    AIRCRAFT_COCKPIT(0x19, "Aircraft Cockpit"),
    TRAIN_FRONT(0x1a, "Train Front"),
    TRAIN_COCKPIT(0x1b, "Train Cockpit"),
    ANIMAL_HEAD_28(0x1c, "Animal Head (28)"),
    ANIMAL_BODY_29(0x1d, "Animal Body (29)");

    public companion object {

        public fun fromValue(value: Int): FujiFilmFaceElementType? =
            entries.firstOrNull { it.value == value }
    }
}
