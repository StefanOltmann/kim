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
 * Tags of the LightingOpt maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#LightingOpt
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonLightingOptTag {

    public val PERIPHERAL_ILLUMINATION_CORR: TagInfoSLong = TagInfoSLong(
        0x1, "PeripheralIlluminationCorr",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val AUTO_LIGHTING_OPTIMIZER: TagInfoSLong = TagInfoSLong(
        0x2, "AutoLightingOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val HIGHLIGHT_TONE_PRIORITY: TagInfoSLong = TagInfoSLong(
        0x3, "HighlightTonePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoSLong = TagInfoSLong(
        0x4, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val HIGH_ISO_NOISE_REDUCTION: TagInfoSLong = TagInfoSLong(
        0x5, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val DIGITAL_LENS_OPTIMIZER: TagInfoSLong = TagInfoSLong(
        0xa, "DigitalLensOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val DUAL_PIXEL_RAW: TagInfoSLong = TagInfoSLong(
        0xb, "DualPixelRaw",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT
    )

    public val ALL: List<TagInfo> = listOf(
        PERIPHERAL_ILLUMINATION_CORR,
        AUTO_LIGHTING_OPTIMIZER,
        HIGHLIGHT_TONE_PRIORITY,
        LONG_EXPOSURE_NOISE_REDUCTION,
        HIGH_ISO_NOISE_REDUCTION,
        DIGITAL_LENS_OPTIMIZER,
        DUAL_PIXEL_RAW
    )
}
