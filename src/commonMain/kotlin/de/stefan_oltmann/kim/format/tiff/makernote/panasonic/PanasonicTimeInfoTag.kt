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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the TimeInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#TimeInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object PanasonicTimeInfoTag {

    public val PANASONIC_DATE_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "PanasonicDateTime", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_TIME_INFO
    )

    public val TIME_LAPSE_SHOT_NUMBER: TagInfoLong = TagInfoLong(
        0x10, "TimeLapseShotNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_TIME_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        PANASONIC_DATE_TIME, TIME_LAPSE_SHOT_NUMBER
    )
}
