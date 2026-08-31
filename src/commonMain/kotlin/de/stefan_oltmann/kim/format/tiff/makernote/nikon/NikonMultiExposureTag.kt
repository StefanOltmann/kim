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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the MultiExposure maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#MultiExposure
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonMultiExposureTag {

    public val MULTI_EXPOSURE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "MultiExposureVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_MULTI_EXPOSURE
    )

    public val MULTI_EXPOSURE_MODE: TagInfoLong = TagInfoLong(
        0x1, "MultiExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_MULTI_EXPOSURE
    )

    public val MULTI_EXPOSURE_SHOTS: TagInfoLong = TagInfoLong(
        0x2, "MultiExposureShots",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_MULTI_EXPOSURE
    )

    public val MULTI_EXPOSURE_AUTO_GAIN: TagInfoLong = TagInfoLong(
        0x3, "MultiExposureAutoGain",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_MULTI_EXPOSURE
    )

    public val ALL: List<TagInfo> = listOf(
        MULTI_EXPOSURE_VERSION, MULTI_EXPOSURE_MODE, MULTI_EXPOSURE_SHOTS, MULTI_EXPOSURE_AUTO_GAIN
    )
}
