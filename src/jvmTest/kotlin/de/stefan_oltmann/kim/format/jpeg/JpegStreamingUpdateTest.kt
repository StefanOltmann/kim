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
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/*
 * The test is placed in jvmTest, because JvmInputStreamByteReader is a
 * one-shot stream that cannot be read twice. If the JPEG updater read the
 * input more than once or buffered it, the output would differ from the
 * ByteArray-based update.
 */
class JpegStreamingUpdateTest {

    private val jpegBytes: ByteArray =
        KimTestData.getBytesOf(1)

    private val timestamp = 1_689_166_125_401 // 2023:07:12 14:48:45 in GMT+02:00

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    @Test
    fun testStreamingOrientationUpdateMatchesByteArrayUpdate() {

        val updates = setOf(
            MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        assertContentEquals(
            expected = updateViaByteArray(updates),
            actual = updateViaStream(updates)
        )
    }

    @Test
    fun testStreamingMultipleUpdatesMatchByteArrayUpdate() {

        val updates = setOf(
            MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
            MetadataUpdate.TakenDate(timestamp),
            MetadataUpdate.Title("Süße Vögelchen"),
            MetadataUpdate.Description("Äußerst süße Vögel fliegen durch die Lüfte."),
            MetadataUpdate.Keywords(setOf("hello", "test")),
            MetadataUpdate.Rating(ExifRating.FOUR_STARS)
        )

        assertContentEquals(
            expected = updateViaByteArray(updates),
            actual = updateViaStream(updates)
        )
    }

    @Test
    fun testStreamingUpdateWritesValidJpeg() {

        val result = updateViaStream(
            setOf(MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT))
        )

        val metadata = Kim.readMetadata(result)!!

        assertEquals(
            TiffOrientation.ROTATE_RIGHT.value.toShort(),
            metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)
        )
    }

    private fun updateViaByteArray(updates: Set<MetadataUpdate>): ByteArray =
        Kim.update(
            bytes = jpegBytes,
            updates = updates
        )

    private fun updateViaStream(updates: Set<MetadataUpdate>): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        Kim.update(
            byteReader = JvmInputStreamByteReader(
                inputStream = jpegBytes.inputStream(),
                contentLength = jpegBytes.size.toLong()
            ),
            byteWriter = byteWriter,
            updates = updates
        )

        return byteWriter.toByteArray()
    }
}
