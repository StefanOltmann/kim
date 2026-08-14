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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong

/**
 * Tags of the FilterInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FilterInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonFilterInfoTag {

    public val GRAINY_BW_FILTER: TagInfoLong = TagInfoLong(
        0x0101, "GrainyBWFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val SOFT_FOCUS_FILTER: TagInfoLong = TagInfoLong(
        0x0201, "SoftFocusFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val TOY_CAMERA_FILTER: TagInfoLong = TagInfoLong(
        0x0301, "ToyCameraFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val MINIATURE_FILTER: TagInfoLong = TagInfoLong(
        0x0401, "MiniatureFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val MINIATURE_FILTER_ORIENTATION: TagInfoLong = TagInfoLong(
        0x0402, "MiniatureFilterOrientation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val MINIATURE_FILTER_POSITION: TagInfoLong = TagInfoLong(
        0x0403, "MiniatureFilterPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val MINIATURE_FILTER_PARAMETER: TagInfoLong = TagInfoLong(
        0x0404, "MiniatureFilterParameter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val FISHEYE_FILTER: TagInfoLong = TagInfoLong(
        0x0501, "FisheyeFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val PAINTING_FILTER: TagInfoLong = TagInfoLong(
        0x0601, "PaintingFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val WATERCOLOR_FILTER: TagInfoLong = TagInfoLong(
        0x0701, "WatercolorFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        GRAINY_BW_FILTER,
        SOFT_FOCUS_FILTER,
        TOY_CAMERA_FILTER,
        MINIATURE_FILTER,
        MINIATURE_FILTER_ORIENTATION,
        MINIATURE_FILTER_POSITION,
        MINIATURE_FILTER_PARAMETER,
        FISHEYE_FILTER,
        PAINTING_FILTER,
        WATERCOLOR_FILTER
    )
}
