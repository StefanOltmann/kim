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
 * Tags of the FunctionsD30 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FunctionsD30
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCustomFunctionsD30Tag {

    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x1, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_AE_LOCK: TagInfoByte = TagInfoByte(
        0x2, "Shutter-AELock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MIRROR_LOCKUP: TagInfoByte = TagInfoByte(
        0x3, "MirrorLockup",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_LEVEL_INCREMENTS: TagInfoByte = TagInfoByte(
        0x4, "ExposureLevelIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_ASSIST: TagInfoByte = TagInfoByte(
        0x5, "AFAssist",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_SYNC_SPEED_AV: TagInfoByte = TagInfoByte(
        0x6, "FlashSyncSpeedAv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_SEQUENCE_AUTO_CANCEL: TagInfoByte = TagInfoByte(
        0x7, "AEBSequenceAutoCancel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_CURTAIN_SYNC: TagInfoByte = TagInfoByte(
        0x8, "ShutterCurtainSync",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LENS_AF_STOP_BUTTON: TagInfoByte = TagInfoByte(
        0x9, "LensAFStopButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FILL_FLASH_AUTO_REDUCTION: TagInfoByte = TagInfoByte(
        0xa, "FillFlashAutoReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MENU_BUTTON_RETURN: TagInfoByte = TagInfoByte(
        0xb, "MenuButtonReturn",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SET_BUTTON_WHEN_SHOOTING: TagInfoByte = TagInfoByte(
        0xc, "SetButtonWhenShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SENSOR_CLEANING: TagInfoByte = TagInfoByte(
        0xd, "SensorCleaning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SUPERIMPOSED_DISPLAY: TagInfoByte = TagInfoByte(
        0xe, "SuperimposedDisplay",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_RELEASE_NO_CF_CARD: TagInfoByte = TagInfoByte(
        0xf, "ShutterReleaseNoCFCard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ALL: List<TagInfo> = listOf(
        LONG_EXPOSURE_NOISE_REDUCTION,
        SHUTTER_AE_LOCK,
        MIRROR_LOCKUP,
        EXPOSURE_LEVEL_INCREMENTS,
        AF_ASSIST,
        FLASH_SYNC_SPEED_AV,
        AEB_SEQUENCE_AUTO_CANCEL,
        SHUTTER_CURTAIN_SYNC,
        LENS_AF_STOP_BUTTON,
        FILL_FLASH_AUTO_REDUCTION,
        MENU_BUTTON_RETURN,
        SET_BUTTON_WHEN_SHOOTING,
        SENSOR_CLEANING,
        SUPERIMPOSED_DISPLAY,
        SHUTTER_RELEASE_NO_CF_CARD
    )
}
