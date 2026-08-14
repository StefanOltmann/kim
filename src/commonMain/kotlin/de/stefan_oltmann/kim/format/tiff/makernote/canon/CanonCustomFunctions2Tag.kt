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
package de.stefan_oltmann.kim.format.tiff.makernote.canon

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte

/**
 * Tags of the Functions2 maker note sub-directory.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html#Functions2
 */
@Suppress("MagicNumber", "StringLiteralDuplication", "MaxLineLength")
public object CanonCustomFunctions2Tag {

    public val EXPOSURE_LEVEL_INCREMENTS: TagInfoByte = TagInfoByte(
        0x101, "ExposureLevelIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ISO_SPEED_INCREMENTS: TagInfoByte = TagInfoByte(
        0x102, "ISOSpeedIncrements",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ISO_SPEED_RANGE: TagInfoByte = TagInfoByte(
        0x103, "ISOSpeedRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_AUTO_CANCEL: TagInfoByte = TagInfoByte(
        0x104, "AEBAutoCancel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_SEQUENCE: TagInfoByte = TagInfoByte(
        0x105, "AEBSequence",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AEB_SHOT_COUNT: TagInfoByte = TagInfoByte(
        0x106, "AEBShotCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SPOT_METER_LINK_TO_AF_POINT: TagInfoByte = TagInfoByte(
        0x107, "SpotMeterLinkToAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SAFETY_SHIFT: TagInfoByte = TagInfoByte(
        0x108, "SafetyShift",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val USABLE_SHOOTING_MODES: TagInfoByte = TagInfoByte(
        0x109, "UsableShootingModes",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val USABLE_METERING_MODES: TagInfoByte = TagInfoByte(
        0x10a, "UsableMeteringModes",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_MODE_IN_MANUAL: TagInfoByte = TagInfoByte(
        0x10b, "ExposureModeInManual",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_SPEED_RANGE: TagInfoByte = TagInfoByte(
        0x10c, "ShutterSpeedRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val APERTURE_RANGE: TagInfoByte = TagInfoByte(
        0x10d, "ApertureRange",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val APPLY_SHOOTING_METERING_MODE: TagInfoByte = TagInfoByte(
        0x10e, "ApplyShootingMeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_SYNC_SPEED_AV: TagInfoByte = TagInfoByte(
        0x10f, "FlashSyncSpeedAv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AE_MICROADJUSTMENT: TagInfoByte = TagInfoByte(
        0x110, "AEMicroadjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FE_MICROADJUSTMENT: TagInfoByte = TagInfoByte(
        0x111, "FEMicroadjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SAME_EXPOSURE_FOR_NEW_APERTURE: TagInfoByte = TagInfoByte(
        0x112, "SameExposureForNewAperture",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val EXPOSURE_COMP_AUTO_CANCEL: TagInfoByte = TagInfoByte(
        0x113, "ExposureCompAutoCancel",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AE_LOCK_METER_MODE_AFTER_FOCUS: TagInfoByte = TagInfoByte(
        0x114, "AELockMeterModeAfterFocus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LONG_EXPOSURE_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x201, "LongExposureNoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val HIGH_ISO_NOISE_REDUCTION: TagInfoByte = TagInfoByte(
        0x202, "HighISONoiseReduction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val HIGHLIGHT_TONE_PRIORITY: TagInfoByte = TagInfoByte(
        0x203, "HighlightTonePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AUTO_LIGHTING_OPTIMIZER: TagInfoByte = TagInfoByte(
        0x204, "AutoLightingOptimizer",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ETTLII: TagInfoByte = TagInfoByte(
        0x304, "ETTLII",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_CURTAIN_SYNC: TagInfoByte = TagInfoByte(
        0x305, "ShutterCurtainSync",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_FIRING: TagInfoByte = TagInfoByte(
        0x306, "FlashFiring",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val VIEW_INFO_DURING_EXPOSURE: TagInfoByte = TagInfoByte(
        0x407, "ViewInfoDuringExposure",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LCD_ILLUMINATION_DURING_BULB: TagInfoByte = TagInfoByte(
        0x408, "LCDIlluminationDuringBulb",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val INFO_BUTTON_WHEN_SHOOTING: TagInfoByte = TagInfoByte(
        0x409, "InfoButtonWhenShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val VIEWFINDER_WARNINGS: TagInfoByte = TagInfoByte(
        0x40a, "ViewfinderWarnings",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LV_SHOOTING_AREA_DISPLAY: TagInfoByte = TagInfoByte(
        0x40b, "LVShootingAreaDisplay",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LV_SHOOTING_AREA_DISPLAY_2: TagInfoByte = TagInfoByte(
        0x40c, "LVShootingAreaDisplay",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val USM_LENS_ELECTRONIC_MF: TagInfoByte = TagInfoByte(
        0x501, "USMLensElectronicMF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_TRACKING_SENSITIVITY: TagInfoByte = TagInfoByte(
        0x502, "AIServoTrackingSensitivity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_IMAGE_PRIORITY: TagInfoByte = TagInfoByte(
        0x503, "AIServoImagePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_TRACKING_METHOD: TagInfoByte = TagInfoByte(
        0x504, "AIServoTrackingMethod",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LENS_DRIVE_NO_AF: TagInfoByte = TagInfoByte(
        0x505, "LensDriveNoAF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LENS_AF_STOP_BUTTON: TagInfoByte = TagInfoByte(
        0x506, "LensAFStopButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_MICROADJUSTMENT: TagInfoByte = TagInfoByte(
        0x507, "AFMicroadjustment",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_AREA_EXPANSION: TagInfoByte = TagInfoByte(
        0x508, "AFPointAreaExpansion",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SELECTABLE_AF_POINT: TagInfoByte = TagInfoByte(
        0x509, "SelectableAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SWITCH_TO_REGISTERED_AF_POINT: TagInfoByte = TagInfoByte(
        0x50a, "SwitchToRegisteredAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_AUTO_SELECTION: TagInfoByte = TagInfoByte(
        0x50b, "AFPointAutoSelection",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_DISPLAY_DURING_FOCUS: TagInfoByte = TagInfoByte(
        0x50c, "AFPointDisplayDuringFocus",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_BRIGHTNESS: TagInfoByte = TagInfoByte(
        0x50d, "AFPointBrightness",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_ASSIST_BEAM: TagInfoByte = TagInfoByte(
        0x50e, "AFAssistBeam",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_POINT_SELECTION_METHOD: TagInfoByte = TagInfoByte(
        0x50f, "AFPointSelectionMethod",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val VF_DISPLAY_ILLUMINATION: TagInfoByte = TagInfoByte(
        0x510, "VFDisplayIllumination",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_DURING_LIVE_VIEW: TagInfoByte = TagInfoByte(
        0x511, "AFDuringLiveView",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SELECT_AF_AREA_SELECT_MODE: TagInfoByte = TagInfoByte(
        0x512, "SelectAFAreaSelectMode",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MANUAL_AF_POINT_SELECT_PATTERN: TagInfoByte = TagInfoByte(
        0x513, "ManualAFPointSelectPattern",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val DISPLAY_ALL_AF_POINTS: TagInfoByte = TagInfoByte(
        0x514, "DisplayAllAFPoints",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FOCUS_DISPLAY_AI_SERVO_AND_MF: TagInfoByte = TagInfoByte(
        0x515, "FocusDisplayAIServoAndMF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ORIENTATION_LINKED_AF_POINT: TagInfoByte = TagInfoByte(
        0x516, "OrientationLinkedAFPoint",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MULTI_CONTROLLER_WHILE_METERING: TagInfoByte = TagInfoByte(
        0x517, "MultiControllerWhileMetering",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ACCELERATION_TRACKING: TagInfoByte = TagInfoByte(
        0x518, "AccelerationTracking",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_FIRST_IMAGE_PRIORITY: TagInfoByte = TagInfoByte(
        0x519, "AIServoFirstImagePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AI_SERVO_SECOND_IMAGE_PRIORITY: TagInfoByte = TagInfoByte(
        0x51a, "AIServoSecondImagePriority",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_AREA_SELECT_METHOD: TagInfoByte = TagInfoByte(
        0x51b, "AFAreaSelectMethod",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AUTO_AF_POINT_COLOR_TRACKING: TagInfoByte = TagInfoByte(
        0x51c, "AutoAFPointColorTracking",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val VF_DISPLAY_ILLUMINATION_2: TagInfoByte = TagInfoByte(
        0x51d, "VFDisplayIllumination",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val INITIAL_AF_POINT_AI_SERVO_AF: TagInfoByte = TagInfoByte(
        0x51e, "InitialAFPointAIServoAF",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MIRROR_LOCKUP: TagInfoByte = TagInfoByte(
        0x60f, "MirrorLockup",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val CONTINUOUS_SHOOTING_SPEED: TagInfoByte = TagInfoByte(
        0x610, "ContinuousShootingSpeed",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val CONTINUOUS_SHOT_LIMIT: TagInfoByte = TagInfoByte(
        0x611, "ContinuousShotLimit",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val RESTRICT_DRIVE_MODES: TagInfoByte = TagInfoByte(
        0x612, "RestrictDriveModes",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_AE_LOCK: TagInfoByte = TagInfoByte(
        0x701, "Shutter-AELock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AF_ON_AE_LOCK_BUTTON_SWITCH: TagInfoByte = TagInfoByte(
        0x702, "AFOnAELockButtonSwitch",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val QUICK_CONTROL_DIAL_IN_METER: TagInfoByte = TagInfoByte(
        0x703, "QuickControlDialInMeter",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SET_BUTTON_WHEN_SHOOTING: TagInfoByte = TagInfoByte(
        0x704, "SetButtonWhenShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MANUAL_TV: TagInfoByte = TagInfoByte(
        0x705, "ManualTv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val DIAL_DIRECTION_TV_AV: TagInfoByte = TagInfoByte(
        0x706, "DialDirectionTvAv",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AV_SETTING_WITHOUT_LENS: TagInfoByte = TagInfoByte(
        0x707, "AvSettingWithoutLens",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val WB_MEDIA_IMAGE_SIZE_SETTING: TagInfoByte = TagInfoByte(
        0x708, "WBMediaImageSizeSetting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LOCK_MICROPHONE_BUTTON: TagInfoByte = TagInfoByte(
        0x709, "LockMicrophoneButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val BUTTON_FUNCTION_CONTROL_OFF: TagInfoByte = TagInfoByte(
        0x70a, "ButtonFunctionControlOff",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ASSIGN_FUNC_BUTTON: TagInfoByte = TagInfoByte(
        0x70b, "AssignFuncButton",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val CUSTOM_CONTROLS: TagInfoByte = TagInfoByte(
        0x70c, "CustomControls",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val START_MOVIE_SHOOTING: TagInfoByte = TagInfoByte(
        0x70d, "StartMovieShooting",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FLASH_BUTTON_FUNCTION: TagInfoByte = TagInfoByte(
        0x70e, "FlashButtonFunction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MULTI_FUNCTION_LOCK: TagInfoByte = TagInfoByte(
        0x70f, "MultiFunctionLock",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val TRASH_BUTTON_FUNCTION: TagInfoByte = TagInfoByte(
        0x710, "TrashButtonFunction",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHUTTER_RELEASE_WITHOUT_LENS: TagInfoByte = TagInfoByte(
        0x711, "ShutterReleaseWithoutLens",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val CONTROL_RING_ROTATION: TagInfoByte = TagInfoByte(
        0x712, "ControlRingRotation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FOCUS_RING_ROTATION: TagInfoByte = TagInfoByte(
        0x713, "FocusRingRotation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val RF_LENS_MF_FOCUS_RING_SENSITIVITY: TagInfoByte = TagInfoByte(
        0x714, "RFLensMFFocusRingSensitivity",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val CUSTOMIZE_DIALS: TagInfoByte = TagInfoByte(
        0x715, "CustomizeDials",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val FOCUSING_SCREEN: TagInfoByte = TagInfoByte(
        0x80b, "FocusingScreen",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val TIMER_LENGTH: TagInfoByte = TagInfoByte(
        0x80c, "TimerLength",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val SHORT_RELEASE_TIME_LAG: TagInfoByte = TagInfoByte(
        0x80d, "ShortReleaseTimeLag",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ADD_ASPECT_RATIO_INFO: TagInfoByte = TagInfoByte(
        0x80e, "AddAspectRatioInfo",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ADD_ORIGINAL_DECISION_DATA: TagInfoByte = TagInfoByte(
        0x80f, "AddOriginalDecisionData",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LIVE_VIEW_EXPOSURE_SIMULATION: TagInfoByte = TagInfoByte(
        0x810, "LiveViewExposureSimulation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val LCD_DISPLAY_AT_POWER_ON: TagInfoByte = TagInfoByte(
        0x811, "LCDDisplayAtPowerOn",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val MEMO_AUDIO_QUALITY: TagInfoByte = TagInfoByte(
        0x812, "MemoAudioQuality",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val DEFAULT_ERASE_OPTION: TagInfoByte = TagInfoByte(
        0x813, "DefaultEraseOption",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val RETRACT_LENS_ON_POWER_OFF: TagInfoByte = TagInfoByte(
        0x814, "RetractLensOnPowerOff",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ADD_IPTC_INFORMATION: TagInfoByte = TagInfoByte(
        0x815, "AddIPTCInformation",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val AUDIO_COMPRESSION: TagInfoByte = TagInfoByte(
        0x816, "AudioCompression",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
    )

    public val ALL: List<TagInfo> = listOf(
        EXPOSURE_LEVEL_INCREMENTS,
        ISO_SPEED_INCREMENTS,
        ISO_SPEED_RANGE,
        AEB_AUTO_CANCEL,
        AEB_SEQUENCE,
        AEB_SHOT_COUNT,
        SPOT_METER_LINK_TO_AF_POINT,
        SAFETY_SHIFT,
        USABLE_SHOOTING_MODES,
        USABLE_METERING_MODES,
        EXPOSURE_MODE_IN_MANUAL,
        SHUTTER_SPEED_RANGE,
        APERTURE_RANGE,
        APPLY_SHOOTING_METERING_MODE,
        FLASH_SYNC_SPEED_AV,
        AE_MICROADJUSTMENT,
        FE_MICROADJUSTMENT,
        SAME_EXPOSURE_FOR_NEW_APERTURE,
        EXPOSURE_COMP_AUTO_CANCEL,
        AE_LOCK_METER_MODE_AFTER_FOCUS,
        LONG_EXPOSURE_NOISE_REDUCTION,
        HIGH_ISO_NOISE_REDUCTION,
        HIGHLIGHT_TONE_PRIORITY,
        AUTO_LIGHTING_OPTIMIZER,
        ETTLII,
        SHUTTER_CURTAIN_SYNC,
        FLASH_FIRING,
        VIEW_INFO_DURING_EXPOSURE,
        LCD_ILLUMINATION_DURING_BULB,
        INFO_BUTTON_WHEN_SHOOTING,
        VIEWFINDER_WARNINGS,
        LV_SHOOTING_AREA_DISPLAY,
        LV_SHOOTING_AREA_DISPLAY_2,
        USM_LENS_ELECTRONIC_MF,
        AI_SERVO_TRACKING_SENSITIVITY,
        AI_SERVO_IMAGE_PRIORITY,
        AI_SERVO_TRACKING_METHOD,
        LENS_DRIVE_NO_AF,
        LENS_AF_STOP_BUTTON,
        AF_MICROADJUSTMENT,
        AF_POINT_AREA_EXPANSION,
        SELECTABLE_AF_POINT,
        SWITCH_TO_REGISTERED_AF_POINT,
        AF_POINT_AUTO_SELECTION,
        AF_POINT_DISPLAY_DURING_FOCUS,
        AF_POINT_BRIGHTNESS,
        AF_ASSIST_BEAM,
        AF_POINT_SELECTION_METHOD,
        VF_DISPLAY_ILLUMINATION,
        AF_DURING_LIVE_VIEW,
        SELECT_AF_AREA_SELECT_MODE,
        MANUAL_AF_POINT_SELECT_PATTERN,
        DISPLAY_ALL_AF_POINTS,
        FOCUS_DISPLAY_AI_SERVO_AND_MF,
        ORIENTATION_LINKED_AF_POINT,
        MULTI_CONTROLLER_WHILE_METERING,
        ACCELERATION_TRACKING,
        AI_SERVO_FIRST_IMAGE_PRIORITY,
        AI_SERVO_SECOND_IMAGE_PRIORITY,
        AF_AREA_SELECT_METHOD,
        AUTO_AF_POINT_COLOR_TRACKING,
        VF_DISPLAY_ILLUMINATION_2,
        INITIAL_AF_POINT_AI_SERVO_AF,
        MIRROR_LOCKUP,
        CONTINUOUS_SHOOTING_SPEED,
        CONTINUOUS_SHOT_LIMIT,
        RESTRICT_DRIVE_MODES,
        SHUTTER_AE_LOCK,
        AF_ON_AE_LOCK_BUTTON_SWITCH,
        QUICK_CONTROL_DIAL_IN_METER,
        SET_BUTTON_WHEN_SHOOTING,
        MANUAL_TV,
        DIAL_DIRECTION_TV_AV,
        AV_SETTING_WITHOUT_LENS,
        WB_MEDIA_IMAGE_SIZE_SETTING,
        LOCK_MICROPHONE_BUTTON,
        BUTTON_FUNCTION_CONTROL_OFF,
        ASSIGN_FUNC_BUTTON,
        CUSTOM_CONTROLS,
        START_MOVIE_SHOOTING,
        FLASH_BUTTON_FUNCTION,
        MULTI_FUNCTION_LOCK,
        TRASH_BUTTON_FUNCTION,
        SHUTTER_RELEASE_WITHOUT_LENS,
        CONTROL_RING_ROTATION,
        FOCUS_RING_ROTATION,
        RF_LENS_MF_FOCUS_RING_SENSITIVITY,
        CUSTOMIZE_DIALS,
        FOCUSING_SCREEN,
        TIMER_LENGTH,
        SHORT_RELEASE_TIME_LAG,
        ADD_ASPECT_RATIO_INFO,
        ADD_ORIGINAL_DECISION_DATA,
        LIVE_VIEW_EXPOSURE_SIMULATION,
        LCD_DISPLAY_AT_POWER_ON,
        MEMO_AUDIO_QUALITY,
        DEFAULT_ERASE_OPTION,
        RETRACT_LENS_ON_POWER_OFF,
        ADD_IPTC_INFORMATION,
        AUDIO_COMPRESSION
    )
}
