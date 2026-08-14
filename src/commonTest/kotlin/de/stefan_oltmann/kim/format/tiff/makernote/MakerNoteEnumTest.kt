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
package de.stefan_oltmann.kim.format.tiff.makernote

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleCameraType
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleHdrImageType
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleImageCaptureType
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonColorSpace
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonDateStampMode
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonModelId
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonPictureStyle
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonSerialNumberFormat
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonSuperMacro
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmAdvancedFilter
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmAfMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmBlurWarning
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmCompositeImageMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmContrast
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmCropMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmDynamicRange
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmDynamicRangeSetting
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmExrMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmFaceElementType
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmFlashMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmFocusMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmFullHdHighSpeedRec
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmHighlightTone
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmImageGeneration
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmPanoramaDirection
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmPictureMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmSaturation
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmSceneRecognition
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmShadowTone
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmSharpness
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmShutterType
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmVideoRecordingMode
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmWhiteBalance
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonActiveDLighting
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonColorSpace
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonCropHiSpeed
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonDateStampMode
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonFlashMode
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonHighIsoNoiseReduction
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonImageAuthentication
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonImageSizeRaw
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonJpgCompression
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonNefCompression
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonRetouchHistory
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonShutterMode
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonSilentPhotography
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonVignetteControl
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusBwMode
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusCameraType
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusCcdScanMode
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusContrast
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusExternalFlashBounce
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFlashDevice
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFlashMode
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFocusMode
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFocusRange
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusMacro
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusNoiseReduction
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusOneTouchWhiteBalance
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusPreviewImageValid
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusSceneMode
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusSharpness
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicAfSubjectDetection
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicAudio
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicBatteryLevel
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicBracketSettings
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicBurstMode
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicCameraOrientation
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicColorEffect
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicDarkFocusEnvironment
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicFilmMode
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicFlashCurtain
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicFocusMode
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicHdr
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicHighlightWarning
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicImageQuality
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicImageStabilization
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicJpegQuality
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicMacroMode
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicPhotoStyle
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicRotation
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicSelfTimer
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicShootingMode
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicShutterType
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicSweepPanoramaDirection
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTextStamp
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicWhiteBalance
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicWorldTimeLocation
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyAfIlluminator
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyAfTracking
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyAntiBlur
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyAutoPortraitFramed
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyColorMode
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyCreativeStyle
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyDistortionCorrectionSetting
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyDynamicRangeOptimizer
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyDynamicRangeOptimizer2
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyElectronicFrontCurtainShutter
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyExposureMode
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyFlashAction
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyFocusMode
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyFocusMode2
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyFocusMode3
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyHighIsoNoiseReduction
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyHighIsoNoiseReduction2
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyImageStabilization
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyIntelligentAuto
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyJpegHeifSwitch
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyJpegQuality
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyLateralChromaticAberration
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyLensType
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyLongExposureNoiseReduction
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMacro
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMeteringMode2
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyModelId
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMultiFrameNoiseReduction
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyMultiFrameNrEffect
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyPictureEffect
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyPrioritySetInAwb
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyQuality
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyRawFileType
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyReleaseMode
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonySceneMode
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonySequenceNumber
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonySoftSkinEffect
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyStepCropShooting
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyTag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyTeleconverter
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyVignettingCorrection
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyWhiteBalance
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyWhiteBalance2
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyZoneMatching
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the MakerNote value enums, checking both the value mapping
 * and the resolution from real test files.
 */
class MakerNoteEnumTest {

    @Test
    fun testCanonEnums() {

        assertEquals(CanonDateStampMode.OFF, CanonDateStampMode.fromValue(0))
        assertEquals(CanonDateStampMode.DATE, CanonDateStampMode.fromValue(1))
        assertEquals(CanonDateStampMode.DATE_AND_TIME, CanonDateStampMode.fromValue(2))
        assertEquals("Date & Time", CanonDateStampMode.DATE_AND_TIME.description)
        assertNull(CanonDateStampMode.fromValue(99))

        assertEquals(CanonSuperMacro.OFF, CanonSuperMacro.fromValue(0))
        assertEquals(CanonSuperMacro.ON_2, CanonSuperMacro.fromValue(2))

        assertEquals(CanonSerialNumberFormat.FORMAT_1, CanonSerialNumberFormat.fromValue(0x90000000.toInt()))
        assertEquals(CanonSerialNumberFormat.FORMAT_2, CanonSerialNumberFormat.fromValue(0xa0000000.toInt()))

        assertEquals(CanonColorSpace.SRGB, CanonColorSpace.fromValue(1))
        assertEquals(CanonColorSpace.ADOBE_RGB, CanonColorSpace.fromValue(2))
        assertEquals(CanonColorSpace.NOT_AVAILABLE, CanonColorSpace.fromValue(65535))
    }

