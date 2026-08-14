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
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * The tags found in IFD0 of Panasonic/Leica RAW, RW2 and RWL images.
 *
 * See https://exiftool.sourceforge.net/TagNames/PanasonicRaw.html
 *
 * The CameraIFD and DistortionInfo sub-directories of the main table
 * are not interpreted.
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object PanasonicRawTag {

    /**
     * The version of the Panasonic raw format.
     */
    public val PANASONIC_RAW_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "PanasonicRawVersion", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The width of the camera sensor.
     */
    public val SENSOR_WIDTH: TagInfoShort = TagInfoShort(
        0x0002, "SensorWidth",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The height of the camera sensor.
     */
    public val SENSOR_HEIGHT: TagInfoShort = TagInfoShort(
        0x0003, "SensorHeight",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The top border of the active sensor area.
     */
    public val SENSOR_TOP_BORDER: TagInfoShort = TagInfoShort(
        0x0004, "SensorTopBorder",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The left border of the active sensor area.
     */
    public val SENSOR_LEFT_BORDER: TagInfoShort = TagInfoShort(
        0x0005, "SensorLeftBorder",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The bottom border of the active sensor area.
     */
    public val SENSOR_BOTTOM_BORDER: TagInfoShort = TagInfoShort(
        0x0006, "SensorBottomBorder",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The right border of the active sensor area.
     */
    public val SENSOR_RIGHT_BORDER: TagInfoShort = TagInfoShort(
        0x0007, "SensorRightBorder",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The number of samples per pixel.
     */
    public val SAMPLES_PER_PIXEL: TagInfoShort = TagInfoShort(
        0x0008, "SamplesPerPixel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The color filter array pattern of the sensor.
     */
    public val CFA_PATTERN: TagInfoShort = TagInfoShort(
        0x0009, "CFAPattern",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The bit depth of the raw samples.
     */
    public val BITS_PER_SAMPLE: TagInfoShort = TagInfoShort(
        0x000a, "BitsPerSample",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The compression of the raw data.
     */
    public val COMPRESSION: TagInfoShort = TagInfoShort(
        0x000b, "Compression",
        TIFF_DIRECTORY_IFD0
    )

    public val PANASONIC_RAW_0X000D: TagInfoShort = TagInfoShort(
        0x000d, "PanasonicRaw_0x000d",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The linearity limit of the red channel.
     */
    public val LINEARITY_LIMIT_RED: TagInfoShort = TagInfoShort(
        0x000e, "LinearityLimitRed",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The linearity limit of the green channel.
     */
    public val LINEARITY_LIMIT_GREEN: TagInfoShort = TagInfoShort(
        0x000f, "LinearityLimitGreen",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The linearity limit of the blue channel.
     */
    public val LINEARITY_LIMIT_BLUE: TagInfoShort = TagInfoShort(
        0x0010, "LinearityLimitBlue",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The red balance of Digilux 2 raw images.
     */
    public val RED_BALANCE: TagInfoShort = TagInfoShort(
        0x0011, "RedBalance",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The blue balance of Digilux 2 raw images.
     */
    public val BLUE_BALANCE: TagInfoShort = TagInfoShort(
        0x0012, "BlueBalance",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The white balance information of the shot.
     */
    public val WB_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0013, "WBInfo", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The ISO of the raw image, stored as a 16-bit value.
     */
    public val ISO: TagInfoShort = TagInfoShort(
        0x0017, "ISO",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The high ISO multiplier of the red channel.
     */
    public val HIGH_ISO_MULTIPLIER_RED: TagInfoShort = TagInfoShort(
        0x0018, "HighISOMultiplierRed",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The high ISO multiplier of the green channel.
     */
    public val HIGH_ISO_MULTIPLIER_GREEN: TagInfoShort = TagInfoShort(
        0x0019, "HighISOMultiplierGreen",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The high ISO multiplier of the blue channel.
     */
    public val HIGH_ISO_MULTIPLIER_BLUE: TagInfoShort = TagInfoShort(
        0x001a, "HighISOMultiplierBlue",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The camera's default noise reduction setup.
     */
    public val NOISE_REDUCTION_PARAMS: TagInfoUndefineds = TagInfoUndefineds(
        0x001b, "NoiseReductionParams", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The black level of the red channel.
     */
    public val BLACK_LEVEL_RED: TagInfoShort = TagInfoShort(
        0x001c, "BlackLevelRed",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The black level of the green channel.
     */
    public val BLACK_LEVEL_GREEN: TagInfoShort = TagInfoShort(
        0x001d, "BlackLevelGreen",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The black level of the blue channel.
     */
    public val BLACK_LEVEL_BLUE: TagInfoShort = TagInfoShort(
        0x001e, "BlackLevelBlue",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The white balance level of the red channel.
     */
    public val WB_RED_LEVEL: TagInfoShort = TagInfoShort(
        0x0024, "WBRedLevel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The white balance level of the green channel.
     */
    public val WB_GREEN_LEVEL: TagInfoShort = TagInfoShort(
        0x0025, "WBGreenLevel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The white balance level of the blue channel.
     */
    public val WB_BLUE_LEVEL: TagInfoShort = TagInfoShort(
        0x0026, "WBBlueLevel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The extended white balance information of the shot.
     */
    public val WB_INFO_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0027, "WBInfo2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The format of the raw data.
     */
    public val RAW_FORMAT: TagInfoShort = TagInfoShort(
        0x002d, "RawFormat",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The JPEG preview embedded in the raw file.
     */
    public val JPG_FROM_RAW: TagInfoUndefineds = TagInfoUndefineds(
        0x002e, "JpgFromRaw", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The top edge of the cropped raw area.
     */
    public val CROP_TOP: TagInfoShort = TagInfoShort(
        0x002f, "CropTop",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The left edge of the cropped raw area.
     */
    public val CROP_LEFT: TagInfoShort = TagInfoShort(
        0x0030, "CropLeft",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The bottom edge of the cropped raw area.
     */
    public val CROP_BOTTOM: TagInfoShort = TagInfoShort(
        0x0031, "CropBottom",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The right edge of the cropped raw area.
     */
    public val CROP_RIGHT: TagInfoShort = TagInfoShort(
        0x0032, "CropRight",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The ISO of the raw image, stored as a 32-bit value.
     */
    public val ISO_2: TagInfoLong = TagInfoLong(
        0x0037, "ISO",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The manufacturer of the camera.
     */
    public val MAKE: TagInfoAscii = TagInfoAscii(
        0x010f, "Make", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The model of the camera.
     */
    public val MODEL: TagInfoAscii = TagInfoAscii(
        0x0110, "Model", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The offset of the raw image data.
     */
    public val STRIP_OFFSETS: TagInfoLong = TagInfoLong(
        0x0111, "StripOffsets",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    /**
     * The orientation of the image.
     */
    public val ORIENTATION: TagInfoShort = TagInfoShort(
        0x0112, "Orientation",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The number of rows per strip.
     */
    public val ROWS_PER_STRIP: TagInfoLong = TagInfoLong(
        0x0116, "RowsPerStrip",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The length of the raw image data.
     */
    public val STRIP_BYTE_COUNTS: TagInfoLong = TagInfoLong(
        0x0117, "StripByteCounts",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The offset of the raw data.
     */
    public val RAW_DATA_OFFSET: TagInfoLong = TagInfoLong(
        0x0118, "RawDataOffset",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    /**
     * The distortion correction information of the lens.
     */
    public val DISTORTION_INFO: TagInfoUndefineds = TagInfoUndefineds(
        0x0119, "DistortionInfo", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The gamma of the raw data.
     */
    public val GAMMA: TagInfoShort = TagInfoShort(
        0x011c, "Gamma",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The camera IFD pointer with further camera settings.
     */
    public val CAMERA_IFD: TagInfoUndefineds = TagInfoUndefineds(
        0x0120, "CameraIFD", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The multishot mode of the capture.
     */
    public val MULTISHOT: TagInfoLong = TagInfoLong(
        0x0121, "Multishot",
        TIFF_DIRECTORY_IFD0
    )

    public val JPG_FROM_RAW_2: TagInfoUndefineds = TagInfoUndefineds(
        0x0127, "JpgFromRaw2", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The artist of the image.
     */
    public val ARTIST: TagInfoAscii = TagInfoAscii(
        0x013b, "Artist", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The XMP application notes.
     */
    public val APPLICATION_NOTES: TagInfoUndefineds = TagInfoUndefineds(
        0x02bc, "ApplicationNotes", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The copyright notice of the image.
     */
    public val COPYRIGHT: TagInfoAscii = TagInfoAscii(
        0x8298, "Copyright", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The IPTC-NAA record of the image.
     */
    public val IPTC_NAA: TagInfoUndefineds = TagInfoUndefineds(
        0x83bb, "IPTC-NAA", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The offset of the Exif IFD.
     */
    public val EXIF_OFFSET: TagInfoLong = TagInfoLong(
        0x8769, "ExifOffset",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    /**
     * The offset of the GPS IFD.
     */
    public val GPS_INFO: TagInfoLong = TagInfoLong(
        0x8825, "GPSInfo",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    public val ALL: List<TagInfo> = listOf(
        PANASONIC_RAW_VERSION, SENSOR_WIDTH, SENSOR_HEIGHT, SENSOR_TOP_BORDER,
        SENSOR_LEFT_BORDER, SENSOR_BOTTOM_BORDER, SENSOR_RIGHT_BORDER,
        SAMPLES_PER_PIXEL, CFA_PATTERN, BITS_PER_SAMPLE, COMPRESSION,
        PANASONIC_RAW_0X000D,
        LINEARITY_LIMIT_RED, LINEARITY_LIMIT_GREEN, LINEARITY_LIMIT_BLUE,
        RED_BALANCE, BLUE_BALANCE, WB_INFO, ISO, HIGH_ISO_MULTIPLIER_RED,
        HIGH_ISO_MULTIPLIER_GREEN, HIGH_ISO_MULTIPLIER_BLUE,
        NOISE_REDUCTION_PARAMS, BLACK_LEVEL_RED, BLACK_LEVEL_GREEN,
        BLACK_LEVEL_BLUE, WB_RED_LEVEL, WB_GREEN_LEVEL, WB_BLUE_LEVEL,
        WB_INFO_2, RAW_FORMAT, JPG_FROM_RAW, CROP_TOP, CROP_LEFT, CROP_BOTTOM,
        CROP_RIGHT, ISO_2, MAKE, MODEL, STRIP_OFFSETS, ORIENTATION,
        ROWS_PER_STRIP, STRIP_BYTE_COUNTS, RAW_DATA_OFFSET, DISTORTION_INFO,
        GAMMA, CAMERA_IFD, MULTISHOT, JPG_FROM_RAW_2, ARTIST,
        APPLICATION_NOTES, COPYRIGHT, IPTC_NAA, EXIF_OFFSET, GPS_INFO
    )
}
