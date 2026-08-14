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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShortRev

/**
 * Tags of the CameraInfo5D maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfo5D
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfo5DTag {

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

    public val LENS_TYPE: TagInfoShortRev = TagInfoShortRev(
        0xc, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x17, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MACRO_MAGNIFICATION: TagInfoSByte = TagInfoSByte(
        0x1b, "MacroMagnification",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_ORIENTATION: TagInfoSByte = TagInfoSByte(
        0x27, "CameraOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x28, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val AF_POINTS_IN_FOCUS5_D: TagInfoShortRev = TagInfoShortRev(
        0x38, "AFPointsInFocus5D",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0x54, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0x58, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val PICTURE_STYLE: TagInfoByte = TagInfoByte(
        0x6c, "PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MIN_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x93, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val MAX_FOCAL_LENGTH: TagInfoShortRev = TagInfoShortRev(
        0x95, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val LENS_TYPE_2: TagInfoShortRev = TagInfoShortRev(
        0x97, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FIRMWARE_REVISION: TagInfoAscii = TagInfoAscii(
        0xa4, "FirmwareRevision", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHORT_OWNER_NAME: TagInfoAscii = TagInfoAscii(
        0xac, "ShortOwnerName", 16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX: TagInfoLong = TagInfoLong(
        0xcc, "DirectoryIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX: TagInfoShort = TagInfoShort(
        0xd0, "FileIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_STANDARD: TagInfoSByte = TagInfoSByte(
        0xe8, "ContrastStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_PORTRAIT: TagInfoSByte = TagInfoSByte(
        0xe9, "ContrastPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_LANDSCAPE: TagInfoSByte = TagInfoSByte(
        0xea, "ContrastLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_NEUTRAL: TagInfoSByte = TagInfoSByte(
        0xeb, "ContrastNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_FAITHFUL: TagInfoSByte = TagInfoSByte(
        0xec, "ContrastFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_MONOCHROME: TagInfoSByte = TagInfoSByte(
        0xed, "ContrastMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_USER_DEF1: TagInfoSByte = TagInfoSByte(
        0xee, "ContrastUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_USER_DEF2: TagInfoSByte = TagInfoSByte(
        0xef, "ContrastUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CONTRAST_USER_DEF3: TagInfoSByte = TagInfoSByte(
        0xf0, "ContrastUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_STANDARD: TagInfoSByte = TagInfoSByte(
        0xf1, "SharpnessStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_PORTRAIT: TagInfoSByte = TagInfoSByte(
        0xf2, "SharpnessPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_LANDSCAPE: TagInfoSByte = TagInfoSByte(
        0xf3, "SharpnessLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_NEUTRAL: TagInfoSByte = TagInfoSByte(
        0xf4, "SharpnessNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_FAITHFUL: TagInfoSByte = TagInfoSByte(
        0xf5, "SharpnessFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_MONOCHROME: TagInfoSByte = TagInfoSByte(
        0xf6, "SharpnessMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_USER_DEF1: TagInfoSByte = TagInfoSByte(
        0xf7, "SharpnessUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_USER_DEF2: TagInfoSByte = TagInfoSByte(
        0xf8, "SharpnessUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHARPNESS_USER_DEF3: TagInfoSByte = TagInfoSByte(
        0xf9, "SharpnessUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_STANDARD: TagInfoSByte = TagInfoSByte(
        0xfa, "SaturationStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_PORTRAIT: TagInfoSByte = TagInfoSByte(
        0xfb, "SaturationPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_LANDSCAPE: TagInfoSByte = TagInfoSByte(
        0xfc, "SaturationLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_NEUTRAL: TagInfoSByte = TagInfoSByte(
        0xfd, "SaturationNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_FAITHFUL: TagInfoSByte = TagInfoSByte(
        0xfe, "SaturationFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILTER_EFFECT_MONOCHROME: TagInfoSByte = TagInfoSByte(
        0xff, "FilterEffectMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_USER_DEF1: TagInfoSByte = TagInfoSByte(
        0x100, "SaturationUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_USER_DEF2: TagInfoSByte = TagInfoSByte(
        0x101, "SaturationUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SATURATION_USER_DEF3: TagInfoSByte = TagInfoSByte(
        0x102, "SaturationUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_STANDARD: TagInfoSByte = TagInfoSByte(
        0x103, "ColorToneStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_PORTRAIT: TagInfoSByte = TagInfoSByte(
        0x104, "ColorTonePortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_LANDSCAPE: TagInfoSByte = TagInfoSByte(
        0x105, "ColorToneLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_NEUTRAL: TagInfoSByte = TagInfoSByte(
        0x106, "ColorToneNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_FAITHFUL: TagInfoSByte = TagInfoSByte(
        0x107, "ColorToneFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val TONING_EFFECT_MONOCHROME: TagInfoSByte = TagInfoSByte(
        0x108, "ToningEffectMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_USER_DEF1: TagInfoSByte = TagInfoSByte(
        0x109, "ColorToneUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_USER_DEF2: TagInfoSByte = TagInfoSByte(
        0x10a, "ColorToneUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val COLOR_TONE_USER_DEF3: TagInfoSByte = TagInfoSByte(
        0x10b, "ColorToneUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val USER_DEF1_PICTURE_STYLE: TagInfoShort = TagInfoShort(
        0x10c, "UserDef1PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val USER_DEF2_PICTURE_STYLE: TagInfoShort = TagInfoShort(
        0x10e, "UserDef2PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val USER_DEF3_PICTURE_STYLE: TagInfoShort = TagInfoShort(
        0x110, "UserDef3PictureStyle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val TIME_STAMP: TagInfoLong = TagInfoLong(
        0x11c, "TimeStamp",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        F_NUMBER,
        EXPOSURE_TIME,
        ISO,
        LENS_TYPE,
        CAMERA_TEMPERATURE,
        MACRO_MAGNIFICATION,
        CAMERA_ORIENTATION,
        FOCAL_LENGTH,
        AF_POINTS_IN_FOCUS5_D,
        WHITE_BALANCE,
        COLOR_TEMPERATURE,
        PICTURE_STYLE,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        LENS_TYPE_2,
        FIRMWARE_REVISION,
        SHORT_OWNER_NAME,
        DIRECTORY_INDEX,
        FILE_INDEX,
        CONTRAST_STANDARD,
        CONTRAST_PORTRAIT,
        CONTRAST_LANDSCAPE,
        CONTRAST_NEUTRAL,
        CONTRAST_FAITHFUL,
        CONTRAST_MONOCHROME,
        CONTRAST_USER_DEF1,
        CONTRAST_USER_DEF2,
        CONTRAST_USER_DEF3,
        SHARPNESS_STANDARD,
        SHARPNESS_PORTRAIT,
        SHARPNESS_LANDSCAPE,
        SHARPNESS_NEUTRAL,
        SHARPNESS_FAITHFUL,
        SHARPNESS_MONOCHROME,
        SHARPNESS_USER_DEF1,
        SHARPNESS_USER_DEF2,
        SHARPNESS_USER_DEF3,
        SATURATION_STANDARD,
        SATURATION_PORTRAIT,
        SATURATION_LANDSCAPE,
        SATURATION_NEUTRAL,
        SATURATION_FAITHFUL,
        FILTER_EFFECT_MONOCHROME,
        SATURATION_USER_DEF1,
        SATURATION_USER_DEF2,
        SATURATION_USER_DEF3,
        COLOR_TONE_STANDARD,
        COLOR_TONE_PORTRAIT,
        COLOR_TONE_LANDSCAPE,
        COLOR_TONE_NEUTRAL,
        COLOR_TONE_FAITHFUL,
        TONING_EFFECT_MONOCHROME,
        COLOR_TONE_USER_DEF1,
        COLOR_TONE_USER_DEF2,
        COLOR_TONE_USER_DEF3,
        USER_DEF1_PICTURE_STYLE,
        USER_DEF2_PICTURE_STYLE,
        USER_DEF3_PICTURE_STYLE,
        TIME_STAMP
    )
}
