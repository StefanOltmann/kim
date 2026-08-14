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
package de.stefan_oltmann.kim.format.tiff.makernote.canon

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Canon MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object CanonTag {

    public val CANON_CAMERA_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "CanonCameraSettings", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_FOCAL_LENGTH: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "CanonFocalLength", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Details of the flash behavior during the shot.
     */
    public val CANON_FLASH_INFO: TagInfoShorts = TagInfoShorts(
        0x0003, "CanonFlashInfo", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_SHOT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0004, "CanonShotInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_PANORAMA: TagInfoUndefineds = TagInfoUndefineds(
        0x0005, "CanonPanorama", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_IMAGE_TYPE: TagInfoAscii = TagInfoAscii(
        0x0006, "CanonImageType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_FIRMWARE_VERSION: TagInfoAscii = TagInfoAscii(
        0x0007, "CanonFirmwareVersion", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The file number assigned to the image.
     */
    public val FILE_NUMBER: TagInfoLong = TagInfoLong(
        0x0008, "FileNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The name of the camera owner.
     */
    public val OWNER_NAME: TagInfoAscii = TagInfoAscii(
        0x0009, "OwnerName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val UNKNOWN_D30: TagInfoUndefineds = TagInfoUndefineds(
        0x000a, "UnknownD30", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The serial number of the camera body.
     */
    public val SERIAL_NUMBER: TagInfoLong = TagInfoLong(
        0x000c, "SerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Model specific camera info blob.
     *
     * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo
     */
    public val CANON_CAMERA_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x000d, "CanonCameraInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_FILE_LENGTH: TagInfoLong = TagInfoLong(
        0x000e, "CanonFileLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The custom functions settings of the camera.
     */
    public val CUSTOM_FUNCTIONS: TagInfoUndefineds = TagInfoUndefineds(
        0x000f, "CustomFunctions", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonModelId].
     */
    public val CANON_MODEL_ID: TagInfoLong = TagInfoLong(
        0x0010, "CanonModelID",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Details about the movie recorded along with the image.
     */
    public val MOVIE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0011, "MovieInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_AF_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0012, "CanonAFInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * All zeros for full frame.
     */
    public val THUMBNAIL_IMAGE_VALID_AREA: TagInfoShorts = TagInfoShorts(
        0x0013, "ThumbnailImageValidArea", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonSerialNumberFormat].
     */
    public val SERIAL_NUMBER_FORMAT: TagInfoLong = TagInfoLong(
        0x0015, "SerialNumberFormat",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0018: TagInfoUndefineds = TagInfoUndefineds(
        0x0018, "Canon_0x0018", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0019: TagInfoShort = TagInfoShort(
        0x0019, "Canon_0x0019",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonSuperMacro].
     */
    public val SUPER_MACRO: TagInfoShort = TagInfoShort(
        0x001a, "SuperMacro",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonDateStampMode].
     *
     * Used only in postcard mode.
     */
    public val DATE_STAMP_MODE: TagInfoShort = TagInfoShort(
        0x001c, "DateStampMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The My Colors setting of the camera.
     */
    public val MY_COLORS: TagInfoUndefineds = TagInfoUndefineds(
        0x001d, "MyColors", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The firmware revision of the camera.
     */
    public val FIRMWARE_REVISION: TagInfoLong = TagInfoLong(
        0x001e, "FirmwareRevision",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0022: TagInfoUndefineds = TagInfoUndefineds(
        0x0022, "Canon_0x0022", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * 2 values: the first is always 8, the second is the categories bit mask.
     */
    public val CATEGORIES: TagInfoLongs = TagInfoLongs(
        0x0023, "Categories", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val FACE_DETECT_1: TagInfoUndefineds = TagInfoUndefineds(
        0x0024, "FaceDetect1", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val FACE_DETECT_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0025, "FaceDetect2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_AF_INFO_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0026, "CanonAFInfo2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CONTRAST_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0027, "ContrastInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val IMAGE_UNIQUE_ID: TagInfoUndefineds = TagInfoUndefineds(
        0x0028, "ImageUniqueID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val WB_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0029, "WBInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X002D: TagInfoLong = TagInfoLong(
        0x002d, "Canon_0x002d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X002E: TagInfoUndefineds = TagInfoUndefineds(
        0x002e, "Canon_0x002e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val FACE_DETECT_3: TagInfoUndefineds = TagInfoUndefineds(
        0x002f, "FaceDetect3", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0031: TagInfoShorts = TagInfoShorts(
        0x0031, "Canon_0x0031", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0032: TagInfoLongs = TagInfoLongs(
        0x0032, "Canon_0x0032", 11,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0033: TagInfoLongs = TagInfoLongs(
        0x0033, "Canon_0x0033", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The time zone in effect when the image was captured.
     */
    public val TIME_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0035, "TimeInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X0037: TagInfoLongs = TagInfoLongs(
        0x0037, "Canon_0x0037", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val BATTERY_TYPE: TagInfoUndefineds = TagInfoUndefineds(
        0x0038, "BatteryType", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val AF_INFO_3: TagInfoUndefineds = TagInfoUndefineds(
        0x003c, "AFInfo3", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X003D: TagInfoLongs = TagInfoLongs(
        0x003d, "Canon_0x003d", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X003F: TagInfoLong = TagInfoLong(
        0x003f, "Canon_0x003f",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val RAW_DATA_OFFSET: TagInfoLong = TagInfoLong(
        0x0081, "RawDataOffset",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val RAW_DATA_LENGTH: TagInfoLong = TagInfoLong(
        0x0082, "RawDataLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The offset of the original decision data block.
     */
    public val ORIGINAL_DECISION_DATA_OFFSET: TagInfoLong = TagInfoLong(
        0x0083, "OriginalDecisionDataOffset",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CUSTOM_FUNCTIONS_1D: TagInfoUndefineds = TagInfoUndefineds(
        0x0090, "CustomFunctions1D", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val PERSONAL_FUNCTIONS: TagInfoUndefineds = TagInfoUndefineds(
        0x0091, "PersonalFunctions", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val PERSONAL_FUNCTION_VALUES: TagInfoUndefineds = TagInfoUndefineds(
        0x0092, "PersonalFunctionValues", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_FILE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0093, "CanonFileInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * EOS 1D AF points: 5 rows (A1-7, B1-10, C1-11, D1-10, E1-7), center point is C6.
     */
    public val AF_POINTS_IN_FOCUS_1D: TagInfoUndefineds = TagInfoUndefineds(
        0x0094, "AFPointsInFocus1D", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The model of the lens used.
     */
    public val LENS_MODEL: TagInfoAscii = TagInfoAscii(
        0x0095, "LensModel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The camera's internal serial number.
     */
    public val INTERNAL_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0096, "InternalSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Data of the camera's dust removal operation.
     */
    public val DUST_REMOVAL_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x0097, "DustRemovalData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CROP_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0098, "CropInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The second set of custom functions settings.
     */
    public val CUSTOM_FUNCTIONS_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0099, "CustomFunctions2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val ASPECT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x009a, "AspectInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Information about the image processing applied.
     */
    public val PROCESSING_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00a0, "ProcessingInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val TONE_CURVE_TABLE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a1, "ToneCurveTable", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val SHARPNESS_TABLE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a2, "SharpnessTable", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val SHARPNESS_FREQ_TABLE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a3, "SharpnessFreqTable", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The white balance table used for the shot.
     */
    public val WHITE_BALANCE_TABLE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a4, "WhiteBalanceTable", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val COLOR_BALANCE: TagInfoUndefineds = TagInfoUndefineds(
        0x00a9, "ColorBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * The measured color values of the scene.
     */
    public val MEASURED_COLOR: TagInfoUndefineds = TagInfoUndefineds(
        0x00aa, "MeasuredColor", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0x00ae, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_FLAGS: TagInfoUndefineds = TagInfoUndefineds(
        0x00b0, "CanonFlags", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val MODIFIED_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00b1, "ModifiedInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val TONE_CURVE_MATCHING: TagInfoUndefineds = TagInfoUndefineds(
        0x00b2, "ToneCurveMatching", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val WHITE_BALANCE_MATCHING: TagInfoUndefineds = TagInfoUndefineds(
        0x00b3, "WhiteBalanceMatching", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonColorSpace].
     */
    public val COLOR_SPACE: TagInfoShort = TagInfoShort(
        0x00b4, "ColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val PREVIEW_IMAGE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00b6, "PreviewImageInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Offset of the VRD 'recipe data' if it exists.
     */
    public val VRD_OFFSET: TagInfoLong = TagInfoLong(
        0x00d0, "VRDOffset",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Information about the camera sensor.
     */
    public val SENSOR_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x00e0, "SensorInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Color-related measurement data of the shot.
     */
    public val COLOR_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0x4001, "ColorData", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CRW_PARAM: TagInfoUndefineds = TagInfoUndefineds(
        0x4002, "CRWParam", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val COLOR_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4003, "ColorInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * Unknown 49 kB block, not copied to JPEG images.
     */
    public val FLAVOR: TagInfoUndefineds = TagInfoUndefineds(
        0x4005, "Flavor", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonPictureStyle].
     */
    public val PICTURE_STYLE_USER_DEF: TagInfoShorts = TagInfoShorts(
        0x4008, "PictureStyleUserDef", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    /**
     * See [CanonPictureStyle].
     */
    public val PICTURE_STYLE_PC: TagInfoShorts = TagInfoShorts(
        0x4009, "PictureStylePC", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CUSTOM_PICTURE_STYLE_FILE_NAME: TagInfoAscii = TagInfoAscii(
        0x4010, "CustomPictureStyleFileName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4011: TagInfoUndefineds = TagInfoUndefineds(
        0x4011, "Canon_0x4011", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4012: TagInfoAscii = TagInfoAscii(
        0x4012, "Canon_0x4012", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val AF_MICRO_ADJ: TagInfoUndefineds = TagInfoUndefineds(
        0x4013, "AFMicroAdj", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val VIGNETTING_CORR: TagInfoUndefineds = TagInfoUndefineds(
        0x4015, "VignettingCorr", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val VIGNETTING_CORR_2: TagInfoUndefineds = TagInfoUndefineds(
        0x4016, "VignettingCorr2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4017: TagInfoLongs = TagInfoLongs(
        0x4017, "Canon_0x4017", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val LIGHTING_OPT: TagInfoUndefineds = TagInfoUndefineds(
        0x4018, "LightingOpt", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val LENS_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4019, "LensInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val AMBIENCE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4020, "AmbienceInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val MULTI_EXP: TagInfoUndefineds = TagInfoUndefineds(
        0x4021, "MultiExp", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4023: TagInfoLongs = TagInfoLongs(
        0x4023, "Canon_0x4023", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val FILTER_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4024, "FilterInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val HDR_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4025, "HDRInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val LOG_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4026, "LogInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4027: TagInfoLongs = TagInfoLongs(
        0x4027, "Canon_0x4027", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val AF_CONFIG: TagInfoUndefineds = TagInfoUndefineds(
        0x4028, "AFConfig", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X402B: TagInfoLongs = TagInfoLongs(
        0x402b, "Canon_0x402b", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X402C: TagInfoLongs = TagInfoLongs(
        0x402c, "Canon_0x402c", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val RAW_BURST_MODE_ROLL: TagInfoUndefineds = TagInfoUndefineds(
        0x403f, "RawBurstModeRoll", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4035: TagInfoUndefineds = TagInfoUndefineds(
        0x4035, "Canon_0x4035", 556,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4037: TagInfoUndefineds = TagInfoUndefineds(
        0x4037, "Canon_0x4037", 24,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4039: TagInfoBytes = TagInfoBytes(
        0x4039, "Canon_0x4039", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X403C: TagInfoLongs = TagInfoLongs(
        0x403c, "Canon_0x403c", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4040: TagInfoLongs = TagInfoLongs(
        0x4040, "Canon_0x4040", 10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4049: TagInfoShorts = TagInfoShorts(
        0x4049, "Canon_0x4049", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X404B: TagInfoShorts = TagInfoShorts(
        0x404b, "Canon_0x404b", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val FOCUS_BRACKETING_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4053, "FocusBracketingInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4054: TagInfoLongs = TagInfoLongs(
        0x4054, "Canon_0x4054", 128,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val CANON_0X4055: TagInfoShorts = TagInfoShorts(
        0x4055, "Canon_0x4055", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val LEVEL_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4059, "LevelInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON
    )

    public val ALL: List<TagInfo> = listOf(
        CANON_CAMERA_SETTINGS, CANON_FOCAL_LENGTH, CANON_FLASH_INFO,
        CANON_SHOT_INFO, CANON_PANORAMA, CANON_IMAGE_TYPE,
        CANON_FIRMWARE_VERSION, FILE_NUMBER, OWNER_NAME, UNKNOWN_D30,
        SERIAL_NUMBER, CANON_CAMERA_INFO, CANON_FILE_LENGTH,
        CUSTOM_FUNCTIONS, CANON_MODEL_ID, MOVIE_INFO, CANON_AF_INFO,
        THUMBNAIL_IMAGE_VALID_AREA, SERIAL_NUMBER_FORMAT,
        CANON_0X0018, CANON_0X0019, SUPER_MACRO, DATE_STAMP_MODE,
        MY_COLORS, FIRMWARE_REVISION, CANON_0X0022, CATEGORIES,
        FACE_DETECT_1, FACE_DETECT_2, CANON_AF_INFO_2, CONTRAST_INFO,
        IMAGE_UNIQUE_ID, WB_INFO, CANON_0X002D, CANON_0X002E,
        FACE_DETECT_3, CANON_0X0031, CANON_0X0032, CANON_0X0033, TIME_INFO,
        CANON_0X0037, BATTERY_TYPE, AF_INFO_3, CANON_0X003D,
        CANON_0X003F,
        RAW_DATA_OFFSET, RAW_DATA_LENGTH, ORIGINAL_DECISION_DATA_OFFSET,
        CUSTOM_FUNCTIONS_1D, PERSONAL_FUNCTIONS, PERSONAL_FUNCTION_VALUES,
        CANON_FILE_INFO, AF_POINTS_IN_FOCUS_1D, LENS_MODEL,
        INTERNAL_SERIAL_NUMBER, DUST_REMOVAL_DATA, CROP_INFO,
        CUSTOM_FUNCTIONS_2, ASPECT_INFO, PROCESSING_INFO,
        TONE_CURVE_TABLE, SHARPNESS_TABLE, SHARPNESS_FREQ_TABLE,
        WHITE_BALANCE_TABLE, COLOR_BALANCE, MEASURED_COLOR,
        COLOR_TEMPERATURE, CANON_FLAGS, MODIFIED_INFO, TONE_CURVE_MATCHING,
        WHITE_BALANCE_MATCHING, COLOR_SPACE, PREVIEW_IMAGE_INFO,
        VRD_OFFSET, SENSOR_INFO,
        COLOR_DATA, CRW_PARAM, COLOR_INFO, FLAVOR,
        PICTURE_STYLE_USER_DEF, PICTURE_STYLE_PC,
        CUSTOM_PICTURE_STYLE_FILE_NAME,
        CANON_0X4011, CANON_0X4012, AF_MICRO_ADJ, VIGNETTING_CORR,
        VIGNETTING_CORR_2, CANON_0X4017, LIGHTING_OPT, LENS_INFO,
        AMBIENCE_INFO, MULTI_EXP, CANON_0X4023, FILTER_INFO, HDR_INFO,
        LOG_INFO, CANON_0X4027, AF_CONFIG, CANON_0X402B, CANON_0X402C,
        CANON_0X4035, CANON_0X4037, CANON_0X4039, CANON_0X403C,
        RAW_BURST_MODE_ROLL, CANON_0X4040, CANON_0X4049, CANON_0X404B,
        FOCUS_BRACKETING_INFO, CANON_0X4054, CANON_0X4055, LEVEL_INFO
    )
}
