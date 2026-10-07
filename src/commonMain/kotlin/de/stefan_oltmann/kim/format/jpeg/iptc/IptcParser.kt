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
import de.stefan_oltmann.kim.common.decodeStrictUtf8
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants.IPTC_WORD_SIZE
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

    /** An 8BIM resource block signature is a 4-byte word. */
    private const val BLOCK_SIGNATURE_LENGTH = 4

    internal val EMPTY_BYTE_ARRAY = byteArrayOf()

    /**
     * The smallest record header after the tag marker: record number,
     * dataset number and the 2-byte size field.
     */
    private const val IPTC_MIN_HEADER_TAIL_BYTE_COUNT = 4

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
    ): IptcMetadata = tryWithImageReadException {

        val records = mutableListOf<IptcRecord>()
        val foreignDatasets = mutableListOf<ByteArray>()

        val blocks = parseAllIptcBlocks(bytes, startsWithApp13Header)

        for (block in blocks) {
            /* Ignore everything but IPTC data. */
            if (!block.isIPTCBlock())
                continue

            val content = parseIPTCBlock(detectWordSwap(block.blockData))

            records.addAll(content.records)
            foreignDatasets.addAll(content.foreignDatasets)
        }

        IptcMetadata(records, blocks, foreignDatasets = foreignDatasets)
    }

    /**
     * Some broken writers store IPTC with 32-bit word swapped bytes, so
     * the marker ends up at index 3 of every word. Like ExifTool, such
     * data is detected and the words are swapped back before parsing.
     */
    private fun detectWordSwap(bytes: ByteArray): ByteArray {

        if (bytes.size < IPTC_WORD_SIZE)
            return bytes

        val startsWithMarker =
            bytes[0].toUInt8() == IptcConstants.IPTC_RECORD_TAG_MARKER

        val markerAtIndex3 =
            bytes[IPTC_WORD_SIZE - 1].toUInt8() == IptcConstants.IPTC_RECORD_TAG_MARKER

        if (startsWithMarker || !markerAtIndex3)
            return bytes

        /* Like ExifTool, the data is padded to full 32-bit words. */
        val paddedSize = bytes.size + (IPTC_WORD_SIZE - bytes.size % IPTC_WORD_SIZE) % IPTC_WORD_SIZE

        val result = ByteArray(paddedSize)

        for (group in 0 until paddedSize step IPTC_WORD_SIZE) {
            for (offset in 0 until IPTC_WORD_SIZE) {
                val source = group + offset
                result[group + (IPTC_WORD_SIZE - 1 - offset)] =
                    if (source < bytes.size) bytes[source] else 0
            }
        }

        return result
    }

    /**
     * Parses a raw IPTC IIM dataset stream, for example from the TIFF
     * tag 0x83BB, that is not wrapped in a Photoshop image resource block.
     */
    @JvmStatic
    public fun parseIptcDataset(bytes: ByteArray): IptcMetadata =
        tryWithImageReadException {
            val content = parseIPTCBlock(detectWordSwap(bytes))

            IptcMetadata(
                records = content.records,
                rawBlocks = emptyList(),
                foreignDatasets = content.foreignDatasets
            )
        }

    private class BlockContent(
        val records: List<IptcRecord>,
        val foreignDatasets: List<ByteArray>
    )

    private fun parseIPTCBlock(bytes: ByteArray): BlockContent {

        var isUtf8 = false

        val records = mutableListOf<IptcRecord>()
        val foreignDatasets = mutableListOf<ByteArray>()

        var index = 0

        @Suppress("LoopWithTooManyJumpStatements")
        while (index + 1 < bytes.size) {

            val datasetStartIndex = index

            val tagMarker = bytes[index++].toUInt8()

            /* We look after the IPTC record tag marker to read. */
            if (tagMarker != IptcConstants.IPTC_RECORD_TAG_MARKER)
                continue

            /*
             * The truncated tail of the block may not hold the record
             * number, type and size. Stop instead of reading past the end.
             */
            if (index + IPTC_MIN_HEADER_TAIL_BYTE_COUNT > bytes.size)
                break

            val recordNumber = bytes[index++].toUInt8()
            val recordType = bytes[index++].toUInt8()

            val recordSize = bytes.toUInt16(index, APP13_BYTE_ORDER)
            index += 2

            /*
             * The IPTC extended-length encoding: when the high bit of the
             * length field is set, its remaining 15 bits hold the size of
             * the length field that follows (1 to 8 bytes), and that field
             * holds the actual length. Like ExifTool, any field size in
             * that range is read instead of assuming exactly four bytes.
             */
            var recordLength = recordSize.toLong()

            if (recordSize and IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_MARKER != 0) {

                /*
                 * The low 15 bits hold the size of the length field that
                 * follows (1 to 8 bytes, ExifTool writes 4). A marker of
                 * exactly 0x8000 is the legacy variant written by older Kim
                 * versions with a 4-byte length field behind it.
                 */
                val lengthFieldSize = (recordSize and IptcConstants.IPTC_EXTENDED_LENGTH_SIZE_MASK)
                    .takeIf { it != 0 }
                    ?: IptcConstants.IPTC_EXTENDED_LENGTH_FIELD_SIZE

                /*
                 * A length field size beyond the defined maximum is a
                 * corrupt header, not a truncated one.
                 */
                if (lengthFieldSize > IptcConstants.IPTC_MAX_EXTENDED_LENGTH_FIELD_SIZE)
                    throw ImageReadException(
                        "IPTC record declares an invalid length field " +
                            "size of $lengthFieldSize bytes."
                    )

                /*
                 * The block ends inside the length field: the declared
                 * structure ends here, so parsing keeps what was read
                 * so far (the clean boundary case).
                 */
                if (index + lengthFieldSize > bytes.size)
                    return BlockContent(records, foreignDatasets)

                recordLength = 0

                for (offset in 0 until lengthFieldSize) {
                    recordLength =
                        (recordLength shl Byte.SIZE_BITS) or bytes[index + offset].toUInt8().toLong()
                }

                index += lengthFieldSize
            }

            /*
             * The record length is file-controlled. A length larger than
             * the remaining block bytes means the record header lies - the
             * block itself is complete. Throwing keeps the strict-read
             * guarantee: a graceful stop would drop this record and every
             * record behind it from the rewrite unheard of. The index
             * overflow protection is a consequence of the throw.
             *
             * The 8-byte length field carries unsigned 64-bit values, so a
             * length with the sign bit set is negative here - without the
             * check it would slip past the too-large guard and silently
             * swallow the record.
             */
            if (recordLength < 0 || recordLength > bytes.size - index)
                throw ImageReadException(
                    "IPTC record declares $recordLength bytes, but only " +
                        "${bytes.size - index} remain in the block."
                )

            val recordData = bytes.slice(index, recordLength.toInt())

            index += recordLength.toInt()

            if (recordNumber == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER &&
                recordType == CODED_CHARACTER_SET_IPTC_CODE
            ) {
                isUtf8 = isUtf8(recordData)
                continue
            }

            /*
             * Datasets outside application record 2 (envelope identifiers,
             * NewsPhoto data) are kept as raw bytes, so an IPTC rewrite can
             * carry them through instead of silently dropping them. The
             * slice spans from the tag marker to the value end, so it is
             * exact regardless of the length encoding the writer chose.
             */
            if (recordNumber != IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER) {

                foreignDatasets.add(
                    bytes.slice(datasetStartIndex, index - datasetStartIndex)
                )

                continue
            }

            if (recordType == 0)
                continue

            /*
             * Datasets the IIM specification defines as binary (rasterized
             * caption, objectData preview) have no text form: re-encoding
             * them through a String would corrupt every byte >= 0x80 and
             * grow the dataset, so like the datasets outside record 2 they
             * are carried through as raw bytes.
             */
            if (recordType == IptcTypes.RASTERIZED_CAPTION.type ||
                recordType == IptcTypes.OBJECT_DATA_PREVIEW_DATA.type
            ) {

                foreignDatasets.add(
                    bytes.slice(datasetStartIndex, index - datasetStartIndex)
                )

                continue
            }

            records.add(
                IptcRecord(
                    iptcType = getIptcType(recordType),
                    value = if (isUtf8)
                        recordData.decodeStrictUtf8("An UTF-8 flagged IPTC record")
                    else
                        recordData.decodeLatin1BytesToString()
                )
            )
        }

        return BlockContent(records, foreignDatasets)
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

            val blockType = readTolerantly { byteReader.readNextNonIgnoredBlockType() } ?: break

            val blockNameLength = readTolerantly { byteReader.readByte("block name length").toUInt8() }
                ?: break

            val blockNameBytes: ByteArray

            if (blockNameLength == 0) {

                readTolerantly { byteReader.readByte("empty name") } ?: break

                blockNameBytes = EMPTY_BYTE_ARRAY

            } else {

                blockNameBytes = readTolerantly { byteReader.readBytes("block name bytes", blockNameLength) }
                    ?: break

                if (blockNameLength % 2 == 0) {

                    readTolerantly { byteReader.readByte("block name padding byte") } ?: break
                }
            }

            val blockSize = readTolerantly { byteReader.read4BytesAsInt("block size", APP13_BYTE_ORDER) }
                ?: break

            /*
             * Note: This doesn't catch cases where blocksize is invalid but is still less
             * than "bytes.size", but will at least prevent OutOfMemory errors.
             * A size with the sign bit set is negative and must fail like an
             * oversized size, or it would silently truncate the parse below.
             */
            if (blockSize < 0 || blockSize > bytes.size)
                throw ImageReadException("Invalid Block Size : " + blockSize + " > " + bytes.size)

            val blockData: ByteArray = readTolerantly { byteReader.readBytes("block data", blockSize) }
                ?: break

            blocks.add(IptcBlock(blockType, blockNameBytes, blockData))

            /*
             * The padding byte of an odd-sized block can be missing at
             * the end of the data. The block itself is complete, so we
             * keep it and stop parsing.
             */
            if (blockSize % 2 != 0) {

                readTolerantly { byteReader.readByte("block data padding byte") } ?: break
            }
        }

        return blocks
    }

    /**
     * Runs one read of the tolerant APP13 block walk.
     *
     * The data can end anywhere, e.g. after a truncated write. Like the
     * tolerated EOF inside the block data, every read that hits the end
     * stops the parse gracefully and keeps the blocks found so far.
     */
    private inline fun <T> readTolerantly(read: () -> T): T? =
        try {
            read()
        } catch (_: ImageReadException) {
            null
        }

    /**
     * Positions the reader right after the next 8BIM resource block
     * signature, skipping invalid markers in between.
     *
     * Returns false at the end of the data.
     */
    private fun ByteReader.skipToNextResourceBlock(): Boolean {

        val signatureBytes = readBytes(BLOCK_SIGNATURE_LENGTH)

        /*
         * Fewer than 4 bytes means the data ended mid-signature - like
         * every other EOF truncation this stops the parse gracefully.
         */
        if (signatureBytes.size < BLOCK_SIGNATURE_LENGTH)
            return false

        val resourceBlockSignature = signatureBytes.toInt(APP13_BYTE_ORDER)

        if (resourceBlockSignature == JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)
            return true

        /*
         * Some files seem to contain invalid markers: 04 3A 00 00 in case
         * of our test data. When another 8BIM (38 42 49 4D) follows, the
         * junk between the blocks is skipped and parsing continues.
         *
         * When no 8BIM follows, the tail is unparseable content that an
         * update would rebuild without it - destroying the bytes. Per the
         * strict read policy this fails instead.
         */
        if (!skipToQuad(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT))
            throw ImageReadException("Unparseable Photoshop APP13 tail.")

        return true
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
