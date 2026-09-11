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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests reading TIFF container level metadata that is not part of the
 * EXIF structure.
 */
class TiffImageParserTest {

    /**
     * TIFF files (for example from scanners and IPTC editing tools) can
     * carry the IPTC IIM block in the IFD0 tag 0x83BB. Like ExifTool,
     * that block must be read into the IPTC metadata instead of being
     * ignored.
     */
    @Test
    fun testReadsIptcFromTiffTag() {

        val keywords = "test1".encodeToByteArray()

        val iptcDataset = byteArrayOf(
            0x1C.toByte(), 0x02, 0x19,
            0x00, 0x05
        ) + keywords

        val bytes = buildLittleEndianTiffWithUndefEntry(
            tag = TiffTag.TIFF_TAG_IPTC_NAA.tag,
            data = iptcDataset
        )

        val metadata = Kim.readMetadata(bytes)

        assertNotNull(metadata)

        assertEquals(
            expected = listOf(IptcRecord(IptcTypes.KEYWORDS, "test1")),
            actual = metadata.iptc?.records
        )
    }

    /**
     * Builds a little endian TIFF whose IFD0 holds a single UNDEF entry
     * that points to the given data behind the directory.
     */
    private fun buildLittleEndianTiffWithUndefEntry(
        tag: Int,
        data: ByteArray
    ): ByteArray {

        val headerSize = 8
        val directorySize = 2 + 12 + 4
        val dataOffset = headerSize + directorySize

        val bytes = ByteArray(dataOffset + data.size)

        /* TIFF header: byte order, magic, offset to IFD0. */
        bytes[0] = 'I'.code.toByte()
        bytes[1] = 'I'.code.toByte()
        bytes[2] = 0x2A
        bytes[3] = 0
        bytes[4] = headerSize.toByte()
        bytes[6] = 0

        /* IFD0 with a single entry. */
        bytes[8] = 1
        bytes[9] = 0

        bytes[10] = tag.toByte()
        bytes[11] = (tag shr 8).toByte()
        bytes[12] = 7
        bytes[13] = 0

        val count = data.size

        bytes[14] = count.toByte()
        bytes[15] = (count shr 8).toByte()
        bytes[16] = (count shr 16).toByte()
        bytes[17] = (count shr 24).toByte()

        bytes[18] = dataOffset.toByte()
        bytes[19] = (dataOffset shr 8).toByte()
        bytes[20] = (dataOffset shr 16).toByte()
        bytes[21] = (dataOffset shr 24).toByte()

        data.copyInto(bytes, dataOffset)

        return bytes
    }
}
