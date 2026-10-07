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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.tiff.GPSInfo
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GpsTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.output.BufferByteWriter
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class TiffEdgeCasesTest {

    /**
     * The tags the writer validates, rewrites or removes on its own -
     * the IFD pointers against real directories, the MakerNote by
     * anchoring, and the tile and thumbnail fields by removal. None of
     * that is the concern of the entry count tests.
     */
    private val pointerTags =
        setOf(
            ExifTag.EXIF_TAG_EXIF_OFFSET.tag,
            ExifTag.EXIF_TAG_GPSINFO.tag,
            ExifTag.EXIF_TAG_INTEROP_OFFSET.tag,
            ExifTag.EXIF_TAG_MAKER_NOTE.tag,
            TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag,
            TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH.tag,
            TiffTag.TIFF_TAG_TILE_OFFSETS.tag,
            TiffTag.TIFF_TAG_TILE_BYTE_COUNTS.tag,
            TiffTag.TIFF_TAG_TILE_WIDTH.tag,
            TiffTag.TIFF_TAG_TILE_LENGTH.tag
        )

    private fun write(outputSet: TiffOutputSet) {
        TiffWriter(ByteOrder.LITTLE_ENDIAN).write(
            ByteArrayByteWriter(),
            outputSet
        )
    }

    @Test
    fun testWriterRejectsUnknownDirectoryType() {

        val outputSet = TiffOutputSet()

        outputSet.addDirectory(TiffOutputDirectory(-5, outputSet.byteOrder))

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    /**
     * The classic TIFF directory declares its entry count in 16 bits.
     * Writing the count with the low bits only would serialize all
     * entries behind a count of 0 - every consumer would then read the
     * entries as the next-IFD pointer. The write fails instead.
     */
    @Test
    fun testDirectoryEntryCountOverflowFailsTheWrite() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        /*
         * The pointer tags are validated against real directories by
         * the writer, which is not the concern here. Tags beyond the
         * 16-bit range stand in for the excluded ones, so the directory
         * ends up one field beyond the representable count.
         */
        for (tag in 0..0xFFFF + pointerTags.size) {

            if (tag in pointerTags)
                continue

            rootDirectory.add(TiffOutputField(tag, FieldTypeByte, 1, byteArrayOf(tag.toByte())))
        }

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    /**
     * A directory with exactly the maximum entry count is the legal
     * boundary and must still write.
     */
    @Test
    fun testMaximumDirectoryEntryCountWrites() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        for (tag in 0 until 0xFFFF) {

            if (tag in pointerTags)
                continue

            rootDirectory.add(TiffOutputField(tag, FieldTypeByte, 1, byteArrayOf(tag.toByte())))
        }

        write(outputSet)
    }

    @Test
    fun testWriterRejectsDuplicateTagsInDirectory() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        val bytes = ByteArray(4)

        rootDirectory.add(TiffOutputField(TiffTag.TIFF_TAG_IMAGE_WIDTH.tag, FieldTypeLong, 1, bytes))
        rootDirectory.add(TiffOutputField(TiffTag.TIFF_TAG_IMAGE_WIDTH.tag, FieldTypeLong, 1, bytes))

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    @Test
    fun testWriterRejectsExifOffsetFieldWithoutExifDirectory() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            TiffOutputField(
                ExifTag.EXIF_TAG_EXIF_OFFSET.tag,
                FieldTypeLong,
                1,
                FieldTypeLong.writeData(0, outputSet.byteOrder)
            )
        )

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    @Test
    fun testWriterRejectsGpsOffsetFieldWithoutGpsDirectory() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            TiffOutputField(
                ExifTag.EXIF_TAG_GPSINFO.tag,
                FieldTypeLong,
                1,
                FieldTypeLong.writeData(0, outputSet.byteOrder)
            )
        )

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    @Test
    fun testWriterRejectsInteropOffsetFieldWithoutInteropDirectory() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            TiffOutputField(
                ExifTag.EXIF_TAG_INTEROP_OFFSET.tag,
                FieldTypeLong,
                1,
                FieldTypeLong.writeData(0, outputSet.byteOrder)
            )
        )

        assertFailsWith<ImageWriteException> {
            write(outputSet)
        }
    }

    @Test
    fun testTiffOutputValue() {

        val value = TiffOutputValue("testValue", byteArrayOf(1, 2, 3, 4))

        assertEquals(4, value.getItemLength())

        assertEquals(
            expected = "testValue = 01020304",
            actual = value.toString()
        )

        /* Updating with the same size works. */
        value.updateValue(byteArrayOf(9, 8, 7, 6))

        assertFailsWith<ImageWriteException> {
            value.updateValue(byteArrayOf(1, 2))
        }
    }

    @Test
    fun testBufferByteWriterOverflow() {

        val buffer = ByteArray(2)

        val writer = BufferByteWriter(buffer, 0)

        writer.write(1.toByte())
        writer.write(2.toByte())

        /* Both writes overflow. */
        assertFailsWith<ImageWriteException> {
            writer.write(3.toByte())
        }

        val writer2 = BufferByteWriter(ByteArray(2), 0)

        writer2.write(byteArrayOf(1))

        assertFailsWith<ImageWriteException> {
            writer2.write(byteArrayOf(2, 3))
        }

        /* flush and close are no-ops. */
        writer2.flush()
        writer2.close()
    }

    @Test
    fun testGpsInfoCreationAndConversion() {

        val gpsDirectory = createGpsDirectory(
            latitudeRef = "N",
            longitudeRef = "E",
            latitudeValues = arrayOf(RationalNumber(53, 1), RationalNumber(13, 1), RationalNumber(9, 1)),
            longitudeValues = arrayOf(RationalNumber(8, 1), RationalNumber(14, 1), RationalNumber(21, 1))
        )

        val gpsInfo = checkNotNull(GPSInfo.createFrom(gpsDirectory))

        assertEquals(
            expected = 53 + 13.0 / 60 + 9.0 / 3600,
            actual = gpsInfo.getLatitudeAsDegreesNorth()
        )
        assertEquals(
            expected = 8 + 14.0 / 60 + 21.0 / 3600,
            actual = gpsInfo.getLongitudeAsDegreesEast()
        )
    }

    @Test
    fun testGpsInfoNegativeHemispheres() {

        val gpsDirectory = createGpsDirectory(
            latitudeRef = "S",
            longitudeRef = "W",
            latitudeValues = arrayOf(RationalNumber(10, 1), RationalNumber(0, 1), RationalNumber(0, 1)),
            longitudeValues = arrayOf(RationalNumber(20, 1), RationalNumber(0, 1), RationalNumber(0, 1))
        )

        val gpsInfo = checkNotNull(GPSInfo.createFrom(gpsDirectory))

        assertEquals(-10.0, gpsInfo.getLatitudeAsDegreesNorth())
        assertEquals(-20.0, gpsInfo.getLongitudeAsDegreesEast())
    }

    @Test
    fun testGpsInfoRejectsUnknownRefs() {

        val gpsDirectory = createGpsDirectory(
            latitudeRef = "X",
            longitudeRef = "E",
            latitudeValues = arrayOf(RationalNumber(10, 1), RationalNumber(0, 1), RationalNumber(0, 1)),
            longitudeValues = arrayOf(RationalNumber(20, 1), RationalNumber(0, 1), RationalNumber(0, 1))
        )

        val gpsInfo = checkNotNull(GPSInfo.createFrom(gpsDirectory))

        assertFailsWith<ImageReadException> {
            gpsInfo.getLatitudeAsDegreesNorth()
        }

        val gpsInfo2 = checkNotNull(
            GPSInfo.createFrom(
                createGpsDirectory(
                    latitudeRef = "N",
                    longitudeRef = "X",
                    latitudeValues = arrayOf(RationalNumber(10, 1), RationalNumber(0, 1), RationalNumber(0, 1)),
                    longitudeValues = arrayOf(RationalNumber(20, 1), RationalNumber(0, 1), RationalNumber(0, 1))
                )
            )
        )

        assertFailsWith<ImageReadException> {
            gpsInfo2.getLongitudeAsDegreesEast()
        }
    }

    @Test
    fun testGpsInfoRejectsMissingFields() {

        /* Missing latitude ref. */
        val missingRefs = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_GPS,
            entries = emptyList(),
            offset = 0,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN
        )

        assertEquals(null, GPSInfo.createFrom(missingRefs))

        /* Empty refs are ignored. */
        val emptyRefs = createGpsDirectory(
            latitudeRef = "",
            longitudeRef = "",
            latitudeValues = arrayOf(RationalNumber(10, 1), RationalNumber(0, 1), RationalNumber(0, 1)),
            longitudeValues = arrayOf(RationalNumber(20, 1), RationalNumber(0, 1), RationalNumber(0, 1))
        )

        assertEquals(null, GPSInfo.createFrom(emptyRefs))
    }

    @Test
    fun testGpsInfoRejectsWrongValueCount() {

        val gpsDirectory = createGpsDirectory(
            latitudeRef = "N",
            longitudeRef = "E",
            latitudeValues = arrayOf(RationalNumber(10, 1), RationalNumber(0, 1)),
            longitudeValues = arrayOf(RationalNumber(20, 1), RationalNumber(0, 1))
        )

        assertFailsWith<ImageReadException> {
            GPSInfo.createFrom(gpsDirectory)
        }
    }

    /**
     * Regression test: hostile files can store the coordinate tags with a
     * non-rational type. That must degrade to unknown GPS instead of
     * throwing a ClassCastException from the unchecked cast.
     */
    @Test
    fun testGpsInfoIgnoresNonRationalCoordinates() {

        val byteOrder = ByteOrder.LITTLE_ENDIAN

        fun field(tag: TagInfo, fieldType: FieldType<out Any>, count: Int, bytes: ByteArray) =
            TiffField(
                offset = 0,
                tag = tag.tag,
                directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
                fieldType = fieldType,
                count = count,
                localValue = null,
                valueOffset = 0,
                valueBytes = bytes,
                byteOrder = byteOrder,
                sortHint = 0
            )

        val gpsDirectory = TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_GPS,
            entries = listOf(
                field(GpsTag.GPS_TAG_GPS_LATITUDE_REF, FieldTypeAscii, 2, "N\u0000".encodeToByteArray()),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, FieldTypeAscii, 2, "E\u0000".encodeToByteArray()),

                /* LONG instead of RATIONAL - the corruption case. */
                field(GpsTag.GPS_TAG_GPS_LATITUDE, FieldTypeLong, 3, intArrayOf(1, 2, 3).toBytes(byteOrder)),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE, FieldTypeLong, 3, intArrayOf(4, 5, 6).toBytes(byteOrder))
            ),
            offset = 0,
            nextDirectoryOffset = 0,
            byteOrder = byteOrder
        )

        assertEquals(null, GPSInfo.createFrom(gpsDirectory))
    }

    private fun createGpsDirectory(
        latitudeRef: String,
        longitudeRef: String,
        latitudeValues: Array<RationalNumber>,
        longitudeValues: Array<RationalNumber>
    ): TiffDirectory {

        val byteOrder = ByteOrder.LITTLE_ENDIAN

        fun field(
            tag: TagInfo,
            fieldType: FieldType<out Any>,
            count: Int,
            bytes: ByteArray
        ): TiffField = TiffField(
            offset = 0,
            tag = tag.tag,
            directoryType = TiffConstants.TIFF_DIRECTORY_GPS,
            fieldType = fieldType,
            count = count,
            localValue = null,
            valueOffset = 0,
            valueBytes = bytes,
            byteOrder = byteOrder,
            sortHint = 0
        )

        val latitudeBytes = latitudeValues
            .fold(ByteArray(0)) { acc, rational -> acc + rational.toBytes(byteOrder) }

        val longitudeBytes = longitudeValues
            .fold(ByteArray(0)) { acc, rational -> acc + rational.toBytes(byteOrder) }

        return TiffDirectory(
            type = TiffConstants.TIFF_DIRECTORY_GPS,
            entries = listOf(
                field(
                    GpsTag.GPS_TAG_GPS_LATITUDE_REF,
                    FieldTypeAscii,
                    latitudeRef.length + 1,
                    "$latitudeRef\u0000".encodeToByteArray()
                ),
                field(
                    GpsTag.GPS_TAG_GPS_LONGITUDE_REF,
                    FieldTypeAscii,
                    longitudeRef.length + 1,
                    "$longitudeRef\u0000".encodeToByteArray()
                ),
                field(GpsTag.GPS_TAG_GPS_LATITUDE, FieldTypeRational, latitudeValues.size, latitudeBytes),
                field(GpsTag.GPS_TAG_GPS_LONGITUDE, FieldTypeRational, longitudeValues.size, longitudeBytes)
            ),
            offset = 0,
            nextDirectoryOffset = 0,
            byteOrder = byteOrder
        )
    }
}
