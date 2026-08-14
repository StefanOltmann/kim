/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Nikon MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object NikonTag {

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "MakerNoteVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val ISO: TagInfoShorts = TagInfoShorts(
        0x0002, "ISO", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The color mode of the image.
     */
    public val COLOR_MODE: TagInfoAscii = TagInfoAscii(
        0x0003, "ColorMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The image quality setting.
     */
    public val QUALITY: TagInfoAscii = TagInfoAscii(
        0x0004, "Quality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The white balance setting.
     */
    public val WHITE_BALANCE: TagInfoAscii = TagInfoAscii(
        0x0005, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val SHARPNESS: TagInfoAscii = TagInfoAscii(
        0x0006, "Sharpness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val FOCUS_MODE: TagInfoAscii = TagInfoAscii(
        0x0007, "FocusMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The flash setting of the camera.
     */
    public val FLASH_SETTING: TagInfoAscii = TagInfoAscii(
        0x0008, "FlashSetting", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val FLASH_TYPE: TagInfoAscii = TagInfoAscii(
        0x0009, "FlashType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val WHITE_BALANCE_FINE_TUNE: TagInfoSShorts = TagInfoSShorts(
        0x000b, "WhiteBalanceFineTune", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The red and blue white balance levels.
     */
    public val WB_RB_LEVELS: TagInfoRationals = TagInfoRationals(
        0x000c, "WB_RBLevels", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The program shift applied.
     */
    public val PROGRAM_SHIFT: TagInfoUndefineds = TagInfoUndefineds(
        0x000d, "ProgramShift", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val EXPOSURE_DIFFERENCE: TagInfoUndefineds = TagInfoUndefineds(
        0x000e, "ExposureDifference", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The ISO selection method used.
     */
    public val ISO_SELECTION: TagInfoAscii = TagInfoAscii(
        0x000f, "ISOSelection", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * A data dump block recorded by the camera.
     */
    public val DATA_DUMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0010, "DataDump", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val PREVIEW_IFD: TagInfoLong = TagInfoLong(
        0x0011, "PreviewIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON,
        isOffset = true
    )

    /**
     * The flash exposure compensation applied.
     */
    public val FLASH_EXPOSURE_COMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0012, "FlashExposureComp", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val ISO_SETTING: TagInfoShorts = TagInfoShorts(
        0x0013, "ISOSetting", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val COLOR_BALANCE_A: TagInfoUndefineds = TagInfoUndefineds(
        0x0014, "ColorBalanceA", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The cropping boundary of the image.
     */
    public val IMAGE_BOUNDARY: TagInfoShorts = TagInfoShorts(
        0x0016, "ImageBoundary", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val EXTERNAL_FLASH_EXPOSURE_COMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0017, "ExternalFlashExposureComp", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val FLASH_EXPOSURE_BRACKET_VALUE: TagInfoUndefineds = TagInfoUndefineds(
        0x0018, "FlashExposureBracketValue", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val EXPOSURE_BRACKET_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x0019, "ExposureBracketValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The image processing settings.
     */
    public val IMAGE_PROCESSING: TagInfoAscii = TagInfoAscii(
        0x001a, "ImageProcessing", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonCropHiSpeed].
     */
    public val CROP_HI_SPEED: TagInfoShorts = TagInfoShorts(
        0x001b, "CropHiSpeed", 7,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The exposure tuning offset applied.
     */
    public val EXPOSURE_TUNING: TagInfoUndefineds = TagInfoUndefineds(
        0x001c, "ExposureTuning", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Used as a key to decrypt other information.
     */
    public val SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x001d, "SerialNumber", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonColorSpace].
     */
    public val COLOR_SPACE: TagInfoShort = TagInfoShort(
        0x001e, "ColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Information about the vibration reduction system.
     */
    public val VR_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x001f, "VRInfo", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonImageAuthentication].
     */
    public val IMAGE_AUTHENTICATION: TagInfoByte = TagInfoByte(
        0x0020, "ImageAuthentication",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val FACE_DETECT: TagInfoUndefineds = TagInfoUndefineds(
        0x0021, "FaceDetect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonActiveDLighting].
     */
    public val ACTIVE_D_LIGHTING: TagInfoShort = TagInfoShort(
        0x0022, "ActiveD-Lighting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val PICTURE_CONTROL_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x0023, "PictureControlData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The world time setting of the camera.
     */
    public val WORLD_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0024, "WorldTime", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Information about the ISO settings.
     */
    public val ISO_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "ISOInfo", 14,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonVignetteControl].
     */
    public val VIGNETTE_CONTROL: TagInfoShort = TagInfoShort(
        0x002a, "VignetteControl",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val DISTORT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x002b, "DistortInfo", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val UNKNOWN_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x002c, "UnknownInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X002D: TagInfoShorts = TagInfoShorts(
        0x002d, "Nikon_0x002d", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val UNKNOWN_INFO_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0032, "UnknownInfo2", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonShutterMode].
     */
    public val SHUTTER_MODE: TagInfoShort = TagInfoShort(
        0x0034, "ShutterMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val HDR_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0035, "HDRInfo", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The number of shots taken with the mechanical shutter.
     */
    public val MECHANICAL_SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x0037, "MechanicalShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val LOCATION_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0039, "LocationInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X003B: TagInfoRationals = TagInfoRationals(
        0x003b, "Nikon_0x003b", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X003C: TagInfoShort = TagInfoShort(
        0x003c, "Nikon_0x003c",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val BLACK_LEVEL: TagInfoShorts = TagInfoShorts(
        0x003d, "BlackLevel", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonImageSizeRaw].
     */
    public val IMAGE_SIZE_RAW: TagInfoShort = TagInfoShort(
        0x003e, "ImageSizeRAW",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val WHITE_BALANCE_FINE_TUNE_2: TagInfoSRationals = TagInfoSRationals(
        0x003f, "WhiteBalanceFineTune", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0040: TagInfoUndefineds = TagInfoUndefineds(
        0x0040, "Nikon_0x0040", 12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0041: TagInfoUndefineds = TagInfoUndefineds(
        0x0041, "Nikon_0x0041", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0042: TagInfoUndefineds = TagInfoUndefineds(
        0x0042, "Nikon_0x0042", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonJpgCompression].
     */
    public val JPG_COMPRESSION: TagInfoShort = TagInfoShort(
        0x0044, "JPGCompression",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val CROP_AREA: TagInfoShorts = TagInfoShorts(
        0x0045, "CropArea", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x004e, "NikonSettings", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val COLOR_TEMPERATURE_AUTO: TagInfoShort = TagInfoShort(
        0x004f, "ColorTemperatureAuto",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val MAKER_NOTES_0X51: TagInfoUndefineds = TagInfoUndefineds(
        0x0051, "MakerNotes0x51", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val MAKER_NOTES_0X56: TagInfoUndefineds = TagInfoUndefineds(
        0x0056, "MakerNotes0x56", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The image adjustment setting.
     */
    public val IMAGE_ADJUSTMENT: TagInfoAscii = TagInfoAscii(
        0x0080, "ImageAdjustment", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The tone compensation applied.
     */
    public val TONE_COMP: TagInfoAscii = TagInfoAscii(
        0x0081, "ToneComp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The auxiliary lens or adapter mounted.
     */
    public val AUXILIARY_LENS: TagInfoAscii = TagInfoAscii(
        0x0082, "AuxiliaryLens", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The type of the lens used.
     */
    public val LENS_TYPE: TagInfoByte = TagInfoByte(
        0x0083, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val LENS: TagInfoRationals = TagInfoRationals(
        0x0084, "Lens", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val MANUAL_FOCUS_DISTANCE: TagInfoRationals = TagInfoRationals(
        0x0085, "ManualFocusDistance", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The digital zoom setting used.
     */
    public val DIGITAL_ZOOM: TagInfoRationals = TagInfoRationals(
        0x0086, "DigitalZoom", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonFlashMode].
     */
    public val FLASH_MODE: TagInfoByte = TagInfoByte(
        0x0087, "FlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Information about the autofocus state.
     */
    public val AF_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0088, "AFInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The shooting mode used.
     */
    public val SHOOTING_MODE: TagInfoShort = TagInfoShort(
        0x0089, "ShootingMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X008A: TagInfoShort = TagInfoShort(
        0x008a, "Nikon_0x008a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The maximum aperture of the lens in f-stops.
     */
    public val LENS_F_STOPS: TagInfoUndefineds = TagInfoUndefineds(
        0x008b, "LensFStops", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The contrast curve used for processing.
     */
    public val CONTRAST_CURVE: TagInfoUndefineds = TagInfoUndefineds(
        0x008c, "ContrastCurve", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The color hue adjustment applied.
     */
    public val COLOR_HUE: TagInfoAscii = TagInfoAscii(
        0x008d, "ColorHue", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The scene mode of the shot.
     */
    public val SCENE_MODE: TagInfoAscii = TagInfoAscii(
        0x008f, "SceneMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The light source of the scene.
     */
    public val LIGHT_SOURCE: TagInfoAscii = TagInfoAscii(
        0x0090, "LightSource", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Shooting-related information recorded by the camera.
     */
    public val SHOT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0091, "ShotInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The hue adjustment applied.
     */
    public val HUE_ADJUSTMENT: TagInfoSShort = TagInfoSShort(
        0x0092, "HueAdjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonNefCompression].
     */
    public val NEF_COMPRESSION: TagInfoShort = TagInfoShort(
        0x0093, "NEFCompression",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val SATURATION_ADJ: TagInfoSShort = TagInfoSShort(
        0x0094, "SaturationAdj",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The noise reduction setting.
     */
    public val NOISE_REDUCTION: TagInfoAscii = TagInfoAscii(
        0x0095, "NoiseReduction", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NEF_LINEARIZATION_TABLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0096, "NEFLinearizationTable", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The color balance settings of the camera.
     */
    public val COLOR_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x0097, "ColorBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The lens data recorded for the shot.
     */
    public val LENS_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x0098, "LensData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The center of the raw image.
     */
    public val RAW_IMAGE_CENTER: TagInfoShorts = TagInfoShorts(
        0x0099, "RawImageCenter", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The pixel size of the sensor.
     */
    public val SENSOR_PIXEL_SIZE: TagInfoRationals = TagInfoRationals(
        0x009a, "SensorPixelSize", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The scene assist setting.
     */
    public val SCENE_ASSIST: TagInfoAscii = TagInfoAscii(
        0x009c, "SceneAssist", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonDateStampMode].
     */
    public val DATE_STAMP_MODE: TagInfoShort = TagInfoShort(
        0x009d, "DateStampMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonRetouchHistory].
     */
    public val RETOUCH_HISTORY: TagInfoShorts = TagInfoShorts(
        0x009e, "RetouchHistory", 10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The serial number of the camera.
     */
    public val SERIAL_NUMBER_2: TagInfoAscii = TagInfoAscii(
        0x00a0, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The size of the image data.
     */
    public val IMAGE_DATA_SIZE: TagInfoLong = TagInfoLong(
        0x00a2, "ImageDataSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X00A3: TagInfoByte = TagInfoByte(
        0x00a3, "Nikon_0x00a3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X00A4: TagInfoUndefineds = TagInfoUndefineds(
        0x00a4, "Nikon_0x00a4", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The number of images taken by the shutter.
     */
    public val IMAGE_COUNT: TagInfoLong = TagInfoLong(
        0x00a5, "ImageCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The number of images deleted by the camera.
     */
    public val DELETED_IMAGE_COUNT: TagInfoLong = TagInfoLong(
        0x00a6, "DeletedImageCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Includes both mechanical and electronic shutter activations for models with this feature.
     */
    public val SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x00a7, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Information about the flash usage.
     */
    public val FLASH_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00a8, "FlashInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The image optimization setting.
     */
    public val IMAGE_OPTIMIZATION: TagInfoAscii = TagInfoAscii(
        0x00a9, "ImageOptimization", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val SATURATION: TagInfoAscii = TagInfoAscii(
        0x00aa, "Saturation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The program variation used.
     */
    public val VARI_PROGRAM: TagInfoAscii = TagInfoAscii(
        0x00ab, "VariProgram", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The image stabilization setting.
     */
    public val IMAGE_STABILIZATION: TagInfoAscii = TagInfoAscii(
        0x00ac, "ImageStabilization", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The autofocus response of the lens.
     */
    public val AF_RESPONSE: TagInfoAscii = TagInfoAscii(
        0x00ad, "AFResponse", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The multi-exposure setting.
     */
    public val MULTI_EXPOSURE: TagInfoUndefineds = TagInfoUndefineds(
        0x00b0, "MultiExposure", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonHighIsoNoiseReduction].
     */
    public val HIGH_ISO_NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x00b1, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The toning effect applied.
     */
    public val TONING_EFFECT: TagInfoAscii = TagInfoAscii(
        0x00b3, "ToningEffect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Date/time when the camera was last powered up.
     */
    public val POWER_UP_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x00b6, "PowerUpTime", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Additional autofocus information.
     */
    public val AF_INFO_2: TagInfoUndefineds = TagInfoUndefineds(
        0x00b7, "AFInfo2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * File-related information recorded by the camera.
     */
    public val FILE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00b8, "FileInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * The autofocus fine-tune setting.
     */
    public val AF_TUNE: TagInfoUndefineds = TagInfoUndefineds(
        0x00b9, "AFTune", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val RETOUCH_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00bb, "RetouchInfo", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X00BC: TagInfoUndefineds = TagInfoUndefineds(
        0x00bc, "Nikon_0x00bc", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val PICTURE_CONTROL_DATA_2: TagInfoUndefineds = TagInfoUndefineds(
        0x00bd, "PictureControlData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * See [NikonSilentPhotography].
     */
    public val SILENT_PHOTOGRAPHY: TagInfoShort = TagInfoShort(
        0x00bf, "SilentPhotography",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X00C0: TagInfoUndefineds = TagInfoUndefineds(
        0x00c0, "Nikon_0x00c0", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Barometric data recorded with the image.
     */
    public val BAROMETER_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00c3, "BarometerInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * Print Image Matching (PrintIM) information.
     */
    public val PRINT_IM: TagInfoUndefineds = TagInfoUndefineds(
        0x0e00, "PrintIM", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_CAPTURE_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x0e01, "NikonCaptureData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0E05: TagInfoLong = TagInfoLong(
        0x0e05, "Nikon_0x0e05",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_CAPTURE_VERSION: TagInfoAscii = TagInfoAscii(
        0x0e09, "NikonCaptureVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_CAPTURE_OFFSETS: TagInfoUndefineds = TagInfoUndefineds(
        0x0e0e, "NikonCaptureOffsets", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_SCAN_IFD: TagInfoLong = TagInfoLong(
        0x0e10, "NikonScanIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON,
        isOffset = true
    )

    public val NIKON_CAPTURE_EDIT_VERSIONS: TagInfoUndefineds = TagInfoUndefineds(
        0x0e13, "NikonCaptureEditVersions", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0E19: TagInfoShorts = TagInfoShorts(
        0x0e19, "Nikon_0x0e19", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_ICC_PROFILE: TagInfoUndefineds = TagInfoUndefineds(
        0x0e1d, "NikonICCProfile", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_CAPTURE_OUTPUT: TagInfoUndefineds = TagInfoUndefineds(
        0x0e1e, "NikonCaptureOutput", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    /**
     * '0 0 0 0' = n/a (JPEG), '8 8 8 0' = 8 x 3, '12 0 0 0' = 12, '14 0 0 0' = 14, '16 16 16 0' = 16 x 3.
     */
    public val NEF_BIT_DEPTH: TagInfoShorts = TagInfoShorts(
        0x0e22, "NEFBitDepth", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val NIKON_0X0E23: TagInfoUndefineds = TagInfoUndefineds(
        0x0e23, "Nikon_0x0e23", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val ALL: List<TagInfo> = listOf(
        MAKER_NOTE_VERSION, ISO, COLOR_MODE, QUALITY, WHITE_BALANCE,
        SHARPNESS, FOCUS_MODE, FLASH_SETTING, FLASH_TYPE,
        WHITE_BALANCE_FINE_TUNE, WB_RB_LEVELS, PROGRAM_SHIFT,
        EXPOSURE_DIFFERENCE, ISO_SELECTION, DATA_DUMP, PREVIEW_IFD,
        FLASH_EXPOSURE_COMP, ISO_SETTING, COLOR_BALANCE_A,
        IMAGE_BOUNDARY, EXTERNAL_FLASH_EXPOSURE_COMP,
        FLASH_EXPOSURE_BRACKET_VALUE, EXPOSURE_BRACKET_VALUE,
        IMAGE_PROCESSING, CROP_HI_SPEED, EXPOSURE_TUNING, SERIAL_NUMBER,
        COLOR_SPACE, VR_INFO, IMAGE_AUTHENTICATION, FACE_DETECT,
        ACTIVE_D_LIGHTING, PICTURE_CONTROL_DATA, WORLD_TIME, ISO_INFO,
        VIGNETTE_CONTROL, DISTORT_INFO, UNKNOWN_INFO, NIKON_0X002D,
        UNKNOWN_INFO_2, SHUTTER_MODE, HDR_INFO, MECHANICAL_SHUTTER_COUNT,
        LOCATION_INFO, NIKON_0X003B, NIKON_0X003C, BLACK_LEVEL,
        IMAGE_SIZE_RAW, WHITE_BALANCE_FINE_TUNE_2,
        NIKON_0X0040, NIKON_0X0041, NIKON_0X0042,
        JPG_COMPRESSION, CROP_AREA, NIKON_SETTINGS, COLOR_TEMPERATURE_AUTO,
        MAKER_NOTES_0X51, MAKER_NOTES_0X56,
        IMAGE_ADJUSTMENT, TONE_COMP, AUXILIARY_LENS, LENS_TYPE, LENS,
        MANUAL_FOCUS_DISTANCE, DIGITAL_ZOOM, FLASH_MODE, AF_INFO,
        SHOOTING_MODE, NIKON_0X008A, LENS_F_STOPS, CONTRAST_CURVE,
        COLOR_HUE, SCENE_MODE, LIGHT_SOURCE, SHOT_INFO, HUE_ADJUSTMENT,
        NEF_COMPRESSION, SATURATION_ADJ, NOISE_REDUCTION,
        NEF_LINEARIZATION_TABLE, COLOR_BALANCE, LENS_DATA,
        RAW_IMAGE_CENTER, SENSOR_PIXEL_SIZE, SCENE_ASSIST, DATE_STAMP_MODE,
        RETOUCH_HISTORY, SERIAL_NUMBER_2, IMAGE_DATA_SIZE,
        NIKON_0X00A3, NIKON_0X00A4, IMAGE_COUNT, DELETED_IMAGE_COUNT,
        SHUTTER_COUNT, FLASH_INFO, IMAGE_OPTIMIZATION, SATURATION,
        VARI_PROGRAM, IMAGE_STABILIZATION, AF_RESPONSE, MULTI_EXPOSURE,
        HIGH_ISO_NOISE_REDUCTION, TONING_EFFECT, POWER_UP_TIME, AF_INFO_2,
        FILE_INFO, AF_TUNE, RETOUCH_INFO, NIKON_0X00BC,
        PICTURE_CONTROL_DATA_2, SILENT_PHOTOGRAPHY, NIKON_0X00C0,
        BAROMETER_INFO,
        PRINT_IM, NIKON_CAPTURE_DATA, NIKON_0X0E05, NIKON_CAPTURE_VERSION,
        NIKON_CAPTURE_OFFSETS, NIKON_SCAN_IFD, NIKON_CAPTURE_EDIT_VERSIONS,
        NIKON_0X0E19, NIKON_ICC_PROFILE, NIKON_CAPTURE_OUTPUT,
        NEF_BIT_DEPTH, NIKON_0X0E23
    )
}