    @Test
    fun testNikonEnums() {

        assertEquals(NikonColorSpace.SRGB, NikonColorSpace.fromValue(1))
        assertEquals(NikonColorSpace.BT_2100, NikonColorSpace.fromValue(4))

        assertEquals(NikonCropHiSpeed.DX_UNCROPPED, NikonCropHiSpeed.fromValue(12))
        assertEquals("DX Uncropped", NikonCropHiSpeed.DX_UNCROPPED.description)

        assertEquals(NikonActiveDLighting.AUTO, NikonActiveDLighting.fromValue(65535))
        assertEquals(NikonActiveDLighting.EXTRA_HIGH, NikonActiveDLighting.fromValue(7))

        assertEquals(NikonFlashMode.FIRED_TTL_MODE, NikonFlashMode.fromValue(9))
        assertEquals(NikonShutterMode.ELECTRONIC_FRONT_CURTAIN, NikonShutterMode.fromValue(48))
        assertEquals(NikonNefCompression.LOSSLESS, NikonNefCompression.fromValue(3))
        assertEquals(NikonNefCompression.HIGH_EFFICIENCY, NikonNefCompression.fromValue(13))
        assertEquals(NikonHighIsoNoiseReduction.NORMAL, NikonHighIsoNoiseReduction.fromValue(4))
        assertEquals(NikonDateStampMode.DATE, NikonDateStampMode.fromValue(2))
        assertEquals(NikonImageSizeRaw.LARGE, NikonImageSizeRaw.fromValue(1))
        assertEquals(NikonJpgCompression.SIZE_PRIORITY, NikonJpgCompression.fromValue(1))
        assertEquals(NikonImageAuthentication.ON, NikonImageAuthentication.fromValue(1))
        assertEquals(NikonVignetteControl.NORMAL, NikonVignetteControl.fromValue(3))
        assertEquals(NikonSilentPhotography.OFF, NikonSilentPhotography.fromValue(0))

        assertNull(NikonColorSpace.fromValue(99))
    }

    @Test
    fun testFujiFilmEnums() {

        assertEquals(FujiFilmSharpness.NORMAL, FujiFilmSharpness.fromValue(0x3))
        assertEquals(FujiFilmSharpness.FILM_SIMULATION, FujiFilmSharpness.fromValue(0x8000))
        assertEquals(FujiFilmContrast.HIGH, FujiFilmContrast.fromValue(0x100))
        assertEquals(FujiFilmFocusMode.AUTO, FujiFilmFocusMode.fromValue(0))
        assertEquals(FujiFilmFocusMode.MOVIE, FujiFilmFocusMode.fromValue(65535))
        assertEquals(FujiFilmAfMode.WIDE_TRACKING, FujiFilmAfMode.fromValue(512))
        assertEquals(FujiFilmShutterType.ELECTRONIC_FRONT_CURTAIN, FujiFilmShutterType.fromValue(3))
        assertEquals(FujiFilmCropMode.FULL_FRAME_ON_GFX, FujiFilmCropMode.fromValue(1))
        assertEquals(FujiFilmDynamicRange.WIDE, FujiFilmDynamicRange.fromValue(3))
        assertEquals(FujiFilmDynamicRangeSetting.WIDE_2, FujiFilmDynamicRangeSetting.fromValue(0x201))
        assertEquals(FujiFilmExrMode.HR, FujiFilmExrMode.fromValue(0x100))
        assertEquals(FujiFilmBlurWarning.BLUR_WARNING, FujiFilmBlurWarning.fromValue(1))
        assertEquals(FujiFilmCompositeImageMode.HDR, FujiFilmCompositeImageMode.fromValue(128))
        assertEquals(FujiFilmVideoRecordingMode.F_LOG, FujiFilmVideoRecordingMode.fromValue(0x10))
        assertEquals(FujiFilmPanoramaDirection.RIGHT, FujiFilmPanoramaDirection.fromValue(1))
        assertEquals(FujiFilmSceneRecognition.PORTRAIT_IMAGE, FujiFilmSceneRecognition.fromValue(0x100))
        assertEquals(FujiFilmImageGeneration.RE_DEVELOPED_FROM_RAW, FujiFilmImageGeneration.fromValue(1))
        assertEquals(FujiFilmFullHdHighSpeedRec.ON, FujiFilmFullHdHighSpeedRec.fromValue(2))

        assertNull(FujiFilmFocusMode.fromValue(99))
    }

