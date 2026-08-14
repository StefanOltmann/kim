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
 * Values of the Panasonic AFSubjectDetection tag.
 */
public enum class PanasonicAfSubjectDetection(
    public val value: Int,
    public val description: String
) {

    NOT_AVAILABLE(0, "n/a"),
    HUMAN_EYE_FACE_BODY(1, "Human Eye/Face/Body"),
    ANIMAL(2, "Animal"),
    HUMAN_EYE_FACE(3, "Human Eye/Face"),
    ANIMAL_BODY(4, "Animal Body"),
    ANIMAL_EYE_BODY(5, "Animal Eye/Body"),
    CAR(6, "Car"),
    MOTORCYCLE(7, "Motorcycle"),
    CAR_MAIN_PART_PRIORITY(8, "Car (main part priority)"),
    MOTORCYCLE_HELMET_PRIORITY(9, "Motorcycle (helmet priority)"),
    TRAIN(10, "Train"),
    TRAIN_MAIN_PART_PRIORITY(11, "Train (main part priority)"),
    AIRPLANE(12, "Airplane"),
    AIRPLANE_NOSE_PRIORITY(13, "Airplane (nose priority)");

    public companion object {

        public fun fromValue(value: Int): PanasonicAfSubjectDetection? =
            entries.firstOrNull { it.value == value }
    }
}
