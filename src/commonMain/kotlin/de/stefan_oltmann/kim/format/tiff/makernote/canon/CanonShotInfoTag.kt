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
 * Tags of the ShotInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#ShotInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonShotInfoTag {

    public val AUTO_ISO: TagInfoSShort = TagInfoSShort(
        0x1, "AutoISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val BASE_ISO: TagInfoSShort = TagInfoSShort(
        0x2, "BaseISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val MEASURED_EV: TagInfoSShort = TagInfoSShort(
        0x3, "MeasuredEV",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val TARGET_APERTURE: TagInfoSShort = TagInfoSShort(
        0x4, "TargetAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val TARGET_EXPOSURE_TIME: TagInfoSShort = TagInfoSShort(
        0x5, "TargetExposureTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val EXPOSURE_COMPENSATION: TagInfoSShort = TagInfoSShort(
        0x6, "ExposureCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val WHITE_BALANCE: TagInfoSShort = TagInfoSShort(
        0x7, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val SLOW_SHUTTER: TagInfoSShort = TagInfoSShort(
        0x8, "SlowShutter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val SEQUENCE_NUMBER: TagInfoSShort = TagInfoSShort(
        0x9, "SequenceNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val OPTICAL_ZOOM_CODE: TagInfoSShort = TagInfoSShort(
        0xa, "OpticalZoomCode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val CAMERA_TEMPERATURE: TagInfoSShort = TagInfoSShort(
        0xc, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val FLASH_GUIDE_NUMBER: TagInfoSShort = TagInfoSShort(
        0xd, "FlashGuideNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val AF_POINTS_IN_FOCUS: TagInfoSShort = TagInfoSShort(
        0xe, "AFPointsInFocus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val FLASH_EXPOSURE_COMP: TagInfoSShort = TagInfoSShort(
        0xf, "FlashExposureComp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val AUTO_EXPOSURE_BRACKETING: TagInfoSShort = TagInfoSShort(
        0x10, "AutoExposureBracketing",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val AEB_BRACKET_VALUE: TagInfoSShort = TagInfoSShort(
        0x11, "AEBBracketValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val CONTROL_MODE: TagInfoSShort = TagInfoSShort(
        0x12, "ControlMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val FOCUS_DISTANCE_UPPER: TagInfoShort = TagInfoShort(
        0x13, "FocusDistanceUpper",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val FOCUS_DISTANCE_LOWER: TagInfoShort = TagInfoShort(
        0x14, "FocusDistanceLower",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val F_NUMBER: TagInfoSShort = TagInfoSShort(
        0x15, "FNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val EXPOSURE_TIME: TagInfoSShort = TagInfoSShort(
        0x16, "ExposureTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val MEASURED_EV2: TagInfoSShort = TagInfoSShort(
        0x17, "MeasuredEV2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val BULB_DURATION: TagInfoSShort = TagInfoSShort(
        0x18, "BulbDuration",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val CAMERA_TYPE: TagInfoSShort = TagInfoSShort(
        0x1a, "CameraType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val AUTO_ROTATE: TagInfoSShort = TagInfoSShort(
        0x1b, "AutoRotate",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val ND_FILTER: TagInfoSShort = TagInfoSShort(
        0x1c, "NDFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val SELF_TIMER2: TagInfoSShort = TagInfoSShort(
        0x1d, "SelfTimer2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val FLASH_OUTPUT: TagInfoSShort = TagInfoSShort(
        0x21, "FlashOutput",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        AUTO_ISO,
        BASE_ISO,
        MEASURED_EV,
        TARGET_APERTURE,
        TARGET_EXPOSURE_TIME,
        EXPOSURE_COMPENSATION,
        WHITE_BALANCE,
        SLOW_SHUTTER,
        SEQUENCE_NUMBER,
        OPTICAL_ZOOM_CODE,
        CAMERA_TEMPERATURE,
        FLASH_GUIDE_NUMBER,
        AF_POINTS_IN_FOCUS,
        FLASH_EXPOSURE_COMP,
        AUTO_EXPOSURE_BRACKETING,
        AEB_BRACKET_VALUE,
        CONTROL_MODE,
        FOCUS_DISTANCE_UPPER,
        FOCUS_DISTANCE_LOWER,
        F_NUMBER,
        EXPOSURE_TIME,
        MEASURED_EV2,
        BULB_DURATION,
        CAMERA_TYPE,
        AUTO_ROTATE,
        ND_FILTER,
        SELF_TIMER2,
        FLASH_OUTPUT
    )
}
