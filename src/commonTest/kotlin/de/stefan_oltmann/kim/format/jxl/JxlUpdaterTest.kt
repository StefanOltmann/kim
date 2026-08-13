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
package de.stefan_oltmann.kim.format.jxl

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.AbstractUpdaterTest
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class JxlUpdaterTest : AbstractUpdaterTest("jxl") {

    /**
     * Metadata boxes behind the codestream must not survive an update,
     * otherwise the file ends up with stale duplicates.
     */
    @Test
    fun testUpdateJxlpTrailingMetadataIsStripped() {

        val updatedBytes = Kim.update(
            bytes = createJxlpFileWithTrailingMetadata(),
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        /* The new Exif box is written, the stale ones behind the codestream are dropped. */
        assertEquals(1, updatedBytes.countOccurrences("Exif"))
        assertFalse(updatedBytes.decodeToString().contains(STALE_XMP))
    }

    /**
     * Metadata boxes behind the codestream must be removed by
     * deleteMetadata, or deleted location data would remain in the file.
     */
    @Test
    fun testDeleteMetadataJxlpTrailingMetadataIsRemoved() {

        val deletedBytes = Kim.deleteMetadata(createJxlpFileWithTrailingMetadata())

        assertEquals(0, deletedBytes.countOccurrences("Exif"))
        assertFalse(deletedBytes.decodeToString().contains(STALE_XMP))
    }

    /**
     * Builds a jxlp file whose Exif and xml boxes come after the codestream
     * boxes, the layout the spec recommends.
     */
    private fun createJxlpFileWithTrailingMetadata(): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        /* The header box with index zero and the codestream signature. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 0, 0xFF.toByte(), 0x0A)
        )

        /* A data box, which triggers the streaming cut. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLP,
            payload = byteArrayOf(0, 0, 0, 1, 1, 2, 3, 4)
        )

        /* Metadata behind the codestream. */
        writeBox(
            byteWriter = byteWriter,
            type = BoxType.EXIF,
            payload = byteArrayOf(0, 0, 0, 0) + byteArrayOf(0x49, 0x49, 0x2A, 0, 8, 0, 0, 0)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.XML,
            payload = STALE_XMP.encodeToByteArray()
        )

        return byteWriter.toByteArray()
    }

    private fun writeBox(byteWriter: ByteArrayByteWriter, type: BoxType, payload: ByteArray) {

        byteWriter.writeInt(payload.size + 8, BMFF_BYTE_ORDER)
        byteWriter.write(type.bytes)
        byteWriter.write(payload)
    }

    private fun ByteArray.countOccurrences(needle: String): Int {

        val needleBytes = needle.encodeToByteArray()

        var count = 0

        for (index in 0..size - needleBytes.size) {

            var matches = true

            for (needleIndex in needleBytes.indices)
                if (this[index + needleIndex] != needleBytes[needleIndex]) {

                    matches = false

                    break
                }

            if (matches)
                count++
        }

        return count
    }

    private companion object {

        const val STALE_XMP: String = "stale xmp"
    }
}
