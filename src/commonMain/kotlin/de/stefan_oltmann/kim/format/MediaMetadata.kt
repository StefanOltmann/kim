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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MediaFormat

/**
 * The metadata of a media file.
 */
public class MediaMetadata internal constructor(
    public val mediaFormat: MediaFormat?,
    public val imageSize: ImageSize?,
    public val exif: TiffContents?,
    public val exifBytes: ByteArray?,
    public val iptc: IptcMetadata?,
    public val xmp: String?
) {

    public fun findStringValue(tagInfo: TagInfo): String? {

        val strings = findTiffField(tagInfo)?.value as? List<String>

        /* Looks like Canon and Fuji OOC JPEGs have lens make in an array.  */
        if (!strings.isNullOrEmpty())
            return strings.first()

        return findTiffField(tagInfo)?.value as? String
    }

    public fun findShortValue(tagInfo: TagInfo): Short? =
        findTiffField(tagInfo)?.toShort()

    public fun findDoubleValue(tagInfo: TagInfo): Double? =
        findTiffField(tagInfo)?.toDouble()

    public fun findTiffField(tagInfo: TagInfo): TiffField? =
        exif?.findTiffField(tagInfo)

    public fun findTiffDirectory(directoryType: Int): TiffDirectory? =
        exif?.findTiffDirectory(directoryType)

    public fun getExifThumbnailBytes(): ByteArray? =
        exif?.getExifThumbnailBytes()

    override fun toString(): String {

        val sb = StringBuilder()
        sb.appendLine("File format : $mediaFormat")
        sb.appendLine("Resolution  : $imageSize")

        if (exif != null)
            sb.append(exif)

        if (iptc != null)
            sb.appendLine(iptc)

        if (xmp != null) {

            sb.appendLine("---- XMP ----")
            sb.append(xmp)
        }

        return sb.toString()
    }

    internal fun withMediaFormat(mediaFormat: MediaFormat) =
        MediaMetadata(
            mediaFormat = mediaFormat,
            imageSize = imageSize,
            exif = exif,
            exifBytes = exifBytes,
            iptc = iptc,
            xmp = xmp
        )

    internal companion object {

        fun createEmpty(mediaFormat: MediaFormat?) =
            MediaMetadata(mediaFormat, null, null, null, null, null)
    }
}
