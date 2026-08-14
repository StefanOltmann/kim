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
 * Values of the Sony FlashAction tag.
 */
public enum class SonyFlashAction(
    public val value: Int,
    public val description: String
) {

    DID_NOT_FIRE(0, "Did not fire"),
    FLASH_FIRED(1, "Flash Fired"),
    EXTERNAL_FLASH_FIRED(2, "External Flash Fired"),
    WIRELESS_CONTROLLED_FLASH_FIRED(3, "Wireless Controlled Flash Fired");

    public companion object {

        public fun fromValue(value: Int): SonyFlashAction? =
            entries.firstOrNull { it.value == value }
    }
}
