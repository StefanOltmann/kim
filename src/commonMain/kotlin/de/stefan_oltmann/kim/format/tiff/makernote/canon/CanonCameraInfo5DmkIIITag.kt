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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the CameraInfo5DmkIII maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo5DmkIII
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo5DmkIIITag {

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
        0x7d, "CameraOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_UPPER: TagInfoShortRev = TagInfoShortRev(
        0x8c, "FocusDistanceUpper",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCUS_DISTANCE_LOWER: TagInfoShortRev = TagInfoShortRev(
        0x8e, "FocusDistanceLower",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0xbc, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0xc0, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE: TagInfoByte = TagInfoByte(
        0xf4, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0x153, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x155, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x157, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_SERIAL_NUMBER: TagInfoUndefineds = TagInfoUndefineds(
        0x164, "LensSerialNumber", 5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX: TagInfoLong = TagInfoLong(
        0x28c, "FileIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX2: TagInfoLong = TagInfoLong(
        0x290, "FileIndex2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX: TagInfoLong = TagInfoLong(
        0x298, "DirectoryIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX2: TagInfoLong = TagInfoLong(
        0x29c, "DirectoryIndex2",
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
        WHITE_BALANCE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE,
        LENS_TYPE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        LENS_SERIAL_NUMBER,
        FILE_INDEX,
        FILE_INDEX2,
        DIRECTORY_INDEX,
        DIRECTORY_INDEX2
    )
}
