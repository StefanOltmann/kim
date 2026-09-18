/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.format.tiff.constant.DngTag
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag.EXIF_DIRECTORY_UNKNOWN
import de.stefan_oltmann.kim.format.tiff.constant.GeoTiffTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.PanasonicRawTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleRunTimeTag
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonAfInfo2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonAfMicroAdjTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonAmbienceTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonAspectInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1000DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DXTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DmkIIITag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DmkIINTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DmkIITag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo1DmkIVTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo40DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo450DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo500DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo50DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo550DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo5DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo5DmkIIITag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo5DmkIITag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo600DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo60DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo650DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo6DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo70DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo750DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo7DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfo80DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoG5XIITag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoPowerShot2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoR6Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoR6m2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoR6m3Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoUnknown32Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraInfoUnknownTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCameraSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCropInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions10DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions1DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions20DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions30DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions350DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions400DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctions5DTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonCustomFunctionsD30Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonFileInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonFilterInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonFocalLengthTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonHdrInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonLensInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonLightingOptTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonMeasuredColorTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonMultiExpTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonPanoramaTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonPictureStyleInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonProcessingTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonSensorInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonShotInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonTimeInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonVignettingCorr2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonVignettingCorrTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmAFCSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmDriveSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmFocusSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmPrioritySettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonAfInfo2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonColorBalance2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonColorBalance4Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonCustomSettingsD5100Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonDistortInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonFileInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonFlashInfo0103Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonFlashInfo0107Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonHdrInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonIsoInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonLensData0204Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonMultiExposureTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonPictureControl2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonPictureControlTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonRetouchInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonShotInfoD5100Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonShotInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonVrInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonWorldTimeTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusCameraSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusEquipmentTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFocusInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusImageProcessingTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusRawDevelopment2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusRawDevelopmentTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicFaceDetInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicFaceRecInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTimeInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyCameraInfo3Tag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyFaceInfoATag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMoreSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyTag
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo

internal object TiffTags {

    /* Note: Ordered to give EXIF tag names priority. */
    private val TIFF_AND_EXIF_TAGS = ExifTag.ALL + TiffTag.ALL + GeoTiffTag.ALL + DngTag.ALL + PanasonicRawTag.ALL

    private val TIFF_AND_EXIF_TAGS_MAP = TIFF_AND_EXIF_TAGS.groupByTo(mutableMapOf()) { it.tag }

    private val PANASONIC_RAW_TAGS_MAP = PanasonicRawTag.ALL.groupByTo(mutableMapOf()) { it.tag }

    /*
     * The Canon CameraInfo variants are model specific tables for the same
     * directory. One lookup searches all of them, with the unknown tables
     * as the fallback.
     */
    private val CANON_CAMERA_INFO_TAGS = listOf(
        CanonCameraInfo1DTag.ALL,
        CanonCameraInfo1DmkIITag.ALL,
        CanonCameraInfo1DmkIINTag.ALL,
        CanonCameraInfo1DmkIIITag.ALL,
        CanonCameraInfo1DmkIVTag.ALL,
        CanonCameraInfo1DXTag.ALL,
        CanonCameraInfo5DTag.ALL,
        CanonCameraInfo5DmkIITag.ALL,
        CanonCameraInfo5DmkIIITag.ALL,
        CanonCameraInfo6DTag.ALL,
        CanonCameraInfo7DTag.ALL,
        CanonCameraInfo40DTag.ALL,
        CanonCameraInfo50DTag.ALL,
        CanonCameraInfo60DTag.ALL,
        CanonCameraInfo70DTag.ALL,
        CanonCameraInfo80DTag.ALL,
        CanonCameraInfo450DTag.ALL,
        CanonCameraInfo500DTag.ALL,
        CanonCameraInfo550DTag.ALL,
        CanonCameraInfo600DTag.ALL,
        CanonCameraInfo650DTag.ALL,
        CanonCameraInfo750DTag.ALL,
        CanonCameraInfo1000DTag.ALL,
        CanonCameraInfoR6Tag.ALL,
        CanonCameraInfoR6m2Tag.ALL,
        CanonCameraInfoR6m3Tag.ALL,
        CanonCameraInfoG5XIITag.ALL,
        CanonCameraInfoPowerShot2Tag.ALL,
        CanonCameraInfoUnknown32Tag.ALL,
        CanonCameraInfoUnknownTag.ALL
    ).flatten()

