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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the PictureControl maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#PictureControl
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonPictureControlTag {

    public val PICTURE_CONTROL_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "PictureControlVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val PICTURE_CONTROL_NAME: TagInfoAscii = TagInfoAscii(
        0x4, "PictureControlName", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val PICTURE_CONTROL_BASE: TagInfoAscii = TagInfoAscii(
        0x18, "PictureControlBase", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val PICTURE_CONTROL_ADJUST: TagInfoByte = TagInfoByte(
        0x30, "PictureControlAdjust",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val PICTURE_CONTROL_QUICK_ADJUST: TagInfoByte = TagInfoByte(
        0x31, "PictureControlQuickAdjust",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val SHARPNESS: TagInfoByte = TagInfoByte(
        0x32, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val CONTRAST: TagInfoByte = TagInfoByte(
        0x33, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val BRIGHTNESS: TagInfoByte = TagInfoByte(
        0x34, "Brightness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val SATURATION: TagInfoByte = TagInfoByte(
        0x35, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val HUE_ADJUSTMENT: TagInfoByte = TagInfoByte(
        0x36, "HueAdjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val FILTER_EFFECT: TagInfoByte = TagInfoByte(
        0x37, "FilterEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val TONING_EFFECT: TagInfoByte = TagInfoByte(
        0x38, "ToningEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val TONING_SATURATION: TagInfoByte = TagInfoByte(
        0x39, "ToningSaturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL
    )

    public val ALL: List<TagInfo> = listOf(
        PICTURE_CONTROL_VERSION,
        PICTURE_CONTROL_NAME,
        PICTURE_CONTROL_BASE,
        PICTURE_CONTROL_ADJUST,
        PICTURE_CONTROL_QUICK_ADJUST,
        SHARPNESS,
        CONTRAST,
        BRIGHTNESS,
        SATURATION,
        HUE_ADJUSTMENT,
        FILTER_EFFECT,
        TONING_EFFECT,
        TONING_SATURATION
    )
}
