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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the LensData0204 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#LensData0204
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object NikonLensData0204Tag {

    public val LENS_DATA_VERSION: TagInfoAscii = TagInfoAscii(
        0x0, "LensDataVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val EXIT_PUPIL_POSITION: TagInfoByte = TagInfoByte(
        0x4, "ExitPupilPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val AF_APERTURE: TagInfoByte = TagInfoByte(
        0x5, "AFAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val FOCUS_POSITION: TagInfoByte = TagInfoByte(
        0x8, "FocusPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val FOCUS_DISTANCE: TagInfoByte = TagInfoByte(
        0xa, "FocusDistance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0xb, "FocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val LENS_ID_NUMBER: TagInfoByte = TagInfoByte(
        0xc, "LensIDNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val LENS_F_STOPS: TagInfoByte = TagInfoByte(
        0xd, "LensFStops",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val MIN_FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0xe, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val MAX_FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0xf, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val MAX_APERTURE_AT_MIN_FOCAL: TagInfoByte = TagInfoByte(
        0x10, "MaxApertureAtMinFocal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val MAX_APERTURE_AT_MAX_FOCAL: TagInfoByte = TagInfoByte(
        0x11, "MaxApertureAtMaxFocal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val MCU_VERSION: TagInfoByte = TagInfoByte(
        0x12, "MCUVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val EFFECTIVE_MAX_APERTURE: TagInfoByte = TagInfoByte(
        0x13, "EffectiveMaxAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA
    )

    public val ALL: List<TagInfo> = listOf(
        LENS_DATA_VERSION,
        EXIT_PUPIL_POSITION,
        AF_APERTURE,
        FOCUS_POSITION,
        FOCUS_DISTANCE,
        FOCAL_LENGTH,
        LENS_ID_NUMBER,
        LENS_F_STOPS,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        MAX_APERTURE_AT_MIN_FOCAL,
        MAX_APERTURE_AT_MAX_FOCAL,
        MCU_VERSION,
        EFFECTIVE_MAX_APERTURE
    )
}
