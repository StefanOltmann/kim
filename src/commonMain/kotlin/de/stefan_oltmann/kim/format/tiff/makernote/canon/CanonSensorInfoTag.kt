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
 * Tags of the SensorInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#SensorInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonSensorInfoTag {

    public val SENSOR_WIDTH: TagInfoShort = TagInfoShort(
        0x1, "SensorWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val SENSOR_HEIGHT: TagInfoShort = TagInfoShort(
        0x2, "SensorHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val SENSOR_LEFT_BORDER: TagInfoShort = TagInfoShort(
        0x5, "SensorLeftBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val SENSOR_TOP_BORDER: TagInfoShort = TagInfoShort(
        0x6, "SensorTopBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val SENSOR_RIGHT_BORDER: TagInfoShort = TagInfoShort(
        0x7, "SensorRightBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val SENSOR_BOTTOM_BORDER: TagInfoShort = TagInfoShort(
        0x8, "SensorBottomBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val BLACK_MASK_LEFT_BORDER: TagInfoShort = TagInfoShort(
        0x9, "BlackMaskLeftBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val BLACK_MASK_TOP_BORDER: TagInfoShort = TagInfoShort(
        0xa, "BlackMaskTopBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val BLACK_MASK_RIGHT_BORDER: TagInfoShort = TagInfoShort(
        0xb, "BlackMaskRightBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val BLACK_MASK_BOTTOM_BORDER: TagInfoShort = TagInfoShort(
        0xc, "BlackMaskBottomBorder",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        SENSOR_WIDTH,
        SENSOR_HEIGHT,
        SENSOR_LEFT_BORDER,
        SENSOR_TOP_BORDER,
        SENSOR_RIGHT_BORDER,
        SENSOR_BOTTOM_BORDER,
        BLACK_MASK_LEFT_BORDER,
        BLACK_MASK_TOP_BORDER,
        BLACK_MASK_RIGHT_BORDER,
        BLACK_MASK_BOTTOM_BORDER
    )
}
