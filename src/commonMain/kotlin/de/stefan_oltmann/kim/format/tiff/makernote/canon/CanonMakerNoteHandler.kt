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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_AF_INFO2
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_ASPECT_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_CROP_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_FILE_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_FILTER_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_HDR_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_LENS_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_LIGHTING_OPT
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_MEASURED_COLOR
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_MULTI_EXP
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_PROCESSING_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_SENSOR_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_SHOT_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_TIME_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteBlobPointer
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Reads the MakerNote of Canon cameras.
 */
@Suppress("MagicNumber")
internal object CanonMakerNoteHandler : MakerNoteHandler() {

    /**
     * The model specific CameraInfo tables of the Canon MakerNote.
     */
    private val CAMERA_INFO_MODELS: Map<String, MakerNoteBlobPointer> = mapOf(
        "EOS-1D Mark II N" to canonCameraInfo(CanonCameraInfo1DmkIINTag.ALL),
        "EOS-1D Mark II" to canonCameraInfo(CanonCameraInfo1DmkIITag.ALL),
        "EOS-1D Mark III" to canonCameraInfo(CanonCameraInfo1DmkIIITag.ALL),
        "EOS-1D Mark IV" to canonCameraInfo(CanonCameraInfo1DmkIVTag.ALL),
        "EOS-1D X" to canonCameraInfo(CanonCameraInfo1DXTag.ALL),
        "EOS-1D" to canonCameraInfo(CanonCameraInfo1DTag.ALL),
        "EOS 5D Mark II" to canonCameraInfo(CanonCameraInfo5DmkIITag.ALL),
        "EOS 5D Mark III" to canonCameraInfo(CanonCameraInfo5DmkIIITag.ALL),
        "EOS 5D" to canonCameraInfo(CanonCameraInfo5DTag.ALL),
        "EOS 6D" to canonCameraInfo(CanonCameraInfo6DTag.ALL),
        "EOS 7D" to canonCameraInfo(CanonCameraInfo7DTag.ALL),
        "EOS 40D" to canonCameraInfo(CanonCameraInfo40DTag.ALL),
        "EOS 50D" to canonCameraInfo(CanonCameraInfo50DTag.ALL),
        "EOS 60D" to canonCameraInfo(CanonCameraInfo60DTag.ALL),
        "EOS 70D" to MakerNoteBlobPointer(
            tagId = 0x000d,
            directoryType = TIFF_MAKER_NOTE_CANON_CAMERA_INFO,
            tagTable = CanonCameraInfo70DTag.ALL,
            byteOffsetMultiplier = 1,
            nestedBlobPointers = listOf(
                MakerNoteBlobPointer(
                    tagId = 0x3cf,
                    directoryType = TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO,
                    tagTable = CanonPictureStyleInfoTag.ALL,
                    byteOffsetMultiplier = 4
                )
            )
        ),
        "EOS 80D" to canonCameraInfo(CanonCameraInfo80DTag.ALL),
        "EOS 450D" to canonCameraInfo(CanonCameraInfo450DTag.ALL),
        "EOS 500D" to canonCameraInfo(CanonCameraInfo500DTag.ALL),
        "EOS 550D" to canonCameraInfo(CanonCameraInfo550DTag.ALL),
        "EOS 600D" to canonCameraInfo(CanonCameraInfo600DTag.ALL),
        "EOS 650D" to canonCameraInfo(CanonCameraInfo650DTag.ALL),
        "EOS 750D" to canonCameraInfo(CanonCameraInfo750DTag.ALL),
        "EOS 1000D" to canonCameraInfo(CanonCameraInfo1000DTag.ALL),
        "EOS R6 Mark II" to canonCameraInfo(CanonCameraInfoR6m2Tag.ALL),
        "EOS R6 Mark III" to canonCameraInfo(CanonCameraInfoR6m3Tag.ALL),
        "EOS R6" to canonCameraInfo(CanonCameraInfoR6Tag.ALL),
        "G5 X Mark II" to canonCameraInfo(CanonCameraInfoG5XIITag.ALL),
        "PowerShot" to canonCameraInfo(CanonCameraInfoPowerShot2Tag.ALL),
        "Unknown32" to canonCameraInfo(CanonCameraInfoUnknown32Tag.ALL)
    )

