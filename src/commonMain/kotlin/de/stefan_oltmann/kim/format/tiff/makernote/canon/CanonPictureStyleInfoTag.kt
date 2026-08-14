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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong

/**
 * Tags of the PictureStyleInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#PictureStyleInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonPictureStyleInfoTag {

    public val CONTRAST_STANDARD: TagInfoSLong = TagInfoSLong(
        0x0, "ContrastStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_STANDARD: TagInfoSLong = TagInfoSLong(
        0x4, "SharpnessStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_STANDARD: TagInfoSLong = TagInfoSLong(
        0x8, "SaturationStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_STANDARD: TagInfoSLong = TagInfoSLong(
        0xc, "ColorToneStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_STANDARD: TagInfoSLong = TagInfoSLong(
        0x10, "FilterEffectStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_STANDARD: TagInfoSLong = TagInfoSLong(
        0x14, "ToningEffectStandard",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x18, "ContrastPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x1c, "SharpnessPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x20, "SaturationPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x24, "ColorTonePortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x28, "FilterEffectPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_PORTRAIT: TagInfoSLong = TagInfoSLong(
        0x2c, "ToningEffectPortrait",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x30, "ContrastLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x34, "SharpnessLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x38, "SaturationLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x3c, "ColorToneLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x40, "FilterEffectLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_LANDSCAPE: TagInfoSLong = TagInfoSLong(
        0x44, "ToningEffectLandscape",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x48, "ContrastNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x4c, "SharpnessNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x50, "SaturationNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x54, "ColorToneNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x58, "FilterEffectNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_NEUTRAL: TagInfoSLong = TagInfoSLong(
        0x5c, "ToningEffectNeutral",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x60, "ContrastFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x64, "SharpnessFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x68, "SaturationFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x6c, "ColorToneFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x70, "FilterEffectFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_FAITHFUL: TagInfoSLong = TagInfoSLong(
        0x74, "ToningEffectFaithful",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x78, "ContrastMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x7c, "SharpnessMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x80, "SaturationMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x84, "ColorToneMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x88, "FilterEffectMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_MONOCHROME: TagInfoSLong = TagInfoSLong(
        0x8c, "ToningEffectMonochrome",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0x90, "ContrastUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0x94, "SharpnessUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0x98, "SaturationUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0x9c, "ColorToneUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0xa0, "FilterEffectUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_USER_DEF1: TagInfoSLong = TagInfoSLong(
        0xa4, "ToningEffectUserDef1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xa8, "ContrastUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xac, "SharpnessUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xb0, "SaturationUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xb4, "ColorToneUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xb8, "FilterEffectUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_USER_DEF2: TagInfoSLong = TagInfoSLong(
        0xbc, "ToningEffectUserDef2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val CONTRAST_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xc0, "ContrastUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SHARPNESS_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xc4, "SharpnessUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val SATURATION_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xc8, "SaturationUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val COLOR_TONE_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xcc, "ColorToneUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val FILTER_EFFECT_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xd0, "FilterEffectUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val TONING_EFFECT_USER_DEF3: TagInfoSLong = TagInfoSLong(
        0xd4, "ToningEffectUserDef3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        CONTRAST_STANDARD,
        SHARPNESS_STANDARD,
        SATURATION_STANDARD,
        COLOR_TONE_STANDARD,
        FILTER_EFFECT_STANDARD,
        TONING_EFFECT_STANDARD,
        CONTRAST_PORTRAIT,
        SHARPNESS_PORTRAIT,
        SATURATION_PORTRAIT,
        COLOR_TONE_PORTRAIT,
        FILTER_EFFECT_PORTRAIT,
        TONING_EFFECT_PORTRAIT,
        CONTRAST_LANDSCAPE,
        SHARPNESS_LANDSCAPE,
        SATURATION_LANDSCAPE,
        COLOR_TONE_LANDSCAPE,
        FILTER_EFFECT_LANDSCAPE,
        TONING_EFFECT_LANDSCAPE,
        CONTRAST_NEUTRAL,
        SHARPNESS_NEUTRAL,
        SATURATION_NEUTRAL,
        COLOR_TONE_NEUTRAL,
        FILTER_EFFECT_NEUTRAL,
        TONING_EFFECT_NEUTRAL,
        CONTRAST_FAITHFUL,
        SHARPNESS_FAITHFUL,
        SATURATION_FAITHFUL,
        COLOR_TONE_FAITHFUL,
        FILTER_EFFECT_FAITHFUL,
        TONING_EFFECT_FAITHFUL,
        CONTRAST_MONOCHROME,
        SHARPNESS_MONOCHROME,
        SATURATION_MONOCHROME,
        COLOR_TONE_MONOCHROME,
        FILTER_EFFECT_MONOCHROME,
        TONING_EFFECT_MONOCHROME,
        CONTRAST_USER_DEF1,
        SHARPNESS_USER_DEF1,
        SATURATION_USER_DEF1,
        COLOR_TONE_USER_DEF1,
        FILTER_EFFECT_USER_DEF1,
        TONING_EFFECT_USER_DEF1,
        CONTRAST_USER_DEF2,
        SHARPNESS_USER_DEF2,
        SATURATION_USER_DEF2,
        COLOR_TONE_USER_DEF2,
        FILTER_EFFECT_USER_DEF2,
        TONING_EFFECT_USER_DEF2,
        CONTRAST_USER_DEF3,
        SHARPNESS_USER_DEF3,
        SATURATION_USER_DEF3,
        COLOR_TONE_USER_DEF3,
        FILTER_EFFECT_USER_DEF3,
        TONING_EFFECT_USER_DEF3
    )
}
