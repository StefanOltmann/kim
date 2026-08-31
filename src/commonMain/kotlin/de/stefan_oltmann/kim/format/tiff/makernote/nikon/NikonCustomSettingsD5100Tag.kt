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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the CustomSettingsD5100 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CustomSettingsD5100
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonCustomSettingsD5100Tag {

    public val AF_C_PRIORITY_SELECTION: TagInfoByte = TagInfoByte(
        0x0, "AF-CPrioritySelection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val AF_ASSIST: TagInfoByte = TagInfoByte(
        0x1, "AFAssist",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val BEEP: TagInfoByte = TagInfoByte(
        0x3, "Beep",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val FILE_NUMBER_SEQUENCE: TagInfoByte = TagInfoByte(
        0x4, "FileNumberSequence",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val RANGE_FINDER: TagInfoByte = TagInfoByte(
        0x5, "RangeFinder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val EV_STEP_SIZE: TagInfoByte = TagInfoByte(
        0x6, "EVStepSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val EXPOSURE_DELAY_MODE: TagInfoByte = TagInfoByte(
        0xa, "ExposureDelayMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val AUTO_BRACKET_SET: TagInfoByte = TagInfoByte(
        0xc, "AutoBracketSet",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val TIMER_FUNCTION_BUTTON: TagInfoByte = TagInfoByte(
        0xd, "TimerFunctionButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val AE_LOCK_BUTTON: TagInfoByte = TagInfoByte(
        0x10, "AELockButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val SHUTTER_RELEASE_BUTTON_AE_L: TagInfoByte = TagInfoByte(
        0x11, "ShutterReleaseButtonAE-L",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val METERING_TIME: TagInfoByte = TagInfoByte(
        0x12, "MeteringTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val SELF_TIMER_TIME: TagInfoByte = TagInfoByte(
        0x13, "SelfTimerTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val IMAGE_REVIEW_TIME: TagInfoByte = TagInfoByte(
        0x14, "ImageReviewTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val PLAYBACK_MENUS_TIME: TagInfoByte = TagInfoByte(
        0x15, "PlaybackMenusTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val INTERNAL_FLASH: TagInfoByte = TagInfoByte(
        0x17, "InternalFlash",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
    )

    public val ALL: List<TagInfo> = listOf(
        AF_C_PRIORITY_SELECTION,
        AF_ASSIST,
        BEEP,
        FILE_NUMBER_SEQUENCE,
        RANGE_FINDER,
        EV_STEP_SIZE,
        EXPOSURE_DELAY_MODE,
        AUTO_BRACKET_SET,
        TIMER_FUNCTION_BUTTON,
        AE_LOCK_BUTTON,
        SHUTTER_RELEASE_BUTTON_AE_L,
        METERING_TIME,
        SELF_TIMER_TIME,
        IMAGE_REVIEW_TIME,
        PLAYBACK_MENUS_TIME,
        INTERNAL_FLASH
    )
}
