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
package de.stefan_oltmann.kim.format.tiff.makernote.olympus

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the RawDevelopment maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#RawDevelopment
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object OlympusRawDevelopmentTag {

    public val RAW_DEV_VERSION: TagInfoByte = TagInfoByte(
        0x0, "RawDevVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_EXPOSURE_BIAS_VALUE: TagInfoByte = TagInfoByte(
        0x100, "RawDevExposureBiasValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_WHITE_BALANCE_VALUE: TagInfoByte = TagInfoByte(
        0x101, "RawDevWhiteBalanceValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_WB_FINE_ADJUSTMENT: TagInfoByte = TagInfoByte(
        0x102, "RawDevWBFineAdjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_GRAY_POINT: TagInfoByte = TagInfoByte(
        0x103, "RawDevGrayPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_SATURATION_EMPHASIS: TagInfoByte = TagInfoByte(
        0x104, "RawDevSaturationEmphasis",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_MEMORY_COLOR_EMPHASIS: TagInfoByte = TagInfoByte(
        0x105, "RawDevMemoryColorEmphasis",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_CONTRAST_VALUE: TagInfoByte = TagInfoByte(
        0x106, "RawDevContrastValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_SHARPNESS_VALUE: TagInfoByte = TagInfoByte(
        0x107, "RawDevSharpnessValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_COLOR_SPACE: TagInfoByte = TagInfoByte(
        0x108, "RawDevColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_ENGINE: TagInfoByte = TagInfoByte(
        0x109, "RawDevEngine",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x10a, "RawDevNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_EDIT_STATUS: TagInfoByte = TagInfoByte(
        0x10b, "RawDevEditStatus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val RAW_DEV_SETTINGS: TagInfoByte = TagInfoByte(
        0x10c, "RawDevSettings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
    )

    public val ALL: List<TagInfo> = listOf(
        RAW_DEV_VERSION,
        RAW_DEV_EXPOSURE_BIAS_VALUE,
        RAW_DEV_WHITE_BALANCE_VALUE,
        RAW_DEV_WB_FINE_ADJUSTMENT,
        RAW_DEV_GRAY_POINT,
        RAW_DEV_SATURATION_EMPHASIS,
        RAW_DEV_MEMORY_COLOR_EMPHASIS,
        RAW_DEV_CONTRAST_VALUE,
        RAW_DEV_SHARPNESS_VALUE,
        RAW_DEV_COLOR_SPACE,
        RAW_DEV_ENGINE,
        RAW_DEV_NOISE_REDUCTION,
        RAW_DEV_EDIT_STATUS,
        RAW_DEV_SETTINGS
    )
}
