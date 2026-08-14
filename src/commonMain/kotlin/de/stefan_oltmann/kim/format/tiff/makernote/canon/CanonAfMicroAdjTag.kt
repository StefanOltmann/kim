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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRational

/**
 * Tags of the AFMicroAdj maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#AFMicroAdj
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonAfMicroAdjTag {

    public val AF_MICRO_ADJ_MODE: TagInfoSLong = TagInfoSLong(
        0x1, "AFMicroAdjMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_MICRO_ADJ
    )

    public val AF_MICRO_ADJ_VALUE: TagInfoSRational = TagInfoSRational(
        0x2, "AFMicroAdjValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_MICRO_ADJ
    )

    public val ALL: List<TagInfo> = listOf(
        AF_MICRO_ADJ_MODE, AF_MICRO_ADJ_VALUE
    )
}
