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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.arw.ArwPreviewExtractor
import de.stefan_oltmann.kim.format.cr2.Cr2PreviewExtractor
import de.stefan_oltmann.kim.format.dng.DngPreviewExtractor
import de.stefan_oltmann.kim.format.nef.NefPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class PreviewExtractorEdgeCasesTest {

    private val emptyTiffContents: TiffContents = TiffContents(
        header = TiffHeader(
            byteOrder = ByteOrder.BIG_ENDIAN,
            tiffVersion = 42,
            offsetToFirstIFD = 8
        ),
        directories = listOf(
            TiffDirectory(
                type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
                entries = emptyList(),
                offset = 8,
                nextDirectoryOffset = 0,
                byteOrder = ByteOrder.BIG_ENDIAN
            )
        ),
        makerNoteDirectory = null,
        makerNoteSubDirectories = emptyList(),
        geoTiffDirectory = null
    )

    private val byteReader = ByteArrayByteReader(byteArrayOf())

    @Test
    fun testDngWithoutDngVersionTag() {

        assertNull(
            DngPreviewExtractor.extractPreviewImage(emptyTiffContents, byteReader)
        )
    }

    @Test
    fun testNefWithoutPreviewTag() {

        assertNull(
            NefPreviewExtractor.extractPreviewImage(emptyTiffContents, byteReader)
        )
    }

    @Test
    fun testArwWithoutPreviewTag() {

        assertNull(
            ArwPreviewExtractor.extractPreviewImage(emptyTiffContents, byteReader)
        )
    }

    @Test
    fun testCr2WithoutPreviewTag() {

        assertNull(
            Cr2PreviewExtractor.extractPreviewImage(emptyTiffContents, byteReader)
        )
    }

    /**
     * A preview length that reaches beyond the end of the file must yield
     * NULL instead of the truncated bytes of a damaged file.
     */
    @Test
    fun testCr2WithPreviewBeyondFileEndReturnsNull() {

        val contents = cr2Contents(previewStart = 8, previewLength = 1_000)

        val reader = ByteArrayByteReader(ByteArray(64))

        assertNull(Cr2PreviewExtractor.extractPreviewImage(contents, reader))
    }

    /**
     * Some files carry random garbage in the preview tags. Data without
     * the JPEG signature must yield NULL instead of unusable bytes.
     */
    @Test
    fun testCr2WithNonJpegPreviewDataReturnsNull() {

        val fileBytes = ByteArray(32) { 0x55 }

        val contents = cr2Contents(previewStart = 8, previewLength = 16)

        assertNull(
            Cr2PreviewExtractor.extractPreviewImage(contents, ByteArrayByteReader(fileBytes))
        )
    }

    /**
     * Valid JPEG data within the file bounds must still be extracted.
     */
    @Test
    fun testCr2WithValidJpegPreviewIsExtracted() {

        val fileBytes = MediaFormatMagicNumbers.jpeg.toByteArray() + byteArrayOf(1, 2, 3, 4)

        val contents = cr2Contents(previewStart = 0, previewLength = fileBytes.size)

        val previewBytes =
            Cr2PreviewExtractor.extractPreviewImage(contents, ByteArrayByteReader(fileBytes))

        assertContentEquals(fileBytes, previewBytes)
    }

    private fun cr2Contents(previewStart: Int, previewLength: Int): TiffContents {

        val startField = longField(
            tag = ExifTag.EXIF_TAG_PREVIEW_IMAGE_START_IFD0,
            value = previewStart
        )

        val lengthField = longField(
            tag = ExifTag.EXIF_TAG_PREVIEW_IMAGE_LENGTH_IFD0,
            value = previewLength
        )

        return tiffContentsWithDirectories(startField, lengthField)
    }

    private fun longField(
        tag: de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong,
        value: Int
    ): TiffField = TiffField(
        offset = 0,
        tag = tag.tag,
        directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
        fieldType = FieldTypeLong,
        count = 1,
        localValue = null,
        valueOffset = 0,
        valueBytes = value.toBytes(ByteOrder.LITTLE_ENDIAN),
        byteOrder = ByteOrder.LITTLE_ENDIAN,
        sortHint = 0
    )

    private fun tiffContentsWithDirectories(vararg entries: TiffField): TiffContents =

        TiffContents(
            header = TiffHeader(
                byteOrder = ByteOrder.BIG_ENDIAN,
                tiffVersion = 42,
                offsetToFirstIFD = 8
            ),
            directories = listOf(
                TiffDirectory(
                    type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
                    entries = entries.toList(),
                    offset = 8,
                    nextDirectoryOffset = 0,
                    byteOrder = ByteOrder.BIG_ENDIAN
                )
            ),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )
}
