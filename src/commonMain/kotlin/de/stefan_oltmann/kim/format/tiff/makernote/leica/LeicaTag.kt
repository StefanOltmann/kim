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
package de.stefan_oltmann.kim.format.tiff.makernote.leica

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Leica MakerNote tags, generated from the ExifTool 13.59 reference
 * dumps of the corpus files, so the names match the reference decoder
 * exactly.
 *
 * See https://exiftool.sourceforge.net/TagNames/Leica.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object LeicaTag {

    public val IMAGE_QUALITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "ImageQuality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FIRMWARE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "FirmwareVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val WHITE_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x0003, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FOCUS_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0007, "FocusMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val A_F_AREA_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x000f, "AFAreaMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val IMAGE_STABILIZATION: TagInfoUndefineds = TagInfoUndefineds(
        0x001a, "ImageStabilization", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val MACRO_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x001c, "MacroMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SHOOTING_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x001f, "ShootingMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val AUDIO: TagInfoUndefineds = TagInfoUndefineds(
        0x0020, "Audio", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_2_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0022, "Panasonic_0x0022", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val WHITE_BALANCE_BIAS: TagInfoUndefineds = TagInfoUndefineds(
        0x0023, "WhiteBalanceBias", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FLASH_BIAS: TagInfoUndefineds = TagInfoUndefineds(
        0x0024, "FlashBias", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val INTERNAL_SERIAL_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "InternalSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC_EXIF_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0026, "PanasonicExifVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val VIDEO_FRAME_RATE: TagInfoUndefineds = TagInfoUndefineds(
        0x0027, "VideoFrameRate", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val COLOR_EFFECT: TagInfoUndefineds = TagInfoUndefineds(
        0x0028, "ColorEffect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TIME_SINCE_POWER_ON: TagInfoUndefineds = TagInfoUndefineds(
        0x0029, "TimeSincePowerOn", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BURST_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x002a, "BurstMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SEQUENCE_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x002b, "SequenceNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CONTRAST_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x002c, "ContrastMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val NOISE_REDUCTION: TagInfoUndefineds = TagInfoUndefineds(
        0x002d, "NoiseReduction", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SELF_TIMER: TagInfoUndefineds = TagInfoUndefineds(
        0x002e, "SelfTimer", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_2F: TagInfoUndefineds = TagInfoUndefineds(
        0x002f, "Panasonic_0x002f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ROTATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0030, "Rotation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val A_F_ASSIST_LAMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0031, "AFAssistLamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val COLOR_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0032, "ColorMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BABY_AGE: TagInfoUndefineds = TagInfoUndefineds(
        0x0033, "BabyAge", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val OPTICAL_ZOOM_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x0034, "OpticalZoomMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CONVERSION_LENS: TagInfoUndefineds = TagInfoUndefineds(
        0x0035, "ConversionLens", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TRAVEL_DAY: TagInfoUndefineds = TagInfoUndefineds(
        0x0036, "TravelDay", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_3_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0037, "Panasonic_0x0037", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BATTERY_LEVEL: TagInfoUndefineds = TagInfoUndefineds(
        0x0038, "BatteryLevel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val WORLD_TIME_LOCATION: TagInfoUndefineds = TagInfoUndefineds(
        0x003a, "WorldTimeLocation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TEXT_STAMP: TagInfoUndefineds = TagInfoUndefineds(
        0x003b, "TextStamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PROGRAM_I_S_O: TagInfoUndefineds = TagInfoUndefineds(
        0x003c, "ProgramISO", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ADVANCED_SCENE_TYPE: TagInfoUndefineds = TagInfoUndefineds(
        0x003d, "AdvancedSceneType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TEXT_STAMP_003e: TagInfoUndefineds = TagInfoUndefineds(
        0x003e, "TextStamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FACES_DETECTED: TagInfoUndefineds = TagInfoUndefineds(
        0x003f, "FacesDetected", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val J_P_E_G_QUALITY: TagInfoUndefineds = TagInfoUndefineds(
        0x0043, "JPEGQuality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC_IMAGE_WIDTH: TagInfoUndefineds = TagInfoUndefineds(
        0x004b, "PanasonicImageWidth", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC_IMAGE_HEIGHT: TagInfoUndefineds = TagInfoUndefineds(
        0x004c, "PanasonicImageHeight", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val A_F_POINT_POSITION: TagInfoUndefineds = TagInfoUndefineds(
        0x004d, "AFPointPosition", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_4F: TagInfoUndefineds = TagInfoUndefineds(
        0x004f, "Panasonic_0x004f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val INTELLIGENT_EXPOSURE: TagInfoUndefineds = TagInfoUndefineds(
        0x005d, "IntelligentExposure", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_5E: TagInfoUndefineds = TagInfoUndefineds(
        0x005e, "Panasonic_0x005e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FLASH_WARNING: TagInfoUndefineds = TagInfoUndefineds(
        0x0062, "FlashWarning", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val RECOGNIZED_FACE_FLAGS: TagInfoUndefineds = TagInfoUndefineds(
        0x0063, "RecognizedFaceFlags", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TITLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0065, "Title", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BABY_NAME: TagInfoUndefineds = TagInfoUndefineds(
        0x0066, "BabyName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val LOCATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0067, "Location", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_6_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0068, "Panasonic_0x0068", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val COUNTRY: TagInfoUndefineds = TagInfoUndefineds(
        0x0069, "Country", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_6A: TagInfoUndefineds = TagInfoUndefineds(
        0x006a, "Panasonic_0x006a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val STATE: TagInfoUndefineds = TagInfoUndefineds(
        0x006b, "State", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_6C: TagInfoUndefineds = TagInfoUndefineds(
        0x006c, "Panasonic_0x006c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CITY: TagInfoUndefineds = TagInfoUndefineds(
        0x006d, "City", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_6E: TagInfoUndefineds = TagInfoUndefineds(
        0x006e, "Panasonic_0x006e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val LANDMARK: TagInfoUndefineds = TagInfoUndefineds(
        0x006f, "Landmark", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val INTELLIGENT_RESOLUTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0070, "IntelligentResolution", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0071, "Panasonic_0x0071", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0072, "Panasonic_0x0072", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7_3: TagInfoUndefineds = TagInfoUndefineds(
        0x0073, "Panasonic_0x0073", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7_4: TagInfoUndefineds = TagInfoUndefineds(
        0x0074, "Panasonic_0x0074", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7_5: TagInfoUndefineds = TagInfoUndefineds(
        0x0075, "Panasonic_0x0075", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val MERGED_IMAGES: TagInfoUndefineds = TagInfoUndefineds(
        0x0076, "MergedImages", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BURST_SPEED: TagInfoUndefineds = TagInfoUndefineds(
        0x0077, "BurstSpeed", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7A: TagInfoUndefineds = TagInfoUndefineds(
        0x007a, "Panasonic_0x007a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CLEAR_RETOUCH: TagInfoUndefineds = TagInfoUndefineds(
        0x007c, "ClearRetouch", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_7F: TagInfoUndefineds = TagInfoUndefineds(
        0x007f, "Panasonic_0x007f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CITY_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0080, "City2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0081, "Panasonic_0x0081", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0082, "Panasonic_0x0082", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_3: TagInfoUndefineds = TagInfoUndefineds(
        0x0083, "Panasonic_0x0083", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_4: TagInfoUndefineds = TagInfoUndefineds(
        0x0084, "Panasonic_0x0084", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_5: TagInfoUndefineds = TagInfoUndefineds(
        0x0085, "Panasonic_0x0085", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_8_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0087, "Panasonic_0x0087", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val W_B_SHIFT_INTELLIGENT_AUTO: TagInfoUndefineds = TagInfoUndefineds(
        0x008b, "WBShiftIntelligentAuto", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ACCELEROMETER_Z: TagInfoUndefineds = TagInfoUndefineds(
        0x008c, "AccelerometerZ", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ACCELEROMETER_X: TagInfoUndefineds = TagInfoUndefineds(
        0x008d, "AccelerometerX", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ACCELEROMETER_Y: TagInfoUndefineds = TagInfoUndefineds(
        0x008e, "AccelerometerY", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CAMERA_ORIENTATION: TagInfoUndefineds = TagInfoUndefineds(
        0x008f, "CameraOrientation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ROLL_ANGLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0090, "RollAngle", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PITCH_ANGLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0091, "PitchAngle", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val W_B_SHIFT_CREATIVE_CONTROL: TagInfoUndefineds = TagInfoUndefineds(
        0x0092, "WBShiftCreativeControl", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SWEEP_PANORAMA_DIRECTION: TagInfoUndefineds = TagInfoUndefineds(
        0x0093, "SweepPanoramaDirection", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SWEEP_PANORAMA_FIELD_OF_VIEW: TagInfoUndefineds = TagInfoUndefineds(
        0x0094, "SweepPanoramaFieldOfView", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_9_7: TagInfoUndefineds = TagInfoUndefineds(
        0x0097, "Panasonic_0x0097", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0_9_8: TagInfoUndefineds = TagInfoUndefineds(
        0x0098, "Panasonic_0x0098", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val INTERNAL_N_D_FILTER: TagInfoUndefineds = TagInfoUndefineds(
        0x009d, "InternalNDFilter", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val FILTER_EFFECT: TagInfoUndefineds = TagInfoUndefineds(
        0x00a1, "FilterEffect", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val CLEAR_RETOUCH_VALUE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a3, "ClearRetouchValue", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val OUTPUT_L_U_T: TagInfoUndefineds = TagInfoUndefineds(
        0x00a7, "OutputLUT", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0A_8: TagInfoUndefineds = TagInfoUndefineds(
        0x00a8, "Panasonic_0x00a8", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_0_0A_9: TagInfoUndefineds = TagInfoUndefineds(
        0x00a9, "Panasonic_0x00a9", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TOUCH_A_E: TagInfoUndefineds = TagInfoUndefineds(
        0x00ab, "TouchAE", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x8000, "MakerNoteVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val SCENE_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x8001, "SceneMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val HIGHLIGHT_WARNING: TagInfoUndefineds = TagInfoUndefineds(
        0x8002, "HighlightWarning", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val DARK_FOCUS_ENVIRONMENT: TagInfoUndefineds = TagInfoUndefineds(
        0x8003, "DarkFocusEnvironment", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val W_B_RED_LEVEL: TagInfoUndefineds = TagInfoUndefineds(
        0x8004, "WBRedLevel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val W_B_GREEN_LEVEL: TagInfoUndefineds = TagInfoUndefineds(
        0x8005, "WBGreenLevel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val W_B_BLUE_LEVEL: TagInfoUndefineds = TagInfoUndefineds(
        0x8006, "WBBlueLevel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val PANASONIC__0X_8_0_0_7: TagInfoUndefineds = TagInfoUndefineds(
        0x8007, "Panasonic_0x8007", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TEXT_STAMP_8008: TagInfoUndefineds = TagInfoUndefineds(
        0x8008, "TextStamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val TEXT_STAMP_8009: TagInfoUndefineds = TagInfoUndefineds(
        0x8009, "TextStamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val BABY_AGE_8010: TagInfoUndefineds = TagInfoUndefineds(
        0x8010, "BabyAge", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_LEICA
    )

    public val ALL: List<TagInfo> = listOf(
        IMAGE_QUALITY,
        FIRMWARE_VERSION,
        WHITE_BALANCE,
        FOCUS_MODE,
        A_F_AREA_MODE,
        IMAGE_STABILIZATION,
        MACRO_MODE,
        SHOOTING_MODE,
        AUDIO,
        PANASONIC__0X_0_0_2_2,
        WHITE_BALANCE_BIAS,
        FLASH_BIAS,
        INTERNAL_SERIAL_NUMBER,
        PANASONIC_EXIF_VERSION,
        VIDEO_FRAME_RATE,
        COLOR_EFFECT,
        TIME_SINCE_POWER_ON,
        BURST_MODE,
        SEQUENCE_NUMBER,
        CONTRAST_MODE,
        NOISE_REDUCTION,
        SELF_TIMER,
        PANASONIC__0X_0_0_2F,
        ROTATION,
        A_F_ASSIST_LAMP,
        COLOR_MODE,
        BABY_AGE,
        OPTICAL_ZOOM_MODE,
        CONVERSION_LENS,
        TRAVEL_DAY,
        PANASONIC__0X_0_0_3_7,
        BATTERY_LEVEL,
        WORLD_TIME_LOCATION,
        TEXT_STAMP,
        PROGRAM_I_S_O,
        ADVANCED_SCENE_TYPE,
        TEXT_STAMP_003e,
        FACES_DETECTED,
        J_P_E_G_QUALITY,
        PANASONIC_IMAGE_WIDTH,
        PANASONIC_IMAGE_HEIGHT,
        A_F_POINT_POSITION,
        PANASONIC__0X_0_0_4F,
        INTELLIGENT_EXPOSURE,
        PANASONIC__0X_0_0_5E,
        FLASH_WARNING,
        RECOGNIZED_FACE_FLAGS,
        TITLE,
        BABY_NAME,
        LOCATION,
        PANASONIC__0X_0_0_6_8,
        COUNTRY,
        PANASONIC__0X_0_0_6A,
        STATE,
        PANASONIC__0X_0_0_6C,
        CITY,
        PANASONIC__0X_0_0_6E,
        LANDMARK,
        INTELLIGENT_RESOLUTION,
        PANASONIC__0X_0_0_7_1,
        PANASONIC__0X_0_0_7_2,
        PANASONIC__0X_0_0_7_3,
        PANASONIC__0X_0_0_7_4,
        PANASONIC__0X_0_0_7_5,
        MERGED_IMAGES,
        BURST_SPEED,
        PANASONIC__0X_0_0_7A,
        CLEAR_RETOUCH,
        PANASONIC__0X_0_0_7F,
        CITY_2,
        PANASONIC__0X_0_0_8_1,
        PANASONIC__0X_0_0_8_2,
        PANASONIC__0X_0_0_8_3,
        PANASONIC__0X_0_0_8_4,
        PANASONIC__0X_0_0_8_5,
        PANASONIC__0X_0_0_8_7,
        W_B_SHIFT_INTELLIGENT_AUTO,
        ACCELEROMETER_Z,
        ACCELEROMETER_X,
        ACCELEROMETER_Y,
        CAMERA_ORIENTATION,
        ROLL_ANGLE,
        PITCH_ANGLE,
        W_B_SHIFT_CREATIVE_CONTROL,
        SWEEP_PANORAMA_DIRECTION,
        SWEEP_PANORAMA_FIELD_OF_VIEW,
        PANASONIC__0X_0_0_9_7,
        PANASONIC__0X_0_0_9_8,
        INTERNAL_N_D_FILTER,
        FILTER_EFFECT,
        CLEAR_RETOUCH_VALUE,
        OUTPUT_L_U_T,
        PANASONIC__0X_0_0A_8,
        PANASONIC__0X_0_0A_9,
        TOUCH_A_E,
        MAKER_NOTE_VERSION,
        SCENE_MODE,
        HIGHLIGHT_WARNING,
        DARK_FOCUS_ENVIRONMENT,
        W_B_RED_LEVEL,
        W_B_GREEN_LEVEL,
        W_B_BLUE_LEVEL,
        PANASONIC__0X_8_0_0_7,
        TEXT_STAMP_8008,
        TEXT_STAMP_8009,
        BABY_AGE_8010,
    )
}
