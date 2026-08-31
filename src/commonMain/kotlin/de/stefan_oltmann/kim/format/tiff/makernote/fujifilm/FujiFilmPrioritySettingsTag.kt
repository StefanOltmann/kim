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
 * Tags of the PrioritySettings maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#PrioritySettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object FujiFilmPrioritySettingsTag {

    public val AF_S_PRIORITY: TagInfoByte = TagInfoByte(
        0x0, "AF-SPriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS,
        mask = 0x000f.toInt()
    )

    public val AF_C_PRIORITY: TagInfoByte = TagInfoByte(
        0x0, "AF-CPriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS,
        mask = 0x00f0.toInt()
    )

    public val ALL: List<TagInfo> = listOf(
        AF_S_PRIORITY, AF_C_PRIORITY
    )
}
