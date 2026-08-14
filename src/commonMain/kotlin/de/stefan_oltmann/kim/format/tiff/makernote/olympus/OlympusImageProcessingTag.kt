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
package de.stefan_oltmann.kim.format.tiff.makernote.olympus

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloats
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefined
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the ImageProcessing maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Olympus.html#ImageProcessing
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication", "MaxLineLength")
public object OlympusImageProcessingTag {

    public val IMAGE_PROCESSING_VERSION: TagInfoByte = TagInfoByte(
        0x0, "ImageProcessingVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS: TagInfoByte = TagInfoByte(
        0x100, "WB_RBLevels",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS3000_K: TagInfoByte = TagInfoByte(
        0x102, "WB_RBLevels3000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS3300_K: TagInfoByte = TagInfoByte(
        0x103, "WB_RBLevels3300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS3600_K: TagInfoByte = TagInfoByte(
        0x104, "WB_RBLevels3600K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS3900_K: TagInfoByte = TagInfoByte(
        0x105, "WB_RBLevels3900K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS4000_K: TagInfoByte = TagInfoByte(
        0x106, "WB_RBLevels4000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS4300_K: TagInfoByte = TagInfoByte(
        0x107, "WB_RBLevels4300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS4500_K: TagInfoByte = TagInfoByte(
        0x108, "WB_RBLevels4500K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS4800_K: TagInfoByte = TagInfoByte(
        0x109, "WB_RBLevels4800K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS5300_K: TagInfoByte = TagInfoByte(
        0x10a, "WB_RBLevels5300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS6000_K: TagInfoByte = TagInfoByte(
        0x10b, "WB_RBLevels6000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS6600_K: TagInfoByte = TagInfoByte(
        0x10c, "WB_RBLevels6600K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS7500_K: TagInfoByte = TagInfoByte(
        0x10d, "WB_RBLevels7500K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS_CWB1: TagInfoByte = TagInfoByte(
        0x10e, "WB_RBLevelsCWB1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS_CWB2: TagInfoByte = TagInfoByte(
        0x10f, "WB_RBLevelsCWB2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS_CWB3: TagInfoByte = TagInfoByte(
        0x110, "WB_RBLevelsCWB3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_RB_LEVELS_CWB4: TagInfoByte = TagInfoByte(
        0x111, "WB_RBLevelsCWB4",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL3000_K: TagInfoByte = TagInfoByte(
        0x113, "WB_GLevel3000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL3300_K: TagInfoByte = TagInfoByte(
        0x114, "WB_GLevel3300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL3600_K: TagInfoByte = TagInfoByte(
        0x115, "WB_GLevel3600K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL3900_K: TagInfoByte = TagInfoByte(
        0x116, "WB_GLevel3900K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL4000_K: TagInfoByte = TagInfoByte(
        0x117, "WB_GLevel4000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL4300_K: TagInfoByte = TagInfoByte(
        0x118, "WB_GLevel4300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL4500_K: TagInfoByte = TagInfoByte(
        0x119, "WB_GLevel4500K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL4800_K: TagInfoByte = TagInfoByte(
        0x11a, "WB_GLevel4800K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL5300_K: TagInfoByte = TagInfoByte(
        0x11b, "WB_GLevel5300K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL6000_K: TagInfoByte = TagInfoByte(
        0x11c, "WB_GLevel6000K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL6600_K: TagInfoByte = TagInfoByte(
        0x11d, "WB_GLevel6600K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL7500_K: TagInfoByte = TagInfoByte(
        0x11e, "WB_GLevel7500K",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val WB_G_LEVEL: TagInfoByte = TagInfoByte(
        0x11f, "WB_GLevel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val COLOR_MATRIX: TagInfoByte = TagInfoByte(
        0x200, "ColorMatrix",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val ENHANCER: TagInfoByte = TagInfoByte(
        0x300, "Enhancer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val ENHANCER_VALUES: TagInfoByte = TagInfoByte(
        0x301, "EnhancerValues",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CORING_FILTER: TagInfoByte = TagInfoByte(
        0x310, "CoringFilter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CORING_VALUES: TagInfoByte = TagInfoByte(
        0x311, "CoringValues",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val BLACK_LEVEL2: TagInfoByte = TagInfoByte(
        0x600, "BlackLevel2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val GAIN_BASE: TagInfoByte = TagInfoByte(
        0x610, "GainBase",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val VALID_BITS: TagInfoByte = TagInfoByte(
        0x611, "ValidBits",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CROP_LEFT: TagInfoByte = TagInfoByte(
        0x612, "CropLeft",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CROP_TOP: TagInfoByte = TagInfoByte(
        0x613, "CropTop",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CROP_WIDTH: TagInfoByte = TagInfoByte(
        0x614, "CropWidth",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CROP_HEIGHT: TagInfoByte = TagInfoByte(
        0x615, "CropHeight",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val UNKNOWN_BLOCK1: TagInfoByte = TagInfoByte(
        0x635, "UnknownBlock1",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val UNKNOWN_BLOCK2: TagInfoByte = TagInfoByte(
        0x636, "UnknownBlock2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val SENSOR_CALIBRATION: TagInfoByte = TagInfoByte(
        0x805, "SensorCalibration",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val NOISE_REDUCTION2: TagInfoByte = TagInfoByte(
        0x1010, "NoiseReduction2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val DISTORTION_CORRECTION2: TagInfoByte = TagInfoByte(
        0x1011, "DistortionCorrection2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val SHADING_COMPENSATION2: TagInfoByte = TagInfoByte(
        0x1012, "ShadingCompensation2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val MULTIPLE_EXPOSURE_MODE: TagInfoByte = TagInfoByte(
        0x101c, "MultipleExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val UNKNOWN_BLOCK3: TagInfoByte = TagInfoByte(
        0x1103, "UnknownBlock3",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val UNKNOWN_BLOCK4: TagInfoByte = TagInfoByte(
        0x1104, "UnknownBlock4",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val ASPECT_RATIO: TagInfoByte = TagInfoByte(
        0x1112, "AspectRatio",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val ASPECT_FRAME: TagInfoByte = TagInfoByte(
        0x1113, "AspectFrame",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val FACES_DETECTED: TagInfoByte = TagInfoByte(
        0x1200, "FacesDetected",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val FACE_DETECT_AREA: TagInfoByte = TagInfoByte(
        0x1201, "FaceDetectArea",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val MAX_FACES: TagInfoByte = TagInfoByte(
        0x1202, "MaxFaces",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val FACE_DETECT_FRAME_SIZE: TagInfoByte = TagInfoByte(
        0x1203, "FaceDetectFrameSize",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val FACE_DETECT_FRAME_CROP: TagInfoByte = TagInfoByte(
        0x1207, "FaceDetectFrameCrop",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val CAMERA_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x1306, "CameraTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val KEYSTONE_COMPENSATION: TagInfoByte = TagInfoByte(
        0x1900, "KeystoneCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val KEYSTONE_DIRECTION: TagInfoByte = TagInfoByte(
        0x1901, "KeystoneDirection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val KEYSTONE_VALUE: TagInfoByte = TagInfoByte(
        0x1906, "KeystoneValue",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val GND_FILTER_TYPE: TagInfoByte = TagInfoByte(
        0x2110, "GNDFilterType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0101: TagInfoShorts = TagInfoShorts(
        0x0101, "Olympus_ImageProcessing_0x0101",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0120: TagInfoShorts = TagInfoShorts(
        0x0120, "Olympus_ImageProcessing_0x0120",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0121: TagInfoShorts = TagInfoShorts(
        0x0121, "Olympus_ImageProcessing_0x0121",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0122: TagInfoSShorts = TagInfoSShorts(
        0x0122, "Olympus_ImageProcessing_0x0122",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0123: TagInfoShorts = TagInfoShorts(
        0x0123, "Olympus_ImageProcessing_0x0123",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0124: TagInfoShort = TagInfoShort(
        0x0124, "Olympus_ImageProcessing_0x0124",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0125: TagInfoShorts = TagInfoShorts(
        0x0125, "Olympus_ImageProcessing_0x0125",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0201: TagInfoShorts = TagInfoShorts(
        0x0201, "Olympus_ImageProcessing_0x0201",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0202: TagInfoShorts = TagInfoShorts(
        0x0202, "Olympus_ImageProcessing_0x0202",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0203: TagInfoShorts = TagInfoShorts(
        0x0203, "Olympus_ImageProcessing_0x0203",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0204: TagInfoShorts = TagInfoShorts(
        0x0204, "Olympus_ImageProcessing_0x0204",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0205: TagInfoShorts = TagInfoShorts(
        0x0205, "Olympus_ImageProcessing_0x0205",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0206: TagInfoShorts = TagInfoShorts(
        0x0206, "Olympus_ImageProcessing_0x0206",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0207: TagInfoShorts = TagInfoShorts(
        0x0207, "Olympus_ImageProcessing_0x0207",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0208: TagInfoShorts = TagInfoShorts(
        0x0208, "Olympus_ImageProcessing_0x0208",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0209: TagInfoShorts = TagInfoShorts(
        0x0209, "Olympus_ImageProcessing_0x0209",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020A: TagInfoShorts = TagInfoShorts(
        0x020a, "Olympus_ImageProcessing_0x020a",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020B: TagInfoShorts = TagInfoShorts(
        0x020b, "Olympus_ImageProcessing_0x020b",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020C: TagInfoShorts = TagInfoShorts(
        0x020c, "Olympus_ImageProcessing_0x020c",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020D: TagInfoShorts = TagInfoShorts(
        0x020d, "Olympus_ImageProcessing_0x020d",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020E: TagInfoShorts = TagInfoShorts(
        0x020e, "Olympus_ImageProcessing_0x020e",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X020F: TagInfoShorts = TagInfoShorts(
        0x020f, "Olympus_ImageProcessing_0x020f",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0210: TagInfoShorts = TagInfoShorts(
        0x0210, "Olympus_ImageProcessing_0x0210",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0211: TagInfoShorts = TagInfoShorts(
        0x0211, "Olympus_ImageProcessing_0x0211",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0212: TagInfoShorts = TagInfoShorts(
        0x0212, "Olympus_ImageProcessing_0x0212",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0213: TagInfoShorts = TagInfoShorts(
        0x0213, "Olympus_ImageProcessing_0x0213",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0214: TagInfoShorts = TagInfoShorts(
        0x0214, "Olympus_ImageProcessing_0x0214",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0215: TagInfoShorts = TagInfoShorts(
        0x0215, "Olympus_ImageProcessing_0x0215",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0216: TagInfoShorts = TagInfoShorts(
        0x0216, "Olympus_ImageProcessing_0x0216",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0217: TagInfoShorts = TagInfoShorts(
        0x0217, "Olympus_ImageProcessing_0x0217",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0218: TagInfoShorts = TagInfoShorts(
        0x0218, "Olympus_ImageProcessing_0x0218",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0219: TagInfoShorts = TagInfoShorts(
        0x0219, "Olympus_ImageProcessing_0x0219",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X021A: TagInfoShorts = TagInfoShorts(
        0x021a, "Olympus_ImageProcessing_0x021a",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0228: TagInfoShorts = TagInfoShorts(
        0x0228, "Olympus_ImageProcessing_0x0228",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0229: TagInfoShorts = TagInfoShorts(
        0x0229, "Olympus_ImageProcessing_0x0229",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0259: TagInfoSShorts = TagInfoSShorts(
        0x0259, "Olympus_ImageProcessing_0x0259",
        99,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0302: TagInfoShort = TagInfoShort(
        0x0302, "Olympus_ImageProcessing_0x0302",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0303: TagInfoShort = TagInfoShort(
        0x0303, "Olympus_ImageProcessing_0x0303",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0304: TagInfoShorts = TagInfoShorts(
        0x0304, "Olympus_ImageProcessing_0x0304",
        11,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0305: TagInfoShorts = TagInfoShorts(
        0x0305, "Olympus_ImageProcessing_0x0305",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0312: TagInfoShort = TagInfoShort(
        0x0312, "Olympus_ImageProcessing_0x0312",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0313: TagInfoShort = TagInfoShort(
        0x0313, "Olympus_ImageProcessing_0x0313",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0314: TagInfoShorts = TagInfoShorts(
        0x0314, "Olympus_ImageProcessing_0x0314",
        11,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0315: TagInfoShorts = TagInfoShorts(
        0x0315, "Olympus_ImageProcessing_0x0315",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0340: TagInfoSShorts = TagInfoSShorts(
        0x0340, "Olympus_ImageProcessing_0x0340",
        24,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0341: TagInfoSShorts = TagInfoSShorts(
        0x0341, "Olympus_ImageProcessing_0x0341",
        24,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0342: TagInfoSShorts = TagInfoSShorts(
        0x0342, "Olympus_ImageProcessing_0x0342",
        24,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0343: TagInfoSShorts = TagInfoSShorts(
        0x0343, "Olympus_ImageProcessing_0x0343",
        24,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0350: TagInfoShorts = TagInfoShorts(
        0x0350, "Olympus_ImageProcessing_0x0350",
        12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0351: TagInfoShorts = TagInfoShorts(
        0x0351, "Olympus_ImageProcessing_0x0351",
        30,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0400: TagInfoShorts = TagInfoShorts(
        0x0400, "Olympus_ImageProcessing_0x0400",
        63,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0401: TagInfoShorts = TagInfoShorts(
        0x0401, "Olympus_ImageProcessing_0x0401",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0402: TagInfoShorts = TagInfoShorts(
        0x0402, "Olympus_ImageProcessing_0x0402",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0403: TagInfoShorts = TagInfoShorts(
        0x0403, "Olympus_ImageProcessing_0x0403",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0404: TagInfoShorts = TagInfoShorts(
        0x0404, "Olympus_ImageProcessing_0x0404",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0405: TagInfoShorts = TagInfoShorts(
        0x0405, "Olympus_ImageProcessing_0x0405",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0406: TagInfoShorts = TagInfoShorts(
        0x0406, "Olympus_ImageProcessing_0x0406",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0407: TagInfoShort = TagInfoShort(
        0x0407, "Olympus_ImageProcessing_0x0407",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0408: TagInfoShorts = TagInfoShorts(
        0x0408, "Olympus_ImageProcessing_0x0408",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0409: TagInfoShorts = TagInfoShorts(
        0x0409, "Olympus_ImageProcessing_0x0409",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X040A: TagInfoShorts = TagInfoShorts(
        0x040a, "Olympus_ImageProcessing_0x040a",
        15,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0410: TagInfoShorts = TagInfoShorts(
        0x0410, "Olympus_ImageProcessing_0x0410",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0420: TagInfoSShort = TagInfoSShort(
        0x0420, "Olympus_ImageProcessing_0x0420",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0421: TagInfoShort = TagInfoShort(
        0x0421, "Olympus_ImageProcessing_0x0421",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0422: TagInfoSShorts = TagInfoSShorts(
        0x0422, "Olympus_ImageProcessing_0x0422",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0423: TagInfoShorts = TagInfoShorts(
        0x0423, "Olympus_ImageProcessing_0x0423",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0424: TagInfoSShorts = TagInfoSShorts(
        0x0424, "Olympus_ImageProcessing_0x0424",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0425: TagInfoShorts = TagInfoShorts(
        0x0425, "Olympus_ImageProcessing_0x0425",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0601: TagInfoShorts = TagInfoShorts(
        0x0601, "Olympus_ImageProcessing_0x0601",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0602: TagInfoShorts = TagInfoShorts(
        0x0602, "Olympus_ImageProcessing_0x0602",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0603: TagInfoShorts = TagInfoShorts(
        0x0603, "Olympus_ImageProcessing_0x0603",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0617: TagInfoShort = TagInfoShort(
        0x0617, "Olympus_ImageProcessing_0x0617",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0618: TagInfoShort = TagInfoShort(
        0x0618, "Olympus_ImageProcessing_0x0618",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0619: TagInfoShort = TagInfoShort(
        0x0619, "Olympus_ImageProcessing_0x0619",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0620: TagInfoShort = TagInfoShort(
        0x0620, "Olympus_ImageProcessing_0x0620",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0630: TagInfoShort = TagInfoShort(
        0x0630, "Olympus_ImageProcessing_0x0630",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0637: TagInfoShorts = TagInfoShorts(
        0x0637, "Olympus_ImageProcessing_0x0637",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0638: TagInfoLong = TagInfoLong(
        0x0638, "Olympus_ImageProcessing_0x0638",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0640: TagInfoShort = TagInfoShort(
        0x0640, "Olympus_ImageProcessing_0x0640",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0641: TagInfoShort = TagInfoShort(
        0x0641, "Olympus_ImageProcessing_0x0641",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0642: TagInfoShort = TagInfoShort(
        0x0642, "Olympus_ImageProcessing_0x0642",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0643: TagInfoShort = TagInfoShort(
        0x0643, "Olympus_ImageProcessing_0x0643",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0644: TagInfoShort = TagInfoShort(
        0x0644, "Olympus_ImageProcessing_0x0644",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0645: TagInfoShort = TagInfoShort(
        0x0645, "Olympus_ImageProcessing_0x0645",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0646: TagInfoShort = TagInfoShort(
        0x0646, "Olympus_ImageProcessing_0x0646",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0647: TagInfoShort = TagInfoShort(
        0x0647, "Olympus_ImageProcessing_0x0647",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0648: TagInfoShort = TagInfoShort(
        0x0648, "Olympus_ImageProcessing_0x0648",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0649: TagInfoShort = TagInfoShort(
        0x0649, "Olympus_ImageProcessing_0x0649",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0650: TagInfoShort = TagInfoShort(
        0x0650, "Olympus_ImageProcessing_0x0650",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0651: TagInfoShort = TagInfoShort(
        0x0651, "Olympus_ImageProcessing_0x0651",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0652: TagInfoShort = TagInfoShort(
        0x0652, "Olympus_ImageProcessing_0x0652",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0653: TagInfoShort = TagInfoShort(
        0x0653, "Olympus_ImageProcessing_0x0653",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0654: TagInfoShort = TagInfoShort(
        0x0654, "Olympus_ImageProcessing_0x0654",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0800: TagInfoFloats = TagInfoFloats(
        0x0800, "Olympus_ImageProcessing_0x0800",
        9,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0801: TagInfoShorts = TagInfoShorts(
        0x0801, "Olympus_ImageProcessing_0x0801",
        16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0802: TagInfoRational = TagInfoRational(
        0x0802, "Olympus_ImageProcessing_0x0802",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0803: TagInfoShorts = TagInfoShorts(
        0x0803, "Olympus_ImageProcessing_0x0803",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X0804: TagInfoShorts = TagInfoShorts(
        0x0804, "Olympus_ImageProcessing_0x0804",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1003: TagInfoShort = TagInfoShort(
        0x1003, "Olympus_ImageProcessing_0x1003",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1004: TagInfoShort = TagInfoShort(
        0x1004, "Olympus_ImageProcessing_0x1004",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1005: TagInfoShorts = TagInfoShorts(
        0x1005, "Olympus_ImageProcessing_0x1005",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1006: TagInfoShorts = TagInfoShorts(
        0x1006, "Olympus_ImageProcessing_0x1006",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1007: TagInfoShorts = TagInfoShorts(
        0x1007, "Olympus_ImageProcessing_0x1007",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1008: TagInfoShort = TagInfoShort(
        0x1008, "Olympus_ImageProcessing_0x1008",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1009: TagInfoShorts = TagInfoShorts(
        0x1009, "Olympus_ImageProcessing_0x1009",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X100A: TagInfoShorts = TagInfoShorts(
        0x100a, "Olympus_ImageProcessing_0x100a",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1013: TagInfoShorts = TagInfoShorts(
        0x1013, "Olympus_ImageProcessing_0x1013",
        10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1014: TagInfoShorts = TagInfoShorts(
        0x1014, "Olympus_ImageProcessing_0x1014",
        10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1015: TagInfoShorts = TagInfoShorts(
        0x1015, "Olympus_ImageProcessing_0x1015",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1016: TagInfoShorts = TagInfoShorts(
        0x1016, "Olympus_ImageProcessing_0x1016",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1017: TagInfoShorts = TagInfoShorts(
        0x1017, "Olympus_ImageProcessing_0x1017",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1018: TagInfoShorts = TagInfoShorts(
        0x1018, "Olympus_ImageProcessing_0x1018",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1019: TagInfoShorts = TagInfoShorts(
        0x1019, "Olympus_ImageProcessing_0x1019",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X101A: TagInfoShort = TagInfoShort(
        0x101a, "Olympus_ImageProcessing_0x101a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X101B: TagInfoSShorts = TagInfoSShorts(
        0x101b, "Olympus_ImageProcessing_0x101b",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X101D: TagInfoUndefined = TagInfoUndefined(
        0x101d, "Olympus_ImageProcessing_0x101d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X101E: TagInfoShorts = TagInfoShorts(
        0x101e, "Olympus_ImageProcessing_0x101e",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1102: TagInfoBytes = TagInfoBytes(
        0x1102, "Olympus_ImageProcessing_0x1102",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1105: TagInfoBytes = TagInfoBytes(
        0x1105, "Olympus_ImageProcessing_0x1105",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1106: TagInfoBytes = TagInfoBytes(
        0x1106, "Olympus_ImageProcessing_0x1106",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1107: TagInfoShorts = TagInfoShorts(
        0x1107, "Olympus_ImageProcessing_0x1107",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1108: TagInfoShorts = TagInfoShorts(
        0x1108, "Olympus_ImageProcessing_0x1108",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1109: TagInfoShorts = TagInfoShorts(
        0x1109, "Olympus_ImageProcessing_0x1109",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110A: TagInfoSShort = TagInfoSShort(
        0x110a, "Olympus_ImageProcessing_0x110a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110B: TagInfoShorts = TagInfoShorts(
        0x110b, "Olympus_ImageProcessing_0x110b",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110C: TagInfoShorts = TagInfoShorts(
        0x110c, "Olympus_ImageProcessing_0x110c",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110D: TagInfoShort = TagInfoShort(
        0x110d, "Olympus_ImageProcessing_0x110d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110E: TagInfoShorts = TagInfoShorts(
        0x110e, "Olympus_ImageProcessing_0x110e",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X110F: TagInfoShorts = TagInfoShorts(
        0x110f, "Olympus_ImageProcessing_0x110f",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1110: TagInfoShorts = TagInfoShorts(
        0x1110, "Olympus_ImageProcessing_0x1110",
        10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1114: TagInfoByte = TagInfoByte(
        0x1114, "Olympus_ImageProcessing_0x1114",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1115: TagInfoShorts = TagInfoShorts(
        0x1115, "Olympus_ImageProcessing_0x1115",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1116: TagInfoShorts = TagInfoShorts(
        0x1116, "Olympus_ImageProcessing_0x1116",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1117: TagInfoLongs = TagInfoLongs(
        0x1117, "Olympus_ImageProcessing_0x1117",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1118: TagInfoSShorts = TagInfoSShorts(
        0x1118, "Olympus_ImageProcessing_0x1118",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1119: TagInfoBytes = TagInfoBytes(
        0x1119, "Olympus_ImageProcessing_0x1119",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X111A: TagInfoBytes = TagInfoBytes(
        0x111a, "Olympus_ImageProcessing_0x111a",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X111B: TagInfoUndefineds = TagInfoUndefineds(
        0x111b, "Olympus_ImageProcessing_0x111b",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X111D: TagInfoShorts = TagInfoShorts(
        0x111d, "Olympus_ImageProcessing_0x111d",
        10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X111E: TagInfoShorts = TagInfoShorts(
        0x111e, "Olympus_ImageProcessing_0x111e",
        18,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X111F: TagInfoShorts = TagInfoShorts(
        0x111f, "Olympus_ImageProcessing_0x111f",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1120: TagInfoByte = TagInfoByte(
        0x1120, "Olympus_ImageProcessing_0x1120",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1121: TagInfoBytes = TagInfoBytes(
        0x1121, "Olympus_ImageProcessing_0x1121",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1122: TagInfoSShorts = TagInfoSShorts(
        0x1122, "Olympus_ImageProcessing_0x1122",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1123: TagInfoByte = TagInfoByte(
        0x1123, "Olympus_ImageProcessing_0x1123",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1124: TagInfoSShorts = TagInfoSShorts(
        0x1124, "Olympus_ImageProcessing_0x1124",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1125: TagInfoSShorts = TagInfoSShorts(
        0x1125, "Olympus_ImageProcessing_0x1125",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1126: TagInfoBytes = TagInfoBytes(
        0x1126, "Olympus_ImageProcessing_0x1126",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1127: TagInfoSShorts = TagInfoSShorts(
        0x1127, "Olympus_ImageProcessing_0x1127",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1204: TagInfoSShorts = TagInfoSShorts(
        0x1204, "Olympus_ImageProcessing_0x1204",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1205: TagInfoSShort = TagInfoSShort(
        0x1205, "Olympus_ImageProcessing_0x1205",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1206: TagInfoSShorts = TagInfoSShorts(
        0x1206, "Olympus_ImageProcessing_0x1206",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1208: TagInfoSShorts = TagInfoSShorts(
        0x1208, "Olympus_ImageProcessing_0x1208",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1209: TagInfoByte = TagInfoByte(
        0x1209, "Olympus_ImageProcessing_0x1209",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X120A: TagInfoShorts = TagInfoShorts(
        0x120a, "Olympus_ImageProcessing_0x120a",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X120B: TagInfoSShort = TagInfoSShort(
        0x120b, "Olympus_ImageProcessing_0x120b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1300: TagInfoSShorts = TagInfoSShorts(
        0x1300, "Olympus_ImageProcessing_0x1300",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1301: TagInfoShorts = TagInfoShorts(
        0x1301, "Olympus_ImageProcessing_0x1301",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1302: TagInfoSShorts = TagInfoSShorts(
        0x1302, "Olympus_ImageProcessing_0x1302",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1303: TagInfoSShorts = TagInfoSShorts(
        0x1303, "Olympus_ImageProcessing_0x1303",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1304: TagInfoSShorts = TagInfoSShorts(
        0x1304, "Olympus_ImageProcessing_0x1304",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1305: TagInfoShorts = TagInfoShorts(
        0x1305, "Olympus_ImageProcessing_0x1305",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1307: TagInfoShort = TagInfoShort(
        0x1307, "Olympus_ImageProcessing_0x1307",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1308: TagInfoSShort = TagInfoSShort(
        0x1308, "Olympus_ImageProcessing_0x1308",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1400: TagInfoSShorts = TagInfoSShorts(
        0x1400, "Olympus_ImageProcessing_0x1400",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1401: TagInfoShorts = TagInfoShorts(
        0x1401, "Olympus_ImageProcessing_0x1401",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1402: TagInfoSShorts = TagInfoSShorts(
        0x1402, "Olympus_ImageProcessing_0x1402",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1403: TagInfoShorts = TagInfoShorts(
        0x1403, "Olympus_ImageProcessing_0x1403",
        10,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1404: TagInfoShorts = TagInfoShorts(
        0x1404, "Olympus_ImageProcessing_0x1404",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1405: TagInfoShorts = TagInfoShorts(
        0x1405, "Olympus_ImageProcessing_0x1405",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1406: TagInfoShorts = TagInfoShorts(
        0x1406, "Olympus_ImageProcessing_0x1406",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1407: TagInfoShorts = TagInfoShorts(
        0x1407, "Olympus_ImageProcessing_0x1407",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1408: TagInfoSShorts = TagInfoSShorts(
        0x1408, "Olympus_ImageProcessing_0x1408",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1409: TagInfoShorts = TagInfoShorts(
        0x1409, "Olympus_ImageProcessing_0x1409",
        15,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1500: TagInfoBytes = TagInfoBytes(
        0x1500, "Olympus_ImageProcessing_0x1500",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1501: TagInfoBytes = TagInfoBytes(
        0x1501, "Olympus_ImageProcessing_0x1501",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1502: TagInfoByte = TagInfoByte(
        0x1502, "Olympus_ImageProcessing_0x1502",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1503: TagInfoSShorts = TagInfoSShorts(
        0x1503, "Olympus_ImageProcessing_0x1503",
        12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1504: TagInfoBytes = TagInfoBytes(
        0x1504, "Olympus_ImageProcessing_0x1504",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1505: TagInfoSShorts = TagInfoSShorts(
        0x1505, "Olympus_ImageProcessing_0x1505",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1506: TagInfoShorts = TagInfoShorts(
        0x1506, "Olympus_ImageProcessing_0x1506",
        12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1507: TagInfoShort = TagInfoShort(
        0x1507, "Olympus_ImageProcessing_0x1507",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1508: TagInfoSShorts = TagInfoSShorts(
        0x1508, "Olympus_ImageProcessing_0x1508",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1509: TagInfoShort = TagInfoShort(
        0x1509, "Olympus_ImageProcessing_0x1509",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150A: TagInfoFloats = TagInfoFloats(
        0x150a, "Olympus_ImageProcessing_0x150a",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150B: TagInfoByte = TagInfoByte(
        0x150b, "Olympus_ImageProcessing_0x150b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150C: TagInfoFloats = TagInfoFloats(
        0x150c, "Olympus_ImageProcessing_0x150c",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150D: TagInfoByte = TagInfoByte(
        0x150d, "Olympus_ImageProcessing_0x150d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150E: TagInfoByte = TagInfoByte(
        0x150e, "Olympus_ImageProcessing_0x150e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X150F: TagInfoByte = TagInfoByte(
        0x150f, "Olympus_ImageProcessing_0x150f",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1600: TagInfoShort = TagInfoShort(
        0x1600, "Olympus_ImageProcessing_0x1600",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1601: TagInfoByte = TagInfoByte(
        0x1601, "Olympus_ImageProcessing_0x1601",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1602: TagInfoShorts = TagInfoShorts(
        0x1602, "Olympus_ImageProcessing_0x1602",
        6,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1603: TagInfoBytes = TagInfoBytes(
        0x1603, "Olympus_ImageProcessing_0x1603",
        12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1604: TagInfoBytes = TagInfoBytes(
        0x1604, "Olympus_ImageProcessing_0x1604",
        12,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1700: TagInfoUndefineds = TagInfoUndefineds(
        0x1700, "Olympus_ImageProcessing_0x1700",
        512,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1800: TagInfoBytes = TagInfoBytes(
        0x1800, "Olympus_ImageProcessing_0x1800",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val OLYMPUS_IMAGE_PROCESSING_0X1801: TagInfoShort = TagInfoShort(
        0x1801, "Olympus_ImageProcessing_0x1801",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
    )

    public val ALL: List<TagInfo> = listOf(
        IMAGE_PROCESSING_VERSION,
        WB_RB_LEVELS,
        OLYMPUS_IMAGE_PROCESSING_0X0101,
        WB_RB_LEVELS3000_K,
        WB_RB_LEVELS3300_K,
        WB_RB_LEVELS3600_K,
        WB_RB_LEVELS3900_K,
        WB_RB_LEVELS4000_K,
        WB_RB_LEVELS4300_K,
        WB_RB_LEVELS4500_K,
        WB_RB_LEVELS4800_K,
        WB_RB_LEVELS5300_K,
        WB_RB_LEVELS6000_K,
        WB_RB_LEVELS6600_K,
        WB_RB_LEVELS7500_K,
        WB_RB_LEVELS_CWB1,
        WB_RB_LEVELS_CWB2,
        WB_RB_LEVELS_CWB3,
        WB_RB_LEVELS_CWB4,
        WB_G_LEVEL3000_K,
        WB_G_LEVEL3300_K,
        WB_G_LEVEL3600_K,
        WB_G_LEVEL3900_K,
        WB_G_LEVEL4000_K,
        WB_G_LEVEL4300_K,
        WB_G_LEVEL4500_K,
        WB_G_LEVEL4800_K,
        WB_G_LEVEL5300_K,
        WB_G_LEVEL6000_K,
        WB_G_LEVEL6600_K,
        WB_G_LEVEL7500_K,
        WB_G_LEVEL,
        OLYMPUS_IMAGE_PROCESSING_0X0120,
        OLYMPUS_IMAGE_PROCESSING_0X0121,
        OLYMPUS_IMAGE_PROCESSING_0X0122,
        OLYMPUS_IMAGE_PROCESSING_0X0123,
        OLYMPUS_IMAGE_PROCESSING_0X0124,
        OLYMPUS_IMAGE_PROCESSING_0X0125,
        COLOR_MATRIX,
        OLYMPUS_IMAGE_PROCESSING_0X0201,
        OLYMPUS_IMAGE_PROCESSING_0X0202,
        OLYMPUS_IMAGE_PROCESSING_0X0203,
        OLYMPUS_IMAGE_PROCESSING_0X0204,
        OLYMPUS_IMAGE_PROCESSING_0X0205,
        OLYMPUS_IMAGE_PROCESSING_0X0206,
        OLYMPUS_IMAGE_PROCESSING_0X0207,
        OLYMPUS_IMAGE_PROCESSING_0X0208,
        OLYMPUS_IMAGE_PROCESSING_0X0209,
        OLYMPUS_IMAGE_PROCESSING_0X020A,
        OLYMPUS_IMAGE_PROCESSING_0X020B,
        OLYMPUS_IMAGE_PROCESSING_0X020C,
        OLYMPUS_IMAGE_PROCESSING_0X020D,
        OLYMPUS_IMAGE_PROCESSING_0X020E,
        OLYMPUS_IMAGE_PROCESSING_0X020F,
        OLYMPUS_IMAGE_PROCESSING_0X0210,
        OLYMPUS_IMAGE_PROCESSING_0X0211,
        OLYMPUS_IMAGE_PROCESSING_0X0212,
        OLYMPUS_IMAGE_PROCESSING_0X0213,
        OLYMPUS_IMAGE_PROCESSING_0X0214,
        OLYMPUS_IMAGE_PROCESSING_0X0215,
        OLYMPUS_IMAGE_PROCESSING_0X0216,
        OLYMPUS_IMAGE_PROCESSING_0X0217,
        OLYMPUS_IMAGE_PROCESSING_0X0218,
        OLYMPUS_IMAGE_PROCESSING_0X0219,
        OLYMPUS_IMAGE_PROCESSING_0X021A,
        OLYMPUS_IMAGE_PROCESSING_0X0228,
        OLYMPUS_IMAGE_PROCESSING_0X0229,
        OLYMPUS_IMAGE_PROCESSING_0X0259,
        ENHANCER,
        ENHANCER_VALUES,
        OLYMPUS_IMAGE_PROCESSING_0X0302,
        OLYMPUS_IMAGE_PROCESSING_0X0303,
        OLYMPUS_IMAGE_PROCESSING_0X0304,
        OLYMPUS_IMAGE_PROCESSING_0X0305,
        CORING_FILTER,
        CORING_VALUES,
        OLYMPUS_IMAGE_PROCESSING_0X0312,
        OLYMPUS_IMAGE_PROCESSING_0X0313,
        OLYMPUS_IMAGE_PROCESSING_0X0314,
        OLYMPUS_IMAGE_PROCESSING_0X0315,
        OLYMPUS_IMAGE_PROCESSING_0X0340,
        OLYMPUS_IMAGE_PROCESSING_0X0341,
        OLYMPUS_IMAGE_PROCESSING_0X0342,
        OLYMPUS_IMAGE_PROCESSING_0X0343,
        OLYMPUS_IMAGE_PROCESSING_0X0350,
        OLYMPUS_IMAGE_PROCESSING_0X0351,
        OLYMPUS_IMAGE_PROCESSING_0X0400,
        OLYMPUS_IMAGE_PROCESSING_0X0401,
        OLYMPUS_IMAGE_PROCESSING_0X0402,
        OLYMPUS_IMAGE_PROCESSING_0X0403,
        OLYMPUS_IMAGE_PROCESSING_0X0404,
        OLYMPUS_IMAGE_PROCESSING_0X0405,
        OLYMPUS_IMAGE_PROCESSING_0X0406,
        OLYMPUS_IMAGE_PROCESSING_0X0407,
        OLYMPUS_IMAGE_PROCESSING_0X0408,
        OLYMPUS_IMAGE_PROCESSING_0X0409,
        OLYMPUS_IMAGE_PROCESSING_0X040A,
        OLYMPUS_IMAGE_PROCESSING_0X0410,
        OLYMPUS_IMAGE_PROCESSING_0X0420,
        OLYMPUS_IMAGE_PROCESSING_0X0421,
        OLYMPUS_IMAGE_PROCESSING_0X0422,
        OLYMPUS_IMAGE_PROCESSING_0X0423,
        OLYMPUS_IMAGE_PROCESSING_0X0424,
        OLYMPUS_IMAGE_PROCESSING_0X0425,
        BLACK_LEVEL2,
        OLYMPUS_IMAGE_PROCESSING_0X0601,
        OLYMPUS_IMAGE_PROCESSING_0X0602,
        OLYMPUS_IMAGE_PROCESSING_0X0603,
        GAIN_BASE,
        VALID_BITS,
        CROP_LEFT,
        CROP_TOP,
        CROP_WIDTH,
        CROP_HEIGHT,
        OLYMPUS_IMAGE_PROCESSING_0X0617,
        OLYMPUS_IMAGE_PROCESSING_0X0618,
        OLYMPUS_IMAGE_PROCESSING_0X0619,
        OLYMPUS_IMAGE_PROCESSING_0X0620,
        OLYMPUS_IMAGE_PROCESSING_0X0630,
        UNKNOWN_BLOCK1,
        UNKNOWN_BLOCK2,
        OLYMPUS_IMAGE_PROCESSING_0X0637,
        OLYMPUS_IMAGE_PROCESSING_0X0638,
        OLYMPUS_IMAGE_PROCESSING_0X0640,
        OLYMPUS_IMAGE_PROCESSING_0X0641,
        OLYMPUS_IMAGE_PROCESSING_0X0642,
        OLYMPUS_IMAGE_PROCESSING_0X0643,
        OLYMPUS_IMAGE_PROCESSING_0X0644,
        OLYMPUS_IMAGE_PROCESSING_0X0645,
        OLYMPUS_IMAGE_PROCESSING_0X0646,
        OLYMPUS_IMAGE_PROCESSING_0X0647,
        OLYMPUS_IMAGE_PROCESSING_0X0648,
        OLYMPUS_IMAGE_PROCESSING_0X0649,
        OLYMPUS_IMAGE_PROCESSING_0X0650,
        OLYMPUS_IMAGE_PROCESSING_0X0651,
        OLYMPUS_IMAGE_PROCESSING_0X0652,
        OLYMPUS_IMAGE_PROCESSING_0X0653,
        OLYMPUS_IMAGE_PROCESSING_0X0654,
        OLYMPUS_IMAGE_PROCESSING_0X0800,
        OLYMPUS_IMAGE_PROCESSING_0X0801,
        OLYMPUS_IMAGE_PROCESSING_0X0802,
        OLYMPUS_IMAGE_PROCESSING_0X0803,
        OLYMPUS_IMAGE_PROCESSING_0X0804,
        SENSOR_CALIBRATION,
        OLYMPUS_IMAGE_PROCESSING_0X1003,
        OLYMPUS_IMAGE_PROCESSING_0X1004,
        OLYMPUS_IMAGE_PROCESSING_0X1005,
        OLYMPUS_IMAGE_PROCESSING_0X1006,
        OLYMPUS_IMAGE_PROCESSING_0X1007,
        OLYMPUS_IMAGE_PROCESSING_0X1008,
        OLYMPUS_IMAGE_PROCESSING_0X1009,
        OLYMPUS_IMAGE_PROCESSING_0X100A,
        NOISE_REDUCTION2,
        DISTORTION_CORRECTION2,
        SHADING_COMPENSATION2,
        OLYMPUS_IMAGE_PROCESSING_0X1013,
        OLYMPUS_IMAGE_PROCESSING_0X1014,
        OLYMPUS_IMAGE_PROCESSING_0X1015,
        OLYMPUS_IMAGE_PROCESSING_0X1016,
        OLYMPUS_IMAGE_PROCESSING_0X1017,
        OLYMPUS_IMAGE_PROCESSING_0X1018,
        OLYMPUS_IMAGE_PROCESSING_0X1019,
        OLYMPUS_IMAGE_PROCESSING_0X101A,
        OLYMPUS_IMAGE_PROCESSING_0X101B,
        MULTIPLE_EXPOSURE_MODE,
        OLYMPUS_IMAGE_PROCESSING_0X101D,
        OLYMPUS_IMAGE_PROCESSING_0X101E,
        OLYMPUS_IMAGE_PROCESSING_0X1102,
        UNKNOWN_BLOCK3,
        UNKNOWN_BLOCK4,
        OLYMPUS_IMAGE_PROCESSING_0X1105,
        OLYMPUS_IMAGE_PROCESSING_0X1106,
        OLYMPUS_IMAGE_PROCESSING_0X1107,
        OLYMPUS_IMAGE_PROCESSING_0X1108,
        OLYMPUS_IMAGE_PROCESSING_0X1109,
        OLYMPUS_IMAGE_PROCESSING_0X110A,
        OLYMPUS_IMAGE_PROCESSING_0X110B,
        OLYMPUS_IMAGE_PROCESSING_0X110C,
        OLYMPUS_IMAGE_PROCESSING_0X110D,
        OLYMPUS_IMAGE_PROCESSING_0X110E,
        OLYMPUS_IMAGE_PROCESSING_0X110F,
        OLYMPUS_IMAGE_PROCESSING_0X1110,
        ASPECT_RATIO,
        ASPECT_FRAME,
        OLYMPUS_IMAGE_PROCESSING_0X1114,
        OLYMPUS_IMAGE_PROCESSING_0X1115,
        OLYMPUS_IMAGE_PROCESSING_0X1116,
        OLYMPUS_IMAGE_PROCESSING_0X1117,
        OLYMPUS_IMAGE_PROCESSING_0X1118,
        OLYMPUS_IMAGE_PROCESSING_0X1119,
        OLYMPUS_IMAGE_PROCESSING_0X111A,
        OLYMPUS_IMAGE_PROCESSING_0X111B,
        OLYMPUS_IMAGE_PROCESSING_0X111D,
        OLYMPUS_IMAGE_PROCESSING_0X111E,
        OLYMPUS_IMAGE_PROCESSING_0X111F,
        OLYMPUS_IMAGE_PROCESSING_0X1120,
        OLYMPUS_IMAGE_PROCESSING_0X1121,
        OLYMPUS_IMAGE_PROCESSING_0X1122,
        OLYMPUS_IMAGE_PROCESSING_0X1123,
        OLYMPUS_IMAGE_PROCESSING_0X1124,
        OLYMPUS_IMAGE_PROCESSING_0X1125,
        OLYMPUS_IMAGE_PROCESSING_0X1126,
        OLYMPUS_IMAGE_PROCESSING_0X1127,
        FACES_DETECTED,
        FACE_DETECT_AREA,
        MAX_FACES,
        FACE_DETECT_FRAME_SIZE,
        OLYMPUS_IMAGE_PROCESSING_0X1204,
        OLYMPUS_IMAGE_PROCESSING_0X1205,
        OLYMPUS_IMAGE_PROCESSING_0X1206,
        FACE_DETECT_FRAME_CROP,
        OLYMPUS_IMAGE_PROCESSING_0X1208,
        OLYMPUS_IMAGE_PROCESSING_0X1209,
        OLYMPUS_IMAGE_PROCESSING_0X120A,
        OLYMPUS_IMAGE_PROCESSING_0X120B,
        OLYMPUS_IMAGE_PROCESSING_0X1300,
        OLYMPUS_IMAGE_PROCESSING_0X1301,
        OLYMPUS_IMAGE_PROCESSING_0X1302,
        OLYMPUS_IMAGE_PROCESSING_0X1303,
        OLYMPUS_IMAGE_PROCESSING_0X1304,
        OLYMPUS_IMAGE_PROCESSING_0X1305,
        CAMERA_TEMPERATURE,
        OLYMPUS_IMAGE_PROCESSING_0X1307,
        OLYMPUS_IMAGE_PROCESSING_0X1308,
        OLYMPUS_IMAGE_PROCESSING_0X1400,
        OLYMPUS_IMAGE_PROCESSING_0X1401,
        OLYMPUS_IMAGE_PROCESSING_0X1402,
        OLYMPUS_IMAGE_PROCESSING_0X1403,
        OLYMPUS_IMAGE_PROCESSING_0X1404,
        OLYMPUS_IMAGE_PROCESSING_0X1405,
        OLYMPUS_IMAGE_PROCESSING_0X1406,
        OLYMPUS_IMAGE_PROCESSING_0X1407,
        OLYMPUS_IMAGE_PROCESSING_0X1408,
        OLYMPUS_IMAGE_PROCESSING_0X1409,
        OLYMPUS_IMAGE_PROCESSING_0X1500,
        OLYMPUS_IMAGE_PROCESSING_0X1501,
        OLYMPUS_IMAGE_PROCESSING_0X1502,
        OLYMPUS_IMAGE_PROCESSING_0X1503,
        OLYMPUS_IMAGE_PROCESSING_0X1504,
        OLYMPUS_IMAGE_PROCESSING_0X1505,
        OLYMPUS_IMAGE_PROCESSING_0X1506,
        OLYMPUS_IMAGE_PROCESSING_0X1507,
        OLYMPUS_IMAGE_PROCESSING_0X1508,
        OLYMPUS_IMAGE_PROCESSING_0X1509,
        OLYMPUS_IMAGE_PROCESSING_0X150A,
        OLYMPUS_IMAGE_PROCESSING_0X150B,
        OLYMPUS_IMAGE_PROCESSING_0X150C,
        OLYMPUS_IMAGE_PROCESSING_0X150D,
        OLYMPUS_IMAGE_PROCESSING_0X150E,
        OLYMPUS_IMAGE_PROCESSING_0X150F,
        OLYMPUS_IMAGE_PROCESSING_0X1600,
        OLYMPUS_IMAGE_PROCESSING_0X1601,
        OLYMPUS_IMAGE_PROCESSING_0X1602,
        OLYMPUS_IMAGE_PROCESSING_0X1603,
        OLYMPUS_IMAGE_PROCESSING_0X1604,
        OLYMPUS_IMAGE_PROCESSING_0X1700,
        OLYMPUS_IMAGE_PROCESSING_0X1800,
        OLYMPUS_IMAGE_PROCESSING_0X1801,
        KEYSTONE_COMPENSATION,
        KEYSTONE_DIRECTION,
        KEYSTONE_VALUE,
        GND_FILTER_TYPE
    )
}
