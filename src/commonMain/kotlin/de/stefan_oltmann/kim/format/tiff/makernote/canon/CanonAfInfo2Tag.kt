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
 * Tags of the AFInfo2 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#AFInfo2
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonAfInfo2Tag {

    public val AF_INFO_SIZE: TagInfoShort = TagInfoShort(
        0x0, "AFInfoSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val AF_AREA_MODE: TagInfoShort = TagInfoShort(
        0x1, "AFAreaMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val NUM_AF_POINTS: TagInfoShort = TagInfoShort(
        0x2, "NumAFPoints",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val VALID_AF_POINTS: TagInfoShort = TagInfoShort(
        0x3, "ValidAFPoints",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val CANON_IMAGE_WIDTH: TagInfoShort = TagInfoShort(
        0x4, "CanonImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val CANON_IMAGE_HEIGHT: TagInfoShort = TagInfoShort(
        0x5, "CanonImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val AF_IMAGE_WIDTH: TagInfoShort = TagInfoShort(
        0x6, "AFImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val AF_IMAGE_HEIGHT: TagInfoShort = TagInfoShort(
        0x7, "AFImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2
    )

    public val ALL: List<TagInfo> = listOf(
        AF_INFO_SIZE,
        AF_AREA_MODE,
        NUM_AF_POINTS,
        VALID_AF_POINTS,
        CANON_IMAGE_WIDTH,
        CANON_IMAGE_HEIGHT,
        AF_IMAGE_WIDTH,
        AF_IMAGE_HEIGHT
    )
}
