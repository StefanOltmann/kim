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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the Functions1D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Functions1D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCustomFunctions1DTag {

    public val FOCUSING_SCREEN: TagInfoByte = TagInfoByte(
        0x0, "FocusingScreen",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FINDER_DISPLAY_DURING_EXPOSURE: TagInfoByte = TagInfoByte(
        0x1, "FinderDisplayDuringExposure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_RELEASE_NO_CF_CARD: TagInfoByte = TagInfoByte(
        0x2, "ShutterReleaseNoCFCard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ISO_SPEED_EXPANSION: TagInfoByte = TagInfoByte(
        0x3, "ISOSpeedExpansion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_AEL_BUTTON: TagInfoByte = TagInfoByte(
        0x4, "ShutterAELButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MANUAL_TV: TagInfoByte = TagInfoByte(
        0x5, "ManualTv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_LEVEL_INCREMENTS: TagInfoByte = TagInfoByte(
        0x6, "ExposureLevelIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val USM_LENS_ELECTRONIC_MF: TagInfoByte = TagInfoByte(
        0x7, "USMLensElectronicMF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LCD_PANELS: TagInfoByte = TagInfoByte(
        0x8, "LCDPanels",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_SEQUENCE_AUTO_CANCEL: TagInfoByte = TagInfoByte(
        0x9, "AEBSequenceAutoCancel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_ILLUMINATION: TagInfoByte = TagInfoByte(
        0xa, "AFPointIllumination",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_SELECTION: TagInfoByte = TagInfoByte(
        0xb, "AFPointSelection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MIRROR_LOCKUP: TagInfoByte = TagInfoByte(
        0xc, "MirrorLockup",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_SPOT_METERING: TagInfoByte = TagInfoByte(
        0xd, "AFPointSpotMetering",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FILL_FLASH_AUTO_REDUCTION: TagInfoByte = TagInfoByte(
        0xe, "FillFlashAutoReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_CURTAIN_SYNC: TagInfoByte = TagInfoByte(
        0xf, "ShutterCurtainSync",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SAFETY_SHIFT_IN_AV_OR_TV: TagInfoByte = TagInfoByte(
        0x10, "SafetyShiftInAvOrTv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_ACTIVATION_AREA: TagInfoByte = TagInfoByte(
        0x11, "AFPointActivationArea",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SWITCH_TO_REGISTERED_AF_POINT: TagInfoByte = TagInfoByte(
        0x12, "SwitchToRegisteredAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LENS_AF_STOP_BUTTON: TagInfoByte = TagInfoByte(
        0x13, "LensAFStopButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_TRACKING_SENSITIVITY: TagInfoByte = TagInfoByte(
        0x14, "AIServoTrackingSensitivity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_CONTINUOUS_SHOOTING: TagInfoByte = TagInfoByte(
        0x15, "AIServoContinuousShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ALL: List<TagInfo> = listOf(
        FOCUSING_SCREEN,
        FINDER_DISPLAY_DURING_EXPOSURE,
        SHUTTER_RELEASE_NO_CF_CARD,
        ISO_SPEED_EXPANSION,
        SHUTTER_AEL_BUTTON,
        MANUAL_TV,
        EXPOSURE_LEVEL_INCREMENTS,
        USM_LENS_ELECTRONIC_MF,
        LCD_PANELS,
        AEB_SEQUENCE_AUTO_CANCEL,
        AF_POINT_ILLUMINATION,
        AF_POINT_SELECTION,
        MIRROR_LOCKUP,
        AF_POINT_SPOT_METERING,
        FILL_FLASH_AUTO_REDUCTION,
        SHUTTER_CURTAIN_SYNC,
        SAFETY_SHIFT_IN_AV_OR_TV,
        AF_POINT_ACTIVATION_AREA,
        SWITCH_TO_REGISTERED_AF_POINT,
        LENS_AF_STOP_BUTTON,
        AI_SERVO_TRACKING_SENSITIVITY,
        AI_SERVO_CONTINUOUS_SHOOTING
    )
}
