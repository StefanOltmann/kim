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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TiffReaderTest {

    /**
     * Regression test: entries with a corrupt count that makes the value
     * length negative must be skipped instead of crashing the parser.
     */
    @Test
    fun testReadSkipsEntryWithOverflowingCount() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0400" + // 4 entries
                "00010100ffffffff00000000" + // ImageWidth, BYTE, count 0xFFFFFFFF
                "010103007fffffff00000000" + // ImageLength, SHORT, count 0x7FFFFFFF
                "000104000100004000000000" + // NewSubfileType (0x00FE), LONG, count 0x40000001
                "120103000100000001000000" + // Orientation, SHORT, count 1, value 1
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        val entries = tiffContents.directories.first().entries

        /*
         * The three corrupt entries are skipped. The third one wraps the
         * count times type size multiplication around to a small positive
         * value (0x40000001 * 4 truncated to Int is 4), which must not
         * smuggle a bogus local value through the guard.
         */
        assertEquals(1, entries.size)

        assertEquals(0x0112, entries.single().tag)
        assertEquals("0100", entries.single().valueBytes.toHex())
    }

    /**
     * Regression test: the thumbnail offset comes from an unsigned LONG
     * and can resolve beyond the signed Int range in hostile files. The
     * directory must be read without a crash and without a thumbnail.
     */
    @Test
    fun testReadSkipsThumbnailWithNegativeResolvedOffset() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0200" + // 2 entries
                "0102040001000000ffffffff" + // JPEGInterchangeFormat (0x0201), LONG, 0xFFFFFFFF
                "020204000100000010000000" + // JPEGInterchangeFormatLength (0x0202), LONG, 16
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        val directory = tiffContents.directories.first()

        assertNotNull(directory.getJpegImageDataElement())

        /* The hostile offset must not crash the reader nor produce bytes. */
        assertEquals(null, directory.thumbnailBytes)
    }

    /**
     * A GPS sub-IFD that cannot be parsed must fail the read like the
     * Exif sub-IFD does. Silently dropping the pointer field would make
     * the next update remove the GPS data from the file permanently
     * (read/update symmetry, see "Never destroy metadata" in the [Kim]
     * documentation).
     */
    @Test
    fun testReadRejectsTruncatedGpsIfd() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0100" + // 1 entry
                "25880400010000001a000000" + // GPSInfo (0x8825), LONG, count 1, GPS IFD at offset 26
                "00000000" + // No next directory
                "3200" // GPS IFD entry count 0x0032 (50), no entries follow
        )

        assertFailsWith<ImageReadException> {
            TiffReader.read(ByteArrayByteReader(bytes))
        }

        /* The same file must fail the update instead of dropping the GPS block. */
        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = bytes,
                updates = setOf(MetadataUpdate.TakenDate(0L))
            )
        }
    }

    /**
     * A BigTIFF (version 43, 20-byte directory entries) must be rejected.
     * Misreading it as classic TIFF would emit a valid-looking file with
     * truncated metadata on rewrite.
     */
    @Test
    fun testReadRejectsBigTiffHeader() {

        /* Header: II, version 43 (BigTIFF), first IFD at offset 8. */
        val bytes = convertHexStringToByteArray(
            "49492b0008000000" +
                "0000000000000000"
        )

        assertFailsWith<ImageReadException> {
            TiffReader.read(ByteArrayByteReader(bytes))
        }
    }

    /**
     * A single GPS text tag (like UserComment) with a non-byte type is
     * hostile per-entry corruption and must be skipped like every other
     * corrupt field instead of failing the whole directory.
     */
    @Test
    fun testReadSkipsHostileGpsTextTag() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0100" + // 1 entry
                "69870400010000001a000000" + // ExifIFDOffset (0x8769), LONG, EXIF IFD at offset 26
                "00000000" + // No next directory
                "0100" + // EXIF IFD: 1 entry
                "869204000100000041414141" + // UserComment (0x9286), LONG, count 1, "AAAA"
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        val exifDirectory = tiffContents.directories
            .first { it.type == TiffConstants.TIFF_DIRECTORY_EXIF }

        val userComment = exifDirectory.findField(ExifTag.EXIF_TAG_USER_COMMENT)

        /* The hostile field must not yield GPS text. */
        assertEquals(null, userComment?.let { it.value as? String })
    }

    /**
     * Regression test: strip offsets come from unsigned LONGs and can
     * resolve beyond the signed Int range in hostile files. Reading the
     * image data must degrade to NULL instead of crashing.
     */
    @Test
    fun testReadSkipsStripWithNegativeResolvedOffset() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0200" + // 2 entries
                "1101040001000000ffffffff" + // StripOffsets (0x0111), LONG, 0xFFFFFFFF
                "170104000100000010000000" + // StripByteCounts (0x0117), LONG, 16
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(
            byteReader = ByteArrayByteReader(bytes),
            readTiffImageBytes = true
        )

        val directory = tiffContents.directories.first()

        assertNotNull(directory.getStripImageDataElements())

        /* The hostile offset must not crash the reader nor produce bytes. */
        assertEquals(null, directory.tiffImageBytes)
    }

    /**
     * Regression test: a value offset near the Int maximum combined with a
     * huge count made the old Int arithmetic wrap the end position back
     * into the valid range and pass a negative resolved offset into the
     * reader. Such an entry must be skipped instead.
     */
    @Test
    fun testReadSkipsEntryWithValueOffsetWrapAround() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0100" + // 1 entry
                "00010100f9ffffff" + // Unknown tag 0x0100, BYTE, count 0x7FFFFFF9
                "ffffff7f" + // Value offset 0x7FFFFFFF
                "00000000" // No next directory
        )

        /*
         * Old arithmetic computed endPos = 8 + 0x7FFFFFFF + 0x7FFFFFF9 = 0
         * due to the overflow and accepted the entry with a negative
         * resolved offset of -2147483641.
         */
        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        assertEquals(0, tiffContents.directories.first().entries.size)
    }

    /**
     * Regression test: a directory-specific tag must resolve to the field in
     * its directory, not to a same-numbered tag in another directory.
     */
    @Test
    fun testFindTiffFieldWithTagNumberCollision() {

        val bytes = convertHexStringToByteArray(
            "49492a0008000000" + // Header: II, version 42, IFD0 at offset 8
                "0200" + // 2 entries
                "01000100010000002a000000" + // Unknown tag 0x0001, BYTE, count 1, value 42
                "25880400010000002a000000" + // GPSInfo (0x8825), LONG, count 1, offset 42
                "00000000" + // No next directory
                "00000000" + // Padding
                "0100" + // GPS directory at offset 42: 1 entry
                "01000200020000004e000000" + // GPSLatitudeRef (0x0001), ASCII, count 2, value "N"
                "00000000" // No next directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        val gpsField = tiffContents.findTiffField(GpsTag.GPS_TAG_GPS_LATITUDE_REF)

        assertNotNull(gpsField)

        /* The lookup must stay in the GPS directory, not match IFD0's tag 0x0001. */
        assertEquals(TiffConstants.TIFF_DIRECTORY_GPS, gpsField.directoryType)
    }

    /**
     * Regression test: a long chain of directories must be followed
     * iteratively, so a hostile file cannot overflow the call stack.
     */
    @Test
    fun testReadFollowsLongDirectoryChain() {

        val chainLength = 10_000

        val byteWriter = ByteArrayByteWriter()

        writeTiffHeader(byteWriter)

        repeat(chainLength) { index ->

            /* No entries. */
            byteWriter.write(shortArrayOf(0).toBytes(ByteOrder.LITTLE_ENDIAN))

            val nextOffset = 8 + (index + 1) * DIRECTORY_SIZE

            if (index == chainLength - 1)
                byteWriter.writeInt(0, ByteOrder.LITTLE_ENDIAN)
            else
                byteWriter.writeInt(nextOffset, ByteOrder.LITTLE_ENDIAN)
        }

        val tiffContents =
            TiffReader.read(DefaultRandomAccessByteReader(ByteArrayByteReader(byteWriter.toByteArray())))

        assertEquals(chainLength, tiffContents.directories.size)
    }

    /**
     * Regression test: a directory that references itself as the next
     * directory must terminate instead of looping forever.
     */
    @Test
    fun testReadTerminatesOnSelfReferencingDirectory() {

        val bytes = convertHexStringToByteArray(
            "49492a00" + // Header: II, version 42, IFD0 at offset 8
                "08000000" +
                "0000" + // No entries
                "08000000" // Next directory: this very directory
        )

        val tiffContents = TiffReader.read(ByteArrayByteReader(bytes))

        assertEquals(1, tiffContents.directories.size)
    }

    /**
     * Regression test: the next-IFD pointer must not be followed inside
     * sub-directories. Per spec it is zero there; following it escalated
     * the directory type (+1), so a chained GPS IFD was mis-tagged as an
     * Exif IFD.
     */
    @Test
    fun testGpsNextIfdChainDoesNotEscalateToExifDirectory() {

        val byteWriter = ByteArrayByteWriter()

        val ifd0Offset = 8
        val ifd0Size = ENTRY_COUNT_SIZE + ENTRY_SIZE + NEXT_DIRECTORY_SIZE
        val gpsOffset = ifd0Offset + ifd0Size
        val gpsSize = ENTRY_COUNT_SIZE + ENTRY_SIZE + NEXT_DIRECTORY_SIZE
        val paddingSize = 16
        val junkIfdOffset = gpsOffset + gpsSize + paddingSize

        writeTiffHeader(byteWriter)

        /* IFD0: GPSInfo pointer. */
        byteWriter.write(shortArrayOf(1).toBytes(ByteOrder.LITTLE_ENDIAN))

        byteWriter.write(shortArrayOf(0x8825.toShort()).toBytes(ByteOrder.LITTLE_ENDIAN))
        byteWriter.write(shortArrayOf(4).toBytes(ByteOrder.LITTLE_ENDIAN)) // LONG
        byteWriter.writeInt(1, ByteOrder.LITTLE_ENDIAN)
        byteWriter.writeInt(gpsOffset, ByteOrder.LITTLE_ENDIAN)

        byteWriter.writeInt(0, ByteOrder.LITTLE_ENDIAN)

        /* GPS IFD: version tag and an illegal non-zero next-IFD pointer. */
        byteWriter.write(shortArrayOf(1).toBytes(ByteOrder.LITTLE_ENDIAN))

        byteWriter.write(shortArrayOf(GpsTag.GPS_TAG_GPS_VERSION_ID.tag.toShort()).toBytes(ByteOrder.LITTLE_ENDIAN))
        byteWriter.write(shortArrayOf(1).toBytes(ByteOrder.LITTLE_ENDIAN)) // BYTE
        byteWriter.writeInt(4, ByteOrder.LITTLE_ENDIAN)
        byteWriter.write(GpsTag.GPS_VERSION)

        byteWriter.writeInt(junkIfdOffset, ByteOrder.LITTLE_ENDIAN)

        repeat(paddingSize) { byteWriter.write(0) }

        /* The junk IFD that used to be mis-tagged as an Exif IFD. */
        byteWriter.write(shortArrayOf(1).toBytes(ByteOrder.LITTLE_ENDIAN))
        byteWriter.write(shortArrayOf(0x1111).toBytes(ByteOrder.LITTLE_ENDIAN))
        byteWriter.write(shortArrayOf(4).toBytes(ByteOrder.LITTLE_ENDIAN)) // LONG
        byteWriter.writeInt(1, ByteOrder.LITTLE_ENDIAN)
        byteWriter.writeInt(7, ByteOrder.LITTLE_ENDIAN)
        byteWriter.writeInt(0, ByteOrder.LITTLE_ENDIAN)

        val tiffContents =
            TiffReader.read(DefaultRandomAccessByteReader(ByteArrayByteReader(byteWriter.toByteArray())))

        /* The GPS IFD itself parses fine. */
        assertNotNull(tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS))

        /* The chain was not followed, so no escalated Exif IFD exists. */
        assertEquals(null, tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_EXIF))
    }

    /**
     * Regression test: sub-directories chained across many levels via the
     * ExifOffset field must be rejected cleanly once the nesting limit is
     * reached instead of overflowing the call stack.
     */
    @Test
    fun testExcessiveSubDirectoryNestingIsRejected() {

        val nestingLevels = 20

        val byteWriter = ByteArrayByteWriter()

        writeTiffHeader(byteWriter)

        /* Entry count, one ExifOffset entry and no next directory. */
        val levelSize = ENTRY_COUNT_SIZE + ENTRY_SIZE + NEXT_DIRECTORY_SIZE

        for (level in 0 until nestingLevels) {

            /* One ExifOffset entry pointing to the next level. */
            byteWriter.write(shortArrayOf(1).toBytes(ByteOrder.LITTLE_ENDIAN))
            byteWriter.write(byteArrayOf(0x69, 0x87.toByte())) // ExifOffset (0x8769)
            byteWriter.write(byteArrayOf(0x04, 0x00)) // LONG
            byteWriter.writeInt(1, ByteOrder.LITTLE_ENDIAN)
            byteWriter.writeInt(8 + (level + 1) * levelSize, ByteOrder.LITTLE_ENDIAN)
            byteWriter.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next directory
        }

        /* The terminal directory the last level points to. */
        byteWriter.write(shortArrayOf(0).toBytes(ByteOrder.LITTLE_ENDIAN))
        byteWriter.writeInt(0, ByteOrder.LITTLE_ENDIAN)

        assertFailsWith<ImageReadException> {
            TiffReader.read(ByteArrayByteReader(byteWriter.toByteArray()))
        }
    }

    private fun writeTiffHeader(byteWriter: ByteArrayByteWriter) {

        byteWriter.write(byteArrayOf(0x49, 0x49, 0x2A, 0)) // II, version 42
        byteWriter.writeInt(8, ByteOrder.LITTLE_ENDIAN) // IFD0 at offset 8
    }

    private companion object {

        /* Entry count and next directory offset of an empty directory. */
        const val DIRECTORY_SIZE: Int = 6

        const val ENTRY_COUNT_SIZE: Int = 2

        const val ENTRY_SIZE: Int = 12

        const val NEXT_DIRECTORY_SIZE: Int = 4
    }
}
