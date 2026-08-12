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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriterBase
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object PngUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        PngWriter.writeImageStreaming(byteReader, byteWriter) { chunks, outputWriter ->

            val metadata = PngImageParser.parseMetadataFromChunks(chunks)

            val xmpMeta: XMPMeta = if (metadata.xmp != null)
                XMPMetaFactory.parseFromString(metadata.xmp)
            else
                XMPMetaFactory.create()

            val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

            val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifBytes: ByteArray? = if (outputSet.applyUpdates(updates)) {

                val exifBytesWriter = ByteArrayByteWriter()

                TiffWriterBase
                    .createTiffWriter(
                        byteOrder = outputSet.byteOrder,
                        oldExifBytes = metadata.exifBytes
                    )
                    .write(exifBytesWriter, outputSet)

                exifBytesWriter.toByteArray()

            } else {
                null
            }

            PngWriter.writeImage(
                chunks = chunks,
                byteWriter = outputWriter,
                exifBytes = exifBytes,
                /*
                 * IPTC is not written because it's not recognized everywhere.
                 * XMP is the better choice. If users demand it we may add it.
                 * The logic is already implemented.
                 */
                iptcBytes = null,
                xmp = updatedXmp
            )
        }
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.png))
            throw ImageWriteException("Provided input bytes are not PNG!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val metadata = PngImageParser.parseMetadataFromChunks(chunks)

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val exifBytesWriter = ByteArrayByteWriter()

        TiffWriterBase
            .createTiffWriter(
                byteOrder = outputSet.byteOrder,
                oldExifBytes = metadata.exifBytes
            )
            .write(exifBytesWriter, outputSet)

        val exifBytes = exifBytesWriter.toByteArray()

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            iptcBytes = null,
            xmp = null // No change to XMP
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
