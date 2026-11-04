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
import de.stefan_oltmann.kim.common.decodeLatin1BytesToString
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser.APP13_BYTE_ORDER
import de.stefan_oltmann.kim.output.BinaryByteWriter
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.jvm.JvmStatic

/**
 * Writes IPTC data into JPEG APP13 segments.
 */
public object IptcWriter {

    /** The record number of the IIM NewsPhoto record. */
    private const val NEWS_PHOTO_RECORD_NUMBER = 3

    /** Envelope record dataset number of Destination. */
    private const val ENVELOPE_DESTINATION_DATASET = 5

    /** Envelope record dataset number of ServiceIdentifier. */
    private const val ENVELOPE_SERVICE_IDENTIFIER_DATASET = 30

    /** Envelope record dataset number of ProductID. */
    private const val ENVELOPE_PRODUCT_ID_DATASET = 50

    /** Envelope record dataset number of TimeSent. */
    private const val ENVELOPE_TIME_SENT_DATASET = 80

    /** Envelope record dataset number of UniqueObjectName. */
    private const val ENVELOPE_UNIQUE_OBJECT_NAME_DATASET = 100

    /** NewsPhoto record dataset number of IPTCPictureNumber. */
    private const val NEWS_PHOTO_PICTURE_NUMBER_DATASET = 10

    /**
     * The size of the dataset header in the short length form: tag
     * marker, record number, dataset number and the 2-byte size field.
     */
    private const val FOREIGN_DATASET_SHORT_HEADER_SIZE = 5

    /**
     * The envelope record dataset numbers the IIM specification
     * defines as string form. Their bytes follow the charset the
     * stream declares.
     */
    private val ENVELOPE_STRING_DATASETS = setOf(
        ENVELOPE_DESTINATION_DATASET,
        ENVELOPE_SERVICE_IDENTIFIER_DATASET,
        ENVELOPE_PRODUCT_ID_DATASET,
        ENVELOPE_TIME_SENT_DATASET,
        ENVELOPE_UNIQUE_OBJECT_NAME_DATASET
    )

    /**
     * The parts of a raw foreign dataset the re-emission logic needs:
     * which record and dataset it belongs to, the value start and the
     * length the header declares.
     */
    private class ForeignDatasetHeader(
        val recordNumber: Int,
        val datasetNumber: Int,
        val dataStartIndex: Int,
        val declaredLength: Long
    )

