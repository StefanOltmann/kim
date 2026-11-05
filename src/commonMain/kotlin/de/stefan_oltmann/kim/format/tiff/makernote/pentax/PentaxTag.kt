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
package de.stefan_oltmann.kim.format.tiff.makernote.pentax

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Pentax MakerNote tags, generated from the ExifTool 13.59 reference
 * dumps of the corpus files, so the names match the reference decoder
 * exactly.
 *
 * See https://exiftool.sourceforge.net/TagNames/Pentax.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object PentaxTag {

    public val PENTAX_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0000, "PentaxVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX_MODEL_TYPE: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "PentaxModelType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PREVIEW_IMAGE_SIZE: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "PreviewImageSize", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PREVIEW_IMAGE_LENGTH: TagInfoUndefineds = TagInfoUndefineds(
        0x0003, "PreviewImageLength", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PREVIEW_IMAGE_START: TagInfoUndefineds = TagInfoUndefineds(
        0x0004, "PreviewImageStart", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX_MODEL_I_D: TagInfoUndefineds = TagInfoUndefineds(
        0x0005, "PentaxModelID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DATE: TagInfoUndefineds = TagInfoUndefineds(
        0x0006, "Date", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0007, "Time", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val QUALITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0008, "Quality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX_IMAGE_SIZE: TagInfoUndefineds = TagInfoUndefineds(
        0x0009, "PentaxImageSize", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PICTURE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x000b, "PictureMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FLASH_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x000c, "FlashMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FOCUS_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x000d, "FocusMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val A_F_POINT_SELECTED: TagInfoUndefineds = TagInfoUndefineds(
        0x000e, "AFPointSelected", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val A_F_POINTS_IN_FOCUS: TagInfoUndefineds = TagInfoUndefineds(
        0x000f, "AFPointsInFocus", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FOCUS_POSITION: TagInfoUndefineds = TagInfoUndefineds(
        0x0010, "FocusPosition", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val EXPOSURE_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0012, "ExposureTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val F_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0013, "FNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val I_S_O: TagInfoUndefineds = TagInfoUndefineds(
        0x0014, "ISO", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val LIGHT_READING: TagInfoUndefineds = TagInfoUndefineds(
        0x0015, "LightReading", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val EXPOSURE_COMPENSATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0016, "ExposureCompensation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val METERING_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0017, "MeteringMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val AUTO_BRACKETING: TagInfoUndefineds = TagInfoUndefineds(
        0x0018, "AutoBracketing", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val WHITE_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x0019, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val WHITE_BALANCE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x001a, "WhiteBalanceMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val BLUE_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x001b, "BlueBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val RED_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x001c, "RedBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FOCAL_LENGTH: TagInfoUndefineds = TagInfoUndefineds(
        0x001d, "FocalLength", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DIGITAL_ZOOM: TagInfoUndefineds = TagInfoUndefineds(
        0x001e, "DigitalZoom", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SATURATION: TagInfoUndefineds = TagInfoUndefineds(
        0x001f, "Saturation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CONTRAST: TagInfoUndefineds = TagInfoUndefineds(
        0x0020, "Contrast", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SHARPNESS: TagInfoUndefineds = TagInfoUndefineds(
        0x0021, "Sharpness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val WORLD_TIME_LOCATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0022, "WorldTimeLocation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val HOMETOWN_CITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0023, "HometownCity", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DESTINATION_CITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0024, "DestinationCity", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val HOMETOWN_D_S_T: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "HometownDST", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DESTINATION_D_S_T: TagInfoUndefineds = TagInfoUndefineds(
        0x0026, "DestinationDST", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val D_S_P_FIRMWARE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0027, "DSPFirmwareVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val EFFECTIVE_L_V: TagInfoUndefineds = TagInfoUndefineds(
        0x002d, "EffectiveLV", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val IMAGE_EDITING: TagInfoUndefineds = TagInfoUndefineds(
        0x0032, "ImageEditing", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PICTURE_MODE_0033: TagInfoUndefineds = TagInfoUndefineds(
        0x0033, "PictureMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DRIVE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0034, "DriveMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val COLOR_SPACE: TagInfoUndefineds = TagInfoUndefineds(
        0x0037, "ColorSpace", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DATA_SCALING: TagInfoUndefineds = TagInfoUndefineds(
        0x003d, "DataScaling", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SENSITIVITY_ADJUST: TagInfoUndefineds = TagInfoUndefineds(
        0x0040, "SensitivityAdjust", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val IMAGE_EDIT_COUNT: TagInfoUndefineds = TagInfoUndefineds(
        0x0041, "ImageEditCount", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CAMERA_TEMPERATURE: TagInfoUndefineds = TagInfoUndefineds(
        0x0047, "CameraTemperature", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val A_E_LOCK: TagInfoUndefineds = TagInfoUndefineds(
        0x0048, "AELock", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val NOISE_REDUCTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0049, "NoiseReduction", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FLASH_EXPOSURE_COMP: TagInfoUndefineds = TagInfoUndefineds(
        0x004d, "FlashExposureComp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val IMAGE_TONE: TagInfoUndefineds = TagInfoUndefineds(
        0x004f, "ImageTone", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val COLOR_TEMPERATURE: TagInfoUndefineds = TagInfoUndefineds(
        0x0050, "ColorTemperature", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SHUTTER_COUNT: TagInfoUndefineds = TagInfoUndefineds(
        0x005d, "ShutterCount", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_5E: TagInfoUndefineds = TagInfoUndefineds(
        0x005e, "Pentax_0x005e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_5F: TagInfoUndefineds = TagInfoUndefineds(
        0x005f, "Pentax_0x005f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_6_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0061, "Pentax_0x0061", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val RAW_DEVELOPMENT_PROCESS: TagInfoUndefineds = TagInfoUndefineds(
        0x0062, "RawDevelopmentProcess", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_6_6: TagInfoUndefineds = TagInfoUndefineds(
        0x0066, "Pentax_0x0066", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val HUE: TagInfoUndefineds = TagInfoUndefineds(
        0x0067, "Hue", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val DYNAMIC_RANGE_EXPANSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0069, "DynamicRangeExpansion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_6A: TagInfoUndefineds = TagInfoUndefineds(
        0x006a, "Pentax_0x006a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val HIGH_LOW_KEY_ADJ: TagInfoUndefineds = TagInfoUndefineds(
        0x006c, "HighLowKeyAdj", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CONTRAST_HIGHLIGHT: TagInfoUndefineds = TagInfoUndefineds(
        0x006d, "ContrastHighlight", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CONTRAST_SHADOW: TagInfoUndefineds = TagInfoUndefineds(
        0x006e, "ContrastShadow", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CONTRAST_HIGHLIGHT_SHADOW_ADJ: TagInfoUndefineds = TagInfoUndefineds(
        0x006f, "ContrastHighlightShadowAdj", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FINE_SHARPNESS: TagInfoUndefineds = TagInfoUndefineds(
        0x0070, "FineSharpness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val HIGH_I_S_O_NOISE_REDUCTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0071, "HighISONoiseReduction", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val A_F_ADJUSTMENT: TagInfoUndefineds = TagInfoUndefineds(
        0x0072, "AFAdjustment", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val MONOCHROME_FILTER_EFFECT: TagInfoUndefineds = TagInfoUndefineds(
        0x0073, "MonochromeFilterEffect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val MONOCHROME_TONING: TagInfoUndefineds = TagInfoUndefineds(
        0x0074, "MonochromeToning", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_7_5: TagInfoUndefineds = TagInfoUndefineds(
        0x0075, "Pentax_0x0075", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FACE_DETECT: TagInfoUndefineds = TagInfoUndefineds(
        0x0076, "FaceDetect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FACE_DETECT_FRAME_SIZE: TagInfoUndefineds = TagInfoUndefineds(
        0x0077, "FaceDetectFrameSize", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_7_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0078, "Pentax_0x0078", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SHADOW_CORRECTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0079, "ShadowCorrection", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val I_S_O_AUTO_MIN_SPEED: TagInfoUndefineds = TagInfoUndefineds(
        0x007a, "ISOAutoMinSpeed", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CROSS_PROCESS: TagInfoUndefineds = TagInfoUndefineds(
        0x007b, "CrossProcess", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_7C: TagInfoUndefineds = TagInfoUndefineds(
        0x007c, "Pentax_0x007c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val BLEACH_BYPASS_TONING: TagInfoUndefineds = TagInfoUndefineds(
        0x007f, "BleachBypassToning", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val ASPECT_RATIO: TagInfoUndefineds = TagInfoUndefineds(
        0x0080, "AspectRatio", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val H_D_R: TagInfoUndefineds = TagInfoUndefineds(
        0x0085, "HDR", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_8_6: TagInfoUndefineds = TagInfoUndefineds(
        0x0086, "Pentax_0x0086", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SHUTTER_TYPE: TagInfoUndefineds = TagInfoUndefineds(
        0x0087, "ShutterType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val INTERVAL_SHOOTING: TagInfoUndefineds = TagInfoUndefineds(
        0x0092, "IntervalShooting", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_9_3: TagInfoUndefineds = TagInfoUndefineds(
        0x0093, "Pentax_0x0093", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SKIN_TONE_CORRECTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0095, "SkinToneCorrection", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CLARITY_CONTROL: TagInfoUndefineds = TagInfoUndefineds(
        0x0096, "ClarityControl", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_0_9_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0098, "Pentax_0x0098", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_1FA: TagInfoUndefineds = TagInfoUndefineds(
        0x01fa, "Pentax_0x01fa", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_1FB: TagInfoUndefineds = TagInfoUndefineds(
        0x01fb, "Pentax_0x01fb", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_1FC: TagInfoUndefineds = TagInfoUndefineds(
        0x01fc, "Pentax_0x01fc", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_1FD: TagInfoUndefineds = TagInfoUndefineds(
        0x01fd, "Pentax_0x01fd", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_1FE: TagInfoUndefineds = TagInfoUndefineds(
        0x01fe, "Pentax_0x01fe", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val BLACK_POINT: TagInfoUndefineds = TagInfoUndefineds(
        0x0200, "BlackPoint", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val WHITE_POINT: TagInfoUndefineds = TagInfoUndefineds(
        0x0201, "WhitePoint", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val FLASH_METERING_SEGMENTS: TagInfoUndefineds = TagInfoUndefineds(
        0x020a, "FlashMeteringSegments", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SLAVE_FLASH_METERING_SEGMENTS: TagInfoUndefineds = TagInfoUndefineds(
        0x020b, "SlaveFlashMeteringSegments", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_1A: TagInfoUndefineds = TagInfoUndefineds(
        0x021a, "Pentax_0x021a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SATURATION_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x021b, "SaturationInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val COLOR_MATRIX_A_2: TagInfoUndefineds = TagInfoUndefineds(
        0x021c, "ColorMatrixA2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val COLOR_MATRIX_B_2: TagInfoUndefineds = TagInfoUndefineds(
        0x021d, "ColorMatrixB2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_1E: TagInfoUndefineds = TagInfoUndefineds(
        0x021e, "Pentax_0x021e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_2_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0227, "Pentax_0x0227", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_2_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0228, "Pentax_0x0228", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val SERIAL_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0229, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_2C: TagInfoUndefineds = TagInfoUndefineds(
        0x022c, "Pentax_0x022c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_2D: TagInfoUndefineds = TagInfoUndefineds(
        0x022d, "Pentax_0x022d", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val CONTRAST_DETECT_A_F_AREA: TagInfoUndefineds = TagInfoUndefineds(
        0x0231, "ContrastDetectAFArea", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0232, "Pentax_0x0232", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3_3: TagInfoUndefineds = TagInfoUndefineds(
        0x0233, "Pentax_0x0233", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3_4: TagInfoUndefineds = TagInfoUndefineds(
        0x0234, "Pentax_0x0234", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3B: TagInfoUndefineds = TagInfoUndefineds(
        0x023b, "Pentax_0x023b", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3C: TagInfoUndefineds = TagInfoUndefineds(
        0x023c, "Pentax_0x023c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3D: TagInfoUndefineds = TagInfoUndefineds(
        0x023d, "Pentax_0x023d", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_3E: TagInfoUndefineds = TagInfoUndefineds(
        0x023e, "Pentax_0x023e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_4_0: TagInfoUndefineds = TagInfoUndefineds(
        0x0240, "Pentax_0x0240", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_4_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0241, "Pentax_0x0241", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_4_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0242, "Pentax_0x0242", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0X_0_2_4_4: TagInfoUndefineds = TagInfoUndefineds(
        0x0244, "Pentax_0x0244", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val PENTAX__0XFFFE: TagInfoUndefineds = TagInfoUndefineds(
        0xfffe, "Pentax_0xfffe", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PENTAX
    )

    public val ALL: List<TagInfo> = listOf(
        PENTAX_VERSION,
        PENTAX_MODEL_TYPE,
        PREVIEW_IMAGE_SIZE,
        PREVIEW_IMAGE_LENGTH,
        PREVIEW_IMAGE_START,
        PENTAX_MODEL_I_D,
        DATE,
        TIME,
        QUALITY,
        PENTAX_IMAGE_SIZE,
        PICTURE_MODE,
        FLASH_MODE,
        FOCUS_MODE,
        A_F_POINT_SELECTED,
        A_F_POINTS_IN_FOCUS,
        FOCUS_POSITION,
        EXPOSURE_TIME,
        F_NUMBER,
        I_S_O,
        LIGHT_READING,
        EXPOSURE_COMPENSATION,
        METERING_MODE,
        AUTO_BRACKETING,
        WHITE_BALANCE,
        WHITE_BALANCE_MODE,
        BLUE_BALANCE,
        RED_BALANCE,
        FOCAL_LENGTH,
        DIGITAL_ZOOM,
        SATURATION,
        CONTRAST,
        SHARPNESS,
        WORLD_TIME_LOCATION,
        HOMETOWN_CITY,
        DESTINATION_CITY,
        HOMETOWN_D_S_T,
        DESTINATION_D_S_T,
        D_S_P_FIRMWARE_VERSION,
        EFFECTIVE_L_V,
        IMAGE_EDITING,
        PICTURE_MODE_0033,
        DRIVE_MODE,
        COLOR_SPACE,
        DATA_SCALING,
        SENSITIVITY_ADJUST,
        IMAGE_EDIT_COUNT,
        CAMERA_TEMPERATURE,
        A_E_LOCK,
        NOISE_REDUCTION,
        FLASH_EXPOSURE_COMP,
        IMAGE_TONE,
        COLOR_TEMPERATURE,
        SHUTTER_COUNT,
        PENTAX__0X_0_0_5E,
        PENTAX__0X_0_0_5F,
        PENTAX__0X_0_0_6_1,
        RAW_DEVELOPMENT_PROCESS,
        PENTAX__0X_0_0_6_6,
        HUE,
        DYNAMIC_RANGE_EXPANSION,
        PENTAX__0X_0_0_6A,
        HIGH_LOW_KEY_ADJ,
        CONTRAST_HIGHLIGHT,
        CONTRAST_SHADOW,
        CONTRAST_HIGHLIGHT_SHADOW_ADJ,
        FINE_SHARPNESS,
        HIGH_I_S_O_NOISE_REDUCTION,
        A_F_ADJUSTMENT,
        MONOCHROME_FILTER_EFFECT,
        MONOCHROME_TONING,
        PENTAX__0X_0_0_7_5,
        FACE_DETECT,
        FACE_DETECT_FRAME_SIZE,
        PENTAX__0X_0_0_7_8,
        SHADOW_CORRECTION,
        I_S_O_AUTO_MIN_SPEED,
        CROSS_PROCESS,
        PENTAX__0X_0_0_7C,
        BLEACH_BYPASS_TONING,
        ASPECT_RATIO,
        H_D_R,
        PENTAX__0X_0_0_8_6,
        SHUTTER_TYPE,
        INTERVAL_SHOOTING,
        PENTAX__0X_0_0_9_3,
        SKIN_TONE_CORRECTION,
        CLARITY_CONTROL,
        PENTAX__0X_0_0_9_8,
        PENTAX__0X_0_1FA,
        PENTAX__0X_0_1FB,
        PENTAX__0X_0_1FC,
        PENTAX__0X_0_1FD,
        PENTAX__0X_0_1FE,
        BLACK_POINT,
        WHITE_POINT,
        FLASH_METERING_SEGMENTS,
        SLAVE_FLASH_METERING_SEGMENTS,
        PENTAX__0X_0_2_1A,
        SATURATION_INFO,
        COLOR_MATRIX_A_2,
        COLOR_MATRIX_B_2,
        PENTAX__0X_0_2_1E,
        PENTAX__0X_0_2_2_7,
        PENTAX__0X_0_2_2_8,
        SERIAL_NUMBER,
        PENTAX__0X_0_2_2C,
        PENTAX__0X_0_2_2D,
        CONTRAST_DETECT_A_F_AREA,
        PENTAX__0X_0_2_3_2,
        PENTAX__0X_0_2_3_3,
        PENTAX__0X_0_2_3_4,
        PENTAX__0X_0_2_3B,
        PENTAX__0X_0_2_3C,
        PENTAX__0X_0_2_3D,
        PENTAX__0X_0_2_3E,
        PENTAX__0X_0_2_4_0,
        PENTAX__0X_0_2_4_1,
        PENTAX__0X_0_2_4_2,
        PENTAX__0X_0_2_4_4,
        PENTAX__0XFFFE,
    )
}
