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
import kotlinx.datetime.TimeZone
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

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
     * The read closes the given stream, like the core Kim API documents.
     * Callers relying on the opposite contract would reuse a closed
     * stream and fail with unexpected IOExceptions.
     */
    @Test
    fun testReadMetadataClosesTheProvidedStream() {

        val stream = object : java.io.ByteArrayInputStream(byteArrayOf(1, 2, 3)) {
            var closed = false

            override fun close() {
                closed = true
                super.close()
            }
        }

        /* Unknown bytes report no metadata, but the stream is consumed. */
        KimAndroid.readMetadata(stream, length = 3)

        assertTrue(stream.closed)
    }

    /**
     * A failing size lookup must not leak the already-opened stream:
     * the length is resolved before the stream opens, like
     * KimJvm.readMetadataFrom documents. A provider without a SIZE
     * column makes the query throw - one file descriptor would leak
     * per failing call if the stream were opened first.
     */
    @Test
    fun testCreateByteReaderFromOpensNoStreamWhenSizeLookupFails() {

        assertFailsWith<ImageReadException> {
            KimAndroid.createByteReaderFrom(
                sizeLookup = { throw IllegalArgumentException("Provider has no SIZE column.") },
                openStream = { fail("The stream must not be opened when the size lookup fails.") }
            )
        }
    }
}
