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
 * Tags of the RawDev2 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#RawDev2
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object OlympusRawDevelopment2Tag {

    public val RAW_DEV_VERSION: TagInfoByte = TagInfoByte(
        0x0, "RawDevVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_EXPOSURE_BIAS_VALUE: TagInfoByte = TagInfoByte(
        0x100, "RawDevExposureBiasValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_WHITE_BALANCE: TagInfoByte = TagInfoByte(
        0x101, "RawDevWhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_WHITE_BALANCE_VALUE: TagInfoByte = TagInfoByte(
        0x102, "RawDevWhiteBalanceValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_WB_FINE_ADJUSTMENT: TagInfoByte = TagInfoByte(
        0x103, "RawDevWBFineAdjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_GRAY_POINT: TagInfoByte = TagInfoByte(
        0x104, "RawDevGrayPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_CONTRAST_VALUE: TagInfoByte = TagInfoByte(
        0x105, "RawDevContrastValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_SHARPNESS_VALUE: TagInfoByte = TagInfoByte(
        0x106, "RawDevSharpnessValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_SATURATION_EMPHASIS: TagInfoByte = TagInfoByte(
        0x107, "RawDevSaturationEmphasis",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_MEMORY_COLOR_EMPHASIS: TagInfoByte = TagInfoByte(
        0x108, "RawDevMemoryColorEmphasis",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_COLOR_SPACE: TagInfoByte = TagInfoByte(
        0x109, "RawDevColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x10a, "RawDevNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_ENGINE: TagInfoByte = TagInfoByte(
        0x10b, "RawDevEngine",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PICTURE_MODE: TagInfoByte = TagInfoByte(
        0x10c, "RawDevPictureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_SATURATION: TagInfoByte = TagInfoByte(
        0x10d, "RawDevPMSaturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_CONTRAST: TagInfoByte = TagInfoByte(
        0x10e, "RawDevPMContrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_SHARPNESS: TagInfoByte = TagInfoByte(
        0x10f, "RawDevPMSharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_BW_FILTER: TagInfoByte = TagInfoByte(
        0x110, "RawDevPM_BWFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_PICTURE_TONE: TagInfoByte = TagInfoByte(
        0x111, "RawDevPMPictureTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_GRADATION: TagInfoByte = TagInfoByte(
        0x112, "RawDevGradation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_SATURATION3: TagInfoByte = TagInfoByte(
        0x113, "RawDevSaturation3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_AUTO_GRADATION: TagInfoByte = TagInfoByte(
        0x119, "RawDevAutoGradation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_PM_NOISE_FILTER: TagInfoByte = TagInfoByte(
        0x120, "RawDevPMNoiseFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_ART_FILTER: TagInfoByte = TagInfoByte(
        0x121, "RawDevArtFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val RAW_DEV_SUB_IFD: TagInfoByte = TagInfoByte(
        0x8000, "RawDevSubIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2
    )

    public val ALL: List<TagInfo> = listOf(
        RAW_DEV_VERSION,
        RAW_DEV_EXPOSURE_BIAS_VALUE,
        RAW_DEV_WHITE_BALANCE,
        RAW_DEV_WHITE_BALANCE_VALUE,
        RAW_DEV_WB_FINE_ADJUSTMENT,
        RAW_DEV_GRAY_POINT,
        RAW_DEV_CONTRAST_VALUE,
        RAW_DEV_SHARPNESS_VALUE,
        RAW_DEV_SATURATION_EMPHASIS,
        RAW_DEV_MEMORY_COLOR_EMPHASIS,
        RAW_DEV_COLOR_SPACE,
        RAW_DEV_NOISE_REDUCTION,
        RAW_DEV_ENGINE,
        RAW_DEV_PICTURE_MODE,
        RAW_DEV_PM_SATURATION,
        RAW_DEV_PM_CONTRAST,
        RAW_DEV_PM_SHARPNESS,
        RAW_DEV_PM_BW_FILTER,
        RAW_DEV_PM_PICTURE_TONE,
        RAW_DEV_GRADATION,
        RAW_DEV_SATURATION3,
        RAW_DEV_AUTO_GRADATION,
        RAW_DEV_PM_NOISE_FILTER,
        RAW_DEV_ART_FILTER,
        RAW_DEV_SUB_IFD
    )
}
