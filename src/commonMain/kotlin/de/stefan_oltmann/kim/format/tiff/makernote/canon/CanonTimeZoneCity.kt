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
 * Values of the Canon TimeZoneCity tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonTimeZoneCity(
    public val value: Int,
    public val displayName: String
) {

    N_A(0, "n/a"),
    CHATHAM_ISLANDS(1, "Chatham Islands"),
    WELLINGTON(2, "Wellington"),
    SOLOMON_ISLANDS(3, "Solomon Islands"),
    SYDNEY(4, "Sydney"),
    ADELAIDE(5, "Adelaide"),
    TOKYO(6, "Tokyo"),
    HONG_KONG(7, "Hong Kong"),
    BANGKOK(8, "Bangkok"),
    YANGON(9, "Yangon"),
    DHAKA(10, "Dhaka"),
    KATHMANDU(11, "Kathmandu"),
    DELHI(12, "Delhi"),
    KARACHI(13, "Karachi"),
    KABUL(14, "Kabul"),
    DUBAI(15, "Dubai"),
    TEHRAN(16, "Tehran"),
    MOSCOW(17, "Moscow"),
    CAIRO(18, "Cairo"),
    PARIS(19, "Paris"),
    LONDON(20, "London"),
    AZORES(21, "Azores"),
    FERNANDO_DE_NORONHA(22, "Fernando de Noronha"),
    SAO_PAULO(23, "Sao Paulo"),
    NEWFOUNDLAND(24, "Newfoundland"),
    SANTIAGO(25, "Santiago"),
    CARACAS(26, "Caracas"),
    NEW_YORK(27, "New York"),
    CHICAGO(28, "Chicago"),
    DENVER(29, "Denver"),
    LOS_ANGELES(30, "Los Angeles"),
    ANCHORAGE(31, "Anchorage"),
    HONOLULU(32, "Honolulu"),
    SAMOA(33, "Samoa"),
    NOT_SET(32766, "(not set)");

    public companion object {

        public fun fromValue(value: Int): CanonTimeZoneCity? =
            entries.firstOrNull { it.value == value }
    }
}
