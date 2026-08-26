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
package de.stefan_oltmann.kim.format.jxl

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the JXL Exif box and its TIFF header offset handling.
 */
class ExifBoxTest {

    /**
     * A minimal valid TIFF.
     */
    private fun minimalTiffBytes(): ByteArray =
        convertHexStringToByteArray(
            "49492a0008000000" + "0100" + "01000100010000002a000000" + "00000000"
        )

    @Test
    fun testExifBoxWithOffsetZero() {

        val tiff = minimalTiffBytes()

        val payload = byteArrayOf(0, 0, 0, 0) + tiff

        val box = ExifBox(
            offset = 0,
            size = (payload.size + 8).toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals(0, box.tiffHeaderOffset)
        assertContentEquals(tiff, box.exifBytes)
        assertNotNull(box.tiffContents)
    }

    /**
     * Files with a non-zero offset, e.g. an "Exif\0\0" prefix before
     * the TIFF header, must be readable.
     */
    @Test
    fun testExifBoxWithNonZeroOffset() {

        val tiff = minimalTiffBytes()

        /* The TIFF header starts at offset 6, after the "Exif\0\0" prefix. */
        val payload = byteArrayOf(0, 0, 0, 6) + "Exif\u0000\u0000".encodeToByteArray() + tiff

        val box = ExifBox(
            offset = 0,
            size = (payload.size + 8).toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals(6, box.tiffHeaderOffset)
        assertContentEquals(tiff, box.exifBytes)
        assertNotNull(box.tiffContents)
    }

    /**
     * Regression test: an invalid offset must degrade instead of throwing.
     * A throwing constructor made JXL files with a corrupt Exif box both
     * unreadable and undeletable.
     */
    @Test
    fun testExifBoxWithInvalidOffsetDegrades() {

        val tiff = minimalTiffBytes()

        /* The offset points beyond the box payload. */
        val beyondPayload = ExifBox(
            offset = 0,
            size = (tiff.size + 12).toLong(),
            largeSize = null,
            payload = byteArrayOf(0, 0, 0, 200.toByte()) + tiff
        )

        assertEquals(200, beyondPayload.tiffHeaderOffset)
        assertContentEquals(byteArrayOf(), beyondPayload.exifBytes)
        assertEquals(null, beyondPayload.tiffContents)

        /* The offset is unsigned, values above Int.MAX_VALUE are invalid. */
        val negativeOffset = ExifBox(
            offset = 0,
            size = (tiff.size + 12).toLong(),
            largeSize = null,
            payload = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()) + tiff
        )

        assertEquals(-1, negativeOffset.tiffHeaderOffset)
        assertContentEquals(byteArrayOf(), negativeOffset.exifBytes)
        assertEquals(null, negativeOffset.tiffContents)
    }

    /**
     * A JXL file with a non-zero offset in its Exif box must be readable.
     */
    @Test
    fun testReadMetadataWithNonZeroOffsetExifBox() {

        val tiff = minimalTiffBytes()

        /* The TIFF header starts at offset 6, after the "Exif\0\0" prefix. */
        val exifBoxPayload = byteArrayOf(0, 0, 0, 6) + "Exif\u0000\u0000".encodeToByteArray() + tiff

        val metadata = Kim.readMetadata(jxlFileWithExifBox(exifBoxPayload))

        assertNotNull(metadata)
        assertNotNull(metadata.exif)
    }

    /**
     * Updating a JXL file with a non-zero offset in its Exif box must work.
     */
    @Test
    fun testUpdateWithNonZeroOffsetExifBox() {

        val tiff = minimalTiffBytes()

        /* The TIFF header starts at offset 6, after the "Exif\0\0" prefix. */
        val exifBoxPayload = byteArrayOf(0, 0, 0, 6) + "Exif\u0000\u0000".encodeToByteArray() + tiff

        val updatedBytes = Kim.update(
            bytes = jxlFileWithExifBox(exifBoxPayload),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val metadata = Kim.readMetadata(updatedBytes)

        assertNotNull(metadata)
        assertNotNull(metadata.exif)
    }

    private fun jxlFileWithExifBox(exifBoxPayload: ByteArray): ByteArray {

        val signatureBox = byteArrayOf(
            0, 0, 0, 12,
            'J'.code.toByte(), 'X'.code.toByte(), 'L'.code.toByte(), ' '.code.toByte(),
            0x0D, 0x0A, 0x87.toByte(), 0x0A
        )

        val ftypPayload = "jxl ".encodeToByteArray() +
            byteArrayOf(0, 0, 0, 0) +
            "jxl ".encodeToByteArray()

        val ftypBox = byteArrayOf(
            0, 0, 0, (ftypPayload.size + 8).toByte(),
            'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte()
        ) + ftypPayload

        val exifBox = byteArrayOf(
            0, 0, 0, (exifBoxPayload.size + 8).toByte(),
            'E'.code.toByte(), 'x'.code.toByte(), 'i'.code.toByte(), 'f'.code.toByte()
        ) + exifBoxPayload

        return signatureBox + ftypBox + exifBox
    }
}
