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
package de.stefan_oltmann.kim.common

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.ktor.KimKtor
import de.stefan_oltmann.kim.ktor.readMetadata
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import io.ktor.utils.io.ByteReadChannel
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/*
 * The test is placed in androidHostTest, because the classes live in ktorMain.
 */

/**
 * Tests reading metadata via the ktor entry points.
 */
class KimKtorHostTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    @Test
    fun testReadMetadataFromByteReadChannel() {

        val bytes = KimTestData.getBytesOf(1)

        val metadata = KimKtor.readMetadata(
            byteReadChannel = ByteReadChannel(bytes),
            contentLength = bytes.size.toLong()
        )

        assertNotNull(metadata)
        assertEquals(MediaFormat.JPEG, metadata.mediaFormat)
    }

    @Test
    fun testKimExtensionFunction() {

        val bytes = KimTestData.getBytesOf(1)

        assertNotNull(Kim.readMetadata(ByteReadChannel(bytes), bytes.size.toLong()))
    }
}
