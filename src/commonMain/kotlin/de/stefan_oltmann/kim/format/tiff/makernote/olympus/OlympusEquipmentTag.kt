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

/**
 * Tags of the Equipment maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Equipment
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object OlympusEquipmentTag {

    public val EQUIPMENT_VERSION: TagInfoByte = TagInfoByte(
        0x0, "EquipmentVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val CAMERA_TYPE2: TagInfoByte = TagInfoByte(
        0x100, "CameraType2",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val SERIAL_NUMBER: TagInfoByte = TagInfoByte(
        0x101, "SerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val INTERNAL_SERIAL_NUMBER: TagInfoByte = TagInfoByte(
        0x102, "InternalSerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val FOCAL_PLANE_DIAGONAL: TagInfoByte = TagInfoByte(
        0x103, "FocalPlaneDiagonal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val BODY_FIRMWARE_VERSION: TagInfoByte = TagInfoByte(
        0x104, "BodyFirmwareVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val LENS_TYPE: TagInfoByte = TagInfoByte(
        0x201, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val LENS_SERIAL_NUMBER: TagInfoByte = TagInfoByte(
        0x202, "LensSerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val LENS_MODEL: TagInfoByte = TagInfoByte(
        0x203, "LensModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val LENS_FIRMWARE_VERSION: TagInfoByte = TagInfoByte(
        0x204, "LensFirmwareVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val MAX_APERTURE_AT_MIN_FOCAL: TagInfoByte = TagInfoByte(
        0x205, "MaxApertureAtMinFocal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val MAX_APERTURE_AT_MAX_FOCAL: TagInfoByte = TagInfoByte(
        0x206, "MaxApertureAtMaxFocal",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val MIN_FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0x207, "MinFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val MAX_FOCAL_LENGTH: TagInfoByte = TagInfoByte(
        0x208, "MaxFocalLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val MAX_APERTURE: TagInfoByte = TagInfoByte(
        0x20a, "MaxAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val LENS_PROPERTIES: TagInfoByte = TagInfoByte(
        0x20b, "LensProperties",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val EXTENDER: TagInfoByte = TagInfoByte(
        0x301, "Extender",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val EXTENDER_SERIAL_NUMBER: TagInfoByte = TagInfoByte(
        0x302, "ExtenderSerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val EXTENDER_MODEL: TagInfoByte = TagInfoByte(
        0x303, "ExtenderModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val EXTENDER_FIRMWARE_VERSION: TagInfoByte = TagInfoByte(
        0x304, "ExtenderFirmwareVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val CONVERSION_LENS: TagInfoByte = TagInfoByte(
        0x403, "ConversionLens",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val FLASH_TYPE: TagInfoByte = TagInfoByte(
        0x1000, "FlashType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val FLASH_MODEL: TagInfoByte = TagInfoByte(
        0x1001, "FlashModel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val FLASH_FIRMWARE_VERSION: TagInfoByte = TagInfoByte(
        0x1002, "FlashFirmwareVersion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val FLASH_SERIAL_NUMBER: TagInfoByte = TagInfoByte(
        0x1003, "FlashSerialNumber",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT
    )

    public val ALL: List<TagInfo> = listOf(
        EQUIPMENT_VERSION,
        CAMERA_TYPE2,
        SERIAL_NUMBER,
        INTERNAL_SERIAL_NUMBER,
        FOCAL_PLANE_DIAGONAL,
        BODY_FIRMWARE_VERSION,
        LENS_TYPE,
        LENS_SERIAL_NUMBER,
        LENS_MODEL,
        LENS_FIRMWARE_VERSION,
        MAX_APERTURE_AT_MIN_FOCAL,
        MAX_APERTURE_AT_MAX_FOCAL,
        MIN_FOCAL_LENGTH,
        MAX_FOCAL_LENGTH,
        MAX_APERTURE,
        LENS_PROPERTIES,
        EXTENDER,
        EXTENDER_SERIAL_NUMBER,
        EXTENDER_MODEL,
        EXTENDER_FIRMWARE_VERSION,
        CONVERSION_LENS,
        FLASH_TYPE,
        FLASH_MODEL,
        FLASH_FIRMWARE_VERSION,
        FLASH_SERIAL_NUMBER
    )
}
