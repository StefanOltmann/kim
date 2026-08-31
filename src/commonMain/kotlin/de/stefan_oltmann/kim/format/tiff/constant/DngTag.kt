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
package de.stefan_oltmann.kim.format.tiff.constant

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType.TIFF_DIRECTORY_IFD0
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoDoubles
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloats
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * The tags defined by the Adobe DNG specification.
 *
 * See https://helpx.adobe.com/camera-raw/digital-negative.html
 */
@Suppress("MagicNumber", "MaxLineLength")
public object DngTag {


    public val TIFF_TAG_DNG_DNG_BACKWARD_VERSION: TagInfoBytes = TagInfoBytes(
        0xc613, "DNGBackwardVersion", 4,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_UNIQUE_CAMERA_MODEL: TagInfoAscii = TagInfoAscii(
        0xc614, "UniqueCameraModel", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_LOCALIZED_CAMERA_MODEL: TagInfoAscii = TagInfoAscii(
        0xc615, "LocalizedCameraModel", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CFA_PLANE_COLOR: TagInfoByte = TagInfoByte(
        0xc616, "CFAPlaneColor",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_CFA_LAYOUT: TagInfoShort = TagInfoShort(
        0xc617, "CFALayout",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_LINEARIZATION_TABLE: TagInfoShorts = TagInfoShorts(
        0xc618, "LinearizationTable", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_BLACK_LEVEL_REPEAT_DIM: TagInfoShorts = TagInfoShorts(
        0xc619, "BlackLevelRepeatDim", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_BLACK_LEVEL: TagInfoRationals = TagInfoRationals(
        0xc61a, "BlackLevel", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_BLACK_LEVEL_DELTA_H: TagInfoSRationals = TagInfoSRationals(
        0xc61b, "BlackLevelDeltaH", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_BLACK_LEVEL_DELTA_V: TagInfoSRationals = TagInfoSRationals(
        0xc61c, "BlackLevelDeltaV", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_WHITE_LEVEL: TagInfoLongs = TagInfoLongs(
        0xc61d, "WhiteLevel", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_DEFAULT_SCALE: TagInfoRationals = TagInfoRationals(
        0xc61e, "DefaultScale", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_DEFAULT_CROP_ORIGIN: TagInfoRationals = TagInfoRationals(
        0xc61f, "DefaultCropOrigin", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_DEFAULT_CROP_SIZE: TagInfoRationals = TagInfoRationals(
        0xc620, "DefaultCropSize", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_COLOR_MATRIX1: TagInfoSRationals = TagInfoSRationals(
        0xc621, "ColorMatrix1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_COLOR_MATRIX2: TagInfoSRationals = TagInfoSRationals(
        0xc622, "ColorMatrix2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CAMERA_CALIBRATION1: TagInfoSRationals = TagInfoSRationals(
        0xc623, "CameraCalibration1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CAMERA_CALIBRATION2: TagInfoSRationals = TagInfoSRationals(
        0xc624, "CameraCalibration2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_REDUCTION_MATRIX1: TagInfoSRationals = TagInfoSRationals(
        0xc625, "ReductionMatrix1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_REDUCTION_MATRIX2: TagInfoSRationals = TagInfoSRationals(
        0xc626, "ReductionMatrix2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ANALOG_BALANCE: TagInfoRationals = TagInfoRationals(
        0xc627, "AnalogBalance", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_AS_SHOT_NEUTRAL: TagInfoRationals = TagInfoRationals(
        0xc628, "AsShotNeutral", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_AS_SHOT_WHITE_XY: TagInfoRationals = TagInfoRationals(
        0xc629, "AsShotWhiteXY", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_BASELINE_EXPOSURE: TagInfoSRational = TagInfoSRational(
        0xc62a, "BaselineExposure",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_BASELINE_NOISE: TagInfoRational = TagInfoRational(
        0xc62b, "BaselineNoise",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_BASELINE_SHARPNESS: TagInfoRational = TagInfoRational(
        0xc62c, "BaselineSharpness",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_BAYER_GREEN_SPLIT: TagInfoLong = TagInfoLong(
        0xc62d, "BayerGreenSplit",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_LINEAR_RESPONSE_LIMIT: TagInfoRational = TagInfoRational(
        0xc62e, "LinearResponseLimit",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_CAMERA_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0xc62f, "CameraSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_LENS_INFO: TagInfoRationals = TagInfoRationals(
        0xc630, "LensInfo", 4,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CHROMA_BLUR_RADIUS: TagInfoRational = TagInfoRational(
        0xc631, "ChromaBlurRadius",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_ANTI_ALIAS_STRENGTH: TagInfoRational = TagInfoRational(
        0xc632, "AntiAliasStrength",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_SHADOW_SCALE: TagInfoRational = TagInfoRational(
        0xc633, "ShadowScale",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_DNG_PRIVATE_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0xc634, "DNGPrivateData", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_MAKER_NOTE_SAFETY: TagInfoShort = TagInfoShort(
        0xc635, "MakerNoteSafety",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_RAW_IMAGE_SEGMENTATION: TagInfoLongs = TagInfoLongs(
        0xc640, "RawImageSegmentation", 3,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CALIBRATION_ILLUMINANT1: TagInfoShort = TagInfoShort(
        0xc65a, "CalibrationIlluminant1",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_CALIBRATION_ILLUMINANT2: TagInfoShort = TagInfoShort(
        0xc65b, "CalibrationIlluminant2",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_BEST_QUALITY_SCALE: TagInfoRational = TagInfoRational(
        0xc65c, "BestQualityScale",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_RAW_DATA_UNIQUE_ID: TagInfoBytes = TagInfoBytes(
        0xc65d, "RawDataUniqueID", 16,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ORIGINAL_RAW_FILE_NAME: TagInfoAscii = TagInfoAscii(
        0xc68b, "OriginalRawFileName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ORIGINAL_RAW_FILE_DATA: TagInfoUndefineds = TagInfoUndefineds(
        0xc68c, "OriginalRawFileData", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ACTIVE_AREA: TagInfoLongs = TagInfoLongs(
        0xc68d, "ActiveArea", 4,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_MASKED_AREAS: TagInfoLongs = TagInfoLongs(
        0xc68e, "MaskedAreas", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_AS_SHOT_ICC_PROFILE: TagInfoUndefineds = TagInfoUndefineds(
        0xc68f, "AsShotICCProfile", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_AS_SHOT_PRE_PROFILE_MATRIX: TagInfoSRationals = TagInfoSRationals(
        0xc690, "AsShotPreProfileMatrix", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CURRENT_ICC_PROFILE: TagInfoUndefineds = TagInfoUndefineds(
        0xc691, "CurrentICCProfile", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CURRENT_PRE_PROFILE_MATRIX: TagInfoSRationals = TagInfoSRationals(
        0xc692, "CurrentPreProfileMatrix", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_COLORIMETRIC_REFERENCE: TagInfoShort = TagInfoShort(
        0xc6bf, "ColorimetricReference",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_S_RAW_TYPE: TagInfoAscii = TagInfoAscii(
        0xc6c5, "SRawType", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CAMERA_CALIBRATION_SIGNATURE: TagInfoAscii = TagInfoAscii(
        0xc6f3, "CameraCalibrationSignature", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_CALIBRATION_SIGNATURE: TagInfoAscii = TagInfoAscii(
        0xc6f4, "ProfileCalibrationSignature", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_EXTRA_CAMERA_PROFILES: TagInfoUndefineds = TagInfoUndefineds(
        0xc6f5, "ExtraCameraProfiles", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_AS_SHOT_PROFILE_NAME: TagInfoAscii = TagInfoAscii(
        0xc6f6, "AsShotProfileName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_NOISE_REDUCTION_APPLIED: TagInfoRational = TagInfoRational(
        0xc6f7, "NoiseReductionApplied",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_PROFILE_NAME: TagInfoAscii = TagInfoAscii(
        0xc6f8, "ProfileName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DIMS: TagInfoLongs = TagInfoLongs(
        0xc6f9, "ProfileHueSatMapDims", 3,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DATA1: TagInfoFloats = TagInfoFloats(
        0xc6fa, "ProfileHueSatMapData1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DATA2: TagInfoFloats = TagInfoFloats(
        0xc6fb, "ProfileHueSatMapData2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_TONE_CURVE: TagInfoFloats = TagInfoFloats(
        0xc6fc, "ProfileToneCurve", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_EMBED_POLICY: TagInfoLong = TagInfoLong(
        0xc6fd, "ProfileEmbedPolicy",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_PROFILE_COPYRIGHT: TagInfoAscii = TagInfoAscii(
        0xc6fe, "ProfileCopyright", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_FORWARD_MATRIX1: TagInfoSRationals = TagInfoSRationals(
        0xc714, "ForwardMatrix1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_FORWARD_MATRIX2: TagInfoSRationals = TagInfoSRationals(
        0xc715, "ForwardMatrix2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PREVIEW_APPLICATION_NAME: TagInfoAscii = TagInfoAscii(
        0xc716, "PreviewApplicationName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PREVIEW_APPLICATION_VERSION: TagInfoAscii = TagInfoAscii(
        0xc717, "PreviewApplicationVersion", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PREVIEW_SETTINGS_NAME: TagInfoAscii = TagInfoAscii(
        0xc718, "PreviewSettingsName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PREVIEW_SETTINGS_DIGEST: TagInfoBytes = TagInfoBytes(
        0xc719, "PreviewSettingsDigest", 16,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PREVIEW_COLOR_SPACE: TagInfoLong = TagInfoLong(
        0xc71a, "PreviewColorSpace",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_PREVIEW_DATE_TIME: TagInfoAscii = TagInfoAscii(
        0xc71b, "PreviewDateTime", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_RAW_IMAGE_DIGEST: TagInfoBytes = TagInfoBytes(
        0xc71c, "RawImageDigest", 16,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ORIGINAL_RAW_FILE_DIGEST: TagInfoBytes = TagInfoBytes(
        0xc71d, "OriginalRawFileDigest", 16,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_SUB_TILE_BLOCK_SIZE: TagInfoLongs = TagInfoLongs(
        0xc71e, "SubTileBlockSize", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_ROW_INTERLEAVE_FACTOR: TagInfoLong = TagInfoLong(
        0xc71f, "RowInterleaveFactor",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_PROFILE_LOOK_TABLE_DIMS: TagInfoLongs = TagInfoLongs(
        0xc725, "ProfileLookTableDims", 3,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_PROFILE_LOOK_TABLE_DATA: TagInfoFloats = TagInfoFloats(
        0xc726, "ProfileLookTableData", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_OPCODE_LIST1: TagInfoUndefineds = TagInfoUndefineds(
        0xc740, "OpcodeList1", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_OPCODE_LIST2: TagInfoUndefineds = TagInfoUndefineds(
        0xc741, "OpcodeList2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_OPCODE_LIST3: TagInfoUndefineds = TagInfoUndefineds(
        0xc74e, "OpcodeList3", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_NOISE_PROFILE: TagInfoDoubles = TagInfoDoubles(
        0xc761, "NoiseProfile", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_NEW_RAW_IMAGE_DIGEST: TagInfoBytes = TagInfoBytes(
        0xc7a7, "NewRawImageDigest", 16,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DNG_CACHE_VERSION: TagInfoByte = TagInfoByte(
        0xc7aa, "CacheVersion",
        TIFF_DIRECTORY_IFD0
    )
    public val TIFF_TAG_DNG_NOISE_REDUCTION_APPLIED_2: TagInfoRational = TagInfoRational(
        0xa460, "NoiseReductionApplied",
        TIFF_DIRECTORY_IFD0
    )
    public val ALL: List<TagInfo> = listOf(
        TIFF_TAG_DNG_DNG_BACKWARD_VERSION,
        TIFF_TAG_DNG_UNIQUE_CAMERA_MODEL,
        TIFF_TAG_DNG_LOCALIZED_CAMERA_MODEL,
        TIFF_TAG_DNG_CFA_PLANE_COLOR,
        TIFF_TAG_DNG_CFA_LAYOUT,
        TIFF_TAG_DNG_LINEARIZATION_TABLE,
        TIFF_TAG_DNG_BLACK_LEVEL_REPEAT_DIM,
        TIFF_TAG_DNG_BLACK_LEVEL,
        TIFF_TAG_DNG_BLACK_LEVEL_DELTA_H,
        TIFF_TAG_DNG_BLACK_LEVEL_DELTA_V,
        TIFF_TAG_DNG_WHITE_LEVEL,
        TIFF_TAG_DNG_DEFAULT_SCALE,
        TIFF_TAG_DNG_DEFAULT_CROP_ORIGIN,
        TIFF_TAG_DNG_DEFAULT_CROP_SIZE,
        TIFF_TAG_DNG_COLOR_MATRIX1,
        TIFF_TAG_DNG_COLOR_MATRIX2,
        TIFF_TAG_DNG_CAMERA_CALIBRATION1,
        TIFF_TAG_DNG_CAMERA_CALIBRATION2,
        TIFF_TAG_DNG_REDUCTION_MATRIX1,
        TIFF_TAG_DNG_REDUCTION_MATRIX2,
        TIFF_TAG_DNG_ANALOG_BALANCE,
        TIFF_TAG_DNG_AS_SHOT_NEUTRAL,
        TIFF_TAG_DNG_AS_SHOT_WHITE_XY,
        TIFF_TAG_DNG_BASELINE_EXPOSURE,
        TIFF_TAG_DNG_BASELINE_NOISE,
        TIFF_TAG_DNG_BASELINE_SHARPNESS,
        TIFF_TAG_DNG_BAYER_GREEN_SPLIT,
        TIFF_TAG_DNG_LINEAR_RESPONSE_LIMIT,
        TIFF_TAG_DNG_CAMERA_SERIAL_NUMBER,
        TIFF_TAG_DNG_LENS_INFO,
        TIFF_TAG_DNG_CHROMA_BLUR_RADIUS,
        TIFF_TAG_DNG_ANTI_ALIAS_STRENGTH,
        TIFF_TAG_DNG_SHADOW_SCALE,
        TIFF_TAG_DNG_DNG_PRIVATE_DATA,
        TIFF_TAG_DNG_MAKER_NOTE_SAFETY,
        TIFF_TAG_DNG_RAW_IMAGE_SEGMENTATION,
        TIFF_TAG_DNG_CALIBRATION_ILLUMINANT1,
        TIFF_TAG_DNG_CALIBRATION_ILLUMINANT2,
        TIFF_TAG_DNG_BEST_QUALITY_SCALE,
        TIFF_TAG_DNG_RAW_DATA_UNIQUE_ID,
        TIFF_TAG_DNG_ORIGINAL_RAW_FILE_NAME,
        TIFF_TAG_DNG_ORIGINAL_RAW_FILE_DATA,
        TIFF_TAG_DNG_ACTIVE_AREA,
        TIFF_TAG_DNG_MASKED_AREAS,
        TIFF_TAG_DNG_AS_SHOT_ICC_PROFILE,
        TIFF_TAG_DNG_AS_SHOT_PRE_PROFILE_MATRIX,
        TIFF_TAG_DNG_CURRENT_ICC_PROFILE,
        TIFF_TAG_DNG_CURRENT_PRE_PROFILE_MATRIX,
        TIFF_TAG_DNG_COLORIMETRIC_REFERENCE,
        TIFF_TAG_DNG_S_RAW_TYPE,
        TIFF_TAG_DNG_CAMERA_CALIBRATION_SIGNATURE,
        TIFF_TAG_DNG_PROFILE_CALIBRATION_SIGNATURE,
        TIFF_TAG_DNG_EXTRA_CAMERA_PROFILES,
        TIFF_TAG_DNG_AS_SHOT_PROFILE_NAME,
        TIFF_TAG_DNG_NOISE_REDUCTION_APPLIED,
        TIFF_TAG_DNG_PROFILE_NAME,
        TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DIMS,
        TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DATA1,
        TIFF_TAG_DNG_PROFILE_HUE_SAT_MAP_DATA2,
        TIFF_TAG_DNG_PROFILE_TONE_CURVE,
        TIFF_TAG_DNG_PROFILE_EMBED_POLICY,
        TIFF_TAG_DNG_PROFILE_COPYRIGHT,
        TIFF_TAG_DNG_FORWARD_MATRIX1,
        TIFF_TAG_DNG_FORWARD_MATRIX2,
        TIFF_TAG_DNG_PREVIEW_APPLICATION_NAME,
        TIFF_TAG_DNG_PREVIEW_APPLICATION_VERSION,
        TIFF_TAG_DNG_PREVIEW_SETTINGS_NAME,
        TIFF_TAG_DNG_PREVIEW_SETTINGS_DIGEST,
        TIFF_TAG_DNG_PREVIEW_COLOR_SPACE,
        TIFF_TAG_DNG_PREVIEW_DATE_TIME,
        TIFF_TAG_DNG_RAW_IMAGE_DIGEST,
        TIFF_TAG_DNG_ORIGINAL_RAW_FILE_DIGEST,
        TIFF_TAG_DNG_SUB_TILE_BLOCK_SIZE,
        TIFF_TAG_DNG_ROW_INTERLEAVE_FACTOR,
        TIFF_TAG_DNG_PROFILE_LOOK_TABLE_DIMS,
        TIFF_TAG_DNG_PROFILE_LOOK_TABLE_DATA,
        TIFF_TAG_DNG_OPCODE_LIST1,
        TIFF_TAG_DNG_OPCODE_LIST2,
        TIFF_TAG_DNG_OPCODE_LIST3,
        TIFF_TAG_DNG_NOISE_PROFILE,
        TIFF_TAG_DNG_NEW_RAW_IMAGE_DIGEST,
        TIFF_TAG_DNG_CACHE_VERSION,
        TIFF_TAG_DNG_NOISE_REDUCTION_APPLIED_2
    )
}


