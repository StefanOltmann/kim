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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the FileInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FileInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonFileInfoTag {

    public val FILE_INFO_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "FileInfoVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FILE_INFO
    )

    public val MEMORY_CARD_NUMBER: TagInfoShort = TagInfoShort(
        0x2, "MemoryCardNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FILE_INFO
    )

    public val DIRECTORY_NUMBER: TagInfoShort = TagInfoShort(
        0x3, "DirectoryNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FILE_INFO
    )

    public val FILE_NUMBER: TagInfoShort = TagInfoShort(
        0x4, "FileNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FILE_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        FILE_INFO_VERSION, MEMORY_CARD_NUMBER, DIRECTORY_NUMBER, FILE_NUMBER
    )
}
