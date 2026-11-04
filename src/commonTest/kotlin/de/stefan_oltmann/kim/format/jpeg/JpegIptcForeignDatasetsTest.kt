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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.encodeToLatin1Bytes
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Datasets outside IPTC application record 2 - such as the envelope
 * record version ExifTool writes into every IPTC block - are part of
 * the file's metadata. An IPTC rewrite must carry them through
 * byte-exact instead of silently dropping them.
 */
class JpegIptcForeignDatasetsTest {

    /** Envelope record dataset number of Destination. */
    private val destinationDatasetNumber = 5

    /**
     * The value bytes of the envelope-record dataset with the given
     * number among the raw foreign datasets. Both fixtures use the
     * short length form, so the value starts behind the 5 header
     * bytes (tag marker, record number, dataset number, 2-byte size).
     */
    private fun envelopeDatasetValue(
        foreignDatasets: List<ByteArray>,
        datasetNumber: Int
    ): ByteArray {

        val dataset = foreignDatasets.single { candidate ->
            candidate.size > 4 &&
                candidate[0] == IptcConstants.IPTC_RECORD_TAG_MARKER.toByte() &&
                candidate[1] == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER.toByte() &&
                candidate[2] == datasetNumber.toByte()
        }

        return dataset.copyOfRange(5, dataset.size)
    }

    @Test
    fun testForeignIimDatasetsSurviveIptcRewrite() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_foreign_iptc_datasets.jpg"
        ).readBytes()

        val before = assertNotNull(assertNotNull(Kim.readMetadata(original)).iptc)

        /*
         * The ExifTool-written fixture carries the envelope record
         * version dataset (1:0), which must survive as foreign data.
         */
        assertTrue(before.foreignDatasets.isNotEmpty())

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        /*
         * Byte lists instead of byte arrays: array equality is
         * identity, and the rewrite re-parses a fresh buffer.
         */
        assertEquals(
            before.foreignDatasets.map { it.toList() },
            after.foreignDatasets.map { it.toList() }
        )
    }

    @Test
    fun testIptcDigestResourceCoversForeignDatasets() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_foreign_iptc_datasets.jpg"
        ).readBytes()

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        val digestResource = assertNotNull(
            after.nonIptcBlocks.singleOrNull { block ->
                block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST
            }
        )

        /*
         * The digest marks the IPTC data as in sync - it must hash
         * exactly the block bytes that were written, foreign datasets
         * included, or digest-aware tools report a mismatch.
         */
        assertEquals(
            Md5.digest(
                IptcWriter.writeIptcBlockData(after.records, after.foreignDatasets)
            ).toList(),
            digestResource.blockData.toList()
        )
    }

    /**
     * The rewrite emits a UTF-8 IPTC stream. A foreign text dataset
     * captured from a stream that declared no coded character set -
     * the IIM default charset is then Latin-1 - must be re-emitted
     * UTF-8-encoded, or every charset-honoring reader misdecodes it.
     */
    @Test
    fun testForeignLatin1TextDatasetIsTranscodedToUtf8() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_latin1_iptc_destination.jpg"
        ).readBytes()

        val before = assertNotNull(assertNotNull(Kim.readMetadata(original)).iptc)

        /*
         * Fixture precondition: ExifTool wrote the Destination with the
         * Latin-1 encoding of "München" and no CodedCharacterSet
         * dataset, so the stream declares no charset at all.
         */
        assertEquals(
            "München".encodeToLatin1Bytes().toList(),
            envelopeDatasetValue(before.foreignDatasets, destinationDatasetNumber).toList()
        )

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        /*
         * The rewritten stream declares UTF-8 - the preserved dataset
         * must carry the UTF-8 encoding of the same string, and the
         * only CodedCharacterSet dataset present is the envelope's
         * UTF-8 declaration.
         */
        val declarationsAfter = after.foreignDatasets.filter { candidate ->
            candidate.size > 2 &&
                candidate[0] == IptcConstants.IPTC_RECORD_TAG_MARKER.toByte() &&
                candidate[1] == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER.toByte() &&
                candidate[2] == IptcParser.CODED_CHARACTER_SET_IPTC_CODE.toByte()
        }

        assertEquals(
            listOf(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE.toList()),
            declarationsAfter.map { declaration ->
                declaration.copyOfRange(5, declaration.size).toList()
            }
        )

        assertEquals(
            "München".encodeToByteArray().toList(),
            envelopeDatasetValue(after.foreignDatasets, destinationDatasetNumber).toList()
        )
    }

    /**
     * An original CodedCharacterSet dataset that declares a non-UTF-8
     * charset misdescribes the UTF-8 stream the rewrite emits and
     * would flip the running charset of every dataset behind it. It is
     * superseded by the envelope the writer emits and must not survive
     * the rewrite, while the text datasets are transcoded.
     */
    @Test
    fun testStaleNonUtf8CharsetDeclarationIsSuperseded() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_latin1_iptc_and_charset_declaration.jpg"
        ).readBytes()

        val before = assertNotNull(assertNotNull(Kim.readMetadata(original)).iptc)

        /*
         * Fixture precondition: the declaration is the ISO 2022 escape
         * "ESC , A" (a Latin-1 designation, byte-exact 1B 2C 41) and
         * the Destination is Latin-1 encoded. The parser captures the
         * declaration like every other foreign dataset.
         */
        assertEquals(
            listOf(0x1B, 0x2C, 0x41),
            envelopeDatasetValue(
                before.foreignDatasets,
                IptcParser.CODED_CHARACTER_SET_IPTC_CODE
            ).map { it.toInt() and 0xFF }
        )

        assertEquals(
            "München".encodeToLatin1Bytes().toList(),
            envelopeDatasetValue(before.foreignDatasets, destinationDatasetNumber).toList()
        )

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        /*
         * The stale declaration is superseded: the rewritten stream
         * carries exactly one CodedCharacterSet dataset - the
         * envelope's UTF-8 declaration.
         */
        val declarationsAfter = after.foreignDatasets.filter { candidate ->
            candidate.size > 2 &&
                candidate[0] == IptcConstants.IPTC_RECORD_TAG_MARKER.toByte() &&
                candidate[1] == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER.toByte() &&
                candidate[2] == IptcParser.CODED_CHARACTER_SET_IPTC_CODE.toByte()
        }

        assertEquals(
            listOf(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE.toList()),
            declarationsAfter.map { staleDeclaration ->
                staleDeclaration.copyOfRange(5, staleDeclaration.size).toList()
            }
        )

        assertEquals(
            "München".encodeToByteArray().toList(),
            envelopeDatasetValue(after.foreignDatasets, destinationDatasetNumber).toList()
        )
    }
}
