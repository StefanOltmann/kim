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
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.ModifiedBytesVerifier
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Regression test based on a fixed small set of test files: applies a
 * fixed metadata change to every corpus JPEG and verifies the rewritten
 * bytes against the committed goldens.
 *
 * Each corpus file runs in its own test function, because one test over
 * all files needs more than the default runner timeout on JavaScript -
 * like the API contract fuzz. The three broken corpus files (44, 45, 47)
 * have no function here: the read tests already pin that they fail the
 * read.
 */
class JpegChangeMetadataTest {

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

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    @Test
    fun testChangeMetadataMedia1() =
        changeMetadataOf(1)

    @Test
    fun testChangeMetadataMedia2() =
        changeMetadataOf(2)

    @Test
    fun testChangeMetadataMedia3() =
        changeMetadataOf(3)

    @Test
    fun testChangeMetadataMedia4() =
        changeMetadataOf(4)

    @Test
    fun testChangeMetadataMedia5() =
        changeMetadataOf(5)

    @Test
    fun testChangeMetadataMedia6() =
        changeMetadataOf(6)

    @Test
    fun testChangeMetadataMedia7() =
        changeMetadataOf(7)

    @Test
    fun testChangeMetadataMedia8() =
        changeMetadataOf(8)

    @Test
    fun testChangeMetadataMedia9() =
        changeMetadataOf(9)

    @Test
    fun testChangeMetadataMedia10() =
        changeMetadataOf(10)

    @Test
    fun testChangeMetadataMedia11() =
        changeMetadataOf(11)

    @Test
    fun testChangeMetadataMedia12() =
        changeMetadataOf(12)

    @Test
    fun testChangeMetadataMedia13() =
        changeMetadataOf(13)

    @Test
    fun testChangeMetadataMedia14() =
        changeMetadataOf(14)

    @Test
    fun testChangeMetadataMedia15() =
        changeMetadataOf(15)

    @Test
    fun testChangeMetadataMedia16() =
        changeMetadataOf(16)

    @Test
    fun testChangeMetadataMedia17() =
        changeMetadataOf(17)

    @Test
    fun testChangeMetadataMedia18() =
        changeMetadataOf(18)

    @Test
    fun testChangeMetadataMedia19() =
        changeMetadataOf(19)

    @Test
    fun testChangeMetadataMedia20() =
        changeMetadataOf(20)

    @Test
    fun testChangeMetadataMedia21() =
        changeMetadataOf(21)

    @Test
    fun testChangeMetadataMedia22() =
        changeMetadataOf(22)

    @Test
    fun testChangeMetadataMedia23() =
        changeMetadataOf(23)

    @Test
    fun testChangeMetadataMedia24() =
        changeMetadataOf(24)

    @Test
    fun testChangeMetadataMedia25() =
        changeMetadataOf(25)

    @Test
    fun testChangeMetadataMedia26() =
        changeMetadataOf(26)

    @Test
    fun testChangeMetadataMedia27() =
        changeMetadataOf(27)

    @Test
    fun testChangeMetadataMedia28() =
        changeMetadataOf(28)

    @Test
    fun testChangeMetadataMedia29() =
        changeMetadataOf(29)

    @Test
    fun testChangeMetadataMedia30() =
        changeMetadataOf(30)

    @Test
    fun testChangeMetadataMedia31() =
        changeMetadataOf(31)

    @Test
    fun testChangeMetadataMedia32() =
        changeMetadataOf(32)

    @Test
    fun testChangeMetadataMedia33() =
        changeMetadataOf(33)

    @Test
    fun testChangeMetadataMedia34() =
        changeMetadataOf(34)

    @Test
    fun testChangeMetadataMedia35() =
        changeMetadataOf(35)

    @Test
    fun testChangeMetadataMedia36() =
        changeMetadataOf(36)

    @Test
    fun testChangeMetadataMedia37() =
        changeMetadataOf(37)

    @Test
    fun testChangeMetadataMedia38() =
        changeMetadataOf(38)

    @Test
    fun testChangeMetadataMedia39() =
        changeMetadataOf(39)

    @Test
    fun testChangeMetadataMedia40() =
        changeMetadataOf(40)

    @Test
    fun testChangeMetadataMedia41() =
        changeMetadataOf(41)

    @Test
    fun testChangeMetadataMedia42() =
        changeMetadataOf(42)

    @Test
    fun testChangeMetadataMedia43() =
        changeMetadataOf(43)

    @Test
    fun testChangeMetadataMedia46() =
        changeMetadataOf(46)

    @Test
    fun testChangeMetadataMedia48() =
        changeMetadataOf(48)

    @Test
    fun testChangeMetadataMedia49() =
        changeMetadataOf(49)

    @Test
    fun testChangeMetadataMedia50() =
        changeMetadataOf(50)

    /**
     * Applies a fixed orientation, date, GPS position, IPTC keyword and
     * XMP packet to one corpus file and verifies the rewritten bytes
     * against the committed goldens.
     */
    private fun changeMetadataOf(index: Int) {

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
