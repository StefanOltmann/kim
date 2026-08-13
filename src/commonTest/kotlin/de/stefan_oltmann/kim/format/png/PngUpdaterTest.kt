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
package de.stefan_oltmann.kim.format.png

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PngUpdaterTest : AbstractUpdaterTest("png") {

    private val originalBytes: ByteArray =
        Resource("de/stefan_oltmann/kim/updates_png/original.png").readBytes()

    /**
     * Verifies that deleting the metadata removes the EXIF and text chunks,
     * but keeps the iCCP chunk that affects how the image is displayed.
     */
    @Test
    fun testDeleteMetadataKeepsIccChunk() {

        val newBytes = Kim.deleteMetadata(originalBytes)

        val chunkTypes = chunkTypes(newBytes)

        /* The ICC profile affects the display and must be kept. */
        assertTrue("iCCP" in chunkTypes)

        /* The EXIF chunk must be removed. */
        assertFalse("eXIf" in chunkTypes)

        /* The text chunks carry XMP, IPTC and comments. */
        assertFalse("tEXt" in chunkTypes)
        assertFalse("zTXt" in chunkTypes)
        assertFalse("iTXt" in chunkTypes)

        /* The modification time is metadata, too. */
        assertFalse("tIME" in chunkTypes)
    }

    /**
     * Returns the types of all chunks of the given PNG bytes.
     */
    private fun chunkTypes(pngBytes: ByteArray): Set<String> {

        val chunkTypes = mutableSetOf<String>()

        var offset = PngConstants.PNG_SIGNATURE.size

        while (offset + 12 <= pngBytes.size) {

            val length = (pngBytes[offset].toInt() and 0xFF) shl 24 or
                (pngBytes[offset + 1].toInt() and 0xFF) shl 16 or
                (pngBytes[offset + 2].toInt() and 0xFF) shl 8 or
                (pngBytes[offset + 3].toInt() and 0xFF)

            val chunkType = pngBytes.copyOfRange(offset + 4, offset + 8).decodeToString()

            chunkTypes.add(chunkType)

            if (chunkType == "IEND")
                break

            offset += 12 + length
        }

        return chunkTypes
    }
}
