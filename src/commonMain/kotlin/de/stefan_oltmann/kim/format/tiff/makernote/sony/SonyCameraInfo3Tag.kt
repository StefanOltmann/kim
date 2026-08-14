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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the CameraInfo3 maker note sub-directory.
 *
 * The AF status grid matches the layout that ExifTool uses for the
 * A4x0/A5x0/NEX cameras. The current ExifTool tables additionally
 * document A33-specific entries at 0xe/0x10/0x19/0x1c/0x20/0x1d, but
 * those would read the AF data of the other models, so they are not
 * part of this table.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#CameraInfo3
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object SonyCameraInfo3Tag {

    public val LENS_SPEC: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "LensSpec", 8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_POINT_SELECTED: TagInfoByte = TagInfoByte(
        0x14, "AFPointSelected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val FOCUS_MODE: TagInfoByte = TagInfoByte(
        0x15, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_POINT: TagInfoByte = TagInfoByte(
        0x18, "AFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_ACTIVE_SENSOR: TagInfoSShort = TagInfoSShort(
        0x1b, "AFStatusActiveSensor",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_TOP_RIGHT: TagInfoSShort = TagInfoSShort(
        0x1d, "AFStatusTop-right",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_BOTTOM_RIGHT: TagInfoSShort = TagInfoSShort(
        0x1f, "AFStatusBottom-right",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_BOTTOM: TagInfoSShort = TagInfoSShort(
        0x21, "AFStatusBottom",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_MIDDLE_HORIZONTAL: TagInfoSShort = TagInfoSShort(
        0x23, "AFStatusMiddleHorizontal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_CENTER_VERTICAL: TagInfoSShort = TagInfoSShort(
        0x25, "AFStatusCenterVertical",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_TOP: TagInfoSShort = TagInfoSShort(
        0x27, "AFStatusTop",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_TOP_LEFT: TagInfoSShort = TagInfoSShort(
        0x29, "AFStatusTop-left",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_BOTTOM_LEFT: TagInfoSShort = TagInfoSShort(
        0x2b, "AFStatusBottom-left",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_LEFT: TagInfoSShort = TagInfoSShort(
        0x2d, "AFStatusLeft",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_CENTER_HORIZONTAL: TagInfoSShort = TagInfoSShort(
        0x2f, "AFStatusCenterHorizontal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val AF_STATUS_RIGHT: TagInfoSShort = TagInfoSShort(
        0x31, "AFStatusRight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3
    )

    public val ALL: List<TagInfo> = listOf(
        LENS_SPEC,
        AF_POINT_SELECTED,
        FOCUS_MODE,
        AF_POINT,
        AF_STATUS_ACTIVE_SENSOR,
        AF_STATUS_TOP_RIGHT,
        AF_STATUS_BOTTOM_RIGHT,
        AF_STATUS_BOTTOM,
        AF_STATUS_MIDDLE_HORIZONTAL,
        AF_STATUS_CENTER_VERTICAL,
        AF_STATUS_TOP,
        AF_STATUS_TOP_LEFT,
        AF_STATUS_BOTTOM_LEFT,
        AF_STATUS_LEFT,
        AF_STATUS_CENTER_HORIZONTAL,
        AF_STATUS_RIGHT
    )
}
