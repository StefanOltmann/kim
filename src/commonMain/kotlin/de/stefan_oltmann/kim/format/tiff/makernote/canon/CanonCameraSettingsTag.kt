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
package de.stefan_oltmann.kim.format.tiff.makernote.canon

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort

/**
 * Tags of the CameraSettings maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraSettings
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraSettingsTag {

    public val MACRO_MODE: TagInfoSShort = TagInfoSShort(
        0x1, "MacroMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val SELF_TIMER: TagInfoSShort = TagInfoSShort(
        0x2, "SelfTimer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val QUALITY: TagInfoSShort = TagInfoSShort(
        0x3, "Quality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CANON_FLASH_MODE: TagInfoSShort = TagInfoSShort(
        0x4, "CanonFlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CONTINUOUS_DRIVE: TagInfoSShort = TagInfoSShort(
        0x5, "ContinuousDrive",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FOCUS_MODE: TagInfoSShort = TagInfoSShort(
        0x7, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val RECORD_MODE: TagInfoSShort = TagInfoSShort(
        0x9, "RecordMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CANON_IMAGE_SIZE: TagInfoSShort = TagInfoSShort(
        0xa, "CanonImageSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val EASY_MODE: TagInfoSShort = TagInfoSShort(
        0xb, "EasyMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val DIGITAL_ZOOM: TagInfoSShort = TagInfoSShort(
        0xc, "DigitalZoom",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CONTRAST: TagInfoSShort = TagInfoSShort(
        0xd, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val SATURATION: TagInfoSShort = TagInfoSShort(
        0xe, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val SHARPNESS: TagInfoSShort = TagInfoSShort(
        0xf, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CAMERA_ISO: TagInfoSShort = TagInfoSShort(
        0x10, "CameraISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val METERING_MODE: TagInfoSShort = TagInfoSShort(
        0x11, "MeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FOCUS_RANGE: TagInfoSShort = TagInfoSShort(
        0x12, "FocusRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val AF_POINT: TagInfoSShort = TagInfoSShort(
        0x13, "AFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CANON_EXPOSURE_MODE: TagInfoSShort = TagInfoSShort(
        0x14, "CanonExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val LENS_TYPE: TagInfoShort = TagInfoShort(
        0x16, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val MAX_FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0x17, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val MIN_FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0x18, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FOCAL_UNITS: TagInfoSShort = TagInfoSShort(
        0x19, "FocalUnits",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val MAX_APERTURE: TagInfoSShort = TagInfoSShort(
        0x1a, "MaxAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val MIN_APERTURE: TagInfoSShort = TagInfoSShort(
        0x1b, "MinAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FLASH_MODEL: TagInfoSShort = TagInfoSShort(
        0x1c, "FlashModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FLASH_BITS: TagInfoSShort = TagInfoSShort(
        0x1d, "FlashBits",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FOCUS_CONTINUOUS: TagInfoSShort = TagInfoSShort(
        0x20, "FocusContinuous",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val AE_SETTING: TagInfoSShort = TagInfoSShort(
        0x21, "AESetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val IMAGE_STABILIZATION: TagInfoSShort = TagInfoSShort(
        0x22, "ImageStabilization",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val DISPLAY_APERTURE: TagInfoSShort = TagInfoSShort(
        0x23, "DisplayAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val ZOOM_SOURCE_WIDTH: TagInfoSShort = TagInfoSShort(
        0x24, "ZoomSourceWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val ZOOM_TARGET_WIDTH: TagInfoSShort = TagInfoSShort(
        0x25, "ZoomTargetWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val SPOT_METERING_MODE: TagInfoSShort = TagInfoSShort(
        0x27, "SpotMeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val PHOTO_EFFECT: TagInfoSShort = TagInfoSShort(
        0x28, "PhotoEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val MANUAL_FLASH_OUTPUT: TagInfoSShort = TagInfoSShort(
        0x29, "ManualFlashOutput",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val COLOR_TONE: TagInfoSShort = TagInfoSShort(
        0x2a, "ColorTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val SRAW_QUALITY: TagInfoSShort = TagInfoSShort(
        0x2e, "SRAWQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val FOCUS_BRACKETING: TagInfoSShort = TagInfoSShort(
        0x32, "FocusBracketing",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val CLARITY: TagInfoSShort = TagInfoSShort(
        0x33, "Clarity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val HDR_PQ: TagInfoSShort = TagInfoSShort(
        0x34, "HDR-PQ",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS
    )

    public val ALL: List<TagInfo> = listOf(
        MACRO_MODE,
        SELF_TIMER,
        QUALITY,
        CANON_FLASH_MODE,
        CONTINUOUS_DRIVE,
        FOCUS_MODE,
        RECORD_MODE,
        CANON_IMAGE_SIZE,
        EASY_MODE,
        DIGITAL_ZOOM,
        CONTRAST,
        SATURATION,
        SHARPNESS,
        CAMERA_ISO,
        METERING_MODE,
        FOCUS_RANGE,
        AF_POINT,
        CANON_EXPOSURE_MODE,
        LENS_TYPE,
        MAX_FOCAL_LENGTH,
        MIN_FOCAL_LENGTH,
        FOCAL_UNITS,
        MAX_APERTURE,
        MIN_APERTURE,
        FLASH_MODEL,
        FLASH_BITS,
        FOCUS_CONTINUOUS,
        AE_SETTING,
        IMAGE_STABILIZATION,
        DISPLAY_APERTURE,
        ZOOM_SOURCE_WIDTH,
        ZOOM_TARGET_WIDTH,
        SPOT_METERING_MODE,
        PHOTO_EFFECT,
        MANUAL_FLASH_OUTPUT,
        COLOR_TONE,
        SRAW_QUALITY,
        FOCUS_BRACKETING,
        CLARITY,
        HDR_PQ
    )
}
