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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.jpeg.JpegSegmentAnalyzer.JpegSegmentInfo
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputField
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
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

        /*
         * A segment with a length of 1 byte. A length of exactly 2 is an
         * empty segment and spec-legal, so only a length below 2 - which
         * cannot be encoded - is rejected.
         */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(),
            0x00, 0x01
        )

        assertFailsWith<ImageReadException> {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(bytes)
            )
        }
    }

    /*
     * The streaming facade reads the detection header first and prepends it
     * again through PrePendingByteReader. The contentLength of that reader
     * must keep matching the logical stream length (header + rest), so a
     * segment that ends within the last header-sized bytes of the file must
     * not be falsely rejected as truncated.
     */
    @Test
    fun testReadMetadataWithLastSegmentNearEndOfFile() {

        val bytes = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffe10016" + "457869660000" + // APP1 EXIF with empty IFD
                "49492a00" + "08000000" + "0000" + "00000000" + // Minimal valid TIFF
                "ffda0008" + "010100003f00" + // SOS
                "1122" + "ffd9" // Tiny scan data and EOI
        )

        /* The APP1 segment ends 14 bytes before EOF. */
        val metadata = Kim.readMetadata(ByteArrayByteReader(bytes))

        assertEquals(MediaFormat.JPEG, metadata?.mediaFormat)
    }

    /*
     * Attention: A corrupt EXIF segment must fail the read loudly instead
     * of degrading to NULL. Degrading would let a subsequent rewrite
     * silently drop all EXIF data of the file, while other tools may
     * still be able to read or repair it. This is a different level than
     * skipping a single invalid GPS value.
     */
    @Test
    fun testReadMetadataRejectsCorruptExif() {

        val bytes = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffe10012" + "457869660000" + // APP1 EXIF with broken IFD offset
                "49492a00" + "ffffff00" + "0000" + // TIFF header, IFD offset outside the payload
                "ffda0008" + "010100003f00" + // SOS
                "11223344" + "ffd9" // Scan data and EOI
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(ByteArrayByteReader(bytes))
        }
    }

    /*
     * An update of a file with a corrupt EXIF segment must fail closed as
     * well. The full-rewrite fallback would silently drop the unreadable
     * EXIF data of the original file.
     */
    @Test
    fun testUpdateRejectsCorruptExifInsteadOfDroppingIt() {

        val bytes = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffe10012" + "457869660000" + // APP1 EXIF with broken IFD offset
                "49492a00" + "ffffff00" + "0000" + // TIFF header, IFD offset outside the payload
                "ffda0008" + "010100003f00" + // SOS
                "11223344" + "ffd9" // Scan data and EOI
        )

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = bytes,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
            )
        }
    }

    /*
     * A corrupt IPTC stream must fail the read loudly instead of degrading
     * to NULL, for the same reason as corrupt EXIF: degrading would let a
     * subsequent rewrite silently drop all IPTC data of the file.
     */
    @Test
    fun testReadMetadataRejectsCorruptIptc() {

        val bytes = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffed001c" + "50686f746f73686f7020332e3000" + // APP13 "Photoshop 3.0\0"
                "3842494d" + "0404" + "0000" + "10000000" + // 8BIM block with size beyond the data
                "ffda0008" + "010100003f00" + // SOS
                "11223344" + "ffd9" // Scan data and EOI
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(ByteArrayByteReader(bytes))
        }
    }

    /*
     * The write path must fail closed for corrupt IPTC as well, so the
     * unreadable data of the original file is never dropped silently.
     */
    @Test
    fun testUpdateRejectsCorruptIptcInsteadOfDroppingIt() {

        val bytes = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffed001c" + "50686f746f73686f7020332e3000" + // APP13 "Photoshop 3.0\0"
                "3842494d" + "0404" + "0000" + "10000000" + // 8BIM block with size beyond the data
                "ffda0008" + "010100003f00" + // SOS
                "11223344" + "ffd9" // Scan data and EOI
        )

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = bytes,
                update = MetadataUpdate.Title("test")
            )
        }
    }

    /**
     * All JPEG readers must reject segments whose length field is below
     * the spec minimum of 2, because the value cannot be encoded. An
     * empty segment with the length exactly 2 is legal and accepted.
     */
    @Test
    fun testReadersRejectIllegalSegmentLength() {

        /*
         * SOI, a valid APP0, an APP1 with the impossible length 1, SOS
         * and EOI. At least 16 bytes are required for the detection.
         */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xE0.toByte(), 0x00, 0x04, 0x00, 0x00,
            0xFF.toByte(), 0xE1.toByte(), 0x00, 0x01,
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

    /**
     * A file that ends right at the SOS marker has no image bytes and
     * no EOI. The segment analysis must report the SOS segment instead
     * of failing with a confusing negative-length error.
     */
    @Test
    fun testFindSegmentInfosToleratesFileEndingAtSos() {

        /* SOI followed directly by SOS. */
        val bytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(),
            0xFF.toByte(), 0xDA.toByte()
        )

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(
            ByteArrayByteReader(bytes)
        )

        assertEquals(JpegConstants.SOS_MARKER, segmentInfos.last().marker)
    }

    /**
     * A file built from an unbounded number of small header segments
     * must fail the read instead of buffering an unbounded amount of
     * segment data during an update.
     */
    @Test
    fun testReadSegmentsRejectsExcessiveHeaderSize() {

        /* A COM segment with a 14-byte payload. The length field includes itself. */
        val comSegmentContentLength = 14

        val comSegment = byteArrayOf(
            0xFF.toByte(), COM_MARKER.toByte(), 0, (comSegmentContentLength + 2).toByte()
        ) + "0123456789ABCD".encodeToByteArray()

        /* Only the payload of each segment counts towards the limit. */
        val segmentCount = (16 * 1024 * 1024) / comSegmentContentLength + 1

        val headerBytes = ByteArray(segmentCount * comSegment.size)

        for (index in 0 until segmentCount)
            comSegment.copyInto(headerBytes, index * comSegment.size)

        val file = byteArrayOf(0xFF.toByte(), SOI_MARKER.toByte()) + headerBytes

        assertFailsWith<ImageReadException> {
            JpegUtils.readSegments(ByteArrayByteReader(file))
        }
    }

    private companion object {

        const val SOI_MARKER: Int = 0xD8

        const val COM_MARKER: Int = 0xFE
    }
}
