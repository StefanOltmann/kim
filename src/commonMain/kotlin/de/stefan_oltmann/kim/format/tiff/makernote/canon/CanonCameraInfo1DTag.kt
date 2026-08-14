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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShortRev

/**
 * Tags of the CameraInfo1D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo1D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo1DTag {

    public val EXPOSURE_TIME: TagInfoByte = TagInfoByte(
        0x4, "ExposureTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0xa, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0xd, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0xe, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0x10, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_FREQUENCY: TagInfoByte = TagInfoByte(
        0x41, "SharpnessFrequency",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS: TagInfoSByte = TagInfoSByte(
        0x42, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE: TagInfoByte = TagInfoByte(
        0x44, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_FREQUENCY_2: TagInfoByte = TagInfoByte(
        0x47, "SharpnessFrequency",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE_2: TagInfoByte = TagInfoByte(
        0x4a, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE: TagInfoByte = TagInfoByte(
        0x4b, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0x4e, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE_2: TagInfoByte = TagInfoByte(
        0x51, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        EXPOSURE_TIME,
        FOCAL_LENGTH,
        LENS_TYPE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        SHARPNESS_FREQUENCY,
        SHARPNESS,
        WHITE_BALANCE,
        SHARPNESS_FREQUENCY_2,
        WHITE_BALANCE_2,
        PICTURE_STYLE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE_2
    )
}
