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
 * Tags of the FileInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FileInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonFileInfoTag {

    public val BRACKET_MODE: TagInfoSShort = TagInfoSShort(
        0x3, "BracketMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val BRACKET_VALUE: TagInfoSShort = TagInfoSShort(
        0x4, "BracketValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val BRACKET_SHOT_NUMBER: TagInfoSShort = TagInfoSShort(
        0x5, "BracketShotNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val RAW_JPG_QUALITY: TagInfoSShort = TagInfoSShort(
        0x6, "RawJpgQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val RAW_JPG_SIZE: TagInfoSShort = TagInfoSShort(
        0x7, "RawJpgSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val LONG_EXPOSURE_NOISE_REDUCTION2: TagInfoSShort = TagInfoSShort(
        0x8, "LongExposureNoiseReduction2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val WB_BRACKET_MODE: TagInfoSShort = TagInfoSShort(
        0x9, "WBBracketMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val WB_BRACKET_VALUE_AB: TagInfoSShort = TagInfoSShort(
        0xc, "WBBracketValueAB",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val WB_BRACKET_VALUE_GM: TagInfoSShort = TagInfoSShort(
        0xd, "WBBracketValueGM",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val FILTER_EFFECT: TagInfoSShort = TagInfoSShort(
        0xe, "FilterEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val TONING_EFFECT: TagInfoSShort = TagInfoSShort(
        0xf, "ToningEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val MACRO_MAGNIFICATION: TagInfoSShort = TagInfoSShort(
        0x10, "MacroMagnification",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val LIVE_VIEW_SHOOTING: TagInfoSShort = TagInfoSShort(
        0x13, "LiveViewShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val FOCUS_DISTANCE_UPPER: TagInfoShort = TagInfoShort(
        0x14, "FocusDistanceUpper",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val FOCUS_DISTANCE_LOWER: TagInfoShort = TagInfoShort(
        0x15, "FocusDistanceLower",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val SHUTTER_MODE: TagInfoSShort = TagInfoSShort(
        0x17, "ShutterMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val FLASH_EXPOSURE_LOCK: TagInfoSShort = TagInfoSShort(
        0x19, "FlashExposureLock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val ANTI_FLICKER: TagInfoSShort = TagInfoSShort(
        0x20, "AntiFlicker",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val RF_LENS_TYPE: TagInfoShort = TagInfoShort(
        0x3d, "RFLensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        BRACKET_MODE,
        BRACKET_VALUE,
        BRACKET_SHOT_NUMBER,
        RAW_JPG_QUALITY,
        RAW_JPG_SIZE,
        LONG_EXPOSURE_NOISE_REDUCTION2,
        WB_BRACKET_MODE,
        WB_BRACKET_VALUE_AB,
        WB_BRACKET_VALUE_GM,
        FILTER_EFFECT,
        TONING_EFFECT,
        MACRO_MAGNIFICATION,
        LIVE_VIEW_SHOOTING,
        FOCUS_DISTANCE_UPPER,
        FOCUS_DISTANCE_LOWER,
        SHUTTER_MODE,
        FLASH_EXPOSURE_LOCK,
        ANTI_FLICKER,
        RF_LENS_TYPE
    )
}
