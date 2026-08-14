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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the VignettingCorr maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#VignettingCorr
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonVignettingCorrTag {

    public val VIGNETTING_CORR_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0, "VignettingCorrVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val PERIPHERAL_LIGHTING: TagInfoSShort = TagInfoSShort(
        0x2, "PeripheralLighting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val DISTORTION_CORRECTION: TagInfoSShort = TagInfoSShort(
        0x3, "DistortionCorrection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val CHROMATIC_ABERRATION_CORR: TagInfoSShort = TagInfoSShort(
        0x4, "ChromaticAberrationCorr",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val CHROMATIC_ABERRATION_CORR_2: TagInfoSShort = TagInfoSShort(
        0x5, "ChromaticAberrationCorr",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val PERIPHERAL_LIGHTING_VALUE: TagInfoSShort = TagInfoSShort(
        0x6, "PeripheralLightingValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val DISTORTION_CORRECTION_VALUE: TagInfoSShort = TagInfoSShort(
        0x9, "DistortionCorrectionValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val ORIGINAL_IMAGE_WIDTH: TagInfoSShort = TagInfoSShort(
        0xb, "OriginalImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val ORIGINAL_IMAGE_HEIGHT: TagInfoSShort = TagInfoSShort(
        0xc, "OriginalImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR
    )

    public val ALL: List<TagInfo> = listOf(
        VIGNETTING_CORR_VERSION,
        PERIPHERAL_LIGHTING,
        DISTORTION_CORRECTION,
        CHROMATIC_ABERRATION_CORR,
        CHROMATIC_ABERRATION_CORR_2,
        PERIPHERAL_LIGHTING_VALUE,
        DISTORTION_CORRECTION_VALUE,
        ORIGINAL_IMAGE_WIDTH,
        ORIGINAL_IMAGE_HEIGHT
    )
}
