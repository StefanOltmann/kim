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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the ShotInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html#ShotInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonShotInfoTag {

    public val SHOT_INFO_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "ShotInfoVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val FIRMWARE_VERSION: TagInfoAscii = TagInfoAscii(
        0x4, "FirmwareVersion", 5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val DISTORTION_CONTROL: TagInfoByte = TagInfoByte(
        0x10, "DistortionControl",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val VR_0X66: TagInfoByte = TagInfoByte(
        0x66, "VR_0x66",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x6a, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val DELETED_IMAGE_COUNT: TagInfoLong = TagInfoLong(
        0x6e, "DeletedImageCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val VIBRATION_REDUCTION: TagInfoByte = TagInfoByte(
        0x75, "VibrationReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val VIBRATION_REDUCTION_2: TagInfoByte = TagInfoByte(
        0x82, "VibrationReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val SHUTTER_COUNT_2: TagInfoUndefineds = TagInfoUndefineds(
        0x157, "ShutterCount", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val VIBRATION_REDUCTION_3: TagInfoByte = TagInfoByte(
        0x1ae, "VibrationReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val SHUTTER_COUNT_3: TagInfoLong = TagInfoLong(
        0x24d, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        SHOT_INFO_VERSION,
        FIRMWARE_VERSION,
        DISTORTION_CONTROL,
        VR_0X66,
        SHUTTER_COUNT,
        DELETED_IMAGE_COUNT,
        VIBRATION_REDUCTION,
        VIBRATION_REDUCTION_2,
        SHUTTER_COUNT_2,
        VIBRATION_REDUCTION_3,
        SHUTTER_COUNT_3
    )
}
