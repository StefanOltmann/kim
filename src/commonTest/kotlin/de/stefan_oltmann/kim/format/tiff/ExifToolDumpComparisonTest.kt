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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Compares the parsed MakerNote values against the ExifTool dumps
 * of the test files, field by field and byte by byte.
 */
class ExifToolDumpComparisonTest {

    private data class DumpField(
        val tag: Int,
        val name: String,
        val bytes: ByteArray
    )

    private data class DumpDirectory(
        val name: String,
        val fields: List<DumpField>,
        val subDirectories: List<DumpDirectory>
    )

    private data class ParseResult(
        val directory: DumpDirectory,
        val endIndex: Int
    )

    private val makerNoteTestFiles: List<Int> = listOf(
        1, 15, 18, 21, 23, 28, 31, 34, 39, 41, 42, 48, 49, 50, 53,
        57, 58, 60, 62, 63, 64, 65, 72, 73, 74, 75, 83, 86, 87, 88
    )
    private val entryLineRegex = Regex("""^[\s|]*\d+\)\s+""")
    private val subDirectoryRegex = Regex("""^[\s|]*\d+\)\s+(\w+) \(SubDirectory\) -->$""")
    private val unnumberedSubDirectoryRegex = Regex("""^[\s|]*(\w+) \(SubDirectory\) -->$""")
    private val fieldRegex = Regex("""^[\s|]*\d+\)\s+([\w.-]+) = .*$""")
    private val binaryFieldRegex = Regex("""^[\s|]*([\w.-]+) = .*$""")
    private val tagRegex = Regex("""- Tag 0x([0-9a-fA-F]+)(?:, mask 0x[0-9a-fA-F]+)? \((\d+) bytes""")

    /*
     * Tags that the reference dumps (ExifTool 13.59) do not know yet,
     * although the current ExifTool tag tables document them. Kim
     * follows the current tables, so these fields are allowed to be
     * named differently or to exist only in kim. The kim name is part
     * of the entry: a future rename has to update it here.
     */
    private val docAheadTags: Set<Triple<String, Int, String>> = setOf(
        Triple("MakerNote", 0x38, "BatteryType"),
        Triple("MakerNote", 0x4020, "AmbienceInfo"),
        Triple("MakerNote", 0x1304, "GEImageSize"),
        Triple("MakerNote", 0x2000, "MakerNoteVersion"),
        Triple("MakerNote", 0x1003, "Panorama"),
        Triple("MakerNote", 0xb050, "HighISONoiseReduction2"),
        Triple("MakerNote/CanonFileInfo", 0x10, "MacroMagnification"),
        Triple("MakerNote/VignettingCorr", 0x0, "VignettingCorrVersion"),
        Triple("MakerNote/CanonCameraInfo60D", 0x3a, "CameraOrientation"),
        Triple("MakerNote/CanonCameraInfo60D", 0x7d, "ColorTemperature"),
        Triple("MakerNote/CanonCameraInfo60D", 0xe8, "LensType"),
        Triple("MakerNote/CanonCameraInfo60D", 0xea, "MinFocalLength"),
        Triple("MakerNote/CanonCameraInfo60D", 0xec, "MaxFocalLength"),
        Triple("MakerNote/CanonCameraInfo60D", 0x1d9, "FileIndex"),
        Triple("MakerNote/CanonCameraInfo60D", 0x1e5, "DirectoryIndex"),
        Triple("MakerNote/ShotInfo02xx", 0x10, "DistortionControl"),
        Triple("MakerNote/ShotInfo02xx", 0x66, "VR_0x66"),
        Triple("MakerNote/ShotInfo02xx", 0x6a, "ShutterCount"),
        Triple("MakerNote/ShotInfo02xx", 0x6e, "DeletedImageCount"),
        Triple("MakerNote/ShotInfo02xx", 0x75, "VibrationReduction"),
        Triple("MakerNote/ShotInfo02xx", 0x82, "VibrationReduction"),
        Triple("MakerNote/ShotInfo02xx", 0x157, "ShutterCount"),
        Triple("MakerNote/ShotInfo02xx", 0x1ae, "VibrationReduction"),
        Triple("MakerNote/ShotInfo02xx", 0x24d, "ShutterCount"),
        Triple("MakerNote/AFInfo2", 0x10, "AFImageWidth"),
        Triple("MakerNote/AFInfo2", 0x12, "AFImageHeight"),
        Triple("MakerNote/AFInfo2", 0x14, "AFAreaXPosition"),
        Triple("MakerNote/AFInfo2", 0x16, "AFAreaYPosition"),
        Triple("MakerNote/AFInfo2", 0x18, "AFAreaWidth"),
        Triple("MakerNote/AFInfo2", 0x1a, "AFAreaHeight"),
        Triple("MakerNote/AFInfo2", 0x1c, "ContrastDetectAFInFocus"),
        Triple("MakerNote/ShotInfoD5100", 0x407, "CustomSettingsD5100"),
        Triple("MakerNote/ColorBalance0215", 0x0, "WB_RGGBLevels"),
        Triple("MakerNote/FocusInfoIFD", 0x1600, "ImageStabilization"),
        Triple("MakerNote/FocusInfoIFD", 0x212, "SceneDetectData"),
        Triple("MakerNote/FocusInfoIFD", 0x328, "AFInfo"),
        Triple("MakerNote/ImageProcessingIFD", 0x635, "UnknownBlock1"),
        Triple("MakerNote/ImageProcessingIFD", 0x636, "UnknownBlock2"),
        Triple("MakerNote/ImageProcessingIFD", 0x1103, "UnknownBlock3"),
        Triple("MakerNote/ImageProcessingIFD", 0x1104, "UnknownBlock4")
    )


