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
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.format.jxl.box.XmlBox
import de.stefan_oltmann.kim.model.MediaFormat

internal object JxlReader {

    fun createMetadata(allBoxes: List<Box>): MediaMetadata {

        val exifBox = allBoxes.filterIsInstance<ExifBox>().firstOrNull()
        val xmlBox = allBoxes.filterIsInstance<XmlBox>().firstOrNull()

        /*
         * An Exif box whose TIFF content cannot be parsed must fail the
         * read loudly: the app will offer editing after a successful read,
         * and an update replaces the Exif box with freshly generated
         * bytes - silently destroying the unparseable content. This
         * mirrors the JPEG behavior for corrupt EXIF segments.
         *
         * deleteMetadata is unaffected: it strips boxes by type and does
         * not go through this method, so the broken metadata can always
         * be removed.
         */
        if (exifBox != null && exifBox.tiffContents == null)
            throw ImageReadException(
                "The file contains an Exif box whose content cannot be parsed as TIFF."
            )

        return MediaMetadata(
            mediaFormat = MediaFormat.JXL,
            imageSize = null, // TODO https://github.com/Ashampoo/kim/issues/65
            exif = exifBox?.tiffContents,
            exifBytes = exifBox?.exifBytes,
            iptc = null, // not covered by ISO BMFF
            xmp = xmlBox?.xmp
        )
    }
}
