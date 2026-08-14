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
    private val GPS_TAGS_MAP = GpsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_TAGS_MAP = CanonTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_TAGS_MAP = NikonTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val FUJIFILM_TAGS_MAP = FujiFilmTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val APPLE_TAGS_MAP = AppleTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_TAGS_MAP = OlympusTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_EQUIPMENT_TAGS_MAP = OlympusEquipmentTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_CAMERA_SETTINGS_TAGS_MAP = OlympusCameraSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_RAW_DEVELOPMENT_TAGS_MAP = OlympusRawDevelopmentTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_RAW_DEV_2_TAGS_MAP = OlympusRawDevelopment2Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_IMAGE_PROCESSING_TAGS_MAP = OlympusImageProcessingTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val OLYMPUS_FOCUS_INFO_TAGS_MAP = OlympusFocusInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val PANASONIC_TAGS_MAP = PanasonicTag.ALL.groupByTo(mutableMapOf()) { it.tag }

    /*
     * The Sony variants share a large part of their tag tables,
     * so they are all resolved from the same map.
     */
    private val SONY_TAGS_MAP = SonyTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_CAMERA_SETTINGS_TAGS_MAP = CanonCameraSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_FOCAL_LENGTH_TAGS_MAP = CanonFocalLengthTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_SHOT_INFO_TAGS_MAP = CanonShotInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_PANORAMA_TAGS_MAP = CanonPanoramaTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_TIME_INFO_TAGS_MAP = CanonTimeInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_FILE_INFO_TAGS_MAP = CanonFileInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_PROCESSING_INFO_TAGS_MAP = CanonProcessingTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_CROP_INFO_TAGS_MAP = CanonCropInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_ASPECT_INFO_TAGS_MAP = CanonAspectInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_MEASURED_COLOR_TAGS_MAP = CanonMeasuredColorTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_AF_MICRO_ADJ_TAGS_MAP = CanonAfMicroAdjTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_VIGNETTING_CORR_TAGS_MAP = CanonVignettingCorrTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_VIGNETTING_CORR2_TAGS_MAP = CanonVignettingCorr2Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_LIGHTING_OPT_TAGS_MAP = CanonLightingOptTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_LENS_INFO_TAGS_MAP = CanonLensInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_AMBIENCE_INFO_TAGS_MAP = CanonAmbienceTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_MULTI_EXP_TAGS_MAP = CanonMultiExpTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_HDR_INFO_TAGS_MAP = CanonHdrInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_VR_INFO_TAGS_MAP = NikonVrInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_WORLD_TIME_TAGS_MAP = NikonWorldTimeTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_ISO_INFO_TAGS_MAP = NikonIsoInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_DISTORT_INFO_TAGS_MAP = NikonDistortInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_HDR_INFO_TAGS_MAP = NikonHdrInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_MULTI_EXPOSURE_TAGS_MAP = NikonMultiExposureTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_FILE_INFO_TAGS_MAP = NikonFileInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_RETOUCH_INFO_TAGS_MAP = NikonRetouchInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_SHOT_INFO_TAGS_MAP = NikonShotInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val PANASONIC_FACE_DET_INFO_TAGS_MAP = PanasonicFaceDetInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val PANASONIC_FACE_REC_INFO_TAGS_MAP = PanasonicFaceRecInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val PANASONIC_TIME_INFO_TAGS_MAP = PanasonicTimeInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val SONY_CAMERA_INFO3_TAGS_MAP = SonyCameraInfo3Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val APPLE_RUN_TIME_TAGS_MAP = AppleRunTimeTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_AF_INFO2_TAGS_MAP = CanonAfInfo2Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_SENSOR_INFO_TAGS_MAP = CanonSensorInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_FILTER_INFO_TAGS_MAP = CanonFilterInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_PICTURE_STYLE_INFO_TAGS_MAP = CanonPictureStyleInfoTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_CAMERA_INFO_TAGS_MAP = listOf(
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
    ).flatten().groupByTo(mutableMapOf()) { it.tag }
    private val CANON_CUSTOM_FUNCTIONS_TAGS_MAP = listOf(
        CanonCustomFunctions1DTag.ALL,
        CanonCustomFunctions5DTag.ALL,
        CanonCustomFunctions10DTag.ALL,
        CanonCustomFunctions20DTag.ALL,
        CanonCustomFunctions30DTag.ALL,
        CanonCustomFunctions350DTag.ALL,
        CanonCustomFunctions400DTag.ALL,
        CanonCustomFunctionsD30Tag.ALL,
        CanonCustomFunctions2Tag.ALL
    ).flatten().groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_FLASH_INFO_TAGS_MAP =
        (NikonFlashInfo0103Tag.ALL + NikonFlashInfo0107Tag.ALL).groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_AF_INFO2_TAGS_MAP = NikonAfInfo2Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_PICTURE_CONTROL_TAGS_MAP =
        (NikonPictureControlTag.ALL + NikonPictureControl2Tag.ALL).groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_LENS_DATA_TAGS_MAP = NikonLensData0204Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_CUSTOM_SETTINGS_TAGS_MAP = NikonCustomSettingsD5100Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_COLOR_BALANCE_TAGS_MAP =
        (NikonColorBalance2Tag.ALL + NikonColorBalance4Tag.ALL).groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_SHOT_INFO_D5100_TAGS_MAP = NikonShotInfoD5100Tag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val SONY_MORE_SETTINGS_TAGS_MAP = SonyMoreSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val SONY_FACE_INFO_TAGS_MAP = SonyFaceInfoATag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val FUJIFILM_PRIORITY_SETTINGS_TAGS_MAP =
        FujiFilmPrioritySettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val FUJIFILM_FOCUS_SETTINGS_TAGS_MAP =
        FujiFilmFocusSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val FUJIFILM_AFC_SETTINGS_TAGS_MAP =
        FujiFilmAFCSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val FUJIFILM_DRIVE_SETTINGS_TAGS_MAP =
        FujiFilmDriveSettingsTag.ALL.groupByTo(mutableMapOf()) { it.tag }

    fun getTag(directoryType: Int, tag: Int): TagInfo? {

        /*
         * GPS and Maker Notes should be exact matches.
         */
        @Suppress("UseIfInsteadOfWhen")
        val possibleMatches = when (directoryType) {
            TiffConstants.TIFF_DIRECTORY_GPS -> GPS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON -> CANON_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON -> NIKON_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_FUJIFILM -> FUJIFILM_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_APPLE -> APPLE_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS -> OLYMPUS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS -> CANON_CAMERA_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH -> CANON_FOCAL_LENGTH_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_SHOT_INFO -> CANON_SHOT_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_PANORAMA -> CANON_PANORAMA_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_TIME_INFO -> CANON_TIME_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_FILE_INFO -> CANON_FILE_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_PROCESSING_INFO -> CANON_PROCESSING_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_CROP_INFO -> CANON_CROP_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_ASPECT_INFO -> CANON_ASPECT_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_MEASURED_COLOR -> CANON_MEASURED_COLOR_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ -> CANON_AF_MICRO_ADJ_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR -> CANON_VIGNETTING_CORR_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2 -> CANON_VIGNETTING_CORR2_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_LIGHTING_OPT -> CANON_LIGHTING_OPT_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_LENS_INFO -> CANON_LENS_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO -> CANON_AMBIENCE_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_MULTI_EXP -> CANON_MULTI_EXP_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_HDR_INFO -> CANON_HDR_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_VR_INFO -> NIKON_VR_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_WORLD_TIME -> NIKON_WORLD_TIME_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_ISO_INFO -> NIKON_ISO_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_DISTORT_INFO -> NIKON_DISTORT_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_HDR_INFO -> NIKON_HDR_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE -> NIKON_MULTI_EXPOSURE_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_FILE_INFO -> NIKON_FILE_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO -> NIKON_RETOUCH_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO -> PANASONIC_FACE_DET_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO -> PANASONIC_FACE_REC_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_PANASONIC_TIME_INFO -> PANASONIC_TIME_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_INFO3 -> SONY_CAMERA_INFO3_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME -> APPLE_RUN_TIME_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_AF_INFO2 -> CANON_AF_INFO2_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_SENSOR_INFO -> CANON_SENSOR_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_FILTER_INFO -> CANON_FILTER_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO -> CANON_PICTURE_STYLE_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO -> CANON_CAMERA_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS -> CANON_CUSTOM_FUNCTIONS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO -> NIKON_FLASH_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_AF_INFO2 -> NIKON_AF_INFO2_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL -> NIKON_PICTURE_CONTROL_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_LENS_DATA -> NIKON_LENS_DATA_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS -> NIKON_CUSTOM_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE -> NIKON_COLOR_BALANCE_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO ->
                NIKON_SHOT_INFO_TAGS_MAP[tag] ?: NIKON_SHOT_INFO_D5100_TAGS_MAP[tag]

            TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_SETTINGS -> SONY_MORE_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_SONY_FACE_INFO -> SONY_FACE_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS -> FUJIFILM_PRIORITY_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS -> FUJIFILM_FOCUS_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS -> FUJIFILM_AFC_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS -> FUJIFILM_DRIVE_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT -> OLYMPUS_EQUIPMENT_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS -> OLYMPUS_CAMERA_SETTINGS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT -> OLYMPUS_RAW_DEVELOPMENT_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEV_2 -> OLYMPUS_RAW_DEV_2_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING -> OLYMPUS_IMAGE_PROCESSING_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO -> OLYMPUS_FOCUS_INFO_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_AF_INFO -> OLYMPUS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_PANASONIC -> PANASONIC_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_SONY,
            TiffConstants.TIFF_MAKER_NOTE_SONY5,
            TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON -> SONY_TAGS_MAP[tag]

            else -> TIFF_AND_EXIF_TAGS_MAP[tag]
        } ?: return null

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





