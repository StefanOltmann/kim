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

/**
 * Tags of the VignettingCorr2 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#VignettingCorr2
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonVignettingCorr2Tag {

    public val PERIPHERAL_LIGHTING_SETTING: TagInfoSLong = TagInfoSLong(
        0x5, "PeripheralLightingSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR2
    )

    public val CHROMATIC_ABERRATION_SETTING: TagInfoSLong = TagInfoSLong(
        0x6, "ChromaticAberrationSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR2
    )

    public val DISTORTION_CORRECTION_SETTING: TagInfoSLong = TagInfoSLong(
        0x7, "DistortionCorrectionSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR2
    )

    public val DIGITAL_LENS_OPTIMIZER_SETTING: TagInfoSLong = TagInfoSLong(
        0x9, "DigitalLensOptimizerSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR2
    )

    public val ALL: List<TagInfo> = listOf(
        PERIPHERAL_LIGHTING_SETTING,
        CHROMATIC_ABERRATION_SETTING,
        DISTORTION_CORRECTION_SETTING,
        DIGITAL_LENS_OPTIMIZER_SETTING
    )
}
