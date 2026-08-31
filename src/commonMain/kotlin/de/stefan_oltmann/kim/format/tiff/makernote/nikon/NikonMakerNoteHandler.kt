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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_AF_INFO2
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_DISTORT_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_FILE_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_FLASH_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_HDR_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_ISO_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_LENS_DATA
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_SHOT_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_VR_INFO
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_NIKON_WORLD_TIME
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteBlobPointer
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Reads the MakerNote of Nikon cameras.
 */
internal object NikonMakerNoteHandler : MakerNoteHandler() {

    private const val NIKON_MAKER_NOTE_SIGNATURE = "Nikon\u0000"
    private const val NIKON_MAKER_NOTE_TYPE_LENGTH = 1
    private const val NIKON_MAKER_NOTE_VERSION_LENGTH = 3

    /**
     * The binary sub-directories of the Nikon MakerNote.
     */
    private val BLOB_POINTERS: List<MakerNoteBlobPointer> = listOf(
        MakerNoteBlobPointer(0x001f, TIFF_MAKER_NOTE_NIKON_VR_INFO, NikonVrInfoTag.ALL, 1),
        MakerNoteBlobPointer(
            0x0023,
            TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL,
            NikonPictureControlTag.ALL,
            1,
            versionTables = mapOf(
                "0100" to MakerNoteBlobPointer(
                    0x0023,
                    TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL,
                    NikonPictureControlTag.ALL,
                    1
                ),
                "0200" to MakerNoteBlobPointer(
                    0x0023,
                    TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL,
                    NikonPictureControl2Tag.ALL,
                    1
                )
            )
        ),
        MakerNoteBlobPointer(0x0024, TIFF_MAKER_NOTE_NIKON_WORLD_TIME, NikonWorldTimeTag.ALL, 1),
        MakerNoteBlobPointer(0x0025, TIFF_MAKER_NOTE_NIKON_ISO_INFO, NikonIsoInfoTag.ALL, 1),
        MakerNoteBlobPointer(0x002b, TIFF_MAKER_NOTE_NIKON_DISTORT_INFO, NikonDistortInfoTag.ALL, 1),
        MakerNoteBlobPointer(
            0x0091,
            TIFF_MAKER_NOTE_NIKON_SHOT_INFO,
            NikonShotInfoTag.ALL,
            1,
            encrypted = true,
            decryptStart = 4,
            versionTables = mapOf(
                "0221" to MakerNoteBlobPointer(
                    tagId = 0x0091,
                    directoryType = TIFF_MAKER_NOTE_NIKON_SHOT_INFO,
                    tagTable = NikonShotInfoD5100Tag.ALL,
                    byteOffsetMultiplier = 1,
                    encrypted = true,
                    decryptStart = 4,
                    nestedBlobPointers = listOf(
                        MakerNoteBlobPointer(
                            tagId = 0x407,
                            directoryType = TIFF_MAKER_NOTE_NIKON_CUSTOM_SETTINGS,
                            tagTable = NikonCustomSettingsD5100Tag.ALL,
                            byteOffsetMultiplier = 1
                        )
                    )
                )
            )
        ),
        MakerNoteBlobPointer(
            0x0097,
            TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE,
            NikonColorBalance4Tag.ALL,
            2,
            encrypted = true,
            decryptStart = 284,
            offsetBase = 288,
            versionTables = mapOf(
                "0219" to colorBalance0219(),
                "0220" to colorBalance0219(),
                "0221" to colorBalance0219(),
                "0222" to colorBalance0219(),
                "0223" to colorBalance0219(),
                "0224" to colorBalance0219()
            )
        ),
        MakerNoteBlobPointer(
            0x0098,
            TIFF_MAKER_NOTE_NIKON_LENS_DATA,
            NikonLensData0204Tag.ALL,
            1,
            encrypted = true,
            decryptStart = 4
        ),
        MakerNoteBlobPointer(
            0x00a8,
            TIFF_MAKER_NOTE_NIKON_FLASH_INFO,
            NikonFlashInfo0103Tag.ALL,
            1,
            versionTables = mapOf(
                "0103" to flashInfo0103(),
                "0107" to flashInfo0107()
            )
        ),
        MakerNoteBlobPointer(0x00b0, TIFF_MAKER_NOTE_NIKON_MULTI_EXPOSURE, NikonMultiExposureTag.ALL, 4),
        MakerNoteBlobPointer(0x00b7, TIFF_MAKER_NOTE_NIKON_AF_INFO2, NikonAfInfo2Tag.ALL, 1),
        MakerNoteBlobPointer(0x00b8, TIFF_MAKER_NOTE_NIKON_FILE_INFO, NikonFileInfoTag.ALL, 2),
        MakerNoteBlobPointer(0x00bb, TIFF_MAKER_NOTE_NIKON_RETOUCH_INFO, NikonRetouchInfoTag.ALL, 1),
        MakerNoteBlobPointer(0x00ba, TIFF_MAKER_NOTE_NIKON_HDR_INFO, NikonHdrInfoTag.ALL, 1)
    )

