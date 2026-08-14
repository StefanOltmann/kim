/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Fujifilm MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object FujiFilmTag {

    /**
     * The version of the FujiFilm maker note format.
     */
    public val VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0000, "Version", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * Unique for most models, and contains the camera model ID and the date of manufacture.
     */
    public val INTERNAL_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0x0010, "InternalSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X0020: TagInfoAscii = TagInfoAscii(
        0x0020, "FujiFilm_0x0020", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The image quality setting.
     */
    public val QUALITY: TagInfoAscii = TagInfoAscii(
        0x1000, "Quality", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmSharpness].
     */
    public val SHARPNESS: TagInfoShort = TagInfoShort(
        0x1001, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmWhiteBalance].
     */
    public val WHITE_BALANCE: TagInfoShort = TagInfoShort(
        0x1002, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmSaturation].
     */
    public val SATURATION: TagInfoShort = TagInfoShort(
        0x1003, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmContrast].
     */
    public val CONTRAST: TagInfoShort = TagInfoShort(
        0x1004, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The color temperature setting.
     */
    public val COLOR_TEMPERATURE: TagInfoShort = TagInfoShort(
        0x1005, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmContrast].
     */
    public val CONTRAST_2: TagInfoShort = TagInfoShort(
        0x1006, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The white balance fine-tune value.
     */
    public val WHITE_BALANCE_FINE_TUNE: TagInfoSLongs = TagInfoSLongs(
        0x100a, "WhiteBalanceFineTune", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The noise reduction setting.
     */
    public val NOISE_REDUCTION: TagInfoShort = TagInfoShort(
        0x100b, "NoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The noise reduction setting.
     */
    public val NOISE_REDUCTION_2: TagInfoShort = TagInfoShort(
        0x100e, "NoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The clarity setting of the shot.
     */
    public val CLARITY: TagInfoSLong = TagInfoSLong(
        0x100f, "Clarity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmFlashMode].
     */
    public val FUJI_FLASH_MODE: TagInfoShort = TagInfoShort(
        0x1010, "FujiFlashMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FLASH_EXPOSURE_COMP: TagInfoSRationals = TagInfoSRationals(
        0x1011, "FlashExposureComp", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmMacro].
     */
    public val MACRO: TagInfoShort = TagInfoShort(
        0x1020, "Macro",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmFocusMode].
     */
    public val FOCUS_MODE: TagInfoShort = TagInfoShort(
        0x1021, "FocusMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmAfMode].
     */
    public val AF_MODE: TagInfoShort = TagInfoShort(
        0x1022, "AFMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FOCUS_PIXEL: TagInfoShorts = TagInfoShorts(
        0x1023, "FocusPixel", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1025: TagInfoLong = TagInfoLong(
        0x1025, "FujiFilm_0x1025",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1026: TagInfoShort = TagInfoShort(
        0x1026, "FujiFilm_0x1026",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val PRIORITY_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x102b, "PrioritySettings", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X102C: TagInfoLong = TagInfoLong(
        0x102c, "FujiFilm_0x102c",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FOCUS_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x102d, "FocusSettings", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val AFC_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x102e, "AFCSettings", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmSlowSync].
     */
    public val SLOW_SYNC: TagInfoShort = TagInfoShort(
        0x1030, "SlowSync",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmPictureMode].
     */
    public val PICTURE_MODE: TagInfoShort = TagInfoShort(
        0x1031, "PictureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The number of exposures combined for this image.
     */
    public val EXPOSURE_COUNT: TagInfoShort = TagInfoShort(
        0x1032, "ExposureCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmExrAuto].
     */
    public val EXR_AUTO: TagInfoShort = TagInfoShort(
        0x1033, "EXRAuto",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmExrMode].
     */
    public val EXR_MODE: TagInfoShort = TagInfoShort(
        0x1034, "EXRMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmMultipleExposure].
     */
    public val MULTIPLE_EXPOSURE: TagInfoShort = TagInfoShort(
        0x1037, "MultipleExposure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmShadowTone].
     */
    public val SHADOW_TONE: TagInfoSLong = TagInfoSLong(
        0x1040, "ShadowTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmHighlightTone].
     */
    public val HIGHLIGHT_TONE: TagInfoSLong = TagInfoSLong(
        0x1041, "HighlightTone",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The digital zoom ratio used.
     */
    public val DIGITAL_ZOOM: TagInfoLong = TagInfoLong(
        0x1044, "DigitalZoom",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmLensModulationOptimizer].
     */
    public val LENS_MODULATION_OPTIMIZER: TagInfoLong = TagInfoLong(
        0x1045, "LensModulationOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1046: TagInfoLong = TagInfoLong(
        0x1046, "FujiFilm_0x1046",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmGrainEffectRoughness].
     */
    public val GRAIN_EFFECT_ROUGHNESS: TagInfoSLong = TagInfoSLong(
        0x1047, "GrainEffectRoughness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmColorChromeEffect].
     */
    public val COLOR_CHROME_EFFECT: TagInfoSLong = TagInfoSLong(
        0x1048, "ColorChromeEffect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val BW_ADJUSTMENT: TagInfoSByte = TagInfoSByte(
        0x1049, "BWAdjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val BW_MAGENTA_GREEN: TagInfoSByte = TagInfoSByte(
        0x104b, "BWMagentaGreen",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmGrainEffectSize].
     */
    public val GRAIN_EFFECT_SIZE: TagInfoShort = TagInfoShort(
        0x104c, "GrainEffectSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmCropMode].
     */
    public val CROP_MODE: TagInfoShort = TagInfoShort(
        0x104d, "CropMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmColorChromeFxBlue].
     */
    public val COLOR_CHROME_FX_BLUE: TagInfoSLong = TagInfoSLong(
        0x104e, "ColorChromeFXBlue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmShutterType].
     */
    public val SHUTTER_TYPE: TagInfoShort = TagInfoShort(
        0x1050, "ShutterType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val CROP_FLAG: TagInfoByte = TagInfoByte(
        0x1051, "CropFlag",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val CROP_TOP_LEFT: TagInfoLong = TagInfoLong(
        0x1052, "CropTopLeft",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val CROP_SIZE: TagInfoLong = TagInfoLong(
        0x1053, "CropSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmAutoBracketing].
     *
     * X-T3 only: 2 = Pre-shot; other models: 2 = No flash & flash, 6 = Pixel Shift.
     */
    public val AUTO_BRACKETING: TagInfoShort = TagInfoShort(
        0x1100, "AutoBracketing",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The sequence number of the shot.
     */
    public val SEQUENCE_NUMBER: TagInfoShort = TagInfoShort(
        0x1101, "SequenceNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmWhiteBalanceBracketing].
     */
    public val WHITE_BALANCE_BRACKETING: TagInfoShort = TagInfoShort(
        0x1102, "WhiteBalanceBracketing",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val DRIVE_SETTINGS: TagInfoUndefineds = TagInfoUndefineds(
        0x1103, "DriveSettings", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The number of pixel-shift exposures combined.
     */
    public val PIXEL_SHIFT_SHOTS: TagInfoShort = TagInfoShort(
        0x1105, "PixelShiftShots",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val PIXEL_SHIFT_OFFSET: TagInfoSRationals = TagInfoSRationals(
        0x1106, "PixelShiftOffset", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmCompositeImageMode].
     */
    public val COMPOSITE_IMAGE_MODE: TagInfoLong = TagInfoLong(
        0x1150, "CompositeImageMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val COMPOSITE_IMAGE_COUNT_1: TagInfoShort = TagInfoShort(
        0x1151, "CompositeImageCount1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val COMPOSITE_IMAGE_COUNT_2: TagInfoShort = TagInfoShort(
        0x1152, "CompositeImageCount2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The angle covered by a panorama shot.
     */
    public val PANORAMA_ANGLE: TagInfoShort = TagInfoShort(
        0x1153, "PanoramaAngle",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmPanoramaDirection].
     */
    public val PANORAMA_DIRECTION: TagInfoShort = TagInfoShort(
        0x1154, "PanoramaDirection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1200: TagInfoShort = TagInfoShort(
        0x1200, "FujiFilm_0x1200",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmAdvancedFilter].
     */
    public val ADVANCED_FILTER: TagInfoLong = TagInfoLong(
        0x1201, "AdvancedFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmColorMode].
     */
    public val COLOR_MODE: TagInfoShort = TagInfoShort(
        0x1210, "ColorMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmBlurWarning].
     */
    public val BLUR_WARNING: TagInfoShort = TagInfoShort(
        0x1300, "BlurWarning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmFocusWarning].
     */
    public val FOCUS_WARNING: TagInfoShort = TagInfoShort(
        0x1301, "FocusWarning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmExposureWarning].
     */
    public val EXPOSURE_WARNING: TagInfoShort = TagInfoShort(
        0x1302, "ExposureWarning",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1303: TagInfoShort = TagInfoShort(
        0x1303, "FujiFilm_0x1303",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val GE_IMAGE_SIZE: TagInfoAscii = TagInfoAscii(
        0x1304, "GEImageSize", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1305: TagInfoShort = TagInfoShort(
        0x1305, "FujiFilm_0x1305",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmDynamicRange].
     */
    public val DYNAMIC_RANGE: TagInfoShort = TagInfoShort(
        0x1400, "DynamicRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * Film Simulation / Film Mode
     *
     * See https://exiftool.sourceforge.net/TagNames/FujiFilm.html#FilmMode
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

    /**
     * See [FujiFilmDynamicRangeSetting].
     */
    public val DYNAMIC_RANGE_SETTING: TagInfoShort = TagInfoShort(
        0x1402, "DynamicRangeSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The dynamic range used during development.
     */
    public val DEVELOPMENT_DYNAMIC_RANGE: TagInfoShort = TagInfoShort(
        0x1403, "DevelopmentDynamicRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The minimum focal length of the lens.
     */
    public val MIN_FOCAL_LENGTH: TagInfoRationals = TagInfoRationals(
        0x1404, "MinFocalLength", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The maximum focal length of the lens.
     */
    public val MAX_FOCAL_LENGTH: TagInfoRationals = TagInfoRationals(
        0x1405, "MaxFocalLength", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The maximum aperture at the minimum focal length.
     */
    public val MAX_APERTURE_AT_MIN_FOCAL: TagInfoRationals = TagInfoRationals(
        0x1406, "MaxApertureAtMinFocal", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The maximum aperture at the maximum focal length.
     */
    public val MAX_APERTURE_AT_MAX_FOCAL: TagInfoRationals = TagInfoRationals(
        0x1407, "MaxApertureAtMaxFocal", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1408: TagInfoUndefineds = TagInfoUndefineds(
        0x1408, "FujiFilm_0x1408", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1409: TagInfoUndefineds = TagInfoUndefineds(
        0x1409, "FujiFilm_0x1409", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X140A: TagInfoShort = TagInfoShort(
        0x140a, "FujiFilm_0x140a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * Whether auto dynamic range was used.
     */
    public val AUTO_DYNAMIC_RANGE: TagInfoShort = TagInfoShort(
        0x140b, "AutoDynamicRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The image stabilization setting.
     */
    public val IMAGE_STABILIZATION: TagInfoShorts = TagInfoShorts(
        0x1422, "ImageStabilization", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1424: TagInfoShort = TagInfoShort(
        0x1424, "FujiFilm_0x1424",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmSceneRecognition].
     */
    public val SCENE_RECOGNITION: TagInfoShort = TagInfoShort(
        0x1425, "SceneRecognition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1430: TagInfoUndefineds = TagInfoUndefineds(
        0x1430, "FujiFilm_0x1430", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val RATING: TagInfoLong = TagInfoLong(
        0x1431, "Rating",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmImageGeneration].
     */
    public val IMAGE_GENERATION: TagInfoShort = TagInfoShort(
        0x1436, "ImageGeneration",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * May reset to 0 when new firmware is installed.
     */
    public val IMAGE_COUNT: TagInfoShort = TagInfoShort(
        0x1438, "ImageCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1439: TagInfoAscii = TagInfoAscii(
        0x1439, "FujiFilm_0x1439", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X1442: TagInfoLong = TagInfoLong(
        0x1442, "FujiFilm_0x1442",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmDRangePriority].
     */
    public val DRANGE_PRIORITY: TagInfoShort = TagInfoShort(
        0x1443, "DRangePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmDRangePriorityAuto].
     */
    public val DRANGE_PRIORITY_AUTO: TagInfoShort = TagInfoShort(
        0x1444, "DRangePriorityAuto",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmDRangePriorityFixed].
     */
    public val DRANGE_PRIORITY_FIXED: TagInfoShort = TagInfoShort(
        0x1445, "DRangePriorityFixed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FLICKER_REDUCTION: TagInfoLong = TagInfoLong(
        0x1446, "FlickerReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_MODEL: TagInfoAscii = TagInfoAscii(
        0x1447, "FujiModel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_MODEL_2: TagInfoAscii = TagInfoAscii(
        0x1448, "FujiModel2", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val WB_RED: TagInfoShort = TagInfoShort(
        0x144a, "WBRed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val WB_GREEN: TagInfoShort = TagInfoShort(
        0x144b, "WBGreen",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val WB_BLUE: TagInfoShort = TagInfoShort(
        0x144c, "WBBlue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val ROLL_ANGLE: TagInfoSRationals = TagInfoSRationals(
        0x144d, "RollAngle", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmVideoRecordingMode].
     */
    public val VIDEO_RECORDING_MODE: TagInfoLong = TagInfoLong(
        0x3803, "VideoRecordingMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmPeripheralLighting].
     */
    public val PERIPHERAL_LIGHTING: TagInfoShort = TagInfoShort(
        0x3804, "PeripheralLighting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X3805: TagInfoShort = TagInfoShort(
        0x3805, "FujiFilm_0x3805",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmVideoCompression].
     */
    public val VIDEO_COMPRESSION: TagInfoShort = TagInfoShort(
        0x3806, "VideoCompression",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X3810: TagInfoLong = TagInfoLong(
        0x3810, "FujiFilm_0x3810",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FRAME_RATE: TagInfoShort = TagInfoShort(
        0x3820, "FrameRate",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FRAME_WIDTH: TagInfoShort = TagInfoShort(
        0x3821, "FrameWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FRAME_HEIGHT: TagInfoShort = TagInfoShort(
        0x3822, "FrameHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val FUJIFILM_0X3823: TagInfoLong = TagInfoLong(
        0x3823, "FujiFilm_0x3823",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmFullHdHighSpeedRec].
     */
    public val FULL_HD_HIGH_SPEED_REC: TagInfoLong = TagInfoLong(
        0x3824, "FullHDHighSpeedRec",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The face element selected by the camera.
     */
    public val FACE_ELEMENT_SELECTED: TagInfoShorts = TagInfoShorts(
        0x4005, "FaceElementSelected", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The number of faces detected.
     */
    public val FACES_DETECTED: TagInfoShort = TagInfoShort(
        0x4100, "FacesDetected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The coordinates of each detected face within the full image.
     */
    public val FACE_POSITIONS: TagInfoShorts = TagInfoShorts(
        0x4103, "FacePositions", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val NUM_FACE_ELEMENTS: TagInfoShort = TagInfoShort(
        0x4200, "NumFaceElements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * See [FujiFilmFaceElementType].
     */
    public val FACE_ELEMENT_TYPES: TagInfoBytes = TagInfoBytes(
        0x4201, "FaceElementTypes", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The coordinates of each face element within the full image.
     */
    public val FACE_ELEMENT_POSITIONS: TagInfoShorts = TagInfoShorts(
        0x4203, "FaceElementPositions", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * Information about face recognition.
     */
    public val FACE_REC_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x4282, "FaceRecInfo", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The source of the image file.
     */
    public val FILE_SOURCE: TagInfoAscii = TagInfoAscii(
        0x8000, "FileSource", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The order number of the image.
     */
    public val ORDER_NUMBER: TagInfoLong = TagInfoLong(
        0x8002, "OrderNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    /**
     * The frame number of the shot.
     */
    public val FRAME_NUMBER: TagInfoShort = TagInfoShort(
        0x8003, "FrameNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val PARALLAX: TagInfoSRationals = TagInfoSRationals(
        0xb211, "Parallax", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM
    )

    public val ALL: List<TagInfo> = listOf(
        VERSION, INTERNAL_SERIAL_NUMBER, FUJIFILM_0X0020,
        QUALITY, SHARPNESS, WHITE_BALANCE, SATURATION, CONTRAST,
        COLOR_TEMPERATURE, CONTRAST_2, WHITE_BALANCE_FINE_TUNE,
        NOISE_REDUCTION, NOISE_REDUCTION_2, CLARITY, FUJI_FLASH_MODE,
        FLASH_EXPOSURE_COMP, MACRO, FOCUS_MODE, AF_MODE, FOCUS_PIXEL,
        FUJIFILM_0X1025, FUJIFILM_0X1026, PRIORITY_SETTINGS,
        FUJIFILM_0X102C, FOCUS_SETTINGS, AFC_SETTINGS, SLOW_SYNC,
        PICTURE_MODE, EXPOSURE_COUNT, EXR_AUTO, EXR_MODE,
        MULTIPLE_EXPOSURE, SHADOW_TONE, HIGHLIGHT_TONE, DIGITAL_ZOOM,
        LENS_MODULATION_OPTIMIZER, FUJIFILM_0X1046,
        GRAIN_EFFECT_ROUGHNESS, COLOR_CHROME_EFFECT, BW_ADJUSTMENT,
        BW_MAGENTA_GREEN, GRAIN_EFFECT_SIZE, CROP_MODE,
        COLOR_CHROME_FX_BLUE, SHUTTER_TYPE, CROP_FLAG, CROP_TOP_LEFT,
        CROP_SIZE,
        AUTO_BRACKETING, SEQUENCE_NUMBER, WHITE_BALANCE_BRACKETING,
        DRIVE_SETTINGS, PIXEL_SHIFT_SHOTS, PIXEL_SHIFT_OFFSET,
        COMPOSITE_IMAGE_MODE, COMPOSITE_IMAGE_COUNT_1,
        COMPOSITE_IMAGE_COUNT_2, PANORAMA_ANGLE, PANORAMA_DIRECTION,
        FUJIFILM_0X1200, ADVANCED_FILTER, COLOR_MODE,
        BLUR_WARNING, FOCUS_WARNING, EXPOSURE_WARNING,
        FUJIFILM_0X1303, GE_IMAGE_SIZE, FUJIFILM_0X1305,
        DYNAMIC_RANGE, FILM_MODE, DYNAMIC_RANGE_SETTING,
        DEVELOPMENT_DYNAMIC_RANGE, MIN_FOCAL_LENGTH, MAX_FOCAL_LENGTH,
        MAX_APERTURE_AT_MIN_FOCAL, MAX_APERTURE_AT_MAX_FOCAL,
        FUJIFILM_0X1408, FUJIFILM_0X1409, FUJIFILM_0X140A,
        AUTO_DYNAMIC_RANGE, IMAGE_STABILIZATION, FUJIFILM_0X1424,
        SCENE_RECOGNITION, FUJIFILM_0X1430, RATING, IMAGE_GENERATION,
        IMAGE_COUNT, FUJIFILM_0X1439, FUJIFILM_0X1442,
        DRANGE_PRIORITY, DRANGE_PRIORITY_AUTO, DRANGE_PRIORITY_FIXED,
        FLICKER_REDUCTION, FUJIFILM_MODEL, FUJIFILM_MODEL_2,
        WB_RED, WB_GREEN, WB_BLUE, ROLL_ANGLE,
        VIDEO_RECORDING_MODE, PERIPHERAL_LIGHTING, FUJIFILM_0X3805,
        VIDEO_COMPRESSION, FUJIFILM_0X3810, FRAME_RATE, FRAME_WIDTH,
        FRAME_HEIGHT, FUJIFILM_0X3823, FULL_HD_HIGH_SPEED_REC,
        FACE_ELEMENT_SELECTED, FACES_DETECTED, FACE_POSITIONS,
        NUM_FACE_ELEMENTS, FACE_ELEMENT_TYPES, FACE_ELEMENT_POSITIONS,
        FACE_REC_INFO,
        FILE_SOURCE, ORDER_NUMBER, FRAME_NUMBER, PARALLAX
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
