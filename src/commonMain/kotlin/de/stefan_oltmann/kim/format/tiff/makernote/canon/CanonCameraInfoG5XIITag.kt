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
 * Tags of the CameraInfoG5XII maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#CameraInfoG5XII
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCameraInfoG5XIITag {

    public val SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x293, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val SHUTTER_COUNT_2: TagInfoLong = TagInfoLong(
        0xa95, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val DIRECTORY_INDEX: TagInfoLong = TagInfoLong(
        0xb21, "DirectoryIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val FILE_INDEX: TagInfoLong = TagInfoLong(
        0xb2d, "FileIndex",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        SHUTTER_COUNT, SHUTTER_COUNT_2, DIRECTORY_INDEX, FILE_INDEX
    )
}
