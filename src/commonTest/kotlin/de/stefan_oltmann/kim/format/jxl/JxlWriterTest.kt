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
package de.stefan_oltmann.kim.format.jxl

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JxlWriterTest {

    /* language=XML */
    private val expectedXmp = """
        <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                    xmlns:exif="http://ns.adobe.com/exif/1.0/"
                  exif:DateTimeOriginal="2020-10-05T13:37:42"
                  xmp:Rating="3"/>
              </rdf:RDF>
            </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    /**
     * A box whose size exceeds Int.MAX_VALUE cannot be written, because the
     * size field is a 32-bit integer. It must be rejected with a clear
     * error instead of corrupting the size field.
     */
    @Test
    fun testWriteImageRejectsOversizeBox() {

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            JxlWriter.writeImage(
                boxes = listOf(
                    Box(
                        type = BoxType.JXLP,
                        offset = 0,
                        size = Int.MAX_VALUE + 1L,
                        largeSize = null,
                        payload = byteArrayOf()
                    )
                ),
                byteWriter = byteWriter,
                exifBytes = null,
                xmp = null
            )
        }
    }

    /**
     * Regression test: a file without a JXLP header box and without an
     * FTYP box has no insertion anchor. The metadata must still be written
     * (behind the first box) instead of being silently dropped, while the
     * old metadata was already removed.
     */
    @Test
    fun testWriteImageInsertsMetadataWithoutAnchorBox() {

        val byteWriter = ByteArrayByteWriter()

        JxlWriter.writeImage(
            boxes = listOf(
                Box(
                    type = BoxType.JXLC,
                    offset = 0,
                    size = (BMFFConstants.BOX_HEADER_LENGTH + 4).toLong(),
                    largeSize = null,
                    payload = byteArrayOf(1, 2, 3, 4)
                )
            ),
            byteWriter = byteWriter,
            exifBytes = byteArrayOf(1, 2, 3),
            xmp = "<xmp/>"
        )

        val outputHex = byteWriter.toByteArray().toHex()

        /* Both new metadata boxes must be present in the output. */
        assertTrue(outputHex.contains(BoxType.EXIF.bytes.toHex()))
        assertTrue(outputHex.contains("<xmp/>".encodeToByteArray().toHex()))
    }

    /**
     * Without any box at all there is nothing to attach the metadata to.
     * The update must fail loudly instead of writing a metadata-only file
     * without image data or dropping the metadata silently.
     */
    @Test
    fun testWriteImageRejectsMetadataWithoutAnyBox() {

        assertFailsWith<ImageWriteException> {
            JxlWriter.writeImage(
                boxes = emptyList(),
                byteWriter = ByteArrayByteWriter(),
                exifBytes = byteArrayOf(1),
                xmp = null
            )
        }
    }

    /**
     * Tests that there is no loss if writing
     * the JXL chunks again without any change.
     */
    @Test
    fun testNoChange() {

        for (index in KimTestData.jxlPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val byteWriter = ByteArrayByteWriter()

            JxlWriter.writeImage(
                byteReader = byteReader,
                byteWriter = byteWriter,
                exifBytes = null,
                xmp = null
            )

            val newBytes = byteWriter.toByteArray()

            assertContentEquals(
                expected = bytes,
                actual = newBytes
            )
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateMetadata() {

        for (index in KimTestData.jxlPhotoIds) {

            // TODO Support compressed boxes
            if (index == KimTestData.JXL_CONTAINER_COMPRESSED_INDEX)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val oldMetadata = Kim.readMetadata(bytes)

            assertNotNull(oldMetadata)

            val oldXmp = oldMetadata.xmp

            assertNotEquals(expectedXmp, oldXmp)

            val tiffOutputSet = oldMetadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifDirectory = tiffOutputSet.getOrCreateExifDirectory()

            /*
             * Note: We write a different date to EXIF than to XMP
             * to see which viewer gives priority to which field.
             */
            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2023:08:01 08:00:00")

            val exifBytesWriter = ByteArrayByteWriter()

            val writer = TiffWriter(byteOrder = tiffOutputSet.byteOrder)

            writer.write(exifBytesWriter, tiffOutputSet)

            val exifBytes: ByteArray = exifBytesWriter.toByteArray()

            val byteWriter = ByteArrayByteWriter()

            JxlWriter.writeImage(
                byteReader = ByteArrayByteReader(bytes),
                byteWriter,
                exifBytes,
                expectedXmp
            )

            val newBytes = byteWriter.toByteArray()

            val actualMetadata = Kim.readMetadata(newBytes)

            assertNotNull(actualMetadata)
            assertNotNull(actualMetadata.exif)
            assertNotNull(actualMetadata.xmp)

            assertEquals(
                expected = expectedXmp,
                actual = actualMetadata.xmp
            )

            ModifiedBytesVerifier.verify(index, "jxl", newBytes)
        }
    }
}
