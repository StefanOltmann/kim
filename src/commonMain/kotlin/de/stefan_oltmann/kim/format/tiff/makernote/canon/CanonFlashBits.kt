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
 * Values of the Canon FlashBits tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonFlashBits(
    public val value: Int,
    public val displayName: String
) {

    NONE(0x0, "(none)"),
    MANUAL(0, "Manual"),
    TTL(1, "TTL"),
    A_TTL(2, "A-TTL"),
    E_TTL(3, "E-TTL"),
    FP_SYNC_ENABLED(4, "FP sync enabled"),
    VALUE_2ND_CURTAIN_SYNC_USED(7, "2nd-curtain sync used"),
    FP_SYNC_USED(11, "FP sync used"),
    BUILT_IN(13, "Built-in"),
    EXTERNAL(14, "External");

    public companion object {

        public fun fromValue(value: Int): CanonFlashBits? =
            entries.firstOrNull { it.value == value }
    }
}