    @Test
    fun testAppleEnums() {

        assertEquals(AppleHdrImageType.HDR_IMAGE, AppleHdrImageType.fromValue(3))
        assertEquals(AppleHdrImageType.ORIGINAL_IMAGE, AppleHdrImageType.fromValue(4))
        assertEquals(AppleImageCaptureType.PHOTO, AppleImageCaptureType.fromValue(10))
        assertEquals(AppleCameraType.BACK_NORMAL, AppleCameraType.fromValue(1))
        assertEquals(AppleCameraType.FRONT, AppleCameraType.fromValue(6))
        assertEquals("Back Wide Angle", AppleCameraType.BACK_WIDE_ANGLE.description)

        assertNull(AppleCameraType.fromValue(99))
    }

    @Test
    fun testOlympusEnums() {

        assertEquals(OlympusMacro.SUPER_MACRO, OlympusMacro.fromValue(2))
        assertEquals(OlympusBwMode.NONE, OlympusBwMode.fromValue(6))
        assertEquals(OlympusFlashMode.ON, OlympusFlashMode.fromValue(2))
        assertEquals(OlympusFlashDevice.INTERNAL_EXTERNAL, OlympusFlashDevice.fromValue(5))
        assertEquals(OlympusFocusRange.MACRO, OlympusFocusRange.fromValue(1))
        assertEquals(OlympusFocusMode.MANUAL, OlympusFocusMode.fromValue(1))
        assertEquals(OlympusSharpness.HARD, OlympusSharpness.fromValue(1))
        assertEquals(OlympusExternalFlashBounce.YES, OlympusExternalFlashBounce.fromValue(1))
        assertEquals(OlympusContrast.HIGH, OlympusContrast.fromValue(0))
        assertEquals(OlympusPreviewImageValid.YES, OlympusPreviewImageValid.fromValue(1))
        assertEquals(OlympusCcdScanMode.PROGRESSIVE, OlympusCcdScanMode.fromValue(1))
        assertEquals(OlympusNoiseReduction.ON, OlympusNoiseReduction.fromValue(1))
        assertEquals(OlympusOneTouchWhiteBalance.ON_PRESET, OlympusOneTouchWhiteBalance.fromValue(2))

        assertNull(OlympusMacro.fromValue(99))
    }

    @Test
    fun testPanasonicEnums() {

        assertEquals(PanasonicImageQuality.RAW, PanasonicImageQuality.fromValue(7))
        assertEquals(PanasonicWhiteBalance.AUTO, PanasonicWhiteBalance.fromValue(1))
        assertEquals(PanasonicFocusMode.AF_C, PanasonicFocusMode.fromValue(7))
        assertEquals(PanasonicImageStabilization.DUAL2_IS, PanasonicImageStabilization.fromValue(11))
        assertEquals(PanasonicMacroMode.TELE_MACRO, PanasonicMacroMode.fromValue(257))
        assertEquals(PanasonicAudio.STEREO, PanasonicAudio.fromValue(3))
        assertEquals(PanasonicColorEffect.VIVID, PanasonicColorEffect.fromValue(8))
        assertEquals(PanasonicBurstMode.AUTO_EXPOSURE_BRACKETING, PanasonicBurstMode.fromValue(2))
        assertEquals(PanasonicSelfTimer.TWO_SECONDS, PanasonicSelfTimer.fromValue(3))
        assertEquals(PanasonicRotation.ROTATE_180, PanasonicRotation.fromValue(3))
        assertEquals(PanasonicBatteryLevel.NEAR_EMPTY, PanasonicBatteryLevel.fromValue(4))
        assertEquals(PanasonicFilmMode.NOSTALGIC, PanasonicFilmMode.fromValue(10))
        assertEquals(PanasonicJpegQuality.VERY_HIGH, PanasonicJpegQuality.fromValue(6))
        assertEquals(PanasonicPhotoStyle.CINELIKE_D2, PanasonicPhotoStyle.fromValue(18))
        assertEquals(PanasonicCameraOrientation.ROTATE_CCW, PanasonicCameraOrientation.fromValue(3))
        assertEquals(PanasonicSweepPanoramaDirection.BOTTOM_TO_TOP, PanasonicSweepPanoramaDirection.fromValue(4))
        assertEquals(PanasonicHdr.EV_1_AUTO, PanasonicHdr.fromValue(32868))
        assertEquals(PanasonicShutterType.HYBRID, PanasonicShutterType.fromValue(2))
        assertEquals(PanasonicAfSubjectDetection.AIRPLANE, PanasonicAfSubjectDetection.fromValue(12))
        assertEquals(PanasonicHighlightWarning.YES, PanasonicHighlightWarning.fromValue(2))
        assertEquals(PanasonicDarkFocusEnvironment.YES, PanasonicDarkFocusEnvironment.fromValue(2))
        assertEquals(PanasonicTextStamp.ON, PanasonicTextStamp.fromValue(2))
        assertEquals(PanasonicFlashCurtain.SECOND, PanasonicFlashCurtain.fromValue(2))
        assertEquals(PanasonicWorldTimeLocation.DESTINATION, PanasonicWorldTimeLocation.fromValue(2))
        assertEquals(PanasonicBracketSettings.FIVE_IMAGES_SEQUENCE_MINUS, PanasonicBracketSettings.fromValue(4))

        assertNull(PanasonicImageQuality.fromValue(99))
    }

