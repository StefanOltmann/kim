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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.GpsCoordinates
import de.stefan_oltmann.kim.model.LocationShown
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.xmp.XMPMeta
import de.stefan_oltmann.xmp.XMPMetaFactory
import de.stefan_oltmann.xmp.XMPRegionArea
import de.stefan_oltmann.xmp.XmpDate
import de.stefan_oltmann.xmp.XmpFaceRegion
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

class XmpWriterEdgeCasesTest {

    private lateinit var xmpMeta: XMPMeta

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
        xmpMeta = XMPMetaFactory.create()
    }

    @AfterTest
    fun tearDown() {
        Kim.defaultTimeZone = null
    }

    private fun apply(update: MetadataUpdate) {
        XmpWriter.updateXmp(
            xmpMeta = xmpMeta,
            update = update,
            writePackageWrapper = false
        )
    }

    /**
     * An unparseable existing packet fails with the documented exception
     * type. The xmpcore exception must not escape the public Kim API -
     * every write throws only ImageWriteException.
     */
    @Test
    fun testBrokenExistingPacketThrowsImageWriteException() {

        assertFailsWith<ImageWriteException> {
            XmpWriter.updateXmp(
                existingXmp = "<not-xmp/>",
                updates = setOf(MetadataUpdate.Title("x")),
                writePackageWrapper = true
            )
        }
    }

    @Test
    fun testUpdateRemovesDate() {

        apply(MetadataUpdate.TakenDate(0L))

        assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))

        apply(MetadataUpdate.TakenDate(null))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))
    }

    /**
     * External writers store both date properties, and the EXIF path
     * rewrites both on a TakenDate update - the XMP set branch must keep
     * the digitized date in sync instead of letting it drift behind.
     */
    @Test
    fun testUpdateSetsDateTimeDigitizedWithTakenDate() {

        xmpMeta.setProperty(XMP_NS_EXIF, "DateTimeOriginal", "2020:08:30 18:43:00")
        xmpMeta.setProperty(XMP_NS_EXIF, "DateTimeDigitized", "2020:08:30 18:43:00")

        apply(MetadataUpdate.TakenDate(1_689_166_125_401))

        /*
         * Both properties must carry the same rendered instant - the
         * exact string comes from the xmpcore serializer.
         */
        val original = assertNotNull(xmpMeta.getPropertyString(XMP_NS_EXIF, "DateTimeOriginal"))

        assertEquals(original, xmpMeta.getPropertyString(XMP_NS_EXIF, "DateTimeDigitized"))
    }

    /**
     * External writers store DateTimeOriginal AND DateTimeDigitized.
     * Removing the taken date must clear both, mirroring the EXIF write
     * path - a leftover digitized date contradicts the deletion.
     */
    @Test
    fun testUpdateRemovesDateTimeDigitized() {

        xmpMeta.setProperty(XMP_NS_EXIF, "DateTimeOriginal", "2020:08:30 18:43:00")
        xmpMeta.setProperty(XMP_NS_EXIF, "DateTimeDigitized", "2020:08:30 18:43:00")

        apply(MetadataUpdate.TakenDate(null))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeOriginal"))
        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "DateTimeDigitized"))
    }

    @Test
    fun testUpdateRemovesGpsCoordinates() {

        apply(
            MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661))
        )

        assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))

        apply(MetadataUpdate.GpsCoordinates(null))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))
    }

    /**
     * A GPS position never travels alone: external writers keep altitude,
     * timestamps and image direction next to it. A GPS update rewrites
     * the position, so the companions of the old position must go with
     * it - the EXIF write path removes every residual GPS field the same
     * way, and mixing the new coordinates with the old altitude would
     * drift the storages apart within one update.
     */
    @Test
    fun testUpdateDropsResidualGpsCompanions() {

        xmpMeta.setProperty(XMP_NS_EXIF, "GPSAltitude", "14/1")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSImgDirection", "270")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSTimeStamp", "12:00:00")

        apply(MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661)))

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSAltitude"))
        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSImgDirection"))
        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSTimeStamp"))

        assertNotNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))
    }

    /**
     * The companion cleanup must cover every exif:GPS property an
     * external writer may have set. Track, destination refs and the
     * measure mode describe the old position or the journey to it and
     * must not survive the location's removal.
     */
    @Test
    fun testUpdateRemovesEveryResidualGpsCompanion() {

        xmpMeta.setProperty(XMP_NS_EXIF, "GPSTrack", "270")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSTrackRef", "T")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSDestLatitudeRef", "N")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSDestLongitudeRef", "E")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSMeasureMode", "3")
        xmpMeta.setProperty(XMP_NS_EXIF, "GPSVersionID", "2.3.0.0")

        apply(MetadataUpdate.GpsCoordinates(null))

        for (propertyName in listOf(
            "GPSTrack",
            "GPSTrackRef",
            "GPSDestLatitudeRef",
            "GPSDestLongitudeRef",
            "GPSMeasureMode",
            "GPSVersionID"
        ))
            assertNull(
                xmpMeta.getProperty(XMP_NS_EXIF, propertyName),
                "Property $propertyName survived the removal"
            )
    }

    @Test
    fun testUpdateRemovesLocationShown() {

        apply(
            MetadataUpdate.LocationShown(
                LocationShown(
                    name = "Times Square",
                    street = null,
                    city = "New York",
                    state = "NY",
                    country = "USA"
                )
            )
        )

        assertNotNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))

        apply(MetadataUpdate.LocationShown(null))

        assertNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))
    }

    @Test
    fun testUpdateGpsCoordinatesAndLocationShownRemovesBoth() {

        apply(
            MetadataUpdate.GpsCoordinatesAndLocationShown(
                gpsCoordinates = null,
                locationShown = null
            )
        )

        assertNull(xmpMeta.getProperty(XMP_NS_EXIF, "GPSLatitude"))
        assertNull(xmpMeta.getProperty(XMP_NS_IPTC_EXT, "LocationShown"))
    }

    @Test
    fun testFlaggingResetsRejectedRating() {

        apply(MetadataUpdate.Rating(ExifRating.REJECTED))
        assertEquals(ExifRating.REJECTED.value, xmpMeta.getPropertyInteger(XMP_NS_XMP, "Rating"))

        /* Flagging a rejected photo resets the rating. */
        apply(MetadataUpdate.Flagged(true))

        assertEquals(ExifRating.UNRATED.value, xmpMeta.getPropertyInteger(XMP_NS_XMP, "Rating"))
    }

    @Test
    fun testRejectingRemovesFlag() {

        apply(MetadataUpdate.Flagged(true))
        apply(MetadataUpdate.Orientation(TiffOrientation.STANDARD))

        /* Rejecting a flagged photo removes the flag. */
        apply(MetadataUpdate.Rating(ExifRating.REJECTED))

        assertNull(xmpMeta.getPropertyBoolean(XMP_NS_XMP, "Flagged"))
    }

    /**
     * The XMP region list is an ordered array, so a write must keep regions
     * without a name and regions that share a name - collapsing them would
     * silently delete detected faces on the round-trip.
     */
    @Test
    fun testFacesRoundTripPreservesNamelessAndDuplicateRegions() {

        val regions = listOf(
            XmpFaceRegion("Swiper", XMPRegionArea(0.404336, 0.422313, 0.124503, 0.240097)),
            XmpFaceRegion("Swiper", XMPRegionArea(0.1, 0.2, 0.3, 0.4)),
            XmpFaceRegion(null, XMPRegionArea(0.5, 0.5, 0.2, 0.2))
        )

        apply(MetadataUpdate.Faces(regions, widthPx = 4390, heightPx = 2927))

        val serialized =
            XmpWriter.updateXmp(xmpMeta, emptySet(), writePackageWrapper = false)

        assertEquals(
            expected = regions,
            actual = XmpReader.readMetadata(serialized).faces
        )
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testUpdateWithSystemTimeZone() {

        Kim.defaultTimeZone = null

        try {
            apply(MetadataUpdate.TakenDate(0L))

            /*
             * Epoch 0 must map to 1970-01-01T00:00 in whatever the
             * platform time zone is, so the expected string is computed
             * from that very zone and asserted exactly. XMP Core renders
             * the canonical form, which always includes the seconds.
             */
            val expected = requireNotNull(
                XmpDate.parse(
                    kotlin.time.Instant.fromEpochMilliseconds(0)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                )
            ).toString()

            val actual: String? = xmpMeta
                .getPropertyString(XMP_NS_EXIF, "DateTimeOriginal")

            assertEquals(
                expected,
                actual
            )
        } finally {
            Kim.defaultTimeZone = null
        }
    }

    private companion object {

        const val XMP_NS_EXIF = "http://ns.adobe.com/exif/1.0/"
        const val XMP_NS_XMP = "http://ns.adobe.com/xap/1.0/"
        const val XMP_NS_IPTC_EXT = "http://iptc.org/std/Iptc4xmpExt/2008-02-29/"
    }
}
