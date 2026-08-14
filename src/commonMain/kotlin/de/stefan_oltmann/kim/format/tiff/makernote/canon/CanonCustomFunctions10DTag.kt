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
 * Tags of the Functions10D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Functions10D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCustomFunctions10DTag {

    public val SET_BUTTON_WHEN_SHOOTING: TagInfoByte = TagInfoByte(
        0x1, "SetButtonWhenShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_RELEASE_NO_CF_CARD: TagInfoByte = TagInfoByte(
        0x2, "ShutterReleaseNoCFCard",
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

    public val AF_ASSIST: TagInfoByte = TagInfoByte(
        0x5, "AFAssist",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_LEVEL_INCREMENTS: TagInfoByte = TagInfoByte(
        0x6, "ExposureLevelIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_REGISTRATION: TagInfoByte = TagInfoByte(
        0x7, "AFPointRegistration",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val RAW_AND_JPG_RECORDING: TagInfoByte = TagInfoByte(
        0x8, "RawAndJpgRecording",
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

    public val ASSIST_BUTTON_FUNCTION: TagInfoByte = TagInfoByte(
        0xd, "AssistButtonFunction",
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

    public val LENS_AF_STOP_BUTTON: TagInfoByte = TagInfoByte(
        0x11, "LensAFStopButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ALL: List<TagInfo> = listOf(
        SET_BUTTON_WHEN_SHOOTING,
        SHUTTER_RELEASE_NO_CF_CARD,
        FLASH_SYNC_SPEED_AV,
        SHUTTER_AE_LOCK,
        AF_ASSIST,
        EXPOSURE_LEVEL_INCREMENTS,
        AF_POINT_REGISTRATION,
        RAW_AND_JPG_RECORDING,
        AEB_SEQUENCE_AUTO_CANCEL,
        SUPERIMPOSED_DISPLAY,
        MENU_BUTTON_DISPLAY_POSITION,
        MIRROR_LOCKUP,
        ASSIST_BUTTON_FUNCTION,
        FILL_FLASH_AUTO_REDUCTION,
        SHUTTER_CURTAIN_SYNC,
        SAFETY_SHIFT_IN_AV_OR_TV,
        LENS_AF_STOP_BUTTON
    )
}