    /**
     * The sub-IFD pointers of the Nikon MakerNote with the directory
     * type of the referenced sub-directory.
     */
    private val SUB_IFD_POINTERS: List<Pair<TagInfo, Int>> = listOf(
        NikonTag.PREVIEW_IFD to TIFF_MAKER_NOTE_NIKON_PREVIEW_IFD
    )

    /**
     * The white balance data layout of the newer Nikon cameras.
     */
    private fun colorBalance0219(): MakerNoteBlobPointer =
        MakerNoteBlobPointer(
            tagId = 0x0097,
            directoryType = TIFF_MAKER_NOTE_NIKON_COLOR_BALANCE,
            tagTable = NikonColorBalance2Tag.ALL,
            byteOffsetMultiplier = 2,
            encrypted = true,
            decryptStart = 4,
            offsetBase = 0x80
        )

    /**
     * The flash info layout of cameras with FlashInfoVersion 0103.
     */
    private fun flashInfo0103(): MakerNoteBlobPointer =
        MakerNoteBlobPointer(
            tagId = 0x00a8,
            directoryType = TIFF_MAKER_NOTE_NIKON_FLASH_INFO,
            tagTable = NikonFlashInfo0103Tag.ALL,
            byteOffsetMultiplier = 1
        )

    /**
     * The flash info layout of cameras with FlashInfoVersion 0107.
     */
    private fun flashInfo0107(): MakerNoteBlobPointer =
        MakerNoteBlobPointer(
            tagId = 0x00a8,
            directoryType = TIFF_MAKER_NOTE_NIKON_FLASH_INFO,
            tagTable = NikonFlashInfo0107Tag.ALL,
            byteOffsetMultiplier = 1
        )

    /**
     * Reads the MakerNote of a Nikon camera.
     *
     * Nikon MakerNotes embed a TIFF header with their own byte order
     * and an IFD offset relative to that header.
     */
    internal fun read(
        /** Positioned at the start of the MakerNote. */
        byteReader: RandomAccessByteReader,
        /** Offset of the MakerNote relative to the embedded TIFF header. */
        makerNoteValueOffset: Int,
        /** The camera model, needed for the decryption key. */
        model: String?,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, NIKON_MAKER_NOTE_SIGNATURE))
            return

        val type = byteReader.readByteAsInt()

        /* We only have test files for type 2 right now. */
        if (type != 2)
            return

        /* Skip the version bytes. */
        byteReader.skipBytes("Nikon MakerNote version", NIKON_MAKER_NOTE_VERSION_LENGTH)

        val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

        /* Skip the TIFF magic and read the offset to the first IFD. */
        byteReader.skipBytes("Nikon MakerNote TIFF magic", 2)

        val ifdOffset = byteReader.read4BytesAsInt("Nikon MakerNote IFD offset", byteOrder)

        val tiffHeaderOffset = makerNoteValueOffset + NIKON_MAKER_NOTE_SIGNATURE.length +
            NIKON_MAKER_NOTE_TYPE_LENGTH + NIKON_MAKER_NOTE_VERSION_LENGTH

        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = tiffHeaderOffset + ifdOffset,
            valueOffsetBase = tiffHeaderOffset,
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON,
            addDirectory = {
                makerNoteDirectory = it
                addDirectory(it)
            }
        )

        makerNoteDirectory?.let { directory ->

            readMakerNoteSubDirectories(
                byteReader = byteReader,
                directory = directory,
                valueOffsetBase = tiffHeaderOffset,
                byteOrder = byteOrder,
                subIfdPointers = SUB_IFD_POINTERS,
                addDirectory = addDirectory
            )

            val serialKey = NikonDecryptor.serialKey(
                serialNumber = directory.findField(NikonTag.SERIAL_NUMBER)?.valueDescription,
                model = model
            )

            val countKey = directory.findField(NikonTag.SHUTTER_COUNT)?.toInt()

            readMakerNoteBlobSubDirectories(
                directory = directory,
                byteOrder = byteOrder,
                blobPointers = BLOB_POINTERS,
                addDirectory = addDirectory,
                serialKey = serialKey,
                countKey = countKey
            )
        }
    }
}

