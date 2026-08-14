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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the FlashInfo0107 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html#FlashInfo0107
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonFlashInfo0107Tag {

    public val FLASH_INFO_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "FlashInfoVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_SOURCE: TagInfoByte = TagInfoByte(
        0x4, "FlashSource",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val EXTERNAL_FLASH_FIRMWARE: TagInfoBytes = TagInfoBytes(
        0x6, "ExternalFlashFirmware", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_COMPENSATION: TagInfoSByte = TagInfoSByte(
        0xa, "FlashCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0xc, "FlashFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val REPEATING_FLASH_RATE: TagInfoByte = TagInfoByte(
        0xd, "RepeatingFlashRate",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val REPEATING_FLASH_COUNT: TagInfoByte = TagInfoByte(
        0xe, "RepeatingFlashCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_GN_DISTANCE: TagInfoByte = TagInfoByte(
        0xf, "FlashGNDistance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_GROUP_A_COMPENSATION: TagInfoSByte = TagInfoSByte(
        0x28, "FlashGroupACompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_GROUP_B_COMPENSATION: TagInfoSByte = TagInfoSByte(
        0x29, "FlashGroupBCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val FLASH_GROUP_C_COMPENSATION: TagInfoSByte = TagInfoSByte(
        0x2a, "FlashGroupCCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        FLASH_INFO_VERSION,
        FLASH_SOURCE,
        EXTERNAL_FLASH_FIRMWARE,
        FLASH_COMPENSATION,
        FLASH_FOCAL_LENGTH,
        REPEATING_FLASH_RATE,
        REPEATING_FLASH_COUNT,
        FLASH_GN_DISTANCE,
        FLASH_GROUP_A_COMPENSATION,
        FLASH_GROUP_B_COMPENSATION,
        FLASH_GROUP_C_COMPENSATION
    )
}