    /*
     * The Canon CustomFunctions variants are model specific tables for the
     * same directory. One lookup searches all of them, with the version 2
     * table as the fallback.
     */
    private val CANON_CUSTOM_FUNCTIONS_TAGS = listOf(
        CanonCustomFunctions1DTag.ALL,
        CanonCustomFunctions5DTag.ALL,
        CanonCustomFunctions10DTag.ALL,
        CanonCustomFunctions20DTag.ALL,
        CanonCustomFunctions30DTag.ALL,
        CanonCustomFunctions350DTag.ALL,
        CanonCustomFunctions400DTag.ALL,
        CanonCustomFunctionsD30Tag.ALL,
        CanonCustomFunctions2Tag.ALL
    ).flatten()

    /*
     * Maps every directory type to the tag tables it resolves its tags
     * from. The tables of a directory are searched in list order, so the
     * first table that knows a tag wins. Variant tables of one directory
     * (model specific Canon tables, Nikon FlashInfo versions, ...) are
     * given as one concatenated table to preserve their entry order.
     */
    private val tagTablesByDirectoryType: Map<Int, List<Map<Int, List<TagInfo>>>> = mapOf(
        TiffConstants.TIFF_DIRECTORY_GPS to tagTables(GpsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON to tagTables(CanonTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON to tagTables(NikonTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM to tagTables(FujiFilmTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_APPLE to tagTables(AppleTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS to tagTables(OlympusTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS to tagTables(CanonCameraSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH to tagTables(CanonFocalLengthTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_SHOT_INFO to tagTables(CanonShotInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_PANORAMA to tagTables(CanonPanoramaTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_TIME_INFO to tagTables(CanonTimeInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_FILE_INFO to tagTables(CanonFileInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_PROCESSING_INFO to tagTables(CanonProcessingTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_CROP_INFO to tagTables(CanonCropInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_ASPECT_INFO to tagTables(CanonAspectInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_MEASURED_COLOR to tagTables(CanonMeasuredColorTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ to tagTables(CanonAfMicroAdjTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR to tagTables(CanonVignettingCorrTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2 to tagTables(CanonVignettingCorr2Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_LIGHTING_OPT to tagTables(CanonLightingOptTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_LENS_INFO to tagTables(CanonLensInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO to tagTables(CanonAmbienceTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_MULTI_EXP to tagTables(CanonMultiExpTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_HDR_INFO to tagTables(CanonHdrInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_AF_INFO2 to tagTables(CanonAfInfo2Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_SENSOR_INFO to tagTables(CanonSensorInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_FILTER_INFO to tagTables(CanonFilterInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO to tagTables(CanonPictureStyleInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO to tagTables(CANON_CAMERA_INFO_TAGS),
        TiffConstants.TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS to tagTables(CANON_CUSTOM_FUNCTIONS_TAGS),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_VR_INFO to tagTables(NikonVrInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_WORLD_TIME to tagTables(NikonWorldTimeTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_ISO_INFO to tagTables(NikonIsoInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_DISTORT_INFO to tagTables(NikonDistortInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_HDR_INFO to tagTables(NikonHdrInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE to tagTables(NikonMultiExposureTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_FILE_INFO to tagTables(NikonFileInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO to tagTables(NikonRetouchInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO to
            tagTables(NikonFlashInfo0103Tag.ALL + NikonFlashInfo0107Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_AF_INFO2 to tagTables(NikonAfInfo2Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL to
            tagTables(NikonPictureControlTag.ALL + NikonPictureControl2Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_LENS_DATA to tagTables(NikonLensData0204Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS to tagTables(NikonCustomSettingsD5100Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE to
            tagTables(NikonColorBalance2Tag.ALL + NikonColorBalance4Tag.ALL),
        /* The D5100 variant is only consulted for tags the regular table does not know. */
        TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO to
            tagTables(NikonShotInfoTag.ALL, NikonShotInfoD5100Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC to tagTables(PanasonicTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO to tagTables(PanasonicFaceDetInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO to tagTables(PanasonicFaceRecInfoTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_TIME_INFO to tagTables(PanasonicTimeInfoTag.ALL),
        /* The Sony variants share a large part of their tag tables. */
        TiffConstants.TIFF_MAKER_NOTE_SONY to tagTables(SonyTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_SONY5 to tagTables(SonyTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON to tagTables(SonyTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_INFO3 to tagTables(SonyCameraInfo3Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_SETTINGS to tagTables(SonyMoreSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_SONY_FACE_INFO to tagTables(SonyFaceInfoATag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME to tagTables(AppleRunTimeTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS to tagTables(FujiFilmPrioritySettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS to tagTables(FujiFilmFocusSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS to tagTables(FujiFilmAFCSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS to tagTables(FujiFilmDriveSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT to tagTables(OlympusEquipmentTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS to tagTables(OlympusCameraSettingsTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT to tagTables(OlympusRawDevelopmentTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEV_2 to tagTables(OlympusRawDevelopment2Tag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING to tagTables(OlympusImageProcessingTag.ALL),
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO to tagTables(OlympusFocusInfoTag.ALL),
        /* The Olympus AF info lives in the regular Olympus table. */
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_AF_INFO to tagTables(OlympusTag.ALL)
    )

    /**
     * Groups the given tag lists into lookup tables, one per list.
     *
     * Multiple lists mean a fallback order: the first table that knows a
     * tag provides its candidates. A single call with one list is the
     * common case of a directory with one tag table.
     */
    private fun tagTables(vararg tagLists: List<TagInfo>): List<Map<Int, List<TagInfo>>> =
        tagLists.map { tags -> tags.groupBy { it.tag } }

    /*
     * Note: Keep in sync with ImageMetadata.findTiffField()
     */
    fun getTag(
        directoryType: Int,
        tag: Int,
        preferPanasonicRawTags: Boolean = false
    ): TagInfo? {

        val tables = tagTablesByDirectoryType[directoryType]

        /*
         * GPS and Maker Notes should be exact matches.
         */
        val possibleMatches: List<TagInfo>? = when {
            preferPanasonicRawTags && directoryType == TiffConstants.TIFF_DIRECTORY_TYPE_IFD0 ->
                PANASONIC_RAW_TAGS_MAP[tag] ?: TIFF_AND_EXIF_TAGS_MAP[tag]

            tables != null -> tables.firstNotNullOfOrNull { it[tag] }

            else -> TIFF_AND_EXIF_TAGS_MAP[tag]
        }

        possibleMatches ?: return null

        return getTag(directoryType, possibleMatches)
    }

    /*
     * Note: Keep in sync with ImageMetadata.findTiffField()
     */
    @Suppress("UnnecessaryParentheses")
    private fun getTag(directoryType: Int, possibleMatches: List<TagInfo>): TagInfo? {

        val exactMatch = possibleMatches.firstOrNull { tagInfo ->
            tagInfo.directoryType?.typeId == directoryType &&
                tagInfo.directoryType != EXIF_DIRECTORY_UNKNOWN
        }

        if (exactMatch != null)
            return exactMatch

        val inexactMatch = possibleMatches.firstOrNull { tagInfo ->
            val isImageDirectory = tagInfo.directoryType?.isImageDirectory ?: false
            val lookupIsImageDirectory =
                directoryType >= 0 ||
                    directoryType == TiffConstants.TIFF_DIRECTORY_EXIF ||
                    directoryType == TiffConstants.TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD
            lookupIsImageDirectory == isImageDirectory
        }

        if (inexactMatch != null)
            return inexactMatch

        val wildcardMatch = possibleMatches.firstOrNull { tagInfo ->
            tagInfo.directoryType == EXIF_DIRECTORY_UNKNOWN
        }

        if (wildcardMatch != null)
            return wildcardMatch

        return null
    }
}
