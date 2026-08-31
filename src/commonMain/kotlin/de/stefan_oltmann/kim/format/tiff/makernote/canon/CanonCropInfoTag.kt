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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort

/**
 * Tags of the CropInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CropInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCropInfoTag {

    public val CROP_LEFT_MARGIN: TagInfoShort = TagInfoShort(
        0x0, "CropLeftMargin",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CROP_INFO
    )

    public val CROP_RIGHT_MARGIN: TagInfoShort = TagInfoShort(
        0x1, "CropRightMargin",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CROP_INFO
    )

    public val CROP_TOP_MARGIN: TagInfoShort = TagInfoShort(
        0x2, "CropTopMargin",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CROP_INFO
    )

    public val CROP_BOTTOM_MARGIN: TagInfoShort = TagInfoShort(
        0x3, "CropBottomMargin",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CROP_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        CROP_LEFT_MARGIN, CROP_RIGHT_MARGIN, CROP_TOP_MARGIN, CROP_BOTTOM_MARGIN
    )
}
