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

import de.stefan_oltmann.kim.common.ImageReadException
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Host tests for the KimAndroid API, which must only throw
 * ImageReadException on failure.
 */
class KimAndroidHostTest {

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

        val directory = Files.createTempDirectory("kim").toFile()

        assertFailsWith<ImageReadException> {
            KimAndroid.readMetadata(directory)
        }
    }
}
