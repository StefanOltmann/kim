/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.jpeg.iptc

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser.APP13_BYTE_ORDER
import de.stefan_oltmann.kim.output.BigEndianBinaryByteWriter
import de.stefan_oltmann.kim.output.BinaryByteWriter
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.jvm.JvmStatic

public object IptcWriter {

    @Suppress("ThrowsCount")
    @JvmStatic
    public fun writeIptcBlocks(
        blocks: List<IptcBlock>,
        includeApp13Identifier: Boolean = true
    ): ByteArray {

        val os = ByteArrayByteWriter()

        val bos: BinaryByteWriter = BigEndianBinaryByteWriter(os)

        if (includeApp13Identifier)
            bos.write(JpegConstants.APP13_IDENTIFIER)

        for (block in blocks) {

            bos.write4Bytes(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

            if (block.blockType < 0 || block.blockType > 0xFFFF)
                throw ImageWriteException("Invalid IPTC block type: ${block.blockType}")

            bos.write2Bytes(block.blockType)

            val blockNameBytes = block.blockNameBytes

            if (blockNameBytes.size > JpegConstants.IPTC_MAX_BLOCK_NAME_LENGTH)
                throw ImageWriteException("IPTC block name is too long: " + blockNameBytes.size)

            bos.write(blockNameBytes.size)
            bos.write(blockNameBytes)

            /* Pad to even size, including length byte */
            if (blockNameBytes.size % 2 == 0)
                bos.write(0)

            val blockData = block.blockData

            bos.write4Bytes(blockData.size)
            bos.write(blockData)

            /* Pad to even size */
            if (blockData.size % 2 == 1)
                bos.write(0)
        }

        bos.flush()

        return os.toByteArray()
    }

    /* Writes the IPTC block in UTF-8 */
    public fun writeIptcBlockData(records: List<IptcRecord>): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        val binaryWriter = BinaryByteWriter.createBinaryByteWriter(byteWriter, APP13_BYTE_ORDER)

        /*
         * Set the envelope for UTF-8
         *
         * It's recommended to use UTF-8 as a default to prevent issues with umlauts.
         * And it's more consistent to always write data the same way.
         */
        binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
        binaryWriter.write(IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER)
        binaryWriter.write(IptcParser.CODED_CHARACTER_SET_IPTC_CODE)
        binaryWriter.write2Bytes(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE.size)
        binaryWriter.write(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE)

        /* First, right record version record */
        binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
        binaryWriter.write(IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)
        binaryWriter.write(IptcTypes.RECORD_VERSION.type)
        binaryWriter.write2Bytes(2) // record version record size
        binaryWriter.write2Bytes(IptcConstants.IPTC_RECORD_VERSION_VALUE)

        /**
         * Write the IPTC records in order.
         */
        for ((iptcType, value) in records.sorted()) {

            /* Ignore the record version, because we already wrote it. */
            if (iptcType === IptcTypes.RECORD_VERSION)
                continue

            binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
            binaryWriter.write(IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)

            if (iptcType.type < 0 || iptcType.type > 0xFF)
                throw ImageWriteException("Invalid record type: " + iptcType.type)

            binaryWriter.write(iptcType.type)

            val recordData = value.encodeToByteArray()

            /*
             * The dataset length field is 2 bytes. Up to 32767 bytes the size is
             * written directly. Larger datasets use the IPTC extended-length
             * encoding: the length field holds the marker 0x8000 and the actual
             * size follows as a 4-byte value. A 2-byte truncation would corrupt
             * the block.
             */
            if (recordData.size > IptcConstants.IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE) {

                binaryWriter.write2Bytes(IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_MARKER)
                binaryWriter.write4Bytes(recordData.size)

            } else {

                binaryWriter.write2Bytes(recordData.size)
            }

            binaryWriter.write(recordData)
        }

        return byteWriter.toByteArray()
    }
}
