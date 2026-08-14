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
package de.stefan_oltmann.kim.format.tiff

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.format.tiff.constant.GeoTiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

class GeoTiffUpdateTest {

    private val resourcePath: String = "de/stefan_oltmann/kim/updates_tif"

    private val originalBytes = Resource("$resourcePath/empty.tif").readBytes()

    private val expectedBytes = Resource("$resourcePath/geotiff.tif").readBytes()

    @Test
    fun testSetGeoTiff() {

        val tiffContents = TiffReader.read(
            byteReader = ByteArrayByteReader(originalBytes),
            readTiffImageBytes = true
        )

        val outputSet: TiffOutputSet = tiffContents.createOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
            doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
        )

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
            doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
        )

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
            shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
        )

        val byteWriter = ByteArrayByteWriter()

        val writer = TiffWriter(outputSet.byteOrder)

        writer.write(
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        val actualBytes = byteWriter.toByteArray()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals) {

            Path("build/geotiff.tif")
                .writeBytes(actualBytes)

            fail("geotiff.tif has not the expected bytes!")
        }
    }
}
