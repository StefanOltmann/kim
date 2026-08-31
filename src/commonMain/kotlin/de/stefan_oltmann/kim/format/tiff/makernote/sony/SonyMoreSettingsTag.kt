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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte

/**
 * Tags of the MoreSettings maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#MoreSettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object SonyMoreSettingsTag {

    public val DRIVE_MODE2: TagInfoByte = TagInfoByte(
        0x1, "DriveMode2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val EXPOSURE_PROGRAM: TagInfoByte = TagInfoByte(
        0x2, "ExposureProgram",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val METERING_MODE: TagInfoByte = TagInfoByte(
        0x3, "MeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val DYNAMIC_RANGE_OPTIMIZER_SETTING: TagInfoByte = TagInfoByte(
        0x4, "DynamicRangeOptimizerSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val COLOR_SPACE: TagInfoByte = TagInfoByte(
        0x6, "ColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val CREATIVE_STYLE_SETTING: TagInfoByte = TagInfoByte(
        0x7, "CreativeStyleSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val CONTRAST_SETTING: TagInfoSByte = TagInfoSByte(
        0x8, "ContrastSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val SATURATION_SETTING: TagInfoSByte = TagInfoSByte(
        0x9, "SaturationSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val SHARPNESS_SETTING: TagInfoSByte = TagInfoSByte(
        0xa, "SharpnessSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val WHITE_BALANCE_SETTING: TagInfoByte = TagInfoByte(
        0xd, "WhiteBalanceSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val COLOR_TEMPERATURE_SETTING: TagInfoByte = TagInfoByte(
        0xe, "ColorTemperatureSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val COLOR_COMPENSATION_FILTER_SET: TagInfoSByte = TagInfoSByte(
        0xf, "ColorCompensationFilterSet",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val FLASH_MODE: TagInfoByte = TagInfoByte(
        0x10, "FlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x11, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val HIGH_ISO_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x12, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val FOCUS_MODE: TagInfoByte = TagInfoByte(
        0x13, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val MULTI_FRAME_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x15, "MultiFrameNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val HDR_SETTING: TagInfoByte = TagInfoByte(
        0x16, "HDRSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val HDR_LEVEL: TagInfoByte = TagInfoByte(
        0x17, "HDRLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val VIEWING_MODE: TagInfoByte = TagInfoByte(
        0x18, "ViewingMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val FACE_DETECTION: TagInfoByte = TagInfoByte(
        0x19, "FaceDetection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS
    )

    public val ALL: List<TagInfo> = listOf(
        DRIVE_MODE2,
        EXPOSURE_PROGRAM,
        METERING_MODE,
        DYNAMIC_RANGE_OPTIMIZER_SETTING,
        COLOR_SPACE,
        CREATIVE_STYLE_SETTING,
        CONTRAST_SETTING,
        SATURATION_SETTING,
        SHARPNESS_SETTING,
        WHITE_BALANCE_SETTING,
        COLOR_TEMPERATURE_SETTING,
        COLOR_COMPENSATION_FILTER_SET,
        FLASH_MODE,
        LONG_EXPOSURE_NOISE_REDUCTION,
        HIGH_ISO_NOISE_REDUCTION,
        FOCUS_MODE,
        MULTI_FRAME_NOISE_REDUCTION,
        HDR_SETTING,
        HDR_LEVEL,
        VIEWING_MODE,
        FACE_DETECTION
    )
}
