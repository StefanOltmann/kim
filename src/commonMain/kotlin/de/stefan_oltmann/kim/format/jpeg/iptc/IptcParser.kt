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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcTypes.Companion.getIptcType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readByte
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipToQuad
import kotlin.jvm.JvmStatic

/**
 * Parses IPTC data from JPEG APP13 segments.
 */
public object IptcParser {

    internal val EMPTY_BYTE_ARRAY = byteArrayOf()

    /**
     * The record header is the record number, the record type and
     * the 2-byte size field.
     */
    private const val IPTC_RECORD_HEADER_BYTE_COUNT = 4

    /**
     * Block types (or Image Resource IDs) that are not recommended to be
     * interpreted when libraries process Photoshop IPTC metadata.
     *
     * See https://www.adobe.com/devnet-apps/photoshop/fileformatashtml/
     */
    @Suppress("MagicNumber")
    private val PHOTOSHOP_IGNORED_BLOCK_TYPE = listOf(1084, 1085, 1086, 1087)

    public const val CODED_CHARACTER_SET_IPTC_CODE: Int = 90

    /* "ESC % G" as bytes */
    public val UTF8_CHARACTER_ESCAPE_SEQUENCE: ByteArray =
        byteArrayOf('\u001B'.code.toByte(), '%'.code.toByte(), 'G'.code.toByte())

    public val APP13_BYTE_ORDER: ByteOrder = ByteOrder.BIG_ENDIAN

    /**
     * Checks if the ByteArray starts with the Photoshop identification header.
     * This is mandatory for IPTC embedded into APP13.
     *
     * The check is limited to the identifier, because Photoshop data that
     * spans multiple APP13 segments continues mid-resource in the following
     * segments.
     */
    @JvmStatic
    public fun isPhotoshopApp13Segment(segmentData: ByteArray): Boolean =
        segmentData.startsWith(JpegConstants.APP13_IDENTIFIER)

    /**
     * Parses IPTC from the given string.
     *
     * @param bytes                 The IPTC bytes
     * @param startsWithApp13Header If IPTC is read from JPEG the header is required.
     */
    @JvmStatic
    public fun parseIptc(
        bytes: ByteArray,
        startsWithApp13Header: Boolean = true
    ): IptcMetadata {

        val records = mutableListOf<IptcRecord>()

        val blocks = parseAllIptcBlocks(bytes, startsWithApp13Header)

        for (block in blocks) {

            /* Ignore everything but IPTC data. */
            if (!block.isIPTCBlock())
                continue

            records.addAll(parseIPTCBlock(block.blockData))
        }

        return IptcMetadata(records, blocks)
    }

    private fun parseIPTCBlock(bytes: ByteArray): List<IptcRecord> {

        var isUtf8 = false

        val records = mutableListOf<IptcRecord>()

        var index = 0

        @Suppress("LoopWithTooManyJumpStatements")
        while (index + 1 < bytes.size) {

            val tagMarker = bytes[index++].toUInt8()

            /* We look after the IPTC record tag marker to read. */
            if (tagMarker != IptcConstants.IPTC_RECORD_TAG_MARKER)
                continue

            /*
             * The truncated tail of the block may not hold the record
             * number, type and size. Stop instead of reading past the end.
             */
            if (index + IPTC_RECORD_HEADER_BYTE_COUNT > bytes.size)
                break

            val recordNumber = bytes[index++].toUInt8()
            val recordType = bytes[index++].toUInt8()

            var recordSize = bytes.toUInt16(index, APP13_BYTE_ORDER)
            index += 2

            /*
             * The IPTC extended-length encoding: a length above 32767 means the
             * 2-byte field holds the marker 0x8000 and the actual size follows
             * as a 4-byte value.
             */
            if (recordSize > IptcConstants.IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE) {

                if (index + IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_SIZE > bytes.size)
                    return records

                recordSize = bytes.toInt(index, APP13_BYTE_ORDER)
                index += IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_SIZE

                if (recordSize < 0)
                    return records
            }

            val recordData = bytes.slice(index, recordSize)

            index += recordSize

            if (recordNumber == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER &&
                recordType == CODED_CHARACTER_SET_IPTC_CODE
            ) {
                isUtf8 = isUtf8(recordData)
                continue
            }

            if (recordNumber != IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)
                continue

            if (recordType == 0)
                continue

            records.add(
                IptcRecord(
                    iptcType = getIptcType(recordType),
                    value = if (isUtf8)
                        recordData.decodeToString()
                    else
                        recordData.decodeLatin1BytesToString()
                )
            )
        }

        return records
    }

