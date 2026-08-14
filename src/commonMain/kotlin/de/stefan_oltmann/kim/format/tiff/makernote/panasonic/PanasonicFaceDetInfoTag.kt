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
package de.stefan_oltmann.kim.format.tiff.makernote.panasonic

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts

/**
 * Tags of the FaceDetInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FaceDetInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object PanasonicFaceDetInfoTag {

    public val NUM_FACE_POSITIONS: TagInfoShort = TagInfoShort(
        0x0, "NumFacePositions",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val FACE1_POSITION: TagInfoShorts = TagInfoShorts(
        0x1, "Face1Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val FACE2_POSITION: TagInfoShorts = TagInfoShorts(
        0x5, "Face2Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val FACE3_POSITION: TagInfoShorts = TagInfoShorts(
        0x9, "Face3Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val FACE4_POSITION: TagInfoShorts = TagInfoShorts(
        0xd, "Face4Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val FACE5_POSITION: TagInfoShorts = TagInfoShorts(
        0x11, "Face5Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        NUM_FACE_POSITIONS, FACE1_POSITION, FACE2_POSITION, FACE3_POSITION, FACE4_POSITION, FACE5_POSITION
    )
}
