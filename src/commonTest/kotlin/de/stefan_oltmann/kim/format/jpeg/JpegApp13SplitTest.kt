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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcBlock
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcRecord
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Segment-splitting behavior of the APP13 Photoshop stream writer.
 */
class JpegApp13SplitTest {

    /**
     * A continuation segment that starts exactly on a resource block
     * boundary begins with the 8BIM signature - readers then treat it
     * as an independent Photoshop stream instead of a continuation, and
     * ExifTool skips such a tail entirely. The writer must move the
     * split into the previous block's data so every continuation starts
     * mid-block.
     */
    @Test
    fun testSplitApp13SegmentsAvoidBlockAlignedContinuations() {

        /*
         * Two opaque blocks whose combined size lands exactly on the
         * second segment stride: 2 * 65519 = 131038 bytes.
         */
        val firstBlock = IptcBlock(0x040F, byteArrayOf(), ByteArray(65506))
        val secondBlock = IptcBlock(0x0410, byteArrayOf(), ByteArray(65508))

        val iptcMetadata = IptcMetadata(
            records = listOf(IptcRecord(IptcTypes.KEYWORDS, "aligned")),
            rawBlocks = listOf(firstBlock, secondBlock)
        )

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(KimTestData.getBytesOf(1)),
            byteWriter = byteWriter,
            metadata = iptcMetadata
        )

        val newBytes = byteWriter.toByteArray()

        /* No continuation segment may start with the 8BIM signature. */
        var seenFirstApp13 = false
        var index = 2

        while (index < newBytes.size - 4) {

            if (newBytes[index] == 0xFF.toByte() &&
                newBytes[index + 1] == JpegConstants.JPEG_APP13_MARKER.toByte()
            ) {
                val segmentLength =
                    ((newBytes[index + 2].toInt() and 0xFF) shl 8) or
                        (newBytes[index + 3].toInt() and 0xFF)

                if (seenFirstApp13) {

                    val payloadStart = index + 4 + JpegConstants.APP13_IDENTIFIER.size

                    val startsWithBlockSignature = newBytes
                        .getRemainingBytes(payloadStart)
                        .startsWith(
                            byteArrayOf(0x38.toByte(), 0x42.toByte(), 0x49.toByte(), 0x4D.toByte())
                        )

                    assertFalse(
                        startsWithBlockSignature,
                        "A continuation segment starts with the 8BIM signature at $index."
                    )
                }

                seenFirstApp13 = true

                index += 2 + segmentLength
            } else {
                index++
            }
        }

        /* The complete metadata must still survive the round trip. */
        val roundTripKeywords = Kim.readMetadata(newBytes)?.iptc?.records
            ?.filter { record -> record.iptcType == IptcTypes.KEYWORDS }
            ?.map { record -> record.value }

        assertEquals(listOf("aligned"), roundTripKeywords)
    }
}
