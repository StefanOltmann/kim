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

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageWriteException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.format.MetadataUpdater
import de.stefan_oltmann.kim.format.bmff.BoxReader
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.xmp.XMPMetaFactory

internal object JxlUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        JxlWriter.writeImageStreaming(byteReader, byteWriter) { boxes, outputWriter ->

            val metadata = JxlReader.createMetadata(boxes)

            /*
             * Only rewrite the xml box when the updates actually changed
             * the XMP content. A parse → apply → serialize round-trip on
             * unchanged data produces identical output (the serializer is
             * deterministic), so a string comparison is sufficient.
             */
            val updatedXmp: String? = metadata.xmp?.let { original ->
                val xmpMeta = XMPMetaFactory.parseFromString(original)
                val updated = XmpWriter.updateXmp(xmpMeta, updates, true)
                if (updated == original) null else updated
            } ?: run {
                val xmpMeta = XMPMetaFactory.create()
                XmpWriter.updateXmp(xmpMeta, updates, true)
            }

            val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifBytes: ByteArray? = if (outputSet.applyUpdates(updates)) {

                val exifBytesWriter = ByteArrayByteWriter()

                TiffWriter(byteOrder = outputSet.byteOrder).write(exifBytesWriter, outputSet)

                exifBytesWriter.toByteArray()

            } else {
                null
            }

            JxlWriter.writeImage(
                boxes = boxes,
                byteWriter = outputWriter,
                exifBytes = exifBytes,
                xmp = updatedXmp
            )
        }
    }

    @Throws(ImageWriteException::class)
    override fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    ) = tryWithImageWriteException {

        JxlWriter.writeImageStreaming(byteReader, byteWriter) { boxes, outputWriter ->

            /*
             * Remove the EXIF and XMP boxes. JPEG XL has no ICC box that
             * would affect how the image is displayed.
             *
             * Compressed boxes are dropped when their wrapped type
             * identifies them as Exif or XMP. Their content cannot be
             * rewritten without brotli support, but leaving them behind
             * would silently keep data the user asked to delete.
             */
            val boxesWithoutMetadata = boxes.filterNot { box ->
                box.type == BoxType.EXIF ||
                    box.type == BoxType.XML ||
                    box is CompressedBox && (
                    box.actualType == BoxType.EXIF ||
                        box.actualType == BoxType.XML
                    )
            }

            JxlWriter.writeImage(
                boxes = boxesWithoutMetadata,
                byteWriter = outputWriter,
                exifBytes = null,
                xmp = null
            )
        }
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(MediaFormatMagicNumbers.jxl))
            throw ImageWriteException("Provided input bytes are not JXL!")

        val byteReader = ByteArrayByteReader(bytes)

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val metadata = JxlReader.createMetadata(allBoxes)

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val exifBytesWriter = ByteArrayByteWriter()

        TiffWriter(byteOrder = outputSet.byteOrder).write(exifBytesWriter, outputSet)

        val exifBytes = exifBytesWriter.toByteArray()

        val byteWriter = ByteArrayByteWriter()

        JxlWriter.writeImage(
            boxes = allBoxes,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            xmp = null // No change to XMP
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
