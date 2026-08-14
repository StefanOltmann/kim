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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Olympus MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/Olympus.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object OlympusTag {

    /*
     * Tags 0x0000 through 0x0100 are used by some older Olympus cameras
     * and are the same as the Konica/Minolta tags.
     */

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0000, "MakerNoteVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val MINOLTA_CAMERA_SETTINGS_OLD: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "MinoltaCameraSettingsOld", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val MINOLTA_CAMERA_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x0003, "MinoltaCameraSettings", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val COMPRESSED_IMAGE_SIZE: TagInfoLong = TagInfoLong(
        0x0040, "CompressedImageSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val PREVIEW_IMAGE_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x0081, "PreviewImageData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The offset of the preview image data.
     */
    public val PREVIEW_IMAGE_START: TagInfoLong = TagInfoLong(
        0x0088, "PreviewImageStart",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The length of the preview image data.
     */
    public val PREVIEW_IMAGE_LENGTH: TagInfoLong = TagInfoLong(
        0x0089, "PreviewImageLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The embedded thumbnail image.
     */
    public val THUMBNAIL_IMAGE: TagInfoUndefineds = TagInfoUndefineds(
        0x0100, "ThumbnailImage", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The firmware version of the camera body.
     */
    public val BODY_FIRMWARE_VERSION: TagInfoAscii = TagInfoAscii(
        0x0104, "BodyFirmwareVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * 3 numbers: 1. Shooting mode: 0=Normal, 2=Fast, 3=Panorama; 2. Sequence Number;
     * 3. Panorama Direction: 1=Left-right, 2=Right-left, 3=Bottom-Top, 4=Top-Bottom.
     */
    public val SPECIAL_MODE: TagInfoLongs = TagInfoLongs(
        0x0200, "SpecialMode", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Quality values are decoded based on the CameraType tag.
     */
    public val QUALITY: TagInfoShort = TagInfoShort(
        0x0201, "Quality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusMacro].
     */
    public val MACRO: TagInfoShort = TagInfoShort(
        0x0202, "Macro",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusBwMode].
     */
    public val BW_MODE: TagInfoShort = TagInfoShort(
        0x0203, "BWMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The digital zoom ratio used.
     */
    public val DIGITAL_ZOOM: TagInfoRationals = TagInfoRationals(
        0x0204, "DigitalZoom", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The diagonal of the focal plane.
     */
    public val FOCAL_PLANE_DIAGONAL: TagInfoRationals = TagInfoRationals(
        0x0205, "FocalPlaneDiagonal", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The lens distortion parameters.
     */
    public val LENS_DISTORTION_PARAMS: TagInfoSShorts = TagInfoSShorts(
        0x0206, "LensDistortionParams", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusCameraType].
     */
    public val CAMERA_TYPE: TagInfoAscii = TagInfoAscii(
        0x0207, "CameraType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val TEXT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0208, "TextInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Identification data of the camera.
     */
    public val CAMERA_ID: TagInfoUndefineds = TagInfoUndefineds(
        0x0209, "CameraID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EPSON_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0x020b, "EpsonImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EPSON_IMAGE_HEIGHT: TagInfoLong = TagInfoLong(
        0x020c, "EpsonImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EPSON_SOFTWARE: TagInfoAscii = TagInfoAscii(
        0x020d, "EpsonSoftware", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Found in ERF and JPG images from some Epson models.
     */
    public val PREVIEW_IMAGE: TagInfoBytes = TagInfoBytes(
        0x0280, "PreviewImage", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The number of pre-capture frames.
     */
    public val PRE_CAPTURE_FRAMES: TagInfoShort = TagInfoShort(
        0x0300, "PreCaptureFrames",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The white board setting used for white balance.
     */
    public val WHITE_BOARD: TagInfoShort = TagInfoShort(
        0x0301, "WhiteBoard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusOneTouchWhiteBalance].
     */
    public val ONE_TOUCH_WB: TagInfoShort = TagInfoShort(
        0x0302, "OneTouchWB",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The white balance bracketing setting.
     */
    public val WHITE_BALANCE_BRACKET: TagInfoShort = TagInfoShort(
        0x0303, "WhiteBalanceBracket",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The white balance bias applied.
     */
    public val WHITE_BALANCE_BIAS: TagInfoShort = TagInfoShort(
        0x0304, "WhiteBalanceBias",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Found in Epson ERF images.
     */
    public val SENSOR_AREA: TagInfoUndefineds = TagInfoUndefineds(
        0x0400, "SensorArea", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Found in Epson ERF images.
     */
    public val BLACK_LEVEL: TagInfoLongs = TagInfoLongs(
        0x0401, "BlackLevel", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusSceneMode].
     */
    public val SCENE_MODE: TagInfoShort = TagInfoShort(
        0x0403, "SceneMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The serial number of the camera.
     */
    public val SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0404, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val FIRMWARE: TagInfoAscii = TagInfoAscii(
        0x0405, "Firmware", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Print Image Matching (PrintIM) information.
     */
    public val PRINT_IM: TagInfoUndefineds = TagInfoUndefineds(
        0x0e00, "PrintIM", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val DATA_DUMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0f00, "DataDump", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Additional camera settings recorded by the camera.
     */
    public val DATA_DUMP_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0f01, "DataDump2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val ZOOMED_PREVIEW_START: TagInfoLong = TagInfoLong(
        0x0f04, "ZoomedPreviewStart",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val ZOOMED_PREVIEW_LENGTH: TagInfoLong = TagInfoLong(
        0x0f05, "ZoomedPreviewLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val ZOOMED_PREVIEW_SIZE: TagInfoShorts = TagInfoShorts(
        0x0f06, "ZoomedPreviewSize", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val SHUTTER_SPEED_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x1000, "ShutterSpeedValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val ISO_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x1001, "ISOValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The aperture value of the shot.
     */
    public val APERTURE_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x1002, "ApertureValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val BRIGHTNESS_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x1003, "BrightnessValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusFlashMode].
     */
    public val FLASH_MODE: TagInfoShort = TagInfoShort(
        0x1004, "FlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusFlashDevice].
     */
    public val FLASH_DEVICE: TagInfoShort = TagInfoShort(
        0x1005, "FlashDevice",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXPOSURE_COMPENSATION: TagInfoSRationals = TagInfoSRationals(
        0x1006, "ExposureCompensation", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The temperature of the sensor.
     */
    public val SENSOR_TEMPERATURE: TagInfoSShort = TagInfoSShort(
        0x1007, "SensorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The temperature of the lens.
     */
    public val LENS_TEMPERATURE: TagInfoSShort = TagInfoSShort(
        0x1008, "LensTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The light condition of the scene.
     */
    public val LIGHT_CONDITION: TagInfoShort = TagInfoShort(
        0x1009, "LightCondition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusFocusRange].
     */
    public val FOCUS_RANGE: TagInfoShort = TagInfoShort(
        0x100a, "FocusRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusFocusMode].
     */
    public val FOCUS_MODE: TagInfoShort = TagInfoShort(
        0x100b, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val MANUAL_FOCUS_DISTANCE: TagInfoRationals = TagInfoRationals(
        0x100c, "ManualFocusDistance", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The number of zoom steps.
     */
    public val ZOOM_STEP_COUNT: TagInfoShort = TagInfoShort(
        0x100d, "ZoomStepCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The number of focus steps.
     */
    public val FOCUS_STEP_COUNT: TagInfoShort = TagInfoShort(
        0x100e, "FocusStepCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusSharpness].
     */
    public val SHARPNESS: TagInfoShort = TagInfoShort(
        0x100f, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The charge level of the flash.
     */
    public val FLASH_CHARGE_LEVEL: TagInfoShort = TagInfoShort(
        0x1010, "FlashChargeLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The color matrix used for processing.
     */
    public val COLOR_MATRIX: TagInfoShorts = TagInfoShorts(
        0x1011, "ColorMatrix", 9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The black level of the sensor.
     */
    public val BLACK_LEVEL_2: TagInfoShorts = TagInfoShorts(
        0x1012, "BlackLevel", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val COLOR_TEMPERATURE_BG: TagInfoShort = TagInfoShort(
        0x1013, "ColorTemperatureBG",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val COLOR_TEMPERATURE_RG: TagInfoShort = TagInfoShort(
        0x1014, "ColorTemperatureRG",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val WB_MODE: TagInfoShorts = TagInfoShorts(
        0x1015, "WBMode", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The red white balance level.
     */
    public val RED_BALANCE: TagInfoShorts = TagInfoShorts(
        0x1017, "RedBalance", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The blue white balance level.
     */
    public val BLUE_BALANCE: TagInfoShorts = TagInfoShorts(
        0x1018, "BlueBalance", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The number of the color matrix used.
     */
    public val COLOR_MATRIX_NUMBER: TagInfoShort = TagInfoShort(
        0x1019, "ColorMatrixNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The serial number of the camera.
     */
    public val SERIAL_NUMBER_2: TagInfoAscii = TagInfoAscii(
        0x101a, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXTERNAL_FLASH_AE1_0: TagInfoLong = TagInfoLong(
        0x101b, "ExternalFlashAE1_0",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXTERNAL_FLASH_AE2_0: TagInfoLong = TagInfoLong(
        0x101c, "ExternalFlashAE2_0",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val INTERNAL_FLASH_AE1_0: TagInfoLong = TagInfoLong(
        0x101d, "InternalFlashAE1_0",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val INTERNAL_FLASH_AE2_0: TagInfoLong = TagInfoLong(
        0x101e, "InternalFlashAE2_0",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXTERNAL_FLASH_AE1: TagInfoLong = TagInfoLong(
        0x101f, "ExternalFlashAE1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXTERNAL_FLASH_AE2: TagInfoLong = TagInfoLong(
        0x1020, "ExternalFlashAE2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val INTERNAL_FLASH_AE1: TagInfoLong = TagInfoLong(
        0x1021, "InternalFlashAE1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val INTERNAL_FLASH_AE2: TagInfoLong = TagInfoLong(
        0x1022, "InternalFlashAE2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The flash exposure compensation applied.
     */
    public val FLASH_EXPOSURE_COMP: TagInfoSRationals = TagInfoSRationals(
        0x1023, "FlashExposureComp", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val INTERNAL_FLASH_TABLE: TagInfoShort = TagInfoShort(
        0x1024, "InternalFlashTable",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val EXTERNAL_FLASH_G_VALUE: TagInfoSRationals = TagInfoSRationals(
        0x1025, "ExternalFlashGValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusExternalFlashBounce].
     */
    public val EXTERNAL_FLASH_BOUNCE: TagInfoShort = TagInfoShort(
        0x1026, "ExternalFlashBounce",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The zoom setting of the external flash.
     */
    public val EXTERNAL_FLASH_ZOOM: TagInfoShort = TagInfoShort(
        0x1027, "ExternalFlashZoom",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The mode of the external flash.
     */
    public val EXTERNAL_FLASH_MODE: TagInfoShort = TagInfoShort(
        0x1028, "ExternalFlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusContrast].
     */
    public val CONTRAST: TagInfoShort = TagInfoShort(
        0x1029, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The sharpness factor setting.
     */
    public val SHARPNESS_FACTOR: TagInfoShort = TagInfoShort(
        0x102a, "SharpnessFactor",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The color control setting.
     */
    public val COLOR_CONTROL: TagInfoShorts = TagInfoShorts(
        0x102b, "ColorControl", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The number of valid bits of the pixel data.
     */
    public val VALID_BITS: TagInfoShorts = TagInfoShorts(
        0x102c, "ValidBits", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The coring filter setting.
     */
    public val CORING_FILTER: TagInfoShort = TagInfoShort(
        0x102d, "CoringFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val OLYMPUS_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0x102e, "OlympusImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val OLYMPUS_IMAGE_HEIGHT: TagInfoLong = TagInfoLong(
        0x102f, "OlympusImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The scene detected by the camera.
     */
    public val SCENE_DETECT: TagInfoShort = TagInfoShort(
        0x1030, "SceneDetect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The area of the scene evaluated by the camera.
     */
    public val SCENE_AREA: TagInfoLongs = TagInfoLongs(
        0x1031, "SceneArea", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * Data of the detected scene.
     */
    public val SCENE_DETECT_DATA: TagInfoLongs = TagInfoLongs(
        0x1033, "SceneDetectData", 720,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The compression ratio of the image.
     */
    public val COMPRESSION_RATIO: TagInfoRationals = TagInfoRationals(
        0x1034, "CompressionRatio", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusPreviewImageValid].
     */
    public val PREVIEW_IMAGE_VALID: TagInfoLong = TagInfoLong(
        0x1035, "PreviewImageValid",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The offset of the preview image data.
     */
    public val PREVIEW_IMAGE_START_2: TagInfoLong = TagInfoLong(
        0x1036, "PreviewImageStart",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The length of the preview image data.
     */
    public val PREVIEW_IMAGE_LENGTH_2: TagInfoLong = TagInfoLong(
        0x1037, "PreviewImageLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val AF_RESULT: TagInfoShort = TagInfoShort(
        0x1038, "AFResult",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusCcdScanMode].
     */
    public val CCD_SCAN_MODE: TagInfoShort = TagInfoShort(
        0x1039, "CCDScanMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * See [OlympusNoiseReduction].
     */
    public val NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x103a, "NoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The focus step position at infinity.
     */
    public val FOCUS_STEP_INFINITY: TagInfoShort = TagInfoShort(
        0x103b, "FocusStepInfinity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /**
     * The focus step position at the near distance.
     */
    public val FOCUS_STEP_NEAR: TagInfoShort = TagInfoShort(
        0x103c, "FocusStepNear",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val LIGHT_VALUE_CENTER: TagInfoSRationals = TagInfoSRationals(
        0x103d, "LightValueCenter", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val LIGHT_VALUE_PERIPHERY: TagInfoSRationals = TagInfoSRationals(
        0x103e, "LightValuePeriphery", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    public val FIELD_COUNT: TagInfoShort = TagInfoShort(
        0x103f, "FieldCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS
    )

    /*
     * Pointers to the sub-IFDs of the MakerNote, which are not interpreted.
     */

    public val EQUIPMENT_IFD: TagInfoLong = TagInfoLong(
        0x2010, "EquipmentIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val CAMERA_SETTINGS_IFD: TagInfoLong = TagInfoLong(
        0x2020, "CameraSettingsIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val RAW_DEVELOPMENT_IFD: TagInfoLong = TagInfoLong(
        0x2030, "RawDevelopmentIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val RAW_DEV_2_IFD: TagInfoLong = TagInfoLong(
        0x2031, "RawDev2IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val IMAGE_PROCESSING_IFD: TagInfoLong = TagInfoLong(
        0x2040, "ImageProcessingIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val FOCUS_INFO_IFD: TagInfoLong = TagInfoLong(
        0x2050, "FocusInfoIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2100_IFD: TagInfoLong = TagInfoLong(
        0x2100, "Olympus2100IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2200_IFD: TagInfoLong = TagInfoLong(
        0x2200, "Olympus2200IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2300_IFD: TagInfoLong = TagInfoLong(
        0x2300, "Olympus2300IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2400_IFD: TagInfoLong = TagInfoLong(
        0x2400, "Olympus2400IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2500_IFD: TagInfoLong = TagInfoLong(
        0x2500, "Olympus2500IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2600_IFD: TagInfoLong = TagInfoLong(
        0x2600, "Olympus2600IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2700_IFD: TagInfoLong = TagInfoLong(
        0x2700, "Olympus2700IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2800_IFD: TagInfoLong = TagInfoLong(
        0x2800, "Olympus2800IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val OLYMPUS_2900_IFD: TagInfoLong = TagInfoLong(
        0x2900, "Olympus2900IFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val RAW_INFO_IFD: TagInfoLong = TagInfoLong(
        0x3000, "RawInfoIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val MAIN_INFO_IFD: TagInfoLong = TagInfoLong(
        0x4000, "MainInfoIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val UNKNOWN_INFO_IFD: TagInfoLong = TagInfoLong(
        0x5000, "UnknownInfoIFD",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS,
        isOffset = true
    )

    public val ALL: List<TagInfo> = listOf(
        MAKER_NOTE_VERSION, MINOLTA_CAMERA_SETTINGS_OLD,
        MINOLTA_CAMERA_SETTINGS, COMPRESSED_IMAGE_SIZE,
        PREVIEW_IMAGE_DATA, PREVIEW_IMAGE_START, PREVIEW_IMAGE_LENGTH,
        THUMBNAIL_IMAGE, BODY_FIRMWARE_VERSION,
        SPECIAL_MODE, QUALITY, MACRO, BW_MODE, DIGITAL_ZOOM,
        FOCAL_PLANE_DIAGONAL, LENS_DISTORTION_PARAMS, CAMERA_TYPE,
        TEXT_INFO, CAMERA_ID, EPSON_IMAGE_WIDTH, EPSON_IMAGE_HEIGHT,
        EPSON_SOFTWARE, PREVIEW_IMAGE,
        PRE_CAPTURE_FRAMES, WHITE_BOARD, ONE_TOUCH_WB,
        WHITE_BALANCE_BRACKET, WHITE_BALANCE_BIAS,
        SENSOR_AREA, BLACK_LEVEL, SCENE_MODE, SERIAL_NUMBER, FIRMWARE,
        PRINT_IM, DATA_DUMP, DATA_DUMP_2,
        ZOOMED_PREVIEW_START, ZOOMED_PREVIEW_LENGTH, ZOOMED_PREVIEW_SIZE,
        SHUTTER_SPEED_VALUE, ISO_VALUE, APERTURE_VALUE, BRIGHTNESS_VALUE,
        FLASH_MODE, FLASH_DEVICE, EXPOSURE_COMPENSATION,
        SENSOR_TEMPERATURE, LENS_TEMPERATURE, LIGHT_CONDITION,
        FOCUS_RANGE, FOCUS_MODE, MANUAL_FOCUS_DISTANCE, ZOOM_STEP_COUNT,
        FOCUS_STEP_COUNT, SHARPNESS, FLASH_CHARGE_LEVEL, COLOR_MATRIX,
        BLACK_LEVEL_2, COLOR_TEMPERATURE_BG, COLOR_TEMPERATURE_RG,
        WB_MODE, RED_BALANCE, BLUE_BALANCE, COLOR_MATRIX_NUMBER,
        SERIAL_NUMBER_2,
        EXTERNAL_FLASH_AE1_0, EXTERNAL_FLASH_AE2_0,
        INTERNAL_FLASH_AE1_0, INTERNAL_FLASH_AE2_0,
        EXTERNAL_FLASH_AE1, EXTERNAL_FLASH_AE2,
        INTERNAL_FLASH_AE1, INTERNAL_FLASH_AE2,
        FLASH_EXPOSURE_COMP, INTERNAL_FLASH_TABLE, EXTERNAL_FLASH_G_VALUE,
        EXTERNAL_FLASH_BOUNCE, EXTERNAL_FLASH_ZOOM, EXTERNAL_FLASH_MODE,
        CONTRAST, SHARPNESS_FACTOR, COLOR_CONTROL, VALID_BITS,
        CORING_FILTER, OLYMPUS_IMAGE_WIDTH, OLYMPUS_IMAGE_HEIGHT,
        SCENE_DETECT, SCENE_AREA, SCENE_DETECT_DATA, COMPRESSION_RATIO,
        PREVIEW_IMAGE_VALID, PREVIEW_IMAGE_START_2, PREVIEW_IMAGE_LENGTH_2,
        AF_RESULT, CCD_SCAN_MODE, NOISE_REDUCTION,
        FOCUS_STEP_INFINITY, FOCUS_STEP_NEAR,
        LIGHT_VALUE_CENTER, LIGHT_VALUE_PERIPHERY, FIELD_COUNT,
        EQUIPMENT_IFD, CAMERA_SETTINGS_IFD, RAW_DEVELOPMENT_IFD,
        RAW_DEV_2_IFD, IMAGE_PROCESSING_IFD, FOCUS_INFO_IFD,
        OLYMPUS_2100_IFD, OLYMPUS_2200_IFD, OLYMPUS_2300_IFD,
        OLYMPUS_2400_IFD, OLYMPUS_2500_IFD, OLYMPUS_2600_IFD,
        OLYMPUS_2700_IFD, OLYMPUS_2800_IFD, OLYMPUS_2900_IFD,
        RAW_INFO_IFD, MAIN_INFO_IFD, UNKNOWN_INFO_IFD
    )
}