    /**
     * The binary sub-directories of the Canon MakerNote.
     */
    private val BLOB_POINTERS: List<MakerNoteBlobPointer> = listOf(
        MakerNoteBlobPointer(0x0001, TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS, CanonCameraSettingsTag.ALL, 2),
        MakerNoteBlobPointer(0x0002, TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH, CanonFocalLengthTag.ALL, 2),
        MakerNoteBlobPointer(
            0x0004,
            TIFF_MAKER_NOTE_CANON_SHOT_INFO,
            CanonShotInfoTag.ALL,
            2,
            fieldFilter = { fields ->
                /* Like ExifTool, the focus distance is only shown when the upper value is non-zero. */
                val focusDistanceUpper = fields.find { it.tag == 0x13 }?.toInt() ?: 0

                if (focusDistanceUpper == 0)
                    fields.filterNot { it.tag == 0x14 }
                else
                    fields
            }
        ),
        MakerNoteBlobPointer(
            0x000d,
            TIFF_MAKER_NOTE_CANON_CAMERA_INFO,
            CanonCameraInfoUnknownTag.ALL,
            1,
            modelTables = CAMERA_INFO_MODELS
        ),
        MakerNoteBlobPointer(0x0026, TIFF_MAKER_NOTE_CANON_AF_INFO2, CanonAfInfo2Tag.ALL, 2),
        MakerNoteBlobPointer(0x0035, TIFF_MAKER_NOTE_CANON_TIME_INFO, CanonTimeInfoTag.ALL, 4),
        MakerNoteBlobPointer(0x0093, TIFF_MAKER_NOTE_CANON_FILE_INFO, CanonFileInfoTag.ALL, 2),
        MakerNoteBlobPointer(0x0098, TIFF_MAKER_NOTE_CANON_CROP_INFO, CanonCropInfoTag.ALL, 2),
        MakerNoteBlobPointer(0x009a, TIFF_MAKER_NOTE_CANON_ASPECT_INFO, CanonAspectInfoTag.ALL, 4),
        MakerNoteBlobPointer(0x00a0, TIFF_MAKER_NOTE_CANON_PROCESSING_INFO, CanonProcessingTag.ALL, 2),
        MakerNoteBlobPointer(0x00aa, TIFF_MAKER_NOTE_CANON_MEASURED_COLOR, CanonMeasuredColorTag.ALL, 2),
        MakerNoteBlobPointer(0x00e0, TIFF_MAKER_NOTE_CANON_SENSOR_INFO, CanonSensorInfoTag.ALL, 2),
        MakerNoteBlobPointer(0x4013, TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ, CanonAfMicroAdjTag.ALL, 4),
        MakerNoteBlobPointer(0x4015, TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR, CanonVignettingCorrTag.ALL, 2),
        MakerNoteBlobPointer(0x4016, TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2, CanonVignettingCorr2Tag.ALL, 4),
        MakerNoteBlobPointer(0x4018, TIFF_MAKER_NOTE_CANON_LIGHTING_OPT, CanonLightingOptTag.ALL, 4),
        MakerNoteBlobPointer(0x4019, TIFF_MAKER_NOTE_CANON_LENS_INFO, CanonLensInfoTag.ALL, 1),
        MakerNoteBlobPointer(0x4020, TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO, CanonAmbienceTag.ALL, 4),
        MakerNoteBlobPointer(0x4021, TIFF_MAKER_NOTE_CANON_MULTI_EXP, CanonMultiExpTag.ALL, 4),
        MakerNoteBlobPointer(0x4025, TIFF_MAKER_NOTE_CANON_HDR_INFO, CanonHdrInfoTag.ALL, 4)
    )

    /**
     * The model specific CustomFunctions tables of the Canon MakerNote.
     */
    private val CUSTOM_FUNCTIONS_MODELS: Map<String, List<TagInfo>> = mapOf(
        "EOS-1D" to CanonCustomFunctions1DTag.ALL,
        "EOS 5D" to CanonCustomFunctions5DTag.ALL,
        "EOS 10D" to CanonCustomFunctions10DTag.ALL,
        "EOS 20D" to CanonCustomFunctions20DTag.ALL,
        "EOS 30D" to CanonCustomFunctions30DTag.ALL,
        "350D" to CanonCustomFunctions350DTag.ALL,
        "REBEL XT" to CanonCustomFunctions350DTag.ALL,
        "Kiss Digital N" to CanonCustomFunctions350DTag.ALL,
        "400D" to CanonCustomFunctions400DTag.ALL,
        "REBEL XTi" to CanonCustomFunctions400DTag.ALL,
        "Kiss Digital X" to CanonCustomFunctions400DTag.ALL,
        "EOS D30" to CanonCustomFunctionsD30Tag.ALL,
        "EOS D60" to CanonCustomFunctionsD30Tag.ALL
    )

