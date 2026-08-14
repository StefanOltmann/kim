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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Tags of the FocusInfo maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Olympus.html#FocusInfo
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object OlympusFocusInfoTag {

    public val FOCUS_INFO_VERSION: TagInfoByte = TagInfoByte(
        0x0, "FocusInfoVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val AUTO_FOCUS: TagInfoByte = TagInfoByte(
        0x209, "AutoFocus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val SCENE_DETECT: TagInfoByte = TagInfoByte(
        0x210, "SceneDetect",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val SCENE_AREA: TagInfoByte = TagInfoByte(
        0x211, "SceneArea",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val SCENE_DETECT_DATA: TagInfoByte = TagInfoByte(
        0x212, "SceneDetectData",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val ZOOM_STEP_COUNT: TagInfoByte = TagInfoByte(
        0x300, "ZoomStepCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val FOCUS_STEP_COUNT: TagInfoByte = TagInfoByte(
        0x301, "FocusStepCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val FOCUS_STEP_INFINITY: TagInfoByte = TagInfoByte(
        0x303, "FocusStepInfinity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val FOCUS_STEP_NEAR: TagInfoByte = TagInfoByte(
        0x304, "FocusStepNear",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val FOCUS_DISTANCE: TagInfoByte = TagInfoByte(
        0x305, "FocusDistance",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val AF_POINT: TagInfoByte = TagInfoByte(
        0x308, "AFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val AF_POINT_DETAILS: TagInfoByte = TagInfoByte(
        0x31b, "AFPointDetails",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val AF_INFO: TagInfoByte = TagInfoByte(
        0x328, "AFInfo",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val EXTERNAL_FLASH: TagInfoByte = TagInfoByte(
        0x1201, "ExternalFlash",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val EXTERNAL_FLASH_GUIDE_NUMBER: TagInfoByte = TagInfoByte(
        0x1203, "ExternalFlashGuideNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val EXTERNAL_FLASH_BOUNCE: TagInfoByte = TagInfoByte(
        0x1204, "ExternalFlashBounce",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val EXTERNAL_FLASH_ZOOM: TagInfoByte = TagInfoByte(
        0x1205, "ExternalFlashZoom",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val INTERNAL_FLASH: TagInfoByte = TagInfoByte(
        0x1208, "InternalFlash",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val MANUAL_FLASH: TagInfoByte = TagInfoByte(
        0x1209, "ManualFlash",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val MACRO_LED: TagInfoByte = TagInfoByte(
        0x120a, "MacroLED",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val SENSOR_TEMPERATURE: TagInfoByte = TagInfoByte(
        0x1500, "SensorTemperature",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val IMAGE_STABILIZATION: TagInfoByte = TagInfoByte(
        0x1600, "ImageStabilization",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    /**
     * The anti-shock waiting time setting of the camera.
     */
    public val ANTI_SHOCK_WAITING_TIME: TagInfoShort = TagInfoShort(
        0x2100, "AntiShockWaitingTime",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0200: TagInfoSShorts = TagInfoSShorts(
        0x0200, "Olympus_FocusInfo_0x0200",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0201: TagInfoSShorts = TagInfoSShorts(
        0x0201, "Olympus_FocusInfo_0x0201",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0202: TagInfoSShorts = TagInfoSShorts(
        0x0202, "Olympus_FocusInfo_0x0202",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0203: TagInfoSShorts = TagInfoSShorts(
        0x0203, "Olympus_FocusInfo_0x0203",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0204: TagInfoSShorts = TagInfoSShorts(
        0x0204, "Olympus_FocusInfo_0x0204",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0205: TagInfoSShort = TagInfoSShort(
        0x0205, "Olympus_FocusInfo_0x0205",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0206: TagInfoSShort = TagInfoSShort(
        0x0206, "Olympus_FocusInfo_0x0206",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0207: TagInfoShort = TagInfoShort(
        0x0207, "Olympus_FocusInfo_0x0207",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020A: TagInfoShort = TagInfoShort(
        0x020a, "Olympus_FocusInfo_0x020a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020B: TagInfoShort = TagInfoShort(
        0x020b, "Olympus_FocusInfo_0x020b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020C: TagInfoSShorts = TagInfoSShorts(
        0x020c, "Olympus_FocusInfo_0x020c",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020D: TagInfoLong = TagInfoLong(
        0x020d, "Olympus_FocusInfo_0x020d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020E: TagInfoLongs = TagInfoLongs(
        0x020e, "Olympus_FocusInfo_0x020e",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X020F: TagInfoLongs = TagInfoLongs(
        0x020f, "Olympus_FocusInfo_0x020f",
        5,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0213: TagInfoShort = TagInfoShort(
        0x0213, "Olympus_FocusInfo_0x0213",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0214: TagInfoShort = TagInfoShort(
        0x0214, "Olympus_FocusInfo_0x0214",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0215: TagInfoShort = TagInfoShort(
        0x0215, "Olympus_FocusInfo_0x0215",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0219: TagInfoSShorts = TagInfoSShorts(
        0x0219, "Olympus_FocusInfo_0x0219",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X021A: TagInfoSShorts = TagInfoSShorts(
        0x021a, "Olympus_FocusInfo_0x021a",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X021B: TagInfoSShorts = TagInfoSShorts(
        0x021b, "Olympus_FocusInfo_0x021b",
        3,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0302: TagInfoShort = TagInfoShort(
        0x0302, "Olympus_FocusInfo_0x0302",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0307: TagInfoShort = TagInfoShort(
        0x0307, "Olympus_FocusInfo_0x0307",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0309: TagInfoShort = TagInfoShort(
        0x0309, "Olympus_FocusInfo_0x0309",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030A: TagInfoSShorts = TagInfoSShorts(
        0x030a, "Olympus_FocusInfo_0x030a",
        20,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030B: TagInfoShort = TagInfoShort(
        0x030b, "Olympus_FocusInfo_0x030b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030C: TagInfoShorts = TagInfoShorts(
        0x030c, "Olympus_FocusInfo_0x030c",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030D: TagInfoShorts = TagInfoShorts(
        0x030d, "Olympus_FocusInfo_0x030d",
        22,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030E: TagInfoShort = TagInfoShort(
        0x030e, "Olympus_FocusInfo_0x030e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X030F: TagInfoSShorts = TagInfoSShorts(
        0x030f, "Olympus_FocusInfo_0x030f",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0310: TagInfoShort = TagInfoShort(
        0x0310, "Olympus_FocusInfo_0x0310",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0311: TagInfoShort = TagInfoShort(
        0x0311, "Olympus_FocusInfo_0x0311",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0312: TagInfoShorts = TagInfoShorts(
        0x0312, "Olympus_FocusInfo_0x0312",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0313: TagInfoSShort = TagInfoSShort(
        0x0313, "Olympus_FocusInfo_0x0313",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0314: TagInfoShort = TagInfoShort(
        0x0314, "Olympus_FocusInfo_0x0314",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0315: TagInfoSShort = TagInfoSShort(
        0x0315, "Olympus_FocusInfo_0x0315",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0316: TagInfoSShorts = TagInfoSShorts(
        0x0316, "Olympus_FocusInfo_0x0316",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0318: TagInfoShort = TagInfoShort(
        0x0318, "Olympus_FocusInfo_0x0318",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0319: TagInfoShort = TagInfoShort(
        0x0319, "Olympus_FocusInfo_0x0319",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X031A: TagInfoShorts = TagInfoShorts(
        0x031a, "Olympus_FocusInfo_0x031a",
        84,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X031C: TagInfoSLongs = TagInfoSLongs(
        0x031c, "Olympus_FocusInfo_0x031c",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X031D: TagInfoSLongs = TagInfoSLongs(
        0x031d, "Olympus_FocusInfo_0x031d",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X031E: TagInfoSLongs = TagInfoSLongs(
        0x031e, "Olympus_FocusInfo_0x031e",
        TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0320: TagInfoShorts = TagInfoShorts(
        0x0320, "Olympus_FocusInfo_0x0320",
        16,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0321: TagInfoSShort = TagInfoSShort(
        0x0321, "Olympus_FocusInfo_0x0321",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0322: TagInfoSShort = TagInfoSShort(
        0x0322, "Olympus_FocusInfo_0x0322",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0323: TagInfoSShort = TagInfoSShort(
        0x0323, "Olympus_FocusInfo_0x0323",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0324: TagInfoSShort = TagInfoSShort(
        0x0324, "Olympus_FocusInfo_0x0324",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0325: TagInfoShorts = TagInfoShorts(
        0x0325, "Olympus_FocusInfo_0x0325",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0326: TagInfoShorts = TagInfoShorts(
        0x0326, "Olympus_FocusInfo_0x0326",
        8,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0327: TagInfoShort = TagInfoShort(
        0x0327, "Olympus_FocusInfo_0x0327",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0329: TagInfoUndefineds = TagInfoUndefineds(
        0x0329, "Olympus_FocusInfo_0x0329",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X032A: TagInfoSShort = TagInfoSShort(
        0x032a, "Olympus_FocusInfo_0x032a",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X032B: TagInfoSShort = TagInfoSShort(
        0x032b, "Olympus_FocusInfo_0x032b",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X032C: TagInfoSShort = TagInfoSShort(
        0x032c, "Olympus_FocusInfo_0x032c",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X032D: TagInfoByte = TagInfoByte(
        0x032d, "Olympus_FocusInfo_0x032d",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X032E: TagInfoLong = TagInfoLong(
        0x032e, "Olympus_FocusInfo_0x032e",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X0402: TagInfoShort = TagInfoShort(
        0x0402, "Olympus_FocusInfo_0x0402",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1200: TagInfoShort = TagInfoShort(
        0x1200, "Olympus_FocusInfo_0x1200",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1202: TagInfoShort = TagInfoShort(
        0x1202, "Olympus_FocusInfo_0x1202",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1206: TagInfoShort = TagInfoShort(
        0x1206, "Olympus_FocusInfo_0x1206",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1207: TagInfoShort = TagInfoShort(
        0x1207, "Olympus_FocusInfo_0x1207",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1501: TagInfoLongs = TagInfoLongs(
        0x1501, "Olympus_FocusInfo_0x1501",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1502: TagInfoShort = TagInfoShort(
        0x1502, "Olympus_FocusInfo_0x1502",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1700: TagInfoBytes = TagInfoBytes(
        0x1700, "Olympus_FocusInfo_0x1700",
        4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1800: TagInfoLong = TagInfoLong(
        0x1800, "Olympus_FocusInfo_0x1800",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1801: TagInfoLong = TagInfoLong(
        0x1801, "Olympus_FocusInfo_0x1801",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1802: TagInfoLongs = TagInfoLongs(
        0x1802, "Olympus_FocusInfo_0x1802",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val OLYMPUS_FOCUS_INFO_0X1900: TagInfoShorts = TagInfoShorts(
        0x1900, "Olympus_FocusInfo_0x1900",
        2,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO
    )

    public val ALL: List<TagInfo> = listOf(
        FOCUS_INFO_VERSION,
        OLYMPUS_FOCUS_INFO_0X0200,
        OLYMPUS_FOCUS_INFO_0X0201,
        OLYMPUS_FOCUS_INFO_0X0202,
        OLYMPUS_FOCUS_INFO_0X0203,
        OLYMPUS_FOCUS_INFO_0X0204,
        OLYMPUS_FOCUS_INFO_0X0205,
        OLYMPUS_FOCUS_INFO_0X0206,
        OLYMPUS_FOCUS_INFO_0X0207,
        AUTO_FOCUS,
        OLYMPUS_FOCUS_INFO_0X020A,
        OLYMPUS_FOCUS_INFO_0X020B,
        OLYMPUS_FOCUS_INFO_0X020C,
        OLYMPUS_FOCUS_INFO_0X020D,
        OLYMPUS_FOCUS_INFO_0X020E,
        OLYMPUS_FOCUS_INFO_0X020F,
        SCENE_DETECT,
        SCENE_AREA,
        SCENE_DETECT_DATA,
        OLYMPUS_FOCUS_INFO_0X0213,
        OLYMPUS_FOCUS_INFO_0X0214,
        OLYMPUS_FOCUS_INFO_0X0215,
        OLYMPUS_FOCUS_INFO_0X0219,
        OLYMPUS_FOCUS_INFO_0X021A,
        OLYMPUS_FOCUS_INFO_0X021B,
        ZOOM_STEP_COUNT,
        FOCUS_STEP_COUNT,
        OLYMPUS_FOCUS_INFO_0X0302,
        FOCUS_STEP_INFINITY,
        FOCUS_STEP_NEAR,
        FOCUS_DISTANCE,
        OLYMPUS_FOCUS_INFO_0X0307,
        AF_POINT,
        OLYMPUS_FOCUS_INFO_0X0309,
        OLYMPUS_FOCUS_INFO_0X030A,
        OLYMPUS_FOCUS_INFO_0X030B,
        OLYMPUS_FOCUS_INFO_0X030C,
        OLYMPUS_FOCUS_INFO_0X030D,
        OLYMPUS_FOCUS_INFO_0X030E,
        OLYMPUS_FOCUS_INFO_0X030F,
        OLYMPUS_FOCUS_INFO_0X0310,
        OLYMPUS_FOCUS_INFO_0X0311,
        OLYMPUS_FOCUS_INFO_0X0312,
        OLYMPUS_FOCUS_INFO_0X0313,
        OLYMPUS_FOCUS_INFO_0X0314,
        OLYMPUS_FOCUS_INFO_0X0315,
        OLYMPUS_FOCUS_INFO_0X0316,
        OLYMPUS_FOCUS_INFO_0X0318,
        OLYMPUS_FOCUS_INFO_0X0319,
        OLYMPUS_FOCUS_INFO_0X031A,
        AF_POINT_DETAILS,
        OLYMPUS_FOCUS_INFO_0X031C,
        OLYMPUS_FOCUS_INFO_0X031D,
        OLYMPUS_FOCUS_INFO_0X031E,
        OLYMPUS_FOCUS_INFO_0X0320,
        OLYMPUS_FOCUS_INFO_0X0321,
        OLYMPUS_FOCUS_INFO_0X0322,
        OLYMPUS_FOCUS_INFO_0X0323,
        OLYMPUS_FOCUS_INFO_0X0324,
        OLYMPUS_FOCUS_INFO_0X0325,
        OLYMPUS_FOCUS_INFO_0X0326,
        OLYMPUS_FOCUS_INFO_0X0327,
        AF_INFO,
        OLYMPUS_FOCUS_INFO_0X0329,
        OLYMPUS_FOCUS_INFO_0X032A,
        OLYMPUS_FOCUS_INFO_0X032B,
        OLYMPUS_FOCUS_INFO_0X032C,
        OLYMPUS_FOCUS_INFO_0X032D,
        OLYMPUS_FOCUS_INFO_0X032E,
        OLYMPUS_FOCUS_INFO_0X0402,
        OLYMPUS_FOCUS_INFO_0X1200,
        EXTERNAL_FLASH,
        OLYMPUS_FOCUS_INFO_0X1202,
        EXTERNAL_FLASH_GUIDE_NUMBER,
        EXTERNAL_FLASH_BOUNCE,
        EXTERNAL_FLASH_ZOOM,
        OLYMPUS_FOCUS_INFO_0X1206,
        OLYMPUS_FOCUS_INFO_0X1207,
        INTERNAL_FLASH,
        MANUAL_FLASH,
        MACRO_LED,
        SENSOR_TEMPERATURE,
        OLYMPUS_FOCUS_INFO_0X1501,
        OLYMPUS_FOCUS_INFO_0X1502,
        IMAGE_STABILIZATION,
        OLYMPUS_FOCUS_INFO_0X1700,
        OLYMPUS_FOCUS_INFO_0X1800,
        OLYMPUS_FOCUS_INFO_0X1801,
        OLYMPUS_FOCUS_INFO_0X1802,
        OLYMPUS_FOCUS_INFO_0X1900,
        ANTI_SHOCK_WAITING_TIME
    )
}
