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
package de.stefan_oltmann.kim.jvm

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import java.io.File
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/*
 * The test is placed in jvmTest, because KimJvm lives in jvmMain.
 */
class KimJvmTest {

    private val jpegBytes: ByteArray =
        KimTestData.getBytesOf(1)

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    @Test
    fun testReadMetadataFromInputStream() {

        val metadata = KimJvm.readMetadata(
            inputStream = jpegBytes.inputStream(),
            length = jpegBytes.size.toLong()
        )

        assertNotNull(metadata)
        assertEquals(MediaFormat.JPEG, metadata.mediaFormat)
    }

    @Test
    fun testReadMetadataFromPathString() {

        val diskPath = Resource("src/commonTest/resources/de/stefan_oltmann/kim/testdata/full/media_1.jpg").path

        val metadata = KimJvm.readMetadata(diskPath)

        assertNotNull(metadata)
        assertEquals(MediaFormat.JPEG, metadata.mediaFormat)
    }

    @Test
    fun testReadMetadataFromFile() {

        val diskPath = Resource("src/commonTest/resources/de/stefan_oltmann/kim/testdata/full/media_1.jpg").path

        val metadata = KimJvm.readMetadata(File(diskPath))

        assertNotNull(metadata)
        assertEquals(MediaFormat.JPEG, metadata.mediaFormat)
    }

    @Test
    fun testReadMetadataFromPath() {

        val diskPath = Resource("src/commonTest/resources/de/stefan_oltmann/kim/testdata/full/media_1.jpg").path

        val metadata = KimJvm.readMetadata(Files.createTempFile("kim", ".jpg"))

        /* A non-image file has no metadata. */
        assertNull(metadata)
    }

    @Test
    fun testReadMetadataFromMissingFile() {

        assertFailsWith<ImageReadException> {
            KimJvm.readMetadata(File("does-not-exist.jpg"))
        }

        assertFailsWith<ImageReadException> {
            KimJvm.readMetadata(Files.createTempFile("kim", ".jpg").resolveSibling("missing.jpg"))
        }
    }

    @Test
    fun testReadMetadataFromDirectory() {

        val directory = Files.createTempDirectory("kim").toFile()

        assertFailsWith<ImageReadException> {
            KimJvm.readMetadata(directory)
        }
    }

    @Test
    fun testKimExtensionFunctions() {

        val metadata = Kim.readMetadata(jpegBytes.inputStream(), jpegBytes.size.toLong())

        assertNotNull(metadata)

        val diskPath = Resource("src/commonTest/resources/de/stefan_oltmann/kim/testdata/full/media_1.jpg").path

        assertNotNull(Kim.readMetadata(diskPath))
        assertNotNull(Kim.readMetadata(File(diskPath)))
        assertNotNull(Kim.readMetadata(java.nio.file.Path.of(diskPath)))
    }
}