    @Test
    fun testMakerNoteValuesMatchExifToolDumps() {

        val failures = mutableListOf<String>()
        val fileSummaries = mutableListOf<String>()
        var comparedFieldCount = 0

        for (index in makerNoteTestFiles) {

            val dumpText = Resource(
                "de/stefan_oltmann/kim/testdata/exiftool/media_$index.txt"
            ).readText()

            val dumpDirectory = parseMakerNoteDump(dumpText)

            val metadata = Kim.readMetadata(KimTestData.getBytesOf(index))
            val contents = requireNotNull(metadata?.exif)
            val makerNoteDirectory = contents.makerNoteDirectory

            fileSummaries.add(
                "media_$index: dump fields=${dumpDirectory?.fields?.size ?: -1} " +
                    "subs=${dumpDirectory?.subDirectories?.size ?: -1} " +
                    "kim type=${makerNoteDirectory?.type ?: -1}"
            )

            if (dumpDirectory == null || makerNoteDirectory == null)
                continue


            comparedFieldCount += compareDirectories(
                mediaIndex = index,
                dumpDirectory = dumpDirectory,
                kimDirectory = makerNoteDirectory,
                kimSubDirectories = contents.makerNoteSubDirectories,
                path = "MakerNote",
                failures = failures
            )
        }

        /* Make sure the dumps were actually parsed and compared. */
        assertTrue(
            comparedFieldCount > 200,
            "Only $comparedFieldCount fields compared.\n${fileSummaries.joinToString("\n")}"
        )

        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    private fun isDocAheadTag(path: String, tag: Int, kimName: String?): Boolean =
        kimName != null && Triple(path, tag, kimName) in docAheadTags

    private fun compareDirectories(
        mediaIndex: Int,
        dumpDirectory: DumpDirectory,
        kimDirectory: TiffDirectory,
        kimSubDirectories: List<TiffDirectory>,
        path: String,
        failures: MutableList<String>
    ): Int {

        var comparedFieldCount = 0

        for (dumpField in dumpDirectory.fields) {

            /* The Nikon AFPointsUsed size depends on the camera model. */
            if (path == "MakerNote/AFInfo2" && dumpField.tag == 0x8)
                continue

            /*
             * The Canon AF area data is packed after the fixed prefix,
             * with offsets that depend on the number of AF points.
             */
            if (path == "MakerNote/CanonAFInfo2" && dumpField.tag in 0x8..0xd)
                continue

            val kimField = kimDirectory.entries.find { it.tag == dumpField.tag }

            if (kimField == null) {

                failures.add(
                    "media_$mediaIndex $path: field 0x${dumpField.tag.toString(16)} " +
                        "(${dumpField.name}) missing in kim"
                )
                continue
            }

            /* Fields whose hex dump was snipped by ExifTool cannot be verified. */
            if (dumpField.bytes.size != kimField.valueBytes.size) {
                failures.add(
                    "media_$mediaIndex $path: field 0x${dumpField.tag.toString(16)} " +
                        "(${dumpField.name}) size mismatch: " +
                        "dump=${dumpField.bytes.size} kim=${kimField.valueBytes.size}"
                )
                continue
            }

            comparedFieldCount++

            if (!kimField.valueBytes.contentEquals(dumpField.bytes)) {

                failures.add(
                    "media_$mediaIndex $path: field 0x${dumpField.tag.toString(16)} " +
                        "(${dumpField.name}) value mismatch: " +
                        "expected ${dumpField.bytes.headHex()}... " +
                        "but was ${kimField.valueBytes.headHex()}..."
                )
            }

            if (kimField.tagInfo?.name != dumpField.name &&
                !isDocAheadTag(path, dumpField.tag, kimField.tagInfo?.name)
            ) {

                failures.add(
                    """media_$mediaIndex $path: field 0x${dumpField.tag.toString(16)} """ +
                        """name mismatch: dump="${dumpField.name}" """ +
                        """kim="${kimField.tagInfo?.name}""""
                )
            }
        }

        /*
         * Every kim field of a sub-directory must also be listed in the dump:
         * a kim-only field means the blob table over-reads for this camera.
         */
        if (path != "MakerNote") {

            val dumpTags = dumpDirectory.fields.map { it.tag }.toSet()

            for (kimField in kimDirectory.entries) {

                if (kimField.tag !in dumpTags &&
                    !isDocAheadTag(path, kimField.tag, kimField.tagInfo?.name)
                ) {

                    failures.add(
                        "media_$mediaIndex $path: field 0x${kimField.tag.toString(16)} " +
                            "(${kimField.tagInfo?.name}) extra in kim"
                    )
                }
            }
        }

        for (subDirectory in dumpDirectory.subDirectories) {

            /*
             * Skip sub-directories without parsed fields, for example the
             * Canon FilterInfo of media_53, which ExifTool could not parse
             * either ("Invalid FilterInfo data"). There is nothing to compare.
             */
            if (subDirectory.fields.isEmpty())
                continue

            val directoryType = getSubDirectoryType(
                name = subDirectory.name,
                parentType = kimDirectory.type
            ) ?: continue

            val kimSubDirectory = kimSubDirectories.find { it.type == directoryType }

            if (kimSubDirectory == null) {

                failures.add(
                    "media_$mediaIndex $path: sub-directory ${subDirectory.name} missing in kim"
                )
                continue
            }

            comparedFieldCount += compareDirectories(
                mediaIndex = mediaIndex,
                dumpDirectory = subDirectory,
                kimDirectory = kimSubDirectory,
                kimSubDirectories = emptyList(),
                path = "$path/${subDirectory.name}",
                failures = failures
            )
        }

        return comparedFieldCount
    }

    private fun getSubDirectoryType(name: String, parentType: Int): Int? {

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_CANON)
            return getCanonSubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_NIKON)
            return getNikonSubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_PANASONIC)
            return getPanasonicSubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_SONY5)
            return getSony5SubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_OLYMPUS)
            return getOlympusSubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_FUJIFILM)
            return getFujiFilmSubDirectoryType(name)

        if (parentType == TiffConstants.TIFF_MAKER_NOTE_APPLE)
            return getAppleSubDirectoryType(name)

        return null
    }

    private fun getAppleSubDirectoryType(name: String): Int? =
        if (name == "RunTime")
            TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME
        else
            null

    private fun getCanonSubDirectoryType(name: String): Int? = when (name) {
        "CanonCameraSettings" -> TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_SETTINGS
        "CanonShotInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_SHOT_INFO
        "TimeInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_TIME_INFO
        "CanonFileInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_FILE_INFO
        "ProcessingInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_PROCESSING_INFO
        "CanonFocalLength" -> TiffConstants.TIFF_MAKER_NOTE_CANON_FOCAL_LENGTH
        "CropInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_CROP_INFO
        "AspectInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_ASPECT_INFO
        "MeasuredColor" -> TiffConstants.TIFF_MAKER_NOTE_CANON_MEASURED_COLOR
        "AFMicroAdj" -> TiffConstants.TIFF_MAKER_NOTE_CANON_AF_MICRO_ADJ
        "VignettingCorr" -> TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR
        "VignettingCorr2" -> TiffConstants.TIFF_MAKER_NOTE_CANON_VIGNETTING_CORR2
        "LightingOpt" -> TiffConstants.TIFF_MAKER_NOTE_CANON_LIGHTING_OPT
        "LensInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_LENS_INFO
        "AmbienceInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_AMBIENCE_INFO
        "MultiExp" -> TiffConstants.TIFF_MAKER_NOTE_CANON_MULTI_EXP
        "HDRInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_HDR_INFO
        "CanonAFInfo2" -> TiffConstants.TIFF_MAKER_NOTE_CANON_AF_INFO2
        "SensorInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_SENSOR_INFO
        "FilterInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_FILTER_INFO
        "CanonCameraInfo70D" -> TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO
        "CanonCameraInfo60D" -> TiffConstants.TIFF_MAKER_NOTE_CANON_CAMERA_INFO
        "PictureStyleInfo" -> TiffConstants.TIFF_MAKER_NOTE_CANON_PICTURE_STYLE_INFO
        "CustomFunctionsUnknown" -> TiffConstants.TIFF_MAKER_NOTE_CANON_CUSTOM_FUNCTIONS
        else -> null
    }

    private fun getNikonSubDirectoryType(name: String): Int? = when (name) {
        "PreviewIFD" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD
        "VRInfo" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_VR_INFO
        "PictureControlData" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL
        "WorldTime" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_WORLD_TIME
        "ISOInfo" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_ISO_INFO
        "DistortInfo" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_DISTORT_INFO
        "ShotInfoD5100" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO
        "ShotInfo02xx" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO
        "ColorBalance0215" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE
        "ColorBalance0219" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE
        "LensData0204" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_LENS_DATA
        "FlashInfo0103" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO
        "FlashInfo0107" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO
        "MultiExposure" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE
        "AFInfo2" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_AF_INFO2
        "FileInfo" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_FILE_INFO
        "RetouchInfo" -> TiffConstants.TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO
        else -> null
    }

    private fun getPanasonicSubDirectoryType(name: String): Int? = when (name) {
        "FaceDetInfo" -> TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_DET_INFO
        "FaceRecInfo" -> TiffConstants.TIFF_MAKER_NOTE_PANASONIC_FACE_REC_INFO
        "TimeInfo" -> TiffConstants.TIFF_MAKER_NOTE_PANASONIC_TIME_INFO
        else -> null
    }

    private fun getSony5SubDirectoryType(name: String): Int? = when (name) {
        "CameraInfo3" -> TiffConstants.TIFF_MAKER_NOTE_SONY_CAMERA_INFO3
        "MoreSettings" -> TiffConstants.TIFF_MAKER_NOTE_SONY_MORE_SETTINGS
        "FaceInfoA" -> TiffConstants.TIFF_MAKER_NOTE_SONY_FACE_INFO
        else -> null
    }

    private fun getOlympusSubDirectoryType(name: String): Int? = when (name) {
        "EquipmentIFD" -> TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_EQUIPMENT
        "CameraSettingsIFD" -> TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
        "RawDevelopmentIFD" -> TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_RAW_DEVELOPMENT
        "ImageProcessingIFD" -> TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_IMAGE_PROCESSING
        "FocusInfoIFD" -> TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO
        else -> null
    }

    private fun getFujiFilmSubDirectoryType(name: String): Int? = when (name) {
        "PrioritySettings" -> TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_PRIORITY_SETTINGS
        "FocusSettings" -> TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS
        "AFCSettings" -> TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_AFC_SETTINGS
        "DriveSettings" -> TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS
        else -> null
    }

    /**
     * Parses the MakerNote section of an ExifTool text dump.
     */
    private fun parseMakerNoteDump(dumpText: String): DumpDirectory? {

        val lines = dumpText.lines().map { it.trimEnd('\r') }

        val makerNoteLineIndex = lines.indexOfFirst {
            it.contains("MakerNote") && it.contains("(SubDirectory)")
        }

        if (makerNoteLineIndex < 0)
            return null

        val makerNoteDepth = getPipeDepth(lines[makerNoteLineIndex])

        val result = parseDirectory(
            lines = lines,
            startIndex = makerNoteLineIndex,
            fieldDepth = makerNoteDepth + 1,
            name = "MakerNote"
        )

        return result.directory
    }

    private fun parseDirectory(
        lines: List<String>,
        startIndex: Int,
        fieldDepth: Int,
        name: String
    ): ParseResult {

        val fields = mutableListOf<DumpField>()
        val subDirectories = mutableListOf<DumpDirectory>()

        var index = startIndex + 1

        while (index < lines.size) {

            val line = lines[index]
            val depth = getPipeDepth(line)

            /* Skip hex dumps, tag lines, snips and directory markers. */
            if (isSkippableLine(line)) {
                index++
                continue
            }

            /* A shallower entry ends this section. */
            if (depth < fieldDepth && isEntryLine(line))
                break

            val subDirectoryMatch = subDirectoryRegex.find(line)
                ?: unnumberedSubDirectoryRegex.find(line)

            if (subDirectoryMatch != null) {

                val subDirectory = parseDirectory(
                    lines = lines,
                    startIndex = index,
                    fieldDepth = depth + 1,
                    name = subDirectoryMatch.groupValues[1]
                )

                subDirectories.add(subDirectory.directory)
                index = subDirectory.endIndex
                continue
            }

            val fieldMatch = fieldRegex.find(line)

            if (fieldMatch != null) {

                parseDumpField(
                    lines = lines,
                    index = index,
                    name = fieldMatch.groupValues[1],
                    fields = fields
                )

                index++
                continue
            }

            /* The BinaryData sub-directories use unnumbered field lines. */
            val binaryFieldMatch = binaryFieldRegex.find(line)

            if (binaryFieldMatch != null) {

                parseDumpField(
                    lines = lines,
                    index = index,
                    name = binaryFieldMatch.groupValues[1],
                    fields = fields
                )

                index++
                continue
            }

            index++
        }

        return ParseResult(DumpDirectory(name, fields, subDirectories), index)
    }

    /**
     * Parses a dump field line together with its tag line and hex dump.
     */
    private fun parseDumpField(
        lines: List<String>,
        index: Int,
        name: String,
        fields: MutableList<DumpField>
    ) {

        val tagLine = lines.getOrNull(index + 1)

        val tagMatch = tagLine?.let {
            tagRegex.find(it)
        }

        if (tagMatch != null) {

            val tag = tagMatch.groupValues[1].toInt(16)
            val byteCount = tagMatch.groupValues[2].toInt()

            val bytes = readHexBytes(
                lines = lines,
                hexDepth = getPipeDepth(tagLine),
                startIndex = index + 2,
                byteCount = byteCount
            )

            /* Fields whose hex dump was snipped by ExifTool cannot be verified. */
            if (bytes != null && bytes.isNotEmpty())
                fields.add(DumpField(tag, name, bytes))
        }
    }


    private fun isEntryLine(line: String): Boolean =
        entryLineRegex.containsMatchIn(line)

    private fun isSkippableLine(line: String): Boolean =

        isHexLine(line) || line.contains("- Tag 0x") || line.contains("[snip ") ||
            line.contains("+ [") && line.contains("directory")

    private fun isHexLine(line: String): Boolean =
        Regex("""^[\s|]*[0-9a-fA-F]+:\s+[0-9a-fA-F ]+\s*\[.*""").matches(line)

    private fun readHexBytes(
        lines: List<String>,
        hexDepth: Int,
        startIndex: Int,
        byteCount: Int
    ): ByteArray? {

        val bytes = mutableListOf<Byte>()

        var index = startIndex

        while (index < lines.size && bytes.size < byteCount) {

            val line = lines[index]

            if (getPipeDepth(line) != hexDepth)
                break

            if (line.contains("[snip "))
                return null

            val hexMatch = Regex("""^[\s|]*[0-9a-fA-F]+:\s+([0-9a-fA-F ]+?)\s*\[""").find(line)

            if (hexMatch != null) {

                val hexPairs = hexMatch.groupValues[1].trim().split(" ")

                for (pair in hexPairs) {
                    if (bytes.size >= byteCount)
                        break
                    bytes.add(pair.toInt(16).toByte())
                }
            }

            index++
        }

        return bytes.toByteArray()
    }

    private fun getPipeDepth(line: String): Int {

        var index = 0

        while (index < line.length && line[index].isWhitespace())
            index++

        var count = 0

        while (index + 1 < line.length && line[index] == '|' && line[index + 1] == ' ') {
            index += 2
            count++
        }

        return count
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }

    private fun ByteArray.headHex(): String =
        copyOfRange(0, minOf(32, size)).toHex()
}

