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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts

/**
 * Tags of the CameraSettings maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Olympus.html#CameraSettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object OlympusCameraSettingsTag {

    public val CAMERA_SETTINGS_VERSION: TagInfoByte = TagInfoByte(
        0x0, "CameraSettingsVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PREVIEW_IMAGE_VALID: TagInfoByte = TagInfoByte(
        0x100, "PreviewImageValid",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PREVIEW_IMAGE_START: TagInfoByte = TagInfoByte(
        0x101, "PreviewImageStart",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PREVIEW_IMAGE_LENGTH: TagInfoByte = TagInfoByte(
        0x102, "PreviewImageLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val EXPOSURE_MODE: TagInfoByte = TagInfoByte(
        0x200, "ExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AE_LOCK: TagInfoByte = TagInfoByte(
        0x201, "AELock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val METERING_MODE: TagInfoByte = TagInfoByte(
        0x202, "MeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val EXPOSURE_SHIFT: TagInfoByte = TagInfoByte(
        0x203, "ExposureShift",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ND_FILTER: TagInfoByte = TagInfoByte(
        0x204, "NDFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MACRO_MODE: TagInfoByte = TagInfoByte(
        0x300, "MacroMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FOCUS_MODE: TagInfoByte = TagInfoByte(
        0x301, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FOCUS_PROCESS: TagInfoByte = TagInfoByte(
        0x302, "FocusProcess",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_SEARCH: TagInfoByte = TagInfoByte(
        0x303, "AFSearch",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_AREAS: TagInfoByte = TagInfoByte(
        0x304, "AFAreas",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_POINT_SELECTED: TagInfoByte = TagInfoByte(
        0x305, "AFPointSelected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_FINE_TUNE: TagInfoByte = TagInfoByte(
        0x306, "AFFineTune",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_FINE_TUNE_ADJ: TagInfoByte = TagInfoByte(
        0x307, "AFFineTuneAdj",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FOCUS_BRACKET_STEP_SIZE: TagInfoByte = TagInfoByte(
        0x308, "FocusBracketStepSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AI_SUBJECT_TRACKING_MODE: TagInfoByte = TagInfoByte(
        0x309, "AISubjectTrackingMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val AF_TARGET_INFO: TagInfoByte = TagInfoByte(
        0x30a, "AFTargetInfo",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val SUBJECT_DETECT_INFO: TagInfoByte = TagInfoByte(
        0x30b, "SubjectDetectInfo",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FLASH_MODE: TagInfoByte = TagInfoByte(
        0x400, "FlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FLASH_EXPOSURE_COMP: TagInfoByte = TagInfoByte(
        0x401, "FlashExposureComp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FLASH_REMOTE_CONTROL: TagInfoByte = TagInfoByte(
        0x403, "FlashRemoteControl",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FLASH_CONTROL_MODE: TagInfoByte = TagInfoByte(
        0x404, "FlashControlMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FLASH_INTENSITY: TagInfoByte = TagInfoByte(
        0x405, "FlashIntensity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MANUAL_FLASH_STRENGTH: TagInfoByte = TagInfoByte(
        0x406, "ManualFlashStrength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val WHITE_BALANCE2: TagInfoByte = TagInfoByte(
        0x500, "WhiteBalance2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val WHITE_BALANCE_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x501, "WhiteBalanceTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val WHITE_BALANCE_BRACKET: TagInfoByte = TagInfoByte(
        0x502, "WhiteBalanceBracket",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val CUSTOM_SATURATION: TagInfoByte = TagInfoByte(
        0x503, "CustomSaturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MODIFIED_SATURATION: TagInfoByte = TagInfoByte(
        0x504, "ModifiedSaturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val CONTRAST_SETTING: TagInfoByte = TagInfoByte(
        0x505, "ContrastSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val SHARPNESS_SETTING: TagInfoByte = TagInfoByte(
        0x506, "SharpnessSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val COLOR_SPACE: TagInfoByte = TagInfoByte(
        0x507, "ColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val SCENE_MODE: TagInfoByte = TagInfoByte(
        0x509, "SceneMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x50a, "NoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val DISTORTION_CORRECTION: TagInfoByte = TagInfoByte(
        0x50b, "DistortionCorrection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val SHADING_COMPENSATION: TagInfoByte = TagInfoByte(
        0x50c, "ShadingCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val COMPRESSION_FACTOR: TagInfoByte = TagInfoByte(
        0x50d, "CompressionFactor",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val GRADATION: TagInfoByte = TagInfoByte(
        0x50f, "Gradation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE: TagInfoByte = TagInfoByte(
        0x520, "PictureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_SATURATION: TagInfoByte = TagInfoByte(
        0x521, "PictureModeSaturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_HUE: TagInfoByte = TagInfoByte(
        0x522, "PictureModeHue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_CONTRAST: TagInfoByte = TagInfoByte(
        0x523, "PictureModeContrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_SHARPNESS: TagInfoByte = TagInfoByte(
        0x524, "PictureModeSharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_BW_FILTER: TagInfoByte = TagInfoByte(
        0x525, "PictureModeBWFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_TONE: TagInfoByte = TagInfoByte(
        0x526, "PictureModeTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val NOISE_FILTER: TagInfoByte = TagInfoByte(
        0x527, "NoiseFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ART_FILTER: TagInfoByte = TagInfoByte(
        0x529, "ArtFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MAGIC_FILTER: TagInfoByte = TagInfoByte(
        0x52c, "MagicFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PICTURE_MODE_EFFECT: TagInfoByte = TagInfoByte(
        0x52d, "PictureModeEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val TONE_LEVEL: TagInfoByte = TagInfoByte(
        0x52e, "ToneLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ART_FILTER_EFFECT: TagInfoByte = TagInfoByte(
        0x52f, "ArtFilterEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val COLOR_CREATOR_EFFECT: TagInfoByte = TagInfoByte(
        0x532, "ColorCreatorEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MONOCHROME_PROFILE_SETTINGS: TagInfoByte = TagInfoByte(
        0x537, "MonochromeProfileSettings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val FILM_GRAIN_EFFECT: TagInfoByte = TagInfoByte(
        0x538, "FilmGrainEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val COLOR_PROFILE_SETTINGS: TagInfoByte = TagInfoByte(
        0x539, "ColorProfileSettings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MONOCHROME_VIGNETTING: TagInfoByte = TagInfoByte(
        0x53a, "MonochromeVignetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MONOCHROME_COLOR: TagInfoByte = TagInfoByte(
        0x53b, "MonochromeColor",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val DRIVE_MODE: TagInfoByte = TagInfoByte(
        0x600, "DriveMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PANORAMA_MODE: TagInfoByte = TagInfoByte(
        0x601, "PanoramaMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val IMAGE_QUALITY2: TagInfoByte = TagInfoByte(
        0x603, "ImageQuality2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val IMAGE_STABILIZATION: TagInfoByte = TagInfoByte(
        0x604, "ImageStabilization",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val STACKED_IMAGE: TagInfoByte = TagInfoByte(
        0x804, "StackedImage",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ISO_AUTO_SETTINGS: TagInfoByte = TagInfoByte(
        0x821, "ISOAutoSettings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MANOMETER_PRESSURE: TagInfoByte = TagInfoByte(
        0x900, "ManometerPressure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val MANOMETER_READING: TagInfoByte = TagInfoByte(
        0x901, "ManometerReading",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val EXTENDED_WB_DETECT: TagInfoByte = TagInfoByte(
        0x902, "ExtendedWBDetect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ROLL_ANGLE: TagInfoByte = TagInfoByte(
        0x903, "RollAngle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val PITCH_ANGLE: TagInfoByte = TagInfoByte(
        0x904, "PitchAngle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val DATE_TIME_UTC: TagInfoByte = TagInfoByte(
        0x908, "DateTimeUTC",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0402: TagInfoShort = TagInfoShort(
        0x0402, "Olympus_CameraSettings_0x0402",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0508: TagInfoShort = TagInfoShort(
        0x0508, "Olympus_CameraSettings_0x0508",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X050E: TagInfoShort = TagInfoShort(
        0x050e, "Olympus_CameraSettings_0x050e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0528: TagInfoShort = TagInfoShort(
        0x0528, "Olympus_CameraSettings_0x0528",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X052A: TagInfoLongs = TagInfoLongs(
        0x052a, "Olympus_CameraSettings_0x052a",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X052B: TagInfoShorts = TagInfoShorts(
        0x052b, "Olympus_CameraSettings_0x052b",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0530: TagInfoShorts = TagInfoShorts(
        0x0530, "Olympus_CameraSettings_0x0530",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0531: TagInfoShorts = TagInfoShorts(
        0x0531, "Olympus_CameraSettings_0x0531",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0605: TagInfoLongs = TagInfoLongs(
        0x0605, "Olympus_CameraSettings_0x0605",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0800: TagInfoShort = TagInfoShort(
        0x0800, "Olympus_CameraSettings_0x0800",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val OLYMPUS_CAMERA_SETTINGS_0X0802: TagInfoShort = TagInfoShort(
        0x0802, "Olympus_CameraSettings_0x0802",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
    )

    public val ALL: List<TagInfo> = listOf(
        CAMERA_SETTINGS_VERSION,
        PREVIEW_IMAGE_VALID,
        PREVIEW_IMAGE_START,
        PREVIEW_IMAGE_LENGTH,
        EXPOSURE_MODE,
        AE_LOCK,
        METERING_MODE,
        EXPOSURE_SHIFT,
        ND_FILTER,
        MACRO_MODE,
        FOCUS_MODE,
        FOCUS_PROCESS,
        AF_SEARCH,
        AF_AREAS,
        AF_POINT_SELECTED,
        AF_FINE_TUNE,
        AF_FINE_TUNE_ADJ,
        FOCUS_BRACKET_STEP_SIZE,
        AI_SUBJECT_TRACKING_MODE,
        AF_TARGET_INFO,
        SUBJECT_DETECT_INFO,
        FLASH_MODE,
        FLASH_EXPOSURE_COMP,
        OLYMPUS_CAMERA_SETTINGS_0X0402,
        FLASH_REMOTE_CONTROL,
        FLASH_CONTROL_MODE,
        FLASH_INTENSITY,
        MANUAL_FLASH_STRENGTH,
        WHITE_BALANCE2,
        WHITE_BALANCE_TEMPERATURE,
        WHITE_BALANCE_BRACKET,
        CUSTOM_SATURATION,
        MODIFIED_SATURATION,
        CONTRAST_SETTING,
        SHARPNESS_SETTING,
        COLOR_SPACE,
        OLYMPUS_CAMERA_SETTINGS_0X0508,
        SCENE_MODE,
        NOISE_REDUCTION,
        DISTORTION_CORRECTION,
        SHADING_COMPENSATION,
        COMPRESSION_FACTOR,
        OLYMPUS_CAMERA_SETTINGS_0X050E,
        GRADATION,
        PICTURE_MODE,
        PICTURE_MODE_SATURATION,
        PICTURE_MODE_HUE,
        PICTURE_MODE_CONTRAST,
        PICTURE_MODE_SHARPNESS,
        PICTURE_MODE_BW_FILTER,
        PICTURE_MODE_TONE,
        NOISE_FILTER,
        OLYMPUS_CAMERA_SETTINGS_0X0528,
        ART_FILTER,
        OLYMPUS_CAMERA_SETTINGS_0X052A,
        OLYMPUS_CAMERA_SETTINGS_0X052B,
        MAGIC_FILTER,
        PICTURE_MODE_EFFECT,
        TONE_LEVEL,
        ART_FILTER_EFFECT,
        OLYMPUS_CAMERA_SETTINGS_0X0530,
        OLYMPUS_CAMERA_SETTINGS_0X0531,
        COLOR_CREATOR_EFFECT,
        MONOCHROME_PROFILE_SETTINGS,
        FILM_GRAIN_EFFECT,
        COLOR_PROFILE_SETTINGS,
        MONOCHROME_VIGNETTING,
        MONOCHROME_COLOR,
        DRIVE_MODE,
        PANORAMA_MODE,
        IMAGE_QUALITY2,
        IMAGE_STABILIZATION,
        OLYMPUS_CAMERA_SETTINGS_0X0605,
        OLYMPUS_CAMERA_SETTINGS_0X0800,
        OLYMPUS_CAMERA_SETTINGS_0X0802,
        STACKED_IMAGE,
        ISO_AUTO_SETTINGS,
        MANOMETER_PRESSURE,
        MANOMETER_READING,
        EXTENDED_WB_DETECT,
        ROLL_ANGLE,
        PITCH_ANGLE,
        DATE_TIME_UTC
    )
}
