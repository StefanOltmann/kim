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
package de.stefan_oltmann.kim.format.tiff.write

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoDouble
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoDoubles
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloat
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloats
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TiffWriterRoundTripTest {

    private val customTagBase = 0xFD00

    private val tagInfoSByte =
        TagInfoSByte(customTagBase, "CustomSByte", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSBytes =
        TagInfoSBytes(customTagBase + 1, "CustomSBytes", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSShort =
        TagInfoSShort(customTagBase + 2, "CustomSShort", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSShorts =
        TagInfoSShorts(customTagBase + 3, "CustomSShorts", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSLong =
        TagInfoSLong(customTagBase + 4, "CustomSLong", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSLongs =
        TagInfoSLongs(customTagBase + 5, "CustomSLongs", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSRational =
        TagInfoSRational(customTagBase + 6, "CustomSRational", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoSRationals =
        TagInfoSRationals(customTagBase + 7, "CustomSRationals", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoFloat =
        TagInfoFloat(customTagBase + 8, "CustomFloat", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoFloats =
        TagInfoFloats(customTagBase + 9, "CustomFloats", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoDouble =
        TagInfoDouble(customTagBase + 10, "CustomDouble", TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    private val tagInfoDoubles =
        TagInfoDoubles(customTagBase + 11, "CustomDoubles", 2, TiffDirectoryType.TIFF_DIRECTORY_IFD0)

    @Test
    fun testWriteAndReadBackAllFieldTypes() {

        val outputSet = TiffOutputSet(ByteOrder.LITTLE_ENDIAN)

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        /* ASCII */
        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION, "Hello World")
        rootDirectory.add(TiffTag.TIFF_TAG_MAKE, "Canon")

        /* Byte */
        rootDirectory.add(TiffTag.TIFF_TAG_DOT_RANGE, 7.toByte())

        /* Bytes */
        rootDirectory.add(TiffTag.TIFF_TAG_DNG_VERSION, byteArrayOf(1, 4, 0, 0))

        /* Short */
        rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, TiffOrientation.ROTATE_RIGHT.value.toShort())

        /* Shorts */
        rootDirectory.add(TiffTag.TIFF_TAG_BITS_PER_SAMPLE, shortArrayOf(8, 8, 8))

        /* Long */
        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_WIDTH, 100)

        /* Longs */
        rootDirectory.add(TiffTag.TIFF_TAG_FREE_OFFSETS, intArrayOf(1, 2, 3))

        /* Rational */
        rootDirectory.add(TiffTag.TIFF_TAG_XRESOLUTION, RationalNumber(72, 1))

        /* Rationals */
        rootDirectory.add(
            TiffTag.TIFF_TAG_WHITE_POINT,
            RationalNumbers(arrayOf(RationalNumber(1, 2), RationalNumber(1, 4)))
        )

        /* Signed types and floating point types with custom tags. */
        rootDirectory.add(tagInfoSByte, (-5).toByte())
        rootDirectory.add(tagInfoSBytes, byteArrayOf(-1, -2))
        rootDirectory.add(tagInfoSShort, (-300).toShort())
        rootDirectory.add(tagInfoSShorts, shortArrayOf(-1, -2))
        rootDirectory.add(tagInfoSLong, -1000)
        rootDirectory.add(tagInfoSLongs, intArrayOf(-1, -2))
        rootDirectory.add(tagInfoSRational, RationalNumber(-1, 2))
        rootDirectory.add(
            tagInfoSRationals,
            RationalNumbers(arrayOf(RationalNumber(-1, 2), RationalNumber(-3, 4)))
        )
        rootDirectory.add(tagInfoFloat, 1.5f)
        rootDirectory.add(tagInfoFloats, floatArrayOf(1.5f, -2.5f))
        rootDirectory.add(tagInfoDouble, 3.14159)
        rootDirectory.add(tagInfoDoubles, doubleArrayOf(1.5, -2.5))

        /* EXIF directory with GPS text and double tag. */
        val exifDirectory = outputSet.getOrCreateExifDirectory()

        exifDirectory.add(ExifTag.EXIF_TAG_USER_COMMENT, "A comment")

        /* GPS directory. */
        val gpsDirectory = outputSet.getOrCreateGPSDirectory()

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_PROCESSING_METHOD, "GPS")
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_VERSION_ID, byteArrayOf(2, 3, 0, 0))

        outputSet.setGpsCoordinates(
            de.stefan_oltmann.kim.model.GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            )
        )

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)

        val writtenBytes = byteWriter.toByteArray()

        /* Read the file back. */
        val tiffContents = TiffReader.read(writtenBytes, readTiffImageBytes = true)

        val readRootDirectory = tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0)
        assertNotNull(readRootDirectory)

        /* Verify the values. */
        assertEquals(
            expected = "Hello World",
            actual = readRootDirectory.findField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)?.value
        )

        assertEquals(
            expected = "Canon",
            actual = readRootDirectory.findField(TiffTag.TIFF_TAG_MAKE)?.value
        )

        assertEquals(
            expected = 7.toByte(),
            actual = (readRootDirectory.findField(TiffTag.TIFF_TAG_DOT_RANGE)?.value as ByteArray).first()
        )

        assertTrue(
            byteArrayOf(1, 4, 0, 0).contentEquals(
                readRootDirectory.findField(TiffTag.TIFF_TAG_DNG_VERSION)?.value as ByteArray
            )
        )

        assertEquals(
            expected = TiffOrientation.ROTATE_RIGHT.value.toShort(),
            actual = (readRootDirectory.findField(TiffTag.TIFF_TAG_ORIENTATION)?.value as ShortArray).first()
        )

        assertEquals(
            expected = 100,
            actual = readRootDirectory.getFieldValue(TiffTag.TIFF_TAG_IMAGE_WIDTH)
        )

        val bitsPerSampleField = readRootDirectory.findField(TiffTag.TIFF_TAG_BITS_PER_SAMPLE)

        assertEquals(
            expected = shortArrayOf(8, 8, 8).contentToString(),
            actual = (bitsPerSampleField?.value as ShortArray).contentToString()
        )

        assertEquals(
            expected = 72.0,
            actual = readRootDirectory.findField(TiffTag.TIFF_TAG_XRESOLUTION)?.toDouble()
        )

        /* Custom signed and floating point types. */
        assertTrue(
            byteArrayOf(-5).contentEquals(
                checkNotNull(readRootDirectory.findField(tagInfoSByte)).value as ByteArray
            )
        )
        assertTrue(
            byteArrayOf(-1, -2).contentEquals(
                checkNotNull(readRootDirectory.findField(tagInfoSBytes)).value as ByteArray
            )
        )
        assertEquals(
            expected = -300,
            actual = (checkNotNull(readRootDirectory.findField(tagInfoSShort)).value as ShortArray).first().toInt()
        )
        assertEquals(
            expected = -1000,
            actual = (checkNotNull(readRootDirectory.findField(tagInfoSLong)).value as IntArray).first()
        )
        assertEquals(
            expected = -0.5,
            actual = checkNotNull(readRootDirectory.findField(tagInfoSRational)).toDouble()
        )
        assertEquals(
            expected = 1.5f,
            actual = (checkNotNull(readRootDirectory.findField(tagInfoFloat)).value as FloatArray).first()
        )
        assertEquals(
            expected = 3.14159,
            actual = (checkNotNull(readRootDirectory.findField(tagInfoDouble)).value as DoubleArray).first()
        )

        /* EXIF directory. */
        val readExifDirectory = tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_EXIF)
        assertNotNull(readExifDirectory)

        assertEquals(
            expected = "A comment",
            actual = readExifDirectory.findField(ExifTag.EXIF_TAG_USER_COMMENT)?.value
        )

        /* GPS directory. */
        val readGpsDirectory = tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)
        assertNotNull(readGpsDirectory)

        assertEquals(
            expected = "GPS",
            actual = readGpsDirectory.findField(GpsTag.GPS_TAG_GPS_PROCESSING_METHOD)?.value
        )

        assertTrue(
            byteArrayOf(2, 3, 0, 0).contentEquals(
                checkNotNull(readGpsDirectory.findField(GpsTag.GPS_TAG_GPS_VERSION_ID)).value as ByteArray
            )
        )
    }

    @Test
    fun testWriteAndReadBackThumbnailAndTiffImage() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_WIDTH, 10)
        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_HEIGHT, 10)

        /* TIFF image data (strips). */
        rootDirectory.setTiffImageBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))

        /* Thumbnail (JPEG) data. */
        val thumbnailBytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0, 0, 0, 0
        )

        outputSet.setThumbnailBytes(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)

        val writtenBytes = byteWriter.toByteArray()

        val tiffContents = TiffReader.read(writtenBytes, readTiffImageBytes = true)

        /* Thumbnail bytes. */
        val readThumbnailBytes = tiffContents.getExifThumbnailBytes()
        assertNotNull(readThumbnailBytes)
        assertTrue(thumbnailBytes.contentEquals(readThumbnailBytes))

        /* TIFF image data. */
        val readRootDirectory = tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0)
        assertNotNull(readRootDirectory)
        assertNotNull(readRootDirectory.tiffImageBytes)
        assertTrue(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8).contentEquals(readRootDirectory.tiffImageBytes))
    }

    /**
     * When the thumbnail bytes could not be captured (the reader rejects
     * garbage thumbnails), the offset/length pair must not survive the
     * rewrite as a dangling length without its offset.
     */
    @Test
    fun testRejectedThumbnailDropsDanglingLengthField() {

        val offsetField = TiffField(
            offset = 0,
            tag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD1,
            fieldType = FieldTypeLong,
            count = 1,
            localValue = 100,
            valueOffset = null,
            valueBytes = byteArrayOf(100, 0, 0, 0),
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

        val lengthField = TiffField(
            offset = 0,
            tag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD1,
            fieldType = FieldTypeLong,
            count = 1,
            localValue = 16,
            valueOffset = null,
            valueBytes = byteArrayOf(0, 0, 0, 16),
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 1
        )

        val ifd1 = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD1,
            entries = listOf(offsetField, lengthField),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val ifd0 = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
            entries = emptyList(),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val tiffContents = TiffContents(
            header = TiffHeader(ByteOrder.BIG_ENDIAN, 42, 8),
            directories = listOf(ifd0, ifd1),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )

        val outputSet = tiffContents.createOutputSet()

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)

        val readBack = TiffReader.read(byteWriter.toByteArray())

        val writtenIfd1 = readBack.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD1)

        /* Both fields must go: a length without its offset is invalid. */
        assertNull(writtenIfd1?.findField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH))
    }

    @Test
    fun testWriteBigEndian() {

        val outputSet = TiffOutputSet(ByteOrder.BIG_ENDIAN)

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_WIDTH, 12345)
        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION, "Big Endian")

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(ByteOrder.BIG_ENDIAN).write(byteWriter, outputSet)

        val writtenBytes = byteWriter.toByteArray()

        /* Byte order bytes. */
        assertEquals('M'.code, writtenBytes[0].toInt())
        assertEquals('M'.code, writtenBytes[1].toInt())

        val tiffContents = TiffReader.read(writtenBytes)

        val readRootDirectory = tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0)
        assertNotNull(readRootDirectory)

        assertEquals(
            expected = 12345,
            actual = readRootDirectory.getFieldValue(TiffTag.TIFF_TAG_IMAGE_WIDTH)
        )
    }

    @Test
    fun testThumbnailIsWrittenBehindAddedSubDirectories() {

        val outputSet = TiffOutputSet()

        outputSet.addRootDirectory()
        outputSet.addExifDirectory()

        outputSet.setThumbnailBytes(ByteArray(64))

        /* The GPS directory is added after the thumbnail, but must still be written before it. */
        outputSet.addGPSDirectory()

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)

        val contents = TiffReader.read(byteWriter.toByteArray())

        val gpsDirectory = contents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)
        val thumbnailDirectory = contents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD1)

        assertNotNull(gpsDirectory)
        assertNotNull(thumbnailDirectory)

        assertTrue(
            gpsDirectory.offset < thumbnailDirectory.offset,
            "The GPS directory must be written before the thumbnail directory."
        )
    }

    /**
     * Fields whose value the caller did not change must be copied byte
     * exact on a rewrite. Decoding and re-encoding text fields is lossy:
     * Latin-1 bytes become mojibake, multi-string values are truncated
     * at the NUL and GPS text encoding prefixes are replaced.
     */
    @Test
    fun testUnchangedTextFieldsAreCopiedByteExact() {

        /* Artist "Rä" in Latin-1 with terminator. */
        val artistBytes = byteArrayOf(0x52, 0xE4.toByte(), 0x00)

        /* UserComment with a UNICODE encoding prefix, as cameras write it. */
        val userCommentBytes =
            "UNICODE ".encodeToByteArray() + byteArrayOf(0x3E, 0x00, 0x2C, 0x00)

        val artistField = TiffField(
            offset = 0,
            tag = TiffTag.TIFF_TAG_ARTIST.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
            fieldType = FieldTypeAscii,
            count = artistBytes.size,
            localValue = null,
            valueOffset = 0,
            valueBytes = artistBytes,
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

        val userCommentField = TiffField(
            offset = 0,
            tag = ExifTag.EXIF_TAG_USER_COMMENT.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_EXIF,
            fieldType = FieldTypeUndefined,
            count = userCommentBytes.size,
            localValue = null,
            valueOffset = 0,
            valueBytes = userCommentBytes,
            byteOrder = ByteOrder.BIG_ENDIAN,
            sortHint = 0
        )

        val ifd0 = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0,
            entries = listOf(artistField),
            offset = 8,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val exifIfd = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_EXIF,
            entries = listOf(userCommentField),
            offset = 100,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.BIG_ENDIAN
        )

        val tiffContents = TiffContents(
            header = TiffHeader(ByteOrder.BIG_ENDIAN, 42, 8),
            directories = listOf(ifd0, exifIfd),
            makerNoteDirectory = null,
            makerNoteSubDirectories = emptyList(),
            geoTiffDirectory = null
        )

        val outputSet = tiffContents.createOutputSet()

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)

        val readBack = TiffReader.read(byteWriter.toByteArray())

        assertEquals(
            expected = artistBytes.toList(),
            actual = readBack.directories
                .first()
                .findField(TiffTag.TIFF_TAG_ARTIST)
                ?.valueBytes
                ?.toList()
        )

        assertEquals(
            expected = userCommentBytes.toList(),
            actual = readBack.directories
                .first { it.type == TiffConstants.TIFF_DIRECTORY_EXIF }
                .findField(ExifTag.EXIF_TAG_USER_COMMENT)
                ?.valueBytes
                ?.toList()
        )
    }

    @Test
    fun testCreateOutputSetFromExistingFile() {

        val bytes = Resource("de/stefan_oltmann/kim/updates_tif/empty.tif").readBytes()

        val tiffContents = TiffReader.read(bytes)

        val outputSet = tiffContents.createOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION, "Modified")

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)

        val writtenBytes = byteWriter.toByteArray()

        val readBack = TiffReader.read(writtenBytes)

        val readRootDirectory = readBack.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0)
        assertNotNull(readRootDirectory)

        assertEquals(
            expected = "Modified",
            actual = readRootDirectory.findField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)?.value
        )
    }

    @Test
    fun testApplyUpdate() {

        val outputSet = TiffOutputSet()

        /* Orientation. */
        outputSet.applyUpdate(MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT))
        assertNotNull(
            outputSet.getOrCreateRootDirectory()
                .findField(TiffTag.TIFF_TAG_ORIENTATION)
        )

        /* TakenDate. */
        outputSet.applyUpdate(MetadataUpdate.TakenDate(0L))

        /* Remove the date again. */
        outputSet.applyUpdate(MetadataUpdate.TakenDate(null))
        assertNull(
            outputSet.getOrCreateExifDirectory().findField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
        )

        /* Description. */
        outputSet.applyUpdate(MetadataUpdate.Description("New Description"))
        assertNotNull(
            outputSet.getOrCreateRootDirectory().findField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)
        )

        /* Remove the description again. */
        outputSet.applyUpdate(MetadataUpdate.Description(null))
        assertNull(
            outputSet.getOrCreateRootDirectory().findField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)
        )

        /* GPS coordinates and combined update. */
        outputSet.applyUpdate(
            MetadataUpdate.GpsCoordinates(
                de.stefan_oltmann.kim.model.GpsCoordinates(
                    latitude = 10.0,
                    longitude = 20.0
                )
            )
        )

        outputSet.applyUpdate(
            MetadataUpdate.GpsCoordinatesAndLocationShown(
                gpsCoordinates = null,
                locationShown = null
            )
        )
    }

    @Test
    fun testApplyUpdateRejectsUnsupportedUpdate() {

        val outputSet = TiffOutputSet()

        assertFailsWith<ImageWriteException> {
            outputSet.applyUpdate(MetadataUpdate.LocationShown(null))
        }
    }

    @Test
    fun testApplyUpdatesAppliesOnlySupportedUpdates() {

        val outputSet = TiffOutputSet()

        val appliedAny = outputSet.applyUpdates(
            setOf(
                MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT),
                MetadataUpdate.Description("New Description"),
                MetadataUpdate.Title("Title")
            )
        )

        assertTrue(appliedAny)

        assertNotNull(
            outputSet.getOrCreateRootDirectory()
                .findField(TiffTag.TIFF_TAG_ORIENTATION)
        )

        assertNotNull(
            outputSet.getOrCreateRootDirectory()
                .findField(TiffTag.TIFF_TAG_IMAGE_DESCRIPTION)
        )
    }

    @Test
    fun testApplyUpdatesWithUnsupportedUpdatesOnly() {

        val outputSet = TiffOutputSet()

        val appliedAny = outputSet.applyUpdates(
            setOf(
                MetadataUpdate.Title("Title"),
                MetadataUpdate.Flagged(true)
            )
        )

        assertFalse(appliedAny)
        assertTrue(outputSet.getDirectories().isEmpty())
    }

    @Test
    fun testAddDirectoryRejectsDuplicates() {

        val outputSet = TiffOutputSet()

        outputSet.addRootDirectory()

        assertFailsWith<ImageWriteException> {
            outputSet.addRootDirectory()
        }
    }

    @Test
    fun testWriterRejectsEmptyOutputSet() {

        val outputSet = TiffOutputSet()

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)
        }
    }

    @Test
    fun testWriterRejectsMissingRootDirectory() {

        val outputSet = TiffOutputSet()

        outputSet.addExifDirectory()

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            TiffWriter(ByteOrder.LITTLE_ENDIAN).write(byteWriter, outputSet)
        }
    }

    @Test
    fun testInteroperabilityDirectoryWithoutExifGetsCreated() {

        val outputSet = TiffOutputSet()

        /* The writer requires the root directory. */
        outputSet.getOrCreateRootDirectory()

        outputSet.addDirectory(TiffOutputDirectory(TiffConstants.TIFF_DIRECTORY_INTEROP, outputSet.byteOrder))

        val byteWriter = ByteArrayByteWriter()

        TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)

        val writtenBytes = byteWriter.toByteArray()

        val tiffContents = TiffReader.read(writtenBytes)

        assertNotNull(tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_EXIF))
        assertNotNull(tiffContents.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_INTEROP))
    }
}
