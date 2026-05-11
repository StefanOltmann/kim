/*
 * Copyright 2026 Gnod
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
package de.stefan_oltmann.kim.format.tiff.constant

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Fujifilm MakerNote Tags
 *
 * See https://exiftool.org/TagNames/FujiFilm.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object FujiFilmTag {

    /*
     * TODO This list is incomplete
     */

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0000, "MakerNoteVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val INTERNAL_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0010, "InternalSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val QUALITY: TagInfoAscii = TagInfoAscii(
        0x1000, "Quality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val SHARPNESS: TagInfoShort = TagInfoShort(
        0x1001, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0x1002, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val SATURATION: TagInfoShort = TagInfoShort(
        0x1003, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val CONTRAST: TagInfoShort = TagInfoShort(
        0x1004, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * Film Simulation / Film Mode
     *
     * See https://exiftool.org/TagNames/FujiFilm.html#FilmMode
     */
    public val FILM_MODE: TagInfoShort = TagInfoShort(
        0x1401, "FilmMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public const val FILM_MODE_PROVIA_STANDARD: Int = 0x000
    public const val FILM_MODE_STUDIO_PORTRAIT: Int = 0x100
    public const val FILM_MODE_ASTIA_SOFT: Int = 0x120
    public const val FILM_MODE_VELVIA_VIVID: Int = 0x200
    public const val FILM_MODE_VELVIA: Int = 0x400
    public const val FILM_MODE_PRO_NEG_STD: Int = 0x500
    public const val FILM_MODE_PRO_NEG_HI: Int = 0x501
    public const val FILM_MODE_CLASSIC_CHROME: Int = 0x600
    public const val FILM_MODE_ETERNA: Int = 0x700
    public const val FILM_MODE_CLASSIC_NEG: Int = 0x800
    public const val FILM_MODE_BLEACH_BYPASS: Int = 0x900
    public const val FILM_MODE_NOSTALGIC_NEG: Int = 0xA00
    public const val FILM_MODE_REALA_ACE: Int = 0xB00

    public val DYNAMIC_RANGE: TagInfoShort = TagInfoShort(
        0x1400, "DynamicRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val DYNAMIC_RANGE_SETTING: TagInfoShort = TagInfoShort(
        0x1402, "DynamicRangeSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val ALL: List<TagInfo> = listOf(
        MAKER_NOTE_VERSION, INTERNAL_SERIAL_NUMBER, QUALITY,
        SHARPNESS, WHITE_BALANCE, SATURATION, CONTRAST,
        FILM_MODE, DYNAMIC_RANGE, DYNAMIC_RANGE_SETTING
    )

    /**
     * Returns the display name for a film mode value.
     */
    public fun getFilmModeName(value: Int): String? =
        when (value) {
            FILM_MODE_PROVIA_STANDARD -> "Provia/Standard"
            FILM_MODE_STUDIO_PORTRAIT -> "Studio Portrait"
            FILM_MODE_ASTIA_SOFT -> "Astia/Soft"
            FILM_MODE_VELVIA_VIVID -> "Velvia/Vivid"
            FILM_MODE_VELVIA -> "Velvia"
            FILM_MODE_PRO_NEG_STD -> "Pro Neg. Std"
            FILM_MODE_PRO_NEG_HI -> "Pro Neg. Hi"
            FILM_MODE_CLASSIC_CHROME -> "Classic Chrome"
            FILM_MODE_ETERNA -> "Eterna"
            FILM_MODE_CLASSIC_NEG -> "Classic Negative"
            FILM_MODE_BLEACH_BYPASS -> "Bleach Bypass"
            FILM_MODE_NOSTALGIC_NEG -> "Nostalgic Neg"
            FILM_MODE_REALA_ACE -> "Reala ACE"
            else -> null
        }
}
