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

import de.stefan_oltmann.kim.common.ByteOrder

/**
 * Defines constants for internal elements from TIFF files and for allowing
 * applications to define parameters for reading and writing TIFF files.
 */
@Suppress("UnderscoresInNumericLiterals")
public object TiffConstants {

    public const val TIFF_VERSION: Int = 42

    /*
     * ExifTool defaults to big endian.
     * It's more natural to read.
     */
    public val DEFAULT_TIFF_BYTE_ORDER: ByteOrder = ByteOrder.BIG_ENDIAN

    public const val TIFF_HEADER_SIZE: Int = 8
    public const val TIFF_DIRECTORY_HEADER_LENGTH: Int = 2
    public const val TIFF_DIRECTORY_FOOTER_LENGTH: Int = 4
    public const val TIFF_ENTRY_LENGTH: Int = 12
    public const val TIFF_ENTRY_MAX_VALUE_LENGTH: Int = 4

    /** Root directory. */
    public const val TIFF_DIRECTORY_TYPE_IFD0: Int = 0

    /** Thumbnail directory. */
    public const val TIFF_DIRECTORY_TYPE_IFD1: Int = 1

    public const val TIFF_DIRECTORY_TYPE_IFD2: Int = 2
    public const val TIFF_DIRECTORY_TYPE_IFD3: Int = 3

    public const val EXIF_SUB_IFD1: Int = 2
    public const val EXIF_SUB_IFD2: Int = 3
    public const val EXIF_SUB_IFD3: Int = 4

    public const val TIFF_DIRECTORY_EXIF: Int = -2
    public const val TIFF_DIRECTORY_GPS: Int = -3
    public const val TIFF_DIRECTORY_INTEROP: Int = -4

    public const val DIRECTORY_TYPE_UNKNOWN: Int = -1

    /* Artificial MakerNote directores */
    public const val TIFF_MAKER_NOTE_CANON: Int = -101
    public const val TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS: Int = -102
    public const val TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH: Int = -103
    public const val TIFF_MAKER_NOTE_CANON_SHOT_INFO: Int = -104
    public const val TIFF_MAKER_NOTE_CANON_PANORAMA: Int = -105
    public const val TIFF_MAKER_NOTE_CANON_TIME_INFO: Int = -106
    public const val TIFF_MAKER_NOTE_CANON_FILE_INFO: Int = -107
    public const val TIFF_MAKER_NOTE_CANON_PROCESSING_INFO: Int = -108
    public const val TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS: Int = -109
    public const val TIFF_MAKER_NOTE_CANON_CAMERA_INFO: Int = -110
    public const val TIFF_MAKER_NOTE_CANON_AF_INFO2: Int = -111
    public const val TIFF_MAKER_NOTE_CANON_CROP_INFO: Int = -112
    public const val TIFF_MAKER_NOTE_CANON_ASPECT_INFO: Int = -113
    public const val TIFF_MAKER_NOTE_CANON_MEASURED_COLOR: Int = -114
    public const val TIFF_MAKER_NOTE_CANON_SENSOR_INFO: Int = -115
    public const val TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ: Int = -116
    public const val TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR: Int = -117
    public const val TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2: Int = -118
    public const val TIFF_MAKER_NOTE_CANON_LIGHTING_OPT: Int = -119
    public const val TIFF_MAKER_NOTE_CANON_LENS_INFO: Int = -120
    public const val TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO: Int = -121
    public const val TIFF_MAKER_NOTE_CANON_MULTI_EXP: Int = -122
    public const val TIFF_MAKER_NOTE_CANON_FILTER_INFO: Int = -123
    public const val TIFF_MAKER_NOTE_CANON_HDR_INFO: Int = -124

