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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the FocusSettings maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FocusSettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object FujiFilmFocusSettingsTag {

    public val FOCUS_MODE2: TagInfoByte = TagInfoByte(
        0x0, "FocusMode2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
        mask = 0x0000000f.toInt()
    )

    public val PRE_AF: TagInfoByte = TagInfoByte(
        0x0, "PreAF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
        mask = 0x00f0.toInt()
    )

    public val AF_AREA_MODE: TagInfoByte = TagInfoByte(
        0x0, "AFAreaMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
        mask = 0x0f00.toInt()
    )

    public val AF_AREA_POINT_SIZE: TagInfoByte = TagInfoByte(
        0x0, "AFAreaPointSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
        mask = 0xf000.toInt()
    )

    public val AF_AREA_ZONE_SIZE: TagInfoByte = TagInfoByte(
        0x0, "AFAreaZoneSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS,
        mask = 0xff0000.toInt()
    )

    public val ALL: List<TagInfo> = listOf(
        FOCUS_MODE2, PRE_AF, AF_AREA_MODE, AF_AREA_POINT_SIZE, AF_AREA_ZONE_SIZE
    )
}
