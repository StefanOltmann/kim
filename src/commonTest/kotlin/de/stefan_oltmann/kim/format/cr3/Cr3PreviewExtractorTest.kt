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
import de.stefan_oltmann.kim.format.bmff.BaseMediaFileFormatImageParser
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
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

    /**
     * Regression test: a CO64 offset that points before the mdat data
     * must yield NULL instead of an exception from a negative slice.
     */
    @Test
    fun testExtractFullSizePreviewRejectsOffsetBeforeMdat() {

        val bytes = buildCr3File(
            mdatPayload = byteArrayOf(0, 0, 0, 0) +
                byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + "jpegdata".encodeToByteArray(),
            jpegLength = 10,
            co64Offset = { it - 6 } /* Points into the mdat header. */
        )

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    /**
     * Regression test: a CO64 offset beyond the file must yield NULL.
     * The old Int truncation could wrap such deltas back into the
     * payload and return arbitrary bytes as a "preview".
     */
    @Test
    fun testExtractFullSizePreviewRejectsOffsetBeyondFile() {

        val bytes = buildCr3File(
            mdatPayload = byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + "jpeg".encodeToByteArray(),
            jpegLength = 12,
            co64Offset = { it + 1000 }
        )

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    /**
     * Data at a valid offset that is not a JPEG is not a preview and
     * must not be handed to the caller as one.
     */
    @Test
    fun testExtractFullSizePreviewRejectsNonJpegContent() {

        val bytes = buildCr3File(
            mdatPayload = "notajpeg".encodeToByteArray(),
            jpegLength = 8,
            co64Offset = { it }
        )

        assertNull(
            Cr3PreviewExtractor.extractFullSizePreviewImage(
                ByteArrayByteReader(bytes)
            )
        )
    }

    /**
     * A preview whose declared JPEG size exceeds the available UUID box
     * bytes is truncated and must be rejected instead of returning the
     * short array.
     */
    @Test
    fun testExtractSmallPreviewRejectsTruncatedJpeg() {

        val payload =
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0) +
                byteArrayOf(0, 0, 0, 0) +
                "PRVW".encodeToByteArray() +
                ByteArray(12) +
                byteArrayOf(0, 0, 10, 0) /* Declares 2560 bytes ... */

        /* ... but only two bytes of actual data follow. */
        val truncatedJpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        val uuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_PREVIEW_UUID) + payload + truncatedJpeg
        )

        assertNull(
            Cr3PreviewExtractor.extractSmallPreviewImage(
                ByteArrayByteReader(uuidBox)
            )
        )
    }

    /**
     * Builds a CR3-like file: ftyp + moov(trak > mdia > minf > stbl with
     * stsz & co64) + mdat.
     *
     * The CO64 offset depends on the mdat position, which depends on the
     * file layout, so the builder receives the resolved mdat data offset
     * in a second pass (the box sizes do not depend on it).
     */
    private fun buildCr3File(
        mdatPayload: ByteArray,
        jpegLength: Int,
        co64Offset: (mdatDataOffset: Long) -> Long
    ): ByteArray {

        val tkhd = tkhdBox()

        val ftypBox = box(
            "ftyp",
            "crx ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "fTyp".encodeToByteArray()
        )

        val minfPayload = stszAndCo64(jpegLength, 0L)

        val trakBox =
            box("trak", tkhd + box("mdia", box("minf", box("stbl", minfPayload))))

        val moovBox = box("moov", trakBox)

        /* First pass determines the layout with a placeholder offset. */
        val prefix = ftypBox + moovBox

        val mdatDataOffset = prefix.size + 8L

        /* Second pass with the real offset. */
        val realMinfPayload = stszAndCo64(jpegLength, co64Offset(mdatDataOffset))

        val realTrakBox =
            box("trak", tkhd + box("mdia", box("minf", box("stbl", realMinfPayload))))

        val realMoovBox = box("moov", realTrakBox)

        val mdatBox = box("mdat", mdatPayload)

        return ftypBox + realMoovBox + mdatBox
    }

    private fun stszAndCo64(jpegLength: Int, co64Offset: Long): ByteArray =
        stszBox(jpegLength) + co64Box(co64Offset)

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
            Cr3Reader.findMetadataSubBoxes(emptyList())
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
            Cr3Reader.findMetadataSubBoxes(listOf(movieBox))
        }
    }

    /**
     * Regression test: a truncated CR3 that ends before its moov box is
     * written must still yield the metadata that survived - for example
     * XMP in a top-level UUID box - instead of failing the whole file.
     */
    @Test
    fun testTruncatedCr3YieldsSurvivingXmp() {

        val ftypBox = box(
            "ftyp",
            "crx ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0)
        )

        val xmpData = "<xmp/>".encodeToByteArray()

        val xmpUuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_XMP_UUID) + xmpData
        )

        val bytes = ftypBox + xmpUuidBox

        val metadata = BaseMediaFileFormatImageParser.parseMetadata(
            ByteArrayByteReader(bytes)
        )

        assertEquals(MediaFormat.CR3, metadata.mediaFormat)

        assertNull(metadata.exif)

        /* The XMP UUID box survived the truncation and must be read. */
        assertEquals("<xmp/>", metadata.xmp)
    }

    /**
     * A moov box without the EXIF metadata UUID box degrades to empty
     * EXIF while XMP in a top-level UUID box is still returned.
     */
    @Test
    fun testMoovWithoutExifUuidYieldsSurvivingXmp() {

        val ftypBox = box(
            "ftyp",
            "crx ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0)
        )

        val moovBox = box("moov", byteArrayOf())

        val xmpData = "<xmp/>".encodeToByteArray()

        val xmpUuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_XMP_UUID) + xmpData
        )

        val bytes = ftypBox + moovBox + xmpUuidBox

        val metadata = BaseMediaFileFormatImageParser.parseMetadata(
            ByteArrayByteReader(bytes)
        )

        assertEquals(MediaFormat.CR3, metadata.mediaFormat)

        assertNull(metadata.exif)

        assertEquals("<xmp/>", metadata.xmp)
    }

    /**
     * Regression test: an interrupted recording cuts the file INSIDE the
     * moov box, whose header still declares the full size. The boxes
     * parsed so far - here the surviving XMP UUID - must be returned
     * instead of aborting the whole read.
     */
    @Test
    fun testTruncatedMoovYieldsSurvivingXmp() {

        val ftypBox = box(
            "ftyp",
            "crx ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0)
        )

        val xmpUuidBox = box(
            "uuid",
            uuidBytes(Cr3Reader.CR3_XMP_UUID) + "<xmp/>".encodeToByteArray()
        )

        /*
         * The moov header declares 4096 bytes of payload, but only a few
         * bytes follow before the file ends.
         */
        val truncatedMoovHeader =
            byteArrayOf(0, 0x10, 0, 0) + "moov".encodeToByteArray() +
                byteArrayOf(1, 2, 3, 4, 5, 6)

        val bytes = ftypBox + xmpUuidBox + truncatedMoovHeader

        val metadata = BaseMediaFileFormatImageParser.parseMetadata(
            ByteArrayByteReader(bytes)
        )

        assertEquals(MediaFormat.CR3, metadata.mediaFormat)

        assertNull(metadata.exif)

        /* The XMP UUID box before the cut moov must be read. */
        assertEquals("<xmp/>", metadata.xmp)
    }
}
