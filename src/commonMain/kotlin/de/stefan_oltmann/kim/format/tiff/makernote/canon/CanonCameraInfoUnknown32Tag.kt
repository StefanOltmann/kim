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
 * Tags of the CameraInfoUnknown32 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfoUnknown32
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfoUnknown32Tag {

    public val CAMERA_TEMPERATURE: TagInfoSLong = TagInfoSLong(
        0x47, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE_2: TagInfoSLong = TagInfoSLong(
        0x53, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE_3: TagInfoSLong = TagInfoSLong(
        0x5b, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE_4: TagInfoSLong = TagInfoSLong(
        0x5c, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val CAMERA_TEMPERATURE_5: TagInfoSLong = TagInfoSLong(
        0x64, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        CAMERA_TEMPERATURE, CAMERA_TEMPERATURE_2, CAMERA_TEMPERATURE_3, CAMERA_TEMPERATURE_4, CAMERA_TEMPERATURE_5
    )
}
