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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

/**
 * Values of the Sony LensSpec tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html
 */
@Suppress("MaxLineLength")
public enum class SonyLensSpec(
    public val value: Int,
    public val displayName: String
) {

    AUTO(0, "Auto"),
    CENTER(1, "Center"),
    TOP(2, "Top"),
    UPPER_RIGHT(3, "Upper-right"),
    RIGHT(4, "Right"),
    LOWER_RIGHT(5, "Lower-right"),
    BOTTOM(6, "Bottom"),
    LOWER_LEFT(7, "Lower-left"),
    LEFT(8, "Left"),
    UPPER_LEFT(9, "Upper-left"),
    MANUAL(0, "Manual"),
    AF_S(1, "AF-S"),
    AF_C(2, "AF-C"),
    AF_A(3, "AF-A"),
    TOP_RIGHT(0, "Top-right"),
    BOTTOM_RIGHT(1, "Bottom-right"),
    BOTTOM_2(2, "Bottom"),
    MIDDLE_HORIZONTAL(3, "Middle Horizontal"),
    CENTER_VERTICAL(4, "Center Vertical"),
    TOP_2(5, "Top"),
    TOP_LEFT(6, "Top-left"),
    BOTTOM_LEFT(7, "Bottom-left"),
    MANUAL_NOT_CONFIRMED_0(0, "Manual - Not confirmed (0)"),
    MANUAL_NOT_CONFIRMED_4(4, "Manual - Not confirmed (4)"),
    AF_C_CONFIRMED(16, "AF-C - Confirmed"),
    AF_C_NOT_CONFIRMED(24, "AF-C - Not Confirmed"),
    AF_S_CONFIRMED(64, "AF-S - Confirmed"),
    OUT_OF_FOCUS(32768, "Out of Focus"),
    IN_FOCUS(0, "In Focus"),
    AUTO_2(0, "Auto"),
    CENTER_2(1, "Center"),
    TOP_3(2, "Top"),
    UPPER_RIGHT_2(3, "Upper-right"),
    RIGHT_2(4, "Right"),
    LOWER_RIGHT_2(5, "Lower-right"),
    BOTTOM_3(6, "Bottom"),
    LOWER_LEFT_2(7, "Lower-left"),
    LEFT_2(8, "Left"),
    UPPER_LEFT_2(9, "Upper-left"),
    FAR_RIGHT(10, "Far Right"),
    FAR_LEFT(11, "Far Left"),
    UPPER_MIDDLE(12, "Upper-middle"),
    NEAR_RIGHT(13, "Near Right"),
    LOWER_MIDDLE(14, "Lower-middle"),
    NEAR_LEFT(15, "Near Left"),
    MANUAL_2(0, "Manual"),
    AF_S_2(1, "AF-S"),
    AF_C_2(2, "AF-C"),
    AF_A_2(3, "AF-A"),
    OUT_OF_FOCUS_2(32768, "Out of Focus"),
    IN_FOCUS_2(0, "In Focus"),
    OUT_OF_FOCUS_3(32768, "Out of Focus"),
    IN_FOCUS_3(0, "In Focus"),
    UPPER_LEFT_3(0, "Upper-left"),
    LEFT_3(1, "Left"),
    LOWER_LEFT_3(2, "Lower-left"),
    FAR_LEFT_2(3, "Far Left"),
    TOP_HORIZONTAL(4, "Top (horizontal)"),
    NEAR_RIGHT_2(5, "Near Right"),
    CENTER_HORIZONTAL(6, "Center (horizontal)"),
    NEAR_LEFT_2(7, "Near Left"),
    BOTTOM_HORIZONTAL(8, "Bottom (horizontal)"),
    TOP_VERTICAL(9, "Top (vertical)"),
    CENTER_VERTICAL_2(10, "Center (vertical)"),
    BOTTOM_VERTICAL(11, "Bottom (vertical)"),
    FAR_RIGHT_2(12, "Far Right"),
    UPPER_RIGHT_3(13, "Upper-right"),
    RIGHT_3(14, "Right"),
    LOWER_RIGHT_3(15, "Lower-right"),
    UPPER_MIDDLE_2(16, "Upper-middle"),
    LOWER_MIDDLE_2(17, "Lower-middle"),
    NONE(255, "(none)"),
    OUT_OF_FOCUS_4(32768, "Out of Focus"),
    IN_FOCUS_4(0, "In Focus"),
    OUT_OF_FOCUS_5(32768, "Out of Focus"),
    IN_FOCUS_5(0, "In Focus"),
    OUT_OF_FOCUS_6(32768, "Out of Focus"),
    IN_FOCUS_6(0, "In Focus"),
    OUT_OF_FOCUS_7(32768, "Out of Focus"),
    IN_FOCUS_7(0, "In Focus"),
    OUT_OF_FOCUS_8(32768, "Out of Focus"),
    IN_FOCUS_8(0, "In Focus"),
    OUT_OF_FOCUS_9(32768, "Out of Focus"),
    IN_FOCUS_9(0, "In Focus"),
    OUT_OF_FOCUS_10(32768, "Out of Focus"),
    IN_FOCUS_10(0, "In Focus"),
    OUT_OF_FOCUS_11(32768, "Out of Focus"),
    IN_FOCUS_11(0, "In Focus"),
    OUT_OF_FOCUS_12(32768, "Out of Focus"),
    IN_FOCUS_12(0, "In Focus"),
    OUT_OF_FOCUS_13(32768, "Out of Focus"),
    IN_FOCUS_13(0, "In Focus");

    public companion object {

        public fun fromValue(value: Int): SonyLensSpec? =
            entries.firstOrNull { it.value == value }
    }
}
