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
 * Tags of the AspectInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#AspectInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonAspectInfoTag {

    public val ASPECT_RATIO: TagInfoLong = TagInfoLong(
        0x0, "AspectRatio",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO
    )

    public val CROPPED_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0x1, "CroppedImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO
    )

    public val CROPPED_IMAGE_HEIGHT: TagInfoLong = TagInfoLong(
        0x2, "CroppedImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO
    )

    public val CROPPED_IMAGE_LEFT: TagInfoLong = TagInfoLong(
        0x3, "CroppedImageLeft",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO
    )

    public val CROPPED_IMAGE_TOP: TagInfoLong = TagInfoLong(
        0x4, "CroppedImageTop",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        ASPECT_RATIO, CROPPED_IMAGE_WIDTH, CROPPED_IMAGE_HEIGHT, CROPPED_IMAGE_LEFT, CROPPED_IMAGE_TOP
    )
}
