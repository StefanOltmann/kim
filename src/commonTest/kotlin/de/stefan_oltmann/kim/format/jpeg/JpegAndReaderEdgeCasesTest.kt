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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.jpeg.JpegSegmentAnalyzer.JpegSegmentInfo
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputField
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class JpegAndReaderEdgeCasesTest {

    @Test
    fun testFindSegmentInfosRejectsWrongMagic() {

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader("not a jpeg".encodeToByteArray())
            )
        }
    }

    @Test
    fun testFindSegmentInfosWithFillBytes() {

        /* SOI, a marker with fill bytes, an APP0 segment, SOS and EOI. */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xE0.toByte(),
            0x00, 0x04,
            0x00, 0x00,
            0xFF.toByte(), 0xDA.toByte(),
            0xFF.toByte(), 0xD9.toByte()
        )

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(bytes)
        )

        assertEquals(
            expected = JpegSegmentInfo(
                offset = 0,
                marker = JpegConstants.SOI_MARKER,
                length = 2
            ),
            actual = segmentInfos.first()
        )

        assertTrue(segmentInfos.size >= 3)

        assertEquals(
            expected = "0 = SOI (Start of Image) [2 bytes]",
            actual = segmentInfos.first().toString()
        )
    }

    @Test
    fun testFindSegmentInfosRejectsIllegalLength() {

        /* A segment with a length of 2 bytes. */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(),
            0x00, 0x02
        )

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(bytes)
            )
        }
    }

    /**
     * All JPEG readers must reject zero-length segments.
     */
    @Test
    fun testReadersRejectIllegalSegmentLength() {

        /*
         * SOI, a valid APP0, a zero-length APP1, SOS and EOI.
         * At least 16 bytes are required for the format detection.
         */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(), 0x00, 0x04, 0x00, 0x00,
            0xFF.toByte(), 0xE1.toByte(), 0x00, 0x02,
            0xFF.toByte(), 0xDA.toByte(),
            0xFF.toByte(), 0xD9.toByte()
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }

        assertFailsWith<ImageReadException> {
            JpegImageParser.getImageSize(ByteArrayByteReader(bytes))
        }

        assertFailsWith<ImageReadException> {
            JpegMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
        }

        assertFailsWith<ImageReadException> {
            JpegOrientationOffsetFinder.findOrientationOffset(ByteArrayByteReader(bytes))
        }
    }

    /**
     * All JPEG readers must reject segments whose length
     * exceeds the remaining bytes.
     */
    @Test
    fun testReadersRejectSegmentLengthBeyondFile() {

        /*
         * SOI, a valid APP0, and an APP1 segment whose length of 16 bytes
         * exceeds the remaining bytes. At least 16 bytes are required for
         * the format detection.
         */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(), 0x00, 0x06, 0x00, 0x00, 0x00, 0x00,
            0xFF.toByte(), 0xE1.toByte(), 0x00, 0x10, 0x00, 0x00
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }

        assertFailsWith<ImageReadException> {
            JpegImageParser.getImageSize(ByteArrayByteReader(bytes))
        }

        assertFailsWith<ImageReadException> {
            JpegMetadataExtractor.extractMetadataBytes(ByteArrayByteReader(bytes))
        }

        assertFailsWith<ImageReadException> {
            JpegOrientationOffsetFinder.findOrientationOffset(ByteArrayByteReader(bytes))
        }
    }

    @Test
    fun testTiffReaderRejectsMismatchedByteOrder() {

        assertFailsWith<ImageReadException> {
            TiffReader.readTiffHeader(
                ByteArrayByteReader(byteArrayOf('I'.code.toByte(), 'M'.code.toByte()))
            )
        }
    }

    @Test
    fun testTiffReaderRejectsInvalidByteOrder() {

        assertFailsWith<ImageReadException> {
            TiffReader.readTiffHeader(
                ByteArrayByteReader(byteArrayOf('X'.code.toByte(), 'X'.code.toByte()))
            )
        }
    }

    @Test
    fun testTiffReaderRejectsFileWithoutDirectories() {

        /* A valid TIFF header with an offset pointing beyond the file. */
        val bytes = byteArrayOf(
            'I'.code.toByte(), 'I'.code.toByte(),
            42, 0,
            0, 0, 0, 100
        )

        assertFailsWith<ImageReadException> {
            TiffReader.read(bytes)
        }
    }

    @Test
    fun testBoxTypeEquality() {

        val first = BoxType.of("ftyp".encodeToByteArray())
        val second = BoxType.of("ftyp".encodeToByteArray())
        val other = BoxType.of("moov".encodeToByteArray())

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, other)
        assertNotEquals<Any>(first, "ftyp")
        assertEquals(first, first)

        /* Special Apple 4CC types with the copyright byte. */
        val fmt = BoxType.of(byteArrayOf(0xA9.toByte(), 'f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte()))
        assertEquals(BoxType.FMT, fmt)

        val ainf = BoxType.of(byteArrayOf(0xA9.toByte(), 'i'.code.toByte(), 'n'.code.toByte(), 'f'.code.toByte()))
        assertEquals(BoxType.AINF, ainf)
    }

    @Test
    fun testTiffOutputField() {

        val field = TiffOutputField(
            tag = 0x0100,
            fieldType = FieldTypeLong,
            count = 1,
            bytes = byteArrayOf(0, 0, 0, 1)
        )

        assertEquals("0x0100", field.tagFormatted)
        assertEquals("TiffOutputField 0x0100", field.toString())
        assertTrue(field.isLocalValue)
        assertEquals(null, field.separateValue)

        /* Same tag, different sort hint. */
        val other = TiffOutputField(
            tag = 0x0100,
            fieldType = FieldTypeLong,
            count = 1,
            bytes = byteArrayOf(0, 0, 0, 2)
        )

        other.sortHint = 5

        assertTrue(field < other)

        /* Different tags sort by tag value. */
        val otherTag = TiffOutputField(
            tag = 0x0101,
            fieldType = FieldTypeLong,
            count = 1,
            bytes = byteArrayOf(0, 0, 0, 1)
        )

        assertTrue(field < otherTag)

        /* Updating with a different size fails. */
        assertFailsWith<ImageWriteException> {
            field.setBytes(byteArrayOf(1, 2))
        }
    }

    @Test
    fun testTiffOutputFieldWithOversizeValue() {

        val field = TiffOutputField(
            tag = 0x0100,
            fieldType = FieldTypeLong,
            count = 4,
            bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        )

        assertTrue(!field.isLocalValue)
        assertTrue(field.separateValue != null)

        /* Setting same-size bytes works. */
        field.setBytes(byteArrayOf(16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
    }
}
