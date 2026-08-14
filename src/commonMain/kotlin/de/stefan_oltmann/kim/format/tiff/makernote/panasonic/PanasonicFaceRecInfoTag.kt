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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts

/**
 * Tags of the FaceRecInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FaceRecInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object PanasonicFaceRecInfoTag {

    public val FACES_RECOGNIZED: TagInfoShort = TagInfoShort(
        0x0, "FacesRecognized",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE1_NAME: TagInfoAscii = TagInfoAscii(
        0x4, "RecognizedFace1Name", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE1_POSITION: TagInfoShorts = TagInfoShorts(
        0x18, "RecognizedFace1Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE1_AGE: TagInfoAscii = TagInfoAscii(
        0x20, "RecognizedFace1Age", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE2_NAME: TagInfoAscii = TagInfoAscii(
        0x34, "RecognizedFace2Name", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE2_POSITION: TagInfoShorts = TagInfoShorts(
        0x48, "RecognizedFace2Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE2_AGE: TagInfoAscii = TagInfoAscii(
        0x50, "RecognizedFace2Age", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE3_NAME: TagInfoAscii = TagInfoAscii(
        0x64, "RecognizedFace3Name", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE3_POSITION: TagInfoShorts = TagInfoShorts(
        0x78, "RecognizedFace3Position", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val RECOGNIZED_FACE3_AGE: TagInfoAscii = TagInfoAscii(
        0x80, "RecognizedFace3Age", 20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        FACES_RECOGNIZED,
        RECOGNIZED_FACE1_NAME,
        RECOGNIZED_FACE1_POSITION,
        RECOGNIZED_FACE1_AGE,
        RECOGNIZED_FACE2_NAME,
        RECOGNIZED_FACE2_POSITION,
        RECOGNIZED_FACE2_AGE,
        RECOGNIZED_FACE3_NAME,
        RECOGNIZED_FACE3_POSITION,
        RECOGNIZED_FACE3_AGE
    )
}
