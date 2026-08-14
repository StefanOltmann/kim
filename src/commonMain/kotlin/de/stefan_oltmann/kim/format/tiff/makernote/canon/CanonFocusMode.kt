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
 * Values of the Canon FocusMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonFocusMode(
    public val value: Int,
    public val displayName: String
) {

    ONE_SHOT_AF(0, "One-shot AF"),
    AI_SERVO_AF(1, "AI Servo AF"),
    AI_FOCUS_AF(2, "AI Focus AF"),
    MANUAL_FOCUS_3(3, "Manual Focus (3)"),
    SINGLE(4, "Single"),
    CONTINUOUS(5, "Continuous"),
    MANUAL_FOCUS_6(6, "Manual Focus (6)"),
    PAN_FOCUS(16, "Pan Focus"),
    ONE_SHOT_AF_LIVE_VIEW(256, "One-shot AF (Live View)"),
    AI_SERVO_AF_LIVE_VIEW(257, "AI Servo AF (Live View)"),
    AI_FOCUS_AF_LIVE_VIEW(258, "AI Focus AF (Live View)"),
    MOVIE_SNAP_FOCUS(512, "Movie Snap Focus"),
    MOVIE_SERVO_AF(519, "Movie Servo AF");

    public companion object {

        public fun fromValue(value: Int): CanonFocusMode? =
            entries.firstOrNull { it.value == value }
    }
}
