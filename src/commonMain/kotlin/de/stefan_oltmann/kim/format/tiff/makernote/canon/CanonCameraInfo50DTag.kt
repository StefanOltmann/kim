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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShortRev

/**
 * Tags of the CameraInfo50D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo50D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo50DTag {

    public val F_NUMBER: TagInfoByte = TagInfoByte(
        0x3, "FNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val EXPOSURE_TIME: TagInfoByte = TagInfoByte(
        0x4, "ExposureTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ISO: TagInfoByte = TagInfoByte(
        0x6, "ISO",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val HIGHLIGHT_TONE_PRIORITY: TagInfoByte = TagInfoByte(
        0x7, "HighlightTonePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FLASH_METERING_MODE: TagInfoByte = TagInfoByte(
        0x15, "FlashMeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x19, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x1e, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_ORIENTATION: TagInfoByte = TagInfoByte(
        0x31, "CameraOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_UPPER: TagInfoShortRev = TagInfoShortRev(
        0x50, "FocusDistanceUpper",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_LOWER: TagInfoShortRev = TagInfoShortRev(
        0x52, "FocusDistanceLower",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0x6f, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0x73, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE: TagInfoByte = TagInfoByte(
        0xa7, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val HIGH_ISO_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0xbd, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val AUTO_LIGHTING_OPTIMIZER: TagInfoByte = TagInfoByte(
        0xbf, "AutoLightingOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0xea, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0xec, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0xee, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX: TagInfoLong = TagInfoLong(
        0x19b, "FileIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX: TagInfoLong = TagInfoLong(
        0x1a7, "DirectoryIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        F_NUMBER,
        EXPOSURE_TIME,
        ISO,
        HIGHLIGHT_TONE_PRIORITY,
        FLASH_METERING_MODE,
        CAMERA_TEMPERATURE,
        FOCAL_LENGTH,
        CAMERA_ORIENTATION,
        FOCUS_DISTANCE_UPPER,
        FOCUS_DISTANCE_LOWER,
        WHITE_BALANCE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE,
        HIGH_ISO_NOISE_REDUCTION,
        AUTO_LIGHTING_OPTIMIZER,
        LENS_TYPE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        FILE_INDEX,
        DIRECTORY_INDEX
    )
}
