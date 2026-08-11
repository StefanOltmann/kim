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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KimEdgeCasesTest {

    private val unknownFormatBytes: ByteArray = "This is not an image file at all".encodeToByteArray()

    @Test
    fun testReadMetadataFromEmptyBytes() {

        assertNull(Kim.readMetadata(byteArrayOf()))
    }

    @Test
    fun testReadMetadataFromUnknownFormat() {

        assertNull(Kim.readMetadata(unknownFormatBytes))
    }

    @Test
    fun testUpdateRejectsUnknownFormat() {

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = unknownFormatBytes,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
            )
        }
    }

    @Test
    fun testUpdateThumbnailRejectsUnknownFormat() {

        assertFailsWith<ImageWriteException> {
            Kim.updateThumbnail(
                bytes = unknownFormatBytes,
                thumbnailBytes = byteArrayOf(1, 2, 3)
            )
        }
    }

    @Test
    fun testExtractMetadataBytesFromUnknownFormat() {

        val result = Kim.extractMetadataBytes(
            ByteArrayByteReader(unknownFormatBytes)
        )

        assertNull(result.first)
        assertEquals(0, result.second.size)
    }

    @Test
    fun testExtractPreviewImageFromUnknownFormat() {

        /* Unknown formats are treated as TIFF and fail to parse. */
        assertFailsWith<de.stefan_oltmann.kim.common.ImageReadException> {
            Kim.extractPreviewImage(
                ByteArrayByteReader(unknownFormatBytes)
            )
        }
    }
}
