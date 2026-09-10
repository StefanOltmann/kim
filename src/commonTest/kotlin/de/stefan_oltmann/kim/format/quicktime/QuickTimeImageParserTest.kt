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
package de.stefan_oltmann.kim.format.quicktime

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertToSummary
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmTag
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import de.stefan_oltmann.kim.testdata.QuickTimeTestVideos
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests reading the metadata of QuickTime MOV and MP4 videos.
 *
 * Both test videos are recorded with a Fujifilm X-T4, which embeds the
 * complete camera EXIF including the MakerNote and a thumbnail as a TIFF
 * structure inside the "MVTG" box of the "moov" user data.
 */
class QuickTimeImageParserTest {

    private val xmpPacketBytes: ByteArray
        get() = QuickTimeTestVideos.xmpPacket.encodeToByteArray()

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    @Test
    fun testReadExifFromMp4() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MP4_TEST_VIDEO_INDEX)
        )

        assertNotNull(metadata)

        assertEquals(MediaFormat.MP4, metadata.mediaFormat)

        assertEquals("FUJIFILM", metadata.findStringValue(TiffTag.TIFF_TAG_MAKE))

        assertEquals("X-T4", metadata.findStringValue(TiffTag.TIFF_TAG_MODEL))

        assertEquals(
            expected = "2026:04:15 12:42:33",
            actual = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
        )

        assertEquals(640, metadata.findTiffField(ExifTag.EXIF_TAG_ISO)?.toInt())

        assertEquals(1.0 / 120.0, metadata.findDoubleValue(ExifTag.EXIF_TAG_EXPOSURE_TIME))

        assertEquals(3.6, metadata.findDoubleValue(ExifTag.EXIF_TAG_FNUMBER))

        val makerNoteDirectory = metadata.exif?.makerNoteDirectory

        assertNotNull(makerNoteDirectory)

        assertNotNull(makerNoteDirectory.findField(FujiFilmTag.FILM_MODE))

        assertEquals(970, metadata.getExifThumbnailBytes()?.size)
    }

    @Test
    fun testReadExifFromMov() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MOV_TEST_VIDEO_INDEX)
        )

        assertNotNull(metadata)

        assertEquals(MediaFormat.MOV, metadata.mediaFormat)

        assertEquals("FUJIFILM", metadata.findStringValue(TiffTag.TIFF_TAG_MAKE))

        assertEquals("X-T4", metadata.findStringValue(TiffTag.TIFF_TAG_MODEL))

        assertEquals(
            expected = "2026:04:15 12:43:42",
            actual = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
        )

        assertEquals(640, metadata.findTiffField(ExifTag.EXIF_TAG_ISO)?.toInt())

        assertEquals(1.0 / 120.0, metadata.findDoubleValue(ExifTag.EXIF_TAG_EXPOSURE_TIME))

        assertEquals(3.6, metadata.findDoubleValue(ExifTag.EXIF_TAG_FNUMBER))

        val makerNoteDirectory = metadata.exif?.makerNoteDirectory

        assertNotNull(makerNoteDirectory)

        assertEquals(970, metadata.getExifThumbnailBytes()?.size)
    }

    /**
     * The raw MVTG payload is exposed as the EXIF bytes, so the original
     * metadata bytes survive byte-exact for consumers and later writes.
     */
    @Test
    fun testExifBytesAreTheRawMvtgPayload() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MP4_TEST_VIDEO_INDEX)
        )

        assertNotNull(metadata)

        assertEquals(2948, metadata.exifBytes?.size)

        assertTrue(metadata.exifBytes?.decodeToString()?.contains("FUJIFILM") == true)
    }

    /**
     * The image size is the display size of the video track from its track
     * header, not the size of an embedded thumbnail.
     */
    @Test
    fun testImageSizeFromMp4VideoTrack() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MP4_TEST_VIDEO_INDEX)
        )

        assertEquals(ImageSize(1920, 1080), metadata?.imageSize)
    }

    @Test
    fun testImageSizeFromMovVideoTrack() {

        val metadata = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MOV_TEST_VIDEO_INDEX)
        )

        assertEquals(ImageSize(3840, 2160), metadata?.imageSize)
    }

    @Test
    fun testSummaryOfMp4() {

        val summary = Kim.readMetadata(
            KimTestData.getBytesOf(KimTestData.MP4_TEST_VIDEO_INDEX)
        )?.convertToSummary()

        assertNotNull(summary)

        assertEquals(1776249753000L, summary.takenDate)

        assertEquals(640, summary.iso)

        assertEquals(1.0 / 120.0, summary.exposureTime)

        assertEquals(3.6, summary.fNumber)

        assertEquals(ImageSize(160, 120), summary.thumbnailImageSize)

        assertEquals(970, summary.thumbnailBytes?.size)
    }

    /**
     * XMP packets of videos are stored in a UUID box with the Adobe XMP
     * UUID inside the moov box.
     */
    @Test
    fun testReadXmpFromUuidBox() {

        val metadata = Kim.readMetadata(
            QuickTimeTestVideos.movWithXmpInMoov(xmpPacketBytes)
        )

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * The XMP packet drives title, keywords and rating of the summary, so
     * rating and keywords can be applied to videos through one XMP packet.
     */
    @Test
    fun testSummaryReadsXmpFromUuidBox() {

        val summary = Kim.readMetadata(
            QuickTimeTestVideos.movWithXmpInMoov(xmpPacketBytes)
        )?.convertToSummary()

        assertNotNull(summary)

        assertEquals("Video title", summary.title)

        assertEquals(setOf("holiday", "beach"), summary.keywords)

        assertEquals(4, summary.rating?.value)
    }

    /**
     * A UUID box that claims to hold XMP but carries no packet is corrupt.
     * It must fail the read instead of reaching sidecar writers as broken
     * bytes, mirroring the CR3 read behavior.
     */
    @Test
    fun testRejectsBrokenXmpInUuidBox() {

        val bytes = QuickTimeTestVideos.movWithXmpInMoov(
            "no xmp packet here".encodeToByteArray()
        )

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }
    }

    /**
     * ExifTool and Adobe write the XMP packet of MOV videos into an "XMP_"
     * box inside the user data. Such packets must be read like the UUID
     * box variants, because dropping them silently would lose them for
     * sidecar writers.
     */
    @Test
    fun testReadsXmpFromXmpUnderscoreBox() {

        val metadata = Kim.readMetadata(
            QuickTimeTestVideos.movWithXmpUnderscoreInUdta(xmpPacketBytes)
        )

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * Some muxers place the XMP UUID box at the top level instead of inside
     * the moov box. Such metadata must be found like any other, because
     * dropping it silently would lose it for sidecar writers.
     */
    @Test
    fun testReadsXmpFromTopLevelUuidBox() {

        val metadata = Kim.readMetadata(
            QuickTimeTestVideos.movWithXmpAtTopLevel(xmpPacketBytes)
        )

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * A container without a moov box has no place where metadata could
     * live and must fail the read instead of reporting empty metadata.
     */
    @Test
    fun testRejectsFileWithoutMoovBox() {

        val bytes = QuickTimeTestVideos.ftypBox() + QuickTimeTestVideos.mdatBox()

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }
    }

    /**
     * Finalized files often place the media data in front of the moov box.
     * The scan must stream past the media data and find the metadata
     * behind it.
     */
    @Test
    fun testReadsMetadataWithMdatBeforeMoov() {

        val uuidBox = QuickTimeTestVideos.box(
            type = "uuid",
            payload = QuickTimeTestVideos.xmpUuidPayload(xmpPacketBytes)
        )

        val moovBox = QuickTimeTestVideos.box(type = "moov", payload = uuidBox)

        val bytes =
            QuickTimeTestVideos.ftypBox() +
                QuickTimeTestVideos.mdatBox() +
                moovBox

        val metadata = Kim.readMetadata(bytes)

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * An interrupted recording ends inside the media data while its box
     * header still declares the full size. The metadata in front of the
     * media data must survive such a read.
     */
    @Test
    fun testReadsMetadataFromInterruptedRecording() {

        val metadata = Kim.readMetadata(
            QuickTimeTestVideos.truncatedVideoWithXmpInMoov(xmpPacketBytes)
        )

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * A media data box with the size field 0 extends to the end of the
     * file per spec. The scan must stop at the exact end of the file and
     * still report the metadata in front of it.
     */
    @Test
    fun testReadsMetadataWithMdatExtendingToFileEnd() {

        val metadata = Kim.readMetadata(
            QuickTimeTestVideos.videoWithMdatExtendingToFileEnd(xmpPacketBytes)
        )

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * Legacy QuickTime muxers pad the file with an 8-byte "wide" box in
     * front of the media data. The padding must not disturb the scan.
     */
    @Test
    fun testReadsMetadataWithLegacyWideBox() {

        val uuidBox = QuickTimeTestVideos.box(
            type = "uuid",
            payload = QuickTimeTestVideos.xmpUuidPayload(xmpPacketBytes)
        )

        val moovBox = QuickTimeTestVideos.box(type = "moov", payload = uuidBox)

        val wideBox = QuickTimeTestVideos.box(type = "wide", payload = ByteArray(0))

        val bytes =
            QuickTimeTestVideos.ftypBox() +
                moovBox +
                wideBox +
                QuickTimeTestVideos.mdatBox()

        val metadata = Kim.readMetadata(bytes)

        assertEquals(QuickTimeTestVideos.xmpPacket, metadata?.xmp)
    }

    /**
     * The image size comes from the video track only. A container with a
     * sound track reports no size instead of the sound track header
     * values.
     */
    @Test
    fun testImageSizeIsNullWithoutVideoTrack() {

        val trakBox = QuickTimeTestVideos.trakBox(handlerType = "soun")

        val moovBox = QuickTimeTestVideos.box(type = "moov", payload = trakBox)

        val bytes = QuickTimeTestVideos.ftypBox() + moovBox

        val metadata = Kim.readMetadata(bytes)

        assertNotNull(metadata)

        assertNull(metadata.imageSize)
    }

    /**
     * Cameras that record the sound track in front of the video track
     * must still get the size of the video track, selected by its handler
     * type.
     */
    @Test
    fun testReadsSizeFromVideoTrackBehindSoundTrack() {

        val soundTrackBox = QuickTimeTestVideos.trakBox(handlerType = "soun")

        val videoTrackBox = QuickTimeTestVideos.trakBox(
            handlerType = "vide",
            tkhdPayload = QuickTimeTestVideos.tkhdPayload(widthPx = 1920, heightPx = 1080)
        )

        val moovBox = QuickTimeTestVideos.box(
            type = "moov",
            payload = soundTrackBox + videoTrackBox
        )

        val bytes = QuickTimeTestVideos.ftypBox() + moovBox

        val metadata = Kim.readMetadata(bytes)

        assertEquals(ImageSize(1920, 1080), metadata?.imageSize)
    }

    /**
     * An MVTG payload that is too short to hold the Fujifilm header, the
     * endian marker and an IFD is a corrupt structure and must fail the
     * read per the strict read policy.
     */
    @Test
    fun testRejectsTooShortMvtgPayload() {

        val bytes = QuickTimeTestVideos.videoWithMvtgPayload(byteArrayOf(0, 0, 0, 0))

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }
    }

    /**
     * Only the little-endian marker written by Fujifilm is known. Anything
     * else is an unknown structure and must fail the read instead of being
     * misinterpreted as TIFF data.
     */
    @Test
    fun testRejectsMvtgWithUnknownEndianMarker() {

        val payload = ByteArray(32)

        payload[12] = 0x42

        val bytes = QuickTimeTestVideos.videoWithMvtgPayload(payload)

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(bytes)
        }
    }

}
