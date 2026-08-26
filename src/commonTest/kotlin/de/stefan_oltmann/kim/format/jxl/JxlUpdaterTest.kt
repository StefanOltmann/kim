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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JxlUpdaterTest : AbstractUpdaterTest("jxl") {

    /**
     * Metadata boxes behind the codestream must not survive an update,
     * otherwise the file ends up with stale duplicates.
     */
    @Test
    fun testUpdateJxlpTrailingMetadataIsStripped() {

        val updatedBytes = Kim.update(
            bytes = createJxlpFileWithTrailingMetadata(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The new Exif box is written, the stale ones behind the codestream are dropped. */
        assertEquals(1, updatedBytes.countOccurrences("Exif"))
        assertFalse(updatedBytes.decodeToString().contains(STALE_XMP))
    }

    /**
     * Regression test: a trailing box whose declared size is smaller than
     * its own header has no payload at all. The metadata branch skips such
     * a box without writing or consuming anything, so the following bytes
     * were misinterpreted as the next box header and the rest of the file
     * was copied garbled. An undersized box must be rejected instead.
     */
    @Test
    fun testDeleteMetadataRejectsTrailingBoxSmallerThanHeader() {

        val jxlpBytes = createJxlpFileWithTrailingMetadata()

        /* A metadata box whose declared size 3 is below the 8 byte header. */
        val malformedBox =
            byteArrayOf(0, 0, 0, 3) + BoxType.XML.bytes

        val exception = assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(jxlpBytes + malformedBox)
        }

        /*
         * The cause pins the rejection to the payload length validation in
         * the writer, not to an unrelated failure further downstream.
         */
        assertTrue(
            exception.cause?.message?.contains("smaller than its header") == true,
            "Unexpected cause: ${exception.cause?.message}"
        )
    }

    /**
     * Regression test: a largesize below both headers leaves no room for
     * a payload and must be rejected like an undersized plain box.
     */
    @Test
    fun testDeleteMetadataRejectsSmallTrailingLargesizeBox() {

        val jxlpBytes = createJxlpFileWithTrailingMetadata()

        val malformedBox =
            byteArrayOf(0, 0, 0, 1) + // size 1 means the real size follows
                BoxType.XML.bytes +
                byteArrayOf(0, 0, 0, 0, 0, 0, 0, 10) // largesize 10 < 2 * 8

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(jxlpBytes + malformedBox)
        }
    }

    /**
     * Metadata boxes behind the codestream must be removed by
     * deleteMetadata, or deleted location data would remain in the file.
     */
    @Test
    fun testDeleteMetadataJxlpTrailingMetadataIsRemoved() {

        val deletedBytes = Kim.deleteMetadata(createJxlpFileWithTrailingMetadata())

        assertEquals(0, deletedBytes.countOccurrences("Exif"))
        assertFalse(deletedBytes.decodeToString().contains(STALE_XMP))
    }

    /**
     * Compressed brob boxes that wrap Exif or XMP must be dropped by
     * deleteMetadata as well, because their content cannot be rewritten
     * without brotli support, but leaving them behind would keep deleted
     * location or description data in the file.
     */
    @Test
    fun testDeleteMetadataRemovesCompressedMetadata() {

        val deletedBytes = Kim.deleteMetadata(createFileWithLeadingBrobBox())

        assertEquals(0, deletedBytes.countOccurrences("brob"))
        assertFalse(deletedBytes.decodeToString().contains(STALE_BROB))
    }

    /**
     * A compressed metadata box behind the codestream must be removed by
     * deleteMetadata too, mirroring the handling of plain Exif and xml
     * boxes in that position.
     */
    @Test
    fun testDeleteMetadataJxlpTrailingCompressedMetadataIsRemoved() {

        val deletedBytes = Kim.deleteMetadata(createJxlpFileWithTrailingBrobBox())

        assertFalse(deletedBytes.decodeToString().contains(STALE_BROB))
    }

    /**
     * A compressed metadata box behind the codestream must not survive an
     * update either, otherwise the file ends up with stale duplicates.
     */
    @Test
    fun testUpdateJxlpTrailingCompressedMetadataIsStripped() {

        val updatedBytes = Kim.update(
            bytes = createJxlpFileWithTrailingBrobBox(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertFalse(updatedBytes.decodeToString().contains(STALE_BROB))
    }

    /**
     * Regression test: re-applying the same update must not rewrite the
     * xml box. The parse → apply → serialize round-trip on unchanged data
     * produces identical output, so the original box is kept byte-stable.
     */
    @Test
    fun testRepeatedUpdateKeepsXmlBoxByteStable() {

        /* First update normalizes the XMP serialization. */
        val firstUpdate = Kim.update(
            bytes = createJxlpFileWithTrailingMetadata(),
            update = MetadataUpdate.Title("stable")
        )

        /* Second update with the same value must not touch the xml box. */
        val secondUpdate = Kim.update(
            bytes = firstUpdate,
            update = MetadataUpdate.Title("stable")
        )

        assertContentEquals(firstUpdate, secondUpdate)
    }

    /**
     * Builds a jxlp file whose Exif and xml boxes come after the codestream
     * boxes, the layout the spec recommends.
     */
    private fun createJxlpFileWithTrailingMetadata(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        /* The header box with index zero and the codestream signature. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /* A data box, which triggers the streaming cut. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 1, 1, 2, 3, 4)
        )

        /* Metadata behind the codestream. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.EXIF,
            payload = byteArrayOf(0, 0, 0, 0) + byteArrayOf(0x49, 0x49, 0x2A, 0, 8, 0, 0, 0)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.XML,
            payload = STALE_XMP.encodeToByteArray()
        )

        return byteWriter.toByteArray()
    }

    /**
     * Regression test: a single-box jxlc file must stream like a jxlp
     * file. The update via stream and via bytes must produce identical
     * output, and metadata behind the codestream must be stripped.
     */
    @Test
    fun testUpdateJxlcFileMatchesBytesAndStripsTrailingMetadata() {

        val jxlcBytes = createJxlcFileWithTrailingMetadata()

        /* The streaming path. */
        val byteWriter = ByteArrayByteWriter()

        Kim.update(
            byteReader = ByteArrayByteReader(jxlcBytes),
            byteWriter = byteWriter,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        val streamedBytes = byteWriter.toByteArray()

        /* The bytes path must produce the same output. */
        val bytesVariant = Kim.update(
            bytes = jxlcBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertContentEquals(bytesVariant, streamedBytes)

        /* The stale metadata behind the codestream must be gone. */
        assertFalse(streamedBytes.decodeToString().contains(STALE_XMP))
    }

    /**
     * Builds a jxlp file with a compressed Exif box before the codestream,
     * where it is parsed into a CompressedBox.
     */
    private fun createFileWithLeadingBrobBox(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.BROB,
            payload = "Exif".encodeToByteArray() + STALE_BROB.encodeToByteArray()
        )

        /* The header box with index zero and the codestream signature. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /* A data box, which triggers the streaming cut. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 1, 1, 2, 3, 4)
        )

        return byteWriter.toByteArray()
    }

    /**
     * Builds a jxlp file with a compressed Exif box behind the codestream
     * boxes, the layout the spec recommends.
     */
    private fun createJxlpFileWithTrailingBrobBox(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        /* The header box with index zero and the codestream signature. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /* A data box, which triggers the streaming cut. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 1, 1, 2, 3, 4)
        )

        /* Metadata behind the codestream. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.BROB,
            payload = "Exif".encodeToByteArray() + STALE_BROB.encodeToByteArray()
        )

        return byteWriter.toByteArray()
    }

    /**
     * Builds a single-box jxlc file with a stale xml box behind the
     * codestream.
     */
    private fun createJxlcFileWithTrailingMetadata(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        /* The complete codestream in one box. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLC,
            payload = byteArrayOf(0xFF.toByte(), 0x0A, 1, 2, 3, 4)
        )

        /* Metadata behind the codestream. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.XML,
            payload = STALE_XMP.encodeToByteArray()
        )

        return byteWriter.toByteArray()
    }

    /**
     * Regression test: a corrupt Exif box must fail the read loudly so the
     * app knows upfront that editing is not possible - an update would
     * replace the unparseable TIFF content with freshly generated bytes.
     * deleteMetadata is exempt: stripping the broken box is exactly what
     * the user asked for.
     */
    @Test
    fun testCorruptExifBoxFailsReadButIsDeletable() {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        /* The header JXLP box with the codestream signature. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /*
         * A corrupt Exif box: the TIFF header offset points beyond the
         * payload, so no TIFF can be parsed from it.
         */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.EXIF,
            payload = byteArrayOf(0, 0, 0, 100.toByte()) + byteArrayOf(1, 2, 3, 4)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.XML,
            payload = GOOD_XMP.encodeToByteArray()
        )

        val bytes = byteWriter.toByteArray()

        /* The read fails loudly - the app knows editing is not safe. */
        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }

        /* ... but the broken box can always be stripped. */
        val deletedBytes = Kim.deleteMetadata(bytes)

        assertFalse(deletedBytes.decodeToString().contains(GOOD_XMP))
    }

    /**
     * Regression test: a truncated brob box may wrap Exif or XMP metadata
     * that an update would drop. Failing the read informs the app upfront
     * that editing is unsafe (read/update symmetry, see Kim.kt).
     */
    @Test
    fun testTruncatedBrobBoxFailsRead() {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /* A brob box with only 2 payload bytes - too short for the type field. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.BROB,
            payload = byteArrayOf(0x45, 0x78)
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(byteWriter.toByteArray())
        }
    }

    private fun writeBox(byteWriter: ByteArrayByteWriter, type: BoxType, payload: ByteArray) {

        byteWriter.writeInt(payload.size + 8, BMFF_BYTE_ORDER)
        byteWriter.write(type.bytes)
        byteWriter.write(payload)
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

    private companion object {

        const val STALE_XMP: String = "stale xmp"

        const val STALE_BROB: String = "stale brob metadata"

        const val GOOD_XMP: String = "<xmp/>"
    }
}
