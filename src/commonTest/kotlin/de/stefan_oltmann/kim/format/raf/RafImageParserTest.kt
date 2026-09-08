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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RafImageParserTest {

    /**
     * The RAF metadata parser is wired into Kim separately from the
     * extractor, so its parse must be covered directly: the EXIF of the
     * JPEG embedded in the RAF becomes the metadata of the file.
     */
    @Test
    fun testParseMetadataReadsTheEmbeddedJpegExif() {

        val bytes = KimTestData.getBytesOf(KimTestData.RAF_TEST_IMAGE_INDEX)

        val metadata = RafImageParser.parseMetadata(ByteArrayByteReader(bytes))

        assertEquals(MediaFormat.RAF, metadata.mediaFormat)

        assertNotNull(metadata.exif)
    }
}
