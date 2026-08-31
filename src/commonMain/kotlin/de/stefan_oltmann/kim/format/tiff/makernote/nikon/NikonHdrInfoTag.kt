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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the HDRInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#HDRInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonHdrInfoTag {

    public val HDR_INFO_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "HDRInfoVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO
    )

    public val HDR: TagInfoByte = TagInfoByte(
        0x4, "HDR",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO
    )

    public val HDR_LEVEL: TagInfoByte = TagInfoByte(
        0x5, "HDRLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO
    )

    public val HDR_SMOOTHING: TagInfoByte = TagInfoByte(
        0x6, "HDRSmoothing",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO
    )

    public val HDR_LEVEL2: TagInfoByte = TagInfoByte(
        0x7, "HDRLevel2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        HDR_INFO_VERSION, HDR, HDR_LEVEL, HDR_SMOOTHING, HDR_LEVEL2
    )
}
