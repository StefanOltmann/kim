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
package de.stefan_oltmann.kim.format.cr3

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Cr3PreviewExtractorTest {

    private fun box(type: String, payload: ByteArray): ByteArray {

        val size = payload.size + 8

        return byteArrayOf(
            (size shr 24).toByte(),
            (size shr 16).toByte(),
            (size shr 8).toByte(),
            size.toByte()
        ) + type.encodeToByteArray() + payload
    }

    private fun tkhdBox(): ByteArray = box("tkhd", byteArrayOf(0, 0, 0, 0))

    private fun uuidBytes(hexString: String): ByteArray =
        ByteArray(hexString.length / 2) { index ->
            hexString.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }

    private fun stszBox(length: Int): ByteArray = box(
        "stsz",
        byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) +
            byteArrayOf(
                (length shr 24).toByte(),
                (length shr 16).toByte(),
                (length shr 8).toByte(),
                length.toByte()
            )
    )

    private fun co64Box(offset: Long): ByteArray = box(
        "co64",
        byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0) +
            byteArrayOf(
                (offset shr 56).toByte(),
                (offset shr 48).toByte(),
                (offset shr 40).toByte(),
                (offset shr 32).toByte(),
                (offset shr 24).toByte(),
                (offset shr 16).toByte(),
                (offset shr 8).toByte(),
                offset.toByte()
            )
    )

    @Test
    fun testExtractFullSizePreviewFromRealFile() {

        val bytes = KimTestData.getBytesOf(KimTestData.CR3_TEST_IMAGE_INDEX)

        val previewBytes = Cr3PreviewExtractor.extractFullSizePreviewImage(
            ByteArrayByteReader(bytes)
        )

        assertNotNull(previewBytes)

        /* It must be a JPEG. */
        assertTrue(previewBytes[0] == 0xFF.toByte() && previewBytes[1] == 0xD8.toByte())
    }

    @Test
    fun testExtractSmallPreviewFromRealFile() {

        val bytes = KimTestData.getBytesOf(KimTestData.CR3_TEST_IMAGE_INDEX)

        val previewBytes = Cr3PreviewExtractor.extractSmallPreviewImage(
            ByteArrayByteReader(bytes)
        )

        assertNotNull(previewBytes)
        assertTrue(previewBytes[0] == 0xFF.toByte() && previewBytes[1] == 0xD8.toByte())
    }

    @Test
    fun testExtractPreviewImageDelegatesToFullSize() {

        val bytes = KimTestData.getBytesOf(KimTestData.CR3_TEST_IMAGE_INDEX)

        val previewBytes = Cr3PreviewExtractor.extractPreviewImage(
            ByteArrayByteReader(bytes)
        )

        assertNotNull(previewBytes)
    }

    @Test
    fun testExtractFromEmptyFileReturnsNull() {

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(byteArrayOf())
            )
        )

        assertNull(
            Cr3PreviewExtractor.extractSmallPreviewImage(
                ByteArrayByteReader(byteArrayOf())
            )
        )
    }

    @Test
    fun testExtractWithoutMovieBoxReturnsNull() {

        val bytes = box("ftyp", "heic".encodeToByteArray() + "0000".encodeToByteArray() + "mif1".encodeToByteArray())

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    @Test
    fun testExtractWithoutTrackBoxReturnsNull() {

        val bytes = box("moov", byteArrayOf())

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    @Test
    fun testExtractWithoutMediaInformationBoxReturnsNull() {

        val mdiaPayload = byteArrayOf(0, 0, 0, 0)

        val mdiaBox = box("mdia", mdiaPayload)

        val trakPayload = tkhdBox() + mdiaBox

        val trakBox = box("trak", trakPayload)

        val moovPayload = trakBox

        val moovBox = box("moov", moovPayload)

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(moovBox)
            )
        )
    }

    @Test
    fun testExtractWithoutSampleTableBoxReturnsNull() {

        val minfPayload = byteArrayOf()

        val minfBox = box("minf", minfPayload)

        val mdiaPayload = minfBox

        val mdiaBox = box("mdia", mdiaPayload)

        val trakPayload = tkhdBox() + mdiaBox

        val trakBox = box("trak", trakPayload)

        val moovPayload = trakBox

        val moovBox = box("moov", moovPayload)

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(moovBox)
            )
        )
    }

    @Test
    fun testExtractWithoutSampleSizesBoxReturnsNull() {

        val stblPayload = byteArrayOf()

        val stblBox = box("stbl", stblPayload)

        val minfPayload = stblBox

        val minfBox = box("minf", minfPayload)

        val mdiaPayload = minfBox

        val mdiaBox = box("mdia", mdiaPayload)

        val trakPayload = tkhdBox() + mdiaBox

        val trakBox = box("trak", trakPayload)

        val moovPayload = trakBox

        val moovBox = box("moov", moovPayload)

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(moovBox)
            )
        )
    }

    @Test
    fun testExtractWithoutMediaDataBoxReturnsNull() {

        val stblPayload = stszBox(10) + co64Box(100)

        val stblBox = box("stbl", stblPayload)

        val minfPayload = stblBox

        val minfBox = box("minf", minfPayload)

        val mdiaPayload = minfBox

        val mdiaBox = box("mdia", mdiaPayload)

        val trakPayload = tkhdBox() + mdiaBox

        val trakBox = box("trak", trakPayload)

        val moovPayload = trakBox

        val moovBox = box("moov", moovPayload)

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(moovBox)
            )
        )
    }

    @Test
    fun testExtractSmallPreviewWithoutUuidBoxReturnsNull() {

        val bytes = box("ftyp", "heic".encodeToByteArray() + "0000".encodeToByteArray() + "mif1".encodeToByteArray())

        assertNull(
            Cr3PreviewExtractor.extractSmallPreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    @Test
    fun testExtractSmallPreviewRejectsWrongMarker() {

        val payload =
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0) +
                byteArrayOf(0, 0, 0, 0) +
                "XXXX".encodeToByteArray()

        val uuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_PREVIEW_UUID) + payload
        )

        val bytes = uuidBox

        assertFailsWith<ImageReadException> {
            Cr3PreviewExtractor.extractSmallPreviewImage(
                ByteArrayByteReader(bytes)
            )
        }
    }

    @Test
    fun testExtractFullSizePreviewWithUuidOnlyReturnsNull() {

        val bytes = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_PREVIEW_UUID) + byteArrayOf(0, 0, 0, 0)
        )

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    @Test
    fun testCr3ReaderCreateMetadataWithoutTiffReturnsEmptyMetadata() {

        /* A moov box with the EXIF UUID box, but without CMT1 data. */
        val uuidData = byteArrayOf(0, 0, 0, 0)

        val uuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_EXIF_UUID) + uuidData
        )

        val movieBox = MovieBox(
            offset = 0,
            size = (8 + uuidBox.size).toLong(),
            largeSize = null,
            payload = uuidBox
        )

        val metadata = Cr3Reader.createMetadata(listOf(movieBox))

        assertNull(metadata.imageSize)
        assertNull(metadata.exif)
        assertNull(metadata.xmp)
    }

    @Test
    fun testCr3ReaderFindMetadataSubBoxesRejectsMissingMovieBox() {

        assertFailsWith<ImageReadException> {
            Cr3Reader.findMetadaSubBoxes(emptyList())
        }
    }

    @Test
    fun testCr3ReaderFindMetadataSubBoxesRejectsMissingUuidBox() {

        val movieBox = MovieBox(
            offset = 0,
            size = 8,
            largeSize = null,
            payload = byteArrayOf()
        )

        assertFailsWith<ImageReadException> {
            Cr3Reader.findMetadaSubBoxes(listOf(movieBox))
        }
    }
}
