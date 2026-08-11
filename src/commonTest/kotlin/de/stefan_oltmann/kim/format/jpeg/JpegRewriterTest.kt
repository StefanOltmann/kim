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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.xmp.XmpWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.io.files.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.test.fail

class JpegRewriterTest {

    private val newDate = "2023:05:10 13:37:42"

    private val keywordWithUmlauts = "Umlauts: äöüß"

    private val crashBuildingGps = GpsCoordinates(
        53.219391,
        8.239661
    )

    /* language=XML */
    private val newXmp = """
        <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                  xmp:Rating="3"/>
              </rdf:RDF>
            </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    private val photosWithoutEmbeddedXmp =
        setOf(2, 20, 23, 30, 48)

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testChangeMetadata() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            // FIXME APP1 segment is too long for rewrite
            if (index == 41)
                continue

            // FIXME Corrupt file. Normal fields have offset in Makernote space.
            //   This should be detected to keep Makernote as is.
            //   ExifTool behaves the same.
            if (index == 42)
                continue

            // FIXME Handle extra ExifOffset in IFD1 (thumbnail)
            if (index == 43)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val metadata = Kim.readMetadata(bytes)

            val exif: TiffContents? = metadata?.exif

            val outputSet: TiffOutputSet = exif?.createOutputSet() ?: TiffOutputSet()

            val rootDirectory = outputSet.getOrCreateRootDirectory()
            val exifDirectory = outputSet.getOrCreateExifDirectory()

            /* Rotate by 180 degrees */

            rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
            rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

            /* Set new date */

            rootDirectory.removeField(TiffTag.TIFF_TAG_DATE_TIME)
            rootDirectory.add(TiffTag.TIFF_TAG_DATE_TIME, newDate)

            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, newDate)

            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED, newDate)

            /* Set GPS */

            outputSet.setGpsCoordinates(crashBuildingGps)

            /* IPTC */

            val iptcMetadata = metadata?.iptc

            val newBlocks = iptcMetadata?.nonIptcBlocks ?: emptyList()
            val oldRecords = iptcMetadata?.records ?: emptyList()

            val newRecords = mutableListOf<IptcRecord>()
            newRecords.addAll(oldRecords)

            newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keywordWithUmlauts))

            val newPhotoshopData = IptcMetadata(newRecords, newBlocks)

            /* Write end result */

            val exifWriter = ByteArrayByteWriter()

            JpegRewriter.updateExifMetadataLossless(
                ByteArrayByteReader(bytes), exifWriter, outputSet
            )

            val newExifBytes = exifWriter.toByteArray()

            val iptcWriter = ByteArrayByteWriter()

            JpegRewriter.writeIPTC(ByteArrayByteReader(newExifBytes), iptcWriter, newPhotoshopData)

            val iptcBytes = iptcWriter.toByteArray()

            val xmpWriter = ByteArrayByteWriter()

            JpegRewriter.updateXmpXml(ByteArrayByteReader(iptcBytes), xmpWriter, newXmp)

            val actualMetadataBytes = xmpWriter.toByteArray()

            val expectedMetadataBytes = KimTestData.getModifiedBytesOf(index)

            val equals = expectedMetadataBytes.contentEquals(actualMetadataBytes)

            if (!equals) {

                Path("build/media_${index}_modified.jpg")
                    .writeBytes(actualMetadataBytes)

                /* Also write a string representation to see differences more quickly. */
                Path("build/media_${index}_modified.txt")
                    .writeBytes(Kim.readMetadata(actualMetadataBytes).toString().encodeToByteArray())

                fail("Media $index has not the expected bytes!")
            }
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    @Suppress("LoopWithTooManyJumpStatements", "LongMethod", "NestedBlockDepth")
    fun testRewriteMetadataUnchanged() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            // FIXME APP1 segment is too long for rewrite
            if (index == 41)
                continue

            // FIXME Corrupt file. Normal fields have offset in Makernote space.
            //   This should be detected to keep Makernote as is.
            //   ExifTool behaves the same.
            if (index == 42)
                continue

            // FIXME Handle extra ExifOffset in IFD1 (thumbnail)
            if (index == 43)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val expectedMetadata = Kim.readMetadata(bytes) as MediaMetadata

            val expectedOutputSet = expectedMetadata.exif?.createOutputSet() ?: continue

            val byteWriter = ByteArrayByteWriter()

            JpegRewriter.updateExifMetadataLossless(ByteArrayByteReader(bytes), byteWriter, expectedOutputSet)

            val newBytes = byteWriter.toByteArray()

            val actualMetadata = Kim.readMetadata(newBytes)

            assertNotNull(actualMetadata)

            assertNotSame(expectedMetadata, actualMetadata)

            val actualOutputSet = actualMetadata.exif?.createOutputSet() ?: continue

            assertNotSame(expectedOutputSet, actualOutputSet)

            assertEquals(
                expectedOutputSet.getDirectories().size,
                actualOutputSet.getDirectories().size,
                "Different directory count."
            )

            for (directoryIndex in expectedOutputSet.getDirectories().indices) {

                val expectedDirectory = expectedOutputSet.getDirectories()[directoryIndex]
                val actualDirectory = actualOutputSet.getDirectories()[directoryIndex]

                val fieldCountMatches = expectedDirectory.getFields().size == actualDirectory.getFields().size

                if (!fieldCountMatches) {

                    val expectedTagInfos = expectedDirectory.getFields().map { it.tag }.sorted()
                    val actualTagInfos = actualDirectory.getFields().map { it.tag }.sorted()

                    /* For some reason these offsets disappear, even if they are written. */
                    val missingTagInfos = expectedTagInfos - actualTagInfos.toSet() -
                        ExifTag.EXIF_TAG_EXIF_OFFSET.tag -
                        ExifTag.EXIF_TAG_GPSINFO.tag -
                        ExifTag.EXIF_TAG_INTEROP_OFFSET.tag -
                        TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag

                    if (missingTagInfos.isNotEmpty())
                        fail(
                            "For file $index, expected ${expectedTagInfos.size} tags, " +
                                "but got ${actualTagInfos.size} tags. Missing: $missingTagInfos." +
                                "Expected: $expectedTagInfos. Actual: $actualTagInfos"
                        )
                }

                val expectedFields = expectedDirectory.getFields()

                @Suppress("LoopWithTooManyJumpStatements")
                for (expectedField in expectedFields) {

                    val actualField = actualDirectory.findField(expectedField.tag)

                    /* Ignore missing offset fields that may not be needed after rewrite. */
                    @Suppress("ComplexCondition")
                    if (expectedField.tag == ExifTag.EXIF_TAG_EXIF_OFFSET.tag ||
                        expectedField.tag == ExifTag.EXIF_TAG_GPSINFO.tag ||
                        expectedField.tag == ExifTag.EXIF_TAG_INTEROP_OFFSET.tag ||
                        expectedField.tag == TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag
                    )
                        continue

                    /* Why is there this tag? */
                    if (expectedField.tag == -1)
                        continue

                    /* JPEGInterchangeFormatLength != JpgFromRawLength */
                    if (expectedField.tag == 514)
                        continue

                    assertNotNull(
                        actualField,
                        "Image $index, value for ${expectedField.tagFormatted} not found."
                    )

                    assertEquals(
                        expectedField.tag,
                        actualField.tag,
                        "Tag mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.fieldType,
                        actualField.fieldType,
                        "Field type mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.count,
                        actualField.count,
                        "Count mismatch for image #$index."
                    )

                    /* Value of offsets is expected to change due to rewrites. */
                    if (
                        expectedField.tag == exifOffsetTag ||
                        expectedField.tag == interopOffsetTag ||
                        expectedField.tag == gpsInfoTag
                    )
                        continue

                    /* Some fields will be auto-corrected. */
                    if (
                        expectedField.tag == ExifTag.EXIF_TAG_USER_COMMENT.tag ||
                        expectedField.tag == TiffTag.TIFF_TAG_ARTIST.tag ||
                        expectedField.tag == TiffTag.TIFF_TAG_COPYRIGHT.tag
                    )
                        continue

                    val expectedBytesAsHex = expectedField.bytesAsHex()
                    val actualBytesAsHex = actualField.bytesAsHex()

                    val bytesEqual = expectedBytesAsHex == actualBytesAsHex

                    if (!bytesEqual) {

                        assertEquals(
                            expected = expectedBytesAsHex,
                            actual = actualBytesAsHex,
                            message = "Value mismatch for image #$index and field ${expectedField.tagFormatted}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testUpdateXmp() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            /* Skip files without embedded XMP */
            if (photosWithoutEmbeddedXmp.contains(index))
                continue

            val bytes = KimTestData.getBytesOf(index)

            val originalXmp = Kim.readMetadata(bytes)?.xmp

            val newXmp = KimTestData.getFormattedXmp(index)

            assertNotEquals(originalXmp, newXmp)

            val byteWriter = ByteArrayByteWriter()

            JpegRewriter.updateXmpXml(ByteArrayByteReader(bytes), byteWriter, newXmp)

            val newBytes = byteWriter.toByteArray()

            val newBytesXmp = Kim.readMetadata(newBytes)?.xmp

            assertEquals(newXmp, newBytesXmp)
        }
    }

    /**
     * Verifies that XMP larger than the maximal JPEG segment size survives a
     * write and re-read round trip.
     */
    @Test
    fun testUpdateXmpLargerThanMaxSegmentSize() {

        val largeXmp = buildLargeXmp()

        val largeXmpByteCount = largeXmp.encodeToByteArray().size

        assertTrue(
            largeXmpByteCount > JpegConstants.MAX_SEGMENT_SIZE,
            "Test XMP must exceed one JPEG segment, but is $largeXmpByteCount bytes."
        )

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            xmpXml = largeXmp
        )

        val newBytes = byteWriter.toByteArray()

        val roundTripXmp = Kim.readMetadata(newBytes)?.xmp

        assertEquals(largeXmp, roundTripXmp)
    }

    private fun buildLargeXmp(): String {

        val keywords = (1..largeXmpKeywordCount)
            .map { index -> "keyword_$index" }
            .toSet()

        return XmpWriter.updateXmp(
            xmpMeta = XMPMetaFactory.create(),
            update = MetadataUpdate.Keywords(keywords),
            writePackageWrapper = true
        )
    }

    /**
     * Verifies that IPTC data larger than the maximal JPEG segment size
     * survives a write and re-read round trip.
     */
    @Test
    fun testUpdateIptcLargerThanMaxSegmentSize() {

        val keywords = (1..iptcKeywordCount)
            .map { index -> "keyword_$index" }
            .toSet()

        val blockData = IptcWriter.writeIptcBlockData(
            keywords.sorted().map { keyword -> IptcRecord(IptcTypes.KEYWORDS, keyword) }
        )

        assertTrue(
            blockData.size > JpegConstants.MAX_SEGMENT_SIZE,
            "Test IPTC block data must exceed one JPEG segment, but is ${blockData.size} bytes."
        )

        val iptcMetadata = IptcMetadata(
            records = keywords.sorted().map { keyword -> IptcRecord(IptcTypes.KEYWORDS, keyword) },
            rawBlocks = emptyList()
        )

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            metadata = iptcMetadata
        )

        val newBytes = byteWriter.toByteArray()

        val roundTripKeywords = Kim.readMetadata(newBytes)?.iptc?.records
            ?.filter { record -> record.iptcType == IptcTypes.KEYWORDS }
            ?.map { record -> record.value }
            ?.toSet()

        assertEquals(keywords, roundTripKeywords)

        /*
         * A second update must replace all APP13 segments, including the
         * continuation segments of the split data.
         */
        val secondKeywords = keywords.map { keyword -> "second_$keyword" }.toSet()

        val secondIptcMetadata = IptcMetadata(
            records = secondKeywords.sorted().map { keyword -> IptcRecord(IptcTypes.KEYWORDS, keyword) },
            rawBlocks = emptyList()
        )

        val secondWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(newBytes),
            byteWriter = secondWriter,
            metadata = secondIptcMetadata
        )

        val secondRoundTripKeywords = Kim.readMetadata(secondWriter.toByteArray())?.iptc?.records
            ?.filter { record -> record.iptcType == IptcTypes.KEYWORDS }
            ?.map { record -> record.value }
            ?.toSet()

        assertEquals(secondKeywords, secondRoundTripKeywords)
    }

    /**
     * Verifies that a single IPTC dataset larger than 32767 bytes (extended-length
     * encoding) survives a write and re-read round trip.
     */
    @Test
    fun testUpdateIptcWithDatasetLargerThanMaxSegmentSize() {

        val description = "x".repeat(70_000)

        val iptcMetadata = IptcMetadata(
            records = listOf(IptcRecord(IptcTypes.CAPTION_ABSTRACT, description)),
            rawBlocks = emptyList()
        )

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            metadata = iptcMetadata
        )

        val newBytes = byteWriter.toByteArray()

        val roundTripDescription = Kim.readMetadata(newBytes)?.iptc?.records
            ?.firstOrNull { record -> record.iptcType == IptcTypes.CAPTION_ABSTRACT }
            ?.value

        assertEquals(description, roundTripDescription)
    }

    /**
     * Verifies that an EXIF payload above the maximal segment payload size is
     * rejected instead of producing a JPEG whose segment length field wraps
     * around the 16-bit range.
     */
    @Test
    fun testExifSegmentLargerThanMaxThrows() {

        val baseJpeg = createJpegWithoutExif()

        val fixedExifBytes = measureFixedExifBytes(baseJpeg)

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            JpegRewriter.updateExifMetadataLossless(
                ByteArrayByteReader(baseJpeg),
                byteWriter,
                createOutputSetWithXmpField(smallestRejectedExifPayloadBytes - fixedExifBytes)
            )
        }
    }

    /**
     * Verifies that an EXIF payload of 65530 bytes, the largest size the
     * aligned TIFF writer can produce below the limit, is written with a
     * correct segment length field and survives a read-back.
     */
    @Test
    fun testExifSegmentNearMaxIsWrittenValid() {

        val baseJpeg = createJpegWithoutExif()

        val fixedExifBytes = measureFixedExifBytes(baseJpeg)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(
            ByteArrayByteReader(baseJpeg),
            byteWriter,
            createOutputSetWithXmpField(largestWriteableExifPayloadBytes - fixedExifBytes)
        )

        val newBytes = byteWriter.toByteArray()

        assertEquals(largestWriteableExifPayloadBytes, exifSegmentPayloadSize(newBytes))

        assertNotNull(Kim.readMetadata(newBytes)?.exif)
    }

    /**
     * Returns a minimal JPEG without an EXIF segment, used as base for the
     * lossy EXIF writer. The bundled test photos all carry an EXIF segment.
     */
    @Suppress("MagicNumber")
    private fun createJpegWithoutExif(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(JpegConstants.SOI)

        /* APP0 JFIF segment. */
        val jfifSegment = JpegConstants.JFIF0_SIGNATURE + byteArrayOf(
            0x01, /* version 1.2 */
            0x02,
            0x01, /* density units */
            0x00, 0x01, /* x density */
            0x00, 0x01, /* y density */
            0x00, /* thumbnail width */
            0x00 /* thumbnail height */
        )

        byteWriter.write(
            JpegConstants.JPEG_APP0_MARKER
                .toShort()
                .toBytes(JpegConstants.JPEG_BYTE_ORDER)
        )

        byteWriter.write(
            (jfifSegment.size + SEGMENT_LENGTH_FIELD_BYTES).toShort()
                .toBytes(JpegConstants.JPEG_BYTE_ORDER)
        )

        byteWriter.write(jfifSegment)

        /* SOS marker with empty payload and a minimal image data blob. */

        byteWriter.write(
            JpegConstants.SOS_MARKER.toShort()
                .toBytes(JpegConstants.JPEG_BYTE_ORDER)
        )

        byteWriter.write(
            SEGMENT_LENGTH_FIELD_BYTES.toShort()
                .toBytes(JpegConstants.JPEG_BYTE_ORDER)
        )

        byteWriter.write(JpegConstants.EOI)

        return byteWriter.toByteArray()
    }

    /**
     * Returns the fixed share of the EXIF payload size, derived from writing
     * a calibration XMP field of known size.
     */
    private fun measureFixedExifBytes(baseJpeg: ByteArray): Int {

        val calibratedJpeg = writeExif(
            baseJpeg,
            createOutputSetWithXmpField(calibrationFieldSize)
        )

        return exifSegmentPayloadSize(calibratedJpeg) - calibrationFieldSize
    }

    /**
     * Writes the given output set as EXIF segment into the given JPEG and
     * returns the resulting bytes.
     */
    private fun writeExif(baseJpeg: ByteArray, outputSet: TiffOutputSet): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(ByteArrayByteReader(baseJpeg), byteWriter, outputSet)

        return byteWriter.toByteArray()
    }

    /**
     * Returns an output set whose root directory holds a single XMP field of
     * the given size, which controls the resulting EXIF payload size.
     */
    private fun createOutputSetWithXmpField(fieldSize: Int): TiffOutputSet {

        val outputSet = TiffOutputSet()

        outputSet.getOrCreateRootDirectory().add(TiffTag.TIFF_TAG_XMP, ByteArray(fieldSize))

        return outputSet
    }

    /**
     * Returns the payload size of the first APP1 segment of the given JPEG bytes.
     */
    private fun exifSegmentPayloadSize(jpegBytes: ByteArray): Int {

        var offset = JpegConstants.SOI.size

        while (offset + SEGMENT_HEADER_BYTES <= jpegBytes.size) {

            val marker = (jpegBytes[offset].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 1].toInt() and 0xFF)

            val segmentLength = (jpegBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (jpegBytes[offset + 3].toInt() and 0xFF)

            if (marker == JpegConstants.JPEG_APP1_MARKER)
                return segmentLength - SEGMENT_LENGTH_FIELD_BYTES

            /* The length field counts itself, so the segment is 2 bytes longer. */
            offset += SEGMENT_MARKER_BYTES + segmentLength
        }

        fail("JPEG bytes contain no APP1 segment.")
    }

    companion object {

        private const val largeXmpKeywordCount = 4000

        private const val iptcKeywordCount = 5000

        private const val exifOffsetTag = 0x8769
        private const val interopOffsetTag = 0xa005
        private const val gpsInfoTag = 0x8825

        private const val calibrationFieldSize = 100

        private const val largestWriteableExifPayloadBytes = 65_530

        private const val smallestRejectedExifPayloadBytes = 65_534

        private const val SEGMENT_MARKER_BYTES = 2

        private const val SEGMENT_LENGTH_FIELD_BYTES = 2

        private const val SEGMENT_HEADER_BYTES = SEGMENT_MARKER_BYTES + SEGMENT_LENGTH_FIELD_BYTES
    }
}
