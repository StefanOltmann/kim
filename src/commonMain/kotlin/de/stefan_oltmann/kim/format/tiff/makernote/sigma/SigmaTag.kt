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
package de.stefan_oltmann.kim.format.tiff.makernote.sigma

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Sigma MakerNote tags, generated from the ExifTool 13.59 reference
 * dumps of the corpus files, so the names match the reference decoder
 * exactly.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sigma.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object SigmaTag {

    public val SERIAL_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val DRIVE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0003, "DriveMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val RESOLUTION_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0004, "ResolutionMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val A_F_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0005, "AFMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val FOCUS_SETTING: TagInfoUndefineds = TagInfoUndefineds(
        0x0006, "FocusSetting", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val WHITE_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x0007, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val EXPOSURE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0008, "ExposureMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val METERING_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0009, "MeteringMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val LENS_FOCAL_RANGE: TagInfoUndefineds = TagInfoUndefineds(
        0x000a, "LensFocalRange", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val COLOR_SPACE: TagInfoUndefineds = TagInfoUndefineds(
        0x000b, "ColorSpace", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val EXPOSURE_ADJUST: TagInfoUndefineds = TagInfoUndefineds(
        0x000c, "ExposureAdjust", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val CONTRAST: TagInfoUndefineds = TagInfoUndefineds(
        0x000d, "Contrast", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SHADOW: TagInfoUndefineds = TagInfoUndefineds(
        0x000e, "Shadow", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val HIGHLIGHT: TagInfoUndefineds = TagInfoUndefineds(
        0x000f, "Highlight", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SATURATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0010, "Saturation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SHARPNESS: TagInfoUndefineds = TagInfoUndefineds(
        0x0011, "Sharpness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val X_3_FILL_LIGHT: TagInfoUndefineds = TagInfoUndefineds(
        0x0012, "X3FillLight", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val COLOR_ADJUSTMENT: TagInfoUndefineds = TagInfoUndefineds(
        0x0014, "ColorAdjustment", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val ADJUSTMENT_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0015, "AdjustmentMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val QUALITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0016, "Quality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val FIRMWARE: TagInfoUndefineds = TagInfoUndefineds(
        0x0017, "Firmware", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SOFTWARE: TagInfoUndefineds = TagInfoUndefineds(
        0x0018, "Software", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val AUTO_BRACKET: TagInfoUndefineds = TagInfoUndefineds(
        0x0019, "AutoBracket", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val PREVIEW_IMAGE_START: TagInfoUndefineds = TagInfoUndefineds(
        0x001a, "PreviewImageStart", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val PREVIEW_IMAGE_LENGTH: TagInfoUndefineds = TagInfoUndefineds(
        0x001b, "PreviewImageLength", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val PREVIEW_IMAGE_SIZE: TagInfoUndefineds = TagInfoUndefineds(
        0x001c, "PreviewImageSize", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x001d, "MakerNoteVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_1E: TagInfoUndefineds = TagInfoUndefineds(
        0x001e, "Sigma_0x001e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val A_F_POINT: TagInfoUndefineds = TagInfoUndefineds(
        0x001f, "AFPoint", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_0: TagInfoUndefineds = TagInfoUndefineds(
        0x0020, "Sigma_0x0020", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0021, "Sigma_0x0021", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val FILE_FORMAT: TagInfoUndefineds = TagInfoUndefineds(
        0x0022, "FileFormat", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_3: TagInfoUndefineds = TagInfoUndefineds(
        0x0023, "Sigma_0x0023", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val CALIBRATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0024, "Calibration", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_5: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "Sigma_0x0025", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_6: TagInfoUndefineds = TagInfoUndefineds(
        0x0026, "Sigma_0x0026", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0027, "Sigma_0x0027", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0028, "Sigma_0x0028", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2_9: TagInfoUndefineds = TagInfoUndefineds(
        0x0029, "Sigma_0x0029", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2A: TagInfoUndefineds = TagInfoUndefineds(
        0x002a, "Sigma_0x002a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2B: TagInfoUndefineds = TagInfoUndefineds(
        0x002b, "Sigma_0x002b", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val COLOR_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x002c, "ColorMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2D: TagInfoUndefineds = TagInfoUndefineds(
        0x002d, "Sigma_0x002d", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2E: TagInfoUndefineds = TagInfoUndefineds(
        0x002e, "Sigma_0x002e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_2F: TagInfoUndefineds = TagInfoUndefineds(
        0x002f, "Sigma_0x002f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val LENS_APERTURE_RANGE: TagInfoUndefineds = TagInfoUndefineds(
        0x0030, "LensApertureRange", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val F_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0031, "FNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val EXPOSURE_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0032, "ExposureTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val EXPOSURE_TIME_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0033, "ExposureTime2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val BURST_SHOT: TagInfoUndefineds = TagInfoUndefineds(
        0x0034, "BurstShot", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val EXPOSURE_COMPENSATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0035, "ExposureCompensation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_3_6: TagInfoUndefineds = TagInfoUndefineds(
        0x0036, "Sigma_0x0036", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_3_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0037, "Sigma_0x0037", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SIGMA__0X_0_0_3_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0038, "Sigma_0x0038", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val SENSOR_TEMPERATURE: TagInfoUndefineds = TagInfoUndefineds(
        0x0039, "SensorTemperature", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val FLASH_EXPOSURE_COMP: TagInfoUndefineds = TagInfoUndefineds(
        0x003a, "FlashExposureComp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val FIRMWARE_003b: TagInfoUndefineds = TagInfoUndefineds(
        0x003b, "Firmware", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val WHITE_BALANCE_003c: TagInfoUndefineds = TagInfoUndefineds(
        0x003c, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SIGMA
    )

    public val ALL: List<TagInfo> = listOf(
        SERIAL_NUMBER,
        DRIVE_MODE,
        RESOLUTION_MODE,
        A_F_MODE,
        FOCUS_SETTING,
        WHITE_BALANCE,
        EXPOSURE_MODE,
        METERING_MODE,
        LENS_FOCAL_RANGE,
        COLOR_SPACE,
        EXPOSURE_ADJUST,
        CONTRAST,
        SHADOW,
        HIGHLIGHT,
        SATURATION,
        SHARPNESS,
        X_3_FILL_LIGHT,
        COLOR_ADJUSTMENT,
        ADJUSTMENT_MODE,
        QUALITY,
        FIRMWARE,
        SOFTWARE,
        AUTO_BRACKET,
        PREVIEW_IMAGE_START,
        PREVIEW_IMAGE_LENGTH,
        PREVIEW_IMAGE_SIZE,
        MAKER_NOTE_VERSION,
        SIGMA__0X_0_0_1E,
        A_F_POINT,
        SIGMA__0X_0_0_2_0,
        SIGMA__0X_0_0_2_1,
        FILE_FORMAT,
        SIGMA__0X_0_0_2_3,
        CALIBRATION,
        SIGMA__0X_0_0_2_5,
        SIGMA__0X_0_0_2_6,
        SIGMA__0X_0_0_2_7,
        SIGMA__0X_0_0_2_8,
        SIGMA__0X_0_0_2_9,
        SIGMA__0X_0_0_2A,
        SIGMA__0X_0_0_2B,
        COLOR_MODE,
        SIGMA__0X_0_0_2D,
        SIGMA__0X_0_0_2E,
        SIGMA__0X_0_0_2F,
        LENS_APERTURE_RANGE,
        F_NUMBER,
        EXPOSURE_TIME,
        EXPOSURE_TIME_2,
        BURST_SHOT,
        EXPOSURE_COMPENSATION,
        SIGMA__0X_0_0_3_6,
        SIGMA__0X_0_0_3_7,
        SIGMA__0X_0_0_3_8,
        SENSOR_TEMPERATURE,
        FLASH_EXPOSURE_COMP,
        FIRMWARE_003b,
        WHITE_BALANCE_003c,
    )
}
