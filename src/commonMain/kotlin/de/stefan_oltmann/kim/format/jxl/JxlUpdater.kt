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
import de.stefan_oltmann.kim.format.exifBytesWithThumbnail
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.updatedExifBytes
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.ByteWriter

internal object JxlUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        JxlWriter.writeImageStreaming(byteReader, byteWriter) { boxes, outputWriter ->

            val metadata = JxlReader.createMetadata(boxes)

            val updatedXmp = XmpWriter.updateXmp(metadata.xmp, updates, true)

            /*
             * Only rewrite the xml box when the updates actually changed
             * the XMP content: a parse → apply → serialize round-trip on
             * unchanged data produces identical output (the serializer is
             * deterministic), so the string comparison is sufficient.
             * Writing a fresh packet instead would drop every field the
             * update did not touch.
             */
            val changedXmp: String? =
                if (metadata.xmp != null && updatedXmp == metadata.xmp) null else updatedXmp

            val exifBytes = metadata.updatedExifBytes(updates)

            JxlWriter.writeImage(
                boxes = boxes,
                byteWriter = outputWriter,
                exifBytes = exifBytes,
                xmp = changedXmp
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

        val allBoxes = BoxReader.readAllBoxes(byteReader = byteReader)

        val metadata = JxlReader.createMetadata(allBoxes)

        val exifBytes = metadata.exifBytesWithThumbnail(thumbnailBytes)

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
