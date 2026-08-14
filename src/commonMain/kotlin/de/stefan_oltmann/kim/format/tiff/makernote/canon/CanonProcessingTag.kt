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

/**
 * Tags of the Processing maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Processing
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonProcessingTag {

    public val TONE_CURVE: TagInfoSShort = TagInfoSShort(
        0x1, "ToneCurve",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val SHARPNESS: TagInfoSShort = TagInfoSShort(
        0x2, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val SHARPNESS_FREQUENCY: TagInfoSShort = TagInfoSShort(
        0x3, "SharpnessFrequency",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val SENSOR_RED_LEVEL: TagInfoSShort = TagInfoSShort(
        0x4, "SensorRedLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val SENSOR_BLUE_LEVEL: TagInfoSShort = TagInfoSShort(
        0x5, "SensorBlueLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val WHITE_BALANCE_RED: TagInfoSShort = TagInfoSShort(
        0x6, "WhiteBalanceRed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val WHITE_BALANCE_BLUE: TagInfoSShort = TagInfoSShort(
        0x7, "WhiteBalanceBlue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val WHITE_BALANCE: TagInfoSShort = TagInfoSShort(
        0x8, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoSShort = TagInfoSShort(
        0x9, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val PICTURE_STYLE: TagInfoSShort = TagInfoSShort(
        0xa, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val DIGITAL_GAIN: TagInfoSShort = TagInfoSShort(
        0xb, "DigitalGain",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val WB_SHIFT_AB: TagInfoSShort = TagInfoSShort(
        0xc, "WBShiftAB",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val WB_SHIFT_GM: TagInfoSShort = TagInfoSShort(
        0xd, "WBShiftGM",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val UNSHARP_MASK_FINENESS: TagInfoSShort = TagInfoSShort(
        0xe, "UnsharpMaskFineness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val UNSHARP_MASK_THRESHOLD: TagInfoSShort = TagInfoSShort(
        0xf, "UnsharpMaskThreshold",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        TONE_CURVE,
        SHARPNESS,
        SHARPNESS_FREQUENCY,
        SENSOR_RED_LEVEL,
        SENSOR_BLUE_LEVEL,
        WHITE_BALANCE_RED,
        WHITE_BALANCE_BLUE,
        WHITE_BALANCE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE,
        DIGITAL_GAIN,
        WB_SHIFT_AB,
        WB_SHIFT_GM,
        UNSHARP_MASK_FINENESS,
        UNSHARP_MASK_THRESHOLD
    )
}
