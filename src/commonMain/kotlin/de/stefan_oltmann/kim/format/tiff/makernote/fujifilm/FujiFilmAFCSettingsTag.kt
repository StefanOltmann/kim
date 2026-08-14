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

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong

/**
 * Tags of the AFCSettings maker note sub-directory.
 *
 * The AF-C setting is a single integer whose lower nibbles carry the
 * three sensitivity settings, like ExifTool reads it with bit masks.
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#AFCSettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object FujiFilmAFCSettingsTag {

    public val AF_C_SETTING: TagInfoLong = TagInfoLong(
        0x0, "AF-CSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_AFC_SETTINGS
    )

    public val AF_C_TRACKING_SENSITIVITY: TagInfoLong = TagInfoLong(
        0x0, "AF-CTrackingSensitivity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_AFC_SETTINGS,
        mask = 0x0f
    )

    public val AF_C_SPEED_TRACKING_SENSITIVITY: TagInfoLong = TagInfoLong(
        0x0, "AF-CSpeedTrackingSensitivity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_AFC_SETTINGS,
        mask = 0xf0
    )

    public val AF_C_ZONE_AREA_SWITCHING: TagInfoLong = TagInfoLong(
        0x0, "AF-CZoneAreaSwitching",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_AFC_SETTINGS,
        mask = 0x0f00
    )

    public val ALL: List<TagInfo> = listOf(
        AF_C_SETTING, AF_C_TRACKING_SENSITIVITY, AF_C_SPEED_TRACKING_SENSITIVITY, AF_C_ZONE_AREA_SWITCHING
    )
}
