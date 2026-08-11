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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.BaseMediaFileFormatImageParser
import de.stefan_oltmann.kim.format.gif.GifImageParser
import de.stefan_oltmann.kim.format.jpeg.JpegImageParser
import de.stefan_oltmann.kim.format.png.PngImageParser
import de.stefan_oltmann.kim.format.raf.RafImageParser
import de.stefan_oltmann.kim.format.tiff.TiffImageParser
import de.stefan_oltmann.kim.format.webp.WebPImageParser
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import kotlin.jvm.JvmStatic

/**
 * Parses the metadata of a media file.
 */
public fun interface ImageParser {

    @Throws(ImageReadException::class)
    public fun parseMetadata(byteReader: ByteReader): MediaMetadata

    public companion object {

        @JvmStatic
        public fun forFormat(mediaFormat: MediaFormat): ImageParser? =
            when (mediaFormat) {

                MediaFormat.JPEG -> JpegImageParser

                MediaFormat.PNG -> PngImageParser

                MediaFormat.WEBP -> WebPImageParser

                MediaFormat.TIFF,
                MediaFormat.CR2,
                MediaFormat.NEF,
                MediaFormat.ARW,
                MediaFormat.RW2,
                MediaFormat.ORF -> TiffImageParser

                MediaFormat.RAF -> RafImageParser

                MediaFormat.HEIC,
                MediaFormat.AVIF,
                MediaFormat.CR3,
                MediaFormat.JXL -> BaseMediaFileFormatImageParser

                MediaFormat.GIF -> GifImageParser

                else -> null
            }
    }
}
