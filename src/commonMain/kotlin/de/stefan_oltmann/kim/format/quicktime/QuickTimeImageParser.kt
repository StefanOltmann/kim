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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.bmff.box.BoxContainer
import de.stefan_oltmann.kim.format.bmff.box.HandlerReferenceBox
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
import de.stefan_oltmann.kim.format.bmff.box.UserDataBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.ImageSize

/**
 * Parses the metadata of QuickTime MOV and MP4 video containers.
 *
 * Both formats share the box structure of the ISO base media file format.
 * The metadata lives inside the "moov" box: Fujifilm cameras embed the
 * complete EXIF including the MakerNote and a thumbnail as a TIFF
 * structure in the "MVTG" box of the user data.
 */
public object QuickTimeImageParser : ImageParser {

    /** The handler type that marks a video track in its handler box. */
    private const val VIDEO_HANDLER_TYPE = "vide"

    private fun isXmpSource(box: Box): Boolean =
        (box is UuidBox && box.isXmp) || box.type == BoxType.XMP_

    /**
     * The UUID box carries the packet behind its 16-byte UUID, the "XMP_"
     * box carries it directly in its payload.
     */
    private fun extractXmpPacket(box: Box): String? =
        if (box is UuidBox)
            box.data.decodeToString()
        else
            box.payload.decodeToString()

    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            /*
             * Hostile files let internal machinery like the eager box
             * construction fail with IllegalStateException, so this is
             * wrapped at the public boundary into the documented
             * ImageReadException.
             */
            createMetadata(
                allBoxes = BoxReader.scanVideoMetadataBoxes(byteReader)
            )
        }

    /**
     * Assembles the metadata from the scanned top-level boxes.
     *
     * A container without a "moov" box has no place where metadata could
     * live and is rejected per the strict read policy in the [Kim]
     * documentation.
     */
    internal fun createMetadata(allBoxes: List<Box>): MediaMetadata {

        val movieBox = allBoxes.filterIsInstance<MovieBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: Has no 'moov' Box.")

        val userDataBox = movieBox.boxes.filterIsInstance<UserDataBox>().firstOrNull()

        val mvtgBox = userDataBox?.boxes?.find { it.type == BoxType.MVTG }

        val exif = mvtgBox?.let { QuickTimeMvtgReader.read(it.payload) }

        /*
         * The XMP packet lives either in a UUID box with the Adobe XMP UUID
         * or in an "XMP_" user data box. Both can sit at the top level or
         * anywhere inside the moov box, so every box of the container is
         * searched - the same way the HEIC reader looks for its UUID boxes.
         */
        val xmp = BoxContainer.findAllBoxesRecursive(allBoxes)
            .firstOrNull { box -> isXmpSource(box) }
            ?.let(::extractXmpPacket)

        /*
         * Like WebP, JXL and CR3, corrupt XMP must fail the read instead of
         * being handed to sidecar writers as a corrupt packet (read/update
         * symmetry: an update would embed the broken bytes as-is).
         */
        if (xmp != null && !xmp.contains("<x:xmpmeta"))
            throw ImageReadException("The XMP box has no <x:xmpmeta> element.")

        return MediaMetadata(
            mediaFormat = null, /* Set by Kim.readMetadata from the detected format. */
            imageSize = findVideoTrackImageSize(movieBox),
            exif = exif,
            exifBytes = mvtgBox?.payload,
            iptc = null, /* Not existent in MOV & MP4. */
            xmp = xmp
        )
    }

    /**
     * Reads the display size from the header of the first video track.
     *
     * Tracks without a video handler are skipped, and a video track whose
     * header reports no size results in no size instead of a fabricated
     * 0x0 one.
     */
    private fun findVideoTrackImageSize(movieBox: MovieBox): ImageSize? {

        val videoTrack = movieBox.boxes
            .filterIsInstance<TrackBox>()
            .firstOrNull { track ->
                track.mediaBox.boxes
                    .filterIsInstance<HandlerReferenceBox>()
                    .any { it.handlerType == VIDEO_HANDLER_TYPE }
            }
            ?: return null

        return videoTrack.trackHeaderBox
            .takeIf { it.width > 0 && it.height > 0 }
            ?.let { ImageSize(width = it.width, height = it.height) }
    }
}