    @Test
    fun testSonyEnums() {

        assertEquals(SonyQuality.RAW_JPEG_HEIF, SonyQuality.fromValue(6))
        assertEquals("RAW + JPEG/HEIF", SonyQuality.RAW_JPEG_HEIF.description)
        assertEquals(SonyWhiteBalance.DAYLIGHT, SonyWhiteBalance.fromValue(0x10))
        assertEquals(SonyHighIsoNoiseReduction.AUTO, SonyHighIsoNoiseReduction.fromValue(256))
        assertEquals(SonyMultiFrameNoiseReduction.NOT_AVAILABLE, SonyMultiFrameNoiseReduction.fromValue(255))
        assertEquals(SonySoftSkinEffect.HIGH, SonySoftSkinEffect.fromValue(3))
        assertEquals(SonyVignettingCorrection.AUTO, SonyVignettingCorrection.fromValue(2))
        assertEquals(SonyFlashAction.EXTERNAL_FLASH_FIRED, SonyFlashAction.fromValue(2))
        assertEquals(SonyFocusMode.AF_A, SonyFocusMode.fromValue(3))
        assertNull(SonyFocusMode.fromValue(6))
        assertEquals(SonyAfTracking.LOCK_ON_AF, SonyAfTracking.fromValue(2))
        assertEquals(SonyRawFileType.LOSSLESS_COMPRESSED_RAW, SonyRawFileType.fromValue(2))
        assertEquals(SonyMeteringMode2.SPOT_STANDARD, SonyMeteringMode2.fromValue(0x301))
        assertEquals(SonyJpegHeifSwitch.HEIF, SonyJpegHeifSwitch.fromValue(1))
        assertEquals(SonyZoneMatching.HIGH_KEY, SonyZoneMatching.fromValue(1))
        assertEquals(SonyImageStabilization.ON, SonyImageStabilization.fromValue(1))
        assertEquals(SonyMacro.CLOSE_FOCUS, SonyMacro.fromValue(2))
        assertEquals(SonyFocusMode2.PERMANENT_AF, SonyFocusMode2.fromValue(4))
        assertEquals(SonyJpegQuality.EXTRA_FINE, SonyJpegQuality.fromValue(2))
        assertEquals(SonyReleaseMode.DRO_BRACKETING, SonyReleaseMode.fromValue(8))
        assertEquals(SonyAntiBlur.ON_SHOOTING, SonyAntiBlur.fromValue(2))
        assertEquals(SonyFocusMode3.SEMI_MANUAL, SonyFocusMode3.fromValue(5))
        assertEquals(SonyDynamicRangeOptimizer2.PLUS, SonyDynamicRangeOptimizer2.fromValue(2))
        assertEquals(SonyHighIsoNoiseReduction2.OFF, SonyHighIsoNoiseReduction2.fromValue(3))
        assertEquals(SonyIntelligentAuto.ADVANCED, SonyIntelligentAuto.fromValue(2))
        assertEquals(SonyWhiteBalance2.UNDERWATER_AUTO, SonyWhiteBalance2.fromValue(19))
        assertEquals(SonyLongExposureNoiseReduction.OFF, SonyLongExposureNoiseReduction.fromValue(0x0))
        assertEquals(SonySequenceNumber.SINGLE, SonySequenceNumber.fromValue(0))
        assertEquals(SonyStepCropShooting.MM_50, SonyStepCropShooting.fromValue(1))
        assertEquals(SonyAfIlluminator.AUTO, SonyAfIlluminator.fromValue(1))
        assertEquals(SonyPrioritySetInAwb.WHITE, SonyPrioritySetInAwb.fromValue(2))
        assertEquals(SonyMultiFrameNrEffect.HIGH, SonyMultiFrameNrEffect.fromValue(1))
        assertEquals(SonyAutoPortraitFramed.YES, SonyAutoPortraitFramed.fromValue(1))
        assertEquals(SonyElectronicFrontCurtainShutter.ON, SonyElectronicFrontCurtainShutter.fromValue(1))
        assertEquals(SonyLateralChromaticAberration.AUTO, SonyLateralChromaticAberration.fromValue(2))
        assertEquals(SonyDistortionCorrectionSetting.OFF, SonyDistortionCorrectionSetting.fromValue(0))

        assertNull(SonyQuality.fromValue(99))
    }

