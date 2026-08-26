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
package de.stefan_oltmann.kim.android

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import kotlinx.datetime.TimeZone
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Host tests for the KimAndroid API, which must only throw
 * ImageReadException on failure.
 */
class KimAndroidHostTest {

    private var tempDirectory: File? = null

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
        tempDirectory = Files.createTempDirectory("kim").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDirectory?.deleteRecursively()
        Kim.defaultTimeZone = null
    }

    @Test
    fun testReadMetadataFromMissingFile() {

        assertFailsWith<ImageReadException> {
            KimAndroid.readMetadata(File("does-not-exist.jpg"))
        }
    }

    @Test
    fun testReadMetadataFromMissingPath() {

        assertFailsWith<ImageReadException> {
            KimAndroid.readMetadata("does-not-exist.jpg")
        }
    }

    @Test
    fun testReadMetadataFromDirectory() {

        assertFailsWith<ImageReadException> {
            KimAndroid.readMetadata(checkNotNull(tempDirectory))
        }
    }

    /**
     * Regression test: a failed update must leave the original photo
     * byte-identical, because the user would otherwise lose it.
     */
    @Test
    fun testFailedUpdateDoesNotDestroyOriginalFile() {

        val file = File(checkNotNull(tempDirectory), "photo.jpg")

        /* A truncated JPEG: the update cannot be applied to this. */
        val truncatedJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffd9" // EOI before SOS
        )

        file.writeBytes(truncatedJpeg)

        assertFailsWith<ImageWriteException> {
            KimAndroid.update(
                file = file,
                update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
            )
        }

        /* The original bytes must be untouched. */
        assertContentEquals(truncatedJpeg, file.readBytes())

        /* No temporary files may leak into the directory. */
        assertEquals(1, checkNotNull(checkNotNull(tempDirectory).listFiles()).size)
    }

    /**
     * A successful update replaces the content and leaves no temporary
     * files behind.
     */
    @Test
    fun testSuccessfulUpdateReplacesFileContent() {

        val file = File(checkNotNull(tempDirectory), "photo.jpg")

        val validJpeg = convertHexStringToByteArray(
            "ffd8" + // SOI
                "ffe00010" + "4a46494600010100000100010000" + // APP0 JFIF
                "ffda0008" + "010100003f00" + // SOS
                "11223344" + // image data
                "ffd9" // EOI
        )

        file.writeBytes(validJpeg)

        KimAndroid.update(
            file = file,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_LEFT)
        )

        /* The orientation must be applied to the file on disk. */
        val metadata = KimAndroid.readMetadata(file)

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.of(metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        )

        /* No temporary files may leak into the directory. */
        assertEquals(1, checkNotNull(checkNotNull(tempDirectory).listFiles()).size)
    }
}