    private fun parseAllIptcBlocks(
        bytes: ByteArray,
        startsWithApp13Header: Boolean
    ): List<IptcBlock> {

        val blocks = mutableListOf<IptcBlock>()

        val byteReader = ByteArrayByteReader(bytes)

        if (startsWithApp13Header) {

            val idString = byteReader.readBytes(
                "App13 Segment identifier",
                JpegConstants.APP13_IDENTIFIER.size
            )

            if (!JpegConstants.APP13_IDENTIFIER.contentEquals(idString))
                throw ImageReadException(
                    "Not a Photoshop App13 segment: ${idString.contentToString()} " +
                        " != " + JpegConstants.APP13_IDENTIFIER.contentToString()
                )
        }

        @Suppress("LoopWithTooManyJumpStatements")
        while (true) {

            if (!byteReader.skipToNextResourceBlock())
                break

            val blockType = byteReader.readNextNonIgnoredBlockType()
                ?: break

            val blockNameLength = byteReader.readByte("block name length").toInt()

            var blockNameBytes: ByteArray

            if (blockNameLength == 0) {

                byteReader.readByte("empty name")
                blockNameBytes = EMPTY_BYTE_ARRAY

            } else {

                blockNameBytes = try {
                    byteReader.readBytes("block name bytes", blockNameLength)
                } catch (ignore: ImageReadException) {
                    break
                }

                if (blockNameLength % 2 == 0)
                    byteReader.readByte("block name padding byte")
            }

            val blockSize = byteReader.read4BytesAsInt("block size", APP13_BYTE_ORDER)

            /*
             * Note: This doesn't catch cases where blocksize is invalid but is still less
             * than "bytes.size", but will at least prevent OutOfMemory errors.
             */
            if (blockSize > bytes.size)
                throw ImageReadException("Invalid Block Size : " + blockSize + " > " + bytes.size)

            val blockData: ByteArray = try {
                byteReader.readBytes("block data", blockSize)
            } catch (ignore: ImageReadException) {
                break
            }

            blocks.add(IptcBlock(blockType, blockNameBytes, blockData))

            /*
             * The padding byte of an odd-sized block can be missing at
             * the end of the data. The block itself is complete, so we
             * keep it and stop parsing.
             */
            if (blockSize % 2 != 0) {

                try {
                    byteReader.readByte("block data padding byte")
                } catch (_: ImageReadException) {
                    break
                }
            }
        }

        return blocks
    }

    /**
     * Positions the reader right after the next 8BIM resource block
     * signature, skipping invalid markers in between.
     *
     * Returns false at the end of the data.
     */
    private fun ByteReader.skipToNextResourceBlock(): Boolean {

        val resourceBlockSignature: Int = try {
            read4BytesAsInt("Image Resource Block Signature", APP13_BYTE_ORDER)
        } catch (ignore: ImageReadException) {
            return false
        }

        if (resourceBlockSignature == JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)
            return true

        /*
         * Some files seem to contain invalid markers: 04 3A 00 00 in case of our test data.
         * We just ignore these and skip to the next 8BIM (38 42 49 4D) segment.
         * If we can't skip to the next we found everything we can interpret.
         */
        return skipToQuad(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)
    }

    /**
     * Reads the block type of the next 8BIM resource block, skipping
     * blocks that the photoshop spec recommends to ignore.
     *
     * The skip consumes the next block's signature, so the block type
     * of the following block is read directly here instead of reading
     * a signature again.
     *
     * Returns null at the end of the data.
     */
    private fun ByteReader.readNextNonIgnoredBlockType(): Int? {

        var blockType = read2BytesAsInt("IPTC block type", APP13_BYTE_ORDER)

        while (PHOTOSHOP_IGNORED_BLOCK_TYPE.contains(blockType)) {

            /*
             * If there is still data in this block, before the next image resource block (8BIM),
             * then we must consume these bytes to leave a pointer ready to read the next block.
             *
             * These block types are skipped because the Photoshop
             * specification classifies them as non-IPTC resources (like
             * resolution or print flag information). They are never part
             * of the IPTC metadata this parser is responsible for, and
             * they remain untouched in the raw block bytes.
             */
            val skipSuccessful = skipToQuad(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

            if (!skipSuccessful)
                return null

            blockType = read2BytesAsInt("IPTC block type", APP13_BYTE_ORDER)
        }

        return blockType
    }

    private fun isUtf8(codedCharset: ByteArray): Boolean {

        /*
         * The record value may be padded with spaces, so they are
         * stripped before comparing against the escape sequence.
         */
        val significantBytes = codedCharset
            .filter { it != ' '.code.toByte() }
            .toByteArray()

        return UTF8_CHARACTER_ESCAPE_SEQUENCE.contentEquals(significantBytes)
    }
}
