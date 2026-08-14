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
 * Values of the Sony FocusMode tag (0xb04e).
 */
public enum class SonyFocusMode3(
    public val value: Int,
    public val description: String
) {

    MANUAL(0, "Manual"),
    AF_S(2, "AF-S"),
    AF_C(3, "AF-C"),
    SEMI_MANUAL(5, "Semi-manual"),
    DMF(6, "DMF");

    public companion object {

        public fun fromValue(value: Int): SonyFocusMode3? =
            entries.firstOrNull { it.value == value }
    }
}
