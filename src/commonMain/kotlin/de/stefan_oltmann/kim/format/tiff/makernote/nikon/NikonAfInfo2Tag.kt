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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the AFInfo2 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Nikon.html#AFInfo2V0100
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonAfInfo2Tag {

    public val AF_INFO2_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "AFInfo2Version", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val AF_DETECTION_METHOD: TagInfoByte = TagInfoByte(
        0x4, "AFDetectionMethod",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val AF_AREA_MODE: TagInfoByte = TagInfoByte(
        0x5, "AFAreaMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val FOCUS_POINT_SCHEMA: TagInfoByte = TagInfoByte(
        0x6, "FocusPointSchema",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val PRIMARY_AF_POINT: TagInfoByte = TagInfoByte(
        0x7, "PrimaryAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val AF_POINTS_USED: TagInfoUndefineds = TagInfoUndefineds(
        0x8, "AFPointsUsed", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The width of the image area evaluated by the AF system.
     */
    public val AF_IMAGE_WIDTH: TagInfoShort = TagInfoShort(
        0x10, "AFImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The height of the image area evaluated by the AF system.
     */
    public val AF_IMAGE_HEIGHT: TagInfoShort = TagInfoShort(
        0x12, "AFImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The X position of the AF area.
     */
    public val AF_AREA_X_POSITION: TagInfoShort = TagInfoShort(
        0x14, "AFAreaXPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The Y position of the AF area.
     */
    public val AF_AREA_Y_POSITION: TagInfoShort = TagInfoShort(
        0x16, "AFAreaYPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The width of the AF area.
     */
    public val AF_AREA_WIDTH: TagInfoShort = TagInfoShort(
        0x18, "AFAreaWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * The height of the AF area.
     */
    public val AF_AREA_HEIGHT: TagInfoShort = TagInfoShort(
        0x1a, "AFAreaHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    /**
     * Whether the subject is in focus according to contrast detection.
     */
    public val CONTRAST_DETECT_AF_IN_FOCUS: TagInfoByte = TagInfoByte(
        0x1c, "ContrastDetectAFInFocus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2
    )

    public val ALL: List<TagInfo> = listOf(
        AF_INFO2_VERSION, AF_DETECTION_METHOD, AF_AREA_MODE, FOCUS_POINT_SCHEMA,
        PRIMARY_AF_POINT, AF_POINTS_USED, AF_IMAGE_WIDTH, AF_IMAGE_HEIGHT,
        AF_AREA_X_POSITION, AF_AREA_Y_POSITION, AF_AREA_WIDTH, AF_AREA_HEIGHT,
        CONTRAST_DETECT_AF_IN_FOCUS
    )
}
