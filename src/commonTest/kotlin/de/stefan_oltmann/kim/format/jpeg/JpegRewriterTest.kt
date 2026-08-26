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
import de.stefan_oltmann.kim.common.convertToSummary
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toHex
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
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import de.stefan_oltmann.xmp.XMPMetaFactory
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testChangeMetadata() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            /* Broken files are rejected by the segment length validation. */
            if (index == 44 || index == 45 || index == 47)
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

            JpegRewriter.updateExifMetadata(
                ByteArrayByteReader(bytes), exifWriter, outputSet
            )

            val newExifBytes = exifWriter.toByteArray()

            val iptcWriter = ByteArrayByteWriter()

            JpegRewriter.writeIPTC(ByteArrayByteReader(newExifBytes), iptcWriter, newPhotoshopData)

            val iptcBytes = iptcWriter.toByteArray()

            val xmpWriter = ByteArrayByteWriter()

            JpegRewriter.updateXmpXml(ByteArrayByteReader(iptcBytes), xmpWriter, newXmp)

            val actualMetadataBytes = xmpWriter.toByteArray()

            ModifiedBytesVerifier.verify(index, "jpg", actualMetadataBytes)
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    @Suppress("LoopWithTooManyJumpStatements", "LongMethod", "NestedBlockDepth")
    fun testRewriteMetadataUnchanged() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            /* Broken files are rejected by the segment length validation. */
            if (index == 44 || index == 45 || index == 47)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val expectedMetadata = Kim.readMetadata(bytes) as MediaMetadata

            val expectedOutputSet = expectedMetadata.exif?.createOutputSet() ?: continue

            val byteWriter = ByteArrayByteWriter()

            JpegRewriter.updateExifMetadata(ByteArrayByteReader(bytes), byteWriter, expectedOutputSet)

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

            /* Broken files are rejected by the segment length validation. */
            if (index == 44 || index == 45 || index == 47)
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
     * XMP larger than a single JPEG segment is written using Adobe extended
     * XMP, exactly like ExifTool does it: one main packet plus extension
     * segments that carry the GUID and chunks of the extended data. The
     * keywords must survive a full round trip.
     */
    @Test
    fun testUpdateXmpLargerThanMaxSegmentUsesExtendedXmp() {

        val largeKeywords = (1..largeXmpKeywordCount)
            .map { index -> "keyword_$index" }
            .toSet()

        val largeXmp = buildXmpForKeywords(largeKeywords)

        assertTrue(largeXmp.encodeToByteArray().size > JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            xmpXml = largeXmp
        )

        val newBytes = byteWriter.toByteArray()

        /* One main packet plus at least one extension segment. */
        assertEquals(
            1,
            newBytes.countOccurrences(JpegConstants.XMP_IDENTIFIER.decodeToString())
        )
        assertTrue(
            newBytes.countOccurrences(JpegConstants.EXTENDED_XMP_IDENTIFIER.decodeToString()) >= 1
        )

        /* The complete metadata must survive the round trip. */
        val roundTripKeywords = Kim.readMetadata(newBytes)?.convertToSummary()?.keywords

        assertEquals(largeKeywords, roundTripKeywords)
    }

    /**
     * The editable padding of an XMP packet carries no information. A
     * packet that only exceeds the segment size because of its padding is
     * trimmed and written as a single segment.
     */
    @Test
    fun testUpdateTrimsOversizedXmpPadding() {

        /*
         * A valid small packet whose padding alone pushes it beyond the
         * segment size limit.
         */
        val paddedXmp = """
            <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
               <dc:title>
                <rdf:Alt>
                 <rdf:li xml:lang="x-default">Padding Test</rdf:li>
                </rdf:Alt>
               </dc:title>
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
        """.trimIndent() + " ".repeat(200_000) + "<?xpacket end=\"w\"?>"

        assertTrue(paddedXmp.encodeToByteArray().size > JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            xmpXml = paddedXmp
        )

        val newBytes = byteWriter.toByteArray()

        /* Exactly one APP1 XMP segment must have been written. */
        assertEquals(
            1,
            newBytes.countOccurrences(JpegConstants.XMP_IDENTIFIER.decodeToString())
        )

        /* The trimmed packet must survive the round trip. */
        val roundTripXmp = Kim.readMetadata(newBytes)?.xmp

        assertNotNull(roundTripXmp)
        assertTrue(roundTripXmp.contains("Padding Test"))
        assertFalse(roundTripXmp.contains(" ".repeat(1000)))
        assertTrue(roundTripXmp.encodeToByteArray().size <= JpegConstants.MAX_XMP_BYTES_PER_SEGMENT)
    }

    private fun buildXmpForKeywords(keywords: Set<String>): String =
        XmpWriter.updateXmp(
            xmpMeta = XMPMetaFactory.create(),
            update = MetadataUpdate.Keywords(keywords),
            writePackageWrapper = true
        )

    /**
     * IPTC data larger than a single JPEG segment is split across multiple
     * APP13 segments, exactly like Photoshop and ExifTool do it. The
     * keywords must survive a full round trip.
     */
    @Test
    fun testUpdateIptcLargerThanMaxSegmentIsSplit() {

        val keywords = (1..iptcKeywordCount)
            .map { index -> "keyword_$index" }
            .toSet()

        val blockData = IptcWriter.writeIptcBlockData(
            keywords.sorted().map { keyword -> IptcRecord(IptcTypes.KEYWORDS, keyword) }
        )

        assertTrue(
            blockData.size > JpegConstants.MAX_PHOTOSHOP_BYTES_PER_SEGMENT,
            "Test IPTC block data must exceed one APP13 segment, but is ${blockData.size} bytes."
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

        /* The data spans several APP13 segments. */
        assertTrue(
            newBytes.countOccurrences(JpegConstants.APP13_IDENTIFIER.decodeToString()) >= 2
        )

        /* The complete metadata must survive the round trip. */
        val roundTripKeywords = Kim.readMetadata(newBytes)?.iptc?.records
            ?.filter { record -> record.iptcType == IptcTypes.KEYWORDS }
            ?.map { record -> record.value }
            ?.toSet()

        assertEquals(keywords, roundTripKeywords)
    }

    /**
     * Verifies that a single IPTC dataset larger than 32767 bytes (extended-length
     * encoding) survives a write and re-read round trip.
     */
    @Test
    fun testUpdateIptcWithDatasetLargerThanMaxSegmentSize() {

        val description = "x".repeat(40_000)

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
            JpegRewriter.updateExifMetadata(
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

        JpegRewriter.updateExifMetadata(
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

        JpegRewriter.updateExifMetadata(ByteArrayByteReader(baseJpeg), byteWriter, outputSet)

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

    /**
     * Regression test: a JPEG that consists only of image data gets its
     * XMP between SOI and SOS.
     *
     * The old code appended the segments behind the image data, where no
     * reader looks, so the metadata silently disappeared: the result
     * started with the SOS marker instead of an APP1 segment and
     * [Kim.readMetadata] reported no XMP at all. Both is asserted here.
     */
    @Test
    fun testUpdateXmpOnJpegWithoutHeaderSegmentsIsPlacedBeforeImage() {

        val outputWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(createBareJpeg()),
            byteWriter = outputWriter,
            xmpXml = "<x:xmpmeta/>"
        )

        val updatedBytes = outputWriter.toByteArray()

        /* SOI followed directly by the new APP1 XMP segment ... */
        assertEquals("ffd8ffe1", updatedBytes.copyOfRange(0, 4).toHex())

        /* ... and the image data must still be present behind it. */
        assertTrue(updatedBytes.size > createBareJpeg().size)

        assertTrue(Kim.readMetadata(updatedBytes)?.xmp?.contains("x:xmpmeta") == true)
    }

    /**
     * Regression test: EXIF can always be placed before the image data,
     * even when the JPEG has no header segments at all. The old code
     * failed with a ClassCastException on the image data piece.
     */
    @Test
    fun testUpdateExifOnJpegWithoutHeaderSegmentsSucceeds() {

        val outputSet = TiffOutputSet()

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_ORIENTATION, 1)

        val outputWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadata(
            byteReader = ByteArrayByteReader(createBareJpeg()),
            byteWriter = outputWriter,
            outputSet = outputSet
        )

        val updatedBytes = outputWriter.toByteArray()

        /* SOI followed directly by the new APP1 EXIF segment. */
        assertEquals("ffd8ffe1", updatedBytes.copyOfRange(0, 4).toHex())

        assertNotNull(Kim.readMetadata(updatedBytes)?.exif)
    }

    /**
     * Regression test: the streaming update path receives only the header
     * segments, so an empty list places the metadata right before the SOS
     * marker. The output must be identical to the non-streaming path.
     */
    @Test
    fun testUpdateTitleOnJpegWithoutHeaderSegmentsSucceeds() {

        val updatedBytes = Kim.update(
            bytes = createBareJpeg(),
            update = MetadataUpdate.Title("Bare")
        )

        assertEquals("ffd8ffe1", updatedBytes.copyOfRange(0, 4).toHex())

        assertTrue(Kim.readMetadata(updatedBytes)?.xmp?.contains("Bare") == true)
    }

    /**
     * For files with header segments, but without APP segments, the
     * insertion position behind the first header segment is kept for
     * byte compatibility with previous releases.
     */
    @Test
    fun testUpdateXmpWithoutAppSegmentsKeepsLegacyPosition() {

        val outputWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(createJpegWithCommentSegments()),
            byteWriter = outputWriter,
            xmpXml = "<x:xmpmeta/>"
        )

        val updatedBytes = outputWriter.toByteArray()

        /*
         * SOI, COM1 and then the new APP1 segment - not behind the
         * second comment segment or the image data.
         */
        val com1End = 2 + 7

        assertEquals("ffe1", updatedBytes.copyOfRange(com1End, com1End + 2).toHex())
    }

    private fun createBareJpeg(): ByteArray {

        val writer = ByteArrayByteWriter()

        writer.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte())) // SOI

        /* SOS with minimal parameters and entropy-coded data. */
        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xDA.toByte(), 0x00, 0x08,
                0x01, 0x01, 0x00, 0x00, 0x3F, 0x00
            )
        )

        /* Entropy-coded image data. */
        writer.write(byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte()))

        writer.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte())) // EOI

        return writer.toByteArray()
    }

    /**
     * Builds a JPEG with two comment segments and no APP segments.
     */
    private fun createJpegWithCommentSegments(): ByteArray {

        val writer = ByteArrayByteWriter()

        writer.write(byteArrayOf(0xFF.toByte(), 0xD8.toByte())) // SOI

        /* COM1 with the payload "abc". */
        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xFE.toByte(), 0x00, 0x05,
                'a'.code.toByte(), 'b'.code.toByte(), 'c'.code.toByte()
            )
        )

        /* COM2 with the payload "xyz". */
        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xFE.toByte(), 0x00, 0x05,
                'x'.code.toByte(), 'y'.code.toByte(), 'z'.code.toByte()
            )
        )

        /* SOS with minimal parameters and entropy-coded data. */
        writer.write(
            byteArrayOf(
                0xFF.toByte(), 0xDA.toByte(), 0x00, 0x08,
                0x01, 0x01, 0x00, 0x00, 0x3F, 0x00
            )
        )

        /* Entropy-coded image data. */
        writer.write(byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte()))

        writer.write(byteArrayOf(0xFF.toByte(), 0xD9.toByte())) // EOI

        return writer.toByteArray()
    }

    private fun ByteArray.countOccurrences(needle: String): Int {

        val needleBytes = needle.encodeToByteArray()

        var count = 0

        for (index in 0..size - needleBytes.size) {

            var matches = true

            for (needleIndex in needleBytes.indices)
                if (this[index + needleIndex] != needleBytes[needleIndex]) {

                    matches = false

                    break
                }

            if (matches)
                count++
        }

        return count
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
