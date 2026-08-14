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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Panasonic MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/Panasonic.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object PanasonicTag {

    /**
     * See [PanasonicImageQuality].
     *
     * Quality of the main image, which may be in a different file.
     */
    public val IMAGE_QUALITY: TagInfoShort = TagInfoShort(
        0x0001, "ImageQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The firmware version of the camera.
     */
    public val FIRMWARE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "FirmwareVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicWhiteBalance].
     */
    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0x0003, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicFocusMode].
     */
    public val FOCUS_MODE: TagInfoShort = TagInfoShort(
        0x0007, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val AF_AREA_MODE: TagInfoBytes = TagInfoBytes(
        0x000f, "AFAreaMode", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicImageStabilization].
     */
    public val IMAGE_STABILIZATION: TagInfoShort = TagInfoShort(
        0x001a, "ImageStabilization",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicMacroMode].
     */
    public val MACRO_MODE: TagInfoShort = TagInfoShort(
        0x001c, "MacroMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicShootingMode].
     */
    public val SHOOTING_MODE: TagInfoShort = TagInfoShort(
        0x001f, "ShootingMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicAudio].
     */
    public val AUDIO: TagInfoShort = TagInfoShort(
        0x0020, "Audio",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * A data dump block recorded by the camera.
     */
    public val DATA_DUMP: TagInfoUndefineds = TagInfoUndefineds(
        0x0021, "DataDump", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0022: TagInfoShort = TagInfoShort(
        0x0022, "Panasonic_0x0022",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The white balance adjustment applied.
     */
    public val WHITE_BALANCE_BIAS: TagInfoSShort = TagInfoSShort(
        0x0023, "WhiteBalanceBias",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The flash bias setting.
     */
    public val FLASH_BIAS: TagInfoSShort = TagInfoSShort(
        0x0024, "FlashBias",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * This number is unique, and contains the date of manufacture, but is not
     * the same as the number printed on the camera body.
     */
    public val INTERNAL_SERIAL_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "InternalSerialNumber", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_EXIF_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0026, "PanasonicExifVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Only valid for older models.
     */
    public val VIDEO_FRAME_RATE: TagInfoShort = TagInfoShort(
        0x0027, "VideoFrameRate",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicColorEffect].
     */
    public val COLOR_EFFECT: TagInfoShort = TagInfoShort(
        0x0028, "ColorEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Time in 1/100 s from when the camera was powered on to when the image is written to the memory card.
     */
    public val TIME_SINCE_POWER_ON: TagInfoLong = TagInfoLong(
        0x0029, "TimeSincePowerOn",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicBurstMode].
     */
    public val BURST_MODE: TagInfoShort = TagInfoShort(
        0x002a, "BurstMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The sequence number of the shot.
     */
    public val SEQUENCE_NUMBER: TagInfoLong = TagInfoLong(
        0x002b, "SequenceNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val CONTRAST_MODE: TagInfoShort = TagInfoShort(
        0x002c, "ContrastMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The noise reduction setting.
     */
    public val NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x002d, "NoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicSelfTimer].
     */
    public val SELF_TIMER: TagInfoShort = TagInfoShort(
        0x002e, "SelfTimer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X002F: TagInfoShort = TagInfoShort(
        0x002f, "Panasonic_0x002f",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicRotation].
     */
    public val ROTATION: TagInfoShort = TagInfoShort(
        0x0030, "Rotation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicAfAssistLamp].
     */
    public val AF_ASSIST_LAMP: TagInfoShort = TagInfoShort(
        0x0031, "AFAssistLamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicColorMode].
     */
    public val COLOR_MODE: TagInfoShort = TagInfoShort(
        0x0032, "ColorMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val BABY_AGE: TagInfoAscii = TagInfoAscii(
        0x0033, "BabyAge", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicOpticalZoomMode].
     */
    public val OPTICAL_ZOOM_MODE: TagInfoShort = TagInfoShort(
        0x0034, "OpticalZoomMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicConversionLens].
     */
    public val CONVERSION_LENS: TagInfoShort = TagInfoShort(
        0x0035, "ConversionLens",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The travel day recorded for the shot.
     */
    public val TRAVEL_DAY: TagInfoShort = TagInfoShort(
        0x0036, "TravelDay",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0037: TagInfoShort = TagInfoShort(
        0x0037, "Panasonic_0x0037",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicBatteryLevel].
     */
    public val BATTERY_LEVEL: TagInfoShort = TagInfoShort(
        0x0038, "BatteryLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The contrast setting of the shot.
     */
    public val CONTRAST: TagInfoShort = TagInfoShort(
        0x0039, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicWorldTimeLocation].
     */
    public val WORLD_TIME_LOCATION: TagInfoShort = TagInfoShort(
        0x003a, "WorldTimeLocation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTextStamp].
     */
    public val TEXT_STAMP: TagInfoShort = TagInfoShort(
        0x003b, "TextStamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The ISO value selected by the program.
     */
    public val PROGRAM_ISO: TagInfoShort = TagInfoShort(
        0x003c, "ProgramISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The advanced scene type of the shot.
     */
    public val ADVANCED_SCENE_TYPE: TagInfoShort = TagInfoShort(
        0x003d, "AdvancedSceneType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTextStamp].
     */
    public val TEXT_STAMP_2: TagInfoShort = TagInfoShort(
        0x003e, "TextStamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The number of faces detected.
     */
    public val FACES_DETECTED: TagInfoShort = TagInfoShort(
        0x003f, "FacesDetected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val SATURATION: TagInfoShort = TagInfoShort(
        0x0040, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val SHARPNESS: TagInfoShort = TagInfoShort(
        0x0041, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicFilmMode].
     */
    public val FILM_MODE: TagInfoShort = TagInfoShort(
        0x0042, "FilmMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicJpegQuality].
     */
    public val JPEG_QUALITY: TagInfoShort = TagInfoShort(
        0x0043, "JPEGQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The color temperature in Kelvin.
     */
    public val COLOR_TEMP_KELVIN: TagInfoShort = TagInfoShort(
        0x0044, "ColorTempKelvin",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicBracketSettings].
     */
    public val BRACKET_SETTINGS: TagInfoShort = TagInfoShort(
        0x0045, "BracketSettings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val WB_SHIFT_AB: TagInfoShort = TagInfoShort(
        0x0046, "WBShiftAB",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val WB_SHIFT_GM: TagInfoShort = TagInfoShort(
        0x0047, "WBShiftGM",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicFlashCurtain].
     */
    public val FLASH_CURTAIN: TagInfoShort = TagInfoShort(
        0x0048, "FlashCurtain",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicLongExposureNoiseReduction].
     */
    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x0049, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X004A: TagInfoShort = TagInfoShort(
        0x004a, "Panasonic_0x004a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0x004b, "PanasonicImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_IMAGE_HEIGHT: TagInfoLong = TagInfoLong(
        0x004c, "PanasonicImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * X Y coordinates of the primary AF area center, in the range 0.0 to 1.0.
     */
    public val AF_POINT_POSITION: TagInfoRationals = TagInfoRationals(
        0x004d, "AFPointPosition", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Information about the faces detected.
     */
    public val FACE_DET_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x004e, "FaceDetInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X004F: TagInfoShort = TagInfoShort(
        0x004f, "Panasonic_0x004f",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0050: TagInfoShort = TagInfoShort(
        0x0050, "Panasonic_0x0050",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The type of the lens used.
     */
    public val LENS_TYPE: TagInfoAscii = TagInfoAscii(
        0x0051, "LensType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The serial number of the lens.
     */
    public val LENS_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0052, "LensSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The type of the accessory attached.
     */
    public val ACCESSORY_TYPE: TagInfoAscii = TagInfoAscii(
        0x0053, "AccessoryType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The serial number of the accessory attached.
     */
    public val ACCESSORY_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0054, "AccessorySerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0055: TagInfoShort = TagInfoShort(
        0x0055, "Panasonic_0x0055",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0057: TagInfoShort = TagInfoShort(
        0x0057, "Panasonic_0x0057",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val TRANSFORM: TagInfoUndefineds = TagInfoUndefineds(
        0x0059, "Transform", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X005A: TagInfoShort = TagInfoShort(
        0x005a, "Panasonic_0x005a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X005B: TagInfoShort = TagInfoShort(
        0x005b, "Panasonic_0x005b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X005C: TagInfoShort = TagInfoShort(
        0x005c, "Panasonic_0x005c",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicIntelligentExposure].
     */
    public val INTELLIGENT_EXPOSURE: TagInfoShort = TagInfoShort(
        0x005d, "IntelligentExposure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X005E: TagInfoUndefineds = TagInfoUndefineds(
        0x005e, "Panasonic_0x005e", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The firmware version of the lens.
     */
    public val LENS_FIRMWARE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0060, "LensFirmwareVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Information about face recognition.
     */
    public val FACE_REC_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0061, "FaceRecInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicFlashWarning].
     */
    public val FLASH_WARNING: TagInfoShort = TagInfoShort(
        0x0062, "FlashWarning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val RECOGNIZED_FACE_FLAGS: TagInfoUndefineds = TagInfoUndefineds(
        0x0063, "RecognizedFaceFlags", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val TITLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0065, "Title", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The name of the baby or pet recorded for the shot.
     */
    public val BABY_NAME: TagInfoUndefineds = TagInfoUndefineds(
        0x0066, "BabyName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LOCATION: TagInfoUndefineds = TagInfoUndefineds(
        0x0067, "Location", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val COUNTRY: TagInfoUndefineds = TagInfoUndefineds(
        0x0069, "Country", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val STATE: TagInfoUndefineds = TagInfoUndefineds(
        0x006b, "State", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val CITY: TagInfoUndefineds = TagInfoUndefineds(
        0x006d, "City", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LANDMARK: TagInfoUndefineds = TagInfoUndefineds(
        0x006f, "Landmark", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicIntelligentResolution].
     */
    public val INTELLIGENT_RESOLUTION: TagInfoByte = TagInfoByte(
        0x0070, "IntelligentResolution",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0072: TagInfoShort = TagInfoShort(
        0x0072, "Panasonic_0x0072",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0073: TagInfoShort = TagInfoShort(
        0x0073, "Panasonic_0x0073",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0074: TagInfoShort = TagInfoShort(
        0x0074, "Panasonic_0x0074",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0075: TagInfoShort = TagInfoShort(
        0x0075, "Panasonic_0x0075",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Number of images in the HDR or Live View Composite picture.
     */
    public val MERGED_IMAGES: TagInfoShort = TagInfoShort(
        0x0076, "MergedImages",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Images per second.
     */
    public val BURST_SPEED: TagInfoShort = TagInfoShort(
        0x0077, "BurstSpeed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicIntelligentDRange].
     */
    public val INTELLIGENT_D_RANGE: TagInfoShort = TagInfoShort(
        0x0079, "IntelligentD-Range",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X007A: TagInfoShort = TagInfoShort(
        0x007a, "Panasonic_0x007a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X007B: TagInfoShort = TagInfoShort(
        0x007b, "Panasonic_0x007b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicClearRetouch].
     */
    public val CLEAR_RETOUCH: TagInfoShort = TagInfoShort(
        0x007c, "ClearRetouch",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X007D: TagInfoShort = TagInfoShort(
        0x007d, "Panasonic_0x007d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X007E: TagInfoShort = TagInfoShort(
        0x007e, "Panasonic_0x007e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val CITY_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0080, "City2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The air pressure measured by the camera.
     */
    public val MANOMETER_PRESSURE: TagInfoShort = TagInfoShort(
        0x0086, "ManometerPressure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicPhotoStyle].
     */
    public val PHOTO_STYLE: TagInfoShort = TagInfoShort(
        0x0089, "PhotoStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicShadingCompensation].
     */
    public val SHADING_COMPENSATION: TagInfoShort = TagInfoShort(
        0x008a, "ShadingCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val WB_SHIFT_INTELLIGENT_AUTO: TagInfoShort = TagInfoShort(
        0x008b, "WBShiftIntelligentAuto",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The acceleration along the up-down axis.
     */
    public val ACCELEROMETER_Z: TagInfoShort = TagInfoShort(
        0x008c, "AccelerometerZ",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The acceleration along the left-right axis.
     */
    public val ACCELEROMETER_X: TagInfoShort = TagInfoShort(
        0x008d, "AccelerometerX",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The acceleration along the front-back axis.
     */
    public val ACCELEROMETER_Y: TagInfoShort = TagInfoShort(
        0x008e, "AccelerometerY",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicCameraOrientation].
     */
    public val CAMERA_ORIENTATION: TagInfoByte = TagInfoByte(
        0x008f, "CameraOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Converted to degrees of clockwise camera rotation.
     */
    public val ROLL_ANGLE: TagInfoShort = TagInfoShort(
        0x0090, "RollAngle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Converted to degrees of upward camera tilt.
     */
    public val PITCH_ANGLE: TagInfoShort = TagInfoShort(
        0x0091, "PitchAngle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val WB_SHIFT_CREATIVE_CONTROL: TagInfoByte = TagInfoByte(
        0x0092, "WBShiftCreativeControl",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicSweepPanoramaDirection].
     */
    public val SWEEP_PANORAMA_DIRECTION: TagInfoByte = TagInfoByte(
        0x0093, "SweepPanoramaDirection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val SWEEP_PANORAMA_FIELD_OF_VIEW: TagInfoShort = TagInfoShort(
        0x0094, "SweepPanoramaFieldOfView",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTimerRecording].
     */
    public val TIMER_RECORDING: TagInfoByte = TagInfoByte(
        0x0096, "TimerRecording",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0097: TagInfoShort = TagInfoShort(
        0x0097, "Panasonic_0x0097",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X0098: TagInfoShort = TagInfoShort(
        0x0098, "Panasonic_0x0098",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The internal ND filter setting.
     */
    public val INTERNAL_ND_FILTER: TagInfoRationals = TagInfoRationals(
        0x009d, "InternalNDFilter", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicHdr].
     */
    public val HDR: TagInfoShort = TagInfoShort(
        0x009e, "HDR",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicShutterType].
     */
    public val SHUTTER_TYPE: TagInfoShort = TagInfoShort(
        0x009f, "ShutterType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val FILTER_EFFECT: TagInfoRationals = TagInfoRationals(
        0x00a1, "FilterEffect", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The value of the clear retouch effect.
     */
    public val CLEAR_RETOUCH_VALUE: TagInfoRationals = TagInfoRationals(
        0x00a3, "ClearRetouchValue", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * 2-column by 432-row binary lookup table of unsigned short values for
     * converting to 16-bit output (1st column) from 14 bits (2nd column)
     * with camera contrast.
     */
    public val OUTPUT_LUT: TagInfoUndefineds = TagInfoUndefineds(
        0x00a7, "OutputLUT", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTouchAE].
     */
    public val TOUCH_AE: TagInfoShort = TagInfoShort(
        0x00ab, "TouchAE",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicMonochromeFilterEffect].
     */
    public val MONOCHROME_FILTER_EFFECT: TagInfoShort = TagInfoShort(
        0x00ac, "MonochromeFilterEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val HIGHLIGHT_SHADOW: TagInfoShorts = TagInfoShorts(
        0x00ad, "HighlightShadow", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val TIME_STAMP: TagInfoAscii = TagInfoAscii(
        0x00af, "TimeStamp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicVideoBurstResolution].
     */
    public val VIDEO_BURST_RESOLUTION: TagInfoShort = TagInfoShort(
        0x00b3, "VideoBurstResolution",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicMultiExposure].
     */
    public val MULTI_EXPOSURE: TagInfoShort = TagInfoShort(
        0x00b4, "MultiExposure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicRedEyeRemoval].
     */
    public val RED_EYE_REMOVAL: TagInfoShort = TagInfoShort(
        0x00b9, "RedEyeRemoval",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val VIDEO_BURST_MODE: TagInfoLong = TagInfoLong(
        0x00bb, "VideoBurstMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicDiffractionCorrection].
     */
    public val DIFFRACTION_CORRECTION: TagInfoShort = TagInfoShort(
        0x00bc, "DiffractionCorrection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val FOCUS_BRACKET: TagInfoShort = TagInfoShort(
        0x00bd, "FocusBracket",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicLongExposureNrUsed].
     */
    public val LONG_EXPOSURE_NR_USED: TagInfoShort = TagInfoShort(
        0x00be, "LongExposureNRUsed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val POST_FOCUS_MERGING: TagInfoLongs = TagInfoLongs(
        0x00bf, "PostFocusMerging", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicVideoPreburst].
     */
    public val VIDEO_PREBURST: TagInfoShort = TagInfoShort(
        0x00c1, "VideoPreburst",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LENS_TYPE_MAKE: TagInfoShort = TagInfoShort(
        0x00c4, "LensTypeMake",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LENS_TYPE_MODEL: TagInfoShort = TagInfoShort(
        0x00c5, "LensTypeModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicSensorType].
     */
    public val SENSOR_TYPE: TagInfoShort = TagInfoShort(
        0x00ca, "SensorType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val ISO: TagInfoLong = TagInfoLong(
        0x00d1, "ISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicMonochromeGrainEffect].
     */
    public val MONOCHROME_GRAIN_EFFECT: TagInfoShort = TagInfoShort(
        0x00d2, "MonochromeGrainEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicHybridLogGamma].
     */
    public val HYBRID_LOG_GAMMA: TagInfoShort = TagInfoShort(
        0x00d4, "HybridLogGamma",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val NOISE_REDUCTION_STRENGTH: TagInfoSRationals = TagInfoSRationals(
        0x00d6, "NoiseReductionStrength", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val AF_AREA_SIZE: TagInfoRationals = TagInfoRationals(
        0x00de, "AFAreaSize", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LENS_TYPE_MODEL_2: TagInfoShort = TagInfoShort(
        0x00e4, "LensTypeModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val MINIMUM_ISO: TagInfoLong = TagInfoLong(
        0x00e8, "MinimumISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicAfSubjectDetection].
     */
    public val AF_SUBJECT_DETECTION: TagInfoShort = TagInfoShort(
        0x00e9, "AFSubjectDetection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicDynamicRangeBoost].
     */
    public val DYNAMIC_RANGE_BOOST: TagInfoShort = TagInfoShort(
        0x00ee, "DynamicRangeBoost",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LUT_1_NAME: TagInfoAscii = TagInfoAscii(
        0x00f1, "LUT1Name", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LUT_1_OPACITY: TagInfoByte = TagInfoByte(
        0x00f3, "LUT1Opacity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LUT_2_NAME: TagInfoAscii = TagInfoAscii(
        0x00f4, "LUT2Name", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val LUT_2_OPACITY: TagInfoByte = TagInfoByte(
        0x00f5, "LUT2Opacity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * Print Image Matching (PrintIM) information.
     */
    public val PRINT_IM: TagInfoUndefineds = TagInfoUndefineds(
        0x0e00, "PrintIM", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val TIME_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x2003, "TimeInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The version of the Panasonic maker note format.
     */
    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x8000, "MakerNoteVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicShootingMode].
     */
    public val SCENE_MODE: TagInfoShort = TagInfoShort(
        0x8001, "SceneMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicHighlightWarning].
     */
    public val HIGHLIGHT_WARNING: TagInfoShort = TagInfoShort(
        0x8002, "HighlightWarning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicDarkFocusEnvironment].
     */
    public val DARK_FOCUS_ENVIRONMENT: TagInfoShort = TagInfoShort(
        0x8003, "DarkFocusEnvironment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The red white balance level.
     */
    public val WB_RED_LEVEL: TagInfoShort = TagInfoShort(
        0x8004, "WBRedLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The green white balance level.
     */
    public val WB_GREEN_LEVEL: TagInfoShort = TagInfoShort(
        0x8005, "WBGreenLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * The blue white balance level.
     */
    public val WB_BLUE_LEVEL: TagInfoShort = TagInfoShort(
        0x8006, "WBBlueLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val PANASONIC_0X8007: TagInfoShort = TagInfoShort(
        0x8007, "Panasonic_0x8007",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTextStamp].
     */
    public val TEXT_STAMP_3: TagInfoShort = TagInfoShort(
        0x8008, "TextStamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    /**
     * See [PanasonicTextStamp].
     */
    public val TEXT_STAMP_4: TagInfoShort = TagInfoShort(
        0x8009, "TextStamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val BABY_AGE_2: TagInfoAscii = TagInfoAscii(
        0x8010, "BabyAge", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val TRANSFORM_2: TagInfoUndefineds = TagInfoUndefineds(
        0x8012, "Transform", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC
    )

    public val ALL: List<TagInfo> = listOf(
        IMAGE_QUALITY, FIRMWARE_VERSION, WHITE_BALANCE, FOCUS_MODE,
        AF_AREA_MODE, IMAGE_STABILIZATION, MACRO_MODE, SHOOTING_MODE,
        AUDIO, DATA_DUMP, PANASONIC_0X0022, WHITE_BALANCE_BIAS,
        FLASH_BIAS, INTERNAL_SERIAL_NUMBER, PANASONIC_EXIF_VERSION,
        VIDEO_FRAME_RATE, COLOR_EFFECT, TIME_SINCE_POWER_ON, BURST_MODE,
        SEQUENCE_NUMBER, CONTRAST_MODE, NOISE_REDUCTION, SELF_TIMER,
        PANASONIC_0X002F, ROTATION, AF_ASSIST_LAMP, COLOR_MODE, BABY_AGE,
        OPTICAL_ZOOM_MODE, CONVERSION_LENS, TRAVEL_DAY, PANASONIC_0X0037,
        BATTERY_LEVEL, CONTRAST, WORLD_TIME_LOCATION, TEXT_STAMP,
        PROGRAM_ISO, ADVANCED_SCENE_TYPE, TEXT_STAMP_2, FACES_DETECTED,
        SATURATION, SHARPNESS, FILM_MODE, JPEG_QUALITY, COLOR_TEMP_KELVIN,
        BRACKET_SETTINGS, WB_SHIFT_AB, WB_SHIFT_GM, FLASH_CURTAIN,
        LONG_EXPOSURE_NOISE_REDUCTION, PANASONIC_0X004A,
        PANASONIC_IMAGE_WIDTH, PANASONIC_IMAGE_HEIGHT, AF_POINT_POSITION,
        FACE_DET_INFO, PANASONIC_0X004F, PANASONIC_0X0050,
        LENS_TYPE, LENS_SERIAL_NUMBER, ACCESSORY_TYPE,
        ACCESSORY_SERIAL_NUMBER,
        PANASONIC_0X0055, PANASONIC_0X0057, TRANSFORM,
        PANASONIC_0X005A, PANASONIC_0X005B, PANASONIC_0X005C,
        INTELLIGENT_EXPOSURE, PANASONIC_0X005E, LENS_FIRMWARE_VERSION,
        FACE_REC_INFO, FLASH_WARNING, RECOGNIZED_FACE_FLAGS,
        TITLE, BABY_NAME, LOCATION, COUNTRY, STATE, CITY, LANDMARK,
        INTELLIGENT_RESOLUTION,
        PANASONIC_0X0072, PANASONIC_0X0073, PANASONIC_0X0074,
        PANASONIC_0X0075, MERGED_IMAGES, BURST_SPEED, INTELLIGENT_D_RANGE,
        PANASONIC_0X007A, PANASONIC_0X007B, CLEAR_RETOUCH,
        PANASONIC_0X007D, PANASONIC_0X007E, CITY_2, MANOMETER_PRESSURE,
        PHOTO_STYLE, SHADING_COMPENSATION, WB_SHIFT_INTELLIGENT_AUTO,
        ACCELEROMETER_Z, ACCELEROMETER_X, ACCELEROMETER_Y,
        CAMERA_ORIENTATION, ROLL_ANGLE, PITCH_ANGLE,
        WB_SHIFT_CREATIVE_CONTROL, SWEEP_PANORAMA_DIRECTION,
        SWEEP_PANORAMA_FIELD_OF_VIEW, TIMER_RECORDING,
        PANASONIC_0X0097, PANASONIC_0X0098, INTERNAL_ND_FILTER, HDR,
        SHUTTER_TYPE, FILTER_EFFECT, CLEAR_RETOUCH_VALUE, OUTPUT_LUT,
        TOUCH_AE, MONOCHROME_FILTER_EFFECT, HIGHLIGHT_SHADOW, TIME_STAMP,
        VIDEO_BURST_RESOLUTION, MULTI_EXPOSURE, RED_EYE_REMOVAL,
        VIDEO_BURST_MODE, DIFFRACTION_CORRECTION, FOCUS_BRACKET,
        LONG_EXPOSURE_NR_USED, POST_FOCUS_MERGING, VIDEO_PREBURST,
        LENS_TYPE_MAKE, LENS_TYPE_MODEL, SENSOR_TYPE, ISO,
        MONOCHROME_GRAIN_EFFECT, HYBRID_LOG_GAMMA,
        NOISE_REDUCTION_STRENGTH, AF_AREA_SIZE, LENS_TYPE_MODEL_2,
        MINIMUM_ISO, AF_SUBJECT_DETECTION, DYNAMIC_RANGE_BOOST,
        LUT_1_NAME, LUT_1_OPACITY, LUT_2_NAME, LUT_2_OPACITY,
        PRINT_IM, TIME_INFO,
        MAKER_NOTE_VERSION, SCENE_MODE, HIGHLIGHT_WARNING,
        DARK_FOCUS_ENVIRONMENT, WB_RED_LEVEL, WB_GREEN_LEVEL,
        WB_BLUE_LEVEL, PANASONIC_0X8007, TEXT_STAMP_3, TEXT_STAMP_4,
        BABY_AGE_2, TRANSFORM_2
    )
}
