/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package de.stefan_oltmann.kim.format.jxl

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.format.jxl.box.XmlBox
import de.stefan_oltmann.kim.format.xmp.requireValidXmpPacket
import de.stefan_oltmann.kim.model.MediaFormat

internal object JxlReader {

    fun createMetadata(allBoxes: List<Box>): MediaMetadata {

        /*
         * EXIF and XMP wrapped in brob boxes cannot be read without
         * brotli support. Per the strict read policy the read fails
         * instead of silently reporting the file without its metadata -
         * a sidecar writer would lose it. deleteMetadata stays possible:
         * removing the unreadable boxes is what deletion means.
         */
        val unreadableCompressedBoxes = allBoxes.filterIsInstance<CompressedBox>()
            .filter { it.actualType == BoxType.EXIF || it.actualType == BoxType.XML }

        if (unreadableCompressedBoxes.isNotEmpty()) {

            val wrappedTypes = unreadableCompressedBoxes
                .map { it.actualType.toString().trim() }
                .distinct()
                .sorted()
                .joinToString(" and ")

            throw ImageReadException(
                "The file stores its $wrappedTypes metadata in brotli-compressed " +
                    "(brob) boxes, which cannot be read without brotli support."
            )
        }

        val exifBox = allBoxes.filterIsInstance<ExifBox>().firstOrNull()
        val xmlBox = allBoxes.filterIsInstance<XmlBox>().firstOrNull()

        /*
         * An Exif box whose TIFF content cannot be parsed must fail the
         * read loudly: the app will offer editing after a successful read,
         * and an update replaces the Exif box with freshly generated
         * bytes - silently destroying the unparseable content. This
         * mirrors the JPEG behavior for corrupt EXIF segments.
         */
        if (exifBox != null && exifBox.tiffContents == null)
            throw ImageReadException(
                "The file contains an Exif box whose content cannot be parsed as TIFF."
            )

        /*
         * Corrupt XMP fails the update path in XMPMetaFactory anyway, so
         * it must fail the read as well (read/update symmetry).
         */
        val xmp = requireValidXmpPacket(xmlBox?.xmp, "The JXL XML box")

        return MediaMetadata(
            mediaFormat = MediaFormat.JXL,
            /*
             * The image size lives inside the codestream, which the metadata
 * scan does not parse - see the limitations in the README.
             */
            imageSize = null,
            exif = exifBox?.tiffContents,
            exifBytes = exifBox?.exifBytes,
            iptc = null, // not covered by ISO BMFF
            xmp = xmp
        )
    }
}
