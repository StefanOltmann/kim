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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort

/**
 * Tags of the WorldTime maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#WorldTime
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonWorldTimeTag {

    public val TIME_ZONE: TagInfoSShort = TagInfoSShort(
        0x0, "TimeZone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_WORLD_TIME
    )

    public val DAYLIGHT_SAVINGS: TagInfoByte = TagInfoByte(
        0x2, "DaylightSavings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_WORLD_TIME
    )

    public val DATE_DISPLAY_FORMAT: TagInfoByte = TagInfoByte(
        0x3, "DateDisplayFormat",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_WORLD_TIME
    )

    public val ALL: List<TagInfo> = listOf(
        TIME_ZONE, DAYLIGHT_SAVINGS, DATE_DISPLAY_FORMAT
    )
}
