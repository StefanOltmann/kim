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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * A naked JPEG XL codestream (starting with FF 0A) has no ISOBMFF boxes
 * and therefore cannot carry metadata. It must be detected as JXL,
 * answered with empty metadata on read and rejected with a clear error
 * on write attempts.
 */
class NakedJxlCodestreamTest {

    private val nakedCodestream = byteArrayOf(0xFF.toByte(), 0x0A.toByte()) +
        ByteArray(32)

    @Test
    fun testDetectReturnsJxl() {

        assertEquals(MediaFormat.JXL, MediaFormat.detect(nakedCodestream))
    }

    /**
     * Reading must not fail box parsing - it answers with empty metadata.
     */
    @Test
    fun testReadMetadataReturnsEmptyMetadata() {

        val metadata = Kim.readMetadata(nakedCodestream)

        assertNotNull(metadata)

        assertEquals(MediaFormat.JXL, metadata.mediaFormat)
        assertNull(metadata.exif)
        assertNull(metadata.xmp)
    }

    /**
     * Writing is not possible without an ISOBMFF container and must be
     * rejected instead of failing somewhere inside box parsing.
     */
    @Test
    fun testUpdateThrowsImageWriteException() {

        assertFailsWith<ImageWriteException> {
            Kim.update(
                bytes = nakedCodestream,
                update = MetadataUpdate.Title("test")
            )
        }
    }
}
