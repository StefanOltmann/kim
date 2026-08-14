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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusCameraSettingsTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusEquipmentTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusFocusInfoTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusImageProcessingTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusRawDevelopmentTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyTag
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for MakerNote parsing, with the expected values verified
 * against ExifTool output.
 */
class MakerNoteTest {

    @Test
    fun testCanonMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(1)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_CANON, makerNoteDirectory.type)

        assertEquals(
            "Canon EOS 70D",
            makerNoteDirectory.findField(CanonTag.CANON_IMAGE_TYPE)?.value
        )
        assertEquals(
            "Firmware Version 1.1.2",
            makerNoteDirectory.findField(CanonTag.CANON_FIRMWARE_VERSION)?.value
        )
        assertEquals(
            "EF-S55-250mm f/4-5.6 IS STM",
            makerNoteDirectory.findField(CanonTag.LENS_MODEL)?.value
        )
    }

    @Test
    fun testNikonMakerNoteLittleEndianParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(15)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_NIKON, makerNoteDirectory.type)

        /* The value offsets are relative to the embedded TIFF header. */
        assertEquals("FINE", makerNoteDirectory.findField(NikonTag.QUALITY)?.toStringValue()?.trim())
        assertEquals("AUTO", makerNoteDirectory.findField(NikonTag.WHITE_BALANCE)?.toStringValue()?.trim())

        assertEquals(14611, makerNoteDirectory.findField(NikonTag.SHUTTER_COUNT)?.toInt())
    }

    @Test
    fun testNikonMakerNoteBigEndianParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(KimTestData.NEF_TEST_IMAGE_INDEX)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_NIKON, makerNoteDirectory.type)

        /* This NEF file embeds a big-endian TIFF header in the MakerNote. */
        assertEquals("RAW", makerNoteDirectory.findField(NikonTag.QUALITY)?.toStringValue()?.trim())

        val isoField = makerNoteDirectory.findField(NikonTag.ISO)
        assertNotNull(isoField)
        assertEquals(listOf(0, 200), isoField.toIntArray().toList())

        val versionField = makerNoteDirectory.findField(NikonTag.MAKER_NOTE_VERSION)
        assertNotNull(versionField)
        assertEquals("0210", versionField.valueBytes.decodeToString())
    }

    @Test
    fun testFujiFilmMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(50)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_FUJIFILM, makerNoteDirectory.type)

        assertEquals("FINE", makerNoteDirectory.findField(FujiFilmTag.QUALITY)?.toStringValue()?.trim())
        assertEquals(5, makerNoteDirectory.findField(FujiFilmTag.SHARPNESS)?.toInt())
        assertEquals(0, makerNoteDirectory.findField(FujiFilmTag.FILM_MODE)?.toInt())
    }

    @Test
    fun testAppleMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(48)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_APPLE, makerNoteDirectory.type)

        assertEquals(14, makerNoteDirectory.findField(AppleTag.MAKER_NOTE_VERSION)?.toInt())
        assertEquals(1, makerNoteDirectory.findField(AppleTag.AE_STABLE)?.toInt())
        assertEquals(165, makerNoteDirectory.findField(AppleTag.AE_TARGET)?.toInt())

        val hdrHeadroomField = makerNoteDirectory.findField(AppleTag.HDR_HEADROOM)
        assertNotNull(hdrHeadroomField)
        assertEquals(46219, (hdrHeadroomField.value as RationalNumbers).values.first().numerator)

        /* The int64 fields must not be skipped as unknown field types. */
        val livePhotoVideoIndexField = makerNoteDirectory.findField(AppleTag.LIVE_PHOTO_VIDEO_INDEX)
        assertNotNull(livePhotoVideoIndexField)
        assertEquals(1112547328L, (livePhotoVideoIndexField.value as LongArray).first())
    }

    /**
     * The value offsets of an iPhone SE 3rd gen HEIC file must be
     * absolute within the TIFF bytes: the AEMatrix of the Apple main
     * directory is resolved against the start of the TIFF data, so a
     * consumer can draw it without knowing the MakerNote blob position.
     */
    @Test
    fun testAppleMakerNoteValueOffsetsAreAbsolute() {

        val fileBytes = KimTestData.getBytesOf("photo_5.heic")

        val metadata = assertNotNull(Kim.readMetadata(fileBytes))

        val makerNoteField = assertNotNull(
            TiffDirectory.findTiffField(
                assertNotNull(metadata.exif).directories,
                ExifTag.EXIF_TAG_MAKER_NOTE
            )
        )

        /* The MakerNote blob sits at this offset within the TIFF bytes. */
        val makerNoteTiffOffset = 782
        assertEquals(makerNoteTiffOffset, makerNoteField.valueOffset)

        val makerNoteDirectory = assertNotNull(metadata.exif.makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_APPLE, makerNoteDirectory.type)

        val aeMatrixField = assertNotNull(makerNoteDirectory.findField(AppleTag.AE_MATRIX))

        /* The AEMatrix data is stored at the MakerNote offset plus 560. */
        val aeMatrixTiffOffset = makerNoteTiffOffset + 560
        assertEquals(aeMatrixTiffOffset, aeMatrixField.valueOffset)

        /* The Exif box payload, which is the TIFF base of this file. */
        val tiffBaseInFile = 21114

        val valueBytes = aeMatrixField.valueBytes
        val expectedBytes = fileBytes.copyOfRange(
            tiffBaseInFile + aeMatrixTiffOffset,
            tiffBaseInFile + aeMatrixTiffOffset + valueBytes.size
        )

        assertTrue(
            valueBytes.contentEquals(expectedBytes),
            "The AEMatrix value must be the matrix data at the absolute offset."
        )

        /* The raw entry offset would land inside the ExifIFD entry table. */
        val ifdTableBytes = fileBytes.copyOfRange(
            tiffBaseInFile + 560,
            tiffBaseInFile + 560 + valueBytes.size
        )

        assertFalse(
            valueBytes.contentEquals(ifdTableBytes),
            "The AEMatrix value must not be read from the ExifIFD entry table."
        )
    }

    @Test
    fun testOlympusMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(39)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_OLYMPUS, makerNoteDirectory.type)

        val specialModeField = makerNoteDirectory.findField(OlympusTag.SPECIAL_MODE)
        assertNotNull(specialModeField)
        assertEquals(listOf(0, 0, 0), specialModeField.toIntArray().toList())

        val cameraIdField = makerNoteDirectory.findField(OlympusTag.CAMERA_ID)
        assertNotNull(cameraIdField)
        assertEquals(
            "OLYMPUS DIGITAL CAMERA",
            cameraIdField.valueBytes.decodeToString().trimEnd('\u0000', ' ')
        )
    }

    /**
     * The Panasonic MakerNote of the RW2 test files lives inside the
     * embedded JPEG preview, so it is tested with a constructed TIFF.
     */
    @Test
    fun testPanasonicMakerNoteParsing() {

        val bytes = convertHexStringToByteArray(
            "49492A0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0300" + // IFD0: 3 entries
                "0F0102000A00000032000000" + // Make -> 50
                "10010200080000003C000000" + // Model -> 60
                "698704000100000044000000" + // ExifOffset -> ExifIFD at 68
                "00000000" + // No next directory
                "50616E61736F6E696300" + // "Panasonic\0"
                "444D432D4C583700" + // "DMC-LX7\0"
                "0100" + // ExifIFD: 1 entry
                "7C9207004200000056000000" + // MakerNote -> 86
                "00000000" + // No next directory
                "50616E61736F6E6963000000" + // "Panasonic\0\0\0" signature
                "0300" + // 3 entries
                "010003000100000007000000" + // ImageQuality = 7
                "2900040001000000D7200000" + // TimeSincePowerOn = 8407
                "250007001000000088000000" + // InternalSerialNumber -> offset 136
                "30313233343536373839414243444546" // "0123456789ABCDEF"
        )

        val contents = TiffReader.read(ByteArrayByteReader(bytes))

        val makerNoteDirectory = contents.makerNoteDirectory

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_PANASONIC, makerNoteDirectory.type)

        assertEquals(7, makerNoteDirectory.findField(PanasonicTag.IMAGE_QUALITY)?.toInt())
        assertEquals(8407, makerNoteDirectory.findField(PanasonicTag.TIME_SINCE_POWER_ON)?.toInt())

        /* The value offsets are relative to the start of the Exif data. */
        val serialNumberField = makerNoteDirectory.findField(PanasonicTag.INTERNAL_SERIAL_NUMBER)
        assertNotNull(serialNumberField)
        assertEquals("0123456789ABCDEF", serialNumberField.valueBytes.decodeToString())
    }

    @Test
    fun testSony5MakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(KimTestData.ARW_TEST_IMAGE_INDEX)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_SONY5, makerNoteDirectory.type)

        assertEquals(274, makerNoteDirectory.findField(SonyTag.SONY_MODEL_ID)?.toInt())
        assertEquals("Standard", makerNoteDirectory.findField(SonyTag.CREATIVE_STYLE)?.value)
        assertEquals(0, makerNoteDirectory.findField(SonyTag.RATING)?.toInt())
    }

    @Test
    fun testSonyMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(KimTestData.HIF_TEST_IMAGE_INDEX)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_SONY, makerNoteDirectory.type)

        assertEquals(388, makerNoteDirectory.findField(SonyTag.SONY_MODEL_ID)?.toInt())
        assertEquals("Standard", makerNoteDirectory.findField(SonyTag.CREATIVE_STYLE)?.value)

        val fullImageSizeField = makerNoteDirectory.findField(SonyTag.FULL_IMAGE_SIZE)
        assertNotNull(fullImageSizeField)
        assertEquals(listOf(4672, 7008), fullImageSizeField.toIntArray().toList())
    }

    /**
     * The Olympus MakerNote sub-directories are interpreted and
     * exposed as separate directories.
     */
    @Test
    fun testOlympusMakerNoteSubDirectories() {

        val contents = getTiffContents(39)

        val equipment = contents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT
        )
        assertNotNull(equipment)
        assertEquals(
            "0100",
            equipment.findField(OlympusEquipmentTag.EQUIPMENT_VERSION)?.valueBytes?.decodeToString()
        )
        assertEquals(
            "S0051",
            equipment.findField(OlympusEquipmentTag.CAMERA_TYPE2)?.toStringValue()?.trim()
        )
        assertEquals(
            "V5PF17523",
            equipment.findField(OlympusEquipmentTag.SERIAL_NUMBER)?.toStringValue()?.trim()
        )
        assertEquals(
            "4158401000050001",
            equipment.findField(OlympusEquipmentTag.INTERNAL_SERIAL_NUMBER)?.toStringValue()?.trim()
        )
        assertEquals(21.6, equipment.findField(OlympusEquipmentTag.FOCAL_PLANE_DIAGONAL)?.toDouble())
        assertEquals(4609, equipment.findField(OlympusEquipmentTag.BODY_FIRMWARE_VERSION)?.toInt())
        assertEquals(
            listOf(0, 0, 20, 16, 0, 0),
            equipment.findField(OlympusEquipmentTag.LENS_TYPE)?.valueBytes?.map { it.toInt() }
        )
        assertEquals(1024, equipment.findField(OlympusEquipmentTag.MAX_APERTURE_AT_MIN_FOCAL)?.toInt())
        assertEquals(40, equipment.findField(OlympusEquipmentTag.MIN_FOCAL_LENGTH)?.toInt())
        assertEquals(150, equipment.findField(OlympusEquipmentTag.MAX_FOCAL_LENGTH)?.toInt())
        assertEquals(
            "OLYMPUS M.40-150mm F4.0-5.6 R",
            equipment.findField(OlympusEquipmentTag.LENS_MODEL)?.toStringValue()?.trim()
        )

        val cameraSettings = contents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
        )
        assertNotNull(cameraSettings)
        assertEquals(
            "0100",
            cameraSettings.findField(OlympusCameraSettingsTag.CAMERA_SETTINGS_VERSION)?.valueBytes?.decodeToString()
        )
        assertEquals(1, cameraSettings.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_VALID)?.toInt())
        assertEquals(1216396, cameraSettings.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_START)?.toInt())
        assertEquals(555951, cameraSettings.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_LENGTH)?.toInt())
        assertEquals(4, cameraSettings.findField(OlympusCameraSettingsTag.EXPOSURE_MODE)?.toInt())
        assertEquals(
            listOf(0, 65),
            cameraSettings.findField(OlympusCameraSettingsTag.FOCUS_MODE)?.toIntArray()?.toList()
        )
        assertEquals(0.0, cameraSettings.findField(OlympusCameraSettingsTag.EXPOSURE_SHIFT)?.toDouble())

        val rawDevelopment = contents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
        )
        assertNotNull(rawDevelopment)
        assertEquals(
            "0100",
            rawDevelopment.findField(OlympusRawDevelopmentTag.RAW_DEV_VERSION)?.valueBytes?.decodeToString()
        )
        assertEquals(0, rawDevelopment.findField(OlympusRawDevelopmentTag.RAW_DEV_WB_FINE_ADJUSTMENT)?.toInt())
        assertEquals(
            listOf(0, 0, 0),
            rawDevelopment.findField(OlympusRawDevelopmentTag.RAW_DEV_GRAY_POINT)?.toIntArray()?.toList()
        )

        val imageProcessing = contents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
        )
        assertNotNull(imageProcessing)
        assertEquals(
            "0112",
            imageProcessing.findField(OlympusImageProcessingTag.IMAGE_PROCESSING_VERSION)?.valueBytes?.decodeToString()
        )

        val focusInfo = contents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO
        )
        assertNotNull(focusInfo)
        assertEquals(
            "0100",
            focusInfo.findField(OlympusFocusInfoTag.FOCUS_INFO_VERSION)?.valueBytes?.decodeToString()
        )
    }

    /**
     * Covers at least three fields of every MakerNote tag type,
     * with the values verified against ExifTool output.
     */
    @Test
    fun testMakerNoteTagTypeCoverage() {

        /* TagInfoShort: FujiFilm Sharpness, FilmMode, Olympus ExposureMode. */
        val fujiMakerNote = getMakerNoteDirectory(50)
        assertNotNull(fujiMakerNote)
        assertEquals(5, fujiMakerNote.findField(FujiFilmTag.SHARPNESS)?.toInt())
        assertEquals(0, fujiMakerNote.findField(FujiFilmTag.FILM_MODE)?.toInt())

        val orfContents = getTiffContents(65)
        val orfCameraSettings = requireNotNull(
            orfContents.findMakerNoteSubDirectory(TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS)
        )
        assertEquals(2, orfCameraSettings.findField(OlympusCameraSettingsTag.EXPOSURE_MODE)?.toInt())
        assertEquals(1, orfCameraSettings.findField(OlympusCameraSettingsTag.MACRO_MODE)?.toInt())

        /* TagInfoShorts: Canon FlashInfo, Canon ThumbnailArea, Olympus FocusMode. */
        val canonMakerNote = getMakerNoteDirectory(1)
        assertNotNull(canonMakerNote)
        assertEquals(
            listOf(0, 0, 0, 0),
            canonMakerNote.findField(CanonTag.CANON_FLASH_INFO)?.toIntArray()?.toList()
        )
        assertEquals(
            listOf(0, 159, 7, 112),
            canonMakerNote.findField(CanonTag.THUMBNAIL_IMAGE_VALID_AREA)?.toIntArray()?.toList()
        )
        assertEquals(
            listOf(0, 0),
            orfCameraSettings.findField(OlympusCameraSettingsTag.FOCUS_PROCESS)?.toIntArray()?.toList()
        )

        /* TagInfoSShorts: Olympus CustomSaturation, Contrast, Sharpness. */
        assertEquals(
            listOf(0, -5, 5),
            orfCameraSettings.findField(OlympusCameraSettingsTag.CUSTOM_SATURATION)?.toIntArray()?.toList()
        )
        assertEquals(
            listOf(0, -5, 5),
            orfCameraSettings.findField(OlympusCameraSettingsTag.CONTRAST_SETTING)?.toIntArray()?.toList()
        )
        assertEquals(
            listOf(0, -5, 5),
            orfCameraSettings.findField(OlympusCameraSettingsTag.SHARPNESS_SETTING)?.toIntArray()?.toList()
        )

        /* TagInfoRationals: Nikon Lens, Apple HdrHeadroom, Panasonic AFPointPosition. */
        val nefMakerNote = getMakerNoteDirectory(KimTestData.NEF_TEST_IMAGE_INDEX)
        assertNotNull(nefMakerNote)
        val lensField = nefMakerNote.findField(NikonTag.LENS)
        assertNotNull(lensField)
        assertEquals(
            listOf(18.0, 55.0, 3.5, 5.6),
            (lensField.value as RationalNumbers).values.map { it.doubleValue() }
        )

        val appleMakerNote = getMakerNoteDirectory(48)
        assertNotNull(appleMakerNote)
        val hdrHeadroomField = appleMakerNote.findField(AppleTag.HDR_HEADROOM)
        assertNotNull(hdrHeadroomField)
        assertEquals(46219, (hdrHeadroomField.value as RationalNumbers).values.first().numerator)

        val rw2MakerNote = getMakerNoteDirectory(KimTestData.RW2_TEST_IMAGE_INDEX)
        assertNotNull(rw2MakerNote)
        val afPointPositionField = rw2MakerNote.findField(PanasonicTag.AF_POINT_POSITION)
        assertNotNull(afPointPositionField)
        assertEquals(
            listOf(0.5, 0.5),
            (afPointPositionField.value as RationalNumbers).values.map { it.doubleValue() }
        )

        /* TagInfoByte: Nikon LensType, Olympus AFFineTune. */
        val nikonMakerNote = getMakerNoteDirectory(15)
        assertNotNull(nikonMakerNote)
        assertEquals(14, nefMakerNote.findField(NikonTag.LENS_TYPE)?.toInt())
        assertEquals(0, orfCameraSettings.findField(OlympusCameraSettingsTag.AF_FINE_TUNE)?.toInt())

        /* TagInfoBytes: Olympus Equipment LensType and Extender. */
        val e500Contents = getTiffContents(39)
        val e500Equipment = requireNotNull(
            e500Contents.findMakerNoteSubDirectory(TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT)
        )
        assertEquals(
            listOf(0, 0, 20, 16, 0, 0),
            e500Equipment.findField(OlympusEquipmentTag.LENS_TYPE)?.valueBytes?.map { it.toInt() }
        )
        assertEquals(
            listOf(0, 0, 0, 0, 0, 0),
            e500Equipment.findField(OlympusEquipmentTag.EXTENDER)?.valueBytes?.map { it.toInt() }
        )

        /* TagInfoSRationals: Olympus FlashExposureComp. */
        assertEquals(
            -2.0,
            orfCameraSettings.findField(OlympusCameraSettingsTag.FLASH_EXPOSURE_COMP)?.toDouble()
        )

        /* TagInfoLongs and TagInfoLong already covered in the other tests. */

        /* TagInfoAscii, TagInfoUndefineds and TagInfoInt64 already covered
         * in the manufacturer specific tests. */
    }

    @Test
    fun testSonyEricssonMakerNoteParsing() {

        val makerNoteDirectory = getMakerNoteDirectory(41)

        assertNotNull(makerNoteDirectory)
        assertEquals(TiffConstants.TIFF_MAKER_NOTE_SONY_ERICSSON, makerNoteDirectory.type)

        val versionField = makerNoteDirectory.findField(SonyTag.MAKER_NOTE_VERSION)
        assertNotNull(versionField)
        assertEquals("0200", versionField.valueBytes.decodeToString())

        assertEquals(62, makerNoteDirectory.findField(SonyTag.PREVIEW_IMAGE_START)?.toInt())
        assertEquals(51856, makerNoteDirectory.findField(SonyTag.PREVIEW_IMAGE_LENGTH)?.toInt())
    }

    @Test
    fun testMakerNoteTagNameResolution() {

        val makerNoteDirectory = getMakerNoteDirectory(15)

        assertNotNull(makerNoteDirectory)

        val qualityField = makerNoteDirectory.findField(NikonTag.QUALITY)
        assertNotNull(qualityField)
        assertEquals("Quality", qualityField.tagInfo?.name)
    }

    private fun getMakerNoteDirectory(index: Int): TiffDirectory? {

        val metadata = Kim.readMetadata(KimTestData.getBytesOf(index))

        return metadata?.exif?.makerNoteDirectory
    }

    private fun getTiffContents(index: Int): TiffContents {

        val metadata = Kim.readMetadata(KimTestData.getBytesOf(index))

        return requireNotNull(metadata?.exif)
    }
}
