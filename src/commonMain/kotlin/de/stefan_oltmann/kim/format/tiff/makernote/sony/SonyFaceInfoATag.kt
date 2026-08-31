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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the FaceInfoA maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#FaceInfoA
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object SonyFaceInfoATag {

    public val FACE_TEST2: TagInfoByte = TagInfoByte(
        0x2, "FaceTest2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val FACES_DETECTED: TagInfoByte = TagInfoByte(
        0x3, "FacesDetected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val FACE_TEST8: TagInfoByte = TagInfoByte(
        0x8, "FaceTest8",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE1_POSITION: TagInfoByte = TagInfoByte(
        0xb, "PotentialFace1Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE2_POSITION: TagInfoByte = TagInfoByte(
        0x15, "PotentialFace2Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE3_POSITION: TagInfoByte = TagInfoByte(
        0x1f, "PotentialFace3Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE4_POSITION: TagInfoByte = TagInfoByte(
        0x29, "PotentialFace4Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE5_POSITION: TagInfoByte = TagInfoByte(
        0x33, "PotentialFace5Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE6_POSITION: TagInfoByte = TagInfoByte(
        0x3d, "PotentialFace6Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE7_POSITION: TagInfoByte = TagInfoByte(
        0x47, "PotentialFace7Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val POTENTIAL_FACE8_POSITION: TagInfoByte = TagInfoByte(
        0x51, "PotentialFace8Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val FACE1_POSITION: TagInfoByte = TagInfoByte(
        0x5b, "Face1Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val FACE2_POSITION: TagInfoByte = TagInfoByte(
        0x65, "Face2Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val FACE3_POSITION: TagInfoByte = TagInfoByte(
        0x6f, "Face3Position",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        FACE_TEST2,
        FACES_DETECTED,
        FACE_TEST8,
        POTENTIAL_FACE1_POSITION,
        POTENTIAL_FACE2_POSITION,
        POTENTIAL_FACE3_POSITION,
        POTENTIAL_FACE4_POSITION,
        POTENTIAL_FACE5_POSITION,
        POTENTIAL_FACE6_POSITION,
        POTENTIAL_FACE7_POSITION,
        POTENTIAL_FACE8_POSITION,
        FACE1_POSITION,
        FACE2_POSITION,
        FACE3_POSITION
    )
}
