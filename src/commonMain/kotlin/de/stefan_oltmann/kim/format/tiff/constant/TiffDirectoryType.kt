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
package de.stefan_oltmann.kim.format.tiff.constant

/**
 * The type of a TIFF directory.
 */
public enum class TiffDirectoryType(
    public val typeId: Int,
    public val displayName: String,
    public val isImageDirectory: Boolean
) {

    TIFF_DIRECTORY_IFD0(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD0, "IFD0", true
    ),
    TIFF_DIRECTORY_IFD1(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD1, "IFD1", true
    ),
    TIFF_DIRECTORY_IFD2(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD2, "IFD2", true
    ),
    TIFF_DIRECTORY_IFD3(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD3, "IFD3", true
    ),
    EXIF_DIRECTORY_INTEROP_IFD(
        TiffConstants.TIFF_DIRECTORY_INTEROP, "InteropIFD", false
    ),
    EXIF_DIRECTORY_EXIF_IFD(
        TiffConstants.TIFF_DIRECTORY_EXIF, "ExifIFD", true
    ),
    EXIF_DIRECTORY_GPS(
        TiffConstants.TIFF_DIRECTORY_GPS, "GPS", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON(
        TiffConstants.TIFF_MAKER_NOTE_CANON, "MakerNoteCanon", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS, "MakerNoteCanonCameraSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_FOCAL_LENGTH(
        TiffConstants.TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH, "MakerNoteCanonFocalLength", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_SHOT_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_SHOT_INFO, "MakerNoteCanonShotInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_PANORAMA(
        TiffConstants.TIFF_MAKER_NOTE_CANON_PANORAMA, "MakerNoteCanonPanorama", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_TIME_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_TIME_INFO, "MakerNoteCanonTimeInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_FILE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_FILE_INFO, "MakerNoteCanonFileInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_PROCESSING_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_PROCESSING_INFO, "MakerNoteCanonProcessingInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS(
        TiffConstants.TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS, "MakerNoteCanonCustomFunctions", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_CAMERA_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO, "MakerNoteCanonCameraInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_INFO2(
        TiffConstants.TIFF_MAKER_NOTE_CANON_AF_INFO2, "MakerNoteCanonAFInfo2", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_CROP_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_CROP_INFO, "MakerNoteCanonCropInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_ASPECT_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_ASPECT_INFO, "MakerNoteCanonAspectInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_MEASURED_COLOR(
        TiffConstants.TIFF_MAKER_NOTE_CANON_MEASURED_COLOR, "MakerNoteCanonMeasuredColor", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_SENSOR_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_SENSOR_INFO, "MakerNoteCanonSensorInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_AF_MICRO_ADJ(
        TiffConstants.TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ, "MakerNoteCanonAFMicroAdj", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR(
        TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR, "MakerNoteCanonVignettingCorr", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_VIGNETTING_CORR2(
        TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2, "MakerNoteCanonVignettingCorr2", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_LIGHTING_OPT(
        TiffConstants.TIFF_MAKER_NOTE_CANON_LIGHTING_OPT, "MakerNoteCanonLightingOpt", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_LENS_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_LENS_INFO, "MakerNoteCanonLensInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_AMBIENCE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO, "MakerNoteCanonAmbienceInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_MULTI_EXP(
        TiffConstants.TIFF_MAKER_NOTE_CANON_MULTI_EXP, "MakerNoteCanonMultiExp", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_FILTER_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_FILTER_INFO, "MakerNoteCanonFilterInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_HDR_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_HDR_INFO, "MakerNoteCanonHDRInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON(
        TiffConstants.TIFF_MAKER_NOTE_NIKON, "MakerNoteNikon", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_PREVIEW_IFD(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD, "MakerNoteNikonPreviewIFD", true
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_FLASH_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO, "MakerNoteNikonFlashInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_VR_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_VR_INFO, "MakerNoteNikonVRInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_WORLD_TIME(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_WORLD_TIME, "MakerNoteNikonWorldTime", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_ISO_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_ISO_INFO, "MakerNoteNikonISOInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_DISTORT_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_DISTORT_INFO, "MakerNoteNikonDistortInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_SHOT_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO, "MakerNoteNikonShotInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_COLOR_BALANCE(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE, "MakerNoteNikonColorBalance", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_MULTI_EXPOSURE(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE, "MakerNoteNikonMultiExposure", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_AF_INFO2(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_AF_INFO2, "MakerNoteNikonAFInfo2", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_FILE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_FILE_INFO, "MakerNoteNikonFileInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_RETOUCH_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO, "MakerNoteNikonRetouchInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_HDR_INFO(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_HDR_INFO, "MakerNoteNikonHDRInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_PICTURE_CONTROL(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL, "MakerNoteNikonPictureControl", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_LENS_DATA(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_LENS_DATA, "MakerNoteNikonLensData", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON_CUSTOM_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS, "MakerNoteNikonCustomSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON_PICTURE_STYLE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO, "MakerNoteCanonPictureStyleInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_SETTINGS, "MakerNoteSonyMoreSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_FACE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_SONY_FACE_INFO, "MakerNoteSonyFaceInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS, "MakerNoteFujiFilmPrioritySettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS, "MakerNoteFujiFilmFocusSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_AFC_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS, "MakerNoteFujiFilmAFCSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS, "MakerNoteFujiFilmDriveSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM(
        TiffConstants.TIFF_MAKER_NOTE_FUJIFILM, "MakerNoteFujiFilm", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_APPLE(
        TiffConstants.TIFF_MAKER_NOTE_APPLE, "MakerNoteApple", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_APPLE_RUN_TIME(
        TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME, "MakerNoteAppleRunTime", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_PENTAX(
        TiffConstants.TIFF_MAKER_NOTE_PENTAX, "MakerNotePentax", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_RICOH(
        TiffConstants.TIFF_MAKER_NOTE_RICOH, "MakerNoteRicoh", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SIGMA(
        TiffConstants.TIFF_MAKER_NOTE_SIGMA, "MakerNoteSigma", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_LEICA(
        TiffConstants.TIFF_MAKER_NOTE_LEICA, "MakerNoteLeica", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS, "MakerNoteOlympus", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_EQUIPMENT(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT, "MakerNoteOlympusEquipment", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS, "MakerNoteOlympusCameraSettings", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT, "MakerNoteOlympusRawDevelopment", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_RAW_DEV_2(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEV_2, "MakerNoteOlympusRawDev2", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING, "MakerNoteOlympusImageProcessing", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_FOCUS_INFO(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO, "MakerNoteOlympusFocusInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_OLYMPUS_AF_INFO(
        TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_AF_INFO, "MakerNoteOlympusAFInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_PANASONIC(
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC, "MakerNotePanasonic", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_DET_INFO(
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO, "MakerNotePanasonicFaceDetInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_FACE_REC_INFO(
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO, "MakerNotePanasonicFaceRecInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_PANASONIC_TIME_INFO(
        TiffConstants.TIFF_MAKER_NOTE_PANASONIC_TIME_INFO, "MakerNotePanasonicTimeInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY(
        TiffConstants.TIFF_MAKER_NOTE_SONY, "MakerNoteSony", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY5(
        TiffConstants.TIFF_MAKER_NOTE_SONY5, "MakerNoteSony5", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_ERICSSON(
        TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON, "MakerNoteSonyEricsson", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_INFO3(
        TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_INFO3, "MakerNoteSonyCameraInfo3", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_MORE_INFO(
        TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_INFO, "MakerNoteSonyMoreInfo", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_CAMERA_SETTINGS3(
        TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_SETTINGS3, "MakerNoteSonyCameraSettings3", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_EXTRA_INFO3(
        TiffConstants.TIFF_MAKER_NOTE_SONY_EXTRA_INFO3, "MakerNoteSonyExtraInfo3", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_SONY_TAG_900B(
        TiffConstants.TIFF_MAKER_NOTE_SONY_TAG_900B, "MakerNoteSonyTag900b", false
    );

    override fun toString(): String =
        displayName
}

