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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShortRev

/**
 * Tags of the CameraInfo70D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo70D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo70DTag {

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

    public val CAMERA_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x1b, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x23, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_ORIENTATION: TagInfoByte = TagInfoByte(
        0x84, "CameraOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_UPPER: TagInfoShortRev = TagInfoShortRev(
        0x93, "FocusDistanceUpper",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_LOWER: TagInfoShortRev = TagInfoShortRev(
        0x95, "FocusDistanceLower",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0xc7, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0x166, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x168, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x16a, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FIRMWARE_VERSION: TagInfoAscii = TagInfoAscii(
        0x25e, "FirmwareVersion", 6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX: TagInfoLong = TagInfoLong(
        0x2b3, "FileIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX: TagInfoLong = TagInfoLong(
        0x2bf, "DirectoryIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        F_NUMBER,
        EXPOSURE_TIME,
        ISO,
        CAMERA_TEMPERATURE,
        FOCAL_LENGTH,
        CAMERA_ORIENTATION,
        FOCUS_DISTANCE_UPPER,
        FOCUS_DISTANCE_LOWER,
        COLOR_TEMPERATURE,
        LENS_TYPE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        FIRMWARE_VERSION,
        FILE_INDEX,
        DIRECTORY_INDEX
    )
}
