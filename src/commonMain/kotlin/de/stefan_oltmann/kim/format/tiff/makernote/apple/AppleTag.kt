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
package de.stefan_oltmann.kim.format.tiff.makernote.apple

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType

import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoInt64
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Apple MakerNote Tags
 *
 * See https://exiftool.sourceforge.net/TagNames/Apple.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object AppleTag {

    public val MAKER_NOTE_VERSION: TagInfoSLong = TagInfoSLong(
        0x0001, "MakerNoteVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AE_MATRIX: TagInfoUndefineds = TagInfoUndefineds(
        0x0002, "AEMatrix", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val RUN_TIME: TagInfoUndefineds = TagInfoUndefineds(
        0x0003, "RunTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AE_STABLE: TagInfoSLong = TagInfoSLong(
        0x0004, "AEStable",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AE_TARGET: TagInfoSLong = TagInfoSLong(
        0x0005, "AETarget",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AE_AVERAGE: TagInfoSLong = TagInfoSLong(
        0x0006, "AEAverage",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AF_STABLE: TagInfoSLong = TagInfoSLong(
        0x0007, "AFStable",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * XYZ coordinates of the acceleration vector in units of g.
     */
    public val ACCELERATION_VECTOR: TagInfoSRationals = TagInfoSRationals(
        0x0008, "AccelerationVector", 3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * See [AppleHdrImageType].
     */
    public val HDR_IMAGE_TYPE: TagInfoSLong = TagInfoSLong(
        0x000a, "HDRImageType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * Unique ID for all images in a burst.
     */
    public val BURST_UUID: TagInfoAscii = TagInfoAscii(
        0x000b, "BurstUUID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val FOCUS_DISTANCE_RANGE: TagInfoSRationals = TagInfoSRationals(
        0x000c, "FocusDistanceRange", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X000D: TagInfoSLong = TagInfoSLong(
        0x000d, "Apple_0x000d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X000E: TagInfoSLong = TagInfoSLong(
        0x000e, "Apple_0x000e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val OIS_MODE: TagInfoSLong = TagInfoSLong(
        0x000f, "OISMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0010: TagInfoSLong = TagInfoSLong(
        0x0010, "Apple_0x0010",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * Called MediaGroupUUID when it appears as an XAttr.
     */
    public val CONTENT_IDENTIFIER: TagInfoAscii = TagInfoAscii(
        0x0011, "ContentIdentifier", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * See [AppleImageCaptureType].
     */
    public val IMAGE_CAPTURE_TYPE: TagInfoSLong = TagInfoSLong(
        0x0014, "ImageCaptureType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val IMAGE_UNIQUE_ID: TagInfoAscii = TagInfoAscii(
        0x0015, "ImageUniqueID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * Divide by RunTimeScale to get time in seconds.
     */
    public val LIVE_PHOTO_VIDEO_INDEX: TagInfoInt64 = TagInfoInt64(
        0x0017, "LivePhotoVideoIndex", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val IMAGE_PROCESSING_FLAGS: TagInfoSLong = TagInfoSLong(
        0x0019, "ImageProcessingFlags",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val QUALITY_HINT: TagInfoAscii = TagInfoAscii(
        0x001a, "QualityHint", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val LUMINANCE_NOISE_AMPLITUDE: TagInfoSRationals = TagInfoSRationals(
        0x001d, "LuminanceNoiseAmplitude", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * Set if a person or pet is detected in the image.
     */
    public val PHOTOS_APP_FEATURE_FLAGS: TagInfoSLong = TagInfoSLong(
        0x001f, "PhotosAppFeatureFlags",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val IMAGE_CAPTURE_REQUEST_ID: TagInfoAscii = TagInfoAscii(
        0x0020, "ImageCaptureRequestID", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val HDR_HEADROOM: TagInfoSRationals = TagInfoSRationals(
        0x0021, "HDRHeadroom", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * First number may be related to focus distance, last number may be related to focus accuracy.
     */
    public val AF_PERFORMANCE: TagInfoSLongs = TagInfoSLongs(
        0x0023, "AFPerformance", 2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SCENE_FLAGS: TagInfoInt64 = TagInfoInt64(
        0x0025, "SceneFlags", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SIGNAL_TO_NOISE_RATIO_TYPE: TagInfoSLong = TagInfoSLong(
        0x0026, "SignalToNoiseRatioType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SIGNAL_TO_NOISE_RATIO: TagInfoSRationals = TagInfoSRationals(
        0x0027, "SignalToNoiseRatio", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0028: TagInfoSLong = TagInfoSLong(
        0x0028, "Apple_0x0028",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val PHOTO_IDENTIFIER: TagInfoAscii = TagInfoAscii(
        0x002b, "PhotoIdentifier", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val COLOR_TEMPERATURE: TagInfoSLong = TagInfoSLong(
        0x002d, "ColorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    /**
     * See [AppleCameraType].
     */
    public val CAMERA_TYPE: TagInfoSLong = TagInfoSLong(
        0x002e, "CameraType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val FOCUS_POSITION: TagInfoSLong = TagInfoSLong(
        0x002f, "FocusPosition",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val HDR_GAIN: TagInfoSRationals = TagInfoSRationals(
        0x0030, "HDRGain", 1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0033: TagInfoSLong = TagInfoSLong(
        0x0033, "Apple_0x0033",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0034: TagInfoSLong = TagInfoSLong(
        0x0034, "Apple_0x0034",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0035: TagInfoSLong = TagInfoSLong(
        0x0035, "Apple_0x0035",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0036: TagInfoSLong = TagInfoSLong(
        0x0036, "Apple_0x0036",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0037: TagInfoSLong = TagInfoSLong(
        0x0037, "Apple_0x0037",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AF_MEASURED_DEPTH: TagInfoSLong = TagInfoSLong(
        0x0038, "AFMeasuredDepth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X003A: TagInfoSLong = TagInfoSLong(
        0x003a, "Apple_0x003a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X003B: TagInfoSLong = TagInfoSLong(
        0x003b, "Apple_0x003b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X003C: TagInfoSLong = TagInfoSLong(
        0x003c, "Apple_0x003c",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val AF_CONFIDENCE: TagInfoSLong = TagInfoSLong(
        0x003d, "AFConfidence",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val COLOR_CORRECTION_MATRIX: TagInfoUndefineds = TagInfoUndefineds(
        0x003e, "ColorCorrectionMatrix", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val GREEN_GHOST_MITIGATION_STATUS: TagInfoSLong = TagInfoSLong(
        0x003f, "GreenGhostMitigationStatus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SEMANTIC_STYLE: TagInfoUndefineds = TagInfoUndefineds(
        0x0040, "SemanticStyle", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SEMANTIC_STYLE_RENDERING_VER: TagInfoSLong = TagInfoSLong(
        0x0041, "SemanticStyleRenderingVer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val SEMANTIC_STYLE_PRESET: TagInfoUndefineds = TagInfoUndefineds(
        0x0042, "SemanticStylePreset", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0043: TagInfoSLong = TagInfoSLong(
        0x0043, "Apple_0x0043",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0044: TagInfoSLong = TagInfoSLong(
        0x0044, "Apple_0x0044",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0045: TagInfoSLong = TagInfoSLong(
        0x0045, "Apple_0x0045",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0046: TagInfoSLong = TagInfoSLong(
        0x0046, "Apple_0x0046",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X004A: TagInfoSLong = TagInfoSLong(
        0x004a, "Apple_0x004a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X004E: TagInfoUndefineds = TagInfoUndefineds(
        0x004e, "Apple_0x004e", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X004F: TagInfoUndefineds = TagInfoUndefineds(
        0x004f, "Apple_0x004f", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X0054: TagInfoUndefineds = TagInfoUndefineds(
        0x0054, "Apple_0x0054", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val APPLE_0X005A: TagInfoUndefineds = TagInfoUndefineds(
        0x005a, "Apple_0x005a", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE
    )

    public val ALL: List<TagInfo> = listOf(
        MAKER_NOTE_VERSION, AE_MATRIX, RUN_TIME,
        AE_STABLE, AE_TARGET, AE_AVERAGE, AF_STABLE,
        ACCELERATION_VECTOR, HDR_IMAGE_TYPE, BURST_UUID,
        FOCUS_DISTANCE_RANGE, APPLE_0X000D, APPLE_0X000E, OIS_MODE,
        APPLE_0X0010, CONTENT_IDENTIFIER,
        IMAGE_CAPTURE_TYPE, IMAGE_UNIQUE_ID, LIVE_PHOTO_VIDEO_INDEX,
        IMAGE_PROCESSING_FLAGS, QUALITY_HINT, LUMINANCE_NOISE_AMPLITUDE,
        PHOTOS_APP_FEATURE_FLAGS, IMAGE_CAPTURE_REQUEST_ID, HDR_HEADROOM,
        AF_PERFORMANCE, SCENE_FLAGS, SIGNAL_TO_NOISE_RATIO_TYPE,
        SIGNAL_TO_NOISE_RATIO, APPLE_0X0028, PHOTO_IDENTIFIER,
        COLOR_TEMPERATURE, CAMERA_TYPE, FOCUS_POSITION, HDR_GAIN,
        APPLE_0X0033, APPLE_0X0034, APPLE_0X0035, APPLE_0X0036,
        APPLE_0X0037, AF_MEASURED_DEPTH,
        APPLE_0X003A, APPLE_0X003B, APPLE_0X003C, AF_CONFIDENCE,
        COLOR_CORRECTION_MATRIX, GREEN_GHOST_MITIGATION_STATUS,
        SEMANTIC_STYLE, SEMANTIC_STYLE_RENDERING_VER, SEMANTIC_STYLE_PRESET,
        APPLE_0X0043, APPLE_0X0044, APPLE_0X0045, APPLE_0X0046,
        APPLE_0X004A, APPLE_0X004E, APPLE_0X004F, APPLE_0X0054, APPLE_0X005A
    )
}
