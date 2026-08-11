/*
 * Copyright 2026 Ramon Bouckaert
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.bmff.box.FileTypeBox
import de.stefan_oltmann.kim.format.bmff.box.HandlerReferenceBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInfoEntryBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInformationBox
import de.stefan_oltmann.kim.format.bmff.box.ItemLocationBox
import de.stefan_oltmann.kim.format.bmff.box.MediaBox
import de.stefan_oltmann.kim.format.bmff.box.MediaDataBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBoxTopLevel
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.PrimaryItemBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
import de.stefan_oltmann.kim.format.bmff.box.TrackHeaderBox
import de.stefan_oltmann.kim.format.bmff.box.UserDataBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.format.jxl.box.JxlParticalCodestreamBox
import de.stefan_oltmann.kim.format.jxl.box.XmlBox
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.read8BytesAsLong
import de.stefan_oltmann.kim.input.readBytes

/**
 * Reads ISOBMFF boxes.
 */
public object BoxReader {

    /**
     * @param byteReader The reader as source for the bytes
     * @param stopAfterMetadataRead If reading the file for metadata on the highest level we
     * want to stop reading after the top-level meta boxes to prevent reading the whole image data
     * block in. For iPhone HEIC this is possible, but Samsung HEIC has "meta" coming after "mdat"
     * @param positionOffset The position where to start reading boxes
     * @param offsetShift The shift to apply to the reported box offsets
     * @param updatePosition A callback to report the position when reading has finished
     * @param parentBoxType can be used to specify the type of the parent box - used when traversing
     * through sub boxes. This can change the logic for parsing boxes as "meta" boxes within a sub
     * box need to be treated differently to "meta" boxes at the top level.
     */
    @Suppress("NestedBlockDepth")
    public fun readBoxes(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false,
        positionOffset: Long = 0,
        offsetShift: Long = 0,
        updatePosition: ((Long) -> Unit)? = null,
        parentBoxType: BoxType? = null
    ): List<Box> {

        var haveSeenJxlHeaderBox = false

        var haveSeenTopLevelMetaBox = false

        var haveSeenXmpDataInUuid = false

        val boxes = mutableListOf<Box>()

        var position: Long = positionOffset

        while (true) {

            val available = byteReader.contentLength - position

            /*
             * Check if there are enough bytes for another box.
             * If so, we at least need the 8 header bytes.
             */
            if (available < BMFFConstants.BOX_HEADER_LENGTH)
                break

            val offset: Long = position

            /* Note: The length includes the 8 header bytes. */
            val size: Long =
                byteReader.read4BytesAsInt("length", BMFF_BYTE_ORDER).toLong()

            val type = BoxType.of(
                byteReader.readBytes("type", BMFFConstants.TYPE_LENGTH)
            )

            position += BMFFConstants.BOX_HEADER_LENGTH

            /*
             * If we read an JXL file and we already have seen the header,
             * all reamining JXLP boxes are image data that we can skip.
             */
            if (stopAfterMetadataRead && type == BoxType.JXLP && haveSeenJxlHeaderBox)
                break

            var largeSize: Long? = null

            val actualLength: Long = when (size) {

                /* A vaule of zero indicates that it's the last box. */
                0L -> available

                /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
                1L -> {
                    largeSize = byteReader.read8BytesAsLong("length", BMFF_BYTE_ORDER)
                    largeSize
                }

                /* Keep the length we already read. */
                else -> size
            }

            val nextBoxOffset = offset + actualLength

            @Suppress("MagicNumber")
            if (size == 1L)
                position += 8

            val remainingBytesToReadInThisBox = (nextBoxOffset - position).toInt()

            val bytes = byteReader.readBytes("data", remainingBytesToReadInThisBox)

            position += remainingBytesToReadInThisBox

            val globalOffset = offset + offsetShift

            val box = when (type) {
                /* Generic EIC/ISO 14496-12 boxes. */
                BoxType.FTYP -> FileTypeBox(globalOffset, size, largeSize, bytes)
                BoxType.META -> if (parentBoxType == null) {
                    MetaBoxTopLevel(globalOffset, size, largeSize, bytes)
                } else {
                    MetaBox(globalOffset, size, largeSize, bytes)
                }

                BoxType.HDLR -> HandlerReferenceBox(globalOffset, size, largeSize, bytes)
                BoxType.IINF -> ItemInformationBox(globalOffset, size, largeSize, bytes)
                BoxType.INFE -> ItemInfoEntryBox(globalOffset, size, largeSize, bytes)
                BoxType.ILOC -> ItemLocationBox(globalOffset, size, largeSize, bytes)
                BoxType.PITM -> PrimaryItemBox(globalOffset, size, largeSize, bytes)
                BoxType.MDAT -> MediaDataBox(globalOffset, size, largeSize, bytes)
                BoxType.MOOV -> MovieBox(globalOffset, size, largeSize, bytes)
                BoxType.TRAK -> TrackBox(globalOffset, size, largeSize, bytes)
                BoxType.TKHD -> TrackHeaderBox(globalOffset, size, largeSize, bytes)
                BoxType.MDIA -> MediaBox(globalOffset, size, largeSize, bytes)
                BoxType.UUID -> UuidBox(globalOffset, size, largeSize, bytes)
                BoxType.UDTA -> UserDataBox(globalOffset, size, largeSize, bytes)
                /* JXL boxes */
                BoxType.EXIF -> ExifBox(globalOffset, size, largeSize, bytes)
                BoxType.XML -> XmlBox(globalOffset, size, largeSize, bytes)
                BoxType.JXLP -> JxlParticalCodestreamBox(globalOffset, size, largeSize, bytes)
                BoxType.BROB -> CompressedBox(globalOffset, size, largeSize, bytes)
                /* Unknown box */
                else -> Box(type, globalOffset, size, largeSize, bytes)
            }

            boxes.add(box)

            if (stopAfterMetadataRead) {

                /* Metadata is here for most HEIC & AVIF */
                if (type == BoxType.META && parentBoxType == null) {
                    haveSeenTopLevelMetaBox = true

                    box as MetaBoxTopLevel

                    /*
                     * If this box references XMP data, we can break. If it's missing XMP, we should
                     * continue reading the file to search for an XMP UUID box (or break now if
                     * we've already seen it)
                     */
                    if (box.referencesXmp || haveSeenXmpDataInUuid) {
                        break
                    }
                }

                /* Some store XMP data in a UUID box instead */
                if (type == BoxType.UUID) {
                    box as UuidBox

                    /*
                     * If this box contains XMP, we can break as soon as we also find the top-level
                     *  META box (or break now if we've already seen it)
                     */
                    if (box.isXmp) {
                        haveSeenXmpDataInUuid = true
                        if (haveSeenTopLevelMetaBox) break
                    }
                }

                /*
                 * When parsing JXL we need to take a note that we saw the header.
                 * This is usually the first JXLP box.
                 */
                if (type == BoxType.JXLP) {

                    box as JxlParticalCodestreamBox

                    if (box.isHeader)
                        haveSeenJxlHeaderBox = true
                }
            }
        }

        updatePosition?.let { it(position) }

        return boxes
    }
}
