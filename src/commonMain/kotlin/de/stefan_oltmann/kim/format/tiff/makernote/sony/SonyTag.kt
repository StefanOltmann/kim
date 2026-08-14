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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Sony MakerNote Tags, covering the Sony (header based), Sony5
 * (headerless) and SonyEricsson ("SEMC MS") MakerNote variants.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object SonyTag {

    /* Sony Ericsson */

    public val PREVIEW_IMAGE_START: TagInfoLong = TagInfoLong(
        0x0201, "PreviewImageStart",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_ERICSSON
    )

    public val PREVIEW_IMAGE_LENGTH: TagInfoLong = TagInfoLong(
        0x0202, "PreviewImageLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_ERICSSON
    )

    public val SONY_ERICSSON_0X0203: TagInfoShort = TagInfoShort(
        0x0203, "Sony_Ericsson_0x0203",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_ERICSSON
    )

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x2000, "MakerNoteVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_ERICSSON
    )

    /* Sony and Sony5 */

    public val CAMERA_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0010, "CameraInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    public val SONY5_0X0018: TagInfoUndefineds = TagInfoUndefineds(
        0x0018, "Sony_0x0018", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    public val MORE_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0020, "MoreInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    /**
     * See [SonyQuality].
     */
    public val QUALITY: TagInfoLong = TagInfoLong(
        0x0102, "Quality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The flash exposure compensation in EV.
     */
    public val FLASH_EXPOSURE_COMP: TagInfoSRationals = TagInfoSRationals(
        0x0104, "FlashExposureComp", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyTeleconverter].
     */
    public val TELECONVERTER: TagInfoLong = TagInfoLong(
        0x0105, "Teleconverter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    /**
     * The white balance fine-tune value.
     */
    public val WHITE_BALANCE_FINE_TUNE: TagInfoLong = TagInfoLong(
        0x0112, "WhiteBalanceFineTune",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val CAMERA_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x0114, "CameraSettings", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    /**
     * See [SonyWhiteBalance].
     */
    public val WHITE_BALANCE: TagInfoLong = TagInfoLong(
        0x0115, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val EXTRA_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0116, "ExtraInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    /**
     * Print Image Matching (PrintIM) information.
     */
    public val PRINT_IM: TagInfoUndefineds = TagInfoUndefineds(
        0x0e00, "PrintIM", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The multi-burst mode setting.
     */
    public val MULTI_BURST_MODE: TagInfoUndefineds = TagInfoUndefineds(
        0x1000, "MultiBurstMode", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The width of one image of a multi-burst.
     */
    public val MULTI_BURST_IMAGE_WIDTH: TagInfoShort = TagInfoShort(
        0x1001, "MultiBurstImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The height of one image of a multi-burst.
     */
    public val MULTI_BURST_IMAGE_HEIGHT: TagInfoShort = TagInfoShort(
        0x1002, "MultiBurstImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val PANORAMA: TagInfoLongs = TagInfoLongs(
        0x1003, "Panorama", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The embedded JPEG preview image.
     */
    public val PREVIEW_IMAGE: TagInfoUndefineds = TagInfoUndefineds(
        0x2001, "PreviewImage", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val RATING: TagInfoLong = TagInfoLong(
        0x2002, "Rating",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2003: TagInfoAscii = TagInfoAscii(
        0x2003, "Sony_0x2003", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val CONTRAST: TagInfoSLong = TagInfoSLong(
        0x2004, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SATURATION: TagInfoSLong = TagInfoSLong(
        0x2005, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SHARPNESS: TagInfoSLong = TagInfoSLong(
        0x2006, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val BRIGHTNESS: TagInfoSLong = TagInfoSLong(
        0x2007, "Brightness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyLongExposureNoiseReduction].
     */
    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoLong = TagInfoLong(
        0x2008, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyHighIsoNoiseReduction].
     */
    public val HIGH_ISO_NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x2009, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * Stored as a 32-bit integer, but read as two 16-bit integers.
     */
    public val HDR: TagInfoLong = TagInfoLong(
        0x200a, "HDR",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyMultiFrameNoiseReduction].
     */
    public val MULTI_FRAME_NOISE_REDUCTION: TagInfoLong = TagInfoLong(
        0x200b, "MultiFrameNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X200D: TagInfoRationals = TagInfoRationals(
        0x200d, "Sony_0x200d", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyPictureEffect].
     */
    public val PICTURE_EFFECT: TagInfoShort = TagInfoShort(
        0x200e, "PictureEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonySoftSkinEffect].
     */
    public val SOFT_SKIN_EFFECT: TagInfoLong = TagInfoLong(
        0x200f, "SoftSkinEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_2010: TagInfoUndefineds = TagInfoUndefineds(
        0x2010, "Tag2010a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyVignettingCorrection].
     */
    public val VIGNETTING_CORRECTION: TagInfoLong = TagInfoLong(
        0x2011, "VignettingCorrection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyLateralChromaticAberration].
     */
    public val LATERAL_CHROMATIC_ABERRATION: TagInfoLong = TagInfoLong(
        0x2012, "LateralChromaticAberration",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyDistortionCorrectionSetting].
     */
    public val DISTORTION_CORRECTION_SETTING: TagInfoLong = TagInfoLong(
        0x2013, "DistortionCorrectionSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * 2 numbers: the first is a shift toward amber (positive), the second a shift toward magenta (positive).
     */
    public val WB_SHIFT_AB_GM: TagInfoSLongs = TagInfoSLongs(
        0x2014, "WBShiftAB_GM", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2015: TagInfoShort = TagInfoShort(
        0x2015, "Sony_0x2015",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyAutoPortraitFramed].
     */
    public val AUTO_PORTRAIT_FRAMED: TagInfoShort = TagInfoShort(
        0x2016, "AutoPortraitFramed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyFlashAction].
     */
    public val FLASH_ACTION: TagInfoLong = TagInfoLong(
        0x2017, "FlashAction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2018: TagInfoLong = TagInfoLong(
        0x2018, "Sony_0x2018",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2019: TagInfoLong = TagInfoLong(
        0x2019, "Sony_0x2019",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyElectronicFrontCurtainShutter].
     */
    public val ELECTRONIC_FRONT_CURTAIN_SHUTTER: TagInfoLong = TagInfoLong(
        0x201a, "ElectronicFrontCurtainShutter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyFocusMode].
     */
    public val FOCUS_MODE: TagInfoByte = TagInfoByte(
        0x201b, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The autofocus area mode setting.
     */
    public val AF_AREA_MODE_SETTING: TagInfoByte = TagInfoByte(
        0x201c, "AFAreaModeSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * X and Y coordinates of the AF point, valid only when AFAreaMode is Flexible Spot.
     */
    public val FLEXIBLE_SPOT_POSITION: TagInfoShorts = TagInfoShorts(
        0x201d, "FlexibleSpotPosition", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The autofocus point selected.
     */
    public val AF_POINT_SELECTED: TagInfoByte = TagInfoByte(
        0x201e, "AFPointSelected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X201F: TagInfoBytes = TagInfoBytes(
        0x201f, "Sony_0x201f", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The autofocus points used.
     */
    public val AF_POINTS_USED: TagInfoBytes = TagInfoBytes(
        0x2020, "AFPointsUsed", 10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyAfTracking].
     */
    public val AF_TRACKING: TagInfoByte = TagInfoByte(
        0x2021, "AFTracking",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The autofocus points used on the focal plane.
     */
    public val FOCAL_PLANE_AF_POINTS_USED: TagInfoUndefineds = TagInfoUndefineds(
        0x2022, "FocalPlaneAFPointsUsed", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyMultiFrameNrEffect].
     */
    public val MULTI_FRAME_NR_EFFECT: TagInfoLong = TagInfoLong(
        0x2023, "MultiFrameNREffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2025: TagInfoBytes = TagInfoBytes(
        0x2025, "Sony_0x2025", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * 2 numbers: the first is a shift toward amber (positive), the second a shift toward magenta (positive).
     */
    public val WB_SHIFT_AB_GM_PRECISE: TagInfoSLongs = TagInfoSLongs(
        0x2026, "WBShiftAB_GM_Precise", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The position in the image where the camera focused.
     */
    public val FOCUS_LOCATION: TagInfoShorts = TagInfoShorts(
        0x2027, "FocusLocation", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The variable low-pass filter setting.
     */
    public val VARIABLE_LOW_PASS_FILTER: TagInfoShorts = TagInfoShorts(
        0x2028, "VariableLowPassFilter", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyRawFileType].
     */
    public val RAW_FILE_TYPE: TagInfoShort = TagInfoShort(
        0x2029, "RAWFileType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_202A: TagInfoUndefineds = TagInfoUndefineds(
        0x202a, "Tag202a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyPrioritySetInAwb].
     */
    public val PRIORITY_SET_IN_AWB: TagInfoByte = TagInfoByte(
        0x202b, "PrioritySetInAWB",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyMeteringMode2].
     */
    public val METERING_MODE_2: TagInfoShort = TagInfoShort(
        0x202c, "MeteringMode2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The exposure standard adjustment applied.
     */
    public val EXPOSURE_STANDARD_ADJUSTMENT: TagInfoSRationals = TagInfoSRationals(
        0x202d, "ExposureStandardAdjustment", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The image quality setting.
     */
    public val QUALITY_2: TagInfoShorts = TagInfoShorts(
        0x202e, "Quality", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The group ID and shot number of a pixel-shift burst.
     */
    public val PIXEL_SHIFT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x202f, "PixelShiftInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The serial number of the camera.
     */
    public val SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x2031, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SHADOWS: TagInfoSLong = TagInfoSLong(
        0x2032, "Shadows",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val HIGHLIGHTS: TagInfoSLong = TagInfoSLong(
        0x2033, "Highlights",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val FADE: TagInfoSLong = TagInfoSLong(
        0x2034, "Fade",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The adjustable range of the sharpness setting.
     */
    public val SHARPNESS_RANGE: TagInfoSLong = TagInfoSLong(
        0x2035, "SharpnessRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val CLARITY: TagInfoSLong = TagInfoSLong(
        0x2036, "Clarity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * Width and height of FocusFrame, centered on FocusLocation.
     */
    public val FOCUS_FRAME_SIZE: TagInfoUndefineds = TagInfoUndefineds(
        0x2037, "FocusFrameSize", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2038: TagInfoByte = TagInfoByte(
        0x2038, "Sony_0x2038",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyJpegHeifSwitch].
     */
    public val JPEG_HEIF_SWITCH: TagInfoShort = TagInfoShort(
        0x2039, "JPEG-HEIFSwitch",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X203A: TagInfoBytes = TagInfoBytes(
        0x203a, "Sony_0x203a", 32,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X203C: TagInfoUndefineds = TagInfoUndefineds(
        0x203c, "Sony_0x203c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X203D: TagInfoLong = TagInfoLong(
        0x203d, "Sony_0x203d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X203E: TagInfoByte = TagInfoByte(
        0x203e, "Sony_0x203e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X203F: TagInfoShort = TagInfoShort(
        0x203f, "Sony_0x203f",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X2041: TagInfoByte = TagInfoByte(
        0x2041, "Sony_0x2041",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val HIDDEN_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x2044, "HiddenInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val FOCUS_LOCATION_2: TagInfoShorts = TagInfoShorts(
        0x204a, "FocusLocation2", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyStepCropShooting].
     */
    public val STEP_CROP_SHOOTING: TagInfoByte = TagInfoByte(
        0x205c, "StepCropShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SHOT_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x3000, "ShotInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X5001: TagInfoRationals = TagInfoRationals(
        0x5001, "Sony_0x5001", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X5002: TagInfoByte = TagInfoByte(
        0x5002, "Sony_0x5002",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_900B: TagInfoUndefineds = TagInfoUndefineds(
        0x900b, "Tag900b", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY5
    )

    public val TAG_9050: TagInfoUndefineds = TagInfoUndefineds(
        0x9050, "Tag9050c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9400: TagInfoUndefineds = TagInfoUndefineds(
        0x9400, "Tag9400c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9401: TagInfoUndefineds = TagInfoUndefineds(
        0x9401, "Tag9401", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9402: TagInfoUndefineds = TagInfoUndefineds(
        0x9402, "Tag9402", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9403: TagInfoUndefineds = TagInfoUndefineds(
        0x9403, "Tag9403", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9404: TagInfoUndefineds = TagInfoUndefineds(
        0x9404, "Sony_0x9404", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9405: TagInfoUndefineds = TagInfoUndefineds(
        0x9405, "Tag9405a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_9406: TagInfoUndefineds = TagInfoUndefineds(
        0x9406, "Tag9406", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9408: TagInfoUndefineds = TagInfoUndefineds(
        0x9408, "Sony_0x9408", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X940A: TagInfoUndefineds = TagInfoUndefineds(
        0x940a, "Sony_0x940a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X940B: TagInfoUndefineds = TagInfoUndefineds(
        0x940b, "Sony_0x940b", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_940C: TagInfoUndefineds = TagInfoUndefineds(
        0x940c, "Tag940c", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X940D: TagInfoUndefineds = TagInfoUndefineds(
        0x940d, "Sony_0x940d", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val TAG_940E: TagInfoUndefineds = TagInfoUndefineds(
        0x940e, "Tag940e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X940F: TagInfoUndefineds = TagInfoUndefineds(
        0x940f, "Sony_0x940f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9412: TagInfoUndefineds = TagInfoUndefineds(
        0x9412, "Sony_0x9412", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9413: TagInfoUndefineds = TagInfoUndefineds(
        0x9413, "Sony_0x9413", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9414: TagInfoUndefineds = TagInfoUndefineds(
        0x9414, "Sony_0x9414", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0X9416: TagInfoUndefineds = TagInfoUndefineds(
        0x9416, "Sony_0x9416", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The file format of the image.
     */
    public val FILE_FORMAT: TagInfoBytes = TagInfoBytes(
        0xb000, "FileFormat", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyModelId].
     */
    public val SONY_MODEL_ID: TagInfoShort = TagInfoShort(
        0xb001, "SonyModelID",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyCreativeStyle].
     */
    public val CREATIVE_STYLE: TagInfoAscii = TagInfoAscii(
        0xb020, "CreativeStyle", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The color temperature of the shot.
     */
    public val COLOR_TEMPERATURE: TagInfoLong = TagInfoLong(
        0xb021, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The color compensation filter value (green/magenta).
     */
    public val COLOR_COMPENSATION_FILTER: TagInfoLong = TagInfoLong(
        0xb022, "ColorCompensationFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonySceneMode].
     */
    public val SCENE_MODE: TagInfoLong = TagInfoLong(
        0xb023, "SceneMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyZoneMatching].
     */
    public val ZONE_MATCHING: TagInfoLong = TagInfoLong(
        0xb024, "ZoneMatching",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyDynamicRangeOptimizer].
     */
    public val DYNAMIC_RANGE_OPTIMIZER: TagInfoLong = TagInfoLong(
        0xb025, "DynamicRangeOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyImageStabilization].
     */
    public val IMAGE_STABILIZATION: TagInfoLong = TagInfoLong(
        0xb026, "ImageStabilization",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyLensType].
     */
    public val LENS_TYPE: TagInfoLong = TagInfoLong(
        0xb027, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val MINOLTA_MAKER_NOTE: TagInfoUndefineds = TagInfoUndefineds(
        0xb028, "MinoltaMakerNote", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyColorMode].
     */
    public val COLOR_MODE: TagInfoLong = TagInfoLong(
        0xb029, "ColorMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * Like LensInfo, but also specifies lens features: DT, E, ZA, G, SSM, SAM,
     * OSS, STF, Reflex, Macro and Fisheye.
     */
    public val LENS_SPEC: TagInfoBytes = TagInfoBytes(
        0xb02a, "LensSpec", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The width and height of the full image.
     */
    public val FULL_IMAGE_SIZE: TagInfoLongs = TagInfoLongs(
        0xb02b, "FullImageSize", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The width and height of the preview image.
     */
    public val PREVIEW_IMAGE_SIZE: TagInfoLongs = TagInfoLongs(
        0xb02c, "PreviewImageSize", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyMacro].
     */
    public val MACRO: TagInfoShort = TagInfoShort(
        0xb040, "Macro",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyExposureMode].
     */
    public val EXPOSURE_MODE: TagInfoShort = TagInfoShort(
        0xb041, "ExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyFocusMode2].
     */
    public val FOCUS_MODE_2: TagInfoShort = TagInfoShort(
        0xb042, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The autofocus area mode.
     */
    public val AF_AREA_MODE: TagInfoShort = TagInfoShort(
        0xb043, "AFAreaMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyAfIlluminator].
     */
    public val AF_ILLUMINATOR: TagInfoShort = TagInfoShort(
        0xb044, "AFIlluminator",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB045: TagInfoShort = TagInfoShort(
        0xb045, "Sony_0xb045",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB046: TagInfoShort = TagInfoShort(
        0xb046, "Sony_0xb046",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyJpegQuality].
     */
    public val JPEG_QUALITY: TagInfoShort = TagInfoShort(
        0xb047, "JPEGQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * The flash level setting.
     */
    public val FLASH_LEVEL: TagInfoSShort = TagInfoSShort(
        0xb048, "FlashLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyReleaseMode].
     */
    public val RELEASE_MODE: TagInfoShort = TagInfoShort(
        0xb049, "ReleaseMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonySequenceNumber].
     *
     * shot number in continuous burst.
     */
    public val SEQUENCE_NUMBER: TagInfoShort = TagInfoShort(
        0xb04a, "SequenceNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyAntiBlur].
     */
    public val ANTI_BLUR: TagInfoShort = TagInfoShort(
        0xb04b, "Anti-Blur",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB04C: TagInfoRationals = TagInfoRationals(
        0xb04c, "Sony_0xb04c", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB04D: TagInfoShort = TagInfoShort(
        0xb04d, "Sony_0xb04d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyFocusMode3].
     */
    public val FOCUS_MODE_3: TagInfoShort = TagInfoShort(
        0xb04e, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyDynamicRangeOptimizer2].
     */
    public val DYNAMIC_RANGE_OPTIMIZER_2: TagInfoShort = TagInfoShort(
        0xb04f, "DynamicRangeOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyHighIsoNoiseReduction2].
     */
    public val HIGH_ISO_NOISE_REDUCTION_2: TagInfoShort = TagInfoShort(
        0xb050, "HighISONoiseReduction2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB051: TagInfoShort = TagInfoShort(
        0xb051, "Sony_0xb051",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyIntelligentAuto].
     */
    public val INTELLIGENT_AUTO: TagInfoShort = TagInfoShort(
        0xb052, "IntelligentAuto",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val SONY_0XB053: TagInfoShort = TagInfoShort(
        0xb053, "Sony_0xb053",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    /**
     * See [SonyWhiteBalance2].
     */
    public val WHITE_BALANCE_2: TagInfoShort = TagInfoShort(
        0xb054, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY
    )

    public val ALL: List<TagInfo> = listOf(
        CAMERA_INFO, SONY5_0X0018, MORE_INFO,
        QUALITY, FLASH_EXPOSURE_COMP, TELECONVERTER,
        WHITE_BALANCE_FINE_TUNE, CAMERA_SETTINGS, WHITE_BALANCE,
        EXTRA_INFO,
        PREVIEW_IMAGE_START, PREVIEW_IMAGE_LENGTH, SONY_ERICSSON_0X0203,
        PRINT_IM,
        MULTI_BURST_MODE, MULTI_BURST_IMAGE_WIDTH, MULTI_BURST_IMAGE_HEIGHT,
        PANORAMA,
        MAKER_NOTE_VERSION,
        PREVIEW_IMAGE, RATING, SONY_0X2003,
        CONTRAST, SATURATION, SHARPNESS, BRIGHTNESS,
        LONG_EXPOSURE_NOISE_REDUCTION, HIGH_ISO_NOISE_REDUCTION, HDR,
        MULTI_FRAME_NOISE_REDUCTION, SONY_0X200D, PICTURE_EFFECT,
        SOFT_SKIN_EFFECT, TAG_2010, VIGNETTING_CORRECTION,
        LATERAL_CHROMATIC_ABERRATION, DISTORTION_CORRECTION_SETTING,
        WB_SHIFT_AB_GM, SONY_0X2015, AUTO_PORTRAIT_FRAMED, FLASH_ACTION,
        SONY_0X2018, SONY_0X2019, ELECTRONIC_FRONT_CURTAIN_SHUTTER,
        FOCUS_MODE, AF_AREA_MODE_SETTING, FLEXIBLE_SPOT_POSITION,
        AF_POINT_SELECTED, SONY_0X201F, AF_POINTS_USED, AF_TRACKING,
        FOCAL_PLANE_AF_POINTS_USED, MULTI_FRAME_NR_EFFECT, SONY_0X2025,
        WB_SHIFT_AB_GM_PRECISE, FOCUS_LOCATION, VARIABLE_LOW_PASS_FILTER,
        RAW_FILE_TYPE, TAG_202A, PRIORITY_SET_IN_AWB, METERING_MODE_2,
        EXPOSURE_STANDARD_ADJUSTMENT, QUALITY_2, PIXEL_SHIFT_INFO,
        SERIAL_NUMBER,
        SHADOWS, HIGHLIGHTS, FADE, SHARPNESS_RANGE, CLARITY,
        FOCUS_FRAME_SIZE, SONY_0X2038, JPEG_HEIF_SWITCH, SONY_0X203A,
        SONY_0X203C, SONY_0X203D, SONY_0X203E, SONY_0X203F, SONY_0X2041,
        HIDDEN_INFO, FOCUS_LOCATION_2, STEP_CROP_SHOOTING, SHOT_INFO,
        SONY_0X5001, SONY_0X5002,
        TAG_900B, TAG_9050,
        TAG_9400, TAG_9401, TAG_9402, TAG_9403, SONY_0X9404, TAG_9405,
        TAG_9406, SONY_0X9408, SONY_0X940A, SONY_0X940B, TAG_940C,
        SONY_0X940D, TAG_940E, SONY_0X940F, SONY_0X9412, SONY_0X9413,
        SONY_0X9414, SONY_0X9416,
        FILE_FORMAT, SONY_MODEL_ID, CREATIVE_STYLE, COLOR_TEMPERATURE,
        COLOR_COMPENSATION_FILTER, SCENE_MODE, ZONE_MATCHING,
        DYNAMIC_RANGE_OPTIMIZER, IMAGE_STABILIZATION, LENS_TYPE,
        MINOLTA_MAKER_NOTE, COLOR_MODE, LENS_SPEC, FULL_IMAGE_SIZE,
        PREVIEW_IMAGE_SIZE,
        MACRO, EXPOSURE_MODE, FOCUS_MODE_2, AF_AREA_MODE, AF_ILLUMINATOR,
        SONY_0XB045, SONY_0XB046, JPEG_QUALITY, FLASH_LEVEL,
        RELEASE_MODE, SEQUENCE_NUMBER, ANTI_BLUR, SONY_0XB04C,
        SONY_0XB04D, FOCUS_MODE_3, DYNAMIC_RANGE_OPTIMIZER_2,
        HIGH_ISO_NOISE_REDUCTION_2, SONY_0XB051, INTELLIGENT_AUTO,
        SONY_0XB053, WHITE_BALANCE_2
    )
}