    /**
     * A CameraInfo table of the Canon MakerNote.
     */
    private fun canonCameraInfo(tagTable: List<TagInfo>): MakerNoteBlobPointer =
        MakerNoteBlobPointer(
            tagId = 0x000d,
            directoryType = TIFF_MAKER_NOTE_CANON_CAMERA_INFO,
            tagTable = tagTable,
            byteOffsetMultiplier = 1
        )

    /**
     * Reads the MakerNote of a Canon camera.
     *
     * Canon MakerNotes use absolute file offsets for their values.
     * The sub-directories are stored as binary blobs with the fields
     * at fixed short offsets, so they are read with the blob parser.
     *
     * Like ExifTool, the value offsets are corrected when the MakerNote
     * ends with a TIFF footer that stores the original offset of the
     * data: some cameras and tools move the MakerNote without updating
     * the offsets inside it.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        makerNoteLength: Int,
        byteOrder: ByteOrder,
        model: String?,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset,
            valueOffsetBase = resolveBaseAdjustment(
                byteReader = byteReader,
                makerNoteValueOffset = makerNoteValueOffset,
                makerNoteLength = makerNoteLength,
                byteOrder = byteOrder
            ),
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_CANON,
            addDirectory = {
                makerNoteDirectory = it
                addDirectory(it)
            }
        )

        makerNoteDirectory?.let { directory ->
            readSubDirectories(
                directory = directory,
                byteOrder = byteOrder,
                model = model,
                addDirectory = addDirectory
            )
        }
    }

    /**
     * Reads the sub-directories of the given Canon MakerNote directory.
     *
     * Used for the MakerNote of JPEG and TIFF images as well as for
     * the MakerNote stored in the CMT3 box of CR3 images.
     */
    internal fun readSubDirectories(
        directory: TiffDirectory,
        byteOrder: ByteOrder,
        model: String?,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        readMakerNoteBlobSubDirectories(
            directory = directory,
            valueOffsetBase = 0,
            byteOrder = byteOrder,
            blobPointers = BLOB_POINTERS,
            addDirectory = addDirectory,
            model = model
        )

        readFilterInfo(
            directory = directory,
            valueOffsetBase = 0,
            byteOrder = byteOrder,
            addDirectory = addDirectory
        )

        readCustomFunctions(
            directory = directory,
            valueOffsetBase = 0,
            byteOrder = byteOrder,
            addDirectory = addDirectory
        )
    }

    /**
     * Returns the value offset base adjustment for the MakerNote, or 0
     * when no adjustment is needed.
     *
     * Canon MakerNotes can end with an 8-byte TIFF footer that stores
     * the original offset of the MakerNote data. When the MakerNote was
     * moved without updating the offsets inside it, the stored offset
     * differs from the actual position, and all value offsets have to
     * be shifted by that difference, like ExifTool's FixBase.
     */
    private fun resolveBaseAdjustment(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        makerNoteLength: Int,
        byteOrder: ByteOrder
    ): Int {

        if (makerNoteLength < 8)
            return 0

        val footerBytes = byteReader.readBytes(
            makerNoteValueOffset + makerNoteLength - 8,
            8
        )

        if (footerBytes.size != 8)
            return 0

        val footerLittleEndian = footerBytes[0] == 'I'.code.toByte() &&
            footerBytes[1] == 'I'.code.toByte() &&
            footerBytes[2].toInt() == 0x2a &&
            footerBytes[3] == 0.toByte()

        val footerBigEndian = footerBytes[0] == 'M'.code.toByte() &&
            footerBytes[1] == 'M'.code.toByte() &&
            footerBytes[2] == 0.toByte() &&
            footerBytes[3].toInt() == 0x2a

        if (!footerLittleEndian && !footerBigEndian)
            return 0

        val footerMarkerMatchesByteOrder =
            (footerLittleEndian && byteOrder == ByteOrder.LITTLE_ENDIAN) ||
                (footerBigEndian && byteOrder == ByteOrder.BIG_ENDIAN)

        if (!footerMarkerMatchesByteOrder)
            return 0

        val footerByteOrder = if (footerLittleEndian)
            ByteOrder.LITTLE_ENDIAN
        else
            ByteOrder.BIG_ENDIAN

        /* The footer stores the original offset of the MakerNote data. */
        val originalOffset = footerBytes.toInt32(4, footerByteOrder)

        return makerNoteValueOffset - originalOffset
    }

