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
import de.stefan_oltmann.kim.format.arw.ArwPreviewExtractor
import de.stefan_oltmann.kim.format.cr2.Cr2PreviewExtractor
import de.stefan_oltmann.kim.format.dng.DngPreviewExtractor
import de.stefan_oltmann.kim.format.nef.NefPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
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
}
