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
 * Values of the Canon AutoISO tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonAutoISO(
    public val value: Int,
    public val displayName: String
) {

    N_A(1, "n/a"),
    OFF(0, "Off"),
    NIGHT_SCENE(1, "Night Scene"),
    ON(2, "On"),
    NONE(3, "None"),
    NONE_MF(0x3000, "None (MF)"),
    RIGHT(0x3001, "Right"),
    CENTER(0x3002, "Center"),
    CENTER_RIGHT(0x3003, "Center+Right"),
    LEFT(0x3004, "Left"),
    LEFT_RIGHT(0x3005, "Left+Right"),
    LEFT_CENTER(0x3006, "Left+Center"),
    ALL(0x3007, "All"),
    ON_2(1, "On"),
    OFF_2(0, "Off"),
    ON_SHOT_1(1, "On (shot 1)"),
    ON_SHOT_2(2, "On (shot 2)"),
    ON_SHOT_3(3, "On (shot 3)"),
    N_A_2(0, "n/a"),
    CAMERA_LOCAL_CONTROL(1, "Camera Local Control"),
    COMPUTER_REMOTE_CONTROL(3, "Computer Remote Control"),
    N_A_3(0, "n/a"),
    EOS_HIGH_END(248, "EOS High-end"),
    COMPACT(250, "Compact"),
    EOS_MID_RANGE(252, "EOS Mid-range"),
    DV_CAMERA(255, "DV Camera"),
    N_A_4(1, "n/a"),
    NONE_2(0, "None"),
    ROTATE_90_CW(1, "Rotate 90 CW"),
    ROTATE_180(2, "Rotate 180"),
    ROTATE_270_CW(3, "Rotate 270 CW"),
    N_A_5(1, "n/a"),
    OFF_3(0, "Off"),
    ON_3(1, "On");

    public companion object {

        public fun fromValue(value: Int): CanonAutoISO? =
            entries.firstOrNull { it.value == value }
    }
}