    /**
     * Reads the custom functions of the Canon MakerNote.
     *
     * The data is a list of function groups, each with a list of
     * parameters that carry the settings.
     */
    private fun readCustomFunctions(
        directory: TiffDirectory,
        valueOffsetBase: Int,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        val field = directory.entries.find { it.tag == 0x000f } ?: return

        val blob = field.valueBytes

        if (blob.size < 8)
            return

        val numGroups = blob.toInt32(4, byteOrder)

        if (numGroups <= 0 || numGroups > 100)
            return

        var pos = 8

        val fields = mutableListOf<TiffField>()

        for (groupIndex in 0 until numGroups) {

            if (pos + 12 > blob.size)
                return

            val numParams = blob.toInt32(pos + 8, byteOrder)

            pos += 12

            for (paramIndex in 0 until numParams) {

                if (pos + 8 > blob.size)
                    return

                val tag = blob.toInt32(pos, byteOrder)
                val count = blob.toInt32(pos + 4, byteOrder)

                pos += 8

                /* Guard against corrupt counts, which can overflow the multiplication below. */
                if (count < 0 || count > (blob.size - pos) / 4)
                    return

                val valueLength = count * 4

                if (pos + valueLength > blob.size)
                    return

                val valueBytes = blob.copyOfRange(pos, pos + valueLength)

                pos += valueLength

                fields.add(
                    TiffField(
                        offset = valueOffsetBase + pos - valueLength,
                        tag = tag,
                        directoryType = TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS,
                        fieldType = FieldTypeLong,
                        count = count,
                        localValue = null,
                        valueOffset = valueOffsetBase + pos - valueLength,
                        valueBytes = valueBytes,
                        byteOrder = byteOrder,
                        sortHint = tag
                    )
                )
            }
        }

        if (fields.isEmpty())
            return

        addDirectory(
            TiffDirectory(
                type = TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS,
                entries = fields,
                offset = valueOffsetBase + (field.valueOffset ?: 0),
                nextDirectoryOffset = 0,
                byteOrder = byteOrder
            )
        )
    }

    /**
     * Reads the creative filter settings of the Canon MakerNote.
     *
     * The data is a list of filters, each with a list of parameters
     * that carry the settings.
     */
    private fun readFilterInfo(
        directory: TiffDirectory,
        valueOffsetBase: Int,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        val field = directory.entries.find { it.tag == 0x4024 } ?: return

        val blob = field.valueBytes

        if (blob.size < 8)
            return

        val numFilters = blob.toInt32(4, byteOrder)

        if (numFilters < 0)
            return

        var pos = 8

        val fields = mutableListOf<TiffField>()

        for (filterIndex in 0 until numFilters) {

            if (pos + 12 > blob.size)
                return

            val size = blob.toInt32(pos + 4, byteOrder)
            val numParams = blob.toInt32(pos + 8, byteOrder)

            if (size < 0 || numParams < 0)
                return

            pos += 12

            for (paramIndex in 0 until numParams) {

                if (pos + 8 > blob.size)
                    return

                val tag = blob.toInt32(pos, byteOrder)
                val count = blob.toInt32(pos + 4, byteOrder)

                pos += 8

                /* Guard against corrupt counts, which can overflow the multiplication below. */
                if (count < 0 || count > (blob.size - pos) / 4)
                    return

                val valueLength = count * 4

                if (pos + valueLength > blob.size)
                    return

                val valueBytes = blob.copyOfRange(pos, pos + valueLength)

                pos += valueLength

                fields.add(
                    TiffField(
                        offset = valueOffsetBase + pos - valueLength,
                        tag = tag,
                        directoryType = TIFF_MAKER_NOTE_CANON_FILTER_INFO,
                        fieldType = FieldTypeLong,
                        count = count,
                        localValue = null,
                        valueOffset = valueOffsetBase + pos - valueLength,
                        valueBytes = valueBytes,
                        byteOrder = byteOrder,
                        sortHint = tag
                    )
                )
            }
        }

        addDirectory(
            TiffDirectory(
                type = TIFF_MAKER_NOTE_CANON_FILTER_INFO,
                entries = fields,
                offset = valueOffsetBase + (field.valueOffset ?: 0),
                nextDirectoryOffset = 0,
                byteOrder = byteOrder
            )
        )
    }
}




