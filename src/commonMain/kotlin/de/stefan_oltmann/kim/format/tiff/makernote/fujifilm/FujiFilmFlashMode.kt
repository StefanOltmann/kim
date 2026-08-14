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
 * Values of the FujiFilm FlashMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FujiFilm
 */
@Suppress("MaxLineLength")
public enum class FujiFilmFlashMode(
    public val value: Int,
    public val displayName: String
) {

    AUTO(0x0, "Auto"),
    ON(0x1, "On"),
    OFF(0x2, "Off"),
    RED_EYE_REDUCTION(0x3, "Red-eye reduction"),
    EXTERNAL(0x4, "External"),
    COMMANDER(0x10, "Commander"),
    NOT_ATTACHED(0x8000, "Not Attached"),
    TTL(0x8120, "TTL"),
    TTL_AUTO_DID_NOT_FIRE(0x8320, "TTL Auto - Did not fire"),
    MANUAL(0x9840, "Manual"),
    FLASH_COMMANDER(0x9860, "Flash Commander"),
    MULTI_FLASH(0x9880, "Multi-flash"),
    VALUE_1ST_CURTAIN_FRONT(0xa920, "1st Curtain (front)"),
    TTL_SLOW_1ST_CURTAIN_FRONT(0xaa20, "TTL Slow - 1st Curtain (front)"),
    TTL_AUTO_1ST_CURTAIN_FRONT(0xab20, "TTL Auto - 1st Curtain (front)"),
    TTL_RED_EYE_FLASH_1ST_CURTAIN_FRONT(0xad20, "TTL - Red-eye Flash - 1st Curtain (front)"),
    TTL_SLOW_RED_EYE_FLASH_1ST_CURTAIN_FRONT(0xae20, "TTL Slow - Red-eye Flash - 1st Curtain (front)"),
    TTL_AUTO_RED_EYE_FLASH_1ST_CURTAIN_FRONT(0xaf20, "TTL Auto - Red-eye Flash - 1st Curtain (front)"),
    VALUE_2ND_CURTAIN_REAR(0xc920, "2nd Curtain (rear)"),
    TTL_SLOW_2ND_CURTAIN_REAR(0xca20, "TTL Slow - 2nd Curtain (rear)"),
    TTL_AUTO_2ND_CURTAIN_REAR(0xcb20, "TTL Auto - 2nd Curtain (rear)"),
    TTL_RED_EYE_FLASH_2ND_CURTAIN_REAR(0xcd20, "TTL - Red-eye Flash - 2nd Curtain (rear)"),
    TTL_SLOW_RED_EYE_FLASH_2ND_CURTAIN_REAR(0xce20, "TTL Slow - Red-eye Flash - 2nd Curtain (rear)"),
    TTL_AUTO_RED_EYE_FLASH_2ND_CURTAIN_REAR(0xcf20, "TTL Auto - Red-eye Flash - 2nd Curtain (rear)"),
    HIGH_SPEED_SYNC_HSS(0xe920, "High Speed Sync (HSS)");

    public companion object {

        public fun fromValue(value: Int): FujiFilmFlashMode? =
            entries.firstOrNull { it.value == value }
    }
}
