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
 * Tags of the Functions30D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Functions30D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCustomFunctions30DTag {

    public val SET_FUNCTION_WHEN_SHOOTING: TagInfoByte = TagInfoByte(
        0x1, "SetFunctionWhenShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x2, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_SYNC_SPEED_AV: TagInfoByte = TagInfoByte(
        0x3, "FlashSyncSpeedAv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_AE_LOCK: TagInfoByte = TagInfoByte(
        0x4, "Shutter-AELock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_ASSIST_BEAM: TagInfoByte = TagInfoByte(
        0x5, "AFAssistBeam",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_LEVEL_INCREMENTS: TagInfoByte = TagInfoByte(
        0x6, "ExposureLevelIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_FIRING: TagInfoByte = TagInfoByte(
        0x7, "FlashFiring",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ISO_EXPANSION: TagInfoByte = TagInfoByte(
        0x8, "ISOExpansion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_SEQUENCE_AUTO_CANCEL: TagInfoByte = TagInfoByte(
        0x9, "AEBSequenceAutoCancel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SUPERIMPOSED_DISPLAY: TagInfoByte = TagInfoByte(
        0xa, "SuperimposedDisplay",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MENU_BUTTON_DISPLAY_POSITION: TagInfoByte = TagInfoByte(
        0xb, "MenuButtonDisplayPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MIRROR_LOCKUP: TagInfoByte = TagInfoByte(
        0xc, "MirrorLockup",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_SELECTION_METHOD: TagInfoByte = TagInfoByte(
        0xd, "AFPointSelectionMethod",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ETTLII: TagInfoByte = TagInfoByte(
        0xe, "ETTLII",
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

    public val MAGNIFIED_VIEW: TagInfoByte = TagInfoByte(
        0x11, "MagnifiedView",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LENS_AF_STOP_BUTTON: TagInfoByte = TagInfoByte(
        0x12, "LensAFStopButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ALL: List<TagInfo> = listOf(
        SET_FUNCTION_WHEN_SHOOTING,
        LONG_EXPOSURE_NOISE_REDUCTION,
        FLASH_SYNC_SPEED_AV,
        SHUTTER_AE_LOCK,
        AF_ASSIST_BEAM,
        EXPOSURE_LEVEL_INCREMENTS,
        FLASH_FIRING,
        ISO_EXPANSION,
        AEB_SEQUENCE_AUTO_CANCEL,
        SUPERIMPOSED_DISPLAY,
        MENU_BUTTON_DISPLAY_POSITION,
        MIRROR_LOCKUP,
        AF_POINT_SELECTION_METHOD,
        ETTLII,
        SHUTTER_CURTAIN_SYNC,
        SAFETY_SHIFT_IN_AV_OR_TV,
        MAGNIFIED_VIEW,
        LENS_AF_STOP_BUTTON
    )
}
