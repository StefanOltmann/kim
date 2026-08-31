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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort

/**
 * Tags of the ISOInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#ISOInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonIsoInfoTag {

    public val ISO: TagInfoByte = TagInfoByte(
        0x0, "ISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_ISO_INFO
    )

    public val ISO_EXPANSION: TagInfoShort = TagInfoShort(
        0x4, "ISOExpansion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_ISO_INFO
    )

    public val ISO2: TagInfoByte = TagInfoByte(
        0x6, "ISO2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_ISO_INFO
    )

    public val ISO_EXPANSION2: TagInfoShort = TagInfoShort(
        0xa, "ISOExpansion2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_ISO_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        ISO, ISO_EXPANSION, ISO2, ISO_EXPANSION2
    )
}
