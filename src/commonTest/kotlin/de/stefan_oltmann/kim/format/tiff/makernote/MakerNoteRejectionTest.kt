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
package de.stefan_oltmann.kim.format.tiff.makernote

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression tests for the ExifTool-compatible handling of broken
 * MakerNotes.
 *
 * Like ExifTool, an unreadable MakerNote sub-directory is skipped
 * while the MakerNote is kept as an opaque binary block, and only a
 * MakerNote field that cannot be read at all rejects the file.
 */
class MakerNoteRejectionTest {

    /**
     * Regression test: a MakerNote sub-directory that points to
     * corrupted data is skipped instead of rejecting the file.
     */
    @Test
    fun testBrokenMakerNoteSubDirectoryIsSkipped() {

        val tiffBytes = BrokenMakerNoteTiff.buildTiff(makerNoteValueOffsetHex = "40000000")

        val metadata = assertNotNull(Kim.readMetadata(tiffBytes))

        val makerNoteField = TiffDirectory.findTiffField(
            assertNotNull(metadata.exif).directories,
            ExifTag.EXIF_TAG_MAKER_NOTE
        )

        assertNotNull(makerNoteField, "The MakerNote field must be preserved.")

        /* The broken FocusInfoIFD sub-directory must not appear. */
        assertNull(
            metadata.exif.findMakerNoteSubDirectory(
                TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO
            )
        )
    }

    /**
     * Regression test: a MakerNote field whose value cannot be read
     * rejects the file, matching ExifTool's fatal error for this case.
     */
    @Test
    fun testUnreadableMakerNoteFieldRejectsFile() {

        /* The MakerNote value offset points beyond the file. */
        val tiffBytes = BrokenMakerNoteTiff.buildTiff(makerNoteValueOffsetHex = "ffffff00")

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(tiffBytes)
        }
    }

    /**
     * Regression test: a truncated embedded preview JPEG must not reject
     * the whole RAW/TIFF file. Like ExifTool, the MakerNote inside such a
     * preview is skipped and stays an opaque binary block.
     */
    @Test
    fun testTruncatedEmbeddedJpegIsSkipped() {

        val tiffBytes = convertHexStringToByteArray(
            "49492a00" + // TIFF header
                "08000000" + // IFD0 offset

                /* IFD0: JPG_FROM_RAW pointing to a malformed JPEG. */
                "0100" + // entry count
                "01020100" + "05000000" + "1a000000" + // JPG_FROM_RAW, BYTES, 5, at 26
                "00000000" + // next IFD

                /* SOI followed by an APP1 marker whose length field is cut off. */
                "ffd8ffe100"
        )

        val metadata = assertNotNull(Kim.readMetadata(tiffBytes))

        assertNotNull(metadata.exif)
    }

    /**
     * Regression test: the plist header fields of an Apple RunTime blob
     * are attacker-controlled. A hostile offset size previously caused an
     * IndexOutOfBoundsException behind wild indices; now the malformed
     * blob degrades to an opaque block and the photo stays readable.
     */
    @Test
    fun testHostileAppleRunTimeBlobIsSkipped() {

        /* Index 48 is the Apple iPhone JPEG whose MakerNote has a RunTime blob. */
        val original = KimTestData.getBytesOf(48)

        val metadata = assertNotNull(Kim.readMetadata(original))

        val exif = assertNotNull(metadata.exif)

        val runTimeField = exif.makerNoteDirectory?.entries?.find { it.tag == 0x0003 }

        val blobStart = assertNotNull(runTimeField?.valueOffset)
        val blobLength = runTimeField.count

        assertTrue(blobLength >= 40, "Fixture changed: RunTime blob too small.")

        /*
         * A huge offset size makes the offset-table walk run past the end
         * of the blob, which crashed before the bounds checks existed.
         */
        val corrupted = original.copyOf()

        corrupted[blobStart + blobLength - 26] = 0x7F

        val corruptedMetadata = assertNotNull(Kim.readMetadata(corrupted))

        val corruptedExif = assertNotNull(corruptedMetadata.exif)

        /* The malformed RunTime directory is gone ... */
        assertNull(corruptedExif.findMakerNoteSubDirectory(TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME))

        /* ... while the rest of the MakerNote survives. */
        assertNotNull(corruptedExif.makerNoteDirectory)
    }

    /**
     * Regression test: a MakerNote whose IFD cannot be read at all (here:
     * an insane entry count running past the end of the file) must keep
     * the MakerNote as an opaque block instead of rejecting the whole
     * file, as documented on MakerNoteHandler and implemented by ExifTool.
     */
    @Test
    fun testUnparseableMakerNoteIfdStaysOpaque() {

        val tiffBytes = buildTiffWithUnparseableAppleMakerNote()

        /* Previously this threw an ImageReadException for the whole file. */
        val metadata = assertNotNull(Kim.readMetadata(tiffBytes))

        val exif = assertNotNull(metadata.exif)

        /* The unparseable MakerNote produced no directory ... */
        assertNull(exif.makerNoteDirectory)

        /* ... but the rest of the metadata survives. */
        assertEquals("Apple", exif.directories.firstOrNull()?.findField(TiffTag.TIFF_TAG_MAKE)?.toStringValue())
    }

    /**
     * Builds a minimal little-endian TIFF with an Apple MakerNote whose
     * embedded IFD declares an entry count that runs past the end of the
     * file, so reading the MakerNote directory fails.
     */
    private fun buildTiffWithUnparseableAppleMakerNote(): ByteArray {

        val out = ByteArrayByteWriter()

        val appleSignature = "Apple iOS\u0000\u0000\u0001".encodeToByteArray()

        /* Signature, byte order and an entry count that exceeds the file. */
        val makerNote =
            appleSignature +
                byteArrayOf(0x49, 0x49) +
                byteArrayOf(0xFF.toByte(), 0xFF.toByte())

        val ifd0Offset = 8
        val ifd0Size = 2 + 2 * ENTRY_LENGTH + 4
        val makeDataOffset = ifd0Offset + ifd0Size
        val exifIfdOffset = makeDataOffset + 6
        val exifIfdSize = 2 + ENTRY_LENGTH + 4
        val makerNoteOffset = exifIfdOffset + exifIfdSize

        out.write(byteArrayOf(0x49, 0x49, 0x2A, 0x00)) // TIFF header
        out.writeInt(ifd0Offset, ByteOrder.LITTLE_ENDIAN)

        /* IFD0: Make + Exif IFD pointer. */
        out.write2BytesAsInt(2, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(TiffTag.TIFF_TAG_MAKE.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_ASCII, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(6, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makeDataOffset, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(ExifTag.EXIF_TAG_EXIF_OFFSET.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_LONG, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(1, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(exifIfdOffset, ByteOrder.LITTLE_ENDIAN)

        out.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next IFD.

        out.write("Apple\u0000".encodeToByteArray())

        /* Exif IFD: MakerNote pointing at the hostile blob. */
        out.write2BytesAsInt(1, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(ExifTag.EXIF_TAG_MAKER_NOTE.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_UNDEFINED, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makerNote.size, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(makerNoteOffset, ByteOrder.LITTLE_ENDIAN)

        out.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next IFD.

        out.write(makerNote)

        return out.toByteArray()
    }

    private companion object {

        private const val ENTRY_LENGTH: Int = 12

        private const val TYPE_ASCII: Int = 2

        private const val TYPE_LONG: Int = 4

        private const val TYPE_UNDEFINED: Int = 7
    }
}
