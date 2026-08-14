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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShortRev

/**
 * Tags of the CameraInfo1DmkIIN maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo1DmkIIN
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo1DmkIINTag {

    public val EXPOSURE_TIME: TagInfoByte = TagInfoByte(
        0x4, "ExposureTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x9, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0xc, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x11, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x13, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE: TagInfoByte = TagInfoByte(
        0x36, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShortRev = TagInfoShortRev(
        0x37, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE: TagInfoByte = TagInfoByte(
        0x73, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS: TagInfoSByte = TagInfoSByte(
        0x74, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST: TagInfoSByte = TagInfoSByte(
        0x75, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION: TagInfoSByte = TagInfoSByte(
        0x76, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE: TagInfoSByte = TagInfoSByte(
        0x77, "ColorTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ISO: TagInfoAscii = TagInfoAscii(
        0x79, "ISO", 5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        EXPOSURE_TIME,
        FOCAL_LENGTH,
        LENS_TYPE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        WHITE_BALANCE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE,
        SHARPNESS,
        CONTRAST,
        SATURATION,
        COLOR_TONE,
        ISO
    )
}