    @Suppress("ThrowsCount")
    @JvmStatic
    /**
     * Writes the given IPTC blocks, optionally prefixed with the 8BIM
     * Photoshop APP13 identifier.
     *
     * @throws ImageWriteException for block types or names outside the
     *         ranges the format defines.
     */
    @Throws(ImageWriteException::class)
    public fun writeIptcBlocks(
        blocks: List<IptcBlock>,
        includeApp13Identifier: Boolean = true
    ): ByteArray {

        val os = ByteArrayByteWriter()

        val bos: BinaryByteWriter = BinaryByteWriter.createBinaryByteWriter(os, APP13_BYTE_ORDER)

        if (includeApp13Identifier)
            bos.write(JpegConstants.APP13_IDENTIFIER)

        for (block in blocks) {

            bos.write4Bytes(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

            if (block.blockType < 0 || block.blockType > IptcConstants.MAX_IPTC_BLOCK_TYPE)
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

    /**
     * Encodes the given records into IPTC application record 2 data,
     * written in UTF-8 with the coded-character-set envelope set.
     *
     * The [foreignDatasets] are re-emitted after the record 2 datasets
     * in their original order - datasets outside record 2 cannot be
     * regenerated from the model, so dropping them would be data loss.
     * The CodedCharacterSet datasets among them are the one exception:
     * the envelope written first supersedes any original declaration,
     * so they are left out instead of duplicating or contradicting it.
     *
     * When the original stream did not declare UTF-8 - the IIM default
     * charset is then Latin-1 - the preserved string datasets are
     * transcoded from Latin-1 to UTF-8: the stream written here
     * declares UTF-8, and Latin-1 bytes inside it would be misdecoded
     * by every charset-honoring reader. All other foreign datasets -
     * binary record content and digit datasets - are re-emitted
     * byte-exact.
     *
     * The IIM stream model does not require ascending dataset order
     * for readers.
     *
     * @throws ImageWriteException for record types outside the range the
     *         format defines.
     */
    @Throws(ImageWriteException::class)
    public fun writeIptcBlockData(
        records: List<IptcRecord>,
        foreignDatasets: List<ByteArray> = emptyList()
    ): ByteArray {

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
        /*  record version record size */
        binaryWriter.write2Bytes(2)
        binaryWriter.write2Bytes(IptcConstants.IPTC_RECORD_VERSION_VALUE)

        /* Write the IPTC records in order. */
        for ((iptcType, value) in records.sorted()) {

            /* Ignore the record version, because we already wrote it. */
            if (iptcType === IptcTypes.RECORD_VERSION)
                continue

            binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
            binaryWriter.write(IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)

            if (iptcType.type < 0 || iptcType.type > IptcConstants.MAX_IPTC_RECORD_TYPE)
                throw ImageWriteException("Invalid record type: " + iptcType.type)

            binaryWriter.write(iptcType.type)

            writeDatasetLengthAndValue(binaryWriter, value.encodeToByteArray())
        }

        for (foreignDataset in prepareForeignDatasetsForUtf8Stream(foreignDatasets))
            binaryWriter.write(foreignDataset)

        return byteWriter.toByteArray()
    }

    /**
     * Writes the dataset length field and value. The dataset length
     * field is 2 bytes. Up to 32767 bytes the size is written directly.
     * Larger datasets use the IPTC extended-length encoding: the length
     * field holds the marker 0x8000 combined with the number of bytes
     * of the length field that follows (4, like ExifTool writes it),
     * and that field holds the actual size. A 2-byte truncation would
     * corrupt the block.
     */
    private fun writeDatasetLengthAndValue(
        binaryWriter: BinaryByteWriter,
        recordData: ByteArray
    ) {

        if (recordData.size > IptcConstants.IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE) {

            binaryWriter.write2Bytes(
                IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_MARKER or
                    IptcConstants.IPTC_EXTENDED_LENGTH_FIELD_SIZE
            )
            binaryWriter.write4Bytes(recordData.size)

        } else {

            binaryWriter.write2Bytes(recordData.size)
        }

        binaryWriter.write(recordData)
    }

    /**
     * Prepares the captured foreign datasets for re-emission inside the
     * UTF-8 stream [writeIptcBlockData] produces.
     *
     * Original CodedCharacterSet datasets are left out: the envelope
     * the caller writes in front of the stream supersedes the
     * declaration, and re-emitting it would duplicate the declaration
     * on every rewrite. When the original stream last declared a
     * non-UTF-8 charset - or no charset at all, which makes the IIM
     * default Latin-1 apply - the string datasets are transcoded from
     * Latin-1 to UTF-8 so their bytes match the declaration the stream
     * carries. Everything else survives byte-exact.
     */
    private fun prepareForeignDatasetsForUtf8Stream(
        foreignDatasets: List<ByteArray>
    ): List<ByteArray> {

        val originalDeclaresUtf8 = foreignDatasetsDeclareUtf8(foreignDatasets)

        return foreignDatasets.mapNotNull { dataset ->

            val header = parseForeignDatasetHeader(dataset) ?: return@mapNotNull dataset

            when {

                isCodedCharacterSetDataset(header) -> null

                !originalDeclaresUtf8 && isForeignStringDataset(header) ->
                    transcodeForeignStringDataset(dataset, header)

                else -> dataset
            }
        }
    }

    /**
     * Whether the last CodedCharacterSet dataset among the foreign
     * datasets declares UTF-8. Like the running charset of the parser,
     * the last declaration wins; without any declaration the IIM
     * default charset Latin-1 applies.
     */
    private fun foreignDatasetsDeclareUtf8(foreignDatasets: List<ByteArray>): Boolean {

        var declaresUtf8 = false

        for (dataset in foreignDatasets) {

            val header = parseForeignDatasetHeader(dataset) ?: continue

            if (isCodedCharacterSetDataset(header))
                declaresUtf8 = IptcParser.isUtf8(datasetValue(dataset, header))
        }

        return declaresUtf8
    }

    /**
     * Reads the header of a raw foreign dataset captured by the
     * parser. Returns NULL when the bytes do not form a dataset header
     * the writer understands - such bytes are re-emitted verbatim
     * instead of being touched.
     */
    private fun parseForeignDatasetHeader(dataset: ByteArray): ForeignDatasetHeader? {

        /*
         * The tag marker, record number, dataset number and the 2-byte
         * length field are the smallest header.
         */
        if (dataset.size < FOREIGN_DATASET_SHORT_HEADER_SIZE)
            return null

        if (dataset[0].toUInt8() != IptcConstants.IPTC_RECORD_TAG_MARKER)
            return null

        val recordNumber = dataset[1].toUInt8()
        val datasetNumber = dataset[2].toUInt8()

        val lengthField = dataset.toUInt16(3, APP13_BYTE_ORDER)

        val dataStartIndex: Int
        val declaredLength: Long

        if (lengthField and IptcConstants.IPTC_EXTENDED_RECORD_LENGTH_MARKER != 0) {

            val lengthFieldSize = (lengthField and IptcConstants.IPTC_EXTENDED_LENGTH_SIZE_MASK)
                .takeIf { it != 0 }
                ?: IptcConstants.IPTC_EXTENDED_LENGTH_FIELD_SIZE

            /* A size beyond the format maximum is not a header we read. */
            if (lengthFieldSize > IptcConstants.IPTC_MAX_EXTENDED_LENGTH_FIELD_SIZE)
                return null

            dataStartIndex = FOREIGN_DATASET_SHORT_HEADER_SIZE + lengthFieldSize

            /*
             * The bytes must reach behind the length field - a dataset
             * that ends inside the header is not one we read. The check
             * stands before the length field is read.
             */
            if (dataStartIndex > dataset.size)
                return null

            var extendedLength = 0L

            for (offset in 0 until lengthFieldSize) {
                extendedLength =
                    (extendedLength shl Byte.SIZE_BITS) or
                        dataset[FOREIGN_DATASET_SHORT_HEADER_SIZE + offset].toUInt8().toLong()
            }

            declaredLength = extendedLength

        } else {

            dataStartIndex = FOREIGN_DATASET_SHORT_HEADER_SIZE
            declaredLength = lengthField.toLong()
        }

        return ForeignDatasetHeader(recordNumber, datasetNumber, dataStartIndex, declaredLength)
    }

    /**
     * The value bytes of a foreign dataset, from the position the
     * header declares to the end of the captured bytes.
     */
    private fun datasetValue(dataset: ByteArray, header: ForeignDatasetHeader): ByteArray =
        dataset.slice(header.dataStartIndex, dataset.size - header.dataStartIndex)

    /**
     * Whether the dataset is a 1:90 CodedCharacterSet dataset. The
     * envelope [writeIptcBlockData] writes supersedes it.
     */
    private fun isCodedCharacterSetDataset(header: ForeignDatasetHeader): Boolean =
        header.recordNumber == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER &&
            header.datasetNumber == IptcParser.CODED_CHARACTER_SET_IPTC_CODE

    /**
     * Whether the foreign dataset is one whose value the IIM
     * specification defines as string form: the envelope record
     * Destination, ServiceIdentifier, ProductID, TimeSent and
     * UniqueObjectName datasets, and the NewsPhoto IPTCPictureNumber
     * dataset. Their bytes follow the charset the stream declares.
     * Binary record content (the ObjectData SubFile among them) and
     * digit datasets are never touched.
     */
    private fun isForeignStringDataset(header: ForeignDatasetHeader): Boolean =
        when (header.recordNumber) {
            IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER ->
                header.datasetNumber in ENVELOPE_STRING_DATASETS
            NEWS_PHOTO_RECORD_NUMBER ->
                header.datasetNumber == NEWS_PHOTO_PICTURE_NUMBER_DATASET
            else -> false
        }

    /**
     * Re-emits the given foreign string dataset with its value
     * transcoded from Latin-1 to UTF-8, rebuilding the header for the
     * new value length. Datasets whose bytes do not end exactly where
     * the header declares are re-emitted verbatim - a value that
     * cannot be interpreted cleanly is never touched.
     */
    private fun transcodeForeignStringDataset(
        dataset: ByteArray,
        header: ForeignDatasetHeader
    ): ByteArray {

        if (header.dataStartIndex + header.declaredLength != dataset.size.toLong())
            return dataset

        val utf8Data = datasetValue(dataset, header)
            .decodeLatin1BytesToString()
            .encodeToByteArray()

        val byteWriter = ByteArrayByteWriter()

        val binaryWriter = BinaryByteWriter.createBinaryByteWriter(byteWriter, APP13_BYTE_ORDER)

        binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
        binaryWriter.write(header.recordNumber)
        binaryWriter.write(header.datasetNumber)

        writeDatasetLengthAndValue(binaryWriter, utf8Data)

        binaryWriter.flush()

        return byteWriter.toByteArray()
    }

    /**
     * Encodes the metadata as the Photoshop image resource block
     * structure (8BIM) without the APP13 identifier: the form both the
     * JPEG APP13 segment and the PNG "Raw profile type iptc" text
     * chunk carry. Non-IPTC resources - the IPTCDigest marker among
     * them - are re-emitted, so a rewrite never drops them.
     *
     * @throws ImageWriteException for record types outside the range the
     *         format defines.
     */
    @JvmStatic
    @Throws(ImageWriteException::class)
    public fun writeIptcResourceBlocks(metadata: IptcMetadata): ByteArray {

        val newBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = writeIptcBlockData(metadata.records, metadata.foreignDatasets)
        )

        return writeIptcBlocks(
            blocks = metadata.nonIptcBlocks + newBlock,
            includeApp13Identifier = false
        )
    }
}
