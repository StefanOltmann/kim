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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the ShotInfoD5100 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html#ShotInfoD5100
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonShotInfoD5100Tag {

    public val SHOT_INFO_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "ShotInfoVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val FIRMWARE_VERSION: TagInfoAscii = TagInfoAscii(
        0x4, "FirmwareVersion", 5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x321, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val CUSTOM_SETTINGS_D5100: TagInfoUndefineds = TagInfoUndefineds(
        0x407, "CustomSettingsD5100", 34,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        SHOT_INFO_VERSION, FIRMWARE_VERSION, SHUTTER_COUNT, CUSTOM_SETTINGS_D5100
    )
}
