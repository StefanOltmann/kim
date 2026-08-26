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
package de.stefan_oltmann.kim.format

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import de.stefan_oltmann.kim.model.ExifRating
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

/*
 * The test is placed in jvmTest, because JvmInputStreamByteReader is a
 * one-shot stream that cannot be read twice. If an updater read the input
 * more than once or buffered it, the output would differ from the
 * ByteArray-based update.
 */
class StreamingUpdateTest {

    private val timestamp = 1_689_166_125_401 // 2023:07:12 14:48:45 in GMT+02:00

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @Test
    fun testPngStreamingUpdateMatchesByteArrayUpdate() {

        assertStreamingMatchesByteArrayUpdate("png")
    }

    @Test
    fun testGifStreamingUpdateMatchesByteArrayUpdate() {

        assertStreamingMatchesByteArrayUpdate("gif")
    }

    @Test
    fun testJxlStreamingUpdateMatchesByteArrayUpdate() {

        assertStreamingMatchesByteArrayUpdate("jxl")
    }

    /*
     * The WebP format cannot stream the image data, because the RIFF size
     * field sits at the start of the file while its value depends on the
     * metadata chunks at the end. The update still works on a one-shot
     * stream, it just buffers the chunks in memory.
     */
    @Test
    fun testWebpUpdateViaStreamMatchesByteArrayUpdate() {

        assertStreamingMatchesByteArrayUpdate("webp")
    }

    @Test
    fun testDeleteMetadataViaStreamMatchesByteArray() {

        for (format in listOf("jpg", "png", "gif", "jxl", "webp")) {

            val bytes = Resource("de/stefan_oltmann/kim/updates_$format/original.$format").readBytes()

            val byteArrayDelete = Kim.deleteMetadata(bytes)

            val byteWriter = ByteArrayByteWriter()

            Kim.deleteMetadata(
                byteReader = JvmInputStreamByteReader(
                    inputStream = bytes.inputStream(),
                    contentLength = bytes.size.toLong()
                ),
                byteWriter = byteWriter
            )

            assertContentEquals(byteArrayDelete, byteWriter.toByteArray())
        }
    }

    private fun assertStreamingMatchesByteArrayUpdate(format: String) {

        val bytes = Resource("de/stefan_oltmann/kim/updates_$format/original.$format").readBytes()

        val updates = setOf(
            MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
            MetadataUpdate.TakenDate(timestamp),
            MetadataUpdate.Title("Süße Vögelchen"),
            MetadataUpdate.Description("Äußerst süße Vögel fliegen durch die Lüfte."),
            MetadataUpdate.Keywords(setOf("hello", "test")),
            MetadataUpdate.Rating(ExifRating.FOUR_STARS)
        )

        val byteArrayUpdate = Kim.update(
            bytes = bytes,
            updates = updates
        )

        val byteWriter = ByteArrayByteWriter()

        Kim.update(
            byteReader = JvmInputStreamByteReader(
                inputStream = bytes.inputStream(),
                contentLength = bytes.size.toLong()
            ),
            byteWriter = byteWriter,
            updates = updates
        )

        assertContentEquals(byteArrayUpdate, byteWriter.toByteArray())
    }
}