    @Test
    fun testEnumResolutionFromCanonFile() {

        /* media_18 has a Canon MakerNote ColorSpace of Adobe RGB. */
        val makerNoteDirectory = getMakerNoteDirectory(18)

        assertEquals(
            CanonColorSpace.ADOBE_RGB,
            CanonColorSpace.fromValue(
                makerNoteDirectory.findField(CanonTag.COLOR_SPACE)?.toInt() ?: -1
            )
        )
    }

    @Test
    fun testEnumResolutionFromNikonFile() {

        val makerNoteDirectory = getMakerNoteDirectory(15)

        assertEquals(
            NikonColorSpace.SRGB,
            NikonColorSpace.fromValue(
                makerNoteDirectory.findField(NikonTag.COLOR_SPACE)?.toInt() ?: -1
            )
        )

        assertEquals(
            NikonDateStampMode.OFF,
            NikonDateStampMode.fromValue(
                makerNoteDirectory.findField(NikonTag.DATE_STAMP_MODE)?.toInt() ?: -1
            )
        )
    }

    @Test
    fun testEnumResolutionFromFujiFilmFile() {

        val makerNoteDirectory = getMakerNoteDirectory(50)

        assertEquals(
            FujiFilmFocusMode.AUTO,
            FujiFilmFocusMode.fromValue(
                makerNoteDirectory.findField(FujiFilmTag.FOCUS_MODE)?.toInt() ?: -1
            )
        )

        assertEquals(
            FujiFilmDynamicRange.STANDARD,
            FujiFilmDynamicRange.fromValue(
                makerNoteDirectory.findField(FujiFilmTag.DYNAMIC_RANGE)?.toInt() ?: -1
            )
        )
    }

    @Test
    fun testEnumResolutionFromAppleFile() {

        val makerNoteDirectory = getMakerNoteDirectory(48)

        assertEquals(
            AppleCameraType.BACK_NORMAL,
            AppleCameraType.fromValue(
                makerNoteDirectory.findField(AppleTag.CAMERA_TYPE)?.toInt() ?: -1
            )
        )
    }

    @Test
    fun testEnumResolutionFromSonyFile() {

        val makerNoteDirectory = getMakerNoteDirectory(KimTestData.HIF_TEST_IMAGE_INDEX)

        assertEquals(
            SonyQuality.RAW_JPEG_HEIF,
            SonyQuality.fromValue(
                makerNoteDirectory.findField(SonyTag.QUALITY)?.toInt() ?: -1
            )
        )
    }

