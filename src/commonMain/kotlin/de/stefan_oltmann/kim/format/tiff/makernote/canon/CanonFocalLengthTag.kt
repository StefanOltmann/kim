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
 * Tags of the FocalLength maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FocalLength
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonFocalLengthTag {

    public val FOCAL_TYPE: TagInfoShort = TagInfoShort(
        0x0, "FocalType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FOCAL_LENGTH
    )

    public val FOCAL_LENGTH: TagInfoShort = TagInfoShort(
        0x1, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FOCAL_LENGTH
    )

    public val FOCAL_PLANE_X_UNKNOWN: TagInfoShort = TagInfoShort(
        0x2, "FocalPlaneXUnknown",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FOCAL_LENGTH
    )

    public val FOCAL_PLANE_Y_UNKNOWN: TagInfoShort = TagInfoShort(
        0x3, "FocalPlaneYUnknown",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_FOCAL_LENGTH
    )

    public val ALL: List<TagInfo> = listOf(
        FOCAL_TYPE, FOCAL_LENGTH, FOCAL_PLANE_X_UNKNOWN, FOCAL_PLANE_Y_UNKNOWN
    )
}
