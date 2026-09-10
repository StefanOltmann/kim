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
package de.stefan_oltmann.kim.format.quicktime

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Reads the Fujifilm "MVTG" box that QuickTime videos of Fujifilm cameras
 * carry in their user data.
 *
 * The MVTG payload starts with a 12-byte Fujifilm header, followed by a
 * 4-byte endian marker and a TIFF directory structure whose IFD0 begins
 * directly behind the marker. All offsets of the structure, including the
 * ones to the Exif IFD and the MakerNote, are stored relative to that
 * position, so the bytes behind the marker can be parsed like the body of
 * a TIFF file whose first IFD sits at offset zero.
 */
internal object QuickTimeMvtgReader {

    /** The Fujifilm header in front of the endian marker. */
    private const val FUJI_HEADER_LENGTH: Int = 12

    /** The length of the endian marker behind the Fujifilm header. */
    private const val ENDIAN_MARKER_LENGTH: Int = 4

    /** Offset of the IFD structure behind the Fujifilm header and marker. */
    private const val IFD_STRUCTURE_OFFSET: Int =
        FUJI_HEADER_LENGTH + ENDIAN_MARKER_LENGTH

    /** The little-endian marker of Fujifilm MVTG boxes. */
    private const val LITTLE_ENDIAN_MARKER: Int = 1

    /**
     * The smallest interpretable structure: the header, the marker and an
     * IFD0 with its 2-byte entry count.
     */
    private const val MINIMUM_PAYLOAD_LENGTH: Int = IFD_STRUCTURE_OFFSET + 2

    fun read(payload: ByteArray): TiffContents {

        if (payload.size < MINIMUM_PAYLOAD_LENGTH)
            throw ImageReadException("The MVTG payload is too short: ${payload.size} bytes.")

        val markerReader = ByteArrayByteReader(payload)

        markerReader.skipBytes("Fujifilm header", FUJI_HEADER_LENGTH)

        val endianMarker =
            markerReader.read4BytesAsInt("endian marker", ByteOrder.LITTLE_ENDIAN)

        /*
         * Only the little-endian marker written by Fujifilm is accepted.
         * Everything else is an unknown structure and fails the read per
         * the strict read policy in the [Kim] documentation.
         */
        if (endianMarker != LITTLE_ENDIAN_MARKER)
            throw ImageReadException("The MVTG payload has an unknown endian marker.")

        val byteOrder = ByteOrder.LITTLE_ENDIAN

        val ifdBytes = payload.copyOfRange(IFD_STRUCTURE_OFFSET, payload.size)

        val byteReader = ByteArrayByteReader(ifdBytes)

        val directories = mutableListOf<TiffDirectory>()

        TiffReader.readDirectory(
            byteReader = byteReader,
            byteOrder = byteOrder,
            directoryOffset = 0,
            directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
            visitedOffsets = hashSetOf(),
            readTiffImageBytes = false,
            addDirectory = { directories.add(it) }
        )

        if (directories.isEmpty())
            throw ImageReadException("The MVTG payload has no TIFF directories.")

        val makerNoteParseResult =
            TiffReader.tryToParseMakerNote(directories, byteReader, byteOrder)

        return TiffContents(
            header = TiffHeader(
                byteOrder = byteOrder,
                tiffVersion = TiffConstants.TIFF_VERSION,
                offsetToFirstIFD = 0
            ),
            directories = directories,
            makerNoteDirectory = makerNoteParseResult?.makerNoteDirectory,
            makerNoteSubDirectories = makerNoteParseResult?.subDirectories.orEmpty(),
            geoTiffDirectory = TiffReader.tryToParseGeoTiff(directories)
        )
    }
}
