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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
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

    /**
     * A BigTIFF header is never "unknown" bytes: the documented rule is
     * that it keeps failing the read instead of being misparsed as
     * classic TIFF, so the facade must fail it like the TiffReader does.
     */
    @Test
    fun testReadMetadataRejectsBigTiffHeader() {

        val littleEndian =
            convertHexStringToByteArray("49492b0008000000" + "0000000000000000")

        val bigEndian =
            convertHexStringToByteArray("4d4d002b" + "00000008" + "0000000000000000")

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(littleEndian)
        }

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bigEndian)
        }

        assertFailsWith<ImageReadException> {
            Kim.extractMetadataBytes(ByteArrayByteReader(littleEndian))
        }

        /* The write side keeps its own exception type for unknown bytes. */
        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = littleEndian,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
            )
        }

        /* Genuinely unknown bytes keep returning NULL. */
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
    fun testUpdateWithEmptyUpdateSetIsRejected() {

        /*
         * An update call without any updates makes no sense, so the library
         * must refuse it instead of silently returning the unchanged file.
         * The guard fires before format detection, hence the unknown bytes.
         */
        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = unknownFormatBytes,
                updates = emptySet()
            )
        }
    }

    @Test
    fun testUpdateWithEmptyUpdateSetOnStreamIsRejected() {

        /*
         * The streaming overload must refuse an empty update set the same way.
         * The guard fires before format detection, hence the unknown bytes.
         */
        assertFailsWith<ImageWriteException> {
            Kim.update(
                byteReader = ByteArrayByteReader(unknownFormatBytes),
                byteWriter = ByteArrayByteWriter(),
                updates = emptySet()
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

    /**
     * Formats without a preview concept - everything except RAF, CR3 and
     * the TIFF family - must report NULL instead of failing inside the
     * TIFF header validation.
     */
    @Test
    fun testExtractPreviewImageFromJpegReturnsNull() {

        /* A plain JPEG test image. */
        val bytes = KimTestData.getBytesOf(2)

        assertNull(Kim.extractPreviewImage(ByteArrayByteReader(bytes)))
    }
}