    @Test
    fun testBigValueTables() {

        /* CanonModelID resolves the Canon EOS 70D of the test file. */
        assertEquals(
            CanonModelId.EOS_70D,
            CanonModelId.fromValue(
                getMakerNoteDirectory(1).findField(CanonTag.CANON_MODEL_ID)
                    ?.toInt()
                    ?: -1
            )
        )
        assertEquals("EOS 70D", CanonModelId.EOS_70D.displayName)
        assertNull(CanonModelId.fromValue(-1))

        assertEquals(CanonPictureStyle.STANDARD, CanonPictureStyle.fromValue(0x1))
        assertEquals(CanonPictureStyle.MONOCHROME, CanonPictureStyle.fromValue(0x86))
        assertEquals("n/a", CanonPictureStyle.fromValue(0xffff)?.displayName)
        assertNull(CanonPictureStyle.fromValue(99))

        assertEquals(NikonRetouchHistory.RED_EYE, NikonRetouchHistory.fromValue(8))
        assertEquals(NikonRetouchHistory.LOW_KEY, NikonRetouchHistory.fromValue(54))
        assertEquals("High Key", NikonRetouchHistory.fromValue(53)?.displayName)
        assertNull(NikonRetouchHistory.fromValue(99))

        /* SonyModelID resolves the cameras of the Sony test files. */
        assertEquals(
            SonyModelId.DSLR_A500,
            SonyModelId.fromValue(
                getMakerNoteDirectory(KimTestData.ARW_TEST_IMAGE_INDEX)
                    .findField(SonyTag.SONY_MODEL_ID)
                    ?.toInt()
                    ?: -1
            )
        )
        assertEquals(
            SonyModelId.ILCE_7M4,
            SonyModelId.fromValue(
                getMakerNoteDirectory(KimTestData.HIF_TEST_IMAGE_INDEX)
                    .findField(SonyTag.SONY_MODEL_ID)
                    ?.toInt()
                    ?: -1
            )
        )
        assertEquals("ILCE-7M4", SonyModelId.ILCE_7M4.displayName)
        assertNull(SonyModelId.fromValue(-1))

        assertEquals(SonyLensType.MINOLTA_AF_85MM_F1_4G, SonyLensType.fromValue(4))
        assertEquals(
            "Carl Zeiss Planar T* 85mm F1.4 ZA (SAL85F14Z)",
            SonyLensType.fromValue(45)?.displayName
        )
        assertNull(SonyLensType.fromValue(-1))

        assertEquals(SonyCreativeStyle.STANDARD, SonyCreativeStyle.fromValue("Standard"))
        assertEquals("Vivid 2", SonyCreativeStyle.fromValue("VV2")?.displayName)
        assertNull(SonyCreativeStyle.fromValue("Unknown"))

        assertEquals(SonyTeleconverter.NONE, SonyTeleconverter.fromValue(0x0))
        assertEquals(SonySceneMode.SUPERIOR_AUTO, SonySceneMode.fromValue(24))
        assertEquals(SonyExposureMode.MANUAL, SonyExposureMode.fromValue(15))
        assertEquals(SonyPictureEffect.HIGH_CONTRAST_MONOCHROME, SonyPictureEffect.fromValue(13))
        assertEquals(SonyColorMode.ADOBE_RGB, SonyColorMode.fromValue(7))
        assertEquals(SonyDynamicRangeOptimizer.ADVANCED_AUTO, SonyDynamicRangeOptimizer.fromValue(2))

        /* The ARW file was written by a DSLR-A500. */
        assertEquals(
            OlympusCameraType.X_2_C_50Z,
            OlympusCameraType.fromValue("D4028")
        )
        assertEquals(OlympusSceneMode.NIGHT_SCENE, OlympusSceneMode.fromValue(7))
        assertEquals(OlympusSceneMode.MAGIC_FILTER, OlympusSceneMode.fromValue(101))
        assertNull(OlympusSceneMode.fromValue(99))

        assertEquals(PanasonicShootingMode.PROGRAM, PanasonicShootingMode.fromValue(6))
        assertEquals("Intelligent Auto", PanasonicShootingMode.fromValue(37)?.displayName)
        assertNull(PanasonicShootingMode.fromValue(99))

        assertEquals(FujiFilmWhiteBalance.DAYLIGHT, FujiFilmWhiteBalance.fromValue(0x100))
        assertEquals(FujiFilmSaturation.ACROS, FujiFilmSaturation.fromValue(0x500))
        assertEquals(FujiFilmFlashMode.MANUAL, FujiFilmFlashMode.fromValue(0x9840))
        assertEquals(FujiFilmPictureMode.MANUAL, FujiFilmPictureMode.fromValue(0x300))
        assertEquals(FujiFilmAdvancedFilter.MINIATURE, FujiFilmAdvancedFilter.fromValue(0x40000))
        assertEquals(FujiFilmFaceElementType.FACE, FujiFilmFaceElementType.fromValue(1))
        assertEquals(FujiFilmShadowTone.HARDEST, FujiFilmShadowTone.fromValue(-64))
        assertEquals("+4 (hardest)", FujiFilmShadowTone.HARDEST.displayName)
        assertEquals(FujiFilmHighlightTone.SOFT, FujiFilmHighlightTone.fromValue(32))
    }

    private fun getMakerNoteDirectory(index: Int): TiffDirectory {

        val metadata = Kim.readMetadata(KimTestData.getBytesOf(index))

        return metadata?.exif?.makerNoteDirectory
            ?: error("MakerNote directory missing for media_$index")
    }
}