    public const val TIFF_MAKER_NOTE_NIKON: Int = -125
    public const val TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD: Int = -126
    public const val TIFF_MAKER_NOTE_NIKON_FLASH_INFO: Int = -127
    public const val TIFF_MAKER_NOTE_NIKON_VR_INFO: Int = -128
    public const val TIFF_MAKER_NOTE_NIKON_WORLD_TIME: Int = -129
    public const val TIFF_MAKER_NOTE_NIKON_ISO_INFO: Int = -130
    public const val TIFF_MAKER_NOTE_NIKON_DISTORT_INFO: Int = -131
    public const val TIFF_MAKER_NOTE_NIKON_SHOT_INFO: Int = -132
    public const val TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE: Int = -133
    public const val TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE: Int = -134
    public const val TIFF_MAKER_NOTE_NIKON_AF_INFO2: Int = -135
    public const val TIFF_MAKER_NOTE_NIKON_FILE_INFO: Int = -136
    public const val TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO: Int = -137
    public const val TIFF_MAKER_NOTE_NIKON_HDR_INFO: Int = -138

    public const val TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL: Int = -161
    public const val TIFF_MAKER_NOTE_NIKON_LENS_DATA: Int = -162
    public const val TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS: Int = -163
    public const val TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO: Int = -164
    public const val TIFF_MAKER_NOTE_SONY_MORE_SETTINGS: Int = -165
    public const val TIFF_MAKER_NOTE_SONY_FACE_INFO: Int = -166
    public const val TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS: Int = -167
    public const val TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS: Int = -168
    public const val TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS: Int = -169
    public const val TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS: Int = -170

    public const val TIFF_MAKER_NOTE_FUJIFILM: Int = -139

    public const val TIFF_MAKER_NOTE_APPLE: Int = -140
    public const val TIFF_MAKER_NOTE_APPLE_RUN_TIME: Int = -171
    public const val TIFF_MAKER_NOTE_PENTAX: Int = -172
    public const val TIFF_MAKER_NOTE_RICOH: Int = -173
    public const val TIFF_MAKER_NOTE_SIGMA: Int = -174
    public const val TIFF_MAKER_NOTE_LEICA: Int = -175

    public const val TIFF_MAKER_NOTE_OLYMPUS: Int = -141
    public const val TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT: Int = -142
    public const val TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS: Int = -143
    public const val TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT: Int = -144
    public const val TIFF_MAKER_NOTE_OLYMPUS_RAW_DEV_2: Int = -145
    public const val TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING: Int = -146
    public const val TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO: Int = -147
    public const val TIFF_MAKER_NOTE_OLYMPUS_AF_INFO: Int = -148

    public const val TIFF_MAKER_NOTE_PANASONIC: Int = -149
    public const val TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO: Int = -150
    public const val TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO: Int = -151
    public const val TIFF_MAKER_NOTE_PANASONIC_TIME_INFO: Int = -152

    public const val TIFF_MAKER_NOTE_SONY: Int = -153
    public const val TIFF_MAKER_NOTE_SONY5: Int = -154
    public const val TIFF_MAKER_NOTE_SONY_ERICSSON: Int = -155
    public const val TIFF_MAKER_NOTE_SONY_CAMERA_INFO3: Int = -156
    public const val TIFF_MAKER_NOTE_SONY_MORE_INFO: Int = -157
    public const val TIFF_MAKER_NOTE_SONY_CAMERA_SETTINGS3: Int = -158
    public const val TIFF_MAKER_NOTE_SONY_EXTRA_INFO3: Int = -159
    public const val TIFF_MAKER_NOTE_SONY_TAG_900B: Int = -160

    public const val FIELD_TYPE_BYTE_INDEX: Int = 1
    public const val FIELD_TYPE_ASCII_INDEX: Int = 2
    public const val FIELD_TYPE_SHORT_INDEX: Int = 3
    public const val FIELD_TYPE_LONG_INDEX: Int = 4
    public const val FIELD_TYPE_RATIONAL_INDEX: Int = 5
    public const val FIELD_TYPE_SBYTE_INDEX: Int = 6
    public const val FIELD_TYPE_UNDEFINED_INDEX: Int = 7
    public const val FIELD_TYPE_SSHORT_INDEX: Int = 8
    public const val FIELD_TYPE_SLONG_INDEX: Int = 9
    public const val FIELD_TYPE_SRATIONAL_INDEX: Int = 10
    public const val FIELD_TYPE_FLOAT_INDEX: Int = 11
    public const val FIELD_TYPE_DOUBLE_INDEX: Int = 12
    public const val FIELD_TYPE_IFD_INDEX: Int = 13
    public const val FIELD_TYPE_INT64_INDEX: Int = 16
}

